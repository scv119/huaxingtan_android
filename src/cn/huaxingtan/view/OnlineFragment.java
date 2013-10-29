package cn.huaxingtan.view;

import java.util.ArrayList;
import java.util.List;

import cn.huaxingtan.model.AudioItem;
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

public class OnlineFragment extends ListFragment {
	private static final String LOG = OnlineFragment.class.getCanonicalName();
	private List<AudioItem> mData;
	private AudioJsonAsyncTask mTask;
	private AudioItemAdapter mAdapter;
	
	public static final String EXTRA = "AudioItem";
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (mData == null) {
			mData = new ArrayList<AudioItem>();
			mAdapter = new AudioItemAdapter(this.getActivity(), mData);
			setListAdapter(mAdapter);
			mTask = new AudioJsonAsyncTask(mData, mAdapter);
			Log.i(LOG, "fragment start load data");
			mTask.execute("http://huaxingtan.cn/api?version=1.0");
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		AudioItem item = mData.get(position);
		Intent musicIntent = new Intent(getActivity(), MusicPlayerActivity.class);
		musicIntent.putExtra(EXTRA, item);
		startActivity(musicIntent);
    }
	
	private static class AudioJsonAsyncTask extends JsonAsyncTask {
		List<AudioItem> mData;
		BaseAdapter mAdapter;
		
		public AudioJsonAsyncTask(List<AudioItem> data, BaseAdapter adapter) {
			this.mData = data;
			this.mAdapter = adapter;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			List<Object> list = (List<Object>) result;
			if (list.size() == 0)
				return;
			
			mData.clear();
			for (Object o:list) {
				AudioItem item = AudioItem.loadJson(o);
				mData.add(item);
			}
			
			mAdapter.notifyDataSetChanged();
			
	    }
	}

	
	
}
