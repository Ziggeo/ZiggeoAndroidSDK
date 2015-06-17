package com.ziggeo.androidsdk.services;

import android.net.Uri;

import com.ziggeo.androidsdk.Ziggeo;

public class UrlService {

	public static String embedServerUri = "https://embed.ziggeo.com";
	public static String embedcdnServerUri = "https://embed-cdn.ziggeo.com";
	
	public static String wowzaRecordingUri = "rtsp://wowza.ziggeo.com:1935/record/_definst_";
	
	public static String getVideoPath(String video_token) {
		return embedcdnServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/video.mp4";
	}

	public static String getImagePath(String video_token) {
		return embedcdnServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/image";
	}

	public static String postVideoPath(String video_token, String stream_token) {
		return embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/streams/" + stream_token + "/recordersubmit";
	}
	
	public static String postNewVideoPath() {
		return embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos?flash_recording=true";
	}

	public static String postNewStreamPath(String video_token) {
		return embedServerUri + "/v1/applications/" + Ziggeo.token + "/videos/" + video_token + "/streams?flash_recording=true";
	}
	
	public static String recordWowzaPath(String video_token, String stream_token) {
		return Uri.parse(wowzaRecordingUri).getPath() + "/applications/" + Ziggeo.token + "/videos/" + video_token + "/streams/" + stream_token + "/video.mp4";
	}

}
