package com.ziggeo.androidsdk;

import com.ziggeo.androidsdk.helper.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

public class RerecordVideoPlayer extends VideoPlayer implements OnClickListener {

	// Dismiss callback
	public OnDismissVideoPlayerCallback callback;

	// Ziggeo Rerecord Button
	private Button mButtonRerecord;

	public static RerecordVideoPlayer newInstance(String vt) {
		
		RerecordVideoPlayer vp = new RerecordVideoPlayer();
		Bundle bundle = new Bundle();
		bundle.putString("VIDEO_TOKEN", vt);
		bundle.putString("IMAGE_PATH", null);
		vp.setArguments(bundle);

	    return vp;
	}

	public static RerecordVideoPlayer newInstance(String vt, String ip) {
		
		RerecordVideoPlayer vp = new RerecordVideoPlayer();
		Bundle bundle = new Bundle();
		bundle.putString("VIDEO_TOKEN", vt);
		bundle.putString("IMAGE_PATH", ip);
		vp.setArguments(bundle);

	    return vp;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View mVideoPlayerLayout = super.onCreateView(inflater, container,
				savedInstanceState);

		// Get the re-record button
		mButtonRerecord = (Button) ((RelativeLayout) mVideoPlayerSurfaceLayout
				.findViewById(R.id.ziggeo_recorderer_layout_2)).getChildAt(0);
		;
		mButtonRerecord.setOnClickListener(this);

		return mVideoPlayerLayout;

	}

	protected void videoPlayerCompletion() {
		super.videoPlayerCompletion();
		mButtonRerecord.setVisibility(View.VISIBLE);
	}

	protected void videoPlayerPrepared() {
		super.videoPlayerPrepared();
		mButtonRerecord.setVisibility(View.VISIBLE);
	}

	protected void videoPlayerPlay() {
		super.videoPlayerPlay();
		mButtonRerecord.setVisibility(View.GONE);
	}

	/* Re-record button click - Callback call */

	@Override
	public void onClick(View arg0) {
		callback.OnDismissVideoPlayer();
	}

}
