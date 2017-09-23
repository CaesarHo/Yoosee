package com.p2p.core.network;

import it.sauronsoftware.base64.Base64;

import org.json.JSONObject;

import android.util.Log;

import com.p2p.core.utils.MyUtils;

public class GetStartInfoResult {
	public String error_code;
    public String Index;
    public String ImageSrc;
    public String pictrue_Link;
    public String  Preview ;//是否预览(1：预览,其他值：非预览)
    public GetStartInfoResult(JSONObject json){
    	init(json);
    }
    public void init(JSONObject json){
    	try{
    	   error_code=json.getString("error_code");
    	   Index=json.getString("Index");
           ImageSrc=json.getString("ImageSrc");
           String link=json.getString("Link");
           String decode_url=Base64.decode(link, "UTF-8");
           pictrue_Link=decode_url;
           Preview=json.getString("Preview");
    	}catch(Exception e){
			if(!MyUtils.isNumeric(error_code)){
				Log.e("my","GetAccountInfoResult json解析错误");
				error_code = String.valueOf(NetManager.JSON_PARSE_ERROR);
			}
			
		}
    }
}
