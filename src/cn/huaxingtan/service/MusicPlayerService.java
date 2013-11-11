package cn.huaxingtan.service;

import java.io.File;
import java.io.IOException;

import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.AudioItem.Status;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MusicPlayerService extends Service {
	private static final String TAG = MusicPlayerService.class.getCanonicalName();
	private MediaPlayer mMediaPlayer = null;
	private AudioItem mAudioItem = null;
	private final IBinder mBinder = new PlayerBinder();
	private WifiLock mWifiLock;
	private boolean doneBuffering = false;

	
	@Override
	public void onCreate() {
		super.onCreate();
		if (mMediaPlayer != null)
			mMediaPlayer.reset();
		else {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
			mWifiLock =  ((WifiManager) getSystemService(Context.WIFI_SERVICE))
				    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
			mWifiLock.setReferenceCounted(false);
		}
		Log.i(TAG, "music player service started");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMediaPlayer != null) {
			mWifiLock.release();
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_NOT_STICKY;
	}
	
	public boolean setAudioItem(AudioItem item, 
			OnPreparedListener onPreparedListener, 
			OnSeekCompleteListener onSeekCompleteListener, 
			OnErrorListener onErrorListener,
			final OnBufferingUpdateListener onBufferingUpdateListener,
			OnCompletionListener onCompletionListener) {
		
		if (onPreparedListener != null)
			mMediaPlayer.setOnPreparedListener(onPreparedListener);

		if (onErrorListener != null)
			mMediaPlayer.setOnErrorListener(onErrorListener);
		
		MediaPlayer.OnBufferingUpdateListener bufferListenerProxy = new MediaPlayer.OnBufferingUpdateListener() {
		    public void onBufferingUpdate(MediaPlayer mp, int percent) {
		        if(percent == 100)
		            doneBuffering = true;
		        if (onBufferingUpdateListener != null)
		        	onBufferingUpdateListener.onBufferingUpdate(mp, percent);
		    }
		};
		mMediaPlayer.setOnBufferingUpdateListener(bufferListenerProxy);
		
		
		if (onSeekCompleteListener != null)
			mMediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
		
		if (onCompletionListener != null)
			mMediaPlayer.setOnCompletionListener(onCompletionListener);
		
		if (getNowPlayingId() != item.getFileId()) {
			mMediaPlayer.reset();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mAudioItem = item;
			String URI = mAudioItem.getFileUrl();
			doneBuffering = false;
			if (mAudioItem.getStatus() == Status.FINISHED)
				URI = new File(this.getFilesDir(), mAudioItem.getPath()).getAbsolutePath();
				bufferListenerProxy.onBufferingUpdate(mMediaPlayer, 100);
			try {
				mMediaPlayer.setDataSource(URI);
			} catch (Exception e) {
				Log.d(TAG, "failed to load "+URI, e);
				doneBuffering = true;
				return false;
			}
			wifiLock();
			mMediaPlayer.prepareAsync();
		} else {
			if (onPreparedListener!=null)
				onPreparedListener.onPrepared(mMediaPlayer);
		}
		
		return true;
	}
	
	public void play() {
		if (!mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
		}
	}
	
	public void pause() {
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
	}
	
	public long getDuration() {
		long dur =  mMediaPlayer.getDuration();
		if (dur >= 1380000000L)
			return 0;
		return dur;
	}
	
	public long getCurrentPosition() {
		return mMediaPlayer.getCurrentPosition();
	}
	
	public boolean isPlaying() {
		return mMediaPlayer.isPlaying();
	}
	
	public boolean seek(double percentage) {
		long duration = getDuration();
		if (duration <= 0)
			return false;
		mMediaPlayer.seekTo((int)(duration/100.0 * percentage));
		return true;
	}
	
	public long getNowPlayingId() {
		if (mMediaPlayer.isPlaying() && mAudioItem != null)
			return mAudioItem.getFileId();
		return -1;
	}
	
	
	public class PlayerBinder extends Binder {
		public MusicPlayerService getService() {
			return MusicPlayerService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public void wifiLock() {
		if (mAudioItem != null && mAudioItem.getStatus() != Status.FINISHED
				&& !doneBuffering)
			mWifiLock.acquire();
	}
	
	public void wifiRelease() {
		doneBuffering = true;
		mWifiLock.release();
	}

	

}
