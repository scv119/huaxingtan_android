package cn.huaxingtan.view;

import java.util.ArrayList;
import java.util.List;

import cn.huaxingtan.controller.CachedDataProvider;
import cn.huaxingtan.controller.CachedDataProvider.Callback;
import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.Serial;
import cn.huaxingtan.service.MusicPlayerService;
import cn.huaxingtan.util.JsonAsyncTask;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class OfflineFragment extends ListFragment {
	private static final String LOG = OfflineFragment.class.getCanonicalName();
	private List<Serial> mData;
	private SerialAdapter mAdapter;
	private FileManager mFileManager;
	
	public static final String EXTRA = "AudioItem";
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (mData == null) {
			mData = new ArrayList<Serial>();
			mFileManager = new FileManager(getActivity());
			mFileManager.getSerials();
			
			mAdapter = new SerialAdapter(this.getActivity(), mData);
			setListAdapter(mAdapter);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		Serial item = mData.get(position);
//		Intent musicIntent = new Intent(getActivity(), MusicPlayerActivity.class);
//		musicIntent.putExtra(EXTRA, item);
//		startActivity(musicIntent);
    }
	
}
