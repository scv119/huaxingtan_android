package cn.huaxingtan.view;

import cn.huaxingtan.player.R;
import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class OnSettingClickedListener implements MenuItem.OnMenuItemClickListener{
	private Context mContext;
	private AlertDialog mAlertDialog;
	
	public OnSettingClickedListener(Context context) {
		mContext = context;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		//
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View dialogView = inflater.inflate(R.layout.setting, null);
		ListView listView = (ListView) dialogView.findViewById(R.id.setting_list);
		listView.setAdapter(new SettingAdapter(mContext));
		mAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Light))
				.setTitle(R.string.action_settings)
				.setView(dialogView)
				.setPositiveButton(R.string.finish, null)
				.show();
		return true;
	}

}
