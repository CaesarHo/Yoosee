package com.home.yoosee.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.home.yoosee.R;
import com.home.yoosee.activitys.AddContactNextActivity;
import com.home.yoosee.activitys.CallActivity;
import com.home.yoosee.activitys.DeviceUpdateActivity;
import com.home.yoosee.activitys.ModifyContactActivity;
import com.home.yoosee.activitys.ModifyNpcPasswordActivity;
import com.home.yoosee.activitys.PlayBackListActivity;
import com.home.yoosee.base.MyApp;
import com.home.yoosee.data.APContact;
import com.home.yoosee.data.Contact;
import com.home.yoosee.data.DataManager;
import com.home.yoosee.data.SharedPreferencesManager;
import com.home.yoosee.entity.LocalDevice;
import com.home.yoosee.fragments.ContactFrag;
import com.home.yoosee.global.Constants;
import com.home.yoosee.global.FList;
import com.home.yoosee.global.NpcCommon;
import com.home.yoosee.utils.T;
import com.home.yoosee.utils.Utils;
import com.home.yoosee.utils.WifiUtils;
import com.home.yoosee.widget.HeaderView;
import com.home.yoosee.widget.NormalDialog;
import com.home.yoosee.widget.PictrueTextView;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wade on 2017/10/29.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private OnItemClickListener mOnItemClickListener;
    private Context context;
    private ContactFrag cf;

    public MainAdapter(Context context, ContactFrag cf) {
        this.context = context;
        this.cf = cf;
        inflater = LayoutInflater.from(context);
    }

//    @Override
//    public Contact getItem(int position) {
//        return FList.getInstance().get(position);
//    }

    @Override
    public int getItemCount() {
        int size = FList.getInstance().size() + FList.getInstance().getAPModeLocalDevices().size();
        return size;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        if (position < FList.getInstance().size()) {
            return 0;
        } else {
            return 1;
        }

    }

//    @Override
//    public int getViewTypeCount() {
//        return 2;
//    }

    //填充onCreateViewHolder方法返回的holder中的控件
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        int size1 = FList.getInstance().size();
        // 显示所有已经添加的设备
        if (position < size1) {
            if (holder == null) {
                return;
            }
            if (holder instanceof MyViewHolder) {
                final Contact contact = FList.getInstance().get(position);
                holder.getName().setText(contact.contactName);
                int deviceType = contact.contactType;
                if (contact.onLineState == Constants.DeviceState.ONLINE) {
                    holder.getHead().updateImage(contact.contactId, false);
                    holder.getOnline_state().setText(R.string.online_state);
                    holder.getOnline_state().setTextColor(ContextCompat.getColor(context,R.color.text_color_blue));
                    if (contact.contactType == P2PValue.DeviceType.UNKNOWN || contact.contactType == P2PValue.DeviceType.PHONE) {
                        holder.getLayout_defence_btn().setVisibility(RelativeLayout.INVISIBLE);
                    } else {
                        holder.getLayout_defence_btn().setVisibility(RelativeLayout.VISIBLE);
                        if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_LOADING) {
                            holder.getProgress_defence().setVisibility(RelativeLayout.VISIBLE);
                            holder.getImage_defence_state().setVisibility(RelativeLayout.INVISIBLE);
                        } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
                            holder.getProgress_defence().setVisibility(RelativeLayout.GONE);
                            holder.getImage_defence_state().setVisibility(RelativeLayout.VISIBLE);
                            holder.getImage_defence_state().setImageResource(R.mipmap.ic_defence_on);

                        } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_OFF) {
                            holder.getProgress_defence().setVisibility(RelativeLayout.GONE);
                            holder.getImage_defence_state().setVisibility(RelativeLayout.VISIBLE);
                            holder.getImage_defence_state().setImageResource(R.mipmap.ic_defence_off);

                        } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_NET) {
                            holder.getProgress_defence().setVisibility(RelativeLayout.GONE);
                            holder.getImage_defence_state().setVisibility(RelativeLayout.VISIBLE);
                            holder.getImage_defence_state().setImageResource(R.mipmap.ic_defence_warning);
                        } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_PWD) {
                            holder.getProgress_defence().setVisibility(RelativeLayout.GONE);
                            holder.getImage_defence_state().setVisibility(RelativeLayout.VISIBLE);
                            holder.getImage_defence_state().setImageResource(R.mipmap.ic_defence_warning);
                        } else if (contact.defenceState == Constants.DefenceState.DEFENCE_NO_PERMISSION) {
                            holder.getProgress_defence().setVisibility(RelativeLayout.GONE);
                            holder.getImage_defence_state().setVisibility(RelativeLayout.VISIBLE);
                            holder.getImage_defence_state().setImageResource(R.mipmap.limit);
                        }
                    }
                    // 如果是门铃且不是访客密码则获取报警推送账号并判断自己在不在其中，如不在则添加(只执行一次)
                    if (deviceType == P2PValue.DeviceType.DOORBELL
                            && contact.defenceState != Constants.DefenceState.DEFENCE_NO_PERMISSION) {
                        if (!getIsDoorBellBind(contact.contactId)) {
                            getBindAlarmId(contact.contactId, contact.contactPassword);
                        } else {

                        }
                    }

                } else {
                    holder.getHead().updateImage(contact.contactId, true);
                    holder.getOnline_state().setText(R.string.offline_state);
                    holder.getOnline_state().setTextColor(context.getResources().getColor(R.color.text_color_gray));
                    holder.getLayout_defence_btn().setVisibility(RelativeLayout.INVISIBLE);
                }

                // 获得布防状态之后判断弱密码

                if (contact.onLineState == Constants.DeviceState.ONLINE
                        && (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_ON || contact.defenceState == Constants.DefenceState.DEFENCE_STATE_OFF)) {
                    if (Utils.isWeakPassword(contact.userPassword)) {
                        holder.getIv_weakpassword().setVisibility(View.VISIBLE);
                    } else {
                        holder.getIv_weakpassword().setVisibility(View.GONE);
                    }
                    if (contact.Update == Constants.P2P_SET.DEVICE_UPDATE.HAVE_NEW_VERSION || contact.Update == Constants.P2P_SET.DEVICE_UPDATE.HAVE_NEW_IN_SD) {
                        holder.getImg_update().setVisibility(ImageView.VISIBLE);
                    } else {
                        holder.getImg_update().setVisibility(ImageView.GONE);
                    }
                    Log.e("update", "contactId=" + contact.contactId + "--" + "update=" + contact.Update);
                } else {
                    holder.getIv_weakpassword().setVisibility(View.GONE);
                    holder.getImg_update().setVisibility(ImageView.GONE);
                }

                switch (deviceType) {
                    case P2PValue.DeviceType.NPC:
                        holder.getLogin_type().setImageResource(R.mipmap.ic_device_type_npc);
                        holder.getCall().setVisibility(View.VISIBLE);
                        holder.getCall_line().setVisibility(View.VISIBLE);
                        holder.getCall().setTextPictrueColor(false);
                        holder.getPlayback().setTextPictrueColor(false);
                        holder.getSet().setTextPictrueColor(false);
                        break;
                    case P2PValue.DeviceType.IPC:
                        holder.getLogin_type().setImageResource(R.mipmap.ic_device_type_ipc);
                        holder.getCall().setVisibility(View.GONE);
                        holder.getCall_line().setVisibility(View.GONE);
                        holder.getCall().setTextPictrueColor(false);
                        holder.getPlayback().setTextPictrueColor(false);
                        holder.getSet().setTextPictrueColor(false);
                        break;
                    case P2PValue.DeviceType.PHONE:
                        holder.getLogin_type().setImageResource(R.mipmap.ic_device_type_phone);
                        holder.getCall().setVisibility(View.GONE);
                        holder.getCall_line().setVisibility(View.GONE);
                        holder.getCall().setTextPictrueColor(false);
                        holder.getPlayback().setTextPictrueColor(false);
                        holder.getSet().setTextPictrueColor(false);
                        break;
                    case P2PValue.DeviceType.DOORBELL:
                        holder.getLogin_type().setImageResource(R.mipmap.ic_device_type_door_bell);
                        holder.getCall().setVisibility(View.GONE);
                        holder.getCall_line().setVisibility(View.GONE);
                        holder.getCall().setTextPictrueColor(false);
                        holder.getPlayback().setTextPictrueColor(false);
                        holder.getSet().setTextPictrueColor(false);
                        break;
                    case P2PValue.DeviceType.UNKNOWN:
                        holder.getLogin_type().setImageResource(R.mipmap.ic_device_type_unknown);
                        holder.getCall().setVisibility(View.GONE);
                        holder.getCall_line().setVisibility(View.GONE);
                        if (Integer.parseInt(contact.contactId) < 256) {
                            holder.getPlayback().setTextPictrueColor(false);
                            holder.getSet().setTextPictrueColor(false);
                        } else {
                            holder.getPlayback().setTextPictrueColor(true);
                            holder.getSet().setTextPictrueColor(true);
                        }
                        break;
                    default:
                        holder.getLogin_type().setImageResource(R.mipmap.ic_device_type_unknown);
                        holder.getCall().setVisibility(View.GONE);
                        holder.getCall_line().setVisibility(View.GONE);
                        holder.getPlayback().setTextPictrueColor(true);
                        holder.getSet().setTextPictrueColor(true);
                        break;
                }
                if (contact.messageCount > 0) {
                    TextView msgCount = holder.getMsgCount();
                    msgCount.setVisibility(RelativeLayout.VISIBLE);
                    if (contact.messageCount > 10) {
                        msgCount.setText("10+");
                    } else {
                        msgCount.setText(contact.messageCount + "");
                    }
                } else {
                    holder.getMsgCount().setVisibility(RelativeLayout.GONE);
                }
                if (deviceType == P2PValue.DeviceType.NPC
                        || deviceType == P2PValue.DeviceType.IPC
                        || deviceType == P2PValue.DeviceType.DOORBELL) {
                    holder.getHead().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            if (isNeedModifyPwd(contact)) {
                                Intent modify_pwd = new Intent(context, ModifyNpcPasswordActivity.class);
                                modify_pwd.putExtra("contact", contact);
                                modify_pwd.putExtra("isWeakPwd", true);
                                context.startActivity(modify_pwd);
                                return;
                            }
                            if (null != FList.getInstance().isContactUnSetPassword(contact.contactId)) {
                                return;
                            }
                            if (contact.contactId == null || contact.contactId.equals("")) {
                                T.showShort(context, R.string.username_error);
                                return;
                            }
                            if (contact.contactPassword == null || contact.contactPassword.equals("")) {
                                T.showShort(context, R.string.password_error);
                                return;
                            }
                            String ipAdress = FList.getInstance().getCompleteIPAddress(contact.contactId);
                            Intent monitor = new Intent();
                            monitor.setClass(context, CallActivity.class);
                            monitor.putExtra("callId", contact.contactId);
                            monitor.putExtra("contactName", contact.contactName);
                            monitor.putExtra("password", contact.contactPassword);
                            monitor.putExtra("contact", contact);
                            if (contact.rtspflag == 1) {
                                if (ipAdress.length() > 0) {
                                    monitor.putExtra("connectType", 1);
                                    monitor.putExtra("ipAddress", ipAdress);
                                }
                            }
                            monitor.putExtra("isOutCall", true);
                            monitor.putExtra("type", Constants.P2P_TYPE.P2P_TYPE_MONITOR);
                            monitor.putExtra("contactType", contact.contactType);
                            context.startActivity(monitor);
                        }

                    });
                    holder.getHeader_icon_play().setVisibility(RelativeLayout.VISIBLE);
                } else if (deviceType == P2PValue.DeviceType.PHONE) {
                    holder.getHead().setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            if (isNeedModifyPwd(contact)) {
                                Intent modify_pwd = new Intent(context, ModifyNpcPasswordActivity.class);
                                modify_pwd.putExtra("contact", contact);
                                modify_pwd.putExtra("isWeakPwd", true);
                                context.startActivity(modify_pwd);
                                return;
                            }
                            if (contact.contactId == null
                                    || contact.contactId.equals("")) {
                                T.showShort(context, R.string.username_error);
                                return;
                            }

                            Intent call = new Intent();
                            call.setClass(context, CallActivity.class);
                            call.putExtra("callId", contact.contactId);
                            call.putExtra("contact", contact);
                            call.putExtra("isOutCall", true);
                            call.putExtra("type", Constants.P2P_TYPE.P2P_TYPE_CALL);
                            context.startActivity(call);
                        }

                    });
                    holder.getHeader_icon_play().setVisibility(RelativeLayout.VISIBLE);
                } else {
                    if (Integer.parseInt(contact.contactId) < 256) {
                        holder.getHead().setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                Intent i = new Intent();
                                i.setClass(context, CallActivity.class);
                                i.putExtra("callId", contact.contactId);
                                i.putExtra("contactName", contact.contactName);
                                i.putExtra("password", contact.contactPassword);
                                i.putExtra("contact", contact);
                                i.putExtra("isOutCall", true);
                                if (contact.rtspflag == 1) {
                                    i.putExtra("connectType", 1);
                                    String ipAddress = "";
                                    try {
                                        ipAddress = Utils.getIntentAddress(context, contact.contactId);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    i.putExtra("ipAddress", ipAddress);
                                }

                                i.putExtra("type",
                                        Constants.P2P_TYPE.P2P_TYPE_MONITOR);
                                context.startActivity(i);

                            }
                        });
                    } else {
                        holder.getHead().setOnClickListener(null);
                        holder.getHeader_icon_play().setVisibility(RelativeLayout.GONE);
                    }
                }

                holder.getLayout_defence_btn().setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                if (isNeedModifyPwd(contact)) {
                                    Intent modify_pwd = new Intent(context, ModifyNpcPasswordActivity.class);
                                    modify_pwd.putExtra("contact", contact);
                                    modify_pwd.putExtra("isWeakPwd", true);
                                    context.startActivity(modify_pwd);
                                    return;
                                }
                                if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_NET
                                        || contact.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_PWD) {
                                    holder.getProgress_defence().setVisibility(RelativeLayout.VISIBLE);
                                    holder.getImage_defence_state().setVisibility(RelativeLayout.INVISIBLE);
                                    Log.e("defence", "contactid=" + contact.contactId + "--" + "Password=" + contact.contactPassword);
                                    P2PHandler.getInstance().getDefenceStates(contact.contactId, contact.contactPassword);
                                    FList.getInstance().setIsClickGetDefenceState(contact.contactId, true);
                                } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
                                    holder.getProgress_defence().setVisibility(RelativeLayout.VISIBLE);
                                    holder.getImage_defence_state().setVisibility(RelativeLayout.INVISIBLE);
                                    P2PHandler.getInstance().setRemoteDefence(
                                            contact.contactId,
                                            contact.contactPassword,
                                            Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_OFF);
                                    FList.getInstance().setIsClickGetDefenceState(contact.contactId, true);
                                } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_OFF) {
                                    holder.getProgress_defence().setVisibility(RelativeLayout.VISIBLE);
                                    holder.getImage_defence_state().setVisibility(RelativeLayout.INVISIBLE);
                                    P2PHandler.getInstance().setRemoteDefence(contact.contactId, contact.contactPassword,
                                            Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_ON);
                                    FList.getInstance().setIsClickGetDefenceState(contact.contactId, true);
                                }
                            }

                        });
                ((MyViewHolder) holder).itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {
                        Intent it = new Intent();
                        it.setAction(Constants.Action.DIAPPEAR_ADD);
                        context.sendBroadcast(it);
                        return false;
                    }
                });
                ((MyViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        LocalDevice localDevice = FList.getInstance().isContactUnSetPassword(contact.contactId);
                        if (null != localDevice) {
                            Contact saveContact = new Contact();
                            saveContact.contactId = localDevice.contactId;
                            saveContact.contactType = localDevice.type;
                            saveContact.messageCount = 0;
                            saveContact.activeUser = NpcCommon.mThreeNum;

                            Intent modify = new Intent();
                            modify.setClass(context, AddContactNextActivity.class);
                            modify.putExtra("isCreatePassword", true);
                            modify.putExtra("contact", saveContact);
                            String mark = localDevice.address.getHostAddress();
                            modify.putExtra("ipFlag", mark.substring(mark.lastIndexOf(".") + 1, mark.length()));
                            context.startActivity(modify);
                            return;
                        } else {
                            if (isNeedModifyPwd(contact)) {
                                Intent modify_pwd = new Intent(context, ModifyNpcPasswordActivity.class);
                                modify_pwd.putExtra("contact", contact);
                                modify_pwd.putExtra("isWeakPwd", true);
                                context.startActivity(modify_pwd);
                                return;
                            }
                        }
                    }

                });

                ((MyViewHolder) holder).itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View arg0) {
                        NormalDialog dialog = new NormalDialog(context, context
                                .getResources().getString(R.string.delete_contact),
                                context.getResources().getString(R.string.are_you_sure_delete) + " " + contact.contactId + "?", context
                                .getResources().getString(R.string.delete),
                                context.getResources().getString(R.string.cancel));
                        dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

                            @Override
                            public void onClick() {
                                FList.getInstance().delete(contact, position, handler);
                                File file = new File(Constants.Image.USER_HEADER_PATH + NpcCommon.mThreeNum + "/" + contact.contactId);
                                Utils.deleteFile(file);
                                if (position == 0 && FList.getInstance().size() == 0 && FList.getInstance().getAPModeLocalDevices().size() == 0) {
                                    Intent it = new Intent();
                                    it.setAction(Constants.Action.DELETE_DEVICE_ALL);
                                    MyApp.app.sendBroadcast(it);
                                }
                            }
                        });
                        dialog.showDialog();
                        return true;
                    }

                });
                holder.getIv_weakpassword().setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent modify_pwd = new Intent(context, ModifyNpcPasswordActivity.class);
                        modify_pwd.putExtra("contact", contact);
                        modify_pwd.putExtra("isWeakPwd", true);
                        context.startActivity(modify_pwd);
                    }
                });
                holder.getCall().setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (isNeedModifyPwd(contact)) {
                            Intent modify_pwd = new Intent(context, ModifyNpcPasswordActivity.class);
                            modify_pwd.putExtra("contact", contact);
                            modify_pwd.putExtra("isWeakPwd", true);
                            context.startActivity(modify_pwd);
                            return;
                        }
                        if (contact.contactType == P2PValue.DeviceType.UNKNOWN && Integer.valueOf(contact.contactId) > 256) {
                            return;
                        }
                        if (contact.contactId == null || contact.contactId.equals("")) {
                            T.showShort(context, R.string.username_error);
                            return;
                        }
                        if (contact.contactPassword == null || contact.contactPassword.equals("")) {
                            T.showShort(context, R.string.password_error);
                            return;
                        }
                        Intent i = new Intent();
                        i.putExtra("contact", contact);
                        i.setAction(Constants.Action.CALL_DEVICE);
                        context.sendBroadcast(i);
                    }
                });
                holder.getPlayback().setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (isNeedModifyPwd(contact)) {
                            Intent modify_pwd = new Intent(context, ModifyNpcPasswordActivity.class);
                            modify_pwd.putExtra("contact", contact);
                            modify_pwd.putExtra("isWeakPwd", true);
                            context.startActivity(modify_pwd);
                            return;
                        }
                        if (contact.contactType == P2PValue.DeviceType.UNKNOWN && Integer.valueOf(contact.contactId) > 256) {
                            return;
                        }
                        Intent playback = new Intent();
                        playback.setClass(context, PlayBackListActivity.class);
                        playback.putExtra("contact", contact);
                        context.startActivity(playback);
                    }
                });
                holder.getEdit().setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (isNeedModifyPwd(contact)) {
                            Intent modify_pwd = new Intent(context, ModifyNpcPasswordActivity.class);
                            modify_pwd.putExtra("contact", contact);
                            modify_pwd.putExtra("isWeakPwd", true);
                            context.startActivity(modify_pwd);
                            return;
                        }
                        Intent modify = new Intent();
                        modify.setClass(context, ModifyContactActivity.class);
                        modify.putExtra("contact", contact);
                        context.startActivity(modify);
                    }
                });
                holder.getSet().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (isNeedModifyPwd(contact)) {
                            Intent modify_pwd = new Intent(context, ModifyNpcPasswordActivity.class);
                            modify_pwd.putExtra("contact", contact);
                            modify_pwd.putExtra("isWeakPwd", true);
                            context.startActivity(modify_pwd);
                            return;
                        }
                        if (contact.contactType == P2PValue.DeviceType.UNKNOWN && Integer.valueOf(contact.contactId) > 256) {
                            return;
                        }
                        if (contact.contactId == null || contact.contactId.equals("")) {
                            T.showShort(context, R.string.username_error);
                            return;
                        }
                        if (contact.contactPassword == null || contact.contactPassword.equals("")) {
                            T.showShort(context, R.string.password_error);
                            return;
                        }
                        Intent i = new Intent();
                        i.putExtra("contact", contact);
                        i.setAction(Constants.Action.ENTER_DEVICE_SETTING);
                        context.sendBroadcast(i);
                    }
                });
                holder.getImg_update().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (contact.contactType == P2PValue.DeviceType.UNKNOWN && Integer.valueOf(contact.contactId) > 256) {
                            return;
                        }
                        Intent check_update = new Intent(context, DeviceUpdateActivity.class);
                        check_update.putExtra("contact", contact);
                        check_update.putExtra("isUpdate", true);
                        context.startActivity(check_update);
                    }
                });
            }
        }
//        else {
//            View view = convertView;
//            final ViewHolder3 holder3;
//            if (view == null) {
//                view = LayoutInflater.from(context).inflate(R.layout.list_contact_item3, null);
//                holder3 = new ViewHolder3();
//                TextView name = (TextView) view.findViewById(R.id.user_name);
//                holder3.setName(name);
//                HeaderView head = (HeaderView) view.findViewById(R.id.user_icon);
//                holder3.setHead(head);
//                view.setTag(holder3);
//            } else {
//                holder3 = (ViewHolder3) view.getTag();
//            }
//            final LocalDevice apdevice = FList.getInstance().getAPDdeviceByPosition(position - size1);
//            holder3.name.setText(apdevice.name);
//            holder3.getHead().updateImage(apdevice.contactId, true);
//            view.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View arg0) {
//                    Contact saveContact = new Contact();
//                    saveContact.contactId = apdevice.contactId;
//                    saveContact.contactName = apdevice.name;
//                    saveContact.contactType = apdevice.type;
//                    saveContact.contactFlag = apdevice.flag;
//                    try {
//                        saveContact.ipadressAddress = InetAddress.getByName("192.168.1.1");
//                    } catch (UnknownHostException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                    saveContact.messageCount = 0;
//                    saveContact.activeUser = NpcCommon.mThreeNum;
//                    String mark = apdevice.address.getHostAddress();
//
//                    Intent modify = new Intent();
//                    modify.setClass(context, AddApDeviceActivity.class);
//                    modify.putExtra("isCreatePassword", false);
//                    if (WifiUtils.getInstance().isConnectWifi(apdevice.name)) {
//                        APContact cona = DataManager.findAPContactByActiveUserAndContactId(context, NpcCommon.mThreeNum, apdevice.contactId);
//                        if (cona != null && cona.Pwd != null && cona.Pwd.length() > 0) {
//                            saveContact.contactPassword = cona.Pwd;
//                            modify.putExtra("isAPModeConnect", 1);
//                        } else {
//                            modify.putExtra("isAPModeConnect", 0);
//                        }
//                    } else {
//                        modify.putExtra("isAPModeConnect", 0);
//                    }
//                    modify.putExtra("contact", saveContact);
//                    modify.putExtra("ipFlag", "1");
//                    context.startActivity(modify);
//                }
//            });
//        }
    }

    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_contact_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private HeaderView head;
        private TextView name;
        private TextView online_state;
        private ImageView login_type;
        private TextView msgCount;
        private ImageView header_icon_play;

        private RelativeLayout layout_defence_btn;
        private ImageView image_defence_state;
        private ProgressBar progress_defence;
        private PictrueTextView call;
        private PictrueTextView playback;
        private PictrueTextView edit;
        private PictrueTextView set;
        private ImageView iv_weakpassword;
        private View call_line;
        private ImageView img_update;


        public ImageView getImg_update() {
            return img_update;
        }

        public void setImg_update(ImageView img_update) {
            this.img_update = img_update;
        }

        public PictrueTextView getCall() {
            return call;
        }

        public void setCall(PictrueTextView call) {
            this.call = call;
        }

        public PictrueTextView getPlayback() {
            return playback;
        }

        public void setPlayback(PictrueTextView playback) {
            this.playback = playback;
        }

        public PictrueTextView getEdit() {
            return edit;
        }

        public void setEdit(PictrueTextView edit) {
            this.edit = edit;
        }

        public PictrueTextView getSet() {
            return set;
        }

        public void setSet(PictrueTextView set) {
            this.set = set;
        }

        public ImageView getIv_weakpassword() {
            return iv_weakpassword;
        }

        public void setIv_weakpassword(ImageView iv_weakpassword) {
            this.iv_weakpassword = iv_weakpassword;
        }

        public View getCall_line() {
            return call_line;
        }

        public void setCall_line(View call_line) {
            this.call_line = call_line;
        }

        public TextView getMsgCount() {
            return msgCount;
        }

        public void setMsgCount(TextView msgCount) {
            this.msgCount = msgCount;
        }

        public ImageView getLogin_type() {
            return login_type;
        }

        public void setLogin_type(ImageView login_type) {
            this.login_type = login_type;
        }

        public TextView getOnline_state() {
            return online_state;
        }

        public void setOnline_state(TextView online_state) {
            this.online_state = online_state;
        }

        public HeaderView getHead() {
            return head;
        }

        public void setHead(HeaderView head) {
            this.head = head;
        }

        public TextView getName() {
            return name;
        }

        public void setName(TextView name) {
            this.name = name;
        }

        public ImageView getHeader_icon_play() {
            return header_icon_play;
        }

        public void setHeader_icon_play(ImageView header_icon_play) {
            this.header_icon_play = header_icon_play;
        }

        public RelativeLayout getLayout_defence_btn() {
            return layout_defence_btn;
        }

        public void setLayout_defence_btn(RelativeLayout layout_defence_btn) {
            this.layout_defence_btn = layout_defence_btn;
        }

        public ImageView getImage_defence_state() {
            return image_defence_state;
        }

        public void setImage_defence_state(ImageView image_defence_state) {
            this.image_defence_state = image_defence_state;
        }

        public ProgressBar getProgress_defence() {
            return progress_defence;
        }

        public void setProgress_defence(ProgressBar progress_defence) {
            this.progress_defence = progress_defence;
        }

        public MyViewHolder(View view) {
            super(view);
            HeaderView head = (HeaderView) view.findViewById(R.id.user_icon);
            setHead(head);
            TextView name = (TextView) view.findViewById(R.id.user_name);
            setName(name);
            TextView onlineState = (TextView) view.findViewById(R.id.online_state);
            setOnline_state(onlineState);
            ImageView loginType = (ImageView) view.findViewById(R.id.login_type);
            setLogin_type(loginType);
            TextView msgCount = (TextView) view.findViewById(R.id.msgCount);
            setMsgCount(msgCount);
            ImageView headerIconPlay = (ImageView) view.findViewById(R.id.header_icon_play);
            setHeader_icon_play(headerIconPlay);

            RelativeLayout layout_defence_btn = (RelativeLayout) view.findViewById(R.id.layout_defence_btn);
            setLayout_defence_btn(layout_defence_btn);
            ImageView image_defence_state = (ImageView) view.findViewById(R.id.image_defence_state);
            setImage_defence_state(image_defence_state);
            ProgressBar progress_defence = (ProgressBar) view.findViewById(R.id.progress_defence);
            setProgress_defence(progress_defence);
            setCall((PictrueTextView) view.findViewById(R.id.call));
            setPlayback((PictrueTextView) view.findViewById(R.id.playback));
            setEdit((PictrueTextView) view.findViewById(R.id.edit));
            setSet((PictrueTextView) view.findViewById(R.id.set));
            setCall_line((View) view.findViewById(R.id.call_line));
            setIv_weakpassword((ImageView) view.findViewById(R.id.iv_weakpassword));
            setImg_update((ImageView) view.findViewById(R.id.img_update));
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);

        void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }


//    class ViewHolder {
//
//
//    }
//
//    class ViewHolder2 {
//        public TextView name;
//        public ImageView device_type;
//
//        public TextView getName() {
//            return name;
//        }
//
//        public void setName(TextView name) {
//            this.name = name;
//        }
//
//        public ImageView getDevice_type() {
//            return device_type;
//        }
//
//        public void setDevice_type(ImageView device_type) {
//            this.device_type = device_type;
//        }
//
//    }
//    class ViewHolder3{
//        public TextView name;
//        private HeaderView head;
//        public TextView getName() {
//            return name;
//        }
//        public void setName(TextView name) {
//            this.name = name;
//        }
//        public HeaderView getHead() {
//            return head;
//        }
//        public void setHead(HeaderView head) {
//            this.head = head;
//        }
//    }


    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            notifyDataSetChanged();
            return true;
        }
    });

    List<String> doorbells = new ArrayList<String>();
    Map<String, String[]> idMaps = new HashMap<String, String[]>();

    private void getBindAlarmId(String id, String password) {
        if (!doorbells.contains(id)) {
            doorbells.add(id);
        }
        P2PHandler.getInstance().getBindAlarmId(id, password);
    }

    public boolean isNeedModifyPwd(Contact contact) {

        if (contact.onLineState == Constants.DeviceState.ONLINE
                && (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_ON || contact.defenceState == Constants.DefenceState.DEFENCE_STATE_OFF)) {
            if (Utils.isWeakPassword(contact.userPassword)) {
                return true;
            }
        }
        return false;
    }

    public void getAllBindAlarmId() {
        for (String ss : doorbells) {
            getBindAlarmId(ss);
        }
    }

    private int count = 0;// 总请求数计数器
    private int SumCount = 20;// 总请求次数上限

    public void getBindAlarmId(String id) {
        Contact contact = DataManager.findContactByActiveUserAndContactId(context, NpcCommon.mThreeNum, id);
        if (contact != null && count <= SumCount) {
            // 获取绑定id列表
            P2PHandler.getInstance().getBindAlarmId(contact.contactId, contact.contactPassword);
            count++;
        }
    }

    public void setBindAlarmId(String id, String[] ids) {
        int ss = 0;
        String[] new_data;
        for (int i = 0; i < ids.length; i++) {
            if (!NpcCommon.mThreeNum.equals(ids[i])) {
                ss++;
            }
        }
        if (ss == ids.length) {
            // 不包含则设置
            new_data = new String[ids.length + 1];
            for (int i = 0; i < ids.length; i++) {
                new_data[i] = ids[i];
            }
            new_data[new_data.length - 1] = NpcCommon.mThreeNum;
            Contact contact = DataManager.findContactByActiveUserAndContactId(context, NpcCommon.mThreeNum, id);
            P2PHandler.getInstance().setBindAlarmId(contact.contactId, contact.contactPassword, new_data.length, new_data);
        } else {
            new_data = ids;
        }
        idMaps.put(id, new_data);
    }

    public void setBindAlarmId(String Id) {
        Contact contact = DataManager.findContactByActiveUserAndContactId(context, NpcCommon.mThreeNum, Id);
        if (contact != null && (!idMaps.isEmpty())) {
            String[] new_data = idMaps.get(Id);
            P2PHandler.getInstance().setBindAlarmId(contact.contactId, contact.contactPassword, new_data.length, new_data);
        }
    }

    public void setBindAlarmIdSuccess(String doorbellid) {
        SharedPreferencesManager.getInstance().putIsDoorbellBind(doorbellid, true, context);
    }

    private boolean getIsDoorBellBind(String doorbellid) {
        return SharedPreferencesManager.getInstance().getIsDoorbellBind(context, doorbellid);
    }
}
