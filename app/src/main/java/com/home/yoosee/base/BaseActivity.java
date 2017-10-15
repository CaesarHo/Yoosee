package com.home.yoosee.base;

import com.p2p.core.BaseCoreActivity;

/**
 * Created by wade on 2017/10/15.
 */

public abstract class BaseActivity extends BaseCoreActivity {

    @Override
    protected void onGoBack() {
        MyApp.app.showNotification();
    }

    @Override
    protected void onGoFront() {
        MyApp.app.hideNotification();
    }

    @Override
    protected void onExit() {
        MyApp.app.hideNotification();
    }

    public abstract int getActivityInfo();

}