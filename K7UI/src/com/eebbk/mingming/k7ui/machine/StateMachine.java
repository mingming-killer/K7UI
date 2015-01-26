package com.eebbk.mingming.k7ui.machine;

import java.util.ArrayList;

import com.eebbk.mingming.k7utils.LogUtils;

/**
 * 
 * The state machine is the container of state, driver by trigger.
 * This is response your logic model.
 * 
 * @author humingming<hmm@dw.gdbbk.com>
 *
 */
public class StateMachine {
	
	private final static String TAG = "StateMachine";
	
	// list of all states.
	private ArrayList<State> mStates;
	
	// list of all triggers.
	private ArrayList<Trigger> mTriggers;
	
	private State mCurrentState;
	
	public StateMachine() {
		mCurrentState = null;
		mStates = new ArrayList<State> ();
		mTriggers = new ArrayList<Trigger> ();
	}
	
	/**
	 * Add a state to machine.
	 * You should not add a repeat state 
	 * 
	 * @param state Object of {@link State}
	 */
	public void addState(State state) {
		if (null == state || null == state.name()) {
			LogUtils.d(TAG, "the state is null or name is null, we can't add it to machine !");
			return;
		}
		if (checkStateExist(state.name())) {
			throw new IllegalArgumentException(TAG + ": the state is already in machine !");
		}
		
		mStates.add(state);
	}
	
	/**
	 * Remove a state from machine.
	 * 
	 * @param name Name of exist state.
	 */
	public void removeState(String name) {
		State state = getStateByName(name);
		if (null != state) {
			mStates.remove(state);
		}
	}
	
	/**
	 * Add a trigger to machine.
	 * 
	 * @param trigger Object of {@link Trigger}.
	 */
	public void addTrigger(Trigger trigger) {
		if (null == trigger) {
			LogUtils.d(TAG, "the trigger is null, we can't add it to machine !");
			return;
		}
		
		mTriggers.add(trigger);
	}
	
	/**
	 * Remove a trigger from machine.
	 * 
	 * @param trigger Remove trigger object.
	 */
	public void removeTrigger(Trigger trigger) {
		if (null == trigger) {
			return;
		}
		mTriggers.remove(trigger);
	}
	
	/**
	 * Start run machine. Every machine should run begin with the 
	 * begin state.
	 * 
	 * @param beginStateName Name of begin state(you should already add it in).
	 */
	public void startRun(String beginStateName) {
		State state = getStateByName(beginStateName);
		if (null == state) {
			throw new RuntimeException(TAG + ": the begin state is not in machine !");
		}
		
		mCurrentState = state;
	}
	
	/**
	 * Test machine trigger, to look for which trigger can fired 
	 * by the give bullet.
	 * 
	 * @param bullet Test bullet.
	 * @param Additional object.
	 * @return True: a trigger has fired by bullet, false: no one trigger fired.
	 */
	public boolean testTrigger(Bullet bullet, Object obj) {
		if (null == bullet) {
			LogUtils.d(TAG, "bullet is null, test trigger failed !");
			return false;
		}
		
		State from = null;
		State to = null;
		for (Trigger trigger : mTriggers) {			
			from = trigger.getFrom();
			to = trigger.getTo();
			// if the trigger don't have from state, we think is invalid.
			if (null == from) {
				continue;
			}
			
			// we test current state trigger whether fired.
			if (from.equals(mCurrentState) && trigger.fire(bullet, obj)) {
				// change the current state first, maybe above callback will use it.
				mCurrentState = to;
				// first call trigger callback
				trigger.onTrigger(from, to, bullet, obj);
				// and then state callback.
				if (trigger.isStandbyTrigger()) {
					from.onStandby(from, bullet, obj);
				} else {
					from.onLeave(from, to, bullet, obj);
					to.onEnter(from, to, bullet, obj);
				}
				// we only fired trigger once every time.
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Release machine resource, after this call, you should not use 
	 * the machine again.
	 */
	public void free() {
		if (null != mStates) {
			mStates.clear();
		}
		if (null != mTriggers) {
			mTriggers.clear();
		}
	}
	
	public State getCurrentState() {
		return mCurrentState;
	}
	
	public String getCurrentStateName() {
		if (null == mCurrentState) {
			return null;
		} else {
			return mCurrentState.name();
		}
	}
	
	private boolean checkStateExist(String name) {
		return (null != getStateByName(name));
	}
	
	private State getStateByName(String name) {
		for (State state : mStates) {
			if (state.name().equals(name)) {
				return state;
			}
		}
		return null;
	}
	
	public String debugDump() {
		String dump = "==========================\n";
		
		dump += ("All State count=" + mStates.size() + "\n");
		dump += ("current=" + mCurrentState + "\n");
		
		int count = 0;
		for (State state : mStates) {
			dump += "No." + count + ", " + state.toString() + "\n";
			count += 1;
		}
		
		dump += "\n\n";
		dump += ("All Tirgger count=" + mTriggers.size() + "\n");
		count = 0;
		for (Trigger trigger : mTriggers) {
			dump += "No." + count + ", " + trigger.toString() + "\n";
			count += 1;
		}
		
		dump += "==========================\n";
		return dump;
	}
	
}
