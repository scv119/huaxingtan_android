package cn.huaxingtan.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.huaxingtan.controller.CachedDataProvider;
import cn.huaxingtan.controller.CachedDataProvider.Callback;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.Serial;
import cn.huaxingtan.player.R;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

public class OnlineFragment extends Fragment {
	private static final String TAG = OnlineFragment.class.getCanonicalName();
	private List<Serial> mData;
	private GridItemAdapter mAdapter;
	private CachedDataProvider mDataProvider = CachedDataProvider.INSTANCE;
	
	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.online_fragment, null);
		GridView gridView = (GridView) v.findViewById(R.id.grid_view);
		mData = new ArrayList<Serial>();
		mAdapter = new GridItemAdapter(inflater, mData);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i(TAG, "item clicked");
				Serial item = mData.get(position);
				Intent intent = new Intent(getActivity(), DetailActivity.class);
				byte[] bytes;
				try {
					bytes = Serialize.serialize(item);
					intent.putExtra("Serial", bytes);
					startActivity(intent);
				} catch (IOException e) {
					Log.e(TAG, "fail to serialize", e);
				}
			}
		});
		
		Log.i(TAG, "fragment start load data");
		mDataProvider.getSerials(new Callback(){

			@Override
			public void success(Object result) {
				List<Serial> list = (List<Serial>) result;
				if (list.size() == 0)
					return;
				
				mData.clear();
				for (Serial o:list) {
					
					mData.add(o);
					mData.add(o);
					mData.add(o);
					mData.add(o);
				}
				mAdapter.notifyDataSetChanged();
				
			}

			@Override
			public void fail(Exception e) {
			}
			
		});
		
		return v;
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	
}
