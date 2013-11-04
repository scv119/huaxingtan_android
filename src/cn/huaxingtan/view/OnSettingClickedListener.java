package cn.huaxingtan.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.MenuItem;

public class OnSettingClickedListener implements MenuItem.OnMenuItemClickListener{
	private Context mContext;
	private AlertDialog mAlertDialog;
	
	public OnSettingClickedListener(Context context) {
		mContext = context;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		mAlertDialog = new AlertDialog.Builder(mContext)
						.setTitle("请输入")
					 	.setIcon(android.R.drawable.ic_dialog_info)
					 	.setPositiveButton("确定", null)
					 	.setNegativeButton("取消", null)
						.show();
		return true;
	}

}
