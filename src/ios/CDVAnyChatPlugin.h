//
//  CDVAnyChatPlugin.h
//  Video
//
//  Created by yanqiong on 2/29/16.
//
//

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import "VideoChatController.h"

#ifndef CDVAnyChatPlugin_h
#define CDVAnyChatPlugin_h

@class VideoChatController;

@interface CDVAnyChatPlugin : CDVPlugin <VideoFinishDelegate>{
    
    IBOutlet VideoChatController* videoChatController;
}

@property (nonatomic, copy) VideoChatController * videoChatController;
@property (nonatomic, copy) NSString* callbackID;


- (void) startChat:(CDVInvokedUrlCommand*)command;

@end

@interface CDVAnyChatOptions : NSObject

@property NSString * serverIp;
@property NSString * userName;
@property NSNumber * serverPort;
@property NSNumber * roomId;
@property NSNumber * remoteUserid;
@property NSString * loginPassword;
@property NSString * enterroomPassword;

+ (instancetype) createFromStartChatArguments:(CDVInvokedUrlCommand*)command;

@end

#endif /* CDVAnyChatPlugin_h */
