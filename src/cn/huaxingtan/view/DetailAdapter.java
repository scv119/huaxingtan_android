package cn.huaxingtan.view;

import java.util.List;

import cn.huaxingtan.player.R;
import android.content.Context;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.Serial;

public class DetailAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<AudioItem> mData;
	
	public DetailAdapter(Context context, List<AudioItem> data) {
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
		return convertView;
	}
	
}