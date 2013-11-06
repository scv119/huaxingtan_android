package cn.huaxingtan.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import cn.huaxingtan.model.AudioItem;
import android.os.AsyncTask;
import android.util.Log;

@Deprecated
public class FileDownloadAsyncTask extends AsyncTask<AudioItem, AudioItem, AudioItem>{
	private static final String LOG = FileDownloadAsyncTask.class.getName();
	private static final int BUFFER_SIZE = 10240;
	
	@Override
	protected AudioItem doInBackground(AudioItem... params) {
		AudioItem mItem = params[0];
		mItem.setStatus(AudioItem.Status.STARTED);
		this.publishProgress(mItem);
		assert(mItem.getPath() != null && mItem.getFileUrl()!=null);
		File mFile = new File(mItem.getPath());
		long rangeOffset = 0;
		boolean success = false;
		if (mFile.exists())
			rangeOffset = mFile.length();
		String mUrl = mItem.getFileUrl();
		int bytesRead = 0;
		BufferedInputStream reader = null;
		BufferedOutputStream out = null;  
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(mUrl);
			get.setHeader("RANGE", "bytes=" + rangeOffset + "-");
			byte[] buffer = new byte[BUFFER_SIZE];
			HttpResponse response = client.execute(get);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				reader = new BufferedInputStream(response.getEntity().getContent());
				out = new BufferedOutputStream(new FileOutputStream(mFile, true));
				while ((bytesRead = reader.read(buffer)) >= 0) {
					out.write(buffer, 0, bytesRead);
					out.flush();
					rangeOffset += bytesRead;
					mItem.setFinishedSize(rangeOffset);
					mItem.setStatus(AudioItem.Status.STARTED);
					this.publishProgress(mItem);
					if (this.isCancelled())
						break;
				}
				out.close();
				if (!this.isCancelled())
					success = true;
			}
		} catch(ClientProtocolException e) {
			Log.e(LOG, "FILEDOWNLOAD", e);
		} catch (IOException e) {
			Log.e(LOG, "FILEDOWNLOAD", e);
		} catch (Exception e) {
			Log.e(LOG, "FILEDOWNLOAD", e);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {}
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {}
		}
		
		if (success)
			mItem.setStatus(AudioItem.Status.FINISHED);
		else
			mItem.setStatus(AudioItem.Status.STOPED);
		
		this.publishProgress(mItem);
		return mItem;
	}

}
