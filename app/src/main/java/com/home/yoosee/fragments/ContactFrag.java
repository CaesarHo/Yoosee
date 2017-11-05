package com.home.yoosee.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.home.yoosee.R;
import com.home.yoosee.activitys.AddContactActivity;
import com.home.yoosee.activitys.CallActivity;
import com.home.yoosee.activitys.LocalDeviceListActivity;
import com.home.yoosee.activitys.MainControlActivity;
import com.home.yoosee.activitys.RadarAddFirstActivity;
import com.home.yoosee.adapters.MainAdapter;
import com.home.yoosee.base.DistributedHandler;
import com.home.yoosee.base.MyApp;
import com.home.yoosee.data.Contact;
import com.home.yoosee.data.DataManager;
import com.home.yoosee.data.SharedPreferencesManager;
import com.home.yoosee.entity.LocalDevice;
import com.home.yoosee.global.Constants;
import com.home.yoosee.global.FList;
import com.home.yoosee.global.NpcCommon;
import com.home.yoosee.thread.MainThread;
import com.home.yoosee.utils.T;
import com.home.yoosee.utils.Utils;
import com.home.yoosee.widget.HeaderTextView;
import com.home.yoosee.widget.NormalDialog;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactFrag extends Fragment implements View.OnClickListener ,DistributedHandler.HandlerPart{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.srl)
    SwipeRefreshLayout srl;
    Unbinder unbinder;
    @BindView(R.id.pull_refresh_list)
    RecyclerView recyclerView;
    @BindView(R.id.net_status_bar_top)
    LinearLayout net_work_status_bar;
    @BindView(R.id.local_device_bar_top)
    RelativeLayout local_device_bar_top;
    @BindView(R.id.text_local_device_count)
    TextView text_local_device_count;
    @BindView(R.id.layout_add)
    LinearLayout layout_add;
    @BindView(R.id.layout_contact)
    RelativeLayout layout_contact;
    @BindView(R.id.radar_add)
    RelativeLayout radar_add;
    @BindView(R.id.manually_add)
    RelativeLayout manually_add;
    @BindView(R.id.layout_no_device)
    RelativeLayout layout_no_device;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Context mContext;
    private boolean isRegFilter = false;
    private boolean isDoorBellRegFilter = false;
    private MainAdapter mAdapter;
    boolean refreshEnd = false;
    boolean isActive;
    boolean isCancelLoading;

    NormalDialog dialog;
    private Contact next_contact;
    Handler myHandler = new Handler();
    int count1 = 0;
    int count2 = 0;

    private boolean isHideAdd = true;
    Animation animation_out, animation_in;
    HeaderTextView v;

    public ContactFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactFrag newInstance(String param1, String param2) {
        ContactFrag fragment = new ContactFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        mContext = getActivity();
        MyApp.app.getMainHandler().addHandlerPart(this);
        unbinder = ButterKnife.bind(this, view);
        v = new HeaderTextView(mContext, Utils.getStringByResouceID(R.string.tv_add_device1), Utils.getStringByResouceID(R.string.tv_add_device2));
        Log.e("my", "createContactFrag");
        initComponent();
        regFilter();

        FList flist = FList.getInstance();
        flist.updateOnlineState();
        flist.searchLocalDevice();

        return view;
    }

    public void initComponent() {
        layout_contact.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (!isHideAdd) {
                    hideAdd();
                }
                return false;
            }
        });

        srl.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.light_blue), ContextCompat.getColor(mContext, R.color.pass_red));
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetDataTask().execute();
            }
        });

        mAdapter = new MainAdapter(mContext, this);
        upadataTextView();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (!isHideAdd) {
                    hideAdd();
                }
                return false;
            }
        });
        List<LocalDevice> localDevices = FList.getInstance().getLocalDevices();
        if (localDevices.size() > 0) {
            local_device_bar_top.setVisibility(RelativeLayout.VISIBLE);
            text_local_device_count.setText(String.valueOf(localDevices.size()));
            layout_no_device.setVisibility(View.GONE);
        } else {
            local_device_bar_top.setVisibility(RelativeLayout.GONE);
            layout_no_device.setVisibility(View.VISIBLE);
        }
        List<Contact> contacts = DataManager.findContactByActiveUser(mContext, NpcCommon.mThreeNum);
        animation_out = AnimationUtils.loadAnimation(mContext, R.anim.scale_amplify);
        animation_in = AnimationUtils.loadAnimation(mContext, R.anim.scale_narrow);
    }

    public void upadataTextView() {
        // 添加空列表提示
        if (mAdapter.getItemCount() == 0) {
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 350);
            v.setLayoutParams(params);
//            recyclerView.addHeaderView(v,null,false);
        } else {
//            recyclerView.removeHeaderView(v);
        }
//        if(recyclerView.getHeaderViewsCount()<2){
//            addListHeader();
//        }
    }

    private void addListHeader() {
        // 添加头部
        HeaderTextView header = new HeaderTextView(mContext, "", "");
        AbsListView.LayoutParams headerParams = new AbsListView.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, R.dimen.contact_item_margin);
        header.setLayoutParams(headerParams);
//        recyclerView.addHeaderView(header,null,false);
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Action.REFRESH_CONTANTS);
        filter.addAction(Constants.Action.GET_FRIENDS_STATE);
        filter.addAction(Constants.Action.LOCAL_DEVICE_SEARCH_END);
        filter.addAction(Constants.Action.ACTION_NETWORK_CHANGE);
        filter.addAction(Constants.P2P.ACK_RET_CHECK_PASSWORD);
        filter.addAction(Constants.P2P.RET_GET_REMOTE_DEFENCE);
        filter.addAction(Constants.Action.SETTING_WIFI_SUCCESS);
        filter.addAction(Constants.Action.DIAPPEAR_ADD);
        filter.addAction(Constants.Action.ADD_CONTACT_SUCCESS);
        filter.addAction(Constants.Action.DELETE_DEVICE_ALL);
        // 接收报警ID----------
        filter.addAction(Constants.P2P.RET_GET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.RET_SET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.ACK_RET_SET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.ACK_RET_GET_BIND_ALARM_ID);
        filter.addAction(Constants.Action.SEARCH_AP_DEVICE);
        filter.addAction(Constants.Action.ENTER_DEVICE_SETTING);
        filter.addAction(Constants.Action.CALL_DEVICE);
        mContext.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    public void regDoorbellFilter() {
        IntentFilter filter = new IntentFilter();
        // 接收报警ID----------
        filter.addAction(Constants.P2P.RET_GET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.RET_SET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.ACK_RET_SET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.ACK_RET_GET_BIND_ALARM_ID);
        // 接收报警ID---------------
        mContext.registerReceiver(mDoorbellReceiver, filter);
        isDoorBellRegFilter = true;
    }

    BroadcastReceiver mDoorbellReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.P2P.RET_GET_BIND_ALARM_ID)) {
                String[] data = intent.getStringArrayExtra("data");
                String srcID = intent.getStringExtra("srcID");
                int max_count = intent.getIntExtra("max_count", 0);
                if (data.length >= max_count) {
                    if (!SharedPreferencesManager.getInstance().getIsDoorBellToast(mContext, srcID)) {
                        T.show(mContext, R.string.alarm_push_limit, 2000);
                        SharedPreferencesManager.getInstance().putIsDoorBellToast(srcID, true, mContext);
                    }
                } else {
                    // 处理绑定推送ID
                    mAdapter.setBindAlarmId(srcID, data);
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_BIND_ALARM_ID)) {
                int result = intent.getIntExtra("result", -1);
                String srcID = intent.getStringExtra("srcID");
                if (result == Constants.P2P_SET.BIND_ALARM_ID_SET.SETTING_SUCCESS) {
                    // 设置成功重新获取列表
                    // mAdapter.getBindAlarmId(srcID);
                    mAdapter.setBindAlarmIdSuccess(srcID);
                } else {
                    Log.d("设置失败 = " , "result --- "+result);
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_SET_BIND_ALARM_ID)) {
                int result = intent.getIntExtra("result", -1);
                String srcID = intent.getStringExtra("srcID");
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    Log.d("设置时网络正常 = " , "result --- "+result);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:set alarm bind id");
                    // 设置时网络错误，重新设置
                    mAdapter.setBindAlarmId(srcID);
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_GET_BIND_ALARM_ID)) {
                int result = intent.getIntExtra("result", -1);
                String srcID = intent.getStringExtra("srcID");
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    Log.d("获得列表网络正常 = " , "result --- "+result);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:get alarm bind id");
                    // 获得列表网络错误，重新获取
                    mAdapter.getBindAlarmId(srcID);
                }
            }
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.Action.REFRESH_CONTANTS)) {
                FList flist = FList.getInstance();
                flist.updateOnlineState();
                mAdapter.notifyDataSetChanged();
                List<LocalDevice> localDevices = FList.getInstance().getLocalDevices();
                if (localDevices.size() > 0) {
                    local_device_bar_top.setVisibility(RelativeLayout.VISIBLE);
                    text_local_device_count.setText(String.valueOf(localDevices.size()));
                } else {
                    local_device_bar_top.setVisibility(RelativeLayout.GONE);
                }
            } else if (intent.getAction().equals(Constants.Action.GET_FRIENDS_STATE)) {
                mAdapter.notifyDataSetChanged();
                refreshEnd = true;
            } else if (intent.getAction().equals(Constants.Action.LOCAL_DEVICE_SEARCH_END)) {
                List<LocalDevice> localDevices = FList.getInstance().getLocalDevices();
                if (localDevices.size() > 0) {
                    local_device_bar_top.setVisibility(RelativeLayout.VISIBLE);
                    text_local_device_count.setText(String.valueOf(localDevices.size()));
                } else {
                    local_device_bar_top.setVisibility(RelativeLayout.GONE);
                }
                mAdapter.notifyDataSetChanged();
                Log.e("my", "" + localDevices.size());
            } else if (intent.getAction().equals(Constants.Action.ACTION_NETWORK_CHANGE)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetInfo != null) {
                    if (!activeNetInfo.isConnected()) {
                        T.showShort(mContext, getString(R.string.network_error) + " " + activeNetInfo.getTypeName());
                        net_work_status_bar.setVisibility(RelativeLayout.VISIBLE);
                    } else {
                        net_work_status_bar.setVisibility(RelativeLayout.GONE);
                    }
                } else {
                    T.showShort(mContext, R.string.network_error);
                    net_work_status_bar.setVisibility(RelativeLayout.VISIBLE);
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_CHECK_PASSWORD)) {
                if (!isActive) {
                    return;
                }
                int result = intent.getIntExtra("result", -1);
                if (!isCancelLoading) {
                    if (result == Constants.P2P_SET.ACK_RESULT.ACK_SUCCESS) {
                        if (null != dialog && dialog.isShowing()) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        Intent control = new Intent();
                        control.setClass(mContext, MainControlActivity.class);
                        control.putExtra("contact", next_contact);
                        control.putExtra("type", P2PValue.DeviceType.NPC);
                        mContext.startActivity(control);
                    } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                        if (null != dialog && dialog.isShowing()) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        T.showShort(mContext, R.string.password_error);
                    } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                        if (next_contact != null) {
                            P2PHandler.getInstance().checkPassword(next_contact.contactId, next_contact.contactPassword);
                        }
                    } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_INSUFFICIENT_PERMISSIONS) {
                        if (null != dialog && dialog.isShowing()) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        T.showShort(mContext, R.string.insufficient_permissions);
                    }
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_REMOTE_DEFENCE)) {
                int state = intent.getIntExtra("state", -1);
                String contactId = intent.getStringExtra("contactId");
                Contact contact = FList.getInstance().isContact(contactId);

                if (state == Constants.DefenceState.DEFENCE_STATE_WARNING_NET) {
                    if (null != contact && contact.isClickGetDefenceState) {
                        T.showShort(mContext, R.string.net_error);
                    }
                } else if (state == Constants.DefenceState.DEFENCE_STATE_WARNING_PWD) {
                    if (null != contact && contact.isClickGetDefenceState) {
                        T.showShort(mContext, R.string.password_error);
                    }
                }

                if (null != contact && contact.isClickGetDefenceState) {
                    FList.getInstance().setIsClickGetDefenceState(contactId, false);
                }

                mAdapter.notifyDataSetChanged();
            } else if (intent.getAction().equals(Constants.Action.SETTING_WIFI_SUCCESS)) {
                FList flist = FList.getInstance();
                flist.updateOnlineState();
                flist.searchLocalDevice();
            } else if (intent.getAction().equals(Constants.Action.DIAPPEAR_ADD)) {
                if (!isHideAdd) {
                    hideAdd();
                }
            } else if (intent.getAction().equals(Constants.Action.ADD_CONTACT_SUCCESS)) {
                List<LocalDevice> localDevices = FList.getInstance().getLocalDevices();
                if (localDevices.size() > 0) {
                    local_device_bar_top.setVisibility(RelativeLayout.VISIBLE);
                    text_local_device_count.setText(String.valueOf(localDevices.size()));
                } else {
                    local_device_bar_top.setVisibility(RelativeLayout.GONE);
                }
				layout_no_device.setVisibility(RelativeLayout.GONE);
                upadataTextView();
                recyclerView.setVisibility(View.VISIBLE);
            } else if (intent.getAction().equals(Constants.Action.DELETE_DEVICE_ALL)) {
				layout_no_device.setVisibility(RelativeLayout.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                upadataTextView();
            } else if (intent.getAction().equals(Constants.Action.ENTER_DEVICE_SETTING)) {
                Contact contact = (Contact) intent.getSerializableExtra("contact");
                next_contact = contact;
                dialog = new NormalDialog(mContext);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        isCancelLoading = true;
                    }
                });
                dialog.showLoadingDialog2();
                dialog.setCanceledOnTouchOutside(false);
                isCancelLoading = false;
                P2PHandler.getInstance().checkPassword(contact.contactId, contact.contactPassword);
                myHandler.postDelayed(runnable, 20000);
                count1++;
            } else if (intent.getAction().equals(Constants.Action.CALL_DEVICE)) {
                Contact contact = (Contact) intent.getSerializableExtra("contact");
                Intent call = new Intent();
                call.setClass(mContext, CallActivity.class);
                call.putExtra("callId", contact.contactId);
                call.putExtra("contactName", contact.contactName);
                call.putExtra("contact", contact);
                call.putExtra("isOutCall", true);
                call.putExtra("type", Constants.P2P_TYPE.P2P_TYPE_CALL);
                startActivity(call);
            }
        }
    };

    @OnClick({ R.id.radar_add, R.id.manually_add, R.id.local_device_bar_top})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.radar_add:
                layout_add.setVisibility(LinearLayout.GONE);
                local_device_bar_top.setClickable(true);
                isHideAdd = true;
                Intent radar_add = new Intent(mContext, RadarAddFirstActivity.class);
                mContext.startActivity(radar_add);
                break;
            case R.id.manually_add:
                layout_add.setVisibility(LinearLayout.GONE);
                local_device_bar_top.setClickable(true);
                isHideAdd = true;
                Intent add_contact = new Intent(mContext, AddContactActivity.class);
                mContext.startActivity(add_contact);
                break;
            case R.id.local_device_bar_top:
                Intent i = new Intent(mContext, LocalDeviceListActivity.class);
                mContext.startActivity(i);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchHandleMessage(Message msg) {
        switch (msg.what){
            case Constants.Messager.ADD_DEVICE_MSG:
                MyApp.app.getMainHandler().removeMessages(Constants.Messager.ADD_DEVICE_MSG);
                if (isHideAdd) {
                    showAdd();
                } else {
                    hideAdd();
                }
                break;
            case Constants.Messager.CHANGE_REFRESHING_LABLE:
                String lable = (String) msg.obj;
                // mPullRefreshListView.setHeadLable(lable);
                break;
        }
        return false;
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            // Simulates a background job.
            Log.e("my", "doInBackground");
            FList flist = FList.getInstance();
            flist.searchLocalDevice();

            if (flist.size() == 0) {
                return null;
            }
            refreshEnd = false;
            flist.updateOnlineState();
            flist.getCheckUpdate();
            while (!refreshEnd) {
                Utils.sleepThread(1000);
            }

            Message msg = new Message();
            msg.what = Constants.Messager.CHANGE_REFRESHING_LABLE;
            msg.obj = getString(R.string.pull_to_refresh_refreshing_success_label);
            MyApp.app.getMainHandler().sendMessage(msg);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            // mListItems.addFirst("Added after refresh...");
            // Call onRefreshComplete when the list has been refreshed.
            if (srl != null){
                srl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (srl.isRefreshing()) {
                            srl.setRefreshing(false);
                        }
                    }
                }, 2000);
            }
            super.onPostExecute(result);
        }
    }

//    public void showQuickActionBar(View view, final Contact contact) {
//        if (contact.contactId != null && !contact.contactId.equals("")) {
//            String type = contact.contactId.substring(0, 1);
//            if (contact.contactType == P2PValue.DeviceType.PHONE) {
//                showQuickActionBar_phone(view.findViewById(R.id.user_icon), contact);
//            } else if (contact.contactType == P2PValue.DeviceType.NPC) {
//                showQuickActionBar_npc(view.findViewById(R.id.user_icon), contact);
//            } else if (contact.contactType == P2PValue.DeviceType.IPC) {
//                showQuickActionBar_ipc(view.findViewById(R.id.user_icon), contact);
//            } else if (contact.contactType == P2PValue.DeviceType.DOORBELL) {
//                showQuickActionBar_doorBell(view.findViewById(R.id.user_icon), contact);
//            } else {
//                if (Integer.parseInt(contact.contactId) < 256) {
//                    showQuickActionBar_ipc(view.findViewById(R.id.user_icon), contact);
//                } else {
//                    showQuickActionBar_unknwon(view.findViewById(R.id.user_icon), contact);
//                }
//            }
//        }
//    }

//    private void showQuickActionBar_phone(View view, final Contact contact) {
//        mBar = new QuickActionBar(getActivity());
//        // mBar.addQuickAction(new QuickAction(getActivity(),
//        // R.drawable.ic_action_call_pressed, R.string.chat));
//        mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_message_pressed, R.string.message));
//        mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_modify_pressed, R.string.edit));
//        mBar.setOnQuickActionClickListener(new OnQuickActionClickListener() {
//            @Override
//            public void onQuickActionClicked(QuickActionWidget widget, int position) {
//                switch (position) {
//                    case 0:
//                        break;
//                    case 1:
//                        Intent modify = new Intent();
//                        modify.setClass(mContext, ModifyContactActivity.class);
//                        modify.putExtra("contact", contact);
//                        mContext.startActivity(modify);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
//
//        mBar.show(view);
//    }

//    private void showQuickActionBar_npc(View view, final Contact contact) {
//        mBar = new QuickActionBar(getActivity());
//        // mBar.addQuickAction(new QuickAction(getActivity(),
//        // R.drawable.ic_action_monitor_pressed, R.string.monitor));
//        if (NpcCommon.mThreeNum.equals("517400")) {
//            mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_playback_pressed, R.string.playback));
//            mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_control_pressed, R.string.control));
//            mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_modify_pressed, R.string.edit));
//            mBar.setOnQuickActionClickListener(new OnQuickActionClickListener() {
//                @Override
//                public void onQuickActionClicked(QuickActionWidget widget, int position) {
//                    switch (position) {
//                        case 0:
//                            Intent playback = new Intent();
//                            playback.setClass(mContext, PlayBackListActivity.class);
//                            playback.putExtra("contact", contact);
//                            mContext.startActivity(playback);
//                            break;
//                        case 1:
//                            if (contact.contactId == null || contact.contactId.equals("")) {
//                                T.showShort(mContext, R.string.username_error);
//                                return;
//                            }
//                            if (contact.contactPassword == null || contact.contactPassword.equals("")) {
//                                T.showShort(mContext, R.string.password_error);
//                                return;
//                            }
//                            next_contact = contact;
//                            dialog = new NormalDialog(mContext);
//                            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                                @Override
//                                public void onCancel(DialogInterface arg0) {
//                                    isCancelLoading = true;
//                                }
//                            });
//                            dialog.showLoadingDialog2();
//                            dialog.setCanceledOnTouchOutside(false);
//                            isCancelLoading = false;
//
//                            P2PHandler.getInstance().checkPassword(contact.contactId, contact.contactPassword);
//                            break;
//                        case 2:
//                            Intent modify = new Intent();
//                            modify.setClass(mContext, ModifyContactActivity.class);
//                            modify.putExtra("contact", contact);
//                            mContext.startActivity(modify);
//                            break;
//                        default:
//                            break;
//                    }
//                }
//
//            });
//            mBar.show(view);
//        } else {
//            mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_call_pressed, R.string.chat));
//            mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_playback_pressed, R.string.playback));
//            mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_control_pressed, R.string.control));
//            mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_modify_pressed, R.string.edit));
//            mBar.setOnQuickActionClickListener(new OnQuickActionClickListener() {
//                @Override
//                public void onQuickActionClicked(QuickActionWidget widget, int position) {
//                    switch (position) {
//                        case 0:
//                            if (contact.contactId == null || contact.contactId.equals("")) {
//                                T.showShort(mContext, R.string.username_error);
//                                return;
//                            }
//                            Intent call = new Intent();
//                            call.setClass(mContext, CallActivity.class);
//                            call.putExtra("callId", contact.contactId);
//                            call.putExtra("contactName", contact.contactName);
//                            call.putExtra("contact", contact);
//                            call.putExtra("isOutCall", true);
//                            call.putExtra("type", Constants.P2P_TYPE.P2P_TYPE_CALL);
//                            startActivity(call);
//                            break;
//                        case 1:
//                            Intent playback = new Intent();
//                            playback.setClass(mContext, PlayBackListActivity.class);
//                            playback.putExtra("contact", contact);
//                            mContext.startActivity(playback);
//                            break;
//                        case 2:
//                            if (contact.contactId == null || contact.contactId.equals("")) {
//                                T.showShort(mContext, R.string.username_error);
//                                return;
//                            }
//                            if (contact.contactPassword == null || contact.contactPassword.equals("")) {
//                                T.showShort(mContext, R.string.password_error);
//                                return;
//                            }
//                            next_contact = contact;
//                            dialog = new NormalDialog(mContext);
//                            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                                @Override
//                                public void onCancel(DialogInterface arg0) {
//                                    isCancelLoading = true;
//                                }
//                            });
//                            dialog.showLoadingDialog2();
//                            dialog.setCanceledOnTouchOutside(false);
//                            isCancelLoading = false;
//                            P2PHandler.getInstance().checkPassword(contact.contactId, contact.contactPassword);
//                            myHandler.postDelayed(runnable, 20000);
//                            count1++;
//                            break;
//                        case 3:
//                            Intent modify = new Intent();
//                            modify.setClass(mContext, ModifyContactActivity.class);
//                            modify.putExtra("contact", contact);
//                            mContext.startActivity(modify);
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            });
//            mBar.show(view);
//        }
//    }

//    private void showQuickActionBar_ipc(View view, final Contact contact) {
//        mBar = new QuickActionBar(getActivity());
//        mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_playback_pressed, R.string.playback));
//        mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_control_pressed, R.string.sets_tab));
//        mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_modify_pressed, R.string.edit));
//        mBar.setOnQuickActionClickListener(new OnQuickActionClickListener() {
//            @Override
//            public void onQuickActionClicked(QuickActionWidget widget, int position) {
//                switch (position) {
//                    case 0:
//                        Intent playback = new Intent();
//                        playback.setClass(mContext, PlayBackListActivity.class);
//                        playback.putExtra("contact", contact);
//                        mContext.startActivity(playback);
//                        break;
//                    case 1:
//                        if (contact.contactId == null || contact.contactId.equals("")) {
//                            T.showShort(mContext, R.string.username_error);
//                            return;
//                        }
//                        if (contact.contactPassword == null || contact.contactPassword.equals("")) {
//                            T.showShort(mContext, R.string.password_error);
//                            return;
//                        }
//                        next_contact = contact;
//                        dialog = new NormalDialog(mContext);
//                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                            @Override
//                            public void onCancel(DialogInterface arg0) {
//                                isCancelLoading = true;
//                            }
//                        });
//                        dialog.showLoadingDialog2();
//                        dialog.setCanceledOnTouchOutside(false);
//                        isCancelLoading = false;
//                        P2PHandler.getInstance().checkPassword(contact.contactId, contact.contactPassword);
//                        myHandler.postDelayed(runnable, 20000);
//                        count1++;
//                        break;
//                    case 2:
//                        Intent modify = new Intent();
//                        modify.setClass(mContext, ModifyContactActivity.class);
//                        modify.putExtra("contact", contact);
//                        mContext.startActivity(modify);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
//        mBar.show(view);
//    }

//    private void showQuickActionBar_doorBell(View view, final Contact contact) {
//        mBar = new QuickActionBar(getActivity());
//        mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_playback_pressed, R.string.playback));
//        mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_control_pressed, R.string.sets_tab));
//        mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_modify_pressed, R.string.edit));
//        mBar.setOnQuickActionClickListener(new OnQuickActionClickListener() {
//            @Override
//            public void onQuickActionClicked(QuickActionWidget widget, int position) {
//                switch (position) {
//                    case 0:
//                        Intent playback = new Intent();
//                        playback.setClass(mContext, PlayBackListActivity.class);
//                        playback.putExtra("contact", contact);
//                        mContext.startActivity(playback);
//                        break;
//                    case 1:
//                        if (contact.contactId == null || contact.contactId.equals("")) {
//                            T.showShort(mContext, R.string.username_error);
//                            return;
//                        }
//                        if (contact.contactPassword == null || contact.contactPassword.equals("")) {
//                            T.showShort(mContext, R.string.password_error);
//                            return;
//                        }
//                        next_contact = contact;
//                        dialog = new NormalDialog(mContext);
//                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                            @Override
//                            public void onCancel(DialogInterface arg0) {
//                                isCancelLoading = true;
//                            }
//                        });
//                        dialog.showLoadingDialog2();
//                        dialog.setCanceledOnTouchOutside(false);
//                        isCancelLoading = false;
//                        P2PHandler.getInstance().checkPassword(contact.contactId,
//                                contact.contactPassword);
//                        break;
//                    case 2:
//                        Intent modify = new Intent();
//                        modify.setClass(mContext, ModifyContactActivity.class);
//                        modify.putExtra("contact", contact);
//                        mContext.startActivity(modify);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
//        mBar.show(view);
//    }

//    private void showQuickActionBar_unknwon(View view, final Contact contact) {
//        mBar = new QuickActionBar(getActivity());
//        mBar.addQuickAction(new QuickAction(getActivity(), R.drawable.ic_action_modify_pressed, R.string.edit));
//
//        mBar.setOnQuickActionClickListener(new OnQuickActionClickListener() {
//            @Override
//            public void onQuickActionClicked(QuickActionWidget widget, int position) {
//                switch (position) {
//                    case 0:
//                        Intent modify = new Intent();
//                        modify.setClass(mContext, ModifyContactActivity.class);
//                        modify.putExtra("contact", contact);
//                        mContext.startActivity(modify);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
//        mBar.show(view);
//    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            count2 = count2 + 1;
            if (count2 == count1) {
                if (dialog != null) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                        T.showShort(mContext, R.string.time_out);
                    }
                }
            }
        }
    };

    @Override
    public void onPause() {
        MainThread.setOpenThread(false);
        super.onPause();
        isActive = false;
        if (isDoorBellRegFilter) {
            mContext.unregisterReceiver(mDoorbellReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainThread.setOpenThread(true);
        regDoorbellFilter();
        isActive = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("my", "onDestroy");
        if (isRegFilter) {
            mContext.unregisterReceiver(mReceiver);
        }
        MyApp.app.getMainHandler().removeHandlerPart(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void hideAdd() {
        layout_add.startAnimation(animation_in);
        layout_add.setVisibility(LinearLayout.GONE);
        local_device_bar_top.setClickable(true);
        isHideAdd = true;
    }

    public void showAdd() {
        layout_add.setVisibility(LinearLayout.VISIBLE);
        layout_add.startAnimation(animation_out);
        local_device_bar_top.setClickable(false);
        isHideAdd = false;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
