package cn.huaxingtan.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.AudioItem.Status;
import cn.huaxingtan.model.Serial;
import cn.huaxingtan.util.Serialize;

public class FileManager {
	private static HashMap<Integer, Serial> serialCache;
	private static HashMap<Long, AudioItem> audioItemCache;
	private static HashMap<Integer, HashSet<Long>> audioItemBySerialIdCache;
	private static HashMap<Long, Long> downloadingIds;

	
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
					audioItemBySerialIdCache = new HashMap<Integer, HashSet<Long>>();
					downloadingIds = new HashMap<Long, Long>();
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
				Log.e(TAG, "fail load Serial info", e);
			}
			if (o != null) {
				Map map = (Map)o;
				serialCache.putAll(map);
			}
		}
		
		val = file_info.getString("Downloading", null);
		if (val != null && !val.equals("")) {
			Object o = null;
			try {
				o = Serialize.fromBase64(val);
			} catch (Exception e) {
				Log.e(TAG, "fail load Downloading info", e);
			}
			if (o != null) {
				Map<Long, Long> map = (Map)o;
				long ids[] = new long[map.keySet().size()];
				int i = 0;
				for (Long tmp:map.keySet())
					ids[i++] = tmp;
				DownloadManager.Query query = new DownloadManager.Query().setFilterById(ids);
				Cursor c = mDownloadManager.query(query);
				if (c != null) {
					if (c.moveToFirst())
					do{
						int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
						long id    = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
						if ((status & (DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING)) != 0)
							downloadingIds.put(id, map.get(id));
					} while (c.moveToNext());
					c.close();
				}
			}
		}
		
		val = file_info.getString("AudioItem", null);
		if (val != null && !val.equals("")) {
			Object o = null;
			try {
				o = Serialize.fromBase64(val);
			} catch (Exception e) {
				Log.e(TAG, "fail load AudioItem info", e);
			}
			if (o != null) {
				Map map = (Map)o;
				
				for (Object key:map.keySet()) {
					Long iKey = (Long) key;
					AudioItem item = (AudioItem)map.get(key);
					File file = new File(mContext.getFilesDir(), item.getPath());
					
					
					if (item.getStatus() == AudioItem.Status.PAUSED || item.getStatus() == Status.STARTED) {
						if (!downloadingIds.containsKey(item.getDownloadId()) ||
								item.getFileId() != downloadingIds.get(item.getDownloadId()) ) {
							item.setStatus(Status.STOPED);
							item.setDownloadId(-1);
						}
					} else {
						item.setDownloadId(-1);
					}
					
					if (item.getStatus() == AudioItem.Status.FINISHED) {
						if (!file.exists() || file.length() != item.getFileSize())
							item.setStatus(Status.STOPED);
					}
					
					if (file.exists() && file.length() == item.getFileSize()) {
						item.setStatus(Status.FINISHED);
						item.setDownloadId(-1);
					}
					
					audioItemCache.put(iKey, item);
					if(!audioItemBySerialIdCache.containsKey(item.getSerialId())) 
						audioItemBySerialIdCache.put(item.getSerialId(), new HashSet<Long>());
					audioItemBySerialIdCache.get(item.getSerialId()).add(item.getFileId());
					
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
	
	public Serial updateByManager(Serial serial) {
		synchronized(FileManager.class) {
			if (serialCache.containsKey(serial.getId())) 
				return serialCache.get(serial.getId());
			set(serial);
			return serial;
		}
	}
	
	public AudioItem updateByManager(AudioItem audioItem) {
		synchronized(FileManager.class) {
			if (audioItemCache.containsKey(audioItem.getFileId())) 
				return audioItemCache.get(audioItem.getFileId());
			set(audioItem);
			return audioItem;
		}
	}
	
	public List<AudioItem> getAudioItems(int serialId) {
		List<AudioItem> list = new ArrayList<AudioItem>();
		Set<Long> tmp = audioItemBySerialIdCache.get(serialId);
		if (tmp != null) {
			for (long id:tmp) {
				list.add(audioItemCache.get(id));
			}
		}
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
				audioItemBySerialIdCache.put(item.getSerialId(), new HashSet<Long>());
			audioItemBySerialIdCache.get(item.getSerialId()).add(item.getFileId());
			
			if (item.getStatus() == AudioItem.Status.FINISHED ) {
				Serial serial = serialCache.get(item.getSerialId());
				Set<Long> list = audioItemBySerialIdCache.get(serial.getId());
				int count = 0;
				if (list != null)
					for (Long tmp:list) {
						count += ((audioItemCache.get(tmp).getStatus() == Status.FINISHED) ? 1:0);
					}
				serial.setDownloaded(count);
			}
			
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
		String downloading = "";
		try {
			serial = Serialize.toBase64(serialCache);
		} catch (IOException e) {
			Log.e(TAG, "fail to save serialCache info", e);
		}
		
		try {
			audioItem = Serialize.toBase64(audioItemCache);
		} catch (IOException e) {
			Log.e(TAG, "fail to save audioItemCache info", e);
		}
		
		try {
			downloading = Serialize.toBase64(downloadingIds);
		} catch (IOException e) {
			Log.e(TAG, "fail to save downloadingIds info", e);
		}
		file_info.edit().putString("Serial", serial).putString("AudioItem", audioItem)
			.putString("Downloading", downloading).commit();
	}
	
	
	public void addDownloadId(long id, long fileId) {
		synchronized(FileManager.class) {
			downloadingIds.put(id, fileId);
		}
	}
	
	public void removeDownloadId(long id) {
		synchronized(FileManager.class) {
			downloadingIds.remove(id);
		}
	}
	
	public long getIdByDownloadId(long id) {
		Long val = downloadingIds.get(id);
		if (val == null)
			return -1;
		return val;
	}
	
	public long[] getDownloadIds() {
		long []tmp = new long[downloadingIds.size()];
		int i = 0;
		for (Long value:downloadingIds.keySet()) {
			if (i >= tmp.length)
				break;
			tmp[i++] = value;
		}
		return tmp;
	}
	
	public boolean saveTmpFile(String tmpPath, long fileId) {
		File file = new File(mContext.getFilesDir(), audioItemCache.get(fileId).getPath());
		File f = new File(tmpPath);
		try {
			copy(f, file);
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "fail to move tmpfile " + f.getAbsolutePath() + " to " + file.getAbsolutePath(), e);
			return false;
		}
	}
	
	public void copy(File src, File dst) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
		    in = new FileInputStream(src);
		    out = new FileOutputStream(dst);
	
		    // Transfer bytes from in to out
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		} finally {
			try{
				if (in != null)
					in.close();
			} catch (Exception e) {}
			try{
				if (out != null)
					out.close();
			} catch (Exception e) {}
		}
	}
	
	
}
