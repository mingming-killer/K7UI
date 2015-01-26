package com.eebbk.mingming.k7ui;

import com.eebbk.mingming.k7ui.R;
import com.eebbk.mingming.k7utils.LogUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * 
 * Provide a container which can smooth scroll.
 * Base android 4.1 launcher PageView. </br>
 * 
 * <br/>
 * TODO: now is only support horizontal scroll, 
 *   it's may to implement vertical scroll.
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class ScrollContainer extends ViewGroup implements ViewGroup.OnHierarchyChangeListener {

	private final static String TAG = "ScrollContainer";
	
	protected final static boolean DEBUG = true;
	
	protected final static int INVALID_POINTER = -1;
	protected final static int INVALID_CHILD_INDEX = -1;
	
	protected final static boolean DEFAULT_SHOW_FPS = false;
	protected final static boolean DEFAULT_ALLOW_OVER_SCROLL = true;
	
	protected final static int DEFAULT_PAGE_CAPACITY = 1;
	protected final static int DEFAULT_FLIP_CHILD_NUM = 1;
	protected final static int DEFAULT_SPACE_CHILD = 0;
	
	protected final static int ONE_SECOND = 1000;
	protected final static int FPS_COLOR = 0xffff00ff;
	protected final static int FPS_SIZE_FACTOR = 20;
	
	// The page is moved more than halfway, 
	// automatically move to the next page on touch up.
	protected final static float SIGNIFICANT_MOVE_THRESHOLD = 0.4f;
    
	protected static final float RETURN_TO_ORIGINAL_INDEX_THRESHOLD = 0.33f;
    
	protected final static float OVERSCROLL_ACCELERATE_FACTOR = 2;
	protected final static float OVERSCROLL_DAMP_FACTOR = 0.14f;
	
    // the min drag distance for a fling to register, 
    // to prevent random page shifts 
	protected final static int MIN_LENGTH_FOR_FLING = 25;

    protected final static int PAGE_SNAP_ANIMATION_DURATION = 550;
    protected final static int SLOW_PAGE_SNAP_ANIMATION_DURATION = 950;
    protected final static float NANOTIME_DIV = 1000000000.0f;
    
    // The following constants need to be scaled based on density. 
    // The scaled versions will be assigned to the corresponding member variables below.
    protected final static float FLING_RANGE_FACTOR = 0.2f;
    protected final static int FLING_THRESHOLD_VELOCITY = 500;
    protected final static int MIN_SNAP_VELOCITY = 1500;
    protected final static int MIN_FLING_VELOCITY = 250;
    
    protected final static float FLING_THRESHOLD_FACTOR = 0.5f;
	
    // touch states 
    protected final static int TOUCH_STATE_REST = 0;
    protected final static int TOUCH_STATE_SCROLLING = 1;
    
	// scroll object
	protected Scroller mScroller;
	protected VelocityTracker mVelocityTracker;
	
	protected float mFlingRangeFactor;
	protected int mFlingThresholdVelocity;
    protected int mMinFlingVelocity;
    protected int mMinSnapVelocity;
    
    protected float mDensity;
    protected float mSmoothingTime;
    protected float mTouchX;
    
    // mOverScrollX is equal to getScrollX() when we're within the normal scroll range. Otherwise
    // it is equal to the scaled overscroll position. We use a separate value so as to prevent
    // the screens from continuing to translate beyond the normal bounds.
    protected int mOverScrollX;
    
    protected int mMinScrollX;
    protected int mMaxScrollX;
    protected int mUnboundedScrollX;
    
    protected boolean mIsContainerMoving = false;
    
    protected float mLastMotionX;
    protected float mLastMotionXRemainder;
    protected float mLastMotionY;
    protected float mTotalMotionX;
    
    protected int mActivePointerId = INVALID_POINTER;
    
    protected int mTouchState = TOUCH_STATE_REST;
    protected int mTouchSlop;
    protected int mPagingTouchSlop;
    protected int mMaximumVelocity;
    protected float mDownMotionX;
	
	protected int mChildWidth;
	
	// for debug show FPS when scroll
	protected long mTimeCount;
	protected int mFrameCount;
	//protected Paint mPaint;
	protected TextView mAddInfoView;
	
	protected ScrollFlipListener mScrollFlipListener;
    
    /** 
     * If true, use a different slop parameter (pagingTouchSlop = 2 * touchSlop) 
     * for deciding to switch to a new page
     */
    protected boolean mUsePagingTouchSlop = true;
    
    /** 
     * If true, the subclass should directly update scrollX itself 
     * in its computeScroll method
     */
    protected boolean mDeferScrollUpdate = false;
    
    
    /**
     * ==============================================
     * XML Attributes
     * ==============================================
     */
    
	/** First visible child index. */
	protected int mFirstVisibleChildIndex;
	
	/** First visible child index which next flip to */
	protected int mNextFirstVisibleChildIndex;
	
	/** 
	 * One page contain how many child view,
	 * For example, if you want a page view scroll effect, set it to 1. 
	 */
	protected int mPageCapacity;
	
	/**
	 * One flip child number, set it to -1 is flip according to user scroll velocity and scroll range.
	 * for example, if you want to page view scroll effect, set it to 1.
	 */
	protected int mFlipChildNum;
	
	/** Child space. */
	protected int mSpaceChild;
	
	/** Whether allow user over scroll. */
    protected boolean mAllowOverScroll;
    
    /** 
     * True on the FPS float pop window. 
     * Notices: only turn on one container fps window.
     */
    protected boolean mShowFPS;
	
    /**
     * ==============================================
     * ==============================================
     */
	
	/**
	 * 
	 * Flip page listener.
	 * 
	 * @author humingming <humingming@oaserver.dw.gdbbk.com>
	 *
	 */
    public interface ScrollFlipListener {
    	
    	/**
    	 * Flip page is occurred.
    	 * 
    	 * @param container The {@link ScrollContainer} object.
    	 * @param newFirstVisibleView First visible view object.
    	 * @param newFirstVisibleIndex First visible view index.
    	 */
        void onScrollFlip(View container, View newFirstVisibleView, int newFirstVisibleIndex);
        
    }
    
    
    public ScrollContainer(Context context) {
		this(context, null);
	}

	public ScrollContainer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ScrollContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollContainer, defStyle, 0);
        
        mShowFPS = a.getBoolean(R.styleable.ScrollContainer_k7uiscShowFPS, DEFAULT_SHOW_FPS);
        mAllowOverScroll = a.getBoolean(R.styleable.ScrollContainer_k7uiscAllowOverScroll, DEFAULT_ALLOW_OVER_SCROLL);
        
        mPageCapacity = a.getInteger(R.styleable.ScrollContainer_k7uiscPageCapacity, DEFAULT_PAGE_CAPACITY);
        mFlipChildNum = a.getInteger(R.styleable.ScrollContainer_k7uiscFlipChildNum, DEFAULT_FLIP_CHILD_NUM);
  
        mSpaceChild = (int) a.getDimension(R.styleable.ScrollContainer_k7uiscSpaceChild, DEFAULT_SPACE_CHILD);
        
        if (mPageCapacity <= 0) {		
        	mPageCapacity = DEFAULT_PAGE_CAPACITY;
        }
        
        a.recycle();
        
		// TODO: when to show not only FPS, May always need to create the
		// additional information view.
		if (mShowFPS) {
			createAddInfoView(context);
		}
        
		init();
	}
	
    /**
     * Initializes various states for this workspace.
     */
    protected void init() {
        mScroller = new Scroller(getContext(), new ScrollInterpolator());

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mPagingTouchSlop = configuration.getScaledPagingTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mDensity = getResources().getDisplayMetrics().density;
        
        mTouchState = TOUCH_STATE_REST;

        mFlingThresholdVelocity = (int) (FLING_THRESHOLD_VELOCITY * mDensity);
        mMinFlingVelocity = (int) (MIN_FLING_VELOCITY * mDensity);
        mMinSnapVelocity = (int) (MIN_SNAP_VELOCITY * mDensity);
        mFlingRangeFactor = FLING_RANGE_FACTOR * mDensity;
        
		mFirstVisibleChildIndex = 0;
		mNextFirstVisibleChildIndex = INVALID_CHILD_INDEX;
		
		mChildWidth = 0;
		
		//mPaint = new Paint();
		
		if (DEBUG) {
        	LogUtils.d(TAG, "Density: " + mDensity + " mMinFlingVelocity: " + mMinFlingVelocity 
        			+ " mMinSnapVelocity: " + mMinSnapVelocity + " mFlingRangeFactor: " + mFlingRangeFactor);
        }
    }
    
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		if (mShowFPS) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - mTimeCount >= ONE_SECOND) {
				mTimeCount = currentTime;

				//mPaint.setColor(FPS_COLOR);
				//mPaint.setTextSize(FPS_SIZE_VELOCITY * mDensity);
				//LogUtils.d(TAG, "scrollX: " + mScroller.getCurrX() + " currX: " + getScrollX());
				//canvas.drawText(mFrameCount + " FPS", getScrollX(), 10, mPaint);
				
				if (null != mAddInfoView) {
					mAddInfoView.setText(mFrameCount + " FPS");
				}
				
				mFrameCount = 0;
			} else {
				++mFrameCount;
			}
		}
	}
	
    
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		//if (widthMode != MeasureSpec.EXACTLY) {
		//	throw new IllegalStateException("ScrollContainer only can run at EXACTLY mode!");
		//}
		//if (heightMode != MeasureSpec.EXACTLY) {
		//	throw new IllegalStateException("ScrollContainer only can run at EXACTLY mode!");
		//}
		
		if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("ScrollContainer cannot have UNSPECIFIED dimensions");
        }
        
        int validWidth = width - getPaddingLeft() - getPaddingRight();
        int validHeight = height - getPaddingBottom() - getPaddingTop();
        
        int validChildWidth = validWidth - (mSpaceChild * (mPageCapacity - 1));
        //int itemTotalHeight = validHeight - (mSpaceY * (mCountY - 1));
        
        //int childWidth = validChildWidth / mPageCapacity;
        mChildWidth = validChildWidth / mPageCapacity;;
        int childHeight = validHeight;
        
        int childCount = getChildCount();
        int totalChildWidth = (mChildWidth * childCount) + (mSpaceChild * (childCount - 1))
        		+ getPaddingLeft() + getPaddingRight();
		
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mChildWidth, MeasureSpec.EXACTLY);
            int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
		}
		
		// compute min and max over scroll range.
		mMinScrollX = 0;
		mMaxScrollX = 0;
		
		//LogUtils.d(TAG, "childWidth: " + mChildWidth + " childCount: " + childCount 
		//		+ " space: " + mSpaceChild + " paddingLeft: " + getPaddingLeft() + " paddingRight: " + getPaddingRight());
		
        if (childCount > 0) {
            mMaxScrollX = Math.max(0, totalChildWidth - width);
        }
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {		
        int width = r - l;
        int height = b - t;
        
        int validWidth = width - getPaddingLeft() - getPaddingRight();
        int validHeight = height - getPaddingBottom() - getPaddingTop();
        
        int childTotalWidth = validWidth - (mSpaceChild * (mPageCapacity - 1));
        //int itemTotalHeight = validHeight - (mSpaceY * (mCountY - 1));
        
        int childWidth = childTotalWidth / mPageCapacity;
        int childHeight = validHeight;

        int childBaseLeft = 0 + getPaddingLeft(); // l + getPaddingLeft();
        int childBaseTop = 0 + getPaddingTop();   // t + getPaddingTop();
        
        int childLeft = childBaseLeft;
        int childTop = childBaseTop;
        
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            
            childLeft = childBaseLeft + ((childWidth + mSpaceChild) * i); 
            childTop = childBaseTop;
            
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        }
	}
	
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // This method JUST determines whether we want to intercept the motion.
        // If we return true, onTouchEvent will be called and we do the actual
        // scrolling there.
        acquireVelocityTrackerAndAddMovement(ev);

        // Skip touch handling if there are no pages to swipe
        if (getChildCount() <= 0) {
        	return super.onInterceptTouchEvent(ev);
        }

        // Shortcut the most recurring case: the user is in the dragging
        // state and he is moving his finger. We want to intercept this motion.
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) &&
                (mTouchState == TOUCH_STATE_SCROLLING)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                // mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                // whether the user has moved far enough from his original down touch.
                if (mActivePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev);
                    //LogUtils.d(TAG, "intercept Touch move touch state: " + mTouchState);
                    break;
                }
                
                // if mActivePointerId is INVALID_POINTER, then we must have missed an ACTION_DOWN 
                // event. in that case, treat the first occurence of a move event as a ACTION_DOWN 
                // i.e. fall through to the next case (don't break) 
                // (We sometimes miss ACTION_DOWN events in Workspace because it ignores all events 
                // while it's small- this was causing a crash before we checked for INVALID_POINTER)
                
                //LogUtils.d(TAG, "intercept Touch move missed touch state: " + mTouchState);
            }

            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                
                // Remember location of down touch
                mDownMotionX = x;
                mLastMotionX = x;
                mLastMotionY = y;
                mLastMotionXRemainder = 0;
                mTotalMotionX = 0;
                mActivePointerId = ev.getPointerId(0);
                //mAllowLongPress = true;

                // If being flinged and user touches the screen, initiate drag;
                // otherwise don't. mScroller.isFinished should be false when
                // being flinged.
                final int xDist = Math.abs(mScroller.getFinalX() - mScroller.getCurrX());
                final boolean finishedScrolling = (mScroller.isFinished() || xDist < mTouchSlop);
                if (finishedScrolling) {
                    mTouchState = TOUCH_STATE_REST;
                    mScroller.abortAnimation();
                } else {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                //LogUtils.d(TAG, "intercept Touch down touch state: " + mTouchState);
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                //mAllowLongPress = false;
                mActivePointerId = INVALID_POINTER;
                releaseVelocityTracker();
                //LogUtils.d(TAG, "intercept Touch up touch state: " + mTouchState);
                break;

            case MotionEvent.ACTION_POINTER_UP:
            	//LogUtils.d(TAG, "intercept Touch pointer up touch state: " + mTouchState);
                onSecondaryPointerUp(ev);
                releaseVelocityTracker();
                break;
        }

        // The only time we want to intercept motion events is 
        // if we are in the drag mode.
        return (mTouchState != TOUCH_STATE_REST);
    }
	
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	//LogUtils.d(TAG, "onTouchEvent " + (ev.getAction()& MotionEvent.ACTION_MASK) + " touch state: " + mTouchState);
    	
        // Skip touch handling if there are no pages to swipe
        if (getChildCount() <= 0) {
        	return super.onTouchEvent(ev);
        }
        
        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
        	onTouchDownEvent(ev);
            break;

        case MotionEvent.ACTION_MOVE:
        	onTouchMoveEvent(ev);
            break;

        case MotionEvent.ACTION_UP:
        	onTouchUpEvent(ev);
            break;

        case MotionEvent.ACTION_CANCEL:
        	onTouchCancelEvent(ev);
            break;

        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            break;
        }

        return true;
    }
    
    private void onTouchDownEvent(MotionEvent ev) {
        // If being flinged and user touches, stop the fling. isFinished
        // will be false if being flinged.
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        // Remember where the motion event started
        mDownMotionX = mLastMotionX = ev.getX();
        mLastMotionXRemainder = 0;
        mTotalMotionX = 0;
        mActivePointerId = ev.getPointerId(0);
        
        if (mTouchState == TOUCH_STATE_SCROLLING) {
            containerBeginMoving();
        }
        
        //LogUtils.d(TAG, "Touch down touch state: " + mTouchState);
    }
    
    private void onTouchMoveEvent(MotionEvent ev) {
    	//LogUtils.d(TAG, "Touch move touch state: " + mTouchState);
        if (mTouchState == TOUCH_STATE_SCROLLING) {
            // Scroll to follow the motion event
            final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            final float x = ev.getX(pointerIndex);
            final float deltaX = mLastMotionX + mLastMotionXRemainder - x;

            mTotalMotionX += Math.abs(deltaX);

            // Only scroll and update mLastMotionX if we have moved some discrete amount.  We
            // keep the remainder because we are actually testing if we've moved from the last
            // scrolled position (which is discrete).
            if (Math.abs(deltaX) >= 1.0f) {
                mTouchX += deltaX;
                mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                if (!mDeferScrollUpdate) {
                    scrollBy((int) deltaX, 0);
                } else {
                    invalidate();
                }
                mLastMotionX = x;
                mLastMotionXRemainder = deltaX - (int) deltaX;
            } else {
                awakenScrollBars();
            }
        } else {
            determineScrollingStart(ev);
        }
        //LogUtils.d(TAG, "Touch move after touch state: " + mTouchState);
    }
    
    private void onTouchUpEvent(MotionEvent ev) {
    	//LogUtils.d(TAG, "Touch up touch state: " + mTouchState);
 
        final int activePointerId = mActivePointerId;
        final int pointerIndex = ev.findPointerIndex(activePointerId);
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(ONE_SECOND, mMaximumVelocity);
        
        final float x = ev.getX(pointerIndex);
        int velocityX = (int) velocityTracker.getXVelocity(activePointerId);
        
        final int deltaX = (int) (x - mDownMotionX);
        
        final int pageWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
        boolean isSignificantMove = Math.abs(deltaX) > (pageWidth * SIGNIFICANT_MOVE_THRESHOLD);
        
        int finalChild;
        int flipChildNum;
    	
        if (mTouchState == TOUCH_STATE_SCROLLING) {
        	
            mTotalMotionX += Math.abs(mLastMotionX + mLastMotionXRemainder - x);
            boolean isFling = mTotalMotionX > MIN_LENGTH_FOR_FLING &&
                    Math.abs(velocityX) > mFlingThresholdVelocity;

            // In the case that the page is moved far to one direction and then is flung
            // in the opposite direction, we use a threshold to determine whether we should
            // just return to the starting page, or if we should skip one further.
            boolean returnToOriginalIndex = false;
            if (Math.abs(deltaX) > pageWidth * RETURN_TO_ORIGINAL_INDEX_THRESHOLD &&
                    Math.signum(velocityX) != Math.signum(deltaX) && isFling) {
                returnToOriginalIndex = true;
            }
            
            // We give flings precedence over large moves, which is why we short-circuit our
            // test for a large move if a fling has been registered. That is, a large
            // move to the left and fling to the right will register as a fling to the right.
            if ((isFling && velocityX > 0) && (mFirstVisibleChildIndex > 0)) {
            	
            	// flip according to scroll velocity or distance
            	if (mFlipChildNum <= -1) {
            		// if user significant scroll a long distance,
            		// we use distance as flip parameter. 
            		if (isSignificantMove) {
            			flipChildNum = computeScrollChildNum(deltaX);
            		} else {
            			flipChildNum = computeScrollChildNumByVelocity(velocityX);
            		}
            	} else {
            		// manual set flip number.
            		flipChildNum = mFlipChildNum;
            	}
            	
            	finalChild = returnToOriginalIndex ? mFirstVisibleChildIndex : mFirstVisibleChildIndex - flipChildNum;
            	if (DEBUG) {
            		LogUtils.d(TAG, "Touch up scroll to prev: state: " + mTouchState + " final child: " + finalChild 
            				+ " returned: " + returnToOriginalIndex + " first: " + mFirstVisibleChildIndex 
            				+ " flip child num: " + flipChildNum);
            	}
                snapToChildWithVelocity(finalChild, velocityX);
                
            } else if ((isFling && velocityX < 0) && 
            		(mFirstVisibleChildIndex < getMaxSnapIndex())) {
            	
            	if (mFlipChildNum <= -1) {
            		if (isSignificantMove) {
            			flipChildNum = computeScrollChildNum(deltaX);
            		} else {
            			flipChildNum = computeScrollChildNumByVelocity(velocityX);
            		}
            	} else {
            		flipChildNum = mFlipChildNum;
            	}
            	
            	finalChild = returnToOriginalIndex ? mFirstVisibleChildIndex : mFirstVisibleChildIndex + flipChildNum;
            	if (DEBUG) {
            		LogUtils.d(TAG, "Touch up scroll to next: state: " + mTouchState + " final child: " + finalChild 
            				+ " returned: " + returnToOriginalIndex + " first: " + mFirstVisibleChildIndex 
            				+ " flip child num: " + flipChildNum);
            	}
                snapToChildWithVelocity(finalChild, velocityX);
                
            } else {
            	finalChild = findNearestChild(getScrollX());
            	if (DEBUG) {
            		LogUtils.d(TAG, "Alignment to index: " + finalChild + " velocityX: " + velocityX);
            	}
            	snapToChildWithVelocity(finalChild, velocityX);
            }
            
        } else {
            onUnhandledTap(ev);
        }
        
        mTouchState = TOUCH_STATE_REST;
        mActivePointerId = INVALID_POINTER;
        releaseVelocityTracker();
    }
    
    private void onTouchCancelEvent(MotionEvent ev) {
        if (mTouchState == TOUCH_STATE_SCROLLING) {
        	if (DEBUG) {
        		LogUtils.d(TAG, "Touch cancel snapToDestination: state: " + mTouchState + " first " + mFirstVisibleChildIndex 
        				+ " next: " + mNextFirstVisibleChildIndex);
        	}
            snapToDestination();
        }
        mTouchState = TOUCH_STATE_REST;
        mActivePointerId = INVALID_POINTER;
        releaseVelocityTracker();
    }
    
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new 
            // active pointer and adjust accordingly. 
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = mDownMotionX = ev.getX(newPointerIndex);
            mLastMotionY = ev.getY(newPointerIndex);
            mLastMotionXRemainder = 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }
    
    @Override
    public void computeScroll() {
        computeScrollHelper();
    }
    
    @Override
    public void scrollBy(int x, int y) {
    	//LogUtils.d(TAG, "scrollBy mUnboundedScrollX: " + mUnboundedScrollX + " x: " + x);
        scrollTo(mUnboundedScrollX + x, getScrollY() + y);
    }

    @Override
    public void scrollTo(int x, int y) {
        mUnboundedScrollX = x;
        
        //LogUtils.d(TAG, "scrollTo mUnboundedScrollX: " + mUnboundedScrollX 
        //		+ " minScroll: " + mMinScrollX + " maxSroll: " + mMaxScrollX);
        
        if (x < mMinScrollX) {
            super.scrollTo(0, y);
            if (mAllowOverScroll) {
                overScroll(x);
            }
        } else if (x > mMaxScrollX) {
            super.scrollTo(mMaxScrollX, y);
            if (mAllowOverScroll) {
                overScroll(x - mMaxScrollX);
            }
        } else {
            mOverScrollX = x;
            super.scrollTo(x, y);
        }

        mTouchX = x;
        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
    }
	
    @Override
    public void onChildViewAdded(View parent, View child) {
        // This ensures that when children are added, they get the correct transforms / alphas 
        // in accordance with any scroll effects.
        //mForceScreenScrolled = true;
        invalidate();
        //invalidateCachedOffsets();
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
    	
    }
	
    /**
     * Get first visible child index.
     * When page capacity is 1, it's means current page index.
     * 
     * @return First visible child index.
     */
	public int getFirstVisibleChildIndex() {
		return mFirstVisibleChildIndex;
	}
	
    /**
     * If you set page capacity to 1, 
     * this will return current page index.
     * Otherwise it not correct, don't use it.
     * 
     * @return Current page.
     */
    public int getCurrentPage() {
    	return (mFirstVisibleChildIndex / mPageCapacity);
    }
	
	/**
	 * Get page capacity.
	 * 
	 * @return Page capacity
	 */
	public int getPageCapacity() {
		return mPageCapacity;
	}
	
	/**
	 * Get flip child number.
	 * 
	 * @return Flip child number
	 */
	public int getFilpChildNum() {
		return mFlipChildNum;
	}
	
	/**
	 * Get space child.
	 * 
	 * @return Space child.
	 */
	public int getSpaceChild() {
		return mSpaceChild;
	}
	
	/**
	 * Whether allow over scroll.
	 * 
	 * @return True means allow, false not.
	 */
	public boolean allowOverScroll() {
		return mAllowOverScroll;
	}
	
	/**
	 * Get give child view index.
	 * 
	 * @param childView Child view.
	 * @return Child view index, -1 if this child is not in container.
	 */
	public int getChildIndex(View childView) {
		int index = -1;
		
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child.equals(childView)) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	/**
	 * Get give child index view.
	 * 
	 * @param childIndex Child index.
	 * @return Child view, null if not have this child.
	 */
	public View getChildView(int childIndex) {
		return getChildAt(childIndex);
	}
	
	/**
	 * Set scroll flip listener.
	 * 
	 * @param scrollFlipListener Scroll flip listener.
	 */
	public void setScrollFlipListener(ScrollFlipListener scrollFlipListener) {
		mScrollFlipListener = scrollFlipListener;
		notifyScrollFlipListener();
	}
	
	/** 
	 * Set page capacity.
	 * 
	 * @param pageCapacity Page capacity.
	 */
	public void setPagecapacity(int pageCapacity) {
		if (pageCapacity == mPageCapacity) {
			return;
		}
		
		mPageCapacity = pageCapacity;
		
		requestLayout();
		invalidate();
	}
	
	/**
	 * Set first visible child index.
	 * It's can use in initialization scroll container.
	 * 
	 * @param childIndex First visible child index.
	 */
	public void setFirstVisibleChild(int childIndex) {
		snapToChild(childIndex);
	}
	
	/** 
	 * According to the position of current layout 
	 * scroll to the destination page. 
	 */
	protected void snapToDestination() {
		LogUtils.d(TAG, "snapToDestination: " + mNextFirstVisibleChildIndex);
		snapToChild(mNextFirstVisibleChildIndex, PAGE_SNAP_ANIMATION_DURATION);
	}
	
	/**
	 * Scroll to give child with give velocity.
	 * The velocity is use by compute scroll animation duration.
	 * 
	 * @param whichChild Snap child index.
	 * @param velocity velocity.
	 */
    public void snapToChildWithVelocity(int whichChild, int velocity) {
    	whichChild = Math.max(0, Math.min(whichChild, getMaxSnapIndex()));
        int halfScreenSize = getMeasuredWidth() / 2;
        
        final int newX = getChildAt(whichChild).getLeft();
        int delta = newX - mUnboundedScrollX;
        int duration = 0;
        
        if (DEBUG) {
        	LogUtils.d(TAG, "newX: " + newX + " mUnboundedScrollX: " + mUnboundedScrollX 
        			+ " delta: " + delta + " velocity: " + velocity + " mMinFlingVelocity: " + mMinFlingVelocity
        			+ " halfScreenSize: " + halfScreenSize);
        }

        // If the velocity is low enough, then treat this more as an automatic page advance 
        // as opposed to an apparent physical response to flinging
        if (Math.abs(velocity) < mMinFlingVelocity) {    
            snapToChild(whichChild, PAGE_SNAP_ANIMATION_DURATION);
            return;
        }

        // Here we compute a "distance" that will be used in the computation of the overall 
        // snap duration. This is a function of the actual distance that needs to be traveled; 
        // we keep this value close to half screen size in order to reduce the variance in snap 
        // duration as a function of the distance the page needs to travel.
        float distanceRatio = Math.min(1f, 1.0f * Math.abs(delta) / (2 * halfScreenSize));
        float distance = halfScreenSize + halfScreenSize *
                distanceInfluenceForSnapDuration(distanceRatio);

        velocity = Math.abs(velocity);
        velocity = Math.max(mMinSnapVelocity, velocity);

        // we want the page's snap velocity to approximately match the velocity at which the
        // user flings, so we scale the duration by a value near to the derivative of the scroll
        // interpolator at zero, ie. 5. We use 4 to make it a little slower.
        duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        
        if (DEBUG) {
        	LogUtils.d(TAG, "distanceRatio: " + distanceRatio + " distance: " + distance 
        			+ " duration: " + duration);
        }

        snapToChild(whichChild, delta, duration);
    }
    
    /**
     * Scroll to give child with default {@link PAGE_SNAP_ANIMATION_DURATION} duration.
     * 
     * @param whichChild Snap child index.
     */
    public void snapToChild(int whichChild) {
        snapToChild(whichChild, PAGE_SNAP_ANIMATION_DURATION);
    }
	
    /**
     * Scroll to give child with give duration.
     * 
     * @param whichChild Snap child index.
     * @param duration Snap animation duration time.
     */
    public void snapToChild(int whichChild, int duration) {
		LogUtils.d(TAG, "snapToChild: child: " + whichChild + " duration: " + duration);
		whichChild = Math.max(0, Math.min(whichChild, getMaxSnapIndex()));
		
	    int newX = getChildAt(whichChild).getLeft();
	    final int sX = mUnboundedScrollX;
	    final int delta = newX - sX;
	    snapToChild(whichChild, delta, duration);
	}

	/**
	 * Scroll to give child with scroll range with give duration.
	 * Usually the when you know target child index, you should use
	 * {@link #snapToChild(int, int)} let it compute scroll range.
	 * So don't use this method directly.
	 * 
	 * @param whichChild Snap child index.
	 * @param delta Snap distance.
	 * @param duration Snap animation duration time.
	 */
    protected void snapToChild(int whichChild, int delta, int duration) {
		LogUtils.d(TAG, "snapToChild-final: child: " + whichChild 
				+ " delta: " + delta + " duration: " + duration);
		
		mNextFirstVisibleChildIndex = whichChild;

	    View focusedChild = getFocusedChild();
	    if (focusedChild != null && whichChild != mFirstVisibleChildIndex && 
	    		focusedChild == getChildAt(mFirstVisibleChildIndex)) {
	        focusedChild.clearFocus();
	    }

	    containerBeginMoving();
	    awakenScrollBars(duration);
	    if (0 == duration) {
	    	duration = Math.abs(delta);
	    }

	    if (!mScroller.isFinished()) {
	    	mScroller.abortAnimation();
	    }
	    
	    mScroller.startScroll(mUnboundedScrollX, 0, delta, 0, duration);

	   	notifyScrollFlipListener();
	    invalidate();
	}
	
    /**
     * We moved this functionality to a helper function so 
     * SmoothPagedView can reuse it.
     * 
     * @return True means complete scroll, false not.
     */
    protected boolean computeScrollHelper() {
        if (mScroller.computeScrollOffset()) {
            /** Don't bother scrolling if the page does not need to be moved */
            if (getScrollX() != mScroller.getCurrX()
                || getScrollY() != mScroller.getCurrY()
                || mOverScrollX != mScroller.getCurrX()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            invalidate();
            return true;
            
        } else if (mNextFirstVisibleChildIndex != INVALID_CHILD_INDEX) {
            mFirstVisibleChildIndex = Math.max(0, 
            		Math.min(mNextFirstVisibleChildIndex, getMaxSnapIndex()));
            mNextFirstVisibleChildIndex = INVALID_CHILD_INDEX;
            notifyScrollFlipListener();
            
            if (DEBUG) {
            	LogUtils.d(TAG, "end scroll: first: " + mFirstVisibleChildIndex);
            }

            // We don't want to trigger a page end moving unless the page has settled
            // and the user has stopped scrolling
            if (mTouchState == TOUCH_STATE_REST) {
            	containerEndMoving();
            }
            return true;
        }
        
        return false;
    }
    
    protected void acceleratedOverScroll(float amount) {
        int screenSize = getMeasuredWidth();

        // We want to reach the max over scroll effect when the user has
        // over scrolled half the size of the screen
        float f = OVERSCROLL_ACCELERATE_FACTOR * (amount / screenSize);

        if (f == 0) return;

        // Clamp this factor, f, to -1 < f < 1
        if (Math.abs(f) >= 1) {
            f /= Math.abs(f);
        }

        int overScrollAmount = (int) Math.round(f * screenSize);
        if (amount < 0) {
            mOverScrollX = overScrollAmount;
            super.scrollTo(0, getScrollY());
        } else {
            mOverScrollX = mMaxScrollX + overScrollAmount;
            super.scrollTo(mMaxScrollX, getScrollY());
        }
        invalidate();
    }
    
    protected void dampedOverScroll(float amount) {
        int screenSize = getMeasuredWidth();

        float f = (amount / screenSize);

        if (f == 0) return;
        f = f / (Math.abs(f)) * (overScrollInfluenceCurve(Math.abs(f)));

        // Clamp this factor, f, to -1 < f < 1
        if (Math.abs(f) >= 1) {
            f /= Math.abs(f);
        }

        int overScrollAmount = (int) Math.round(OVERSCROLL_DAMP_FACTOR * f * screenSize);
        if (amount < 0) {
            mOverScrollX = overScrollAmount;
            //super.scrollTo(0, getScrollY());
            super.scrollTo(mOverScrollX, getScrollY());
        } else {
            mOverScrollX = mMaxScrollX + overScrollAmount;
            //super.scrollTo(mMaxScrollX, getScrollY());
            super.scrollTo(mOverScrollX, getScrollY());
        }
        
        invalidate();
    }

    protected void overScroll(float amount) {
    	//LogUtils.d(TAG, "overScroll: " + amount);
        dampedOverScroll(amount);
    }
	
	protected void notifyScrollFlipListener() {
        if (null != mScrollFlipListener) {
        	mScrollFlipListener.onScrollFlip(this,
        			getChildAt(mFirstVisibleChildIndex), mFirstVisibleChildIndex);
        }
    }
	
    protected void containerBeginMoving() {
        if (!mIsContainerMoving) {
        	mIsContainerMoving = true;
        	onContainerBeginMoving();
        }
    }

    protected void containerEndMoving() {
        if (mIsContainerMoving) {
        	mIsContainerMoving = false;
            onContainerEndMoving();
        }
    }

    protected boolean isContainerMoving() {
        return mIsContainerMoving;
    }

    /** 
     * A method that subclasses can override to add behavior
     */
    protected void onContainerBeginMoving() {
    	
    }

    /**
     *  A method that subclasses can override to add behavior
     */
    protected void onContainerEndMoving() {
    	
    }
    
    protected void onUnhandledTap(MotionEvent ev) {
    	
    }

    /**
     * We want the duration of the page snap animation to be influenced by the distance that 
     * the screen has to travel, however, we don't want this duration to be effected in a 
     * purely linear fashion. Instead, we use this method to moderate the effect that the distance 
     * of travel has on the overall snap duration.
     * 
     * @param f
     * @return
     */
    protected float distanceInfluenceForSnapDuration(float f) {
    	// center the values about 0.
        f -= 0.5f;
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }
    
    protected void determineScrollingStart(MotionEvent ev) {
        determineScrollingStart(ev, 1.0f);
    }
    
    /**
     * Determines if we should change the touch state to start scrolling after the
     * user moves their touch point too far.
     */
    protected void determineScrollingStart(MotionEvent ev, float touchSlopScale) {
        // Locally do absolute value. mLastMotionX is set to the y value
        // of the down event.
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex == -1) {
            return;
        }
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);
        final int xDiff = (int) Math.abs(x - mLastMotionX);
        final int yDiff = (int) Math.abs(y - mLastMotionY);

        final int touchSlop = Math.round(touchSlopScale * mTouchSlop);
        boolean xPaged = xDiff > mPagingTouchSlop;
        boolean xMoved = xDiff > touchSlop;
        boolean yMoved = yDiff > touchSlop;

        if (xMoved || xPaged || yMoved) {
            if (mUsePagingTouchSlop ? xPaged : xMoved) {
                // Scroll if the user moved far enough along the X axis
                mTouchState = TOUCH_STATE_SCROLLING;
                mTotalMotionX += Math.abs(mLastMotionX - x);
                mLastMotionX = x;
                mLastMotionXRemainder = 0;
                mTouchX = getScrollX();
                mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                containerBeginMoving();
            }
            
            // Either way, cancel any pending longpress
            //cancelCurrentPageLongPress();
        }
    }
    
    /**
     * According to give velocity compute how many child can be scrolled.
     * In fact, it's compute scroll range though scroll velocity.
     * So see {@link #computeScrollChildNum(int)}.
     * 
     * @param velocity Scroll velocity
     * @return Scrolled child number.
     */
    protected final int computeScrollChildNumByVelocity(float velocity) {
    	int delta = (int) (velocity * mFlingRangeFactor);
    	
    	if (DEBUG) {
    		LogUtils.d(TAG, "velocity: " + velocity + " mFlingRangeFactor: " + mFlingRangeFactor 
    				+ " delta: " + delta);
    	}
    	
    	return computeScrollChildNum(delta);
    }
    
    /**
     * According to give scroll range to compute how many child can be scrolled.
     * 
     * @param delta Scroll distance.
     * @return Scrolled child number.
     */
    protected final int computeScrollChildNum(int delta) {
    	int num = Math.abs(delta) / (mChildWidth + mSpaceChild);
    	int rest = delta % (mChildWidth + mSpaceChild);
    	
    	if (0 != rest) {
    		if (rest >= (int)(mChildWidth * FLING_THRESHOLD_FACTOR)) {
    			num += 1;
    		}
    	}
    	
    	if (DEBUG) {
    		LogUtils.d(TAG, "delta: " + delta + " passed child: " + num);
    	}
    	
    	return num;
    }
    
    /**
     * Find the nearest child with give position.
     * 
     * @param pos Give position.
     * @return Nearest give position child index
     */
    protected final int findNearestChild(int pos) {
    	int index;
    	int space;
    	
    	int nearest = 0;
    	int minSpace = 9999999;
    	
    	View child;
    	
    	for (index = 0; index < getChildCount(); index++) {
    		child = getChildAt(index);
    		space = Math.abs(pos - child.getLeft());
    		
    		if (space < minSpace) {
    			nearest = index;
    			minSpace = space;
    		}
    	}
    	
    	if (DEBUG) {
    		LogUtils.d(TAG, "pos: " + pos + " nearest: " + nearest
    				+ " nearest pos: " + getChildAt(nearest).getLeft());
    	}
    	
    	return nearest;
    }
    
    /** 
     * Only can snap to the last page first child. 
     */
    protected final int getMaxSnapIndex() {
    	return ((getChildCount() - 1) - (mPageCapacity - 1));
    }
    
	protected final void createAddInfoView(Context context) {
		if (null != context) {
			WindowManager manager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);

			WindowManager.LayoutParams params = new WindowManager.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_APPLICATION,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);
			params.gravity = Gravity.TOP;
			mAddInfoView = new TextView(getContext());
			mAddInfoView.setTextColor(FPS_COLOR);
			mAddInfoView.setTextSize(FPS_SIZE_FACTOR * mDensity);
			manager.addView(mAddInfoView, params);
		}
	}
	
    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    
    /**
     * This curve determines how the effect of scrolling over the limits of the page 
     * dimishes as the user pulls further and further from the bounds.
     * 
     * @param f
     * @return
     */
    private float overScrollInfluenceCurve(float f) {
        f -= 1.0f;
        return f * f * f + 1.0f;
    }
    
    private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t*t*t*t*t + 1;
        }
    }
	
}
