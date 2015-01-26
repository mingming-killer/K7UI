package com.eebbk.mingming.k7ui.machine;

import android.view.MotionEvent;

public class TouchDownBullet extends Bullet {
	
	public final static String NAME = "TouchDownBullet";
	
	public MotionEvent mEvent;
	
	public TouchDownBullet() {
		mEvent = null;
	}
	
	@Override
	public String name() {
		return NAME;
	}
	
}
