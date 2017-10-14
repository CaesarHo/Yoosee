package com.home.yoosee.base;

import android.content.Intent;
import android.util.Log;

import com.home.yoosee.R;
import com.home.yoosee.data.SharedPreferencesManager;
import com.home.yoosee.global.Constants;
import com.home.yoosee.utils.MusicManger;
import com.p2p.core.P2PInterface.IP2P;
import com.p2p.core.P2PValue;

public class P2PListener implements IP2P {
	@Override
	public void vAllarmingWitghTime(String srcId, int type, int option, int iGroup, int iItem, int imagecounts, String imagePath, String alarmCapDir, String VideoPath) {

	}

	@Override
	public void vRetNewSystemMessage(int iSystemMessageType, int iSystemMessageIndex) {

	}


	@Override
	public void vCalling(boolean isOutCall, String threeNumber, int type) {
		// TODO Auto-generated method stub
		if (isOutCall) {
			P2PConnect.vCalling(true, type);
		} else {
			int c_muteState = SharedPreferencesManager.getInstance()
					.getCMuteState(MyApp.app);
			if (c_muteState == 1) {
				MusicManger.getInstance().playCommingMusic();
			}

			int c_vibrateState = SharedPreferencesManager.getInstance()
					.getCVibrateState(MyApp.app);
			if (c_vibrateState == 1) {
				MusicManger.getInstance().Vibrate();
			}

			P2PConnect.setCurrent_call_id(threeNumber);

			P2PConnect.vCalling(false, type);
		}
	}

	@Override
	public void vReject(int reason_code) {
		// TODO Auto-generated method stub
		String reason = "";
		switch (reason_code) {
		case 0:
			reason = MyApp.app.getResources().getString(R.string.pw_incrrect);
			break;
		case 1:
			reason = MyApp.app.getResources().getString(R.string.busy);
			break;
		case 2:
			reason = MyApp.app.getResources().getString(R.string.none);
			break;
		case 3:
			reason = MyApp.app.getResources().getString(R.string.id_disabled);
			break;
		case 4:
			reason = MyApp.app.getResources().getString(R.string.id_overdate);
			break;
		case 5:
			reason = MyApp.app.getResources().getString(R.string.id_inactived);
			break;
		case 6:
			reason = MyApp.app.getResources().getString(R.string.offline);
			break;
		case 7:
			reason = MyApp.app.getResources().getString(R.string.powerdown);
			break;
		case 8:
			reason = MyApp.app.getResources().getString(R.string.nohelper);
			break;
		case 9:
			reason = MyApp.app.getResources().getString(R.string.hungup);
			break;
		case 10:
			reason = MyApp.app.getResources().getString(R.string.timeout);
			break;
		case 11:
			reason = MyApp.app.getResources().getString(R.string.no_body);
			break;
		case 12:
			reason = MyApp.app.getResources()
					.getString(R.string.internal_error);
			break;
		case 13:
			reason = MyApp.app.getResources().getString(R.string.conn_fail);
			break;
		case 14:
			reason = MyApp.app.getResources().getString(R.string.not_support);
			break;
		case 15:
			reason = MyApp.app.getResources().getString(R.string.rtsp_not_frame);
			break;
		default:
			reason = MyApp.app.getResources().getString(R.string.conn_fail)+"("+reason_code+")";
			break;
		
		}
		P2PConnect.vReject(reason);
	}

	@Override
	public void vAccept(int type, int state) {
		// TODO Auto-generated method stub
		P2PConnect.vAccept(type, state);
	}

	@Override
	public void vConnectReady() {
		// TODO Auto-generated method stub
		P2PConnect.vConnectReady();
	}

	@Override
	public void vAllarming(String srcId, int type,
			boolean isSupportExternAlarm, int iGroup, int iItem,
			boolean isSurpportDelete) {
		// TODO Auto-generated method stub
		P2PConnect.vAllarming(Integer.parseInt(srcId), type,
				isSupportExternAlarm, iGroup, iItem, isSurpportDelete);
	}

	@Override
	public void vChangeVideoMask(int state) {
		// TODO Auto-generated method stub
		Intent i = new Intent(Constants.P2P.P2P_CHANGE_IMAGE_TRANSFER);
		i.putExtra("state", state);
		MyApp.app.sendBroadcast(i);
	}

	@Override
	public void vRetPlayBackPos(int length, int currentPos) {
		// TODO Auto-generated method stub
		Intent i = new Intent();
		i.setAction(Constants.P2P.PLAYBACK_CHANGE_SEEK);
		i.putExtra("max", length);
		i.putExtra("current", currentPos);
		MyApp.app.sendBroadcast(i);
	}

	@Override
	public void vRetPlayBackStatus(int state) {
		// TODO Auto-generated method stub
		Intent i = new Intent();
		i.setAction(Constants.P2P.PLAYBACK_CHANGE_STATE);
		i.putExtra("state", state);
		MyApp.app.sendBroadcast(i);
	}

	@Override
	public void vGXNotifyFlag(int flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vRetPlaySize(int iWidth, int iHeight) {
		// TODO Auto-generated method stub
		Log.e("my", "vRetPlaySize:" + iWidth + "-" + iHeight);
        Log.e("vRetPlaySize", "vRetPlaySize:" + iWidth + "-" + iHeight);
		Intent i = new Intent();
		i.setAction(Constants.P2P.P2P_RESOLUTION_CHANGE);
		if (iWidth == 1280 || iWidth == 1920) {
			P2PConnect.setMode(P2PValue.VideoMode.VIDEO_MODE_HD);
			i.putExtra("mode", P2PValue.VideoMode.VIDEO_MODE_HD);
		} else if (iWidth == 640) {
			P2PConnect.setMode(P2PValue.VideoMode.VIDEO_MODE_SD);
			i.putExtra("mode", P2PValue.VideoMode.VIDEO_MODE_SD);
		} else if (iWidth == 320) {
			P2PConnect.setMode(P2PValue.VideoMode.VIDEO_MODE_LD);
			i.putExtra("mode", P2PValue.VideoMode.VIDEO_MODE_LD);
		}
		MyApp.app.sendBroadcast(i);
	}

	@Override
	public void vRetPlayNumber(int iNumber) {
		// TODO Auto-generated method stub
		Log.e("my", "vRetPlayNumber:" + iNumber);
		P2PConnect.setNumber(iNumber);
		Intent i = new Intent();
		i.setAction(Constants.P2P.P2P_MONITOR_NUMBER_CHANGE);
		i.putExtra("number", iNumber);
		MyApp.app.sendBroadcast(i);
	}

	@Override
	public void vRecvAudioVideoData(byte[] AudioBuffer, int AudioLen,
			int AudioFrames, long AudioPTS, byte[] VideoBuffer, int VideoLen,
			long VideoPTS) {
		// TODO Auto-generated method stub
	}

	@Override
	public void vRetRTSPNotify(int arg2,String msg) {
		// TODO Auto-generated method stub
        Log.e("vRetRTSPNotify", "arg2="+arg2+"--"+"msg="+msg);
	}
}
