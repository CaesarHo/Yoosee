package com.home.yoosee.global;

import android.os.Environment;


import com.p2p.core.global.Config;

import java.io.File;

/**
 * Created by dxs on 2015/8/28.
 */
public class AppConfig {
    /**
     * 调试相关参数，正式版记得改为合理值
     */
    public static class DeBug{
        public static final boolean isWrightAllLog=false;//是否写所有日志到SD卡
        public static final boolean isWrightErroLog=true;//是否记录错误日志到SD卡
    }

	/**
	 * 正式版参数
	 */
	public static class Relese {
		public static final String VERSION = Config.AppConfig.VERSION;
		public static final String APTAG = "GW_IPC_";
		public static final String PREPOINTPATH = Environment
				.getExternalStorageDirectory().getPath()
				+ File.separator
				+ "prepoint" + File.separator + NpcCommon.mThreeNum;
		public static final String SCREENSHORT = Environment
				.getExternalStorageDirectory().getPath()
				+ File.separator
				+ "screenshot";
	}

}
