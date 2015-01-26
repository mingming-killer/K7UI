package com.eebbk.mingming.k7ui.effect.view;

import com.eebbk.mingming.k7ui.machine.Bullet;
import com.eebbk.mingming.k7ui.machine.FlingBullet;
import com.eebbk.mingming.k7ui.machine.State;
import com.eebbk.mingming.k7ui.machine.Trigger;

public class Titofp extends Trigger {
	
	public Titofp(State from, State to) {
		super(from, to);
	}

	@Override
	public boolean fire(Bullet bullet, Object obj) {
		if (!FlingBullet.NAME.equals(bullet.name())) {
			return false;
		}
		
		EffectPageContainer hostView = (EffectPageContainer) obj;
		FlingBullet rBullet = (FlingBullet) bullet;
		
		// this is over scroll, the fling abandon this bullet.
		int currentPage = hostView.getSelection();
		if (!hostView.isLoopShow() && currentPage <= 0) {
			return false;
		}
		
		if (EffectPageContainer.HORIZONTAL == hostView.getOrientation()) {
			if (rBullet.mVelX > EffectPageContainer.FLING_VELOCITY_THRESHOLD) {
				return true;
			}
		} else {
			if (rBullet.mVelY > EffectPageContainer.FLING_VELOCITY_THRESHOLD) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void onTrigger(State from, State to, Bullet bullet, Object obj) {
		// TODO Auto-generated method stub
		
	}
	
	
}
