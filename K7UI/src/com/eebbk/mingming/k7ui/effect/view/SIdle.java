package com.eebbk.mingming.k7ui.effect.view;

import com.eebbk.mingming.k7ui.machine.Bullet;
import com.eebbk.mingming.k7ui.machine.State;

public class SIdle extends State {
	
	public final static String NAME = "StateIdle";
	
	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public void onEnter(State from, State to, Bullet bullet, Object obj) {
		EffectPageContainer hostView = (EffectPageContainer) obj;
		
		float factor = hostView.getPageFactor();
		if (isFromScrollToNext(from)) {
			if (factor < EffectPageContainer.SRC_PAGE_FACTOR) {
				hostView.backtoCurrentPage();
			} else {
				hostView.changeToTargetPage();
			}
		} else if (isFromScrollToPrev(from)) {
			if (factor > EffectPageContainer.SRC_PAGE_FACTOR) {
				hostView.backtoCurrentPage();
			} else {
				hostView.changeToTargetPage();
			}
		} else {
			// we enter idle state, there is means the effect is end.
			hostView.endPageEffect();
		}
		
		hostView.notifyChangeState();
		hostView.notifyChangePage();
	}
	
	@Override
	public void onLeave(State from, State to, Bullet bullet, Object obj) {
		//EffectPageContainer hostView = (EffectPageContainer) obj;
		// we leave idle state, there is means the effect is begin show.
		// add view to adapter view maybe will change view visible, so is must set 
		// view visible before show effect. -_-||
		//hostView.showEffect(true);
	}
	
	@Override
	public void onStandby(State in, Bullet bullet, Object obj) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean isFromScrollToPrev(State from) {
		if (null == from) {
			return false;
		}
		
		if (SScrollToPrev.NAME.equals(from.name())) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isFromScrollToNext(State from) {
		if (null == from) {
			return false;
		}
		
		if (SScrollToNext.NAME.equals(from.name())) {
			return true;
		} else {
			return false;
		}
	}
	
}
