package com.eebbk.mingming.k7ui.machine;

/**
 * 
 * The state is the base unit in {@link StateMachine}.
 * State represent a part of complex model. You should use state to assemble your own model.
 * 
 * @author humingming<hmm@dw.gdbbk.com>
 *
 */
public abstract class State {
	
	public State() {
	}
	
	protected boolean isFromSelf(State from) {
		if (null == from || from.name().equals(name())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Name of state. This should be unique in one machine and never be null.
	 * 
	 * @return String of name.
	 */
	public abstract String name();
	
	/**
	 * Callback of enter this state.
	 * 
	 * @param from Which state it's from.
	 * @param to Which state will go to(usually is self).
	 * @param bullet Which fired enter this state trigger.
	 * @param obj Additional object.
	 */
	public abstract void onEnter(State from, State to, Bullet bullet, Object obj);
	
	/**
	 * Callback of leave this state.
	 * 
	 * @param from Which state it's from(usually is self).
	 * @param to Which state will go to.
	 * @param bullet Which fired leave this state trigger.
	 * @param obj Additional object.
	 */
	public abstract void onLeave(State from, State to, Bullet bullet, Object obj);
	
	/**
	 * Callback of fire a trigger.
	 * 
	 * @param in Which state fire the trigger(usually is self).
	 * @param bullet Which fired standby this state trigger.
	 * @param obj Additional object.
	 */
	public abstract void onStandby(State in, Bullet bullet, Object obj);
	
}
