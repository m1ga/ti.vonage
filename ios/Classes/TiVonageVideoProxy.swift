//
//  TiVonageVideoProxy.swift
//  ti.vonage
//
//  Created by Hans Knöchel
//  Copyright (c) 2022 Hans Knöchel. All rights reserved.
//

import TitaniumKit
import OpenTok

@objc(TiVonageVideoProxy)
public class TiVonageVideoProxy : TiViewProxy {
  
  public func _init(withPageContext context: TiEvaluator!, videoView: UIView) -> Self! {
    super._init(withPageContext: context)
    
    self.publisherView.videoView = videoView
    
    return self
  }

  lazy var publisherView: TiVonageVideo = {
    return self.view as! TiVonageVideo
  }()
}
