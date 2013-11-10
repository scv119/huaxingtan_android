package cn.huaxingtan.view;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.huaxingtan.controller.CachedDataProvider;
import cn.huaxingtan.controller.CachedDataProvider.Callback;
import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.Serial;
import cn.huaxingtan.model.AudioItem.Status;

import cn.huaxingtan.player.R;
import cn.huaxingtan.service.MusicPlayerService;
import cn.huaxingtan.util.ImageDownloader;
import cn.huaxingtan.util.Misc;
import cn.huaxingtan.util.Serialize;
import cn.huaxingtan.util.ImageDownloader.Mode;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadingActivity extends Activity {
	private static final String TAG = DownloadingActivity.class.getCanonicalName();
    private static final ImageDownloader mImageDownloader = new ImageDownloader();
    static {
    	mImageDownloader.setMode(Mode.CORRECT);
    }
	private ListView mListView;
	private List<Long> mData;

	private FileManager mFileManager;
	private DownloadingAdapter mAdapter;
	private Handler mHandler;
	private volatile boolean mRefreshUI = false;
	private volatile boolean active = false;
	private DownloadManager mDownloadManager;
	private Thread mThread;
	public static volatile WeakReference<DownloadingActivity> running;
	private MenuItem mPlayItem;
	private MusicPlayerService mMusicPlayerService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.activity_detail);
		
		
		mData = new ArrayList<Long>();
		mFileManager = new FileManager(this);
		mHandler = new Handler();
		
		mDownloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
		this.active = true;
		
		mListView = (ListView)this.findViewById(R.id.detail);
		mAdapter = new DownloadingAdapter(this, mData);
		mListView.setAdapter(mAdapter);
		mRefreshUI = true;
		refreshOfflineUI();
		
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mRefreshUI) {
					Log.d(TAG, "refreshing UI");
					if (active) {
						mAdapter.updateInfoView();
					}
					mHandler.postDelayed(this, 200);
				}
			}
			
		}, 200);
		
		mThread = new Thread(new Runnable(){
			@Override
			public void run() {
				while(mRefreshUI) {
					Log.d(TAG, "updating data");
					try {
						if (active) {
							updateDownloadData();
						}
					} catch (Exception e) {
						Log.i(TAG, "download data update failed", e);
					}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						Log.e(TAG, "download data thread interrupted", e);
					}
				}
			}
		});
		
		mThread.start();
		bindService(new Intent(this,  MusicPlayerService.class),
				mConn, Context.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection mConn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mMusicPlayerService = ((MusicPlayerService.PlayerBinder) service).getService();
			}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMusicPlayerService = null;
		}
	};
	
	
			

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		MenuItem settingItem = menu.findItem(R.id.action_settings);
		settingItem.setOnMenuItemClickListener(new OnSettingClickedListener(this));
		
		MenuItem downloadItem = menu.findItem(R.id.action_download);
		downloadItem.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(DownloadingActivity.this, DownloadingActivity.class);
				startActivity(intent);
				return true;
			}
		});
		
		downloadItem.setVisible(false);
		
		mPlayItem = menu.findItem(R.id.action_play);
		mPlayItem.setVisible(true);
		
		mPlayItem.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				long fileId = -1;
				if (mMusicPlayerService != null) {
					fileId = mMusicPlayerService.getNowPlayingId();
				}
				if (fileId != -1) {
					Intent intent = new Intent(DownloadingActivity.this, MusicPlayerActivity.class);
					intent.putExtra("fileId", fileId);
					startActivity(intent);
					return true;
				}
				Toast.makeText(DownloadingActivity.this, "没有播放内容", Toast.LENGTH_SHORT).show();
				return false;
			}
		});
		
		return super.onCreateOptionsMenu(menu);
	}
	
	public void refreshOfflineUI() {
		mData.clear();
		Set<Long> set = new HashSet<Long>();
		long[] downloadIds = mFileManager.getDownloadIds();
		for (long dId:downloadIds) {
			long fileId = mFileManager.getIdByDownloadId(dId);
			AudioItem item = mFileManager.getAudioItem(fileId);
			if (item.getStatus() == Status.STARTED && !set.contains(fileId)) {
				mData.add(fileId);
				set.add(fileId);
			}
		}
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				mAdapter.notifyDataSetChanged();
			}
			
		});
	}
	
	public void onPause() {
		super.onPause();
		this.active = false;
		if (running != null)
			running.clear();
		running = null;
	}
	
	public void onResume(){
		super.onResume();
		this.refreshOfflineUI();
		this.active = true;
		running = new WeakReference<DownloadingActivity>(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onDestroy() {
		mRefreshUI = false;
		unbindService(mConn);
		super.onDestroy();
	}

	
	
	private void updateDownloadData() {
		long[] downloadids = new long[mData.size()];
		Arrays.fill(downloadids, -1);
		int i = 0;
		for (Long id:mData) {
			AudioItem item = mFileManager.getAudioItem(id);
			if (item.getStatus() == AudioItem.Status.STARTED) {
				downloadids[i++] = item.getDownloadId();
			}
		}
		
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(downloadids);
		query.setFilterByStatus( DownloadManager.STATUS_PAUSED |
				DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
		Cursor c = mDownloadManager.query(query);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					int status =  c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
					long id    = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
					final long fileId = mFileManager.getIdByDownloadId(id);
					long bytes = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
					AudioItem aitem = mFileManager.getAudioItem(fileId);
					if (aitem != null) {
						aitem.setFinishedSize(bytes);
						Log.d(TAG, fileId + " downloaded " + bytes + " percentage: " + (100.0f * bytes / aitem.getFileSize()));
					}
				} while(c.moveToNext());
			}
			c.close();
		}
		
		
	}
	
}
