package com.home.yoosee.activitys;

import android.os.Bundle;

import com.home.yoosee.R;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.home.yoosee.base.BaseActivity;
import com.home.yoosee.data.Contact;
import com.home.yoosee.data.DataManager;
import com.home.yoosee.global.Constants;
import com.home.yoosee.global.FList;
import com.home.yoosee.global.NpcCommon;
import com.home.yoosee.utils.T;
import com.home.yoosee.utils.Utils;
import com.home.yoosee.widget.MyPassLinearLayout;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddContactActivity extends BaseActivity implements OnClickListener {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.ibtn_right)
    ImageButton mNext;
    @BindView(R.id.tv_right)
    TextView tvRight;
    @BindView(R.id.ibtn_left)
    ImageButton mBack;

    @BindView(R.id.bt_ensure)
    Button ensure;
    @BindView(R.id.input_device_id)
    EditText input_device_id;
    @BindView(R.id.input_contact_name)
    EditText input_device_name;
    @BindView(R.id.input_contact_pwd)
    EditText input_device_password;
    Context mContext;
    Contact mContact;
    Contact saveContact = new Contact();
    private MyPassLinearLayout llPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        ButterKnife.bind(this);
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        mContext = this;
        initCompent();
        String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera";
        File dirFile = new File(path);
        if (dirFile.exists()) {
            Log.e("file", "------");
        }
    }

    public void initCompent() {
        mBack.setImageResource(R.mipmap.back);
        tvTitle.setText(getString(R.string.add_online_device));
        mNext.setVisibility(View.GONE);
        tvRight.setVisibility(View.GONE);
        tvRight.setText(getString(R.string.next));


        input_device_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        llPass = (MyPassLinearLayout) findViewById(R.id.ll_p);
        llPass.setEditextListener(input_device_password);
    }

    @OnClick({R.id.ibtn_right,R.id.ibtn_left,R.id.bt_ensure})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_left:
                this.finish();
                break;
            case R.id.ibtn_right:
                next();
                break;
            case R.id.bt_ensure:
                next();
                break;
            default:
                break;
        }
    }

    public void next() {
        String input_id = input_device_id.getText().toString();
        String input_name = input_device_name.getText().toString();
        String input_pwd = input_device_password.getText().toString();
        if (input_id != null && input_id.trim().equals("")) {
            T.showShort(mContext, R.string.input_contact_id);
            return;
        }
        if (input_id.charAt(0) == '0' || input_id.length() > 9 || !Utils.isNumeric(input_id)) {
            T.show(mContext, R.string.device_id_invalid, Toast.LENGTH_SHORT);
            return;
        }
        if (null != FList.getInstance().isContact(input_id)) {
            T.showShort(mContext, R.string.contact_already_exist);
            return;
        }

        int type;
        if (input_id.charAt(0) == '0') {
            type = P2PValue.DeviceType.PHONE;
        } else {
            type = P2PValue.DeviceType.UNKNOWN;
        }
        if (input_name != null && input_name.trim().equals("")) {
            T.showShort(mContext, R.string.input_contact_name);
            return;
        }
        saveContact.contactId = input_id;
        saveContact.contactType = type;
        saveContact.activeUser = NpcCommon.mThreeNum;
        saveContact.messageCount = 0;
        List<Contact> lists = DataManager.findContactByActiveUser(mContext, NpcCommon.mThreeNum);
        for (Contact c : lists) {
            if (c.contactName.equals(input_name)) {
                T.showShort(mContext, R.string.device_name_exist);
                return;
            }
        }
        if (input_pwd == null || input_pwd.trim().equals("")) {
            T.showShort(this, R.string.input_password);
            return;
//			input_pwd = "";
        }
        if (saveContact.contactType != P2PValue.DeviceType.PHONE) {
            if (input_pwd != null && !input_pwd.trim().equals("")) {
                if (input_pwd.charAt(0) == '0' || input_pwd.length() > 30) {
                    T.showShort(mContext, R.string.device_password_invalid);
                    return;
                }
            }
        }

        List<Contact> contactlist = DataManager.findContactByActiveUser(mContext, NpcCommon.mThreeNum);
        for (Contact contact : contactlist) {
            if (contact.contactId.equals(saveContact.contactId)) {
                T.showShort(mContext, R.string.contact_already_exist);
                return;
            }
        }
        saveContact.contactName = input_name;
        saveContact.userPassword = input_pwd;
        String pwd = P2PHandler.getInstance().EntryPassword(input_pwd);
        saveContact.contactPassword = pwd;
        FList.getInstance().insert(saveContact);
        FList.getInstance().updateLocalDeviceWithLocalFriends();
        sendSuccessBroadcast();
        finish();
    }

    @Override
    public int getActivityInfo() {
        return Constants.ActivityInfo.ACTIVITY_ADDCONTACTACTIVITY;
    }

    public void sendSuccessBroadcast() {
        Intent refreshContans = new Intent();
        refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
        refreshContans.putExtra("contact", saveContact);
        mContext.sendBroadcast(refreshContans);

        Intent createPwdSuccess = new Intent();
        createPwdSuccess.setAction(Constants.Action.UPDATE_DEVICE_FALG);
        createPwdSuccess.putExtra("threeNum", saveContact.contactId);
        mContext.sendBroadcast(createPwdSuccess);

        Intent add_success = new Intent();
        add_success.setAction(Constants.Action.ADD_CONTACT_SUCCESS);
        add_success.putExtra("contact", saveContact);
        mContext.sendBroadcast(add_success);

        Intent refreshNearlyTell = new Intent();
        refreshNearlyTell.setAction(Constants.Action.ACTION_REFRESH_NEARLY_TELL);
        mContext.sendBroadcast(refreshNearlyTell);
        T.showShort(mContext, R.string.add_success);
    }
}

