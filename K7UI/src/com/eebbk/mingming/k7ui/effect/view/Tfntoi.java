package com.eebbk.mingming.k7ui.effect.view;

import com.eebbk.mingming.k7ui.machine.AnimatorBullet;
import com.eebbk.mingming.k7ui.machine.Bullet;
import com.eebbk.mingming.k7ui.machine.State;
import com.eebbk.mingming.k7ui.machine.Trigger;

public class Tfntoi extends Trigger {
	
	public Tfntoi(State from, State to) {
		super(from, to);
	}

	@Override
	public boolean fire(Bullet bullet, Object obj) {
		if (!AnimatorBullet.NAME.equals(bullet.name())) {
			return false;
		}
		
		EffectPageContainer hostView = (EffectPageContainer) obj;
		@SuppressWarnings("unused")
		AnimatorBullet rBullet = (AnimatorBullet) bullet;
		
		if (hostView.isAnimCancelled()) {
			return false;
		}
		
		return true;
	}

	@Override
	public void onTrigger(State from, State to, Bullet bullet, Object obj) {
		// TODO Auto-generated method stub
		
	}
	
	
}
