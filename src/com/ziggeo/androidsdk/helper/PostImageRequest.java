package com.ziggeo.androidsdk.helper;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.graphics.Bitmap;
import android.util.*;

public class PostImageRequest extends PostRequest {

	private Bitmap image;

	public PostImageRequest(Bitmap image) {
		this.image = image;
	}
	
	protected void prepare(HttpPost httppost) throws UnsupportedEncodingException {
		try {
		// Create output stream and byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream); // Pick format and quality
        byte [] imageBytes = stream.toByteArray();
        
        // Encode image as a string and add it to the requests
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("file", encodedImage));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (Exception e) {
		}
	}
	
}
