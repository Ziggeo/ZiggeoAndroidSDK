package com.ziggeo.androidsdk.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import net.majorkernelpanic.streaming.Session;
import android.os.Environment;
import android.view.SurfaceHolder;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

public class Preview implements SurfaceHolder.Callback, Camera.PreviewCallback {

	public SurfaceCallback callback;
	private Session mSession;
	private RtspClient mClient;
	private byte[] lastFrameData;
	private ArrayList<ImageItem> frames = new ArrayList<ImageItem>();
	private int framesTaken = 0;
	
	public Preview() { }
	
	public void setResources(Session mSession, RtspClient mClient) {	
		this.mSession = mSession;
		this.mClient = mClient;
	}
	
	public void release() {
		reset();
		this.mSession = null;
		this.mClient = null;		
	}
	
	/* Surface callbacks */
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	    if (holder.getSurface() == null) return;
		callback.previewSurfaceChanged(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mSession.startPreview();
		callback.previewSurfaceCreated();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mClient.stopStream();
	}

	/* Camera callbacks */
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		this.lastFrameData = data;
	}
	
	
	// Manipulate the frame data to obtain a preview image
	public void getPreviewFrame(int w, int h, int maxDuration) {
		
		if (lastFrameData == null) return;

		// Convert to JPG
		YuvImage yuvimage = new YuvImage(lastFrameData, ImageFormat.NV21, w, h, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(0, 0, w, h), 100, baos);
		byte[] jdata = baos.toByteArray();
		
		// Convert to Bitmap
		Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
		
		// Rotate the Bitmap via a matrix
	    Matrix matrix = new Matrix();
	    matrix.postRotate(90);
	    bmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, false);
	    
	    // Save the bitmap on storage as JPEG
	    String path = Environment.getExternalStorageDirectory() + "/ZiggeoPictures" + frames.size() + ".jpg";
	    File file = new File(Environment.getExternalStorageDirectory() + "/ZiggeoPictures" + frames.size() + ".jpg");
	    try {
            FileOutputStream os = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
	    } catch(Exception e) { }
	    
		
		// Save the bitmap into an array, with no more than 4 frames held in total
		// If one of the four positions is empty, append the frame at the end
		// If all are occupied, keep a frame for every quarter of the total (expected)
		// video duration. Since a frame is taken each second, the amount of frames
		// corresponds to the second passed so far
		if (frames.size() == 4) {
			// Get in which quarter of the video we are
			int quarter = (int) Math.floor((double) framesTaken / (maxDuration/4));
			// Safety check
			if (quarter > 3) quarter = 3;
			// Remove a frame from the index corresponding to the quarter
			deleteFrameAtPath(frames.get(quarter).getPath());
			frames.remove(quarter);			
		}
		
		// Add the new frame at the end
		frames.add(new ImageItem(bmp, path));
		
		framesTaken++;
		
	}

	public ArrayList<ImageItem> getFrames() { return this.frames; }
	
	public void reset() { 
		
		Iterator<ImageItem> iterator = frames.iterator();
		
		while (iterator.hasNext()) {

		    ImageItem imageItem = iterator.next();
			deleteFrameAtPath(imageItem.getPath());
			iterator.remove();
		}
		
		framesTaken = 0;
	}
	
	private void deleteFrameAtPath(String path) {
		try {
			File file = new File(path);  
			OutputStream out = new FileOutputStream(file, false);
			out.close();
			file.delete();
		} catch (Exception e) {
			
		}		
	}
	
}
