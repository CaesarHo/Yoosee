package com.p2p.core.utils;

import android.graphics.Bitmap.Config;
import android.os.Handler;

import com.p2p.core.MediaPlayer;
import com.p2p.core.global.Config.AppConfig;

import java.io.UnsupportedEncodingException;

/**
 * Created by dxs on 2015/10/9.
 */
public class RtspThread extends Thread {
    private String ipAddress;
    private Handler rtspHandler;
    private long id;
    private String password;
    private boolean isOutCall;
    private int callType;
    private String callId;
    private String ipFlag;
    private String pushMes;
    private int connectTime=3;

    public RtspThread(String ipAddress, Handler rtspHandler, String contactId, String password, boolean isOutCall, int callType, String callId, String ipFlag, String pushMes) {
        this.ipAddress = ipAddress;
        this.rtspHandler = rtspHandler;
        this.password = password;
        this.isOutCall = isOutCall;
        this.callType = callType;
        this.callId = callId;
        this.ipFlag = ipFlag;
        this.pushMes = pushMes;

        this.id = Long.parseLong(callId);
        if (callId.charAt(0) == '0') {
            this.id = 0 - id;
        }
        connectTime=3;
    }

    @Override
    public void run() {

        int ret= 0;
        try {
            ret = MediaPlayer.getInstance().native_p2p_call(id, callType, 0, -1, AppConfig.VideoMode,new byte[8], pushMes.getBytes("utf-8"), ipAddress);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        ret=MediaPlayer.getInstance().native_rtsp_call(id,ipAddress);
//        while(connectTime>0) {
//            if (ret != 0) {
//                break;
//            } else {
//                try {
//                    Log.e("error","rtsp第"+connectTime+"次连接");
//                    ret = MediaPlayer.getInstance().native_p2p_call(id, callType, Integer.parseInt(password), -1, new byte[8], pushMes.getBytes("utf-8"), ipAddress);
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//
//                try {
//                    sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            connectTime--;
//        }
        if(ret==0){
            rtspHandler.sendEmptyMessage(0);
        }else{
            rtspHandler.sendEmptyMessage(1);
        }


    }
}
