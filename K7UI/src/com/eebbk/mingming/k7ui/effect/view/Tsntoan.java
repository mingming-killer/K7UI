package com.eebbk.mingming.k7ui.effect.view;

import com.eebbk.mingming.k7ui.machine.Bullet;
import com.eebbk.mingming.k7ui.machine.State;
import com.eebbk.mingming.k7ui.machine.TouchUpBullet;
import com.eebbk.mingming.k7ui.machine.Trigger;

public class Tsntoan extends Trigger {
	
	public Tsntoan(State from, State to) {
		super(from, to);
	}

	@Override
	public boolean fire(Bullet bullet, Object obj) {
		if (!TouchUpBullet.NAME.equals(bullet.name())) {
			return false;
		}
		
		return true;
	}

	@Override
	public void onTrigger(State from, State to, Bullet bullet, Object obj) {
		// TODO Auto-generated method stub
		
	}
	
	
}
