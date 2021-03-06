package cn.huaxingtan.view;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.huaxingtan.controller.CachedDataProvider;
import cn.huaxingtan.controller.CachedDataProvider.Callback;
import cn.huaxingtan.controller.FileManager;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.Serial;
import cn.huaxingtan.service.MusicPlayerService;
import cn.huaxingtan.util.JsonAsyncTask;
import cn.huaxingtan.util.Serialize;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class OfflineFragment extends ListFragment {
	private static final String TAG = OfflineFragment.class.getCanonicalName();
	private List<Serial> mData;
	private SerialAdapter mAdapter;
	private FileManager mFileManager;
	public static volatile WeakReference<OfflineFragment> running;
	public static final String EXTRA = "AudioItem";
	private Handler mHandler;
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		mFileManager = new FileManager(getActivity());
		mData = new ArrayList<Serial>();
		mAdapter = new SerialAdapter(this.getActivity(), mData);
		setListAdapter(mAdapter);
		mHandler = new Handler();
		refreshUI();
    }
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		running = new WeakReference<OfflineFragment>(this);
		refreshUI();
	}
	
	@Override
	public void onPause() {
		super.onPause();		
		if (running != null)
			running.clear();
		running.clear();
		running = null;
		refreshUI();
	}
	
	
	@Override
	public void onDestroy() {
		mFileManager.save();
		super.onDestroy();
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(TAG, "item clicked");
		Serial item = mData.get(position);
		Intent intent = new Intent(getActivity(), DetailActivity.class);
		byte[] bytes;
		try {
			bytes = Serialize.serialize(item);
			intent.putExtra("Serial", bytes);
			intent.putExtra("isOffline", true);
			startActivity(intent);
		} catch (IOException e) {
			Log.e(TAG, "fail to serialize", e);
		}
    }
	
	public void refreshUI() {
		mData.clear();
		List<Serial> tmp = mFileManager.getSerials();
		
		for (Serial tItem:tmp) {
			if (tItem.getDownloaded() > 0)
				mData.add(tItem);
		}
		
		Collections.sort(mData, new Comparator<Serial>(){

			@Override
			public int compare(Serial lhs, Serial rhs) {
				return lhs.getId() - rhs.getId();
			}});
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				mAdapter.notifyDataSetChanged();
			}
			
		});
	}
	
}
