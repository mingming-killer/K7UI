package com.eebbk.mingming.k7ui.effect.view;

import java.util.LinkedList;

import com.eebbk.mingming.k7ui.R;
import com.eebbk.mingming.k7ui.accessibility.ScrollDetector;
import com.eebbk.mingming.k7ui.accessibility.ScrollDetector.ScrollDetectorListener;
import com.eebbk.mingming.k7ui.effector.GLEffectView;
import com.eebbk.mingming.k7ui.effector.IEffectView;
import com.eebbk.mingming.k7ui.effector.Effector;
import com.eebbk.mingming.k7ui.effector.CanvasEffectView;
import com.eebbk.mingming.k7ui.machine.AnimatorBullet;
import com.eebbk.mingming.k7ui.machine.FlingBullet;
import com.eebbk.mingming.k7ui.machine.ScrollBullet;
import com.eebbk.mingming.k7ui.machine.StateMachine;
import com.eebbk.mingming.k7ui.machine.TouchDownBullet;
import com.eebbk.mingming.k7ui.machine.TouchUpBullet;
import com.eebbk.mingming.k7utils.LogUtils;
import com.eebbk.mingming.k7utils.ReflectUtils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Adapter;
import android.widget.AdapterView;

/**
 * 
 * Switch page with effect. </br>
 * 
 * </br>
 * Normal direct: </br> 
 * &nbsp;&nbsp; {@link #HORIZONTAL}: next: scroll to right; previous: scroll to left. </br>
 * &nbsp;&nbsp; {@link #VERTICAL}: next: scroll to bottom; previous: scroll to up. </br>
 * Use {@link #reverseSwitchDirect(boolean)}: to reverse direct(not support yet).
 * 
 * </br>
 * 
 * @author humingming <hmm@dw.gdbbk.com>
 * 
 */
public class EffectPageContainer extends AdapterView<Adapter> implements ScrollDetectorListener,
	Handler.Callback, AnimatorListener, AnimatorUpdateListener {
	
	private final static String TAG = "EffectPageContainer";
	// this flag is use for debug.
	public static boolean DEBUG = false; //true;
	
	/** switch type: horizontal  */
	public final static int HORIZONTAL = 0;
	/** switch type: vertical  */
	public final static int VERTICAL = 1;
	/** 
	 * Scroll level: don't support scroll any more, in this mode you 
	 * can switch page only by call API.
	 */
	public final static int SCROLL_LEVEL_NONE = 0;
	/** 
	 * Scroll level: only support fling, in this mode you switch page 
	 * by fling and call API.
	 */
	public final static int SCROLL_LEVEL_FLING = 1;
	/** 
	 * Scroll level: support all scroll, scroll, fling and call API.
	 */
	public final static int SCROLL_LEVEL_ALL = 2;
	
	/** Idle state: {@link SIdle} */
	public final static int STATE_IDLE = 0;
	/** ScrollToPrev State: {@link SScrollToPrev} */
	public final static int STATE_SCROLL_TO_PREV = 1;
	/** AlignFromPrev State: {@link AlignFromPrev} */
	public final static int STATE_ALIGN_FROM_PREV = 2;
	/** AlignFromPrev State: {@link AlignFromPrev} */
	public final static int STATE_FLING_TO_PREV = 3;
	/** ScrollToNext State: {@link ScrollToNext} */
	public final static int STATE_SCROLL_TO_NEXT = 4;
	/** AlignFromNext State: {@link AlignFromNext}  */
	public final static int STATE_ALIGN_FROM_NEXT = 5;
	/** FlingToNext State: {@link FlingToNext} */
	public final static int STATE_FLING_TO_NEXT = 6;
	
	// Previous page factor constant.
	/* package */ final static float PREV_PAGE_FACTOR = -1.0f;
	// Source page factor constant.
	/* package */ final static float SRC_PAGE_FACTOR = 0.0f;
	// Next page factor constant.
	/* package */ final static float NEXT_PAGE_FACTOR = 1.0f;
	// Half page factor constant.
	/* package */ final static float HALF_PAGE_FACTOR = 0.5f;
	// Destination page factor constant.
	/* package */ final static float DST_PAGE_FACTOR = 1.0f;
	// Over scroll factor bound constant. 
	/* package */ final static float OVER_SCROLL_FACTOR_BOUND = 0.45f;
	// Trigger fling velocity threshold
	/* package */ final static float FLING_VELOCITY_THRESHOLD = 800;
	
	private final static String STR_ORIENTATION_H = "horizontal"; 
	private final static String STR_ORIENTATION_V = "vertical";
    
	private final static String STR_SCROLL_LEVEL_NONE = "none";
	private final static String STR_SCROLL_LEVEL_FLING = "fling";
	private final static String STR_SCROLL_LEVEL_ALL = "all";
    
	private final static String DEFAULT_EFFECTOR = "ScrollEffector";
	private final static String DEFAULT_NAMESPACE = "com.eebbk.mingming.k7ui.effector.";
	private final static int DEFAULT_ORIENTATION = HORIZONTAL;
	private final static int DEFAULT_SCROLL_LEVEL = SCROLL_LEVEL_ALL;
	private final static boolean DEFAULT_LOOP_SHOW = false;
	private final static boolean DEFAULT_REVERSE = false;
	private final static boolean DEFAULT_USE_GL = false;
	
	private final static long DEFAULT_SWITCH_DURATION = 500;
	
	// this time is give for GL change texture,
	// in some device, GL vendor is not powerful, so we give some time for it.
	// TODO: this is a magic number, maybe we should provider some method to adjust it.
	private final static long SHOW_EFFECT_DELAY_TIME = 30; // 30ms
	
	private final static int MSG_UI_SIZE_CHANGED = 100;
	private final static int MSG_UI_SHOW_EFFECT = 101;
	
	// state machine and bullets.
	private StateMachine mStateMachine;
	@SuppressWarnings("unused")
	private TouchDownBullet mTouchDownBullet;
	private TouchUpBullet mTouchUpBullet;
	private ScrollBullet mScrollBullet;
	private FlingBullet mFlingBullet;
	private AnimatorBullet mAnimatorBullet;
    
    // True: in over scroll, false: normal
	private boolean mInOverScroll;
    // Over scroll drawable.
	// TODO: add support left and right.
	private Drawable mDrOverScroll;
	// effector background color.
	private int mBkColor;
	
    // switch orientation.
	private int mOrientation;
    // scroll level.
	private int mScrollLevel;
    // True: loop switch，false: limit in bound.
	private boolean mLoopShow;
    // True: reverse switch, false: normal
	// TODO: now not support.
	private boolean mReverseDirect;
    
	// page factor, range: [PREV_PAGE_FACTOR, NEXT_PAGE_FACTOR]
	// the factor is more close to bound, means current state is more close to target page.
	private float mPageFactor;
	
	// flag whether is animation is end by cancel.
	private boolean mAnimCancelled;
    
    // will switch page index.
	private int mTargetPageIndex;
	// current page index.
	private int mCurrentPageIndex;
    
	private ScrollDetector mScrollDetector = null;
	
	private long mAnimDuration;
	private TimeInterpolator mAnimInterpolator = null;
	private TimeInterpolator mLinearInterpolator = null;
	private ObjectAnimator mSwitchAnim = null;
	
	private View mDstView = null;
	private View mSrcView = null;
    
	private IEffectView mIEffectView = null;
	private View mEffectView = null;
	
	private LinkedList<View> mViewPool = null;
	private Adapter mAdapter = null;
	private AdapterDataSetObserver mDataSetObserver = null;
    
    private boolean mIsGL = false;
    private Rect mChangeSize = null;
    private Handler mUIHandler = null;
    
    private EffectPageContainerListener mListener = null;
    
    /**
     * 
     * Page container listener.
     * 
     * @author humingming <hmm@dw.gdbbk.com>
     * 
     */
    public interface EffectPageContainerListener {
    	
    	/**
    	 * Switch to new page.
    	 * 
    	 * @param view Page content view.
    	 * @param currentPage new page index.
    	 */
    	public void onEffectPageChangePage(View view, int currentPage);
    	
    	/**
    	 * Prepare new page. This is only tell you the page container is going to 
    	 * prepare new page, your can do something before it.
    	 * 
    	 * @param targetPage Target page index.
    	 */
    	public void onEffectPagePreparePage(int targetPage);
    	
    	/**
    	 * Page container state changed.
    	 * 
    	 * @param currentState current state.
    	 */
    	public void onEffectPageChangeState(int currentState);
    	
    	/**
    	 * Page container orientation is changed.
    	 * 
    	 * @param orientation {@link EffectPageContainer#HORIZONTAL}, {@link EffectPageContainer#VERTICAL}
    	 */
    	public void onEffectPageChangeOrientation(int orientation);
    	
    }
    
	public EffectPageContainer(Context context) {
		this(context, null);
	}
	
	public EffectPageContainer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public EffectPageContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EffectPageContainer, defStyle, 0);
        
        String strAttr = null;
        Effector effector = null;
        
        mIsGL = a.getBoolean(R.styleable.EffectPageContainer_k7uiepcUseGL, DEFAULT_USE_GL);
        
        mLoopShow = a.getBoolean(R.styleable.EffectPageContainer_k7uiepcLoopShow, DEFAULT_LOOP_SHOW);
        mReverseDirect = a.getBoolean(R.styleable.EffectPageContainer_k7uiepcReverse, DEFAULT_REVERSE);
        
        strAttr = a.getString(R.styleable.EffectPageContainer_k7uiepcEffector);
        if (null == strAttr) {
        	strAttr = DEFAULT_EFFECTOR;
        }
        try {
        	strAttr = fillNameSpace(strAttr);
        	effector = (Effector) ReflectUtils.newObject(strAttr);
        	effector.setHighQuality(true);
        } catch (Exception e) {
        	effector = null;
        }
        
        strAttr = a.getString(R.styleable.EffectPageContainer_k7uiepcOrientation);
        if (null == strAttr) {
        	mOrientation = DEFAULT_ORIENTATION;
        } else if (strAttr.equals(STR_ORIENTATION_H)) {
        	mOrientation = HORIZONTAL;
        } else if (strAttr.equals(STR_ORIENTATION_V)) {
        	mOrientation = VERTICAL;
        } else {
        	mOrientation = DEFAULT_ORIENTATION;
        }
        
        strAttr = a.getString(R.styleable.EffectPageContainer_k7uiepcScrollLevel);
        if (null == strAttr) {
        	mScrollLevel = DEFAULT_SCROLL_LEVEL;
        } else if (strAttr.equals(STR_SCROLL_LEVEL_NONE)) {
        	mScrollLevel = SCROLL_LEVEL_NONE;
        } else if (strAttr.equals(STR_SCROLL_LEVEL_FLING)) {
        	mScrollLevel = SCROLL_LEVEL_FLING;
        } else if (strAttr.equals(STR_SCROLL_LEVEL_ALL)) {
        	mScrollLevel = SCROLL_LEVEL_ALL; 
        } else {
        	mScrollLevel = DEFAULT_SCROLL_LEVEL;
        }
        
        a.recycle();
		
		mTargetPageIndex = -1;
		mCurrentPageIndex = -1;
		
		mAnimCancelled = false;
		
		mPageFactor = SRC_PAGE_FACTOR;

		mInOverScroll = false;
		mDrOverScroll = new ColorDrawable(0x00ffffff);
		mBkColor = 0x00ffffff;
		
		mAnimDuration = DEFAULT_SWITCH_DURATION;
		mAnimInterpolator = new LinearInterpolator();
		mLinearInterpolator = new LinearInterpolator();
		mSwitchAnim = ObjectAnimator.ofFloat(null, 
				IEffectView.PROPERTY_NAME, 
				Effector.SRC_FACTOR, Effector.DST_FACTOR);
		mSwitchAnim.setDuration(mAnimDuration);
		mSwitchAnim.setInterpolator(mAnimInterpolator);
		mSwitchAnim.addListener(this);
		mSwitchAnim.addUpdateListener(this);
		
		mScrollDetector = new ScrollDetector(context, this, mOrientation, this);
		
		mListener = null;
		mChangeSize = new Rect();
		
		mSrcView = null;
		mDstView = null;
		mViewPool = new LinkedList<View> ();
		
		// TODO: we don't support switch between Canvas and GL effector.
		// this is too complex, and don't have useful.
		if (isGL()) {
			mEffectView = null;
			mIEffectView = null;
			setEffector(effector);
		} else {
			mEffectView = new CanvasEffectView(context);
			mIEffectView = (IEffectView) mEffectView;
			setEffector(effector);
			addAdapterView(mEffectView, false);
		}
		
		initHandler();
		initStateMachine();
	}
	
	private String fillNameSpace(String className) {
		if (null == className) {
			return null;
		}
		
		if (-1 == className.indexOf(".")) {
			return DEFAULT_NAMESPACE + className;
		} else {
			return className;
		}
	}
	
	private void initHandler() {
    	mUIHandler = new Handler(this);
    }
	
	private void initStateMachine() {
		mStateMachine = new StateMachine();
		
		mTouchDownBullet = new TouchDownBullet();
		mTouchUpBullet = new TouchUpBullet();
		mScrollBullet = new ScrollBullet();
		mFlingBullet = new FlingBullet();
		mAnimatorBullet = new AnimatorBullet();
		
		// build states and triggers.
		SIdle sIdle = new SIdle();
		SScrollToPrev sScrollToPrev = new SScrollToPrev();
		SAlignFromPrev sAlignFromPrev = new SAlignFromPrev();
		SFlingToPrev sFlingToPrev = new SFlingToPrev();
		SScrollToNext sScrollToNext = new SScrollToNext();
		SAlignFromNext sAlignFromNext = new SAlignFromNext();
		SFlingToNext sFlingToNext = new SFlingToNext();
		
		
		// notices: first add trigger is have high priority test.
		Titofp titofp = new Titofp(sIdle, sFlingToPrev);
		Titosp titosp = new Titosp(sIdle, sScrollToPrev);
		Tsptofp tsptofp = new Tsptofp(sScrollToPrev, sFlingToPrev);
		Tsptoi tsptoi = new Tsptoi(sScrollToPrev, sIdle);
		Tsptosp tsptosp = new Tsptosp(sScrollToPrev, sScrollToPrev);
		Tsptoap tsptoap = new Tsptoap(sScrollToPrev, sAlignFromPrev);
		Taptoi taptoi = new Taptoi(sAlignFromPrev, sIdle);
		Tfptoi tfptoi = new Tfptoi(sFlingToPrev, sIdle);
		//Tfptosp tfptosp = new Tfptosp(sFlingToPrev, sScrollToPrev);
		
		Titofn titofn = new Titofn(sIdle, sFlingToNext);
		Titosn titosn = new Titosn(sIdle, sScrollToNext);
		Tsntofn tsntofn = new Tsntofn(sScrollToNext, sFlingToNext);
		Tsntoi tsntoi = new Tsntoi(sScrollToNext, sIdle);
		Tsntosn tsntosn = new Tsntosn(sScrollToNext, sScrollToNext);
		Tsntoan tsntoan = new Tsntoan(sScrollToNext, sAlignFromNext);
		Tantoi tantoi = new Tantoi(sAlignFromNext, sIdle);
		Tfntoi tfntoi = new Tfntoi(sFlingToNext, sIdle);
		//Tfntosn tfntosn = new Tfntosn(sFlingToNext, sScrollToNext);
		
		
		mStateMachine.addState(sIdle);
		mStateMachine.addState(sScrollToPrev);
		mStateMachine.addState(sAlignFromPrev);
		mStateMachine.addState(sFlingToPrev);
		
		mStateMachine.addState(sScrollToNext);
		mStateMachine.addState(sAlignFromNext);
		mStateMachine.addState(sFlingToNext);
		
		
		mStateMachine.addTrigger(titofp);
		mStateMachine.addTrigger(titosp);
		mStateMachine.addTrigger(tsptofp);
		mStateMachine.addTrigger(tsptoi);
		mStateMachine.addTrigger(tsptosp);
		mStateMachine.addTrigger(tsptoap);
		mStateMachine.addTrigger(taptoi);
		mStateMachine.addTrigger(tfptoi);
		//mStateMachine.addTrigger(tfptosp);
		
		mStateMachine.addTrigger(titofn);
		mStateMachine.addTrigger(titosn);
		mStateMachine.addTrigger(tsntofn);
		mStateMachine.addTrigger(tsntoi);
		mStateMachine.addTrigger(tsntosn);
		mStateMachine.addTrigger(tsntoan);
		mStateMachine.addTrigger(tantoi);
		mStateMachine.addTrigger(titofn);
		mStateMachine.addTrigger(tfntoi);
		//mStateMachine.addTrigger(tfntosn);
		
		
		// run state machine with idle state as begin state.
		resetPageToInit();
		//mStateMachine.startRun(sIdle.name());
		if (DEBUG) mStateMachine.debugDump();
	}
	
	private void resetPageToInit() {
		cancelSwitchAnim();
		
		mCurrentPageIndex = 0;
		mTargetPageIndex = 0;
		
		mPageFactor = SRC_PAGE_FACTOR;
		
		mStateMachine.startRun(SIdle.NAME);
		
		prepareTargetPageContent(mCurrentPageIndex);
		showEffect(false);
	}
	
	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}
	
	@Override
	public void setAdapter(Adapter adapter) {
        if (null != mAdapter && null != mDataSetObserver) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        
        mAdapter = adapter;

        if (null != mAdapter) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
        
        setFocusable(true);
        showPage(0, false, true);
	}
	
	@Override
	public View getSelectedView() {
		return mDstView;
	}
	
	@Override
	public void setSelection(int position) {
		showPage(position, false);
	}
	
	/*@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		//LogUtils.d(TAG, "dispatch: act: " + ev.getAction()
		//		+ ", act my mask: " + (ev.getAction() & MotionEvent.ACTION_MASK)
		//		+ ", actMasked: " + ev.getActionMasked() 
		//		+ ", actIndex: " + ev.getActionIndex());
		//debugShowCurrentState("dispatch: ");
		
		return super.dispatchTouchEvent(ev);
	}*/
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean handled = mScrollDetector.onInterceptTouchEvent(ev);
		if (DEBUG) LogUtils.d(TAG, "onInterceptTouchEvent: handled: " + handled);
		
		if (!handled) {
			onUp(ev);
		}
		
		return handled;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mScrollDetector.onTouchEvent(ev)) {
			if (DEBUG) LogUtils.d(TAG, "onTouchEvent: scroll detector handled onTouch event !");
			return true;
		}
		return super.onTouchEvent(ev);
	}
	
	@Override
	public boolean onDetectScrolling() {
		// we only give up touch event when we are in idle state.
		if (SIdle.NAME.equals(mStateMachine.getCurrentStateName())) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public boolean onDown(MotionEvent ev) {
		if (DEBUG) LogUtils.d(TAG, "onDown: act: " + ev.getAction()
				+ ", act my mask: " + (ev.getAction() & MotionEvent.ACTION_MASK)
				+ ", actMasked: " + ev.getActionMasked() 
				+ ", actIndex: " + ev.getActionIndex());
		debugShowCurrentState("onDown");
		
		if (SCROLL_LEVEL_NONE == mScrollLevel ||
				SCROLL_LEVEL_FLING == mScrollLevel) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean onUp(MotionEvent ev) {
		if (DEBUG) LogUtils.d(TAG, "onUp: act: " + ev.getAction()
				+ ", act my mask: " + (ev.getAction() & MotionEvent.ACTION_MASK)
				+ ", actMasked: " + ev.getActionMasked() 
				+ ", actIndex: " + ev.getActionIndex());
		debugShowCurrentState("onDown");
		
		if (SCROLL_LEVEL_NONE == mScrollLevel ||
				SCROLL_LEVEL_FLING == mScrollLevel) {
			return false;
		}
		
		mTouchUpBullet.mEvent = ev;
		boolean fired = mStateMachine.testTrigger(mTouchUpBullet, this);
		if (DEBUG) LogUtils.d(TAG, "Touch up bullet: currentState=" + mStateMachine.getCurrentStateName());
		return fired;
	}
	
	@Override
	public boolean onScroll(MotionEvent ev1, MotionEvent ev2, float distanceX,
			float distanceY) {
		if (DEBUG) LogUtils.d(TAG, "onScroll: disX: " + distanceX + ", disY: " + distanceY);
		debugShowCurrentState("onScroll");
		
		if (SCROLL_LEVEL_NONE == mScrollLevel ||
				SCROLL_LEVEL_FLING == mScrollLevel) {
			return false;
		}
		
		mScrollBullet.mEvent1 = ev1;
		mScrollBullet.mEvent2 = ev2;
		mScrollBullet.mDisX = distanceX;
		mScrollBullet.mDisY = distanceY;
		boolean fired = mStateMachine.testTrigger(mScrollBullet, this);
		if (DEBUG) LogUtils.d(TAG, "Scroll bullet: currentState=" + mStateMachine.getCurrentStateName());
		return fired;
	}
	
	@Override
	public boolean onFling(MotionEvent ev1, MotionEvent ev2, float velocityX,
			float velocityY) {
		if (DEBUG) LogUtils.d(TAG, "onFling: velX=" + velocityX + ", velY=" + velocityY + 
				", act: " + ev1.getAction() + ", ev2: " + ev2.getAction());
		debugShowCurrentState("onFling");
		
		if (SCROLL_LEVEL_NONE == mScrollLevel) {
			return false;
		}
		
		mFlingBullet.mEvent1 = ev1;
		mFlingBullet.mEvent2 = ev2;
		mFlingBullet.mVelX = velocityX;
		mFlingBullet.mVelY = velocityY;
		boolean fired = mStateMachine.testTrigger(mFlingBullet, this);
		if (DEBUG) LogUtils.d(TAG, "Fling bullet: currentState=" + mStateMachine.getCurrentStateName());
		return fired;
	}
	
	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		// update the page factor by animation changed.
		updatePageFactorByAnim();
	}
	
	@Override
	public void onAnimationCancel(Animator animation) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onAnimationEnd(Animator animation) {
		if (DEBUG) LogUtils.d(TAG, "onAnimationEnd");
		
		mAnimatorBullet.mAnimator = animation;
		mStateMachine.testTrigger(mAnimatorBullet, this);
		if (DEBUG) LogUtils.d(TAG, "Animator bullet: currentState=" + mStateMachine.getCurrentStateName());
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
	}

	@Override
	public void onAnimationStart(Animator animation) {
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		try {
			switch (msg.what) {
			case MSG_UI_SIZE_CHANGED:
				Rect changeSize = (Rect) msg.obj;
				handleUISizeChanged(changeSize.left, changeSize.top, 
						changeSize.right, changeSize.bottom);
				break;
				
			case MSG_UI_SHOW_EFFECT:
				showEffectForGL();
				break;
				
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	protected void handleUISizeChanged(int w, int h, int oldW, int oldH) {
        int childWidth = w - getPaddingLeft() - getPaddingRight();
        int childHeight = h - getPaddingTop() - getPaddingBottom();
        
		if (null != mIEffectView) {
			mIEffectView.buildImage(childWidth, childHeight, Bitmap.Config.ARGB_8888);
		}
		
		setupOverScrollDrawableBound();
		refreshCurrentPage();
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = getChildCount();
        if (childCount <= 0 /*|| !changed*/) {
        	return;
        }
		
        int width = r - l;
        int height = b - t;
        int childWidth = width - getPaddingLeft() - getPaddingRight();
        int childHeight = height - getPaddingTop() - getPaddingBottom();
        
        View childView;
        for (int i = 0; i < getChildCount(); i++) {
        	childView = getChildAt(i); 	
        	childView.layout(
        			0 + getPaddingLeft(),
        			0 + getPaddingTop(),
        			0 + getPaddingLeft() + childWidth,
        			0 + getPaddingTop() + childHeight);
        }
	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height =  MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        
        View childView;
        
        // if don't specific size, we use first view size as whole view group size.
    	if ((widthMode == MeasureSpec.UNSPECIFIED && 0 == width) || 
    			(heightMode == MeasureSpec.UNSPECIFIED && 0 == height)) {
    		childView = getChildAt(0);
    		if (null != childView) {
    			// let child measure itself.
    			measureChild(childView, 
    					MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST), 
    					MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST));
    			
    			if (widthMode == MeasureSpec.UNSPECIFIED && 0 == width) {
    				width = childView.getMeasuredWidth();
    				widthMode = MeasureSpec.EXACTLY;
    			}
    			
    			if (heightMode == MeasureSpec.UNSPECIFIED && 0 == height) {
    				height = childView.getMeasuredHeight();
    				heightMode = MeasureSpec.EXACTLY;
    			}
    		}
    	}
        
        int childWidth = width - getPaddingLeft() - getPaddingRight();
        int childHeight = height - getPaddingTop() - getPaddingBottom();
    	
        for (int i = 0; i < getChildCount(); i++) {
        	childView = getChildAt(i);
        	childView.measure(
        			MeasureSpec.makeMeasureSpec(childWidth, widthMode), 
        			MeasureSpec.makeMeasureSpec(childHeight, heightMode));
        }
        
        setMeasuredDimension(width, height);
        
        mScrollDetector.onMeasure(getMeasuredWidth(), getMeasuredHeight());
    }
    
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		if (w > 0 && w != oldw && 
				h > 0 && h != oldh) {
			// here though parent is already layouted, but child view don't layouted.
			// we use a message queue to wait layout message has been done.
			// we update view in next message loop handle.
			mChangeSize.set(w, h, oldw, oldh);
			Message msg = mUIHandler.obtainMessage(MSG_UI_SIZE_CHANGED, mChangeSize);
			mUIHandler.sendMessage(msg);
		}
	}
	
	//@Override
	//protected void onAttachedToWindow() {
	//	super.onAttachedToWindow();
	//}
	
	//protected void onDetachedFromWindow() {
	//	super.onDetachedFromWindow();
	//	
	//	mViewPool.clear();
	//}
	
    /** 
     * Release resources.
     */
    public void free() {
    	if (null == mViewPool) {
    		mViewPool.clear();
    	}
    	
    	if (null != mIEffectView) {
    		mIEffectView.free();
    	}
    }
    
    /**
     * Pause the effect, this is useful for GL effector.
     * Please call it in your Activity onPause.
     */
	public void onPause() {
		if (null != mIEffectView) {
			mIEffectView.pauseEffect();
		}
	}

    /**
     * Resume the effect, this is useful for GL effector.
     * Please call it in your Activity onResume.
     */
	public void onResume() {
		if (null != mIEffectView) {
			mIEffectView.resumeEffect();
		}
	}
	
	/**
	 * Set current show item. see {@link #showPage}.
	 * 
	 * @param position 
	 * @param animate
	 */
	public void setSelection(int position, boolean animate) {
		showPage(position, animate);
	}
	
	/**
	 * Set effector. it can only switch some type effector(e.g GL switch to GL, Canvas switch to Canvas).
	 * 
	 * @param effector Object of {@link Effector}
	 */
	public void setEffector(Effector effector) {
		if (isGL()) {
			if (null != mIEffectView) {
				mIEffectView.free();
				removeEffectView();
			}
			mEffectView = createHardwareEffectViewByEffector(effector);
			if (null == mEffectView) {
				return;
			}
			mIEffectView = (IEffectView) mEffectView;
			addAdapterView(mEffectView, false);
			
			if (null != mDstView) {
				bringChildToFront(mDstView);
			}
		}
		
		if (null == mIEffectView) {
			return;
		}
		
		effector.setBkColor(mBkColor);
		mIEffectView.setEffector(effector, true);
		mIEffectView.buildImage(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		
		refreshCurrentPage();
	}
	
	private GLEffectView createHardwareEffectViewByEffector(Effector effector) {
		if (null == effector) {
			return null;
		}
		
		// the hardware effect view don't support dynamic change effector.
		// because hardware effect view is base SufarceView, is must be re-create to change effector.
		int layout = com.eebbk.mingming.k7ui.R.layout.k7ui_evgl;
		if (effector.supportTranBk()) {
			layout = com.eebbk.mingming.k7ui.R.layout.k7ui_evgl_tran;
		}
		
		return (GLEffectView)inflate(getContext(), layout, null);
	}
	
	/**
	 * Set effector type, this is implement in effector.
	 * 
	 * @param type
	 */
	public void setEffectType(int type) {
		if (null == mIEffectView) {
			return;
		}
		mIEffectView.setEffectType(type);
	}

	/**
	 * Set effect quality.
	 * 
	 * @param highQuality True: high，false: low
	 */
	public void setHighQuality(boolean highQuality) {
		if (null == mIEffectView) {
			return;
		}
		mIEffectView.setHighQuality(highQuality);
	}
	
	/**
	 * Set background color, notice: this is set effector background color.
	 * 
	 * @param color
	 */
	public void setBkColor(int color) {
		mBkColor = color;
		if (null != mIEffectView) {
			mIEffectView.setBkColor(color);
		}
	}
	
	/**
	 * Set the over scroll drawable, this is use from draw over scroll.
	 * 
	 * @param drawable
	 */
	public void setOverScrollDrawable(Drawable drawable) {
		mDrOverScroll = drawable;
		setupOverScrollDrawableBound();
	}
	
	private void setupOverScrollDrawableBound() {
		if (null != mDrOverScroll) {
			// TODO: measure padding.
			mDrOverScroll.setBounds(0, 0, getWidth(), getHeight());
		}
	}
	
	/**
	 * Set switch orientation.
	 * 
	 * @param type {@link #HORIZONTAL} or {@link #VERTICAL}
	 */
	public void setOrientation(int orientation) {
		if (orientation == mOrientation) {
			return;
		}
		
		mOrientation = orientation;
		if (null != mListener) {
			mListener.onEffectPageChangeOrientation(mOrientation);
		}
		
		if (HORIZONTAL == mOrientation) {
			mScrollDetector.setScrollType(ScrollDetector.HORIZONTAL);
		} else {
			mScrollDetector.setScrollType(ScrollDetector.VERTICAL);
		}
	}
	
	/**
	 * Set scroll level.
	 * 
	 * @param level {@link #SCROLL_LEVEL_NONE}, {@link #SCROLL_LEVEL_FLING}, {@link #SCROLL_LEVEL_ALL}
	 */
	public void setScrollLevel(int level) {
		mScrollLevel = level;
		if (null != mIEffectView || !mIEffectView.haveEffector()) {
			mScrollLevel = SCROLL_LEVEL_NONE;
		}
		
		refreshCurrentPage();
	}
	
	/**
	 * Set whether allow loop show.
	 * 
	 * @param looped True: allow，false: disable
	 */
	public void setLoopShow(boolean looped) {
		mLoopShow = looped;
	}
	
	/**
	 * Set switch animation duration time.
	 * 
	 * @param duration time
	 */
	public void setSwitchAnimDuration(long duration) {
		mAnimDuration = duration;
		//mSwitchAnim.setDuration(duration);
	}
	
	/**
	 * Set switch animation interpolator.
	 * 
	 * @param interpolator Object of {@link TimeInterpolator}
	 */
	public void setSwitchAnimInterpolator(TimeInterpolator interpolator) {
		mAnimInterpolator = interpolator;
		//mSwitchAnim.setInterpolator(interpolator);
	}
	
	/**
	 * Set page listener.
	 * 
	 * @param listener Object of {@link EffectPageContainerListener}
	 */
	public void setEffectPageContainerListener(EffectPageContainerListener listener) {
		mListener = listener;
	}
	
	/**
	 * Set whether reverse switch direct.
	 * 
	 * @param reverse True: reverse, false: normal
	 */
	public void reverseSwitchDirect(boolean reverse) {
		mReverseDirect = reverse;
	}
	
	/**
	 * Get current page index.
	 * 
	 * @return current page index.
	 */
	public int getSelection() {
		return mCurrentPageIndex;
	}
	
	/**
	 * Get total page count.
	 * 
	 * @return total page count.
	 */
	public int getTotalPage() {
		if (null == mAdapter) {
			return 0;
		}
		return mAdapter.getCount();
	}
	
	/**
	 * Get effector name.
	 * 
	 * @return String name.
	 */
	public String getEffectName() { 
		if (null == mIEffectView) {
			return null;
		}
		return mIEffectView.getEffectorName();
	}
	
	/**
	 * Get effector object. Your the better not use it.
	 * 
	 * @return Object of {@link Effector}
	 */
	public Effector getEffector() {
		if (null == mIEffectView) {
			return null;
		}
		return mIEffectView.getEffector();
	}
	
	/** 
	 * Get switch orientation.
	 * 
	 * @return {@link #HORIZONTAL}, {@link #VERTICAL}
	 */
	public int getOrientation() {
		return mOrientation;
	}
	
	/**
	 * Get scroll level.
	 * 
	 * @return {@link #SCROLL_LEVEL_NONE}, {@link #SCROLL_LEVEL_FLING}, {@link #SCROLL_LEVEL_ALL}.
	 */
	public int getScrollLevel() {
		return mScrollLevel;
	}
	
	/**
	 * Get effect quality.
	 * 
	 * @return True: high, false: low
	 */
	public boolean isHighEffectQuality() {
		if (null == mIEffectView) {
			return false;
		}
		return mIEffectView.isHighQuality();
	}
	
	/**
	 * Query whether allow loop show.
	 * 
	 * @return True: allow, false: disable.
	 */
	public boolean isLoopShow() {
		return mLoopShow;
	}
	
	/**
	 * Query whether reverse direct.
	 * 
	 * @return True: reverse, false: normal
	 */
	public boolean isReverseDirect() {
		return mReverseDirect;
	}
	
	/**
	 * Query effector is GL or Canvas.
	 * 
	 * @return True: GL, false: Canvas.
	 */
	public boolean isGL() {
		return mIsGL; 
	}
	
	/**
	 * Switch to previous page.
	 * 
	 * @param animate True: with animation, false: no-animation.
	 * @return
	 */
	public boolean showPrevious(boolean animate) {
		fakeSwitchAnimEnd();
		return showPage(mCurrentPageIndex - 1, animate);
	}
	
	/**
	 * Switch to next page.
	 * 
	 * @param animate True: with animation, false: no-animation.
	 * @return
	 */
	public boolean showNext(boolean animate) {
		return showPage(mCurrentPageIndex + 1, animate);
	}
	
	/**
	 * Switch to give page.
	 * 
	 * @param whichPage Target page index
	 * @param animate True: with animation, false: no-animation.
	 * @return
	 */
	public boolean showPage(int whichPage, boolean animate) {
		return showPage(whichPage, animate, false);
	}
	
	/**
	 * Switch to give page.
	 * 
	 * @param whichPage Target page index
	 * @param animate True: with animation, false: no-animation.
	 * @param forceUpdate True: force update page, false: if current is in target page, it will not update.
	 * @return True: success, false: failed.
	 */
	public boolean showPage(int whichPage, boolean animate, boolean forceUpdate) {
		debugShowCurrentState("showPage(x,x,x)");
		
		// now before show a page new, we end old animation first.
		// it's simple and effectually.
		cancelSwitchAnim();
		fakeSwitchAnimEnd();
		
		if (!forceUpdate) {
			if (whichPage == mCurrentPageIndex) {
				LogUtils.d(TAG, "current page already in give page.");
				return false;
			}
		}
		
		if (null == mAdapter) {
			LogUtils.d(TAG, "Adatper is null, you should call setAdapter to set first !!");
			return false;
		}
		
		// calculate target page.
		boolean toNext = whichPage >= mCurrentPageIndex ? true : false;
		calcTargetPageIndex(whichPage);
		
		// API is out of any state, so we can't use the state machine to handle it. -_-||
		if (animate && !mInOverScroll 
				&& null != mIEffectView && null != mSwitchAnim 
				&& mCurrentPageIndex != mTargetPageIndex) {
			if (STATE_IDLE == getCurrentState()) {
				// if we already in idle, prepare a page switch effect resource.
				prepareEffectRes(mTargetPageIndex, toNext);		
			} else {
				// otherwise(in show effect) we update page switch effect resource.
				updateEffectRes(toNext);
			}
			
			showEffect(true);
			changeToTargetPageWithAnim(toNext);
			//mIEffectView.setEffectFactor(0f);
			
			// use API to change page, we set state machine state manually. -_-||
			if (toNext) {
				mStateMachine.startRun(SFlingToNext.NAME);
			} else {
				mStateMachine.startRun(SFlingToPrev.NAME);
			}
			notifyChangeState();
			
		} else {
			prepareTargetPageContent(mTargetPageIndex);
			if (STATE_IDLE == getCurrentState()) {
				// if we already in idle, we manually end page effect.
				endPageEffect();
			} else {
				// otherwise we let the state machine to handle it.
				fakeSwitchAnimEnd();
			}
			notifyChangePage();
		}
		
		return true;
	}
	
	/**
	 * Refresh current page(redraw).
	 */
	public void refreshCurrentPage() {
		if (STATE_IDLE == getCurrentState()) {
			if (null != mDstView) {
				mAdapter.getView(mCurrentPageIndex, mDstView, this);
			}
		} else {
			if (null != mIEffectView) {
				mIEffectView.captureImage(Effector.ID_SRC | Effector.ID_DST);
				mIEffectView.refresh();
			}
		}
	}
	
	/**
	 * Refresh current page(redraw).
	 */
	public void refreshEffectView() {
		if (null != mIEffectView && mIEffectView.isGL()) {
			capturePageContent(true);
			mIEffectView.refresh();
		}
	}
	
	/* package */ boolean isAnimCancelled() {
		return mAnimCancelled;
	}
	
	/* package */ float getPageFactor() {
		return mPageFactor;
	}
	
	/* package */ int getTargetPageIndex() {
		return mTargetPageIndex;
	}
	
	/* package */ boolean isOverScroll() {
		if (getTotalPage() <= 1) {
			return true;
		} else {
			return mInOverScroll;
		}
	}
	
	/* package */ void endPageEffect() {
		mPageFactor = SRC_PAGE_FACTOR;
		mCurrentPageIndex = mTargetPageIndex;
		showEffect(false);
	}
	
	/* package */ void setPageFactor(float factor, boolean toNext) {
		mPageFactor = factor;
		if (null != mIEffectView) {
			if (toNext) {
				mIEffectView.reverseEffect(false);
			} else {
				mIEffectView.reverseEffect(true);
			}
			mIEffectView.setEffectFactor(Math.abs(mPageFactor));
		}
	}
	
	/* package */ void showEffect(boolean show) {
		if (show) {
			if (isGL()) {
				mUIHandler.removeMessages(MSG_UI_SHOW_EFFECT);
				mUIHandler.sendEmptyMessageDelayed(MSG_UI_SHOW_EFFECT, SHOW_EFFECT_DELAY_TIME);
			} else {
				showView(mSrcView, false);
				showView(mDstView, false);
				showView(mEffectView, true);
			}
		} else {
			showView(mSrcView, false);
			showView(mDstView, true);
			showView(mEffectView, false);
		}
	}
	
	private void showEffectForGL() {
		showView(mSrcView, false);
		showView(mDstView, false);
		showView(mEffectView, true);
	}
	
	/* package */ void prepareToPrevEffectRes() {
		prepareEffectRes(mCurrentPageIndex - 1);
	}
	
	/* package */ void prepareToNextEffectRes() {
		prepareEffectRes(mCurrentPageIndex + 1);
	}
	
	/* package */ void prepareEffectRes(int whichPage) {
		boolean toNext = whichPage >= mCurrentPageIndex ? true : false;
		calcTargetPageIndex(whichPage);
		prepareEffectRes(mTargetPageIndex, toNext);
	}
	
	private void calcTargetPageIndex(int whichPage) {
		int totalPage = getTotalPage();
		if (totalPage <= 0) {
			mTargetPageIndex = 0;
			mInOverScroll = true;
			return;
		}
		
		mTargetPageIndex = whichPage;
		
		if (mLoopShow) {
			if (mTargetPageIndex < 0) {
				mTargetPageIndex = totalPage - ((-mTargetPageIndex) % totalPage);
			} else if (mTargetPageIndex >= totalPage) {
				mTargetPageIndex = mTargetPageIndex % totalPage;
			}
			if (totalPage <= 1) {
				mInOverScroll = true;
			} else {
				mInOverScroll = false; 
			}
		} else {
			if (mTargetPageIndex < 0) {
				mTargetPageIndex = 0;
				mInOverScroll = true;
			} else if (mTargetPageIndex >= totalPage) {
				mTargetPageIndex = totalPage - 1;
				mInOverScroll = true;
			} else {
				mInOverScroll = false;
			}
		}
	}
	
	private void prepareTargetPageContent(int whichPage) {
		// create new page view.
		if (null != mListener) {
			mListener.onEffectPagePreparePage(whichPage);
		}
		if (-1 != indexOfChild(mDstView)) {
			mDstView = mAdapter.getView(whichPage, mDstView, this);
		} else {
			mDstView = viewFromAdapter(whichPage);
		}
	}
	
	private void prepareNewPageContent(int whichPage) {
		// change the src view and dst view.
		// if the dstView == srcView we don't need to change.
		if (mDstView != mSrcView) {
			// we remove the old view.
			if (null != mSrcView) {
				removeAdapterView(mSrcView);
			}
			// and let origin view as source view.
			mSrcView = mDstView;
		}
		
		// create new page view.
		if (null != mListener) {
			mListener.onEffectPagePreparePage(whichPage);
		}
		mDstView = viewFromAdapter(whichPage);
	}
	
	private void updatePageContent() {
		// update dst view
		if (null != mListener) {
			mListener.onEffectPagePreparePage(mTargetPageIndex);
		}
		mDstView = viewFromAdapter(mTargetPageIndex);
		
		// update src view
		if (!mInOverScroll) {
			if (null != mListener) {
				mListener.onEffectPagePreparePage(mCurrentPageIndex);
			}
			if (-1 != indexOfChild(mSrcView)) {
				mSrcView = mAdapter.getView(mCurrentPageIndex, mSrcView, this);
			} else {
				mSrcView = viewFromAdapter(mCurrentPageIndex);
			}
		} else {
			if (null != mSrcView) {
				removeAdapterView(mSrcView);
			}
			mSrcView = mDstView;
		}
	}
	
	private void prepareEffectRes(int whichPage, boolean toNext) {
		// prepare new page.
		prepareNewPageContent(whichPage);
		// create switch image.
		capturePageContent(toNext);
	}
	
	private void updateEffectRes(boolean toNext) {		
		// rebuild page content.
		updatePageContent();
		// create switch image.
		capturePageContent(toNext);
	}
	
	private void capturePageContent(boolean toNext) {
		if (null != mIEffectView) {
			if (toNext) {
				mIEffectView.setTargetView(mSrcView, mDstView);
				if (mInOverScroll) {
					mIEffectView.captureImage(Effector.ID_SRC);
					mIEffectView.fillImage(mDrOverScroll, Effector.ID_DST);
				} else {
					mIEffectView.captureImage(Effector.ID_SRC | Effector.ID_DST);
				}
			} else {
				mIEffectView.setTargetView(mDstView, mSrcView);
				if (mInOverScroll) {
					mIEffectView.captureImage(Effector.ID_DST);
					mIEffectView.fillImage(mDrOverScroll, Effector.ID_SRC);
				} else {
					mIEffectView.captureImage(Effector.ID_SRC | Effector.ID_DST);
				}
			}
		}
	}
	
	/* package */ void changeToTargetPage() {
		mCurrentPageIndex = mTargetPageIndex;
		mPageFactor = SRC_PAGE_FACTOR;
	}
	
	/* package */ void changeToTargetPageWithAnim(boolean toNext) {
		if (!mInOverScroll && null != mSwitchAnim && null != mIEffectView) {
			mSwitchAnim.setTarget(mEffectView);
			mSwitchAnim.setFloatValues(Math.abs(mPageFactor), DST_PAGE_FACTOR);
			setupAnimProperty(Math.abs(mPageFactor), DST_PAGE_FACTOR);
			if (toNext) {
				mIEffectView.reverseEffect(false);
			} else {
				mIEffectView.reverseEffect(true);
			}
			startSwitchAnim();
			//mIEffectView.setEffectFactor(0f);
		} else {
			// we fake the animation is end.
			fakeSwitchAnimEnd();
		}
	}
	
	/* package */ void backtoCurrentPage() {
		mTargetPageIndex = mCurrentPageIndex;
		if (mDstView != mSrcView) {
			View tmp = mDstView;
			if (null != mDstView) {
				removeAdapterView(mDstView);
			}
			mDstView = mSrcView;
			mSrcView = tmp;
		}
	}
	
	/* package */ void backToCurrentPageWithAnim(boolean fromNext) {
		// first let page content back to current page.
		backtoCurrentPage();
		
		// and then do animation.
		if (null != mSwitchAnim && null != mIEffectView) {
			mSwitchAnim.setTarget(mEffectView);
			mSwitchAnim.setFloatValues(Math.abs(mPageFactor), SRC_PAGE_FACTOR);
			setupAnimProperty(Math.abs(mPageFactor), SRC_PAGE_FACTOR);
			if (fromNext) {
				mIEffectView.reverseEffect(false);
			} else {
				mIEffectView.reverseEffect(true);
			}
			startSwitchAnim();
		} else {
			// we fake the animation is end.
			fakeSwitchAnimEnd();
		}
	}
	
	/* package */ void notifyChangePage() {
		mCurrentPageIndex = mTargetPageIndex;
		if (DEBUG) LogUtils.d(TAG, "change to page: " + mCurrentPageIndex);
		
		if (null != mListener) {
			mListener.onEffectPageChangePage(this, mCurrentPageIndex);
		}
	}
	
	/* package */ void notifyChangeState() {
		if (DEBUG) LogUtils.d(TAG, "change to state: " + getCurrentState());
		if (null != mListener) {
			mListener.onEffectPageChangeState(getCurrentState());
		}
	}
	
	private int getCurrentState() {
		if (null == mStateMachine) {
			return STATE_IDLE;
		}
		
		String stateName = mStateMachine.getCurrentStateName();
		if (SScrollToPrev.NAME.equals(stateName)) {
			return STATE_SCROLL_TO_PREV;
		} else if (SAlignFromPrev.NAME.equals(stateName)) {
			return STATE_ALIGN_FROM_PREV;
		} else if (SFlingToPrev.NAME.equals(stateName)) {
			return STATE_FLING_TO_PREV;
		} else if (SScrollToNext.NAME.equals(stateName)) {
			return STATE_SCROLL_TO_NEXT;
		} else if (SAlignFromNext.NAME.equals(stateName)) {
			return STATE_ALIGN_FROM_NEXT;
		} else if (SFlingToNext.NAME.equals(stateName)) {
			return STATE_FLING_TO_NEXT;
		} else {
			return STATE_IDLE;
		}
	}
	
	private void cacheView(View view) {
		if (null == view) {
			return;
		}
		
		//if (mViewPool.size() >= mViewPoolSize) {
		//	return;
		//}
		
		mViewPool.offer(view);
	}
	
	private View getCacheView() {
		if (null == mViewPool) {
			return null;
		}
		
		return mViewPool.poll();
	}
	
	private View viewFromAdapter(int position) {
		if (null == mAdapter) {
			LogUtils.d(TAG, "Adatper is null, you should call setAdapter to set first !!");
			return null;
		}
		
		View cacheView = getCacheView();
		View view = mAdapter.getView(position, cacheView, this);
		
		if (null == view) {
			return null;
		}
		
		if (!view.equals(cacheView)) {
			cacheView(cacheView);
		}
		
		addAdapterView(view, view.equals(cacheView));
		return view;
		
		//View view = mAdapter.getView(position, oldView, this);
		//if (null == oldView) {
		//	LayoutParams params = getReuseLayoutParams(view);
		//	addViewInLayout(view, -1, params, true);
		//}
		//return view;
	}
	
	private void addAdapterView(View view, boolean cacheView) {
		LayoutParams params = getReuseLayoutParams(view);
		if (cacheView) {
			attachViewToParent(view, -1, params);
		} else {
			//requestLayout();
			//invalidate();
			addViewInLayout(view, -1, params, true);
		}
		// we need layout immediately.
		layoutChildImmediately(view);
	}
	
	private void removeEffectView() {
		if (null == mEffectView) {
			return;
		}
		
		mEffectView.clearAnimation();
		removeViewInLayout(mEffectView);
	}
	
	private void removeAdapterView(View view) {
		if (null == view) {
			return;
		}
		
		view.clearAnimation();
		if (-1 != indexOfChild(view)) {
			detachViewFromParent(view);
			cacheView(view);
		}
	}
	
	private LayoutParams getReuseLayoutParams(View view) {
		if (null != view.getLayoutParams()) {
			return view.getLayoutParams();
		}
		
		return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.MATCH_PARENT);
	}
	
	private void layoutChildImmediately(View childView) {
		if (null == childView) {
			return;
		}
		
		int width = getWidth();
		int height = getHeight();
		
        int childWidth = width - getPaddingLeft() - getPaddingRight();
        int childHeight = height - getPaddingTop() - getPaddingBottom();
        
    	childView.measure(
    			MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), 
    			MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
		
        childView.layout(
        		0 + getPaddingLeft(),
        		0 + getPaddingTop(),
        		0 + getPaddingLeft() + childWidth,
        		0 + getPaddingTop() + childHeight);
	}
	
	private boolean isShow(View view) {
		if (null == view) {
			return false;
		}
		return (VISIBLE == view.getVisibility());
	}
	
	private void showView(View view, boolean show) {
		if (null == view) {
			return;
		}
		
		if (show && !isShow(view)) {
			if (isGL() && !view.equals(mEffectView)) {
				view.bringToFront();
			}
			view.setVisibility(VISIBLE);
		} else if (!show && isShow(view)) {
			if (isGL() && view.equals(mEffectView)) {
				return;
			}
			view.setVisibility(INVISIBLE);
		}
	}
	
	private void setupAnimProperty(float src, float dst) {
		float delta = Math.abs(dst - src);
		long duration = (long)(delta * mAnimDuration);
		mSwitchAnim.setDuration(duration);
		if (duration >= DEFAULT_SWITCH_DURATION) {
			mSwitchAnim.setInterpolator(mAnimInterpolator);
		} else {
			mSwitchAnim.setInterpolator(mLinearInterpolator);
		}
	}
	
	private synchronized void startSwitchAnim() {
		mAnimCancelled = false;
		mSwitchAnim.start();
	}
	
	/* package */ synchronized void cancelSwitchAnim() {
		mAnimCancelled = true;
		mSwitchAnim.cancel();
	}
	
	private synchronized void fakeSwitchAnimEnd() {
		mAnimCancelled = false;
		onAnimationEnd(mSwitchAnim);
	}
	
	private void updatePageFactorByAnim() {
		if (null == mIEffectView) {
			return;
		}
		
		mPageFactor = mIEffectView.getEffectFactor();
		if (mIEffectView.isReverseEffect()) {
			mPageFactor = -mPageFactor;
		}
	}
	
	private class AdapterDataSetObserver extends DataSetObserver {
		
		public AdapterDataSetObserver() {
		}
		
		@Override
		public void onChanged() {
			onDataChanged();
		}
		
		@Override
		public void onInvalidated() {
			onDataChanged();
		}
		
		private void onDataChanged() {
			if (null == mAdapter) {
				LogUtils.d(TAG, "Adatper is null, you should call setAdapter to set first !!");
				return;
			}
			
			// re-calculate current page.
			int totalPage = getTotalPage();
			if (totalPage <= 0) {
				resetPageToInit();
				return;
			}
			
			if (mCurrentPageIndex < 0) {
				mCurrentPageIndex = 0;
			} else {
				mCurrentPageIndex = Math.min(mCurrentPageIndex, totalPage - 1);
			}
			
			int state = getCurrentState();
			if (STATE_IDLE == state) {
				// if we are in idle, force update page directly.
				showPage(mCurrentPageIndex, false, true);
			} else {
				// when show effect, we update page carefully.
				if (STATE_SCROLL_TO_PREV == state || STATE_ALIGN_FROM_PREV == state 
						|| STATE_FLING_TO_PREV == state) {
					calcTargetPageIndex(mCurrentPageIndex - 1);
					updateEffectRes(false);
				} else {
					calcTargetPageIndex(mCurrentPageIndex + 1);
					updateEffectRes(true);
				}
				showEffect(true);
				if (null != mEffectView) {
					mEffectView.invalidate();
				}
			}
		}
	}
	
	/* package */ void debugShowCurrentState(String prefix) {
		if (DEBUG) {
			int srcIndex = -1;
			int dstIndex = -1;
			try {
				srcIndex = (Integer)mSrcView.getTag();
			} catch (Exception e) {
				srcIndex = -1;
			}
			try {
				dstIndex = (Integer)mDstView.getTag();
			} catch (Exception e) {
				dstIndex = -1;
			}
			LogUtils.d(TAG, prefix + ", pageState: " + mStateMachine.getCurrentStateName() 
				+ ", currentPage: " + mCurrentPageIndex + ", targetPage: " + mTargetPageIndex 
				+ ", pageFactor: " + mPageFactor
				+ ", srcIndex: " + srcIndex + ", dstIndex: " + dstIndex);
		}
	}
	
}
