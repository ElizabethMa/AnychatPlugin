//
//  VideoChatController.m
//  AnyChat
//
//  Created by bairuitech on 11-11-22.
//  Copyright 2011年 __MyCompanyName__. All rights reserved.
//

#import "VideoChatController.h"

#import "AnyChatPlatform.h"
#import "AnyChatDefine.h"
#import "AnyChatErrorCode.h"
#import "AnyChatObjectDefine.h"

@implementation VideoChatController

@synthesize delegate;

@synthesize remoteVideoSurface;
@synthesize localVideoSurface;
@synthesize theLocalView;

-(void) helloDelegate
{
    [delegate doFinishVideo:0];
}

-(void) dealloc
{
    delegate = nil;
}


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}


- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // 注册通知中心
    UIDevice *device = [UIDevice currentDevice];
    [device beginGeneratingDeviceOrientationNotifications];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(AnyChatNotifyHandler:) name:@"ANYCHATNOTIFY" object:nil ];
    
    //初始化SDK
    anychat = [[AnyChatPlatform alloc] init];
    
    //AnyChat通知消息代理(回调事件接收者)
    anychat.notifyMsgDelegate = self;
    
    // 初始化 AnyChatSDK
    [AnyChatPlatform InitSDK:0];
    [self updateLocalSettings];

    
    //连接服务器,第一个参数为服务器地址,第二参数为端口。
    [AnyChatPlatform Connect:_serverIp :_serverPort];
    
//    UIDevice *device = [UIDevice currentDevice];
//    [device beginGeneratingDeviceOrientationNotifications];
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(deviceOrientation:) name:UIDeviceOrientationDidChangeNotification object:device];
}

- (void)AnyChatNotifyHandler:(NSNotification*)notify
{
    NSDictionary* dict = notify.userInfo;
    [anychat OnRecvAnyChatNotify:dict];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

//AnyChat SDK自动调用“摄像头硬件初始化”方法
- (void) OnLocalVideoInit:(id)session
{
    //通过 session 控制设备的视频数据输入和输出流向
    self.localVideoSurface = [AVCaptureVideoPreviewLayer layerWithSession: (AVCaptureSession*)session];
    //视频显示层 UI 设置
    self.localVideoSurface.frame = CGRectMake(0, 0, 120, 160);
    self.localVideoSurface.videoGravity = AVLayerVideoGravityResizeAspectFill;
    //视频显示层添加到自定义的 theLocalView 界面显示视图中。
    [self.theLocalView.layer addSublayer:self.localVideoSurface];
}

- (void) OnLocalVideoRelease:(id)sender
{
    //“localVideoSurface”表示视频显示层全局变量 (参考 4.2.2)
    if(localVideoSurface) {
        localVideoSurface = nil;
    }
}

- (void) StartVideoChat:(int) userid
{
    // open local video
    
    _remoteUserid = userid;
    
//    self->iRemoteUserId = userid;
    
    [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_ORIENTATION : self.interfaceOrientation];
}

- (void) FinishVideoChat
{
    [AnyChatPlatform UserSpeakControl: -1 : NO];
    [AnyChatPlatform UserCameraControl: -1 : NO];
    
    
    [AnyChatPlatform UserSpeakControl: _remoteUserid : NO];
    [AnyChatPlatform UserCameraControl: _remoteUserid : NO];
    
    [AnyChatPlatform Logout];
    
    [self dismissViewControllerAnimated:YES completion:nil];
    
    [self helloDelegate];
    
}

- (IBAction) OnSwitchCameraBtnClicked:(id)sender
{
    NSLog(@"===== click SwitchCameraBtn ======");
    static int CurrentCameraDevice = 0;
    NSMutableArray* cameraDeviceArray = [AnyChatPlatform EnumVideoCapture];
    if(cameraDeviceArray.count == 2)
    {
        CurrentCameraDevice = (CurrentCameraDevice+1) % 2;
        [AnyChatPlatform SelectVideoCapture:[cameraDeviceArray objectAtIndex:CurrentCameraDevice]];
    }
}

- (IBAction) OnFinishVideoChatBtnClicked:(id)sender
{
    NSLog(@"===== click ReturnBtn ======");
    [self FinishVideoChat];
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Orientation Rotation

- (BOOL)shouldAutorotate
{
    return NO;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Video Rotation

- (void)deviceOrientation:(NSNotification *)notification
{
    //device orientation
    UIDeviceOrientation devOrientation = [UIDevice currentDevice].orientation;
    
    if (devOrientation == UIDeviceOrientationLandscapeLeft)
    {
        [self setFrameOfLandscapeLeft];
    }
    else if (devOrientation == UIDeviceOrientationLandscapeRight)
    {
        [self setFrameOfLandscapeRight];
    }
    if (devOrientation == UIDeviceOrientationPortrait)
    {
        [self setFrameOfPortrait];
    }
    
}


-(void)setFrameOfPortrait
{
    //Rotate
    remoteVideoSurface.layer.transform = kLayer3DRotation_Z_Axis(0.0);
    //Scale
    self.remoteVideoSurface.frame = CGRectMake(0, 0, kSelfView_Width, kSelfView_Height);
}

-(void)setFrameOfLandscapeLeft
{
    //Rotate
    remoteVideoSurface.layer.transform = kLayer3DRotation_Z_Axis(-90.0);
    //Scale
    self.remoteVideoSurface.frame = CGRectMake(0, 0, kSelfView_Width, kSelfView_Height);
}

-(void)setFrameOfLandscapeRight
{
    //Rotate
    remoteVideoSurface.layer.transform = kLayer3DRotation_Z_Axis(90.0);
    //Scale
    self.remoteVideoSurface.frame = CGRectMake(0, 0, kSelfView_Width, kSelfView_Height);
}

#pragma mark - UITouch

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    static int clicked = 1;
    if (clicked % 2)
    {
//        self.theNavBar.hidden = YES;
        clicked++;
    }
    else
    {
//        self.theNavBar.hidden = NO;
        clicked++;
    }
}

#pragma mark - AnyChatNotifyMessageDelegate

// 连接服务器消息
- (void) OnAnyChatConnect:(BOOL) bSuccess
{
    NSLog(@"OnAnyChatConnect : %@", bSuccess?@"YES":@"NO");
    if (bSuccess == YES) {
        //用户登录(userName 变量:登录用户名)
        [AnyChatPlatform Login:_userName :_loginPassword];
    } else {
        NSLog(@"Connect error");
        // 返回错误值 ???
    }
    

}
// 用户登陆消息
- (void) OnAnyChatLogin:(int) dwUserId : (int) dwErrorCode
{
    NSLog(@"OnAnyChatLogin : %i , %i ", dwUserId, dwErrorCode);
    
    if (dwErrorCode == 0) {
        //进入房间
        [AnyChatPlatform EnterRoom:(int)_roomId :_enterroomPassword];
    } else {
        NSLog(@"Login error");
        // 返回错误值 ???
    }
    
}
// 用户进入房间消息
- (void) OnAnyChatEnterRoom:(int) dwRoomId : (int) dwErrorCode
{
    NSLog(@"OnAnyChatEnterRoom : %i , %i ", dwRoomId, dwErrorCode);
    
    if (dwErrorCode == 0) {
        //打开本地音频(参数“-1”表示本地用户,也可以用本地的真实 userid)
        [AnyChatPlatform UserSpeakControl: -1:YES];
        //设置本地视频 UI(“0”为默认适配视频显示位置与尺寸大小)
        [AnyChatPlatform SetVideoPos:-1 :self :0 :0 :0 :0];
        //打开本地视频(参数“-1”表示本地用户,也可以用本地的真实 userid)
        [AnyChatPlatform UserCameraControl:-1 : YES];
        
        NSMutableArray * userList =[[NSMutableArray alloc] initWithArray:[AnyChatPlatform GetOnlineUser]];
        
        int i = 0;
        int count = (int)userList.count;
        
        for (i= 0; i < count ; i++) {
            NSLog(@"房间中的userId=%@ ; _remoteUserid=%i",userList[i],_remoteUserid);
            if ([userList[i] intValue] == _remoteUserid) {
                
                NSLog(@"需要打开对方视频，userId=%@",userList[i]);
                // 打开远程视频音频
                //打开当前房间在线目标对象的音视频,需要传入它的 userid
                [AnyChatPlatform UserSpeakControl: _remoteUserid:YES];
                //绑定目标对象视频显示在自定义的 remoteVideoSurface
                [AnyChatPlatform SetVideoPos:_remoteUserid: self.remoteVideoSurface:0:0:0:0];
                // 打开目标用户视频
                [AnyChatPlatform UserCameraControl:_remoteUserid : YES];
            }
        }

        
    } else {
        NSLog(@"enter room error");
        // 返回错误值 ???
    }
}

// 房间在线用户消息
- (void) OnAnyChatOnlineUser:(int) dwUserNum : (int) dwRoomId
{
    // dwUserNum 当前房间用户数目
    NSLog(@"OnAnyChatOnlineUser : %i , %i ", dwUserNum, dwRoomId);
}

// 用户进入房间消息
- (void) OnAnyChatUserEnterRoom:(int) dwUserId
{
    NSLog(@"OnAnyChatUserEnterRoom : %i", dwUserId);
    if (dwUserId == _remoteUserid) {
        // request other user video
        //打开当前房间在线目标对象的音视频,需要传入它的 userid
        [AnyChatPlatform UserSpeakControl: _remoteUserid:YES];
        //绑定目标对象视频显示在自定义的 remoteVideoSurface
        [AnyChatPlatform SetVideoPos:_remoteUserid: self.remoteVideoSurface:0:0:0:0];
        // 打开目标用户视频
        [AnyChatPlatform UserCameraControl:_remoteUserid : YES];
    }
}
// 用户退出房间消息
- (void) OnAnyChatUserLeaveRoom:(int) dwUserId
{
    NSLog(@"OnAnyChatUserLeaveRoom : %i", dwUserId);
    if (dwUserId == _remoteUserid) {
        [self FinishVideoChat];
    }
}
// 网络断开消息
- (void) OnAnyChatLinkClose:(int) dwErrorCode
{
    NSLog(@"OnAnyChatLinkClose : %i", dwErrorCode);
    [self FinishVideoChat];
}

// 更新本地参数设置
- (void) updateLocalSettings
{
    [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_OVERLAY :1];
    [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_ORIENTATION : self.interfaceOrientation];
    
//    NSString* const kUseP2P = @"usep2p";
//    NSString* const kUseServerParam = @"useserverparam";
//    NSString* const kVideoSolution = @"videosolution";
//    NSString* const kVideoFrameRate = @"videoframerate";
//    NSString* const kVideoBitrate = @"videobitrate";
//    NSString* const kVideoPreset = @"videopreset";
//    NSString* const kVideoQuality = @"videoquality";
//    
//    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
//    [defaults synchronize];
//    
//    BOOL bUseP2P = [[defaults objectForKey:kUseP2P] boolValue];
//    BOOL bUseServerVideoParam = [[defaults objectForKey:kUseServerParam] boolValue];
//    int iVideoSolution =    [[defaults objectForKey:kVideoSolution] intValue];
//    int iVideoBitrate =     [[defaults objectForKey:kVideoBitrate] intValue];
//    int iVideoFrameRate =   [[defaults objectForKey:kVideoFrameRate] intValue];
//    int iVideoPreset =      [[defaults objectForKey:kVideoPreset] intValue];
//    int iVideoQuality =     [[defaults objectForKey:kVideoQuality] intValue];
//    
//    // P2P
//    [AnyChatPlatform SetSDKOptionInt:BRAC_SO_NETWORK_P2PPOLITIC : (bUseP2P ? 1 : 0)];
//    
//    if(bUseServerVideoParam)
//    {
//        // 屏蔽本地参数，采用服务器视频参数设置
//        [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_APPLYPARAM :0];
//    }
//    else
//    {
//        int iWidth, iHeight;
//        switch (iVideoSolution) {
//            case 0:     iWidth = 1280;  iHeight = 720;  break;
//            case 1:     iWidth = 640;   iHeight = 480;  break;
//            case 2:     iWidth = 480;   iHeight = 360;  break;
//            case 3:     iWidth = 352;   iHeight = 288;  break;
//            case 4:     iWidth = 192;   iHeight = 144;  break;
//            default:    iWidth = 352;   iHeight = 288;  break;
//        }
//        [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_WIDTHCTRL :iWidth];
//        [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_HEIGHTCTRL :iHeight];
//        [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_BITRATECTRL :iVideoBitrate];
//        [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_FPSCTRL :iVideoFrameRate];
//        [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_PRESETCTRL :iVideoPreset];
//        [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_QUALITYCTRL :iVideoQuality];
//        
//        // 采用本地视频参数设置，使参数设置生效
//        [AnyChatPlatform SetSDKOptionInt:BRAC_SO_LOCALVIDEO_APPLYPARAM :1];
//    }
    
}



@end
