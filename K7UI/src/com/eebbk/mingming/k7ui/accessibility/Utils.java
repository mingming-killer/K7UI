package com.eebbk.mingming.k7ui.accessibility;

import android.os.Environment;

/**
 * 
 * K7UI 小工具
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class Utils {
	
	public final static float F_ZERO = 1e-6f;
	
	// TODO: 只用 android 公用的接口，保证最大兼容性
	public final static String INTERNAL_SDCARD_PATH = Environment.getExternalStorageDirectory().toString();
	//public final static String EXTERNAL_SDCARD_PATH = Environment.getExternalFlashStorageDirectory().toString();
	//public final static String DISK_A_PATH = Environment.getInternalStorageDirectory().toString();
	//public final static String DISK_B_PATH = Environment.getExternalStorageDirectory().toString();
	
	public final static boolean equalZeroF(float f) {
		return (Math.abs(f - 0.0f) <= F_ZERO);
	}
	
	public final static boolean equalF(float src, float dst) {
		return (Math.abs(src - dst) <= F_ZERO);
	}
	
}
