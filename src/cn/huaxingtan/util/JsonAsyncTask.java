package cn.huaxingtan.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

import android.util.Log;

public abstract class JsonAsyncTask extends AsyncTask<String, Integer, Object>{
	private static final String TAG = JsonAsyncTask.class.getName();
	@Override
	protected Object doInBackground(String... params) {
		Object mRet = null;
		String mUrl = params[0];
		String line = null;
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(mUrl);
		
		try {
			HttpResponse response = client.execute(get);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				while((line = reader.readLine()) != null) {
					sb.append(line);
				}
			}
			mRet = Json.loads(sb.toString());
		} catch(ClientProtocolException e) {
			Log.e(TAG, "NETWORK", e);
		} catch (IOException e) {
			Log.e(TAG, "NETWORK", e);
		} catch (Exception e) {
			Log.e(TAG, "NETWORK", e);
		} finally {
			try {
				if (reader != null)
				reader.close();
			} catch (IOException e) {
			}
		}
		
		return mRet;
	}
	
}
