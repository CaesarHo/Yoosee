package com.home.yoosee.global;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.home.yoosee.base.P2PConnect;
import com.home.yoosee.entity.Account;
import com.home.yoosee.thread.MainThread;
import com.p2p.core.P2PHandler;

public class MainService extends Service {
    Context context;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Notification notification = new Notification();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;

        Account account = AccountPersist.getInstance().getActiveAccountInfo(this);
        try {
            int codeStr1 = (int) Long.parseLong(account.rCode1);
            int codeStr2 = (int) Long.parseLong(account.rCode2);
            Log.e("result", "codeStr1" + codeStr1 + "---------" + "codeStr2=" + codeStr1);
            if (account != null) {
                boolean result = P2PHandler.getInstance().p2pConnect(account.three_number, codeStr1, codeStr2);
                Log.e("result", "result=" + result);
                if (result) {
                    new P2PConnect(getApplicationContext());
                    new MainThread(context).go();
                } else {
                    Log.e("result", "result = " + false);
                }
            } else {
                Log.e("account", "account != null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainThread.getInstance().kill();
        P2PHandler.getInstance().p2pDisconnect();
    }

}
