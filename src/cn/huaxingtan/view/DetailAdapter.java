package cn.huaxingtan.view;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import cn.huaxingtan.player.R;
import cn.huaxingtan.receiver.DownloadManagerReceiver;
import cn.huaxingtan.service.MusicPlayerService;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = mInflater.inflate(R.layout.detail_item, null);
		mView2Pos.put(convertView, position);
		TextView title = (TextView) convertView.findViewById(R.id.detail_item_title);
		title.setText(mFileManager.getAudioItem(mData.get(position)).getName());
		TextView content = (TextView)convertView.findViewById(R.id.detail_item_info);
		content.setText(mFileManager.getAudioItem(mData.get(position)).getName());
		ImageButton button = (ImageButton) convertView.findViewById(R.id.detail_item_button);
		button.setOnClickListener(new ButtonOnClickListener(mData.get(position), button));
		
		ImageView playingImage = (ImageView) convertView.findViewById(R.id.detail_play_image); 
		if (mPlayer.getNowPlayingId() == mFileManager.getAudioItem(mData.get(position)).getFileId()) {
			playingImage.setVisibility(View.VISIBLE);
		} else
			playingImage.setVisibility(View.INVISIBLE);
		
		return convertView;
	}
	
	class ButtonOnClickListener implements View.OnClickListener {
		private Long mId;
		private ImageButton mButton;
		
		ButtonOnClickListener(Long id, ImageButton button) {
			mId = id;
			mButton = button;
		}
		@Override
		public void onClick(View v) {
			AudioItem item = DetailAdapter.this.mFileManager.getAudioItem(mId);
			if (item.getStatus() == AudioItem.Status.STOPED) {
				Uri uri = Uri.parse(item.getFileUrl());
				DownloadManager.Request request = new DownloadManager.Request(uri);
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
				request.setTitle(item.getName());
				long id = DetailAdapter.this.mDownloadManager.enqueue(request);
				mFileManager.addDownloadId(id, item.getFileId());
				item.setDownloadId(id);
				item.setStatus(Status.STARTED);
				mFileManager.set(item);
			}
		}
		
	}
	
}