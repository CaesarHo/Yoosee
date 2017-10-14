package com.home.yoosee.global;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.home.yoosee.base.MyApp;
import com.home.yoosee.entity.LocalDevice;
import com.home.yoosee.utils.TcpClient;
import com.home.yoosee.utils.Utils;
import com.p2p.core.P2PValue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class APList {
	private Context context;
	public static APList Apmanager = null;
	public static List<LocalDevice> aplist = new ArrayList<LocalDevice>();
	public static List<LocalDevice> temlist = new ArrayList<LocalDevice>();

	public APList(Context context) {
		if (aplist != null) {
			aplist.clear();
		}
		this.context = context;
		Apmanager = this;
	}

	public static APList getInstance() {
		if (Apmanager == null) {
			Apmanager = new APList(MyApp.app);
		}
		return Apmanager;
	}

	public List<LocalDevice> getAPDeviceList() {
		List<ScanResult> wifilist=new ArrayList<ScanResult>();
		WifiManager wifimanager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifimanager.startScan();
		String WifiName;
		if(wifimanager==null){
			return aplist;
		}
        wifilist = wifimanager.getScanResults();
		if(wifilist==null){
			return aplist;
		}
		temlist.clear();
		for (LocalDevice localdevice : aplist) {
			temlist.add(localdevice);
		}
		aplist.clear();
		for (int i = 0; i < wifilist.size(); i++) {
			WifiName = Utils.getWifiName(wifilist.get(i).SSID);
			if (WifiName.startsWith(AppConfig.Relese.APTAG)) {
				LocalDevice apdevice = new LocalDevice();
				apdevice.contactId = WifiName.substring(AppConfig.Relese.APTAG
						.length());
				apdevice.name = WifiName;
				apdevice.flag = Constants.DeviceFlag.UNKNOW;
				apdevice.type = P2PValue.DeviceType.UNKNOWN;
				try {
					apdevice.address = InetAddress.getByName("192.168.1.1");
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				aplist.add(apdevice);
				Log.e("apdevice", "apdevice.contactId=" + apdevice.contactId
						+ "--" + "name=" + apdevice.name);
			}
		}
		for (int j = 0; j < aplist.size(); j++) {
			boolean isexit = false;
			for (int i = 0; i < temlist.size(); i++) {
				if (aplist.get(j).contactId.equals(temlist.get(i).contactId)) {
					isexit = true;
					break;
				}
			}
			if (!isexit) {
				Intent i = new Intent();
				i.setAction(Constants.Action.SEARCH_AP);
				MyApp.app.sendBroadcast(i);
				break;
			}
		}
		if (aplist.size() == 0 && temlist.size() != 0) {
			Intent i = new Intent();
			i.setAction(Constants.Action.SEARCH_AP);
			MyApp.app.sendBroadcast(i);
		}
		return aplist;

	}

	public void gainDeviceMode(String id) {
		for (int i = 0; i < aplist.size(); i++) {
			if (aplist.get(i).contactId.equals(id)) {
				TcpClient tcpClient = new TcpClient(Utils.gainWifiMode());
				try {
					tcpClient.setIpdreess(InetAddress.getByName("192.168.1.1"));
					tcpClient.setCallBack(mHandler);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tcpClient.createClient();
				break;
			}
		}
	}

	public LocalDevice getByPosition(int position) {
		if (position > aplist.size()) {
			return null;
		} else {
			return aplist.get(position);
		}
	}

	public boolean isApMode(String id) {
		for (int i = 0; i < aplist.size(); i++) {
			if (aplist.get(i).contactId.equals(id)
					&& aplist.get(i).flag == Constants.DeviceFlag.AP_MODE) {
				return true;
			}
		}
		return false;
	}

	public static Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case TcpClient.SEARCH_AP_DEVICE:
				Bundle bundle = msg.getData();
				String id = bundle.getString("contactId");
				Log.e("receive", "id=" + id);
				for (int i = 0; i < aplist.size(); i++) {
					if (aplist.get(i).contactId.equals(id)) {
						aplist.get(i).flag = Constants.DeviceFlag.AP_MODE;
					}
				}
				break;

			default:
				break;
			}
			return false;
		}
	});

}
