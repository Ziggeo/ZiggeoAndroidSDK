package com.ziggeo.androidsdk.helper;

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

	// Complete callback
	public OnPostRequestCompletedCallback callback = null;

	@Override
	protected Long doInBackground(String... urls) {

		long responseCode = -1;

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(urls[0]);
			prepare(httppost);
			HttpResponse response = httpclient.execute(httppost);
			jObject = new JSONObject(EntityUtils.toString(response.getEntity()));
			responseCode = response.getStatusLine().getStatusCode();
		} catch (Exception e) {
		}

		return responseCode;
	}

	protected void prepare(HttpPost httppost)
			throws UnsupportedEncodingException {
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
