package com.home.yoosee.activitys;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.home.yoosee.R;
import com.home.yoosee.fragments.AlarmLogsFragment;
import com.home.yoosee.fragments.ContactFrag;
import com.home.yoosee.widget.CoordinatorMenu;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton iBtn_left;
    private CoordinatorMenu mCoordinatorMenu;
    private ContactFrag contactFrag  = null;
    private AlarmLogsFragment alarmLogsFragment = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (contactFrag == null){
                        contactFrag = ContactFrag.newInstance("","");
                    }
                    replaceFragment(R.id.content,contactFrag,"");
                    return true;
                case R.id.navigation_dashboard:
                    if (alarmLogsFragment == null){
                        alarmLogsFragment = AlarmLogsFragment.newInstance("","");
                    }
                    return true;
                case R.id.navigation_notifications:

                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //侧滑代码
        mCoordinatorMenu = (CoordinatorMenu) findViewById(R.id.menu);
        initView();
    }

    public void initView() {
        iBtn_left = (ImageButton) findViewById(R.id.ibtn_left);
        iBtn_left.setOnClickListener(this);
    }

    @OnClick({R.id.ibtn_left})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_left:
                if (mCoordinatorMenu.isOpened()) {
                    mCoordinatorMenu.closeMenu();
                } else {
                    mCoordinatorMenu.openMenu();
                }
                break;
        }
    }

    public void replaceFragment(int container, Fragment fragment, String tag) {
        try {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            // transaction.setCustomAnimations(android.R.anim.fade_in,
            // android.R.anim.fade_out);
            transaction.replace(container, fragment, tag);
            transaction.commit();
            manager.executePendingTransactions();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("my", "replaceFrag error--main");
        }
    }


    //退出时间
    private long currentBackPressedTime = 0;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mCoordinatorMenu.isOpened()) {
            mCoordinatorMenu.closeMenu();
            return false;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((System.currentTimeMillis() - currentBackPressedTime) > 2000) {
                    Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    currentBackPressedTime = System.currentTimeMillis();
                } else {
                    finish();
                    System.exit(0);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
