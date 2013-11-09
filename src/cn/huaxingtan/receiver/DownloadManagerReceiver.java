package cn.huaxingtan.receiver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.AudioItem.Status;
import cn.huaxingtan.view.DetailActivity;
import cn.huaxingtan.view.OfflineFragment;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class DownloadManagerReceiver extends BroadcastReceiver{
	private static final String TAG = DownloadManagerReceiver.class.getCanonicalName();
	
	private FileManager mFileManager;
	private DownloadManager mDownloadManager;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mFileManager = new FileManager(context);
		mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		ActivityManager am = (ActivityManager) context.
		    getSystemService(Activity.ACTIVITY_SERVICE);
		
		long []tmp = mFileManager.getDownloadIds();
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(tmp);
		query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
		Cursor c = mDownloadManager.query(query);
		DownloadAsyncTask task = new DownloadAsyncTask();
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, c);
	};
	
	private  synchronized void parseDownloadStatus(Cursor c) {
		int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
		long id    = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
		final long fileId = mFileManager.getIdByDownloadId(id);
		
		final String path = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
		long bytes = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
		int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
		
		Log.d(TAG, fileId + " status:" + status + " bytes:" + bytes);
		
		if (fileId == -1)
			return;
		
		final AudioItem audioItem = mFileManager.getAudioItem(fileId);
		String message = null;
		audioItem.setFinishedSize(bytes);
//		if (status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_RUNNING)
//			audioItem.setStatus(Status.STARTED);
//		if (status == DownloadManager.STATUS_PAUSED)
//			audioItem.setStatus(Status.PAUSED);
		if (status == DownloadManager.STATUS_FAILED && audioItem.getStatus() != AudioItem.Status.FINISHED 
				&& mFileManager.getIdByDownloadId(id) >= 0) {
			mFileManager.removeDownloadId(id);
			audioItem.setStatus(Status.STOPED);
			audioItem.setDownloadId(-1);
			mFileManager.set(audioItem);
			Log.d(TAG, fileId + " download failed");
		}
		if (status == DownloadManager.STATUS_SUCCESSFUL && audioItem.getStatus() != AudioItem.Status.FINISHED
				&& mFileManager.getIdByDownloadId(id) >= 0) {
			mFileManager.removeDownloadId(id);
			if (!mFileManager.saveTmpFile(path, fileId)) {
				audioItem.setStatus(Status.STOPED);
				Log.d(TAG, fileId + " download failed");
			}
			else {
				audioItem.setStatus(Status.FINISHED);
				Log.d(TAG, fileId + " download finished");
			}
			audioItem.setDownloadId(-1);
			mFileManager.set(audioItem);
			
			if (audioItem.getStatus() == Status.FINISHED) {
				OfflineFragment fragment = null;
				try {
					if (OfflineFragment.running != null)
						fragment = OfflineFragment.running.get();
					if (fragment != null) {
						fragment.refreshUI();
					}
				} catch (Exception e) {
					Log.e(TAG, "fail to refresh OfflineFragment", e);
				}
				
				DetailActivity detail = null;
				try {
					if (DetailActivity.running != null)
						detail = DetailActivity.running.get();
					if (detail != null) {
						detail.refreshOfflineUI();
					}
				} catch (Exception e) {
					Log.e(TAG, "fail to refresh DetailActivity", e);
				}
			}
		}
		
	}
	
	class DownloadAsyncTask extends AsyncTask<Cursor, Object, Object> {

		@Override
		protected Object doInBackground(Cursor... params) {
			Cursor c = params[0];
			if (c == null)
				return null;
			if (c.moveToFirst())
				do {
					parseDownloadStatus(c);
				} while (c.moveToNext());
			c.close();
			return null;
		}
	}
	
}
