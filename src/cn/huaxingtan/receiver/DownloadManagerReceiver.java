package cn.huaxingtan.receiver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.AudioItem.Status;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

public class DownloadManagerReceiver extends BroadcastReceiver{
	private static final String TAG = DownloadManagerReceiver.class.getCanonicalName();
	
	FileManager mFileManager;
	DownloadManager mDownloadManager;
	private static Map<Long, Long> downloadingIds = new HashMap<Long, Long>();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (mFileManager == null) {
			mFileManager = new FileManager(context);
			mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		}
		long []tmp = new long[downloadingIds.size()];
		int i = 0;
		for (Long value:downloadingIds.keySet()) {
			if (i >= tmp.length)
				break;
			tmp[i++] = value;
		}
		Cursor c = mDownloadManager.query(new DownloadManager.Query().setFilterById(tmp));
		if (c == null)
			return;
		i = 0;
		c.moveToFirst();
		do {
			parseDownloadStatus(c);
		} while (c.moveToNext());
		
	};
	
	private void parseDownloadStatus(Cursor c) {
		int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
		long id    = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
		long fileId = downloadingIds.get(id);
		String path = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
		long bytes = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
		int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
		
		Log.d(TAG, fileId + " status:" + status + " bytes:" + bytes);
		
		AudioItem audioItem = mFileManager.getAudioItem(fileId);
		String message = null;
		audioItem.setFinishedSize(bytes);
		if (status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_RUNNING)
			audioItem.setStatus(Status.STARTED);
		if (status == DownloadManager.STATUS_PAUSED)
			audioItem.setStatus(Status.PAUSED);
		if (status == DownloadManager.STATUS_SUCCESSFUL) {
			if (!mFileManager.saveTmpFile(path, fileId))
				audioItem.setStatus(Status.STOPED);
			else
				audioItem.setStatus(Status.FINISHED);
		}
		
		if (status == DownloadManager.STATUS_FAILED)
			audioItem.setStatus(Status.STOPED);
		
		notify(fileId, message);
	}
	
	public static synchronized void addDownloadId(long id, long fileId) {
		downloadingIds.put(id, fileId);
	}
	
	public static synchronized void removeDownloadId(long id) {
		downloadingIds.remove(id);
	}
	
	private void notify(long fileId, String message) {
		
	}
	
}
