//
//  CDVAnyChatPlugin.m
//  Video
//
//  Created by yanqiong on 2/29/16.
//
//

#import <Foundation/Foundation.h>
#import "CDVAnyChatPlugin.h"
#import "VideoChatController.h"

@implementation CDVAnyChatPlugin

@synthesize callbackID;

@synthesize videoChatController;

-(void) doFinishVideo:(NSInteger *)resultCode
{
    NSDictionary* deviceProperties = [self deviceProperties];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:deviceProperties];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackID];

}

- (void)startChat:(CDVInvokedUrlCommand*)command
{
    callbackID = command.callbackId;
    CDVAnyChatOptions* options = [CDVAnyChatOptions createFromStartChatArguments:command];

    NSLog(@"serverIP:%@",options.serverIp);
    NSLog(@"userName:%@",options.userName);
    NSLog(@"serverPort:%@",options.serverPort);
    NSLog(@"roomId:%@",options.roomId);
    NSLog(@"remoteUserid:%@",options.remoteUserid);
    NSLog(@"loginPassword:%@",options.loginPassword);
    NSLog(@"enterroomPassword:%@",options.enterroomPassword);
    
    videoChatController = [[VideoChatController alloc] initWithNibName:@"VideoChatController" bundle:nil];
    
    videoChatController.serverIp = options.serverIp;
    videoChatController.userName = options.userName;
    videoChatController.serverPort = (int)[options.serverPort integerValue];
    videoChatController.roomId = (int)[options.roomId integerValue];
    videoChatController.remoteUserid = (int)[options.remoteUserid integerValue];
    videoChatController.loginPassword = options.loginPassword;
    videoChatController.enterroomPassword = options.enterroomPassword;
    
    videoChatController.delegate = self;
    
    
    [self.viewController presentViewController:videoChatController animated:YES completion:nil];
//
//    self.videoChatController = [[VideoChatController alloc] initWithNibName:@"VideoChatController" bundle:nil];
//    [self.viewController presentViewController:self.videoChatController.view  animated:YES completion:nil];
    
//    NSDictionary* deviceProperties = [self deviceProperties];
//    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:deviceProperties];
//    
//    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (NSDictionary*)deviceProperties
{
    NSMutableDictionary* devProps = [NSMutableDictionary dictionaryWithCapacity:2];
    [devProps setObject:@"0" forKey:@"resultCode"];
    [devProps setObject:@"iOS" forKey:@"resultMsg"];
    NSDictionary* devReturn = [NSDictionary dictionaryWithDictionary:devProps];
    return devReturn;
}
@end

@implementation CDVAnyChatOptions

+ (instancetype) createFromStartChatArguments:(CDVInvokedUrlCommand*)command
{
    CDVAnyChatOptions* options = [[CDVAnyChatOptions alloc] init];
    options.serverIp = [command argumentAtIndex:0 withDefault:@("demo.anychat.cn")];
    options.userName = [command argumentAtIndex:1 withDefault:@("userName")];
    options.serverPort = [command argumentAtIndex:2 withDefault:@(8906)];
    options.roomId = [command argumentAtIndex:3 withDefault:@(1)];
    options.remoteUserid = [command argumentAtIndex:4 withDefault:@(1234)];
    options.loginPassword = [command argumentAtIndex:5 withDefault:nil];
    options.enterroomPassword = [command argumentAtIndex:6 withDefault:nil];
    
    return options;
}

@end