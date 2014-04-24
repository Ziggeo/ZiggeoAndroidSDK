package com.ziggeo.androidsdk;

public class Ziggeo {
	
	public static String scheme = "http://schemas.android.com/apk/lib/com.ziggeo.androidsdk";
	
	public static String token = null;
	
	public static String embedServerUri = "embed.ziggeo.com";
	
	public static void initialize(String token) {
		Ziggeo.token = token;
	}
	
	public static String getVideoPath(String video_token) {
		return "https://" + Ziggeo.embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/video.mp4";
	}

	public static String getImagePath(String video_token) {
		return "https://" + Ziggeo.embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/image";
	}

}
