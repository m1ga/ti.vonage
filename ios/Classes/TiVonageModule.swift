//
//  TiVonageModule.swift
//  ti.vonage
//
//  Created by Hans Knöchel
//  Copyright (c) 2022 Hans Knöchel. All rights reserved.
//

import UIKit
import TitaniumKit
import OpenTok

@objc(TiVonageModule)
class TiVonageModule: TiModule {

  var session: OTSession!

  var publisher: OTPublisher?

  var subscriber: OTSubscriber?

  var _apiKey: String?

  var _sessionId: String?

  var _token: String?
  
  var _audioOnly: Bool = false

  func moduleGUID() -> String {
    return "8669e6e4-ff3a-4a19-b85a-ead686c4c18c"
  }
  
  override func moduleId() -> String! {
    return "ti.vonage"
  }

  @objc(initialize:)
  func initialize(arguments: Array<Any>?) {
    // TODO: Require some permissions?
    fireEvent("ready")
  }

  @objc(connect:)
  func connect(unused: Any?) {
    guard let apiKey = _apiKey, let sessionId = _sessionId, let token = _token else {
      NSLog("[ERROR] Missing apiKey, sessionId or token property! Please set before calling \"connect()\"")
      return
    }

    session = OTSession(apiKey: apiKey, sessionId: sessionId, delegate: self)
    var error: OTError?
    session?.connect(withToken: token, error: &error)

    if let error = error {
      NSLog("[ERROR] Error connecting: \(error.localizedDescription)")
    }
  }
  
  @objc(disconnect:)
  func disconnect(unused: Any) {
    if let session = session {
      var error: OTError?
      session.disconnect(&error)
      
      if let error = error {
        NSLog("[ERROR] Error disconnecting: \(error.localizedDescription)")
      }
    }
  }

  @objc(setApiKey:)
  func setApiKey(apiKey: String) {
    _apiKey = apiKey
    replaceValue(apiKey, forKey: "apiKey", notification: false)
  }
  
  @objc(apiKey)
  func apiKey() -> String? {
    return _apiKey
  }
  
  @objc(setSessionId:)
  func setSessionId(sessionId: String) {
    _sessionId = sessionId
    replaceValue(sessionId, forKey: "sessionId", notification: false)
  }
  
  @objc(sessionId)
  func sessionId() -> String? {
    return _sessionId
  }
  
  @objc(setToken:)
  func setToken(token: String) {
    _token = token
    replaceValue(token, forKey: "token", notification: false)
  }
  
  @objc(token)
  func token() -> String? {
    return _token
  }
  
  @objc(setAudioOnly:)
  func setAudioOnly(audioOnly: Any) {
    if let audioOnly = audioOnly as? Bool {
      _audioOnly = audioOnly
      replaceValue(audioOnly, forKey: "audioOnly", notification: false)
    }
  }

  @objc(audioOnly)
  func audioOnly() -> Any {
    return _audioOnly
  }
}

// MARK: OTSessionDelegate

extension TiVonageModule : OTSessionDelegate {

  func session(_ session: OTSession, didFailWithError error: OTError) {
    if error.code == 1022 {
      fireEvent("streamDropped")
    } else {
      fireEvent("sessionError")
    }
  }
  
  func sessionDidConnect(_ session: OTSession) {
    let settings = OTPublisherSettings()
    settings.name = UIDevice.current.name
    settings.videoTrack = !_audioOnly;

    guard let publisher = OTPublisher(delegate: self, settings: settings) else {
        return
    }

    var error: OTError?
    session.publish(publisher, error: &error)

    guard error == nil else {
        print(error!)
        return
    }

    guard let publisherView = publisher.view else {
        return
    }
    publisherView.frame = CGRect(x: 0, y: 0, width: 512, height: 512)

    let viewProxy = TiVonageVideoProxy()._init(withPageContext: executionContext,
                                               videoView: publisherView)
    
    let event: [String: Any] = [
      "view": viewProxy!,
      "userType": "published"
    ]

    fireEvent("streamReceived", with: event)
  }
  
  func sessionDidDisconnect(_ session: OTSession) {
    fireEvent("disconnected")
  }
  
  func session(_ session: OTSession, receivedSignalType type: String?, from connection: OTConnection?, with string: String?) {
    // TODO: Fire an event here as well?
  }
  
  func session(_ session: OTSession, streamCreated stream: OTStream) {
    subscriber = OTSubscriber(stream: stream, delegate: self)
    guard let subscriber = subscriber else {
        return
    }

    var error: OTError?
    session.subscribe(subscriber, error: &error)
    guard error == nil else {
        print(error!)
        return
    }

    guard let subscriberView = subscriber.view else {
        return
    }
    subscriberView.frame = UIScreen.main.bounds

    let viewProxy = TiVonageVideoProxy()._init(withPageContext: pageContext,
                                               videoView: subscriberView)
    
    let event: [String: Any] = [
      "view": viewProxy!,
      "userType": "subscriber",
      "streamId": stream.streamId,
      "connectionData": stream.connection.data ?? "",
      "connectionId": stream.connection.connectionId,
      "connectionCreationTime": stream.connection.creationTime
    ]
  
    fireEvent("streamReceived", with: event)
  }
  
  func session(_ session: OTSession, streamDestroyed stream: OTStream) {
    fireEvent("streamDropped", with: ["type": "subscriber", "streamId": stream.streamId ])
  }
}

// MARK: OTPublisherDelegate

extension TiVonageModule : OTPublisherDelegate {

  func publisher(_ publisher: OTPublisherKit, didFailWithError error: OTError) {
    if error.code == 1022 {
      fireEvent("streamDropped")
    } else {
      fireEvent("error", with: ["message": error.localizedDescription])
    }
  }
  
  func publisher(_ publisher: OTPublisherKit, streamCreated stream: OTStream) {
    fireEvent("streamCreated", with: ["streamId": stream.streamId ])
  }
  
  func publisher(_ publisher: OTPublisherKit, streamDestroyed stream: OTStream) {
    fireEvent("streamDestroyed", with: ["type": "publisher", "streamId": stream.streamId ])
  }
}

// MARK: OTSubscriberKitDelegate

extension TiVonageModule : OTSubscriberKitDelegate {

  func subscriberDidConnect(toStream subscriber: OTSubscriberKit) {
    // TODO: Fire an event here as well?
  }

  func subscriber(_ subscriber: OTSubscriberKit, didFailWithError error: OTError) {
    if error.code == 1022 {
      fireEvent("streamDropped")
    }
    // TODO: Fire "error" event here as well?
  }
}
