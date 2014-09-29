package com.ziggeo.androidsdk.helper;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.Surface;

public class CameraHelper {

	public static int getCameraDisplayOrientation(Activity activity, Camera camera, int mCameraId) {

		 if (camera == null) return 0;
		 android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		 android.hardware.Camera.getCameraInfo(mCameraId, info);
		 
		 int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		 
		 int degrees = 0;
		 switch (rotation) {
		     case Surface.ROTATION_0: degrees = 0; break;
		     case Surface.ROTATION_90: degrees = 90; break;
		     case Surface.ROTATION_180: degrees = 180; break; 
		     case Surface.ROTATION_270: degrees = 270; break; // 270
		 }
		 int result;
		 if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
		     result = (info.orientation + degrees) % 360;
		     result = (360 - result) % 360;  // compensate the mirror
		 } else {  // back-facing
		     result = (info.orientation - degrees + 360) % 360;
		 }
		 return result;
	 }	
	
	 public static boolean isOrientationPortrait(Activity activity) {
		   
		    int orientation = activity.getResources().getConfiguration().orientation;

		    switch(orientation) {
		    	case Configuration.ORIENTATION_LANDSCAPE: return false;
		    	case Configuration.ORIENTATION_PORTRAIT: return true;
		    }
		    return true;
	 }
	 
	 
	 public static void lockOrientation(Activity activity) {
		 
		 switch (activity.getResources().getConfiguration().orientation){
	        case Configuration.ORIENTATION_PORTRAIT:
	            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO){
	            	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	            } else {
	                int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	            if(rotation == android.view.Surface.ROTATION_90|| rotation == android.view.Surface.ROTATION_180){
	            	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
	                } else {
	                	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	                }
	            }   
	        break;

	        case Configuration.ORIENTATION_LANDSCAPE:
	            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO){
	                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	            } else {
	                int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	                if(rotation == android.view.Surface.ROTATION_0 || rotation == android.view.Surface.ROTATION_90){
	                	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	                } else {
	                	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
	                }
	            }
	        break;
	    }

	 }
	 
	 public static void unlockScreenOrientation(Activity activity) {
		 activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	 }

}
