package com.home.yoosee.base;

import android.app.Application;
import android.content.IntentFilter;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heshengfang on 2017/10/14.
 */

public class YooseeApp extends Application implements DistributedHandler.HandlerPart{
    public final static String TAG = "YooseeApp";
    private HandlerThread mThread;
    private DistributedHandler mMainHandler;

    public static YooseeApp wiFiApp;

    public static YooseeApp getInstance() {
        return wiFiApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wiFiApp = this;
        mMainHandler = new DistributedHandler();
        mMainHandler.addHandlerPart(this);
        mThread = new HandlerThread("yf.bt.BackgroundThread");
        mThread.start();
    }


    public DistributedHandler getMainHandler() {
        return mMainHandler;
    }

    @Override
    public boolean dispatchHandleMessage(Message msg) {
        switch (msg.what) {

        }
        return false;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (null != mThread.getLooper()) {
            mThread.getLooper().quit();
        }
        Log.d(TAG, "onTrimMemory");
    }
}
