package com.home.yoosee.activitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Message;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.IdRes;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.home.yoosee.R;
import com.home.yoosee.base.BaseActivity;
import com.home.yoosee.base.DistributedHandler;
import com.home.yoosee.base.MyApp;
import com.home.yoosee.data.SharedPreferencesManager;
import com.home.yoosee.entity.Account;
import com.home.yoosee.entity.LocalDevice;
import com.home.yoosee.global.APList;
import com.home.yoosee.global.AccountPersist;
import com.home.yoosee.global.Constants;
import com.home.yoosee.global.NpcCommon;
import com.home.yoosee.utils.T;
import com.home.yoosee.utils.Utils;
import com.home.yoosee.widget.NormalDialog;
import com.p2p.core.network.LoginResult;
import com.p2p.core.network.NetManager;

import org.json.JSONObject;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements OnClickListener,
        DistributedHandler.HandlerPart, CompoundButton.OnCheckedChangeListener,RadioGroup.OnCheckedChangeListener {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;


    private Context mContext;
    private boolean isRegFilter = false;

    public static final int ACCOUNT_NO_EXIST = 3;
    private String mInputName, mInputPwd;
    private AppCompatCheckBox checkBox;
    private boolean isDialogCanel = false;
    private NormalDialog dialog;
    private TextView dfault_name, dfault_count;
    private RelativeLayout choose_country;
    private RadioButton type_phone, type_email;
    private int current_type;
    private boolean isExitAp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;
        isExitAp = getIntent().getBooleanExtra("isExitAp", false);
        initComponent();
        if (SharedPreferencesManager.getInstance().getRecentLoginType(mContext) == Constants.LoginType.PHONE) {
            current_type = Constants.LoginType.PHONE;
            choose_country.setVisibility(RelativeLayout.VISIBLE);
            type_phone.setChecked(true);
        } else {
            choose_country.setVisibility(RelativeLayout.GONE);
            current_type = Constants.LoginType.EMAIL;
            type_email.setChecked(true);
        }
        regFilter();
        initRememberPass();
    }

    public void initComponent() {
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.btn_sign || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mBtnSign = (Button) findViewById(R.id.btn_sign);
        mBtnSign.setOnClickListener(this);


        Button mRegister = (Button) findViewById(R.id.register);
        mRegister.setOnClickListener(this);

        checkBox = (AppCompatCheckBox) findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(this);
        dfault_name = (TextView) findViewById(R.id.name);
        dfault_count = (TextView) findViewById(R.id.count);
        choose_country = (RelativeLayout) findViewById(R.id.country_layout);
        choose_country.setOnClickListener(this);

        RadioGroup type_group = (RadioGroup) findViewById(R.id.type_group);
        type_group.setOnCheckedChangeListener(this);
        type_phone = (RadioButton) findViewById(R.id.type_phone);
        type_email = (RadioButton) findViewById(R.id.type_email);

        TextView forget_pwd = (TextView) findViewById(R.id.forget_pwd);
        forget_pwd.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        forget_pwd.setOnClickListener(this);

        if (SharedPreferencesManager.getInstance().getIsApEnter(mContext)) {
            Intent i = new Intent(mContext, MainActivity.class);
            startActivity(i);
        } else {
            showApDialog();
        }
    }

    public void initRememberPass() {
        String recentName = "";
        String recentPwd = "";
        String recentCode = "";
        if (current_type == Constants.LoginType.PHONE) {
            recentName = SharedPreferencesManager.getInstance().getData(mContext, SharedPreferencesManager.KEY_RECENTNAME);
            recentPwd = SharedPreferencesManager.getInstance().getData(mContext, SharedPreferencesManager.KEY_RECENTPASS);
            recentCode = SharedPreferencesManager.getInstance().getData(mContext, SharedPreferencesManager.KEY_RECENTCODE);
            if (!recentName.equals("")) {
                mEmailView.setText(recentName);
            } else {
                mEmailView.setText("");
            }

            if (!recentCode.equals("")) {
                dfault_count.setText("+" + recentCode);
                String name = SearchListActivity.getNameByCode(mContext, Integer.parseInt(recentCode));
                dfault_name.setText(name);
            } else {
                if (getResources().getConfiguration().locale.getCountry().equals("TW")) {
                    dfault_count.setText("+886");
                    String name = SearchListActivity.getNameByCode(mContext, 886);
                    dfault_name.setText(name);
                } else if (getResources().getConfiguration().locale.getCountry().equals("CN")) {
                    dfault_count.setText("+86");
                    String name = SearchListActivity.getNameByCode(mContext, 86);
                    dfault_name.setText(name);
                } else {
                    dfault_count.setText("+1");
                    String name = SearchListActivity.getNameByCode(mContext, 1);
                    dfault_name.setText(name);
                }
            }

            if (SharedPreferencesManager.getInstance().getIsRememberPass(mContext)) {
                checkBox.setChecked(true);
                if (!recentPwd.equals("")) {
                    mPasswordView.setText(recentPwd);
                } else {
                    mPasswordView.setText("");
                }
            } else {
                checkBox.setChecked(false);
                mPasswordView.setText("");
            }
        } else {
            recentName = SharedPreferencesManager.getInstance().getData(mContext, SharedPreferencesManager.KEY_RECENTNAME_EMAIL);
            recentPwd = SharedPreferencesManager.getInstance().getData(mContext, SharedPreferencesManager.KEY_RECENTPASS_EMAIL);

            if (!recentName.equals("")) {
                mEmailView.setText(recentName);
            } else {
                mEmailView.setText("");
            }

            if (SharedPreferencesManager.getInstance().getIsRememberPass_email(mContext)) {
                checkBox.setChecked(true);
                if (!recentPwd.equals("")) {
                    mPasswordView.setText(recentPwd);
                } else {
                    mPasswordView.setText("");
                }
            } else {
                checkBox.setChecked(false);
                mPasswordView.setText("");
            }
        }
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Action.REPLACE_EMAIL_LOGIN);
        filter.addAction(Constants.Action.REPLACE_PHONE_LOGIN);
        filter.addAction(Constants.Action.ACTION_COUNTRY_CHOOSE);
        mContext.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.Action.REPLACE_EMAIL_LOGIN)) {
                type_email.setChecked(true);
                type_phone.setChecked(false);
                choose_country.setVisibility(RelativeLayout.GONE);
                mEmailView.setText(intent.getStringExtra("username"));
                mPasswordView.setText(intent.getStringExtra("password"));
                current_type = Constants.LoginType.EMAIL;
                attemptLogin();
            } else if (intent.getAction().equals(Constants.Action.REPLACE_PHONE_LOGIN)) {
                type_email.setChecked(false);
                type_phone.setChecked(true);
                choose_country.setVisibility(RelativeLayout.VISIBLE);
                mEmailView.setText(intent.getStringExtra("username"));
                mPasswordView.setText(intent.getStringExtra("password"));
                dfault_count.setText("+" + intent.getStringExtra("code"));
                current_type = Constants.LoginType.PHONE;
                attemptLogin();
            } else if (intent.getAction().equals(Constants.Action.ACTION_COUNTRY_CHOOSE)) {
                String[] info = intent.getStringArrayExtra("info");
                dfault_name.setText(info[0]);
                dfault_count.setText("+" + info[1]);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forget_pwd:
                Uri uri = Uri.parse(Constants.FORGET_PASSWORD_URL);
                Intent open_web = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(open_web);
                break;
            case R.id.country_layout:
                Intent i = new Intent(mContext, SearchListActivity.class);
                startActivity(i);
                break;
            case R.id.btn_sign:
                attemptLogin();
                break;
            case R.id.register:
                if (current_type == Constants.LoginType.PHONE) {
                    // Intent register = new Intent(mContext,RegisterActivity.class);
                    // startActivity(register);
                } else {
                    // Intent register_email = new Intent(mContext,RegisterActivity2.class);
                    // register_email.putExtra("isEmailRegister", true);
                    // startActivity(register_email);
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.checkbox:
                if (current_type == Constants.LoginType.PHONE) {
                    SharedPreferencesManager.getInstance().putIsRememberPass(mContext, isChecked);
                } else {
                    SharedPreferencesManager.getInstance().putIsRememberPass_email(mContext, isChecked);
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (group.getId()){
            case R.id.type_group:
                int radioButtonId = group.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton)findViewById(radioButtonId);
                if (rb == type_phone){
                    choose_country.setVisibility(RelativeLayout.VISIBLE);
                    current_type = Constants.LoginType.PHONE;
                    initRememberPass();
                }else if (rb == type_email){
                    choose_country.setVisibility(RelativeLayout.GONE);
                    current_type = Constants.LoginType.EMAIL;
                    initRememberPass();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.isGoExit(true);
        this.finish();
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mInputName = mEmailView.getText().toString().trim();
        mInputPwd = mPasswordView.getText().toString().trim();
        if ((mInputName != null && !mInputName.equals("")) && (mInputPwd != null && !mInputPwd.equals(""))) {
            if (null != dialog && dialog.isShowing()) {
                Log.e("my", "isShowing");
                return;
            }
            dialog = new NormalDialog(mContext);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    isDialogCanel = true;
                }
            });
            dialog.setTitle(getString(R.string.login_ing));
            dialog.showLoadingDialog();
            dialog.setCanceledOnTouchOutside(false);
            isDialogCanel = false;

            if (current_type == Constants.LoginType.PHONE) {
                String name = dfault_count.getText().toString() + "-" + mInputName;
                new UserLoginTask(name, mInputPwd).execute();
            } else {
                if (Utils.isNumeric(mInputName)) {
                    if (mInputName.charAt(0) != '0') {
                        MyApp.app.getMainHandler().sendEmptyMessage(ACCOUNT_NO_EXIST);
                        return;
                    }
                    new UserLoginTask(mInputName, mInputPwd).execute();
                } else {
                    new UserLoginTask(mInputName, mInputPwd).execute();
                }
            }

        } else {
            if ((mInputName == null || mInputName.equals("")) && (mInputPwd != null && !mInputPwd.equals(""))) {
                T.showShort(mContext, R.string.input_account);
            } else if ((mInputName != null && !mInputName.equals("")) && (mInputPwd == null || mInputPwd.equals(""))) {
                T.showShort(mContext, R.string.input_password);
            } else {
                T.showShort(mContext, R.string.input_tip);
            }
        }
    }

    public void showApDialog() {
        if (isExitAp) {
            return;
        }
        List<LocalDevice> aplist = APList.aplist;
        if (aplist.size() > 0) {
            if (dialog == null || !dialog.isShowing()) {
                dialog = new NormalDialog(mContext, getString(R.string.ap_device), getString(R.string.ap_device_enter),
                        getString(R.string.ensure), getString(R.string.cancel));
                dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {
                    @Override
                    public void onClick() {
                        SharedPreferencesManager.getInstance().putIsApEnter(mContext, true);
                        NpcCommon.mThreeNum = "517400";
                        Account account = AccountPersist.getInstance().getActiveAccountInfo(mContext);
                        if (null == account) {
                            account = new Account();
                        }
                        account.three_number = "517400";
                        account.rCode1 = "0";
                        account.rCode2 = "0";
                        // account.phone = "0";
                        // account.email = "0";
                        account.sessionId = "0";
                        // account.countryCode = "0";
                        AccountPersist.getInstance().setActiveAccount(mContext, account);
                        NpcCommon.mThreeNum = AccountPersist.getInstance().getActiveAccountInfo(mContext).three_number;
                        Intent i = new Intent(mContext, MainActivity.class);
                        startActivity(i);
                        ((LoginActivity) mContext).finish();
                    }
                });
                dialog.setOnButtonCancelListener(new NormalDialog.OnButtonCancelListener() {
                    @Override
                    public void onClick() {
                        isExitAp = false;
                        dialog.dismiss();
                    }
                });
                dialog.showNormalDialog();
                dialog.setCanceledOnTouchOutside(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showApDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegFilter) {
            isRegFilter = false;
            mContext.unregisterReceiver(mReceiver);
        }
    }


    @Override
    public boolean dispatchHandleMessage(Message msg) {
        switch (msg.what) {
            case ACCOUNT_NO_EXIST:
                T.showShort(mContext, R.string.account_no_exist);
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                break;
            default:
                break;
        }
        return false;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mEmail = username;
            mPassword = password;
        }

        @Override
        protected Object doInBackground(Object... params) {
            Utils.sleepThread(2000);
            return NetManager.getInstance(mContext).login(mEmail, mPassword);
        }

        @Override
        protected void onPostExecute(Object object) {
            mAuthTask = null;


            LoginResult result = NetManager.createLoginResult((JSONObject) object);
            switch (Integer.parseInt(result.error_code)) {
                case NetManager.SESSION_ID_ERROR:
                    Intent i = new Intent();
                    i.setAction(Constants.Action.SESSION_ID_ERROR);
                    MyApp.app.sendBroadcast(i);
                    break;
                case NetManager.CONNECT_CHANGE:
                    new UserLoginTask(mEmail, mPassword).execute();
                    return;
                case NetManager.LOGIN_SUCCESS:
                    if (isDialogCanel) {
                        return;
                    }
                    if (null != dialog) {
                        dialog.dismiss();
                        dialog = null;
                    }

                    if (current_type == Constants.LoginType.PHONE) {
                        SharedPreferencesManager.getInstance().putData(mContext, SharedPreferencesManager.KEY_RECENTNAME, mInputName);
                        SharedPreferencesManager.getInstance().putData(mContext, SharedPreferencesManager.KEY_RECENTPASS, mInputPwd);
                        String code = dfault_count.getText().toString();
                        code = code.substring(1, code.length());
                        SharedPreferencesManager.getInstance().putData(mContext, SharedPreferencesManager.KEY_RECENTCODE, code);
                        SharedPreferencesManager.getInstance().putRecentLoginType(mContext, Constants.LoginType.PHONE);
                    } else {
                        SharedPreferencesManager.getInstance().putData(mContext, SharedPreferencesManager.KEY_RECENTNAME_EMAIL, mInputName);
                        SharedPreferencesManager.getInstance().putData(mContext, SharedPreferencesManager.KEY_RECENTPASS_EMAIL, mInputPwd);
                        SharedPreferencesManager.getInstance().putRecentLoginType(mContext, Constants.LoginType.EMAIL);
                    }

                    String codeStr1 = String.valueOf(Long.parseLong(result.rCode1));
                    String codeStr2 = String.valueOf(Long.parseLong(result.rCode2));
                    Account account = AccountPersist.getInstance().getActiveAccountInfo(mContext);
                    if (null == account) {
                        account = new Account();
                    }
                    account.three_number = result.contactId;
                    account.phone = result.phone;
                    account.email = result.email;
                    account.sessionId = result.sessionId;
                    account.rCode1 = codeStr1;
                    account.rCode2 = codeStr2;
                    account.countryCode = result.countryCode;
                    AccountPersist.getInstance().setActiveAccount(mContext, account);
                    NpcCommon.mThreeNum = AccountPersist.getInstance().getActiveAccountInfo(mContext).three_number;
                    SharedPreferencesManager.getInstance().putIsApEnter(mContext, false);
                    Log.e("loginmode", "login=" + SharedPreferencesManager.getInstance().getIsApEnter(mContext));
                    Intent login = new Intent(mContext, MainActivity.class);
                    startActivity(login);
                    ((LoginActivity) mContext).finish();
                    break;
                case NetManager.LOGIN_USER_UNEXIST:
                    if (dialog != null) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    if (!isDialogCanel) {
                        T.showShort(mContext, R.string.account_no_exist);
                    }
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    break;
                case NetManager.LOGIN_PWD_ERROR:
                    if (dialog != null) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    if (!isDialogCanel) {
                        T.showShort(mContext, R.string.password_error);
                    }
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    break;
                default:
                    if (dialog != null) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    if (!isDialogCanel) {
                        T.showShort(mContext, R.string.loginfail);
                    }
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    @Override
    public int getActivityInfo() {
        return Constants.ActivityInfo.ACTIVITY_LOGINACTIVITY;
    }
}

