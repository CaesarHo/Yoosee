package com.p2p.core.network;

import it.sauronsoftware.base64.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MallUrlResult {
	public String error_code;
	public String store_link;
	public MallUrlResult(JSONObject json){
		init(json);
	}
    public void init(JSONObject json){
    	try {
			error_code=json.getString("error_code");
			String url=json.optString("StoreLink");
			store_link=Base64.decode(url);
			Log.e("mall_url", "link="+store_link);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
