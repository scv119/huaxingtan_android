package cn.huaxingtan.view;

import java.util.List;

import cn.huaxingtan.player.R;
import cn.huaxingtan.util.ImageDownloader;
import cn.huaxingtan.util.ImageDownloader.Mode;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.Serial;

public class SerialAdapter extends BaseAdapter {
	private static final String TAG = SerialAdapter.class.getCanonicalName();
    private static final ImageDownloader mImageDownloader = new ImageDownloader();
    static {
    	mImageDownloader.setMode(Mode.CORRECT);
    }

	
	private LayoutInflater mInflater;
	private List<Serial> mData;
	
	public SerialAdapter(Context context, List<Serial> data) {
		this.mInflater = LayoutInflater.from(context);
		this.mData = data;
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
		return mData.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = mInflater.inflate(R.layout.my_item, null);
		TextView title = (TextView) convertView.findViewById(R.id.my_item_title);
		title.setText(mData.get(position).getName());
		TextView content = (TextView)convertView.findViewById(R.id.my_item_info);
		content.setText("已下载" + mData.get(position).getDownloaded() + "讲");
		ImageView imageView = (ImageView) convertView.findViewById(R.id.my_item_image);
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		mImageDownloader.download(mData.get(position).getCoverUrl(), imageView);
		return convertView;
	}
	
	
	
}