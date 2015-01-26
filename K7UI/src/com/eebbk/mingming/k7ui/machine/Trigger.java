package com.eebbk.mingming.k7ui.machine;

/**
 * 
 * The trigger is a condition transition between states.
 * Your should implement your own trigger to driver the machine running. 
 * 
 * @author humingming<hmm@dw.gdbbk.com>
 *
 */
public abstract class Trigger {
	
	/** Which state this trigger is from. */
	protected State mFrom;
	
	/** Which state will go to. */
	protected State mTo;
	
	public Trigger() {
		this(null, null);
	}
	
	public Trigger(State from, State to) {
		mFrom = from;
		mTo = to;
	}
	
	/**
	 * Test whether this trigger can be fired by give bullet.
	 * 
	 * @param bullet Test bullet.
	 * @param obj Additional object.
	 * @return True: hit, false: miss.
	 */
	public abstract boolean fire(Bullet bullet, Object obj);
	
	/**
	 * Callback of trigger is fired.
	 * 
	 * @param from State which is from.
	 * @param to State which is to.
	 * @param bullet Which fired this trigger.
	 * @param obj Additional object.
	 */
	public abstract void onTrigger(State from, State to, Bullet bullet, Object obj);
	
	/**
	 * Check whether this trigger is standby trigger, 
	 * this is means from state and to state is the same one.
	 * 
	 * @return True is, false not.
	 */
	public boolean isStandbyTrigger() {
		if (null == mFrom) {
			return false;
		}
		return mFrom.equals(mTo);
	}
	
	public void setTarget(State from, State to) {
		mFrom = from;
		mTo = to;
	}
	
	public State getFrom() {
		return mFrom;
	}
	
	public State getTo() {
		return mTo;
	}
	
}
