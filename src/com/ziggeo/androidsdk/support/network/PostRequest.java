package com.ziggeo.androidsdk.support.network;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.AsyncTask;

public class PostRequest extends AsyncTask<String, Integer, Long> {

	private JSONObject jObject = null;
	private OnPostRequestCompletedCallback callback;
	private int resilienceCount;
	
	public PostRequest(OnPostRequestCompletedCallback callback, int resilienceCount) {
		this.callback = callback;
		this.resilienceCount = resilienceCount;
	}
	
	private long exec(String url, int resilience) {
		long responseCode = -1;
		if (resilience > 0) {
			try {
				HttpResponse response = new DefaultHttpClient().execute(new HttpPost(url));
				jObject = new JSONObject(EntityUtils.toString(response.getEntity()));
				responseCode = response.getStatusLine().getStatusCode();
			} catch (Exception e) {
			}
			if (responseCode < 200 || responseCode >= 300)
				responseCode = exec(url, resilience - 1);
		}
		return responseCode;
	}

	@Override
	protected Long doInBackground(String... urls) {
		return exec(urls[0], resilienceCount);
	}

	@Override
	protected void onPostExecute(Long result) {
		if (callback != null)
			callback.OnPostRequestCompleted(jObject);
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
	}

}
