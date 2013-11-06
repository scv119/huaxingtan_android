package cn.huaxingtan.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.AudioItem.Status;
import cn.huaxingtan.model.Serial;
import cn.huaxingtan.util.Serialize;

public class FileManager {
	private static HashMap<Integer, Serial> serialCache;
	private static HashMap<Long, AudioItem> audioItemCache;
	private static HashMap<Integer, List<AudioItem>> audioItemBySerialIdCache;
	private static final String TAG = FileManager.class.getCanonicalName();
	private static final String P_NAME = "file_info";
	
	private Context mContext;
	private DownloadManager mDownloadManager;
	
	
	public FileManager(Context context) {
		mContext = context;
		mDownloadManager = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		
		if (serialCache == null) {
			synchronized(FileManager.class) {
				if (serialCache == null) {
					serialCache = new HashMap<Integer, Serial>();
					audioItemCache = new HashMap<Long, AudioItem>();
					audioItemBySerialIdCache = new HashMap<Integer, List<AudioItem>>();
					load();
				}
			}
		}
	}
	
	private void load() {
		SharedPreferences file_info = mContext.getSharedPreferences(P_NAME, 0); 
		String val = file_info.getString("Serial", null);
		Map <Integer, Integer> tmpMap = new HashMap<Integer, Integer>();
		if (val != null && !val.equals("")) {
			Object o = null;
			try {
				o = Serialize.fromBase64(val);
			} catch (Exception e) {
				Log.e(TAG, "fail load file info", e);
			}
			if (o != null) {
				Map map = (Map)o;
				serialCache.putAll(map);
			}
		}
		
		val = file_info.getString("AudioItem", null);
		if (val != null && !val.equals("")) {
			Object o = null;
			try {
				o = Serialize.fromBase64(val);
			} catch (Exception e) {
				Log.e(TAG, "fail load file info", e);
			}
			if (o != null) {
				Map map = (Map)o;
				
				for (Object key:map.keySet()) {
					Long iKey = (Long) key;
					AudioItem item = (AudioItem)map.get(key);
					if (item.getStatus() == AudioItem.Status.FINISHED) {
						File file = new File(mContext.getFilesDir(), item.getPath());
						if (!file.exists() || file.length() != item.getFileSize())
							item.setStatus(Status.STOPED);
					}
					audioItemCache.put(iKey, item);
					if(!audioItemBySerialIdCache.containsKey(item.getSerialId())) 
						audioItemBySerialIdCache.put(item.getSerialId(), new ArrayList<AudioItem>());
					audioItemBySerialIdCache.get(item.getSerialId()).add(item);
					
					if (item.getStatus() == AudioItem.Status.FINISHED) {
						if (!tmpMap.containsKey(item.getSerialId()))
							tmpMap.put(item.getSerialId(), 1);
						else
							tmpMap.put(item.getSerialId(), tmpMap.get(item.getSerialId()) + 1);
					}
				}
			}
		}
		
		for (Integer key:serialCache.keySet()) {
			if (tmpMap.containsKey(key))
				serialCache.get(key).setDownloaded(tmpMap.get(key));
			else
				serialCache.get(key).setDownloaded(0);
		}
		
	}
	
	public Serial getSerial(int serialId) {
		return serialCache.get(serialId);
	}
	
	public AudioItem getAudioItem(Long audioItemId) {
		return audioItemCache.get(audioItemId);
	}
	
	public List<Serial> getSerials() {
		List<Serial> list = new ArrayList<Serial>();
		list.addAll(serialCache.values());
		return list;
	}
	
	public List<AudioItem> getAudioItems() {
		List<AudioItem> list = new ArrayList<AudioItem>();
		list.addAll(audioItemCache.values());
		return list;
	}
	
	public List<AudioItem> getAudioItems(int serialId) {
		List<AudioItem> list = new ArrayList<AudioItem>();
		List<AudioItem> tmp = audioItemBySerialIdCache.get(serialId);
		if (tmp != null)
			list.addAll(tmp);
		return list;
	}
	
	public void set(Serial item) {
		synchronized(FileManager.class) {
			serialCache.put(item.getId(), item);
		}
	}
	
	public void set(AudioItem item) {
		synchronized(FileManager.class) {
			audioItemCache.put(item.getFileId(), item);
			if(!audioItemBySerialIdCache.containsKey(item.getSerialId())) 
				audioItemBySerialIdCache.put(item.getSerialId(), new ArrayList<AudioItem>());
			audioItemBySerialIdCache.get(item.getSerialId()).add(item);
		}
	}
	
	public void save() {
		synchronized(FileManager.class) {
			_save();
		}
	}
	
	private void _save() {
		SharedPreferences file_info = mContext.getSharedPreferences(P_NAME, 0); 
		String serial = "";
		String audioItem = "";
		try {
			serial = Serialize.toBase64(serialCache);
		} catch (IOException e) {
			Log.e(TAG, "fail to save file info", e);
		}
		
		try {
			audioItem = Serialize.toBase64(audioItemCache);
		} catch (IOException e) {
			Log.e(TAG, "fail to save file info", e);
		}
		file_info.edit().putString("Serial", serial).putString("AudioItem", audioItem).commit();
	}
	
	
	
}
