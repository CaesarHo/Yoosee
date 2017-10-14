package com.home.yoosee.global;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.home.yoosee.base.MyApp;
import com.home.yoosee.data.Contact;
import com.home.yoosee.data.DataManager;
import com.home.yoosee.data.SharedPreferencesManager;
import com.home.yoosee.entity.LocalDevice;
import com.home.yoosee.utils.TcpClient;
import com.home.yoosee.utils.Utils;
import com.home.yoosee.utils.WifiUtils;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.p2p.shake.ShakeManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FList {
	private static FList manager = null;
	private static List<Contact> lists = null;
	private static HashMap<String, Contact> maps = new HashMap<String, Contact>();
	private static List<LocalDevice> localdevices = new ArrayList<LocalDevice>();
	private static List<LocalDevice> tempLocalDevices = new ArrayList<LocalDevice>();
	private static List<LocalDevice> foundLocalDevices = new ArrayList<LocalDevice>();
	private static List<LocalDevice> apdevices = new ArrayList<LocalDevice>();
	// 局域网内搜索到的全部的设备
	private static List<LocalDevice> allLocalDevices = new ArrayList<LocalDevice>();

	public FList() {
		if (null != lists) {
			lists.clear();
		}

		if (null != localdevices) {
			localdevices.clear();
		}
		if (null != foundLocalDevices) {
			foundLocalDevices.clear();
		}
		if (null != allLocalDevices) {
			allLocalDevices.clear();
		}
		manager = this;
		lists = DataManager.findContactByActiveUser(MyApp.app,
				NpcCommon.mThreeNum);
		maps.clear();
		for (Contact contact : lists) {
			maps.put(contact.contactId, contact);
		}
	}

	public static FList getInstance() {
		if (manager == null) {
			manager = new FList();
		}
		return manager;
	}

	public List<Contact> list() {
		return this.lists;
	}

	public HashMap<String, Contact> map() {
		return this.maps;
	}

	public Contact get(int position) {
		if (position >= lists.size()) {
			return null;
		} else {
			return lists.get(position);
		}
	}

	public int getType(String threeNum) {
		Contact contact = maps.get(threeNum);
		if (null == contact) {
			return P2PValue.DeviceType.UNKNOWN;
		} else {
			return contact.contactType;
		}

	}

	public void setType(String threeNum, int type) {
		Contact contact = maps.get(threeNum);
		if (null != contact) {
			contact.contactType = type;
			DataManager.updateContact(MyApp.app, contact);
		}
	}

	public int getState(String threeNum) {
		Contact contact = maps.get(threeNum);
		if (null == contact) {
			return Constants.DeviceState.OFFLINE;
		} else {
			return contact.onLineState;
		}
	}

	public void setState(String threeNum, int state) {
		Contact contact = maps.get(threeNum);
		if (null != contact) {
			contact.onLineState = state;
		}
	}

	public void setDefenceState(String threeNum, int state) {
		Contact contact = maps.get(threeNum);
		if (null != contact) {
			contact.defenceState = state;
			if(state==Constants.DefenceState.DEFENCE_STATE_WARNING_NET||state==Constants.DefenceState.DEFENCE_STATE_WARNING_PWD||state==Constants.DefenceState.DEFENCE_NO_PERMISSION||state==Constants.DefenceState.DEFENCE_STATE_LOADING){
				
			}
		}
	}
    public void setUpdate(String contactId, int state,String cur_version,String up_version){
    	Contact contact = maps.get(contactId);
		if (null != contact) {
			contact.Update = state;
			contact.cur_version=cur_version;
			contact.up_version=up_version;
		}
    }
	public void setIsClickGetDefenceState(String threeNum, boolean bool) {
		Contact contact = maps.get(threeNum);
		if (null != contact) {
			contact.isClickGetDefenceState = bool;
		}
	}

	public int size() {
		return lists.size();
	}

	public void sort() {
		Collections.sort(lists);
	}

	public String getContactId(String ip) {
		for (int i = 0; i < allLocalDevices.size(); i++) {
			String mack = allLocalDevices.get(i).address.getHostName();
			String ipaddress = mack.substring(mack.lastIndexOf(".") + 1,
					mack.length());
			if (ipaddress.equals(ip)) {
				return allLocalDevices.get(i).contactId;
			}
		}
		return "";
	}

	public void delete(Contact contact, int position, Handler handler) {
		maps.remove(contact.contactId);
		lists.remove(position);
		DataManager.deleteContactByActiveUserAndContactId(MyApp.app,
				NpcCommon.mThreeNum, contact.contactId);
		if (contact.contactType == P2PValue.DeviceType.DOORBELL) {// 如果删除门铃则绑定标记为否，下次默认绑定报警ID
			SharedPreferencesManager.getInstance().putIsDoorbellBind(
					contact.contactId, false, MyApp.app);
			SharedPreferencesManager.getInstance().putIsDoorBellToast(
					contact.contactId, false, MyApp.app);
		}
		handler.sendEmptyMessage(0);

		Intent refreshNearlyTell = new Intent();
		refreshNearlyTell
				.setAction(Constants.Action.ACTION_REFRESH_NEARLY_TELL);
		MyApp.app.sendBroadcast(refreshNearlyTell);
	}

	public void insert(Contact contact) {
		Log.e("flist", "insert");
		DataManager.insertContact(MyApp.app, contact);
		lists.add(contact);
		maps.put(contact.contactId, contact);
		String[] contactIds = new String[] { contact.contactId };
		P2PHandler.getInstance().getFriendStatus(contactIds);
	}

	public void update(Contact contact) {
		int i = 0;
		for (Contact u : lists) {
			if (u.contactId.equals(contact.contactId)) {
				lists.set(i, contact);
				break;
			}
			i++;
		}

		maps.put(contact.contactId, contact);
		DataManager.updateContact(MyApp.app, contact);
	}

	public Contact isContact(String contactId) {
		return maps.get(contactId);
	}

	public synchronized void updateOnlineState() {
		// 获取好友在线状态

		FList flist = FList.getInstance();
		if (flist.size() <= 0) {
			Intent friends = new Intent();
			friends.setAction(Constants.Action.GET_FRIENDS_STATE);
			MyApp.app.sendBroadcast(friends);
			return;
		}

		String[] contactIds = new String[flist.size()];
		List<Contact> lists = flist.list();
		int i = 0;
		for (Contact contact : lists) {
			contactIds[i] = contact.contactId;
			i++;
		}
		P2PHandler.getInstance().getFriendStatus(contactIds);
	}
    
	public void getDefenceState() {
		new Thread() {
			public void run() {
				for (int i = 0; i < manager.lists.size(); i++) {
					Contact contact = manager.lists.get(i);
					if ((contact.contactType == P2PValue.DeviceType.DOORBELL
							|| contact.contactType == P2PValue.DeviceType.IPC || contact.contactType == P2PValue.DeviceType.NPC)) {

						P2PHandler.getInstance().getDefenceStates(
								contact.contactId, contact.contactPassword);

					}
				}
			}
		}.start();

	}
	public void getCheckUpdate(){
		new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for(int i=0;i<manager.list().size();i++){
					Contact contact=manager.list().get(i);
					if((contact.contactType == P2PValue.DeviceType.DOORBELL
							|| contact.contactType == P2PValue.DeviceType.IPC || contact.contactType == P2PValue.DeviceType.NPC)){
						if(contact.Update!=Constants.P2P_SET.DEVICE_UPDATE.HAVE_NEW_VERSION&&contact.Update!=Constants.P2P_SET.DEVICE_UPDATE.HAVE_NEW_IN_SD){
							P2PHandler.getInstance().checkDeviceUpdate(contact.contactId,
									contact.contactPassword);					
						}
					}
				}
			}
			
		}.start();
	}

	// public void getPermissions(){
	// new Thread(){
	// public void run(){
	// for(int i=0;i<manager.lists.size();i++){
	// Contact contact = manager.lists.get(i);
	// if(contact.contactType==P2PValue.DeviceType.IPC){
	// P2PHandler.getInstance().checkPassword(contact.contactId,
	// contact.contactPassword);
	// }
	// }
	// }
	// }.start();
	// }

	public synchronized void searchLocalDevice() {

		try {
			ShakeManager.getInstance().setSearchTime(5000);
			ShakeManager.getInstance().setInetAddress(
					Utils.getIntentAddress(MyApp.app));
			ShakeManager.getInstance().setHandler(mHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (ShakeManager.getInstance().shaking()) {
			tempLocalDevices.clear();
		}
//		WifiUtils.getInstance().setHandler(mHandler);
//		WifiUtils.getInstance().getAPmodeDevice();
	}

	public void updateLocalDeviceWithLocalFriends() {
		List<LocalDevice> removeList = new ArrayList<LocalDevice>();
		for (LocalDevice localDevice : localdevices) {
			if (null != manager.isContact(localDevice.getContactId())) {
				removeList.add(localDevice);
			}
		}

		for (LocalDevice localDevice : removeList) {
			localdevices.remove(localDevice);
		}
	}

	public List<LocalDevice> getLocalDevices() {
		return this.localdevices;
	}

	public List<LocalDevice> getUnsetPasswordLocalDevices() {
		List<LocalDevice> datas = new ArrayList<LocalDevice>();

		for (LocalDevice device : this.localdevices) {
			int flag = device.flag;
			if (flag == Constants.DeviceFlag.UNSET_PASSWORD
					&& null == this.isContact(device.contactId)) {
				datas.add(device);
			}
		}
		return datas;
	}

	public String getCompleteIPAddress(String contactid) {
		String ip = "";
		for (LocalDevice ss : tempLocalDevices) {
			if (ss.contactId.equals(contactid)) {
				ip = ss.address.getHostAddress();
			}
		}
		return ip;
	}

	public List<LocalDevice> getSetPasswordLocalDevices() {
		List<LocalDevice> datas = new ArrayList<LocalDevice>();
		for (LocalDevice device : this.localdevices) {
			int flag = device.flag;
			if (flag == Constants.DeviceFlag.ALREADY_SET_PASSWORD
					&& null == this.isContact(device.contactId)) {
				datas.add(device);
			}
		}
		return datas;
	}

	/**
	 * 获得AP模式设备
	 * 
	 * @return
	 */
	public List<LocalDevice> getAPModeLocalDevices() {
		List<LocalDevice> datas = new ArrayList<LocalDevice>();
		for (LocalDevice device : this.localdevices) {
			int flag = device.flag;
			if (flag == Constants.DeviceFlag.UNKNOW
					|| flag == Constants.DeviceFlag.AP_MODE
					&& null == this.isContact(device.contactId)) {
				datas.add(device);
			}
		}
		if (apdevices.size() != datas.size()) {
			apdevices = datas;
			Intent i = new Intent();
			i.setAction(Constants.Action.SEARCH_AP_DEVICE);
			i.putExtra("apdevice_size", datas.size());
			MyApp.app.sendBroadcast(i);
		}
		apdevices = datas;
		return datas;
	}

	public LocalDevice getAPDdeviceByPosition(int position) {
		if (position >= apdevices.size()) {
			return null;
		} else {
			return apdevices.get(position);
		}
	}

	public LocalDevice isContactUnSetPassword(String contactId) {
		if (null == this.isContact(contactId)) {
			return null;
		}

		for (LocalDevice device : this.foundLocalDevices) {
			if (device.contactId.equals(contactId)) {
				if (device.flag == Constants.DeviceFlag.UNSET_PASSWORD) {
					return device;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	public void updateLocalDeviceFlag(String contactId, int flag) {
		for (LocalDevice device : this.localdevices) {
			if (device.contactId.equals(contactId)) {
				device.flag = flag;
				return;
			}
		}
	}

    public InetAddress getLocalDeviceIp(String id){
    	for(int i=0;i<allLocalDevices.size();i++){
    		if(allLocalDevices.get(i).contactId.equals(id)){
//    			String mark=allLocalDevices.get(i).address.getHostAddress();
//    			return mark.substring(mark.lastIndexOf(".")+1,mark.length());
    			return allLocalDevices.get(i).address;
    		}
    	}
    	return null;
    	
    }


//	public String getLocalDeviceIp(String id) {
//		for (int i = 0; i < allLocalDevices.size(); i++) {
//			if (allLocalDevices.get(i).contactId.equals(id)) {
//				String mark = allLocalDevices.get(i).address.getHostAddress();
//				return mark.substring(mark.lastIndexOf(".") + 1, mark.length());
//			}
//		}
//		return "";
//
//	}


	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case ShakeManager.HANDLE_ID_SEARCH_END:
				localdevices.clear();
				allLocalDevices.clear();
				foundLocalDevices.clear();
				for (LocalDevice localDevice : tempLocalDevices) {
					localdevices.add(localDevice);
					foundLocalDevices.add(localDevice);
					allLocalDevices.add(localDevice);
				}
				for(int i=0;i<lists.size();i++){
					if(!searchLocalContact(lists.get(i).contactId)){
						lists.get(i).ipadressAddress=null;
					}
				}
				updateLocalDeviceWithLocalFriends();
				Intent i = new Intent();
				i.setAction(Constants.Action.LOCAL_DEVICE_SEARCH_END);
				MyApp.app.sendBroadcast(i);
				break;
			case ShakeManager.HANDLE_ID_RECEIVE_DEVICE_INFO:
				Bundle bundle = msg.getData();
				String id = bundle.getString("id");
				String name = bundle.getString("name");
				int flag = bundle.getInt("flag",
						Constants.DeviceFlag.ALREADY_SET_PASSWORD);
				int type = bundle.getInt("type", P2PValue.DeviceType.UNKNOWN);
                int rflag=bundle.getInt("rtspflag", 0);
                int rtspflag=(rflag>>2)&1;
				InetAddress address = (InetAddress) bundle
						.getSerializable("address");
				LocalDevice localDevice = new LocalDevice();
				localDevice.setContactId(id);
				localDevice.setFlag(flag);
				localDevice.setType(type);
				localDevice.setAddress(address);
				localDevice.setName(name);
				localDevice.setRtspFrag(rtspflag);
				if (!tempLocalDevices.contains(localDevice)) {
					tempLocalDevices.add(localDevice);
				}
				Log.e("ipdress","id="+id+"rtspflag="+rtspflag+"rflag="+rflag);
				Contact contact = isContact(id);
				if (contact != null) {
					contact.ipadressAddress = address;
					contact.rtspflag=rtspflag;
				}
				String mark=address.getHostAddress();
				String ip=mark.substring(mark.lastIndexOf(".")+1, mark.length());
				Contact c = isContact(id);
				if(c!=null){
					 c.ipadressAddress=address;
					 c.rtspflag=rtspflag;
				}
				break;
			case ShakeManager.HANDLE_ID_APDEVICE_END:
				Log.e("dxswifi", "wifi搜索结束");
				WifiUtils.getInstance().wifiUnlock();
				break;
			}
			return false;
		}
	});

	public void gainDeviceMode(String id) {
		for (int i = 0; i < apdevices.size(); i++) {
			if (apdevices.get(i).contactId.equals(id)) {
				TcpClient tcpClient = new TcpClient(Utils.gainWifiMode());
				try {
					tcpClient.setIpdreess(InetAddress.getByName("192.168.1.1"));
					tcpClient.setCallBack(myHandler);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tcpClient.createClient();
				break;
			}
		}
	}

	public boolean isApMode(String id) {
		for (int i = 0; i < apdevices.size(); i++) {
			if (apdevices.get(i).contactId.equals(id)
					&& apdevices.get(i).flag == Constants.DeviceFlag.AP_MODE) {
				return true;
			}
		}
		return false;
	}

	private static Handler myHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case TcpClient.SEARCH_AP_DEVICE:
				Bundle bundle = msg.getData();
				String id = bundle.getString("contactId");
				for (int i = 0; i < apdevices.size(); i++) {
					if (apdevices.get(i).contactId.equals(id)) {
						apdevices.get(i).flag = Constants.DeviceFlag.AP_MODE;
					}
				}
				break;

			default:
				break;
			}
			return false;
		}
	});
//	搜索到的列表中是否有联系人
	public boolean searchLocalContact(String contactId){
		boolean isHas=false;
		for(int i=0;i<allLocalDevices.size();i++){
			LocalDevice d=allLocalDevices.get(i);
			if(d.contactId.equals(contactId)){
				isHas=true;
				return isHas;
			}
		}
		return isHas;
	}

}
