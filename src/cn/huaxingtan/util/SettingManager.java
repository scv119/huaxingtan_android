package cn.huaxingtan.util;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingManager {
	private static final String CELL = "cellularData";
	public static boolean enableCellular(Context context) {
		SharedPreferences cell = context.getSharedPreferences(CELL, 0);
		return cell.getBoolean(CELL, false);
	}
	
	public static void setEnableCellular(Context context, boolean enable) {
		SharedPreferences cell = context.getSharedPreferences(CELL, 0);
		cell.edit().putBoolean(CELL, enable).commit();
		
	}
	
	public static int getAllowNetworkType(Context context) {
		boolean enableCell = enableCellular(context);
		return enableCell? (DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI) : DownloadManager.Request.NETWORK_WIFI;
	}
}
