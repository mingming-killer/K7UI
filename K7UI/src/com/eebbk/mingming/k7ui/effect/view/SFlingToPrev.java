package com.eebbk.mingming.k7ui.effect.view;

import com.eebbk.mingming.k7ui.machine.Bullet;
import com.eebbk.mingming.k7ui.machine.State;

public class SFlingToPrev extends State {
	
	public final static String NAME = "StateFlingToPrev";
	
	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void onEnter(State from, State to, Bullet bullet, Object obj) {
		EffectPageContainer hostView = (EffectPageContainer) obj;
		
		hostView.cancelSwitchAnim();
		
		// switch from idle we should prepare the switch effect resource.
		if (isFromIdle(from)) {
			hostView.prepareToPrevEffectRes();
		}
		
		hostView.showEffect(true);
		hostView.changeToTargetPageWithAnim(false);
		
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
