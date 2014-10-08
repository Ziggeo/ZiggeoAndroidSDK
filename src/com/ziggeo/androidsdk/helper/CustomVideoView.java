package com.ziggeo.androidsdk.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

public class CustomVideoView extends VideoView implements OnClickListener {

    private PlayPauseListener mListener;

    public CustomVideoView(Context context) {
        super(context);
		setStyle();
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
   
    public void setPlayPauseListener(PlayPauseListener listener) {
        mListener = listener;
    }
    
    public PlayPauseListener getPlayPauseListener() {
        return this.mListener;
    }
    
	public void reset() {
		this.mListener = null;
	}
	
	private void setStyle() {
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.setLayoutParams(layoutParams);	
	}


    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onPlay();
        }
    }

    public interface PlayPauseListener {
        void onPlay();
        void onPause();
    }

	@Override
	public void onClick(View v) { start(); }
	
}
