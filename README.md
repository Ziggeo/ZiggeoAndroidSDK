Ziggeo Android SDK (Alpha)
==========================

Ziggeo API (http://ziggeo.com) allows you to integrate video recording and playback with only
two lines of code in your site, service or app. This is the Android SDK repository. It's open source,
so if you want to improve on it, feel free to add a pull request.


## Prerequisites

Add the following permission settings to your app:

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Integrating the Video Player

```
Ziggeo.initialize("YOUR_APPLICATION_TOKEN");

getFragmentManager().
	beginTransaction().
	add(R.id.main_layout,
			VideoPlayer.newInstance("YOUR_VIDEO_TOKEN"),
			"VideoPlayer"
	).commit();
```


## Integrating the Video Recorder

```
Ziggeo.initialize("YOUR_APPLICATION_TOKEN");

getFragmentManager().
	beginTransaction().
	add(R.id.main_layout,
			VideoRecorder.newInstance(RECORDING_DURATION_LIMIT),
			"VideoRecorder"
	).commit();
```


## License
MIT Software License.

Copyright (c) 2014 Ziggeo


## Contributors
- Gianluca Di Maggio 
- Oliver Friedmann 


## Credits
- https://github.com/fyhertz/libstreaming
