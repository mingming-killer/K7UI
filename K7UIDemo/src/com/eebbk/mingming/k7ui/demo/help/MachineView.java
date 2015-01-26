package com.eebbk.mingming.k7ui.demo.help;

import com.eebbk.mingming.k7ui.accessibility.ScrollDetector;
import com.eebbk.mingming.k7ui.accessibility.ScrollDetector.ScrollDetectorListener;
import com.eebbk.mingming.k7ui.demo.R;
import com.eebbk.mingming.k7ui.effector.Effector;
import com.eebbk.mingming.k7ui.effector.IEffectView;
import com.eebbk.mingming.k7ui.machine.AnimatorBullet;
import com.eebbk.mingming.k7ui.machine.FlingBullet;
import com.eebbk.mingming.k7ui.machine.ScrollBullet;
import com.eebbk.mingming.k7ui.machine.TouchDownBullet;
import com.eebbk.mingming.k7ui.machine.TouchUpBullet;
import com.eebbk.mingming.k7ui.machine.State;
import com.eebbk.mingming.k7ui.machine.StateMachine;
import com.eebbk.mingming.k7ui.machine.Trigger;
import com.eebbk.mingming.k7ui.machine.Bullet;
import com.eebbk.mingming.k7utils.LogUtils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MachineView extends FrameLayout implements ScrollDetectorListener, 
	AnimatorListener, AnimatorUpdateListener {
	
	private final static String TAG = "MachineView";
	
	private final static String PROPERTY_NAME = "Factor";
	
	private final static float PREV_PAGE_FACTOR = -1.0f;
	private final static float SRC_PAGE_FACTOR = 0.0f;
	private final static float NEXT_PAGE_FACTOR = 1.0f;
	private final static float HALF_PAGE_FACTOR = 0.5f;
	private final static float DST_PAGE_FACTOR = 1.0f;
	
	private StateMachine mStateMachine;
	private TouchDownBullet mTouchDownBullet;
	private TouchUpBullet mTouchUpBullet;
	private ScrollBullet mScrollBullet;
	private FlingBullet mFlingBullet;
	private AnimatorBullet mAnimatorBullet;
	
	private String mStateTrack = null;
	
	private float mFactor = 0f;
	private boolean mAnimCancelled = false;
	private ObjectAnimator mAnimator;
	
	private TextView mTvInfo = null;
	private ScrollDetector mScrollDetector = null;
	
	public MachineView(Context context) {
		super(context);
		init(context);
	}

	public MachineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public MachineView(Context context, AttributeSet attrs, int style) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		mScrollDetector = new ScrollDetector(context, this, ScrollDetector.HORIZONTAL, this);
		
		mAnimator = ObjectAnimator.ofFloat(null, 
				PROPERTY_NAME, 
				0f, 1f);
		mAnimator.addListener(this);
		mAnimator.addUpdateListener(this);
		
		initStateMachine();
	}
	
	private void initStateMachine() {
		mStateMachine = new StateMachine();
		
		mTouchDownBullet = new TouchDownBullet();
		mTouchUpBullet = new TouchUpBullet();
		mScrollBullet = new ScrollBullet();
		mFlingBullet = new FlingBullet();
		mAnimatorBullet = new AnimatorBullet();
		
		SIdle sIdle = new SIdle();
		SScrollToPrev sScrollToPrev = new SScrollToPrev();
		SAlignFromPrev sAlignFromPrev = new SAlignFromPrev();
		//SFlingToPrev sAlignFromPrev = new SAlignFromPrev();
		
		Tidtosp tidtosp = new Tidtosp(sIdle, sScrollToPrev);
		Tsptosp tsptosp = new Tsptosp(sScrollToPrev, sScrollToPrev);
		Tsptoap tsptoap = new Tsptoap(sScrollToPrev, sAlignFromPrev);
		Taptoid taptoid = new Taptoid(sAlignFromPrev, sIdle);
		
		mStateMachine.addState(sIdle);
		mStateMachine.addState(sScrollToPrev);
		mStateMachine.addState(sAlignFromPrev);

		mStateMachine.addTrigger(tidtosp);
		mStateMachine.addTrigger(tsptosp);
		mStateMachine.addTrigger(tsptoap);
		mStateMachine.addTrigger(taptoid);
		
		mStateMachine.startRun(sIdle.name());
		LogUtils.d(TAG, "state machine: " + mStateMachine.debugDump());
	}
	
	@Override
	protected void onFinishInflate() {
		mTvInfo = (TextView) findViewById(R.id.tv_info);
	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	mScrollDetector.onMeasure(getMeasuredWidth(), getMeasuredHeight());
    }
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean handled = mScrollDetector.onInterceptTouchEvent(ev);
		if (!handled) {
			onUp(ev);
		}
		return handled;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mScrollDetector.onTouchEvent(ev)) {
			return true;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean onDetectScrolling() {
		LogUtils.d(TAG, "onDetectScrolling: ");
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		LogUtils.d(TAG, "onDown: " + e.toString());
		
		//mTouchDownBullet.mEvent = e;
		//mStateMachine.testTrigger(mTouchDownBullet, this);
		
		return true;
	}

	@Override
	public boolean onUp(MotionEvent e) {
		LogUtils.d(TAG, "onUp: " + e.toString());
		mTouchUpBullet.mEvent = e;
		boolean fired = mStateMachine.testTrigger(mTouchUpBullet, this);
		LogUtils.d(TAG, "Touch up bullet: currentState=" + mStateMachine.getCurrentStateName());
		return fired;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		LogUtils.d(TAG, "onScroll disX=" + distanceX + ", disY=" + distanceY);
		mScrollBullet.mEvent1 = e1;
		mScrollBullet.mEvent2 = e2;
		mScrollBullet.mDisX = distanceX;
		mScrollBullet.mDisY = distanceY;
		boolean fired = mStateMachine.testTrigger(mScrollBullet, this);
		LogUtils.d(TAG, "Scroll bullet: currentState=" + mStateMachine.getCurrentStateName());
		return fired;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		LogUtils.d(TAG, "onFling !!");
		mFlingBullet.mEvent1 = e1;
		mFlingBullet.mEvent2 = e2;
		mFlingBullet.mVelX = velocityX;
		mFlingBullet.mVelY = velocityY;
		boolean fired = mStateMachine.testTrigger(mFlingBullet, this);
		LogUtils.d(TAG, "Fling bullet: currentState=" + mStateMachine.getCurrentStateName());
		return fired;
	}
	

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animator animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		LogUtils.d(TAG, "onAnimationEnd !!");
		mAnimatorBullet.mAnimator = animation;
		mStateMachine.testTrigger(mAnimatorBullet, this);
		LogUtils.d(TAG, "Animator bullet: currentState=" + mStateMachine.getCurrentStateName());
	}

	@Override
	public void onAnimationCancel(Animator animation) {
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		// TODO Auto-generated method stub
		
	}
	
	public void setFactor(float factor) {
		mFactor = factor;
	}
	
	public float getFactor() {
		return mFactor;
	}
	
	public synchronized boolean isAnimationCancelled() {
		return mAnimCancelled;
	}
	
	public void alignAnimation() {
		cancelAnimation();
		
		mAnimator.setTarget(this);
		mAnimator.setFloatValues(Math.abs(mFactor), DST_PAGE_FACTOR);
		startAnimation();
	}
	
	private synchronized void startAnimation() {
		mAnimCancelled = false;
		mAnimator.start();
	}
	
	private synchronized void cancelAnimation() {
		mAnimCancelled = true;
		mAnimator.cancel();
	}
	
	
	// ===================================================
	// State machine handle
	// ===================================================
	
	class SIdle extends State {
		@Override
		public String name() {
			return "StateIdle";
		}
		
		@Override
		public void onEnter(State from, State to, Bullet bullet, Object obj) {
			LogUtils.d(TAG, name() + ": onEnter: from=" + from + ", to=" + to + ", bullet=" + bullet);
		}

		@Override
		public void onLeave(State from, State to, Bullet bullet, Object obj) {
			LogUtils.d(TAG, name() + ": onLeave: from=" + from + ", to=" + to + ", bullet=" + bullet);
		}

		@Override
		public void onStandby(State in, Bullet bullet, Object obj) {
			LogUtils.d(TAG, name() + ": onStandby: in=" + in + ", bullet=" + bullet);
		}
	}
	
	class SScrollToPrev extends State {
		@Override
		public String name() {
			return "StateScrollToPrev";
		}

		@Override
		public void onEnter(State from, State to, Bullet bullet, Object obj) {
			LogUtils.d(TAG, name() + ": onEnter: from=" + from + ", to=" + to + ", bullet=" + bullet);
		}

		@Override
		public void onLeave(State from, State to, Bullet bullet, Object obj) {
			LogUtils.d(TAG, name() + ": onLeave: from=" + from + ", to=" + to + ", bullet=" + bullet);
			
			if (TouchUpBullet.NAME.equals(bullet.name())) {
				leaveByTouchUp(from, to, bullet, obj);
			}
		}
		
		private void leaveByTouchUp(State from, State to, Bullet bullet, Object obj) {
			TouchUpBullet rBullet = (TouchUpBullet) bullet;
			MachineView view = (MachineView) obj;
			view.alignAnimation();
		}

		@Override
		public void onStandby(State in, Bullet bullet, Object obj) {
			LogUtils.d(TAG, name() + ": onStandby: in=" + in + ", bullet=" + bullet);
			
			ScrollBullet rBullet = (ScrollBullet) bullet;
			MachineView view = (MachineView) obj;
			
			int width = view.getWidth();
			if (0 == width) {
				return;
			}
			
			float factor = 0;
			float oldDelta = width * view.getFactor();
			float delta = oldDelta + rBullet.mDisX;
			if (width <= 0) {
				factor = 0;
			} else {
				factor = delta / (float)width;
			}
			
			view.setFactor(factor);
			LogUtils.d(TAG, name() + ": onStandby: set factor to: " + factor);
		}
	}
	
	class SAlignFromPrev extends State {
		@Override
		public String name() {
			return "StateAlignFromPrev";
		}

		@Override
		public void onEnter(State from, State to, Bullet bullet, Object obj) {
			LogUtils.d(TAG, name() + ": onEnter: from=" + from + ", to=" + to + ", bullet=" + bullet);
		}

		@Override
		public void onLeave(State from, State to, Bullet bullet, Object obj) {
			LogUtils.d(TAG, name() + ": onLeave: from=" + from + ", to=" + to + ", bullet=" + bullet);
		}

		@Override
		public void onStandby(State in, Bullet bullet, Object obj) {
			LogUtils.d(TAG, name() + ": onStandby: in=" + in + ", bullet=" + bullet);
		}
	}
	
	class SFlipToPrev extends State {
		@Override
		public String name() {
			return "StateFlipToPrev";
		}

		@Override
		public void onEnter(State from, State to, Bullet bullet, Object obj) {
		}

		@Override
		public void onLeave(State from, State to, Bullet bullet, Object obj) {
		}

		@Override
		public void onStandby(State in, Bullet bullet, Object obj) {
		}
	}
	
	class Tidtosp extends Trigger {
		public Tidtosp(State from, State to) {
			super(from, to);
		}
		
		@Override
		public boolean fire(Bullet bullet, Object obj) {
			if (!ScrollBullet.NAME.equals(bullet.name())) {
				return false;
			}
			
			MachineView view = (MachineView) obj;
			ScrollBullet rBullet = (ScrollBullet) bullet;
			
			int width = view.getWidth();
			if (0 == width) {
				return false;
			}
			
			float factor = 0;
			float oldDelta = width * view.getFactor();
			float delta = oldDelta + rBullet.mDisX;
			if (width <= 0) {
				factor = 0;
			} else {
				factor = delta / (float)width;
			}
			
			if (factor > MachineView.SRC_PAGE_FACTOR) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void onTrigger(State from, State to, Bullet bullet, Object obj) {
		}
	}
	
	class Tsptosp extends Trigger {
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
		}
	}
	
	class Tsptoap extends Trigger {
		public Tsptoap(State from, State to) {
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
		}
	}
	
	class Taptosp extends Trigger {
		public Taptosp(State from, State to) {
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
		}
	}
	
	class Taptoid extends Trigger {
		public Taptoid(State from, State to) {
			super(from, to);
		}
		
		@Override
		public boolean fire(Bullet bullet, Object obj) {
			if (!AnimatorBullet.NAME.equals(bullet.name())) {
				return false;
			}
			
			MachineView view = (MachineView) obj;
			if (view.isAnimationCancelled()) {
				return false;
			}
			
			return true;
		}
		
		@Override
		public void onTrigger(State from, State to, Bullet bullet, Object obj) {
		}
	}

}
