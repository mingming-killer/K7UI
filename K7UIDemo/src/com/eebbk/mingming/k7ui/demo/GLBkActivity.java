package com.eebbk.mingming.k7ui.demo;

import com.eebbk.mingming.k7ui.GLBkSurfaceView;

import android.os.Bundle;
import android.app.Activity;
import android.view.Window;

public class GLBkActivity extends Activity {

	@SuppressWarnings("unused")
	private final static String TAG = "GLBkActivity";
	
	private GLBkSurfaceView mGLBkView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gl_bk_activity);
		
		mGLBkView = (GLBkSurfaceView) findViewById(R.id.gl_bk_view);
		mGLBkView.setBackground(R.raw.test_bk, 0, 0, 768, 980);
	}
	
}
