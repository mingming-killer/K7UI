package com.eebbk.mingming.k7ui.machine;

import android.view.MotionEvent;

public class FlingBullet extends Bullet {
	
	public final static String NAME = "FlingBullet";
	
	public MotionEvent mEvent1;
	public MotionEvent mEvent2;
	public float mVelX;
	public float mVelY;
	
	public FlingBullet() {
		mEvent1 = null;
		mEvent2 = null;
		mVelX = 0f;
		mVelY = 0f;
	}
	
	@Override
	public String name() {
		return NAME;
	}
	
}
