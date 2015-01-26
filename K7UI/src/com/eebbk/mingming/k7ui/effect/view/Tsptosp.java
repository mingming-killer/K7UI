package com.eebbk.mingming.k7ui.effect.view;

import com.eebbk.mingming.k7ui.machine.Bullet;
import com.eebbk.mingming.k7ui.machine.ScrollBullet;
import com.eebbk.mingming.k7ui.machine.State;
import com.eebbk.mingming.k7ui.machine.Trigger;

public class Tsptosp extends Trigger {

	public Tsptosp(State from, State to) {
		super(from, to);
	}
	
	@Override
	public boolean fire(Bullet bullet, Object obj) {
		if (!ScrollBullet.NAME.equals(bullet.name())) {
			return false;
		}
		
		return true;
	}

	@Override
	public void onTrigger(State from, State to, Bullet bullet, Object obj) {
		// TODO Auto-generated method stub
		
	}
	
	
}
