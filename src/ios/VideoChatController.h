//
//  VideoChatController.h
//  AnyChat
//
//  Created by bairuitech on 11-11-22.
//  Copyright 2011年 __MyCompanyName__. All rights reserved.
//

#define kLocalVideo_Width                   120.0f
#define kLocalVideo_Height                  160.0f
#define kUI_Spacing                         15.0f
#define kLocalVideoPortrait_CGRect          CGRectMake(15, 305, 120, 160)
#define kLocalVideoLandscape_CGRect         CGRectMake(15, 185, 160, 120)


#define SCREEN_WIDTH  [[UIScreen mainScreen] bounds].size.width
#define SCREEN_HEIGHT [[UIScreen mainScreen] bounds].size.height

//#define kSelfView_Width                     self.view.frame.size.width
//#define kSelfView_Height                    self.view.frame.size.height

#define kSelfView_Width   [[UIScreen mainScreen] bounds].size.width
#define kSelfView_Height  [[UIScreen mainScreen] bounds].size.height
#define kRadians(degrees)                   M_PI / 180.0 * degrees
#define kLayer3DRotation_Z_Axis(degrees)    CATransform3DMakeRotation(kRadians(degrees), 0.0, 0.0, 1.0)


#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#include <AVFoundation/AVFoundation.h>

#import "AnyChatPlatform.h"
#import "AnyChatDefine.h"


@class VideoChatController;
@protocol VideoFinishDelegate
-(void) doFinishVideo: (NSInteger * ) resultCode;
@end


@interface VideoChatController : UIViewController <AnyChatNotifyMessageDelegate> {
    
        IBOutlet UIImageView *remoteVideoSurface;
    
    //创建视频显示层全局变量
    AVCaptureVideoPreviewLayer*localVideoSurface;
    
    
    int iRemoteUserId;
    
    //新建核心类对象
    AnyChatPlatform *anychat;
}

@property (nonatomic, assign) id delegate;

@property NSString * serverIp;
@property NSString * userName;
@property int serverPort;
@property int roomId;
@property int remoteUserid;
@property NSString * loginPassword;
@property NSString * enterroomPassword;

@property (weak, nonatomic) IBOutlet UIView                 *theLocalView;
@property (nonatomic, strong) AVCaptureVideoPreviewLayer    *localVideoSurface;
@property (nonatomic, strong) UIImageView                   *remoteVideoSurface;

@property (weak, nonatomic) IBOutlet UINavigationBar        *theNavBar;

-(void) helloDelegate;

- (void)AnyChatNotifyHandler:(NSNotification*)notify;

- (void) OnLocalVideoInit:(id)session;

- (void) OnLocalVideoRelease:(id)sender;

- (void) StartVideoChat:(int) userid;

- (void) FinishVideoChat;

- (IBAction) OnFinishVideoChatBtnClicked:(id)sender;

- (IBAction) OnSwitchCameraBtnClicked:(id)sender;

@end
