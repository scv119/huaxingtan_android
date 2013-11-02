package cn.huaxingtan.view;

import java.util.List;

import cn.huaxingtan.player.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.huaxingtan.model.Serial;

public class SerialAdapter extends BaseAdapter {
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
		convertView = mInflater.inflate(R.layout.list_item, null);
		TextView title = (TextView) convertView.findViewById(R.id.item_title);
		title.setText(mData.get(position).getName());
		TextView content = (TextView)convertView.findViewById(R.id.item_content);
		content.setText(mData.get(position).getName());
		return convertView;
	}
	
}