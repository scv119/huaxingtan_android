package cn.huaxingtan.view;

import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.player.R;
import cn.huaxingtan.service.MusicPlayerService;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

public class MainActivity extends Activity {
	private Menu mMenu;
	private FileManager mFileManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFileManager = new FileManager(this);
		setContentView(R.layout.activity_main);
		final ActionBar bar = getActionBar();
		startService(new Intent(this, MusicPlayerService.class));
		bar.addTab(bar.newTab()
				.setText(R.string.tab_online)
				.setTabListener(new TabListener<OnlineFragment>(
						this, "online", OnlineFragment.class)));
		bar.addTab(bar.newTab()
				.setText(R.string.tab_local)
				.setTabListener(new TabListener<OfflineFragment>(
						this, "local", OfflineFragment.class)));
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	}
	
	@Override
	public void onDestroy() {
		mFileManager.save();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		mMenu = menu;
		getMenuInflater().inflate(R.menu.main, menu);
		
		MenuItem settingItem = menu.findItem(R.id.action_settings);
		settingItem.setOnMenuItemClickListener(new OnSettingClickedListener(this));
		
		MenuItem downloadItem = menu.findItem(R.id.action_download);
		downloadItem.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(MainActivity.this, DownloadingActivity.class);
				startActivity(intent);
				return true;
			}
		});
		
		return super.onCreateOptionsMenu(menu);
	}

	
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;
		private final Bundle mArgs;
		private Fragment mFragment;
		
		public TabListener(Activity activity, String tag, Class<T> clz) {
			this(activity, tag, clz, null);
		}
		
		public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
			mArgs = args;
			mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
			if (mFragment != null && !mFragment.isDetached()) {
				FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
				ft.detach(mFragment);
				ft.commit();
			}
		}
		
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (mFragment == null) {
				mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				ft.attach(mFragment);
			}
		}
		
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null) {
				ft.detach(mFragment);
			}
		}
		
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			return;
		}


	}

}
