package com.eebbk.mingming.k7ui.machine;

import android.view.MotionEvent;

public class TouchUpBullet extends Bullet {
	
	public final static String NAME = "TouchDownBullet";
	
	public MotionEvent mEvent;
	
	public TouchUpBullet() {
		mEvent = null;
	}
	
	@Override
	public String name() {
		return NAME;
	}
	
}
