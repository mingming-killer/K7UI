package com.eebbk.mingming.k7ui.effect.view;

import com.eebbk.mingming.k7ui.machine.Bullet;
import com.eebbk.mingming.k7ui.machine.State;

public class SAlignFromPrev extends State {
	
	public final static String NAME = "StateAlignFromPrev";
	
	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void onEnter(State from, State to, Bullet bullet, Object obj) {
		EffectPageContainer hostView = (EffectPageContainer) obj;
		
		float factor = hostView.getPageFactor();
		// if current factor is large than half, we change the page, otherwise we align to origin.
		if (Math.abs(factor) > EffectPageContainer.HALF_PAGE_FACTOR) {
			hostView.changeToTargetPageWithAnim(false);
		} else {
			hostView.backToCurrentPageWithAnim(false);
		}
		
		hostView.notifyChangeState();
	}

	@Override
	public void onLeave(State from, State to, Bullet bullet, Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStandby(State in, Bullet bullet, Object obj) {
		// TODO Auto-generated method stub
		
	}
	
}
