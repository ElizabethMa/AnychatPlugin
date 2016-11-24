/**
 * Open.java
 * <p/>
 * Copyright (C) 2014 Carlos Antonio
 * <p/>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package com.cordova.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

import com.anychat.zhongqihuo.VideoActivity;

import org.json.JSONObject;

/**
 * This class starts an activity for an intent to view files
 */
public class AnyChatPlugin extends CordovaPlugin {

    private static final String TAG = "AnyChatPlugin";

    public static final String CHAT_ACTION = "startChat";
    public static final String CLOSE_ACTION = "close";

    private CallbackContext callbackContext;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(CHAT_ACTION)) {
            String mStrIP = args.getString(0);
            String mStrName = args.getString(1);
            int mSPort = args.getInt(2);
            int mSRoomID = args.getInt(3);
            int mRemoteUserid = args.getInt(4);
            String loginPassword = args.getString(5);
            String enterroomPassword = args.getString(6);
            this.callVideoIntentForResult(mStrIP, mStrName, mSPort, mSRoomID, mRemoteUserid, loginPassword, enterroomPassword, callbackContext);
//            return true;
        } else if (action.equals(CLOSE_ACTION)) {
            callbackContext.error(CLOSE_ACTION + " in dev");
        } else {
            callbackContext.error("no " + action + " action");
        }
        return true;
    }

    private void callVideoIntentForResult(String mStrIP, String mStrName, int mSPort, int mSRoomID, int mRemoteUserid,
                                         String loginPassword, String enterroomPassword, CallbackContext callbackContext) {
        try {
//            Intent intent = new Intent("anychat.plugin");
            Intent intent = new Intent(cordova.getActivity(), VideoActivity.class);
            intent.putExtra("mStrIP", mStrIP);
            intent.putExtra("mStrName", mStrName);
            intent.putExtra("mSPort", mSPort);
            intent.putExtra("mSRoomID", mSRoomID);
            intent.putExtra("mRemoteUserid", mRemoteUserid);
            intent.putExtra("loginPassword", loginPassword);
            intent.putExtra("enterroomPassword", enterroomPassword);
//            cordova.getActivity().startActivity(intent);
            cordova.startActivityForResult((CordovaPlugin) this, intent, 111);

            this.callbackContext = callbackContext;
//            callbackContext.success("success");

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Toast.makeText(cordova.getActivity().getApplicationContext(), "无法打开视频通话插件", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult called: requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == 111 && resultCode == android.app.Activity.RESULT_OK) {
            try {
                super.onActivityResult(requestCode, resultCode, data);
                //传递返回值 给js方法
                Bundle b = data.getExtras();  //data为第二个Activity中回传的Intent
                int code = b.getInt("resultCode");
                String msg = b.getString("resultMsg");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("resultCode", code);
                jsonObject.put("resultMsg", msg);
                callbackContext.success(jsonObject);

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                super.onActivityResult(requestCode, resultCode, data);

                callbackContext.error(e.toString());
            }
        }


    }
}
