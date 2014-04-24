package com.ziggeo.androidsdk;

import com.ziggeo.androidsdk.helper.*;

import android.content.*;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;

public class ZiggeoPlayerView extends FrameLayout {
	
	public enum State {
		CREATED,
		ATTACHED,
		PLAYING
	}

    private VideoView vv;
	private ImageView iv;
	private ImageButton ib;
	private State state;

	public ZiggeoPlayerView(Context context) {
		super(context);
		vv = new VideoView(context);
		iv = new ImageView(context);
		ib = new ImageButton(context);
		initialize();
	}

	public ZiggeoPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		vv = new VideoView(context, attrs);
		iv = new ImageView(context, attrs);
		ib = new ImageButton(context);
		initialize(attrs);
	}

	public ZiggeoPlayerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		vv = new VideoView(context, attrs, defStyle);
		iv = new ImageView(context, attrs, defStyle);
		ib = new ImageButton(context);
		initialize(attrs);
	}
	
	private void initialize() {
		state = State.CREATED;
		vv.setVisibility(View.INVISIBLE);
		addView(vv);
		LayoutParams lp = new LayoutParams(96, 96);
	    lp.gravity = Gravity.CENTER; 
		ib.setLayoutParams(lp);
		ib.setScaleType(ImageView.ScaleType.FIT_CENTER);
		ib.setAdjustViewBounds(true);
		addView(iv);
		ib.setImageResource(R.drawable.button_play);
		ib.setBackgroundResource(0);
		addView(ib);
		ib.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	ZiggeoPlayerView player = (ZiggeoPlayerView) v.getParent();
	        	player.play();
	        }
		});
	}
	
	private void initialize(AttributeSet attrs) {
		initialize();
		String token = attrs.getAttributeValue(Ziggeo.scheme, "token");
		if (token != null)
			attach(token);
	}

	public void attach(String token) {
		if (state != State.CREATED)
			return;
		new DownloadImageTask(iv).execute(Ziggeo.getImagePath(token));
    	vv.setVideoURI(Uri.parse(Ziggeo.getVideoPath(token)));
    	state = State.ATTACHED;
	}

	public void play() {
		if (state != State.ATTACHED)
			return;
		state = State.PLAYING;
		iv.setVisibility(View.INVISIBLE);
		ib.setVisibility(View.INVISIBLE);		
		vv.setVisibility(View.VISIBLE);
		vv.requestFocus();
		MediaController mediaController = new MediaController(this.getContext());
		mediaController.setAnchorView(vv);
		vv.setMediaController(mediaController);
		vv.start();
	}

}
