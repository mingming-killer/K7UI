package com.eebbk.mingming.k7ui.machine;

import android.animation.Animator;

public class AnimatorBullet extends Bullet {
	
	public final static String NAME = "AnimatorBullet";
	
	public Animator mAnimator;
	
	public AnimatorBullet() {
		mAnimator = null;
	}
	
	@Override
	public String name() {
		return NAME;
	}
	
}
