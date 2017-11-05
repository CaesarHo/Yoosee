package com.home.yoosee.activitys;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.home.yoosee.R;
import com.home.yoosee.base.BaseActivity;
import com.home.yoosee.base.DistributedHandler;
import com.home.yoosee.base.MyApp;
import com.home.yoosee.base.P2PListener;
import com.home.yoosee.base.SettingListener;
import com.home.yoosee.data.DataManager;
import com.home.yoosee.data.SharedPreferencesManager;
import com.home.yoosee.entity.Account;
import com.home.yoosee.fragments.APContactFrag;
import com.home.yoosee.fragments.MessageFragment;
import com.home.yoosee.fragments.ContactFrag;
import com.home.yoosee.global.APList;
import com.home.yoosee.global.AccountPersist;
import com.home.yoosee.global.AppConfig;
import com.home.yoosee.global.Constants;
import com.home.yoosee.global.FList;
import com.home.yoosee.global.MainService;
import com.home.yoosee.global.NpcCommon;
import com.home.yoosee.utils.T;
import com.home.yoosee.utils.Utils;
import com.home.yoosee.widget.CoordinatorMenu;
import com.home.yoosee.widget.NormalDialog;
import com.p2p.core.P2PHandler;
import com.p2p.core.network.NetManager;
import com.p2p.core.update.UpdateManager;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements View.OnClickListener, DistributedHandler.HandlerPart {
    private Context mContext;

    private AlertDialog dialog_downapk;
    private ProgressBar downApkBar;
    private long last_time;
    private boolean isApEnter = false;
    boolean isRegFilter = false;

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.ibtn_right)
    ImageButton ibtnRight;
    @BindView(R.id.ibtn_left)
    ImageButton iBtn_left;

    private int type = 0;
    private CoordinatorMenu mCoordinatorMenu;
    private ContactFrag contactFrag = null;
    private APContactFrag apcontactFrag = null;
    private MessageFragment messageFragment = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (SharedPreferencesManager.getInstance().getIsApEnter(mContext)) {
                        if (null == apcontactFrag) {
                            apcontactFrag = new APContactFrag();
                        }
                        replaceFragment(R.id.content, contactFrag, "");
                    } else {
                        if (contactFrag == null) {
                            contactFrag = ContactFrag.newInstance("", "");
                        }
                        replaceFragment(R.id.content, contactFrag, "");
                        tvTitle.setText(getString(R.string.all_tel));
                        type = 0;
                    }

                    return true;
                case R.id.navigation_dashboard:
                    if (messageFragment == null) {
                        messageFragment = MessageFragment.newInstance("", "");
                    }
                    replaceFragment(R.id.content,messageFragment,"");
                    type = 1;
                    return true;
                case R.id.navigation_notifications:

                    type = 2;
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


        mContext = this;
        isApEnter = SharedPreferencesManager.getInstance().getIsApEnter(mContext);
        Log.e("loginmode", "main=" + SharedPreferencesManager.getInstance().getIsApEnter(mContext));
        if (!isApEnter) {
            DataManager.findAlarmMaskByActiveUser(mContext, "");
        }
        NpcCommon.verifyNetwork(mContext);
        if (!verifyLogin() && !isApEnter) {
            Intent login = new Intent(mContext, LoginActivity.class);
            startActivity(login);
            finish();
        } else {
            regFilter();
            MyApp.app.getMainHandler().addHandlerPart(this);
            initView();
            new APList(mContext);
            new FList();
            P2PHandler.getInstance().p2pInit(this, new P2PListener(), new SettingListener());
            connect();
            if (isApEnter) {
                if (null == apcontactFrag) {
                    apcontactFrag = new APContactFrag();
                }
                replaceFragment(R.id.content, apcontactFrag, "");
            } else {
                if (null == contactFrag) {
                    contactFrag = new ContactFrag();
                }
                replaceFragment(R.id.content, contactFrag, "");
                tvTitle.setText(getString(R.string.all_tel));
            }
        }
    }

    public void initView() {
//        iBtn_left = (ImageButton) findViewById(R.id.ibtn_left);
//        iBtn_left.setOnClickListener(this);
    }

    private void connect() {
        final Intent intent = new Intent(this, MainService.class);
        intent.setAction(MyApp.MAIN_SERVICE_START);
        startService(intent);
        if (AppConfig.DeBug.isWrightAllLog) {
            Intent log = new Intent(MyApp.LOGCAT);
            log.setPackage(getPackageName());
            startService(log);
        }
    }

    private boolean verifyLogin() {
        Account activeUser = AccountPersist.getInstance().getActiveAccountInfo(mContext);
        if (activeUser != null) {
            NpcCommon.mThreeNum = activeUser.three_number;
            return true;
        }
        return false;
    }

    @OnClick({R.id.ibtn_left,R.id.ibtn_right})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_left:
                if (mCoordinatorMenu.isOpened()) {
                    mCoordinatorMenu.closeMenu();
                } else {
                    mCoordinatorMenu.openMenu();
                }
                break;
            case R.id.ibtn_right:
                if (type == 0){
                    MyApp.app.getMainHandler().sendEmptyMessage(Constants.Messager.ADD_DEVICE_MSG);
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

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Action.ACTION_NETWORK_CHANGE);
        filter.addAction(Constants.Action.ACTION_SWITCH_USER);
        filter.addAction(Constants.Action.ACTION_EXIT);
        filter.addAction(Constants.Action.RECEIVE_MSG);
        filter.addAction(Constants.Action.RECEIVE_SYS_MSG);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Constants.Action.ACTION_UPDATE);
        filter.addAction(Constants.Action.SESSION_ID_ERROR);
        filter.addAction(Constants.Action.EXITE_AP_MODE);

        // filter.addAction(Constants.Action.SETTING_WIFI_SUCCESS);
        this.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.Action.ACTION_NETWORK_CHANGE)) {
                boolean isNetConnect = false;
                ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetInfo != null) {
                    if (activeNetInfo.isConnected()) {
                        isNetConnect = true;
                        T.showShort(mContext, getString(R.string.message_net_connect) + activeNetInfo.getTypeName());
                        WifiManager wifimanager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
                        if (wifimanager == null) {
                            return;
                        }
                        WifiInfo wifiinfo = wifimanager.getConnectionInfo();
                        if (wifiinfo == null) {
                            return;
                        }
                        if (wifiinfo.getSSID().length() > 0) {
                            String wifiName = Utils.getWifiName(wifiinfo.getSSID());
                            if (wifiName.startsWith(AppConfig.Relese.APTAG)) {
                                String id = wifiName.substring(AppConfig.Relese.APTAG.length());
                                APList.getInstance().gainDeviceMode(id);
                                FList.getInstance().gainDeviceMode(id);
                            }
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Intent intentNew = new Intent();
                        intentNew.setAction(Constants.Action.NET_WORK_TYPE_CHANGE);
                        mContext.sendBroadcast(intentNew);
                    } else {
                        T.showShort(mContext, getString(R.string.network_error) + " " + activeNetInfo.getTypeName());
                    }

                    if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        NpcCommon.mNetWorkType = NpcCommon.NETWORK_TYPE.NETWORK_WIFI;
                    } else {
                        NpcCommon.mNetWorkType = NpcCommon.NETWORK_TYPE.NETWORK_2GOR3G;
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }

                NpcCommon.setNetWorkState(isNetConnect);

            } else if (intent.getAction().equals(Constants.Action.ACTION_SWITCH_USER)) {
                Account account = AccountPersist.getInstance().getActiveAccountInfo(mContext);
                Log.e("account", account.three_number + "--" + account.sessionId);
                new ExitTask(account).execute();
                AccountPersist.getInstance().setActiveAccount(mContext, new Account());
                NpcCommon.mThreeNum = "";
                Log.e("account", "++++++++++++++++++++++++");
                Intent i = new Intent(MyApp.MAIN_SERVICE_START);
                stopService(i);
                Log.e("account", "++++++++++++++++++++++++");
                Intent login = new Intent(mContext, LoginActivity.class);
                startActivity(login);
                Log.e("account", "------------------");
                finish();
            } else if (intent.getAction().equals(Constants.Action.SESSION_ID_ERROR)) {
                Account account = AccountPersist.getInstance().getActiveAccountInfo(mContext);
                new ExitTask(account).execute();
                AccountPersist.getInstance().setActiveAccount(mContext, new Account());
                Intent i = new Intent(MyApp.MAIN_SERVICE_START);
                stopService(i);
                Intent login = new Intent(mContext, LoginActivity.class);
                startActivity(login);
                T.showShort(mContext, R.string.session_id_error);
                finish();
            } else if (intent.getAction().equals(Constants.Action.ACTION_EXIT)) {
                NormalDialog dialog = new NormalDialog(mContext, getString(R.string.exit), getString(R.string.confirm_exit),
                        getString(R.string.exit), getString(R.string.cancel));
                dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {
                    @Override
                    public void onClick() {
                        Intent i = new Intent(MyApp.MAIN_SERVICE_START);
                        stopService(i);
                        isGoExit(true);
                        finish();
                    }
                });
                dialog.showNormalDialog();
            } else if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {

            } else if (intent.getAction().equals(Constants.Action.RECEIVE_MSG)) {
                int result = intent.getIntExtra("result", -1);
                String msgFlag = intent.getStringExtra("msgFlag");

                if (result == Constants.P2P_SET.ACK_RESULT.ACK_SUCCESS) {
                    DataManager.updateMessageStateByFlag(mContext, msgFlag, Constants.MessageType.SEND_SUCCESS);
                } else {
                    DataManager.updateMessageStateByFlag(mContext, msgFlag, Constants.MessageType.SEND_FAULT);
                }

            } else if (intent.getAction().equals(Constants.Action.RECEIVE_SYS_MSG)) {

            } else if (intent.getAction().equals(Constants.Action.ACTION_UPDATE)) {
                String data = intent.getStringExtra("updateDescription");
                NormalDialog dialog = new NormalDialog(mContext, "", data, "", "");
                if (dialog.isShowing()) {
                    Log.e("my", "isShowing");
                    return;
                }
                dialog.showUpdateDialog();
            } else if (intent.getAction().equals(Constants.Action.SETTING_WIFI_SUCCESS)) {
                if (null == contactFrag) {
                    contactFrag = new ContactFrag();
                }
                replaceFragment(R.id.content, contactFrag, "");
            } else if (intent.getAction().equals(Constants.Action.EXITE_AP_MODE)) {
                Log.e("exite", "-------------");
                finish();
            }
        }

    };


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegFilter) {
            isRegFilter = false;
            this.unregisterReceiver(mReceiver);
        }
    }

    @Override
    public boolean dispatchHandleMessage(Message msg) {
        int value = msg.arg1;
        switch (msg.what) {
            case UpdateManager.HANDLE_MSG_DOWNING:
                if ((System.currentTimeMillis() - last_time) > 1000) {
                    MyApp.app.showDownNotification(
                            UpdateManager.HANDLE_MSG_DOWNING, value);
                    last_time = System.currentTimeMillis();
                }
                break;
            case UpdateManager.HANDLE_MSG_DOWN_SUCCESS:
                // MyApp.app.showDownNotification(UpdateManager.HANDLE_MSG_DOWN_SUCCESS,0);
                MyApp.app.hideDownNotification();
                // T.showShort(mContext, R.string.down_success);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                File file = new File(Environment.getExternalStorageDirectory() + "/" + Constants.Update.SAVE_PATH + "/"
                        + Constants.Update.FILE_NAME);
                if (!file.exists()) {
                    return true;
                }
                intent.setDataAndType(Uri.fromFile(file), Constants.Update.INSTALL_APK);
                mContext.startActivity(intent);
                break;
            case UpdateManager.HANDLE_MSG_DOWN_FAULT:

                MyApp.app.showDownNotification(UpdateManager.HANDLE_MSG_DOWN_FAULT, value);
                T.showShort(mContext, R.string.down_fault);
                break;
        }
        return false;
    }

    private class ExitTask extends AsyncTask {
        Account account;

        public ExitTask(Account account) {
            this.account = account;
        }

        @Override
        protected Object doInBackground(Object... params) {
            return NetManager.getInstance(mContext).exit_application(account.three_number, account.sessionId);
        }

        @Override
        protected void onPostExecute(Object object) {
            int result = (Integer) object;
            switch (result) {
                case NetManager.CONNECT_CHANGE:
                    new ExitTask(account).execute();
                    return;
                default:

                    break;
            }
        }
    }

    @Override
    public int getActivityInfo() {
        return Constants.ActivityInfo.ACTIVITY_MAINACTIVITY;
    }
}
