//
//  TiVonageVideo.swift
//  ti.vonage
//
//  Created by Hans Knöchel
//  Copyright (c) 2022 Hans Knöchel. All rights reserved.
//

import TitaniumKit
import OpenTok

@objc(TiVonageVideo)
public class TiVonageVideo : TiUIView {
  
  public var videoView: UIView?

  public override func frameSizeChanged(_ frame: CGRect, bounds: CGRect) {
    super.frameSizeChanged(frame, bounds: bounds)
    
    if let videoView = videoView {
      if self.subviews.isEmpty {
        self.addSubview(videoView)
      }

      TiUtils.setView(videoView, positionRect: bounds)
    }
  }
}
