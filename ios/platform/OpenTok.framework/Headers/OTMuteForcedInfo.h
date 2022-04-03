//
//  OTMuteForcedInfo.h
//  OpenTok
//
//  Created by Tamir Nahum on 01/11/2020.
//  Copyright © 2020 TokBox. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * Defines the <code>muteForced</code> parameter of the
 * <[OTSessionDelegate session:muteForced:]> message.
 */
@interface OTMuteForcedInfo : NSObject

/**
 * When set to <code>YES</code>, the moderator has muted streams in the session;
 * when set to <code>NO</code>, a moderator has disabled the mute state
 * in the session.
 */
@property(nonatomic, assign) BOOL active;

@end

NS_ASSUME_NONNULL_END
