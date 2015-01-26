package com.eebbk.mingming.k7ui.effect.view;

import com.eebbk.mingming.k7ui.machine.Bullet;
import com.eebbk.mingming.k7ui.machine.ScrollBullet;
import com.eebbk.mingming.k7ui.machine.State;
import com.eebbk.mingming.k7ui.machine.Trigger;

public class Titosn extends Trigger {
	
	public Titosn(State from, State to) {
		super(from, to);
	}
	
	@Override
	public boolean fire(Bullet bullet, Object obj) {
		if (!ScrollBullet.NAME.equals(bullet.name())) {
			return false;
		}
		
		EffectPageContainer hostView = (EffectPageContainer) obj;
		ScrollBullet rBullet = (ScrollBullet) bullet;
		
		if (EffectPageContainer.HORIZONTAL == hostView.getOrientation()) {
			if (rBullet.mDisX > 0f) {
				return true;
			}
		} else {
			if (rBullet.mDisY > 0f) {
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
