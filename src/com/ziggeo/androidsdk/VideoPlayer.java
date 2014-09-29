package com.ziggeo.androidsdk;

import com.ziggeo.androidsdk.helper.*;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class VideoPlayer extends Fragment {
	
	// Surface showing the video
	private CustomVideoView mVideoView;
	// Media controller for video controls
	private MediaController mMediaController;
	// Video player layout
	private FrameLayout mVideoPlayerLayout;
	// Surface layout
	protected RelativeLayout mVideoPlayerSurfaceLayout;
	// Frame preview layout
	private FrameLayout mBackgroundFrameLayout;
	
	// Ziggeo play button
	private ImageButton mButtonZiggeoPlay;
	// Progress bar
	private ProgressBar mProgressBar;
	
	// Application context
	private Context ctx;
	
	/* Application info */
	private String VIDEO_TOKEN = "";
	private String IMAGE_PATH = "";
	
	public static VideoPlayer newInstance(String vt) {
		
		VideoPlayer vp = new VideoPlayer();
		Bundle bundle = new Bundle();
		bundle.putString("VIDEO_TOKEN", vt);
		bundle.putString("IMAGE_PATH", null);
		vp.setArguments(bundle);

	    return vp;
	}

	public static VideoPlayer newInstance(String vt, String ip) {
		
		VideoPlayer vp = new VideoPlayer();
		Bundle bundle = new Bundle();
		bundle.putString("VIDEO_TOKEN", vt);
		bundle.putString("IMAGE_PATH", ip);
		vp.setArguments(bundle);

	    return vp;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.ctx = activity;
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    	// Get the two layouts
		mVideoPlayerLayout = (FrameLayout)inflater.inflate(R.layout.video_player_layout, container, false);
		mVideoPlayerSurfaceLayout = (RelativeLayout)mVideoPlayerLayout.findViewById(R.id.ziggeo_video_player);
		
		// Get the play button
		mButtonZiggeoPlay = (ImageButton) mVideoPlayerSurfaceLayout.findViewById(R.id.ziggeo_player_play);
		
		// Get progress bar
		mProgressBar = (ProgressBar)mVideoPlayerLayout.findViewById(R.id.ziggeo_progress_bar);

		// Get the background showing the preview frame
		mBackgroundFrameLayout = (FrameLayout) mVideoPlayerSurfaceLayout.findViewById(R.id.ziggeo_background_frame_layout);
		
        return mVideoPlayerLayout;
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);

    	this.VIDEO_TOKEN = this.getArguments().getString("VIDEO_TOKEN");  	
    	this.IMAGE_PATH = this.getArguments().getString("IMAGE_PATH");  	
    	initializeVideoPlayer();

    }
    
	public void initializeVideoPlayer() {
		
		// Make a new surface for video and insert it in the layout
		mVideoView =  new CustomVideoView(ctx);
		mMediaController = new MediaController(ctx);
		mVideoPlayerSurfaceLayout.addView(mVideoView, 0);
		togglePlayerUI(View.VISIBLE);

		// Set up button clicks
		mButtonZiggeoPlay.setOnClickListener(mVideoView);
		
		// Show progess bar
		mProgressBar.setVisibility(View.VISIBLE);
		
		// Set preview frame
		mBackgroundFrameLayout.setVisibility(View.VISIBLE);
	    ImageView backgroundFrame = (ImageView) mBackgroundFrameLayout.getChildAt(0);
	    
	    // If the image path is provided, load the image from there
	    if (IMAGE_PATH != null) {
	    	Bitmap bmImg = BitmapFactory.decodeFile(IMAGE_PATH);
	    	backgroundFrame.setImageBitmap(bmImg);
	    } 
	    // Otherwise retrieve image from server
	    else {
	    	new DownloadImageTask(backgroundFrame).execute(Ziggeo.getImagePath(VIDEO_TOKEN));
	    }
	    		
		// Set up listeners
		setupVideoPlayerListeners();
				
		Uri uri = Uri.parse(Ziggeo.getVideoPath(VIDEO_TOKEN));			
	   
	    // Begin video processing
	    mVideoView.setVideoURI(uri);
	    mVideoView.setZOrderMediaOverlay(true);
	    				
	}
	
	private void togglePlayerUI(int toggle) {
		mVideoPlayerLayout.setVisibility(toggle);
	    mVideoPlayerSurfaceLayout.setVisibility(toggle);	
	}
	
	/* Video Listeners */
	
	protected void videoPlayerCompletion() {
	  mButtonZiggeoPlay.setVisibility(View.VISIBLE);
	  mBackgroundFrameLayout.setVisibility(View.VISIBLE);
	  mVideoView.setMediaController(null);
	}
	
	protected void videoPlayerPrepared() {
	  // Hide the spinner and show the play button
	  mProgressBar.setVisibility(View.GONE);
	  mButtonZiggeoPlay.setVisibility(View.VISIBLE);	
	}
	
	protected void videoPlayerPlay() {
    	// Hide the preview frame if showing, as well as ziggeo play button
        if (mBackgroundFrameLayout.getVisibility() == View.VISIBLE) {
        	mVideoView.setVisibility(View.VISIBLE);
        	mBackgroundFrameLayout.setVisibility(View.GONE);
        	mButtonZiggeoPlay.setVisibility(View.GONE);
    		mVideoView.setMediaController(mMediaController);
    		mMediaController.show();
        }
	}
    
    private void setupVideoPlayerListeners() {
    
	    // Set listeners to control when video is ready or completed
	    mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
			  @Override
			  public void onCompletion(MediaPlayer mp) {
				  mp.seekTo(0);
				  videoPlayerCompletion();
			  }
		  
		});

	    mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
		  @Override
		  public void onPrepared(MediaPlayer mp) {
			  videoPlayerPrepared();
		  }
	    });
	    		    
		// Add callbacks from media events
		mVideoView.setPlayPauseListener(new CustomVideoView.PlayPauseListener() {
		    
			@Override
		    public void onPlay() {
				videoPlayerPlay();
		    }

		    @Override
		    public void onPause() {}
		    
		});
    	
    }
    
	/* Fragment Lifecycle */

	@Override
	public void onPause(){
		super.onPause();
		// Check if the user is playing a video, in which case remember
		// the position of the stream
		if (mVideoView != null && mVideoView.isPlaying()) {
			mVideoView.pause();	
			mProgressBar.setVisibility(View.VISIBLE);
			mBackgroundFrameLayout.setVisibility(View.VISIBLE);
		}
	}

}
