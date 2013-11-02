package cn.huaxingtan.view;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.player.R;
import cn.huaxingtan.service.MusicPlayerService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;


public class MusicPlayerActivity extends Activity implements OnPreparedListener, OnBufferingUpdateListener{
	private static final String TAG = MusicPlayerActivity.class.getCanonicalName();
	private AudioItem mAudioItem;
	private MusicPlayerService mPlayerService;
	private SeekBar mSeekBar;
	private Handler mHandler;
	private boolean mRefreshUI;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent musicIntent = getIntent();
		mAudioItem = (AudioItem)musicIntent.getExtras().get(OnlineFragment.EXTRA);

		setContentView(R.layout.activity_music_player);
		mSeekBar = (SeekBar)findViewById(R.id.music_seekBar);
		
		mHandler = new Handler();
		mRefreshUI = true;
		bindService(new Intent(this,  MusicPlayerService.class),
				mConn, Context.BIND_AUTO_CREATE);
		
	}
	
	private ServiceConnection mConn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "service connected");
			mPlayerService = ((MusicPlayerService.PlayerBinder) service).getService();
			mPlayerService.setAudioItem(mAudioItem, MusicPlayerActivity.this, null, null, MusicPlayerActivity.this, null);
			
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
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.d(TAG, "music buffered %" + percent);
		this.mSeekBar.setSecondaryProgress(percent);
	}

	

}
