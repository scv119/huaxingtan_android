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

public class OnlineFragment extends ListFragment implements ServiceConnection{
	private static final String LOG = OnlineFragment.class.getCanonicalName();
	private List<AudioItem> mData;
	private AudioJsonAsyncTask mTask;
	private AudioItemAdapter mAdapter;
	private MusicPlayerService mPlayerService;
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (mData == null) {
			mData = new ArrayList<AudioItem>();
			mAdapter = new AudioItemAdapter(this.getActivity(), mData);
			setListAdapter(mAdapter);
			mTask = new AudioJsonAsyncTask(mData, mAdapter);
			Log.e(LOG, "fragment start load data");
			mTask.execute("http://huaxingtan.cn/api?version=1.0");
		}
		getActivity().getApplicationContext().bindService(new Intent(getActivity(),  MusicPlayerService.class),
				this, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onDestroy() {
		getActivity().getApplicationContext().unbindService(this);
		super.onDestroy();
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		AudioItem item = mData.get(position);
		this.mPlayerService.setAudioItem(item, null, null, null, null, null);
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

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.mPlayerService = ((MusicPlayerService.PlayerBinder) service).getService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		this.mPlayerService = null;
	}
	
}
