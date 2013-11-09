package cn.huaxingtan.service;

import java.io.IOException;

import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.AudioItem.Status;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MusicPlayerService extends Service {
	private static final String TAG = MusicPlayerService.class.getCanonicalName();
	private MediaPlayer mMediaPlayer = null;
	private AudioItem mAudioItem = null;
	private final IBinder mBinder = new PlayerBinder();

	
	@Override
	public void onCreate() {
		super.onCreate();
		if (mMediaPlayer != null)
			mMediaPlayer.reset();
		else
			mMediaPlayer = new MediaPlayer();
		Log.i(TAG, "music player service started");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMediaPlayer != null) {
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
			OnBufferingUpdateListener onBufferingUpdateListener,
			OnCompletionListener onCompletionListener) {
		
		if (onPreparedListener != null)
			mMediaPlayer.setOnPreparedListener(onPreparedListener);

		if (onErrorListener != null)
			mMediaPlayer.setOnErrorListener(onErrorListener);

		
		if (onBufferingUpdateListener != null)
			mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
		
		if (onSeekCompleteListener != null)
			mMediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
		
		if (onCompletionListener != null)
			mMediaPlayer.setOnCompletionListener(onCompletionListener);
		
		if (getNowPlayingId() != item.getFileId()) {
			mMediaPlayer.reset();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mAudioItem = item;
			String URI = mAudioItem.getFileUrl();
			if (mAudioItem.getStatus() == Status.FINISHED)
				URI = mAudioItem.getPath();
			try {
				mMediaPlayer.setDataSource(URI);
			} catch (Exception e) {
				Log.d(TAG, "failed to load "+URI, e);
				return false;
			}

			mMediaPlayer.prepareAsync();
		}
		
		
		return true;
	}
	
	public void play() {
		if (!mMediaPlayer.isPlaying())
			mMediaPlayer.start();
	}
	
	public void pause() {
		if (mMediaPlayer.isPlaying())
			mMediaPlayer.pause();
	}
	
	public long getDuration() {
		return mMediaPlayer.getDuration();
	}
	
	public long getCurrentPosition() {
		return mMediaPlayer.getCurrentPosition();
	}
	
	public boolean isPlaying() {
		return mMediaPlayer.isPlaying();
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

	

}
