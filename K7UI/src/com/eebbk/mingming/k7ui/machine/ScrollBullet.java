package com.eebbk.mingming.k7ui.machine;

import android.view.MotionEvent;

public class ScrollBullet extends Bullet {
	
	public final static String NAME = "ScrollBullet";
	
	public MotionEvent mEvent1;
	public MotionEvent mEvent2;
	public float mDisX;
	public float mDisY;
	
	public ScrollBullet() {
		mEvent1 = null;
		mEvent2 = null;
		mDisX = 0f;
		mDisY = 0f;
	}
	
	@Override
	public String name() {
		return NAME;
	}
	
}
