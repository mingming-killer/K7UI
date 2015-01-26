package com.eebbk.mingming.k7ui.effect.view;

import com.eebbk.mingming.k7ui.machine.Bullet;
import com.eebbk.mingming.k7ui.machine.ScrollBullet;
import com.eebbk.mingming.k7ui.machine.State;
import com.eebbk.mingming.k7utils.LogUtils;

public class SScrollToNext extends State {
	
	public final static String NAME = "StateScrollToNext";
	
	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void onEnter(State from, State to, Bullet bullet, Object obj) {
		EffectPageContainer hostView = (EffectPageContainer) obj;
		
		// switch from idle we should prepare the switch effect resource.
		if (isFromIdle(from)) {
			hostView.prepareToNextEffectRes();
		}
		
		hostView.notifyChangeState();
	}
	
	@Override
	public void onLeave(State from, State to, Bullet bullet, Object obj) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStandby(State in, Bullet bullet, Object obj) {
		ScrollBullet rBullet = (ScrollBullet) bullet;
		EffectPageContainer hostView = (EffectPageContainer) obj;
		
		//if (mReverseDirect) {
		//	distanceX = -distanceX;
		//	distanceY = -distanceY;
		//}
		
		int width = hostView.getWidth();
		int height = hostView.getHeight();
		float oldFactor = hostView.getPageFactor();
		
		if (width <= 0 || height <= 0) {
			return;
		}
		
		float factor = 0;
		float delta = 0;
		float oldDelta = 0;
		
		if (EffectPageContainer.HORIZONTAL == hostView.getOrientation()) {
			oldDelta = width * oldFactor;
			delta = oldDelta + rBullet.mDisX;
			factor = delta / (float)width;
		} else {
			oldDelta = height * oldFactor;
			delta = oldDelta + rBullet.mDisY;
			factor = delta / (float)height;
		}
		
		// limit over scroll bound.
		if (hostView.isOverScroll()) {
			if (Math.abs(factor) > EffectPageContainer.OVER_SCROLL_FACTOR_BOUND) {
				if (factor > EffectPageContainer.SRC_PAGE_FACTOR) {
					factor = EffectPageContainer.OVER_SCROLL_FACTOR_BOUND;
				} else {
					factor = -EffectPageContainer.OVER_SCROLL_FACTOR_BOUND;
				}
			}
		}
		
		hostView.debugShowCurrentState("onScrollToNext");
		if (EffectPageContainer.DEBUG) LogUtils.d(NAME, "onStandby factor=" + factor);
		
		hostView.showEffect(true);
		hostView.setPageFactor(factor, true);
	}
	
	private boolean isFromIdle(State from) {
		if (null == from) {
			return false;
		}
		
		if (SIdle.NAME.equals(from.name())) {
			return true;
		} else {
			return false;
		}
	}
	
}
