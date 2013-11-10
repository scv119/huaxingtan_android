package cn.huaxingtan.view;

import java.util.List;

import com.umeng.analytics.MobclickAgent;

import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.controller.CachedDataProvider.Callback;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.player.R;
import cn.huaxingtan.service.MusicPlayerService;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Menu mMenu;
	private FileManager mFileManager;
	private MenuItem mPlayItem;
	private MusicPlayerService mMusicPlayerService;
	
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
		bindService(new Intent(this,  MusicPlayerService.class),
				mConn, Context.BIND_AUTO_CREATE);
	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		}
	
	public void onPause() {
		super.onPause();
		MobclickAgent.onResume(this);
	}
	
	
	private ServiceConnection mConn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mMusicPlayerService = ((MusicPlayerService.PlayerBinder) service).getService();
			}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMusicPlayerService = null;
		}
	};
	
	@Override
	public void onDestroy() {
		mFileManager.save();
		unbindService(mConn);
		mMusicPlayerService = null;
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
		
		mPlayItem = menu.findItem(R.id.action_play);
		mPlayItem.setVisible(true);
		
		mPlayItem.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				long fileId = -1;
				if (mMusicPlayerService != null) {
					fileId = mMusicPlayerService.getNowPlayingId();
				}
				if (fileId != -1) {
					Intent intent = new Intent(MainActivity.this, MusicPlayerActivity.class);
					intent.putExtra("fileId", fileId);
					startActivity(intent);
					return true;
				}
				Toast.makeText(MainActivity.this, "没有播放内容", Toast.LENGTH_SHORT).show();
				return false;
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
