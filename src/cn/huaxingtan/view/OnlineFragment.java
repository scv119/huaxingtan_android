package cn.huaxingtan.view;

import java.util.ArrayList;
import java.util.List;

import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.util.JsonAsyncTask;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

public class OnlineFragment extends ListFragment{
	private List<AudioItem> mData;
	private AudioJsonAsyncTask mTask;
	private AudioItemAdapter mAdapter;
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mData = new ArrayList<AudioItem>();
		mAdapter = new AudioItemAdapter(this.getActivity(), mData);
		getListView().setAdapter(mAdapter);
		mTask = new AudioJsonAsyncTask(mData, mAdapter);
		mTask.execute("http://huaxingtan.cn/api?version=1.0");
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
