package cn.huaxingtan.view;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import cn.huaxingtan.controller.CachedDataProvider;
import cn.huaxingtan.controller.CachedDataProvider.Callback;
import cn.huaxingtan.model.AudioItem;
import cn.huaxingtan.model.Serial;

import cn.huaxingtan.player.R;
import cn.huaxingtan.service.MusicPlayerService;
import cn.huaxingtan.util.ImageDownloader;
import cn.huaxingtan.util.Serialize;
import cn.huaxingtan.util.ImageDownloader.Mode;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DetailActivity extends Activity {
	private static final String TAG = DetailActivity.class.getCanonicalName();
    private static final ImageDownloader mImageDownloader = new ImageDownloader();
    static {
    	mImageDownloader.setMode(Mode.CORRECT);
    }
	private Serial mSerial;
	private View mHeaderView;
	private ListView mListView;
	private List<AudioItem> mData;

	private CachedDataProvider mDataProvider = CachedDataProvider.INSTANCE;
	private DetailAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.activity_detail);
		mData = new ArrayList<AudioItem>();
		Intent intent = getIntent();
		byte[] bytes = (byte[])intent.getExtras().get("Serial");
		try {
			mSerial = (Serial) Serialize.deserialize(bytes);
		} catch (Exception e) {
			Log.e(TAG, "fail to deserialize Serial", e);
		}
		
		LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHeaderView = inflator.inflate(R.layout.detail_header, null);
		ImageView imageView = (ImageView) mHeaderView.findViewById(R.id.detail_header_image);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		mImageDownloader.download(mSerial.getCoverUrl(), imageView);
		
		TextView textView = (TextView) mHeaderView.findViewById(R.id.detail_header_name);
		TextPaint tp = textView.getPaint(); 
//		tp.setFakeBoldText(true);
		textView.setText(mSerial.getName());
		
		textView = (TextView) mHeaderView.findViewById(R.id.detail_header_quantity);
		tp = textView.getPaint(); 
//		tp.setFakeBoldText(true);
		textView.setText("共"+mSerial.getQuantity()+"講");
		
		mListView = (ListView)this.findViewById(R.id.detail);
		mListView.addHeaderView(mHeaderView);
		mAdapter = new DetailAdapter(this, mData);
		mListView.setAdapter(mAdapter);
		
		mDataProvider.getAudiosBySerial(mSerial.getId(), new Callback(){

			@Override
			public void success(Object result) {
				List<AudioItem> list = (List<AudioItem>) result;
				if (list.size() == 0)
					return;
				
				mData.clear();
				for (AudioItem o:list) {
					
					mData.add(o);
				}
				mAdapter.notifyDataSetChanged();
				
			}

			@Override
			public void fail(Exception e) {
			}
			
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		MenuItem settingItem = menu.findItem(R.id.action_settings);
		settingItem.setOnMenuItemClickListener(new OnSettingClickedListener(this));
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
