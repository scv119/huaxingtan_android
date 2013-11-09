package cn.huaxingtan.view;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import cn.huaxingtan.player.R;
import cn.huaxingtan.receiver.DownloadManagerReceiver;
import cn.huaxingtan.service.MusicPlayerService;
import cn.huaxingtan.util.Misc;
import cn.huaxingtan.util.SettingManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.AudioItem.Status;

public class DetailAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Long> mData;
	private DownloadManager mDownloadManager;
	private Context mContext;
	private FileManager mFileManager;

	private Map<View, Integer> mView2Pos;
	private MusicPlayerService mPlayer;
	
	
	public DetailAdapter(Context context, List<Long> data, MusicPlayerService musicPlayer) {
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mData = data;
		this.mDownloadManager = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		this.mFileManager = new FileManager(mContext);
		this.mPlayer = musicPlayer;
		this.mView2Pos = new WeakHashMap<View, Integer>();
	}
	
	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mFileManager.getAudioItem(mData.get(position)).getFileId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = mInflater.inflate(R.layout.detail_item, null);
		mView2Pos.put(convertView, position);
		TextView title = (TextView) convertView.findViewById(R.id.detail_item_title);
		title.setText(mFileManager.getAudioItem(mData.get(position)).getName());
		TextView content = (TextView)convertView.findViewById(R.id.detail_item_info);
		content.setText(mFileManager.getAudioItem(mData.get(position)).getAuthor() + "  " 
						+ Misc.formatDuration(mFileManager.getAudioItem(mData.get(position)).getDuration() ));
		ImageButton button = (ImageButton) convertView.findViewById(R.id.detail_item_button);
		button.setOnClickListener(new ButtonOnClickListener(mData.get(position), button, false));
		
		ImageButton fButton = (ImageButton) convertView.findViewById(R.id.detail_finish_button);
		fButton.setOnClickListener(new ButtonOnClickListener(mData.get(position), fButton, true)); 
		
		if (mFileManager.getAudioItem(mData.get(position)).getStatus() == Status.FINISHED){
			fButton.setVisibility(View.VISIBLE);
			button.setVisibility(View.INVISIBLE);
		} else {
			fButton.setVisibility(View.INVISIBLE);
			button.setVisibility(View.VISIBLE);
		}
		
		ImageView playingImage = (ImageView) convertView.findViewById(R.id.detail_play_image); 
		if (mPlayer.getNowPlayingId() == mFileManager.getAudioItem(mData.get(position)).getFileId()) {
			playingImage.setVisibility(View.VISIBLE);
		} else
			playingImage.setVisibility(View.INVISIBLE);
		
		TextView infoView = (TextView) convertView.findViewById(R.id.detial_button_text);
		updateInfoView(infoView, mData.get(position));
		
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Long fildId = mData.get(position);
				Intent intent = new Intent(mContext, MusicPlayerActivity.class);
				intent.putExtra("fileId", fildId);
				mContext.startActivity(intent);
			}
		});
		return convertView;
	}
	
	private void updateInfoView(TextView infoView, long fileId) {
		AudioItem item = mFileManager.getAudioItem(fileId);
		if (item.getStatus() == Status.STOPED) {
			infoView.setVisibility(View.INVISIBLE);
		} else if (item.getStatus() == Status.STARTED || item.getStatus() == Status.PAUSED) {
			infoView.setVisibility(View.VISIBLE);
			int pct = (int)(100.0f * item.getFinishedSize() / item.getFileSize());
			String percentage = pct + "";
			while (percentage.length() != 3) {
				percentage =  " " + percentage;
			}
			infoView.setText("下載中"+ percentage + "%");
		} else {
			infoView.setVisibility(View.VISIBLE);
			infoView.setText("     已下載");
		}

	}
	
	private void udpateButtonView(ImageButton button, ImageButton fButton, long fileId) {
		AudioItem item = mFileManager.getAudioItem(fileId);
		
		if (item.getStatus() == Status.FINISHED){
			fButton.setVisibility(View.VISIBLE);
			button.setVisibility(View.INVISIBLE);
		} else {
			fButton.setVisibility(View.INVISIBLE);
			button.setVisibility(View.VISIBLE);
		}
	}
	
	class ButtonOnClickListener implements View.OnClickListener {
		private Long mId;
		
		ButtonOnClickListener(Long id, ImageButton button, boolean isFinishButton) {
			mId = id;
		}
		@Override
		public void onClick(View v) {
			AudioItem item = DetailAdapter.this.mFileManager.getAudioItem(mId);
			if (item.getStatus() == AudioItem.Status.STOPED) {
				Uri uri = Uri.parse(item.getFileUrl());
				DownloadManager.Request request = new DownloadManager.Request(uri);
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
				request.setTitle(item.getName());
				request.setAllowedNetworkTypes(SettingManager.getAllowNetworkType(mContext));
				long id = DetailAdapter.this.mDownloadManager.enqueue(request);
				mFileManager.addDownloadId(id, item.getFileId());
				item.setDownloadId(id);
				item.setStatus(Status.STARTED);
				mFileManager.set(item);
				Toast.makeText(mContext, "开始下载",
					     Toast.LENGTH_SHORT).show();
			} else if (item.getStatus() == AudioItem.Status.STARTED) {
				Toast.makeText(mContext, "正在下载",
					     Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "下载完成",
					     Toast.LENGTH_SHORT).show();
			}
		}
		
	}

	public void updatePlayingId() {
		long playingId = mPlayer.getNowPlayingId();
		for (View view:this.mView2Pos.keySet()) {
			int visibility = (playingId == mData.get(mView2Pos.get(view)) ? View.VISIBLE : View.INVISIBLE);
			ImageView playingImage = (ImageView) view.findViewById(R.id.detail_play_image); 
			playingImage.setVisibility(visibility);
		}
	}
	
	public void updateInfoView() {
		for (View view:this.mView2Pos.keySet()) {
			TextView infoView = (TextView) view.findViewById(R.id.detial_button_text);
			ImageButton button = (ImageButton) view.findViewById(R.id.detail_item_button);
			ImageButton fbutton = (ImageButton) view.findViewById(R.id.detail_finish_button);
			updateInfoView(infoView, mData.get(mView2Pos.get(view)));
			udpateButtonView(button, fbutton, mData.get(mView2Pos.get(view)));
		}
	}
	
}