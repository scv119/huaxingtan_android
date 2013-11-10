package cn.huaxingtan.util;

public class Misc {
	public static String formatDuration(long sec) {
		long min = sec/60;
		sec = sec % 60;
		long hou = min/60;
		min = min % 60;
		String ret = "";
		if (hou > 0) {
			ret = ret + hou + "小时";
		}
		if (min > 0 || ret.length() > 0)
			ret = ret + min + "分";
		ret = ret + sec + "秒";
		return ret;
	}
	
	public static String formatEnglishDuration(long sec) {
		if (sec <= 0) 
			return "00:00";
		long min = sec/60;
		sec = sec % 60;
		long hou = min/60;
		min = min % 60;
		String ret = "";
		if (hou > 0) {
			ret = ret + hou + ":";
		}
		if (min < 10)
			ret = ret + "0" + min + ":";
		else
			ret = ret + min + ":";
		if (sec < 10)
			ret = ret + "0" + sec ;
		else
			ret = ret + sec;
		return ret;
	}
}
