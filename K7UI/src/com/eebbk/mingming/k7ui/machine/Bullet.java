package com.eebbk.mingming.k7ui.machine;

/**
 * 
 * The bullet is use for test trigger fired.
 * Your can use subclass of it for test your trigger. 
 * 
 * @author humingming<hmm@dw.gdbbk.com>
 *
 */
public abstract class Bullet {
	
	public Bullet() {
	}
	
	/**
	 * Name of the bullet, this should be unique between other Bullet.
	 * Subclass should override it.
	 * 
	 * @return
	 */
	public abstract String name();
	
}
