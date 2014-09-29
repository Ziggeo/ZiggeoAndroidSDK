package com.ziggeo.androidsdk;

import android.net.Uri;

public class Ziggeo {
	
	public static String token = null;
	
	public static String embedServerUri = "https://embed.ziggeo.com";
	
	public static String wowzaRecordingUri = "rtsp://wowza.ziggeo.com:1935/record/_definst_";
	
	public static void initialize(String token) {
		Ziggeo.token = token;
	}
	
	public static String getVideoPath(String video_token) {
		return Ziggeo.embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/video.mp4";
	}

	public static String getImagePath(String video_token) {
		return Ziggeo.embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/image";
	}

	public static String postImagePath(String video_token, String stream_token) {
		return Ziggeo.embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/streams/" + stream_token + "/image";
	}

	public static String postVideoPath(String video_token, String stream_token) {
		return Ziggeo.embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/streams/" + stream_token + "/recordersubmit";
	}
	
	public static String postNewVideoPath() {
		return Ziggeo.embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos?flash_recording=true";
	}

	public static String postNewStreamPath(String video_token) {
		return Ziggeo.embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/streams?flash_recording=true";
	}
	
	public static String recordWowzaPath(String video_token, String stream_token) {
		return Uri.parse(Ziggeo.wowzaRecordingUri).getPath() + "/applications/" + Ziggeo.token + "/videos/" + video_token + "/streams/" + stream_token + "/video.mp4";
	}

}
