package com.ziggeo.androidsdk.helper;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

public class PostImageRequest extends PostRequest {

	private Bitmap image;

	public PostImageRequest(Bitmap image) {
		this.image = image;
	}
	
	protected void prepare(HttpPost httppost) throws UnsupportedEncodingException {
		try {
		Log.d("ZIGGEO", "preparing");
		// Create output stream and byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Log.d("ZIGGEO", "preparing 1");
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream); // Pick format and quality
        Log.d("ZIGGEO", "preparing 2");
        byte [] imageBytes = stream.toByteArray();
        Log.d("ZIGGEO", "preparing 3");
        
        // Encode image as a string and add it to the requests
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        Log.d("ZIGGEO", "preparing 4");
        ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
        Log.d("ZIGGEO", "preparing 5");
        nameValuePairs.add(new BasicNameValuePair("file", encodedImage));
        Log.d("ZIGGEO", "preparing 6");
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		Log.d("ZIGGEO", "done preparing");
		} catch (Exception e) {
			Log.d("ZIGGEO", e.getMessage());
		}
	}
	
}
