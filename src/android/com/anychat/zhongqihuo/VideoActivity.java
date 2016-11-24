package com.anychat.zhongqihuo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.anychat.config.ConfigEntity;
import com.anychat.config.ConfigService;
import com.zhongqihuo.R;

import org.apache.cordova.CordovaActivity;

public class VideoActivity extends CordovaActivity implements AnyChatBaseEvent  {

    private final static String TAG = "VideoActivity";

    private final int UPDATEVIDEOBITDELAYMILLIS = 200; //监听音频视频的码率的间隔刷新时间（毫秒）

    int userID;
    boolean bOnPaused = false;
    private boolean bSelfVideoOpened = false; // 本地视频是否已打开
    private boolean bOtherVideoOpened = false; // 对方视频是否已打开
    private Boolean mFirstGetVideoBitrate = false; //"第一次"获得视频码率的标致
    private Boolean mFirstGetAudioBitrate = false; //"第一次"获得音频码率的标致

    private SurfaceView mOtherView;
    private SurfaceView mMyView;
    private ImageButton mImgSwitchVideo;
    private Button mEndCallBtn;
    private ImageButton mBtnCameraCtrl; // 控制视频的按钮
    private ImageButton mBtnSpeakCtrl; // 控制音频的按钮

    private String mStrIP = "demo.anychat.cn";
    private String mStrName = "name";
    private int mSPort = 8906;
    private int mSRoomID = 1;
    private int mRemoteUserid = 1;

    private String login_password = "";
    private String enterroom_password = "";

    private String resultMsg = "";
    private int resultCode = 0; // 0 断网后主动关闭; 1 用户单击确认退出; 2 对方客户退出后退出;

    public AnyChatCoreSDK anychatSDK;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
//        userID = Integer.parseInt(intent.getStringExtra("UserID"));
        mStrIP = intent.getStringExtra("mStrIP");
        mStrName = intent.getStringExtra("mStrName");
        mSPort = intent.getIntExtra("mSPort", 8906);
        mSRoomID = intent.getIntExtra("mSRoomID", 1);
//        mRemoteUserid = intent.getIntExtra("mRemoteUserid", 1);
        userID = intent.getIntExtra("mRemoteUserid", 1);

        login_password = intent.getStringExtra("loginPassword");
        enterroom_password = intent.getStringExtra("enterroomPassword");


        InitSDK();

        InitLayout();

        ApplyVideoConfig();

        // 如果视频流过来了，则把背景设置成透明的
        handler.postDelayed(runnable, UPDATEVIDEOBITDELAYMILLIS);
    }

    private void InitSDK() {
        if (anychatSDK == null) {
            anychatSDK = new AnyChatCoreSDK();
            anychatSDK.SetBaseEvent(this);
            anychatSDK.InitSDK(android.os.Build.VERSION.SDK_INT, 0);
            // 启动 AnyChat 传感器监听
            anychatSDK.mSensorHelper.InitSensor(this);
            // 初始化Camera上下文句柄
            AnyChatCoreSDK.mCameraHelper.SetContext(this);
            // Login
            Log.i(TAG, mStrIP + " : " + mSPort);
            anychatSDK.Connect(mStrIP, mSPort);
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION, 0);
        }
    }

    private void InitLayout() {
        this.setContentView(R.layout.video_frame);
        this.setTitle("与 \"" + anychatSDK.GetUserName(userID) + "\" 对话中");
        mMyView = (SurfaceView) findViewById(R.id.surface_local);
        mOtherView = (SurfaceView) findViewById(R.id.surface_remote);
//        mMyView = (SurfaceView) findViewById(R.id.surface_remote);
//        mOtherView = (SurfaceView) findViewById(R.id.surface_local);
        mImgSwitchVideo = (ImageButton) findViewById(R.id.ImgSwichVideo);
        mEndCallBtn = (Button) findViewById(R.id.endCall);
        mBtnSpeakCtrl = (ImageButton) findViewById(R.id.btn_speakControl);
        mBtnCameraCtrl = (ImageButton) findViewById(R.id.btn_cameraControl);
        mBtnSpeakCtrl.setOnClickListener(onClickListener);
        mBtnCameraCtrl.setOnClickListener(onClickListener);
        mImgSwitchVideo.setOnClickListener(onClickListener);
        mEndCallBtn.setOnClickListener(onClickListener);
        // 如果是采用Java视频采集，则需要设置Surface的CallBack
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            mMyView.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);
        }

        // 如果是采用Java视频显示，则需要设置Surface的CallBack
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
            int index = anychatSDK.mVideoHelper.bindVideo(mOtherView
                    .getHolder());
            anychatSDK.mVideoHelper.SetVideoUser(index, userID);
        }

        mOtherView.setZOrderOnTop(true);

        // 判断是否显示本地摄像头切换图标
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            if (AnyChatCoreSDK.mCameraHelper.GetCameraNumber() > 1) {
                // 默认打开前置摄像头
                AnyChatCoreSDK.mCameraHelper
                        .SelectVideoCapture(AnyChatCoreSDK.mCameraHelper.CAMERA_FACING_FRONT);
            }
        } else {
            String[] strVideoCaptures = anychatSDK.EnumVideoCapture();
            if (strVideoCaptures != null && strVideoCaptures.length > 1) {
                // 默认打开前置摄像头
                for (int i = 0; i < strVideoCaptures.length; i++) {
                    String strDevices = strVideoCaptures[i];
                    if (strDevices.indexOf("Front") >= 0) {
                        anychatSDK.SelectVideoCapture(strDevices);
                        break;
                    }
                }
            }
        }

        // 根据屏幕方向改变本地surfaceview的宽高比
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            adjustLocalVideo(true);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            adjustLocalVideo(false);
        }

        // 打开本地视频预览,开始采集本地视频数据
        mMyView.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);

    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            try {
                int videoBitrate = anychatSDK.QueryUserStateInt(userID,
                        AnyChatDefine.BRAC_USERSTATE_VIDEOBITRATE);
                int audioBitrate = anychatSDK.QueryUserStateInt(userID,
                        AnyChatDefine.BRAC_USERSTATE_AUDIOBITRATE);
                if (videoBitrate > 0) {
                    //handler.removeCallbacks(runnable);
                    mFirstGetVideoBitrate = true;
                    mOtherView.setBackgroundColor(Color.TRANSPARENT);
                }

                if (audioBitrate > 0) {
                    mFirstGetAudioBitrate = true;
                }

                if (mFirstGetVideoBitrate) {
                    if (videoBitrate <= 0) {
                        Toast.makeText(VideoActivity.this, "对方视频中断了!", Toast.LENGTH_SHORT).show();
                        // 重置下，如果对方退出了，有进去了的情况
                        mFirstGetVideoBitrate = false;
                    }
                }

                if (mFirstGetAudioBitrate) {
                    if (audioBitrate <= 0) {
                        Toast.makeText(VideoActivity.this, "对方音频中断了!", Toast.LENGTH_SHORT).show();
                        // 重置下，如果对方退出了，有进去了的情况
                        mFirstGetAudioBitrate = false;
                    }
                }

                handler.postDelayed(runnable, UPDATEVIDEOBITDELAYMILLIS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case (R.id.ImgSwichVideo): {

                    // 如果是采用Java视频采集，则在Java层进行摄像头切换
                    if (AnyChatCoreSDK
                            .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
                        AnyChatCoreSDK.mCameraHelper.SwitchCamera();
                        return;
                    }

                    String strVideoCaptures[] = anychatSDK.EnumVideoCapture();
                    String temp = anychatSDK.GetCurVideoCapture();
                    for (int i = 0; i < strVideoCaptures.length; i++) {
                        if (!temp.equals(strVideoCaptures[i])) {
                            anychatSDK.UserCameraControl(-1, 0);
                            bSelfVideoOpened = false;
                            anychatSDK.SelectVideoCapture(strVideoCaptures[i]);
                            anychatSDK.UserCameraControl(-1, 1);
                            break;
                        }
                    }
                }
                break;
                case (R.id.endCall): {
                    exitVideoDialog();
                }
                case R.id.btn_speakControl:
                    if ((anychatSDK.GetSpeakState(-1) == 1)) {
                        mBtnSpeakCtrl.setImageResource(R.drawable.speak_off);
                        anychatSDK.UserSpeakControl(-1, 0);
                    } else {
                        mBtnSpeakCtrl.setImageResource(R.drawable.speak_on);
                        anychatSDK.UserSpeakControl(-1, 1);
                    }

                    break;
                case R.id.btn_cameraControl:
                    if ((anychatSDK.GetCameraState(-1) == 2)) {
                        mBtnCameraCtrl.setImageResource(R.drawable.camera_off);
                        anychatSDK.UserCameraControl(-1, 0);
                    } else {
                        mBtnCameraCtrl.setImageResource(R.drawable.camera_on);
                        anychatSDK.UserCameraControl(-1, 1);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void refreshAV() {
        anychatSDK.UserCameraControl(userID, 1);
        anychatSDK.UserSpeakControl(userID, 1);
        anychatSDK.UserCameraControl(-1, 1);
        anychatSDK.UserSpeakControl(-1, 1);
        mBtnSpeakCtrl.setImageResource(R.drawable.speak_on);
        mBtnCameraCtrl.setImageResource(R.drawable.camera_on);
        bOtherVideoOpened = false;
        bSelfVideoOpened = false;
    }

    private void exitVideoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确定退出视频吗?")
                .setCancelable(false)
                .setPositiveButton("退出",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                destroyCurActivity(1);

                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void destroyCurActivity(int resultCode) {
        Intent mIntent = new Intent();
        mIntent.putExtra("resultCode", resultCode);
        // 设置结果，并进行传送
        setResult(RESULT_OK, mIntent);

        onPause();
        onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitVideoDialog();
        }

        return super.onKeyDown(keyCode, event);
    }

    protected void onRestart() {
        super.onRestart();
        // 如果是采用Java视频显示，则需要设置Surface的CallBack
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
            int index = anychatSDK.mVideoHelper.bindVideo(mOtherView
                    .getHolder());
            anychatSDK.mVideoHelper.SetVideoUser(index, userID);
        }
        refreshAV();
        bOnPaused = false;
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
        bOnPaused = true;
        anychatSDK.UserCameraControl(userID, 0);
        anychatSDK.UserSpeakControl(userID, 0);
        anychatSDK.UserCameraControl(-1, 0);
        anychatSDK.UserSpeakControl(-1, 0);
    }

    public void onDestroy() {

        anychatSDK.LeaveRoom(-1);
        anychatSDK.Logout();
        anychatSDK.Release();
        handler.removeCallbacks(runnable);
        anychatSDK.mSensorHelper.DestroySensor();

        finish();
        super.onDestroy();
    }

    public void adjustLocalVideo(boolean bLandScape) {
        float width;
        float height = 0;
        DisplayMetrics dMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
        width = (float) dMetrics.widthPixels / 4;
        LinearLayout layoutLocal = (LinearLayout) this
                .findViewById(R.id.frame_local_area);
        FrameLayout.LayoutParams layoutParams = (android.widget.FrameLayout.LayoutParams) layoutLocal
                .getLayoutParams();
        if (bLandScape) {

            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL) != 0)
                height = width
                        * AnyChatCoreSDK
                        .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL)
                        / AnyChatCoreSDK
                        .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL)
                        + 5;
            else
                height = (float) 3 / 4 * width + 5;
        } else {

            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL) != 0)
                height = width
                        * AnyChatCoreSDK
                        .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL)
                        / AnyChatCoreSDK
                        .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL)
                        + 5;
            else
                height = (float) 4 / 3 * width + 5;
        }
        layoutParams.width = (int) width;
        layoutParams.height = (int) height;
        layoutLocal.setLayoutParams(layoutParams);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            adjustLocalVideo(true);
            AnyChatCoreSDK.mCameraHelper.setCameraDisplayOrientation();
        } else {
            adjustLocalVideo(false);
            AnyChatCoreSDK.mCameraHelper.setCameraDisplayOrientation();
        }

    }

    @Override
    //连接服务器触发(connet),“bSuccess==true”连接服务器成功,反之连接服务器失 败
    public void OnAnyChatConnectMessage(boolean bSuccess) {
        if (bSuccess == true) {
            Log.i(TAG, "OnAnyChatConnectMessage == Success");
            anychatSDK.Login(mStrName, login_password);
        } else {
            Log.i(TAG, "OnAnyChatConnectMessage == fail");
        }
    }

    @Override
    //用户登录触发(login),dwUserId是服务器为客户端分配的唯一标识userid,dwErrorCode==0 表示登录成功,其他值为登录服务器失败的错误代码
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
        Log.i(TAG, "OnAnyChatLoginMessage dwUserId :" + dwUserId + ", dwErrorCode : " + dwErrorCode);
        anychatSDK.EnterRoom(mSRoomID, enterroom_password);
    }

    @Override
    // 进入房间触发,dwRoomId为房间号,dwErrorCode==0表示进入房间成功,其他值为进入房间失败的错误代码
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
        Log.i(TAG, "进入房间 : " + dwRoomId + " ！dwErrorCode : " + dwErrorCode);
        if(dwErrorCode == 0){
            Log.i(TAG, "打开本地视频+音频");
            anychatSDK.UserCameraControl(-1, 1);
            anychatSDK.UserSpeakControl(-1, 1);
        }else{
            anychatSDK.EnterRoom(mSRoomID, enterroom_password);
        }
    }

    @Override
    // 当前房间在线用户消息,进入房间成功后调用一次。dwUserNum 当前房间总人数(包含自 己)
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
        Log.i(TAG, "当前房间总用户数 : " + dwUserNum + " , 进入房间 : " + dwRoomId + " ！");

        int[] userIDList = anychatSDK.GetOnlineUser();
        for (int index = 0; index < userIDList.length; index++) {
            Log.i(TAG, "index: " + index + ", userid : " + userIDList[index] + ", username : " + anychatSDK.GetUserName(userIDList[index]));
            if (userIDList[index] == userID){
                Log.i(TAG, "userID: " + userID);
                int ind = anychatSDK.mVideoHelper.bindVideo(mOtherView.getHolder());
                anychatSDK.mVideoHelper.SetVideoUser(ind, userID);
                anychatSDK.UserCameraControl(userID, 1);
                anychatSDK.UserSpeakControl(userID, 1);
            }
        }
    }

    @Override
    // 当前房间用户离开或者进入房间触发这个回调,dwUserId用户 id," bEnter==true"表示进入 房间,反之表示离开房间
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
        Log.i(TAG, "OnAnyChatUserAtRoomMessage : dwUerId = " + dwUserId + "; bEnter = " + bEnter);

        if (!bEnter) {
            if (dwUserId == userID) {
                Toast.makeText(VideoActivity.this, "对方已离开！", Toast.LENGTH_SHORT).show();
                userID = 0;
                if (bOtherVideoOpened) {
                    anychatSDK.UserCameraControl(userID, 0);
                    anychatSDK.UserSpeakControl(userID, 0);
                    bOtherVideoOpened = false;
                }
                if (bSelfVideoOpened) {
                    anychatSDK.UserCameraControl(-1, 0);
                    anychatSDK.UserSpeakControl(-1, 0);
                    bSelfVideoOpened = false;
                }

                destroyCurActivity(2); //客服离开房间退出
            }
        } else {
            if (dwUserId != 0) {
                int index = anychatSDK.mVideoHelper.bindVideo(mOtherView.getHolder());
                anychatSDK.mVideoHelper.SetVideoUser(index, userID);
                anychatSDK.UserCameraControl(userID, 1);
                anychatSDK.UserSpeakControl(userID, 1);
            }
        }
    }

    @Override
    // 跟服务器网络断触发该消息。收到该消息后可以关闭音视频以及做相关提示工作
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
        Log.i(TAG, "OnAnyChatLinkCloseMessage : dwErrorCode = " + dwErrorCode + ".");
        // 网络连接断开之后，上层需要主动关闭已经打开的音视频设备
        if (bOtherVideoOpened) {
            anychatSDK.UserCameraControl(userID, 0);
            anychatSDK.UserSpeakControl(userID, 0);
            bOtherVideoOpened = false;
        }
        if (bSelfVideoOpened) {
            anychatSDK.UserCameraControl(-1, 0);
            anychatSDK.UserSpeakControl(-1, 0);
            bSelfVideoOpened = false;
        }

        // 销毁当前界面
        destroyCurActivity(0);

    }

    // 根据配置文件配置视频参数
    private void ApplyVideoConfig() {
        ConfigEntity configEntity = ConfigService.LoadConfig(this);
        if (configEntity.mConfigMode == 1) // 自定义视频参数配置
        {
            // 设置本地视频编码的码率（如果码率为0，则表示使用质量优先模式）
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_BITRATECTRL,
                    configEntity.mVideoBitrate);
//          if (configEntity.mVideoBitrate == 0) {
            // 设置本地视频编码的质量
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_QUALITYCTRL,
                    configEntity.mVideoQuality);
//          }
            // 设置本地视频编码的帧率
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_FPSCTRL,
                    configEntity.mVideoFps);
            // 设置本地视频编码的关键帧间隔
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_GOPCTRL,
                    configEntity.mVideoFps * 4);
            // 设置本地视频采集分辨率
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL,
                    configEntity.mResolutionWidth);
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL,
                    configEntity.mResolutionHeight);
            // 设置视频编码预设参数（值越大，编码质量越高，占用CPU资源也会越高）
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_PRESETCTRL,
                    configEntity.mVideoPreset);
        }
        // 让视频参数生效
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_APPLYPARAM,
                configEntity.mConfigMode);
        // P2P设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_NETWORK_P2PPOLITIC,
                configEntity.mEnableP2P);
        // 本地视频Overlay模式设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_OVERLAY,
                configEntity.mVideoOverlay);
        // 回音消除设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_AUDIO_ECHOCTRL,
                configEntity.mEnableAEC);
        // 平台硬件编码设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_CORESDK_USEHWCODEC,
                configEntity.mUseHWCodec);
        // 视频旋转模式设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_ROTATECTRL,
                configEntity.mVideoRotateMode);
        // 本地视频采集偏色修正设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_FIXCOLORDEVIA,
                configEntity.mFixColorDeviation);
        // 视频GPU渲染设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_VIDEOSHOW_GPUDIRECTRENDER,
                configEntity.mVideoShowGPURender);
        // 本地视频自动旋转设置
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION,
                configEntity.mVideoAutoRotation);
    }

}
