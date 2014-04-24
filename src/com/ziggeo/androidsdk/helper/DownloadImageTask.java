package com.ziggeo.androidsdk.helper;

import java.net.URL;

import android.graphics.*;
import android.os.AsyncTask;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	
	ImageView iv;

	public DownloadImageTask(ImageView iv) {
		this.iv = iv;
	}

	protected Bitmap doInBackground(String... urls) {
		try {
			return BitmapFactory.decodeStream(new URL(urls[0]).openStream());
		} catch (Exception e) {
			return null;
		}
	}

	protected void onPostExecute(Bitmap result) {
		if (result != null)
			iv.setImageBitmap(result);
	}
	
}