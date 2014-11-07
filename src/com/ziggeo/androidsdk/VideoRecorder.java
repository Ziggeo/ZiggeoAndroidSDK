package com.ziggeo.androidsdk;

import java.util.*;

import org.json.JSONObject;

import com.ziggeo.androidsdk.helper.*;

import net.majorkernelpanic.streaming.*;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.video.VideoQuality;
import android.app.*;
import android.content.*;
import android.hardware.SensorManager;
import android.hardware.Camera.*;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class VideoRecorder extends Fragment implements OnClickListener,
		RtspClient.Callback, Session.Callback, AdapterView.OnItemClickListener {
	
	private boolean useSnapshots = false;

	/* Application info */
	private String IMAGE_PATH = "";
	private String VIDEO_TOKEN = "";
	private String STREAM_TOKEN = "";
	
	// Callback
	public OnUploadCompletedCallback callback;
	
	// Application context
	private Context ctx;
	// Recorder layout
	private RelativeLayout mVideoRecorderLayout;
	// ImageButtons
	private ImageButton mButtonStart, mButtonFlash, mButtonCamera, mButtonSettings;
	// Buttons
	private Button mButtonRerecord; 
	// Frame Layouts
	private FrameLayout mSurfaceLayout, mRecorderControls;
	// Relative layouts
	private RelativeLayout mGridViewLayout;
	// Text view
	private TextView mTextBitrate, mTextTimer;
	// Main surface
	private SurfaceView mSurfaceView;
	// Grid view and adapter
	private GridView mGridView;
	private GridViewAdapter mGridViewAdapter;
	
	private boolean mSessionStopped, mFrameSelected;
	private boolean isFlashOn = false;
	private boolean videoRecorded = false;
	
	private ProgressBar mProgressBar;
	private FrameLayout mBackgroundFrameLayout;

	private Session mSession;
	private RtspClient mClient;
	
	private Preview mPreview;
	private int mWidth, mHeight;
	
	protected CountDownTimer timer;
	private Timer mPreviewFrameTimer;
	private int recordingDuration;
	
	// Orientation info
	private OrientationEventListener mOrientationEventListener;
	private int currentOrientation;


	// Remember whether a video has already been created
	private boolean isInitialStream = true;
	
	public static VideoRecorder newInstance(int video_duration) {
		VideoRecorder vr = new VideoRecorder();
		Bundle bundle = new Bundle();
		bundle.putInt("duration", video_duration);
		vr.setArguments(bundle);
	    return vr;
	}
	
	public static VideoRecorder newInstance() {
		VideoRecorder vr = new VideoRecorder();
		Bundle bundle = new Bundle();
		bundle.putInt("duration", 0);
		vr.setArguments(bundle);
	    return vr;
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.ctx = activity;
        this.recordingDuration = getArguments().getInt("duration");
		currentOrientation = getCurrentOrientation();
        mOrientationEventListener = new OrientationEventListener(this.ctx, SensorManager.SENSOR_DELAY_NORMAL){
		    @Override
		    public void onOrientationChanged(int arg0) {

		    	// Get current orientation of the screen
		    	int orientation = getCurrentOrientation();
		    	
		    	// Compare it with previous one, and check for 180d difference
		    	if ( Math.abs(orientation - currentOrientation) == 180 ) {
		    		currentOrientation  = orientation; // Save new orientation
		    		mSession.rotateCamera(currentOrientation, 0, 0);
		    	}
		    	
		    }
		};
		if (mOrientationEventListener.canDetectOrientation())
			mOrientationEventListener.enable();
    }
    
    /* Create the interface of the VideoRecorder, using the xml file as a template */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	// Get the recorder xml layout
    	mVideoRecorderLayout = (RelativeLayout)inflater.inflate(R.layout.video_recorder_layout, container, false);
		
    	// Get all buttons using the getIdentifier method
		String pn = this.ctx.getPackageName();
		mButtonStart = (ImageButton)mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_recorder_start", "id", pn));
		mButtonFlash = (ImageButton)mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_recorder_flash", "id", pn));
		mButtonCamera = (ImageButton)mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_recorder_switch_camera", "id", pn));
//		mButtonSettings = (ImageButton)mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_recorder_settings", "id", pn));
		mButtonRerecord = (Button) ((RelativeLayout)mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_recorderer_layout_1", "id", pn))).getChildAt(0);
		
		// Surface view showing recorder camera
		mSurfaceView = (SurfaceView)mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_recorder_surface", "id", pn));
		mSurfaceLayout = (FrameLayout)mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_recorder_surface_layout", "id", pn));

		// Display info
		mTextBitrate = (TextView)mVideoRecorderLayout.findViewById(R.id.bitrate);
		mTextTimer = (TextView)mVideoRecorderLayout.findViewById(R.id.timer);
		mTextTimer.setText(Integer.toString(recordingDuration));
		mGridView = (GridView)mVideoRecorderLayout.findViewById(R.id.grid);
		mGridViewLayout = (RelativeLayout)mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_grid_layout", "id", pn));
		mProgressBar = (ProgressBar)mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_progress_bar", "id", pn));
		mRecorderControls = (FrameLayout)mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_recorder_controls", "id", pn));
		mBackgroundFrameLayout = (FrameLayout) mVideoRecorderLayout.findViewById(getResources().getIdentifier("ziggeo_background_frame_layout", "id", pn));

		// Set buttons listener
		mButtonStart.setOnClickListener(this);
		mButtonFlash.setOnClickListener(this);
		mButtonCamera.setOnClickListener(this);
//		mButtonSettings.setOnClickListener(this);
		mButtonRerecord.setOnClickListener(this);
		mGridView.setOnItemClickListener(this);
		
		// Setup client and session
		setupRecorder();
    	
    	return mVideoRecorderLayout;
    }

    /* Set up the parameters of the recorder w.r.t streaming */
    private void setupRecorder() {
    	    	
		// Set preview resources and callback
		mPreview = new Preview();
    	
	    // Configures the recoder session
		mSession = SessionBuilder.getInstance()
				.setContext(this.ctx.getApplicationContext())
				.setAudioEncoder(SessionBuilder.AUDIO_AAC)
				.setAudioQuality(new AudioQuality(8000,16000))
				.setVideoEncoder(SessionBuilder.VIDEO_H264)
				.setSurfaceView(mSurfaceView)
				.setPreviewCallback(mPreview)
				.setPreviewOrientation(0)
				.setCallback(this)
				.build();

		// Set a callback to be notified when the orientation change
		mPreview.callback = new SurfaceCallback() {
			public void previewSurfaceChanged(int width, int height) {
				currentOrientation = getCurrentOrientation();
				mSession.rotateCamera(currentOrientation, width, height);
			}
			public void previewSurfaceCreated() {
			}
		};
		
		// Configures the RTSP client
		mClient = new RtspClient();
		mClient.setTransportMode(RtspClient.TRANSPORT_TCP);
		mClient.setSession(mSession);
		mClient.setCallback(this);
		
		// Set client parameters
		mClient.setCredentials("", "");
		Uri uri = Uri.parse(Ziggeo.wowzaRecordingUri);
		mClient.setServerAddress(uri.getHost(), uri.getPort());
		
		mPreview.setResources(mSession, mClient); 
		mSurfaceView.getHolder().addCallback(mPreview);
		
		// Aspect ratio of the surface view to match the camera preview
		//mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);

		// Set quality of the surface
		mSession.setVideoQuality(VideoQuality.DEFAULT_VIDEO_QUALITY);
		mWidth = VideoQuality.DEFAULT_VIDEO_QUALITY.resX;
		mHeight = VideoQuality.DEFAULT_VIDEO_QUALITY.resY;
    }
    
    /* Handle recorder button events */
	@Override
	public void onClick(View v) {
		int tag;
		try {
			tag = Integer.parseInt((String)v.getTag());
		} catch (Exception e) { tag = 1; }
		
		System.out.println("Tag:" + tag);
		switch (tag) {
			case 0: disableUI(); toggleStream(); break;
			case 1: toggleFlash(); break;
			case 2: mSession.switchCamera(); break;
			case 3: break;
			case 4: resetStream(); break;
		}
	}
	
		
	/* Handle user selection of a frame after recording the video */
	@Override	
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		postImageExecute(mPreview.getFrames().get(position));
	}
	
	
    private void postImageExecute(ImageItem imageItem) {
		// Post the image selected to a server
		try {
			
			// Post requests for video and preview frame
			final PostRequest videoPostRequest = new PostRequest();		
			PostRequest imagePostRequest = new PostImageRequest(imageItem.getImage());
			IMAGE_PATH = imageItem.getPath();
			// Post the image and the video, depending on the configuration used			
			imagePostRequest.callback = new OnPostRequestCompletedCallback() {
				public void OnPostRequestCompleted(JSONObject jObject) {
					videoPostRequest.execute(Ziggeo.postVideoPath(VIDEO_TOKEN, STREAM_TOKEN) + "?rotation=" + currentOrientation);
				}
			};
			imagePostRequest.execute(Ziggeo.postImagePath(VIDEO_TOKEN, STREAM_TOKEN));
			
			// Set the frame as main background and hide grid view
		    mGridViewLayout.setVisibility(View.GONE);
		    ImageView backgroundFrame = (ImageView) mBackgroundFrameLayout.getChildAt(0);
		    backgroundFrame.setImageBitmap(imageItem.getImage());
			mBackgroundFrameLayout.setVisibility(View.VISIBLE);
		    mFrameSelected = true;
		    		    
		    // Video has been already streamed to the server, start loading the video
		    if (mSessionStopped) {
		    	if (callback != null)
		    		callback.OnUploadCompleted(VIDEO_TOKEN, IMAGE_PATH);
		    	enableUI();
		    }

		    // Video has not finished streaming, show a ProgressBar and wait.
		    // The player will be set up once the session is completed
			else 
				mProgressBar.setVisibility(View.VISIBLE);
		    
		    isInitialStream = true;
			
		} catch(Exception e) {
		}
		
	}
	
    private void postExecute() {
		try {
			// Post requests for video and preview frame
			final PostRequest videoPostRequest = new PostRequest();		
			videoPostRequest.execute(Ziggeo.postVideoPath(VIDEO_TOKEN, STREAM_TOKEN) + "?rotation=" + currentOrientation);
			// Set the frame as main background and hide grid view
		    mGridViewLayout.setVisibility(View.GONE);
		    mFrameSelected = true;
		    		    
		    // Video has been already streamed to the server, start loading the video
		    if (mSessionStopped) {
				if (callback != null)
					callback.OnUploadCompleted(VIDEO_TOKEN, IMAGE_PATH);
		    	enableUI();
		    }

		    // Video has not finished streaming, show a ProgressBar and wait.
		    // The player will be set up once the session is completed
			else 
				mProgressBar.setVisibility(View.VISIBLE);
		    
		    isInitialStream = true;
			
		} catch(Exception e) {
		}
		
	}
    
    /***** Stream start, stop and reset methods ******/
	
	/* Connects/disconnects to the RTSP server and starts/stops the stream */
	public void toggleStream() {
//		mPreview.getPreviewFrame(mWidth,mHeight, maxDuration);
//		mSessionStopped = true;
//		showFrameSelection();
		
		mProgressBar.setVisibility(View.VISIBLE);
		if (!mClient.isStreaming()) {
			CameraHelper.lockOrientation((Activity) this.ctx);
			retrieveTokens();
		}
		else
			stopStream();
	}
	
	/* Retrieve either the stream or the video token */
	private void retrieveTokens() {
		// If this is the first stream, then create a new video via a POST request
		if (isInitialStream) {
			try {
				PostRequest postRequest = new PostRequest();
				// Retrieve tokens, then start recording
				postRequest.callback = new OnPostRequestCompletedCallback() {
					public void OnPostRequestCompleted(JSONObject jObject) {
						try {
							VIDEO_TOKEN = jObject.getJSONObject("video").getString("token");   
							STREAM_TOKEN = jObject.getJSONObject("stream").getString("token");
						} catch (Exception e) {
						}
						startStream();
					}
				};
				postRequest.execute(Ziggeo.postNewVideoPath());
			} catch(Exception e) {
			}	
			isInitialStream = false;
		}
		
		//If not initial stream, then create a new one using same video token
		else {
			try {
				PostRequest postRequest = new PostRequest();
				// Retrieve one token only, then start recording
				postRequest.callback = new OnPostRequestCompletedCallback() {
					public void OnPostRequestCompleted(JSONObject jObject) {
						try {
							STREAM_TOKEN = jObject.getString("token");
						} catch (Exception e) {}
						startStream();
					}
				};
				postRequest.execute(Ziggeo.postNewStreamPath(VIDEO_TOKEN));
			} catch(Exception e) {
			}			
		}
				
	}
	
	private void startStream() {
		mClient.setStreamPath(Ziggeo.recordWowzaPath(VIDEO_TOKEN, STREAM_TOKEN));
		mClient.startStream();
	}
		
	
	private void stopStream() {
		
		// Stop the task responsible for taking frames
		mPreviewFrameTimer.cancel();
		
		// Display the frame selection
		if (useSnapshots)
			showFrameSelection();
		
		// Stops the stream and disconnects from the RTSP server
		mClient.stopStream();
		
		videoRecorded = true;
		if (!useSnapshots)
			postExecute();
				
	}
	
	/* Reset the recorder */
	public void resetStream() {
		
		// Empty frames array
		mPreview.reset(); 
	    
		// Hide frame selection page
	    mGridViewLayout.setVisibility(View.GONE);
	    
	    // Invalidate adapter
	    mGridViewAdapter = null;
	    
	    videoRecorded = false;
	    
	    // Hide spinner and preview
	    mProgressBar.setVisibility(View.GONE);
	    mBackgroundFrameLayout.setVisibility(View.GONE);
	    
	    // Make surface visible again while hiding videoView
	    toggleRecorderUI(View.VISIBLE); // 1: show
	    
	    CameraHelper.unlockScreenOrientation((Activity) this.ctx);
        
	}	
	
	/* UI and quality methods */
	
	/**
	 * Set a quality for a video stream.
	 * @param resX The horizontal resolution
	 * @param resY The vertical resolution
	 * @param framerate The FrameRate. If 0, standard value (20) will be used
	 */
	public void setVideoQuality(int resX, int resY, int framerate) {
		
		// Create standard quality with values received
		VideoQuality quality = null;
		
		// No framerate provided, use standard one
		if (framerate == 0)
			quality = new VideoQuality(resX, resY);
		else {
			int closestFramerate[] = VideoQuality.determineMaximumSupportedFramerate(mSession.getCameraParams());
			if (framerate > closestFramerate[1]/1000) framerate = closestFramerate[1]/1000;
			quality = new VideoQuality(resX, resY, framerate);
		}
		
		// Set whatever quality is closest to the one requested
		mSession.setVideoQuality(VideoQuality.determineClosestSupportedResolution(mSession.getCameraParams().getSupportedPreviewSizes(), quality));
		mSession.configure();
		
		// Save current resolution
		mWidth = resX; mHeight = resY;
	}	
	
	public Size getHighestQuality() { 
		return mSession.getCameraParams().getSupportedPreviewSizes().get(0);
	}
	
	public List<Size> getSupportedQualities() {
		return mSession.getCameraParams().getSupportedPreviewSizes();
	}
	
	public int getHighestFPS() {
		List<int[]> supportedFPS = mSession.getCameraParams().getSupportedPreviewFpsRange();
		return (supportedFPS.get(supportedFPS.size() - 1)[1]) / 1000;
	}


	private void enableUI() {
		mButtonStart.setEnabled(true);
		mButtonCamera.setEnabled(true);
	}
	
	private void disableUI() {
		mButtonStart.setEnabled(false);
		mButtonCamera.setEnabled(false);		
	}
	
	private void toggleRecorderUI(int toggle) {		
		mSurfaceLayout.setVisibility(toggle); // Surface layout
		mTextBitrate.setVisibility(toggle); // Bitrate info
		mTextTimer.setVisibility(toggle); // Timer info
		mRecorderControls.setVisibility(toggle); // Recorder buttons	
	}

	private void showFrameSelection() {
		
		// Use a Gridview to display up to four frames
	    mGridViewLayout.setVisibility(View.VISIBLE);
		mGridViewAdapter = new GridViewAdapter(ctx, R.layout.grid_cell, mPreview.getFrames());
	    mGridView.setAdapter(mGridViewAdapter);
	    mButtonRerecord.setVisibility(View.VISIBLE);
	    
	    // Hide recorder UI
	    toggleRecorderUI(View.GONE); // 0: hide
	    
	}
	
	public void toggleFlash() {
		if (isFlashOn) {
			isFlashOn = false;
			mButtonFlash.setImageResource(R.drawable.ic_flash_on_holo_light);
		} else {
			isFlashOn = true;
			mButtonFlash.setImageResource(R.drawable.ic_flash_off_holo_light);
		}
		mSession.toggleFlash();
	}

	private void setUpTimer() {
		timer = new CountDownTimer(recordingDuration * 1000, 1000) {       
			   public void onTick(long millisLeft) { mTextTimer.setText("" + (int)(millisLeft / 1000)); }       
			   public void onFinish() { mTextTimer.setText("0"); stopStream(); }   
		}.start(); 
	}
		
	/* Session and Client callback */
	
	@Override
	public void onBitrateUpdate(long bitrate) {

		mTextBitrate.setText(""+bitrate/1000+" kbps");
	}
	
	@Override
	public void onPreviewStarted() {
		if (mSession.getCamera() == CameraInfo.CAMERA_FACING_FRONT) {
			mButtonFlash.setEnabled(false);
			mButtonFlash.setTag("off");
			mButtonFlash.setImageResource(R.drawable.ic_flash_on_holo_light);
		}
		else {
			mButtonFlash.setEnabled(true);
		}
		// Set callback for main camera
		currentOrientation = getCurrentOrientation();
		mSession.setPreviewOrientation(currentOrientation);
		// Always pick the highest quality for the preview surface
		Size highestQuality = getHighestQuality();
		int highestFPS = getHighestFPS();
		mWidth = highestQuality.width; mHeight = highestQuality.height;
		mSession.setVideoQuality(new VideoQuality(highestQuality.width, highestQuality.height, highestFPS));
		mSession.configure();
	}	

	private int getCurrentOrientation() {
		if (mSession == null) return 0;
		return CameraHelper.getCameraDisplayOrientation((Activity)this.ctx, mSession.getCurrentCamera(), mSession.getCamera());
	}
	
	@Override
	public void onSessionStarted() {
		enableUI();
		mButtonStart.setImageResource(R.drawable.ic_switch_video_active);
		mProgressBar.setVisibility(View.GONE);
		setUpTimer();
		mSessionStopped = false;
		mFrameSelected = false;
				
		// Set up a repeat function to take screenshots
		mPreviewFrameTimer = new Timer();
		mPreviewFrameTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				try {
					if (useSnapshots)
						mPreview.getPreviewFrame(mWidth, mHeight, recordingDuration);
				} catch (Exception e) {}
			}
		}, 0, 1000);
	}
	
	// Streaming with server has completed, which could imply that all video
	// has been sent to the server. Dismiss the ProgressBar corresponding to
	// the upload of the stream to the Wowza server
	@Override
	public void onSessionStopped() {
		enableUI();
		mButtonStart.setImageResource(R.drawable.ic_switch_video);
		mProgressBar.setVisibility(View.GONE);
		mSessionStopped = true;
	    
		// Session could be completed either before or after the user
		// has already selected the preview frame. In the former case,
		// the player has been already setup the video player in
		// the onItemClick method, hence nothing more need to be done
		// here. In the latter case, a spinner has been shown instead,
		// that we need to stop here and then display the player
		if (mFrameSelected && videoRecorded) {
			mProgressBar.setVisibility(View.GONE);
			if (callback != null)
				callback.OnUploadCompleted(VIDEO_TOKEN, IMAGE_PATH);
			enableUI();
		}
		
	}
	
	@Override
	public void onSessionConfigured() {

	}
	
	@Override
	public void onSessionError(int reason, int streamType, Exception e) {
		mProgressBar.setVisibility(View.GONE);
		switch (reason) {
		case Session.ERROR_CAMERA_ALREADY_IN_USE:
			break;
		case Session.ERROR_CAMERA_HAS_NO_FLASH:
			mButtonFlash.setImageResource(R.drawable.ic_flash_on_holo_light);
			mButtonFlash.setTag("off");
			break;
		case Session.ERROR_INVALID_SURFACE:
			break;
		case Session.ERROR_STORAGE_NOT_READY:
			break;
		case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:
			VideoQuality quality = mSession.getVideoTrack().getVideoQuality();
			logError("The following settings are not supported on this phone: "+
			quality.toString()+" "+
			"("+e.getMessage()+")");
			e.printStackTrace();
			return;
		case Session.ERROR_OTHER:
			break;
		}

		if (e != null) {
			logError(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void onRtspUpdate(int message, Exception e) {
		switch (message) {
		case RtspClient.ERROR_CONNECTION_FAILED:
		case RtspClient.ERROR_WRONG_CREDENTIALS:
			mProgressBar.setVisibility(View.GONE);
			enableUI();
			logError(e.getMessage());
			e.printStackTrace();
			break;
		}
	}
	
	private void logError(final String msg) {
		// Displays a popup to report the error to the user
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage(msg).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/*********** Application Lifecycle ***********/

	// This method is called when the App is partially obscured. 
	// This method is called before onStop.
	@Override
	public void onPause(){
		super.onPause();
		// Check if the user is streaming a video to the server
		if (mClient.isStreaming()) {
			stopStream(); // stop the stream
			resetStream();
		}
		
	}

	// This method is called when the app is destroyed
	@Override
	public void onDestroy(){
		super.onDestroy();
		mClient.release();
		mSession.release();
		mSurfaceView.getHolder().removeCallback(mPreview);
		mPreview.release();
		mSurfaceView = null;
	}
	
}
