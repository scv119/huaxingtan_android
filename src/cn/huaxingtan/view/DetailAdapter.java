package cn.huaxingtan.view;

import java.util.List;

import cn.huaxingtan.player.R;
import cn.huaxingtan.receiver.DownloadManagerReceiver;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.Serial;

public class DetailAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<AudioItem> mData;
	private DownloadManager mDownloadManager;
	private Context mContext;
	private FileManager mFileManager;
	
	public DetailAdapter(Context context, List<AudioItem> data) {
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mData = data;
		this.mDownloadManager = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		this.mFileManager = new FileManager(mContext);
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
		return mData.get(position).getFileId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = mInflater.inflate(R.layout.detail_item, null);
		TextView title = (TextView) convertView.findViewById(R.id.detail_item_title);
		title.setText(mData.get(position).getName());
		TextPaint tp = title.getPaint(); 
//		tp.setFakeBoldText(true);
		TextView content = (TextView)convertView.findViewById(R.id.detail_item_info);
		content.setText(mData.get(position).getName());
		tp = content.getPaint(); 
//		tp.setFakeBoldText(true);
		ImageButton button = (ImageButton) convertView.findViewById(R.id.detail_item_button);
		button.setOnClickListener(new ButtonOnClickListener(mData.get(position), button));
		
		return convertView;
	}
	
	class ButtonOnClickListener implements View.OnClickListener {
		private AudioItem mAudioItem;
		private ImageButton mButton;
		
		ButtonOnClickListener(AudioItem item, ImageButton button) {
			mAudioItem = item;
			mButton = button;
		}
		@Override
		public void onClick(View v) {
			if (mAudioItem.getStatus() == AudioItem.Status.STOPED) {
				Uri uri = Uri.parse(mAudioItem.getFileUrl());
				DownloadManager.Request request = new DownloadManager.Request(uri);
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
				request.setTitle(mAudioItem.getName());
				long id = DetailAdapter.this.mDownloadManager.enqueue(request);
				mFileManager.addDownloadId(id, mAudioItem.getFileId());
				mAudioItem.setDownloadId(id);
			}
		}
		
	}
	
}