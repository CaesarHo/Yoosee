package com.home.yoosee.utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.home.yoosee.global.Constants;
import com.home.yoosee.base.MyApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {
	Socket socket = null;
	int port = 10086;
	InetAddress ipAddress;
	byte[] data;
	private Handler mHandler;
	public static final int SEARCH_AP_DEVICE = 0x66;

	public TcpClient(byte[] data) {
		// TODO Auto-generated constructor stub
		this.data = data;
	}

	public void setIpdreess(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setCallBack(Handler handler) {
		this.mHandler = handler;
	}

	public void createClient() {
		try {
			Log.e("receivedata", "---------------------------");
			socket = new Socket(ipAddress, port);
			if (socket.isConnected()) {
				startListen();
				Log.e("receivedata", "connect---------------------");
				// DataInputStream is=new DataInputStream(new
				// ByteArrayInputStream(data));
				OutputStream os = socket.getOutputStream();
				os.write(data);
				os.flush();
			} else {
				int isconnect = 1;
				while (isconnect == 1) {
					try {
						socket = new Socket(ipAddress, port);
						Thread.sleep(100);
						Log.e("receivedata", "connect+++++++++++++++");
						if (socket.isConnected()) {
							startListen();
							// DataInputStream is=new DataInputStream(new
							// ByteArrayInputStream(data));
							OutputStream os = socket.getOutputStream();
							os.write(data);
							os.flush();
							isconnect = 0;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startListen() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				OutputStream outputStream;
				try {
					// outputStream = socket.getOutputStream();
					// byte buffer [] = new byte[1024];
					// outputStream.write(buffer, 0, 1);
					// outputStream.flush();
					// Log.e("receivedata",buffer.toString());
					// String s="";
					// for(int i=0;i<buffer.length;i++){
					// s=s+buffer[i]+" ";
					// }
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					InputStream in = socket.getInputStream();
					byte[] buffer = new byte[272];
					in.read(buffer);
					String s = "";
					for (int i = 0; i < buffer.length; i++) {
						s = s + buffer[i] + " ";
					}
					Log.e("receivedata", "data=" + s);
					if (buffer.length < 0) {
						return;
					}
					if (buffer[0] == 3) {
						int result = bytesToInt(buffer, 4);
						Log.e("receivedata", "result=" + result);
						Intent i = new Intent();
						i.setAction(Constants.Action.SET_AP_DEVICE_WIFI_PWD);
						i.putExtra("result", result);
						MyApp.app.sendBroadcast(i);
					} else if (buffer[0] == 1 && buffer[4] == 1
							&& buffer.length >= 20) {
						int id = bytesToInt(buffer, 16);
						int ip = bytesToInt(buffer, 12);
						Log.e("receivedata", "contactId=" + id + "--" + "ip="
								+ ip);
						if (null != mHandler) {
							Message msg = new Message();
							msg.what = SEARCH_AP_DEVICE;
							Bundle bundler = new Bundle();
							bundler.putString("contactId", String.valueOf(id));
							msg.setData(bundler);
							mHandler.sendMessage(msg);
						}
					}
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}).start();
	}

	public static int bytesToInt(byte[] src, int offset) {
		int value;
		value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8)
				| ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 3] & 0xFF) << 24));
		return value;
	}
}
