package com.home.yoosee.thread;

import android.os.Handler;
import android.os.Message;

import com.home.yoosee.global.Constants;
import com.home.yoosee.base.MyApp;
import com.home.yoosee.utils.Utils;
import com.p2p.core.update.UpdateManager;

public class UpdateCheckVersionThread extends Thread {
	boolean isNeedUpdate = false;
	Handler handler;

	public UpdateCheckVersionThread(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			isNeedUpdate = UpdateManager.getInstance().checkUpdate();
			if (isNeedUpdate) {
				Message msg = new Message();
				msg.what = Constants.Update.CHECK_UPDATE_HANDLE_TRUE;
				String data = "";
				if (Utils.isZh(MyApp.app)) {
					data = UpdateManager.getInstance().getUpdateDescription();
				} else {
					data = UpdateManager.getInstance()
							.getUpdateDescription_en();
				}
				msg.obj = data;
				handler.sendMessage(msg);
			} else {
				Message msg = new Message();
				msg.what = Constants.Update.CHECK_UPDATE_HANDLE_FALSE;
				handler.sendMessage(msg);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
