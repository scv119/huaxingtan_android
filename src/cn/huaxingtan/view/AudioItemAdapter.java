package cn.huaxingtan.view;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import cn.huaxingtan.player.R;
import cn.huaxingtan.service.MusicPlayerService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.huaxingtan.model.AudioItem;

@Deprecated
public class AudioItemAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<AudioItem> mData;
	private Map<View, Integer> mView2Pos;
	private MusicPlayerService mPlayer;
	
	public AudioItemAdapter(Context context, List<AudioItem> data, MusicPlayerService player) {
		this.mInflater = LayoutInflater.from(context);
		this.mData = data;
		this.mView2Pos = new WeakHashMap<View, Integer>();
		this.mPlayer = player;
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
			convertView = mInflater.inflate(R.layout.list_item, null);
		mView2Pos.put(convertView, position);
		TextView title = (TextView) convertView.findViewById(R.id.item_title);
		title.setText(mData.get(position).getName());
		TextView content = (TextView)convertView.findViewById(R.id.item_content);
		content.setText(mData.get(position).getName());
		return convertView;
	}
	
}