package cn.huaxingtan.util;

import java.util.ArrayList;
import java.util.List;

import cn.huaxingtan.model.AudioItem;

@Deprecated
public enum FileDownloadManager {
	INSTANCE;
	
	private FileDownloadAsyncTask mTask;
	private AudioItem mItem;
	private long mRemovedId;
	private boolean isRunning;
	private List<AudioItem> itemList;
	
	private FileDownloadManager() {
		itemList = new ArrayList<AudioItem>();
		isRunning = false;
	}
	
	public synchronized void start() {
		if (isRunning)
			return;
		isRunning = true;
		notifyStartDownload();
	}
	
	public synchronized void stop() {
		if (!isRunning)
			return;
		isRunning = false;
		if (mTask != null) {
			mTask.cancel(true);
		}
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void addTask(AudioItem item) {
		if (item.getStatus() == AudioItem.Status.FINISHED)
			return;
		synchronized(this) {
			for (AudioItem listItem: itemList) {
				if (listItem.getFileId() == item.getFileId())
					return;
			}
			
			itemList.add(item);
			item.setStatus(AudioItem.Status.PAUSED);
			notifyStartDownload();
		}
	}
	
	public synchronized void removeTask(long fileId) {
		int idx = -1;
		for (int i = 0;i < itemList.size();i++) {
			if (fileId == itemList.get(i).getFileId()) {
				idx = i;
				break;
			}
		}
		
		if (idx >= 0) {
			mRemovedId = fileId;
			itemList.remove(idx);
		}
	}

	private void notifyStartDownload() {
		
	}
}
