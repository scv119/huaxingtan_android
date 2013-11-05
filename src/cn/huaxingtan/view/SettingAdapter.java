package cn.huaxingtan.view;


import cn.huaxingtan.player.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class SettingAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	
	public SettingAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return 4;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (position < 3)
			convertView = mInflater.inflate(R.layout.setting_switch_item, null);
		else
			convertView = mInflater.inflate(R.layout.setting_text_item, null);
		
		
		if (position < 3) {
			Switch switzh = (Switch) convertView.findViewById(R.id.setting_switch);
			int res = R.string.setting_celldata;
			if (position == 1)
				res = R.string.setting_backdownload;
			if (position == 2)
				res = R.string.setting_backplay;
			switzh.setText(res);
			switzh.setOnCheckedChangeListener(new SwitzhListener(position));
		} else {
			TextView text = (TextView)convertView.findViewById(R.id.setting_text);
			text.setText(R.string.setting_feedback);
			text.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					//TODO feedback
				}
				
			});
		}
		return convertView;
	}
	
	class SwitzhListener implements OnCheckedChangeListener {
		private int mPos;
		SwitzhListener (int pos) {
			mPos = pos;
		}
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			//TODO setting changed
			switch(mPos) {
				default:
					break;
			}
			
		}
		
	}
	
}