package cn.huaxingtan.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.Serial;
import cn.huaxingtan.util.JsonAsyncTask;

public class CachedDataProvider {
	
	private static Map<Integer, Timestamped> mCache = new ConcurrentHashMap<Integer, Timestamped>();
	private static final String TAG = CachedDataProvider.class.getCanonicalName();
	
	public void getSerials(Callback callback) {
		Integer key = 0;
		if (mCache.containsKey(key) && !mCache.get(key).expired()) {
			callback.success(mCache.get(key).value);
			return;
		}
		
		SerialJsonTask task = new SerialJsonTask(key, callback);
		task.execute("http://huaxingtan.cn/api/serial?version=1.0");
	}
	
	public void getAudiosBySerial(int serialId, Callback callback) {
		Integer key = serialId;
		if (mCache.containsKey(key) && !mCache.get(key).expired()) {
			callback.success(mCache.get(key).value);
			return;
		}
		
		FeedJsonTask task = new FeedJsonTask(key, callback);
		task.execute("http://huaxingtan.cn/api/feed?version=1.0");
	}
	
	public static interface Callback {
		public void success(Object result);
		public void fail(Exception e);
	}
	
	private class SerialJsonTask extends JsonAsyncTask {
		private Callback mCallback;
		private Integer mKey;
		
		SerialJsonTask(Integer key, Callback callback) {
			this.mCallback = callback;
			mKey = key;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			if (result == null) {
				this.mCallback.fail(new Exception("load failed"));
				return;
			}
			
			List<Object> list = (List<Object>) result;
			List<Serial> serials = new ArrayList<Serial>();
			for (Object o:list) {
				serials.add(Serial.loadJson(o));
			}
			mCache.put(mKey, new Timestamped(serials));
			this.mCallback.success(serials);
	    }
	}
	
	private class FeedJsonTask extends JsonAsyncTask {
		private Callback mCallback;
		private Integer mKey;
		
		FeedJsonTask(Integer key, Callback callback) {
			this.mCallback = callback;
			mKey = key;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			if (result == null) {
				this.mCallback.fail(new Exception("load failed"));
				return;
			}
			List<Object> list = (List<Object>) result;
			Map<Integer, List<AudioItem>> map = new HashMap<Integer, List<AudioItem>>();
			for (Object o:list) {
				AudioItem item = AudioItem.loadJson(o);
				if (!map.containsKey(item.getSerialId())) {
					map.put(item.getSerialId(), new ArrayList<AudioItem> ());
				}
				map.get(item.getSerialId()).add(item);
			}
			for (Integer key:map.keySet())
				mCache.put(key, new Timestamped(map.get(key)));
			this.mCallback.success(mCache.get(mKey).value
					);
	    }
	}
	
	private class Timestamped{
		long timestamp;
		Object value;
		
		boolean expired() {
			return ((new Date()).getTime() - timestamp) > 1000L * 60 *  60;
		}
		
		Timestamped(Object value) {
			this.value = value;
			this.timestamp = (new Date()).getTime();
		}
	}
}
