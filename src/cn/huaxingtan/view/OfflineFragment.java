package cn.huaxingtan.view;

import java.io.IOException;
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
	
	public static final String EXTRA = "AudioItem";
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (mData == null) {
			mData = new ArrayList<Serial>();
			mFileManager = new FileManager(getActivity());
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
			
			mAdapter = new SerialAdapter(this.getActivity(), mData);
			setListAdapter(mAdapter);
		}
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
	
}
