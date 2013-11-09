package cn.huaxingtan.view;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.player.R;
import cn.huaxingtan.service.MusicPlayerService;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class MusicPlayerActivity extends Activity implements OnPreparedListener, OnBufferingUpdateListener, OnSeekCompleteListener{
	private static final String TAG = MusicPlayerActivity.class.getCanonicalName();
	private Long fileId;
	private MusicPlayerService mPlayerService;
	private SeekBar mSeekBar;
	private Handler mHandler;
	private boolean mRefreshUI;
	private FileManager mFileManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		mFileManager = new FileManager(this);
		Intent musicIntent = getIntent();
		fileId = musicIntent.getExtras().getLong("fileId");

		setTitle(mFileManager.getAudioItem(fileId).getName());

		setContentView(R.layout.activity_music_player);
		mSeekBar = (SeekBar)findViewById(R.id.music_seekBar);
		
		mSeekBar.setClickable(false);
		
		mHandler = new Handler();
		mRefreshUI = true;
		bindService(new Intent(this,  MusicPlayerService.class),
				mConn, Context.BIND_AUTO_CREATE);
		

		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					if(mPlayerService.seek(seekBar.getProgress()))
						mSeekBar.setClickable(false);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
	}
	
	private ServiceConnection mConn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "service connected");
			mPlayerService = ((MusicPlayerService.PlayerBinder) service).getService();
			mPlayerService.setAudioItem(mFileManager.getAudioItem(fileId), MusicPlayerActivity.this,  MusicPlayerActivity.this, null, MusicPlayerActivity.this, null);
			
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					int percentage = 0;
					long cur = mPlayerService.getCurrentPosition();
					long dur = mPlayerService.getDuration();
					if (dur > 0 && cur >= 0)
						percentage = (int)(cur * 100/dur);
					Log.d(TAG, "current play percentage:" + percentage + ", current " + cur + ", duration " + dur);
					mSeekBar.setProgress(percentage);
					if (mRefreshUI) {
						mHandler.postDelayed(this, 200);
					}
				}
				
			}, 200);

		}
	
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mPlayerService = null;
		}
	};
	
	@Override
	protected void onDestroy() {
		mRefreshUI = false;
		unbindService(mConn);
		super.onDestroy();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mPlayerService.play();
		mSeekBar.setClickable(true);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.d(TAG, "music buffered %" + percent);
		this.mSeekBar.setSecondaryProgress(percent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		MenuItem settingItem = menu.findItem(R.id.action_settings);
		settingItem.setOnMenuItemClickListener(new OnSettingClickedListener(this));
		
		MenuItem downloadItem = menu.findItem(R.id.action_download);
		downloadItem.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(MusicPlayerActivity.this, DownloadingActivity.class);
				startActivity(intent);
				return true;
			}
		});
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

}
