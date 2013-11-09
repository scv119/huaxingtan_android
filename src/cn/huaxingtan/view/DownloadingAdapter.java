package cn.huaxingtan.view;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import cn.huaxingtan.player.R;
import cn.huaxingtan.receiver.DownloadManagerReceiver;
import cn.huaxingtan.service.MusicPlayerService;
import cn.huaxingtan.util.ImageDownloader;
import cn.huaxingtan.util.Misc;
import cn.huaxingtan.util.ImageDownloader.Mode;
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
import android.widget.Toast;
import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.AudioItem.Status;

public class DownloadingAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Long> mData;
	private Context mContext;
	private FileManager mFileManager;
    private static final ImageDownloader mImageDownloader = new ImageDownloader();
    static {
    	mImageDownloader.setMode(Mode.CORRECT);
    }

	private Map<View, Integer> mView2Pos;
	
	
	public DownloadingAdapter(Context context, List<Long> data) {
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mData = data;
		this.mFileManager = new FileManager(mContext);
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
			convertView = mInflater.inflate(R.layout.downloading_item, null);
		mView2Pos.put(convertView, position);
		ImageView image = (ImageView) convertView.findViewById(R.id.downloading_image);
		mImageDownloader.download(mFileManager.getSerial(mFileManager.getAudioItem(mData.get(position)).getSerialId()).getCoverUrl(), 
				image);
		TextView title = (TextView) convertView.findViewById(R.id.downloading_name);
		title.setText(mFileManager.getAudioItem(mData.get(position)).getName());
		TextView infoView = (TextView) convertView.findViewById(R.id.downloading_percentage);
		updateInfoView(infoView, mData.get(position));
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
			infoView.setText("已下載");
		}

	}
	

	public void updateInfoView() {
		for (View view:this.mView2Pos.keySet()) {
			TextView infoView = (TextView) view.findViewById(R.id.downloading_percentage);
			updateInfoView(infoView, mData.get(mView2Pos.get(view)));
		}
	}
	
}