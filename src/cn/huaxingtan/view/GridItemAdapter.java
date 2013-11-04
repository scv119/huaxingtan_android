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
import android.widget.ImageView;
import android.widget.TextView;
import cn.huaxingtan.model.Serial;

public class GridItemAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Serial> mData;
	private static final ImageDownloader imageDownloader = new ImageDownloader();
	static {

		imageDownloader.setMode(Mode.NO_DOWNLOADED_DRAWABLE);
	}
	
	public GridItemAdapter(LayoutInflater inflater, List<Serial> data) {
		this.mInflater = inflater;
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
			convertView = mInflater.inflate(R.layout.grid_item, null);
		TextView title = (TextView) convertView.findViewById(R.id.grid_text);
		title.setText(mData.get(position).getName());
		ImageView image = (ImageView)convertView.findViewById(R.id.grid_image);
		image.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageDownloader.download(mData.get(position).getCoverUrl(), image);
		return convertView;
	}
	
}