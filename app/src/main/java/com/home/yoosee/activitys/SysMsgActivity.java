package com.home.yoosee.activitys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.home.yoosee.R;

public class SysMsgActivity extends AppCompatActivity {
    public static final String REFRESH = "com.home.REFRESH";
    public static final String DELETE_REFESH = "com.home.DELETE_REFESH";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_msg);
    }
}
