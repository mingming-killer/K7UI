package com.eebbk.mingming.k7ui.accessibility;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * 
 * 滑动检测器。 </br>
 * 支持水平滑动和竖直滑动，优先相应子 View 的滑动。
 * 如果子 View 的滑动方向和检测器相反，则能很好的兼容（子 View 一个滑动方向，检测器一个滑动方向）。
 * 如果子 View 的滑动方向和检测器相同，则在子 View 滑动时，检测器放弃滑动。
 * 
 * </br>
 * 代码参考 Android 自带的 {@link ViewPager} 和 {@link GestureDetector} 的滑动处理。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class ScrollDetector {
	
	private final static String TAG = "ScrollDetector";
	private final static boolean DEBUG = false;
	
	/** 滑动类型：水平方向  */
	public final static int HORIZONTAL = 0;
	
	/** 滑动类型：竖直方向 */
	public final static int VERTICAL = 1;
	
    protected static final int INVALID_POINTER = -1;
    protected static final int DEFAULT_GUTTER_SIZE = 16; // dips
	
    protected int mScrollType;
    
    protected int mHostWidth;
    protected int mHostHeight;
    protected View mHostView;
    
    protected int mDefaultGutterSize;
    protected int mGutterSizeH;
    protected int mGutterSizeV;
    
    protected int mTouchSlop;
    protected int mTouchSlopSquare;
    protected int mMinimumFlingVelocity;
    protected int mMaximumFlingVelocity;
    
    protected float mInitialMotionX;
    protected float mInitialMotionY;
    protected float mLastMotionX;
    protected float mLastMotionY;
    protected int mActivePointerId;
    
    /** True 表示 Host View 当前可以拖拽（滑动）；false 相反 */
    protected boolean mIsBeingDragged;
    
    /** True 表示 Host View 当前无法拖拽（滑动），有可能子 View 要滑动；false 相反 */
    protected boolean mIsUnableToDrag;
    
    private PointF mFlingVelocity;
    
    protected MotionEvent mCurrentDownEvent;
    protected VelocityTracker mVelocityTracker;
    
    protected ScrollDetectorListener mListener;
    
    
    /**
     * 
     * 滑动检测器监听器
     * 
     * @author humingming <humingming@oaserver.dw.gdbbk.com>
     *
     */
    public interface ScrollDetectorListener {
    	
    	/**
    	 * 检测当前是否处于滑动状态。调用者要自己实现。
    	 * 如果当前处于滑动状态的话，检测器会忽略检测子 View 是否可以滑动。
    	 * 
    	 * @return True 表示处于滑动状态，false 表示不处于滑动状态。
    	 */
    	boolean onDetectScrolling();
    	
        /**
         * 通知按下事件发生了。任何按下动作都会触发这个事件。
         * 其它的通知事件都是基于这个事件产生的。
         *
         * @param e 触发按下事件的 {@link MotionEvent}.
         * @return True 表示处理该触摸事件，false 不处理
         */
        boolean onDown(MotionEvent e);
        
        /**
         * 通知松手事件发生了。
         * 
         * @param e 触发松手事件的 {@link MotionEvent}
         * @return True 表示处理该触摸事件，false 不处理
         */
        boolean onUp(MotionEvent e);
    	
        /**
         * Notified when a scroll occurs with the initial on down {@link MotionEvent} and the
         * current move {@link MotionEvent}. The distance in x and y is also supplied for
         * convenience.
         *
         * @param e1 The first down motion event that started the scrolling.
         * @param e2 The move motion event that triggered the current onScroll.
         * @param distanceX The distance along the X axis that has been scrolled since the last
         *              call to onScroll. This is NOT the distance between {@code e1}
         *              and {@code e2}.
         * @param distanceY The distance along the Y axis that has been scrolled since the last
         *              call to onScroll. This is NOT the distance between {@code e1}
         *              and {@code e2}.
         * @return True 表示处理该触摸事件，false 不处理
         */
        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

        /**
         * Notified of a fling event when it occurs with the initial on down {@link MotionEvent}
         * and the matching up {@link MotionEvent}. The calculated velocity is supplied along
         * the x and y axis in pixels per second.
         * 
         * 
         *
         * @param e1 The first down motion event that started the fling.
         * @param e2 The move motion event that triggered the current onFling.
         * @param velocityX The velocity of this fling measured in pixels per second
         *              along the x axis.
         * @param velocityY The velocity of this fling measured in pixels per second
         *              along the y axis.
         * @return True 表示处理该触摸事件，false 不处理
         */
        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
    	
    }
    
    public ScrollDetector(Context context, View hostView) {
    	this(context, hostView, HORIZONTAL, null);
    }
    
    public ScrollDetector(Context context, View hostView, int scrollType) {
    	this(context, hostView, scrollType, null);
    }
    
    public ScrollDetector(Context context, View hostView, int scrollType, ScrollDetectorListener listener) {
    	mHostView = hostView;
    	mListener = listener;
    	
    	init(context);
    	setScrollType(scrollType);
    }
    
    private void init(Context context) {
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        int touchSlop = configuration.getScaledTouchSlop();
        mTouchSlop = configuration.getScaledPagingTouchSlop();
        mTouchSlopSquare = touchSlop * touchSlop;
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        
        final float density = context.getResources().getDisplayMetrics().density;
        mDefaultGutterSize = (int) (DEFAULT_GUTTER_SIZE * density);
        
        mIsBeingDragged = false;
        mIsUnableToDrag = false;
        
    	mLastMotionX = 0;
    	mLastMotionY = 0;
    	mInitialMotionX = 0;
    	mInitialMotionY = 0;
    	
    	mActivePointerId = INVALID_POINTER;
    	
    	mFlingVelocity = new PointF(0, 0);
        
        mHostWidth = 0;
        mHostHeight = 0;
        
        mScrollType = HORIZONTAL;
    }
    
    /**
     * 设置检测器类型
     * 
     * @param type {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    public void setScrollType(int type) {
    	switch (type) {
    	case HORIZONTAL:
    	case VERTICAL:
    		mScrollType = type;
    		break;
    	
    	default:
    		mScrollType = HORIZONTAL;
    	}
    }
    
    /**
     * 设置监听器
     * 
     * @param listener Object of {@link ScrollDetectorListener}
     */
    public void setListener(ScrollDetectorListener listener) {
    	mListener = listener;
    }
    
    /**
     * 在  Host View {@link ViewGroup#onInterceptTouchEvent(MotionEvent)} 中分析滑动事件。
     * 这个是激发滑动回调的基本分析过程，要在 Host View 的 onInterceptTouchEvent 中调用。
     * 
     * @param ev see {@link ViewGroup#onInterceptTouchEvent(MotionEvent)}
     * @return True 表示触摸事件被 Host view 截断，Host view 接收滑动事件；false 表示 Host view 
     * 不接收滑动事件，子 View 接收。
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // This method JUST determines whether we want to intercept the motion.
        // If we return true, onMotionEvent will be called and we do the actual 
    	// scrolling there.
    	
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        debugShow("Intercept", ev);
        
        // Always take care of the touch gesture being complete.
        if (MotionEvent.ACTION_CANCEL == action || MotionEvent.ACTION_UP == action) {
            // Release the drag.
        	debugShow("Intercept: cancel or up", ev);
            mIsBeingDragged = false;
            mIsUnableToDrag = false;
            mActivePointerId = INVALID_POINTER;
            if (null != mVelocityTracker) {
            	// this is just a back door, this case the calculate and save the fling velocity.
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                mFlingVelocity.x = mVelocityTracker.getXVelocity();
                mFlingVelocity.y = mVelocityTracker.getYVelocity();
            	
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            return false;
        }
        
        // Nothing more to do here if we have decided whether or not we
        // are dragging.
        if (MotionEvent.ACTION_DOWN != action) {
            if (mIsBeingDragged) {
            	debugShow("Intercept: !donw return true", ev);
                return true;
            }
            if (mIsUnableToDrag) {
            	debugShow("Intercept: !donw return false", ev);
                return false;
            }
        }
        
        switch (action) {        
        case MotionEvent.ACTION_DOWN: {
            // Remember location of down touch.
            // ACTION_DOWN always refers to pointer index 0.
        	mLastMotionX = ev.getX();
        	mLastMotionY = ev.getY();	
            if (HORIZONTAL == mScrollType) {
            	mInitialMotionX = mLastMotionX;
            } else {
            	mInitialMotionY = mLastMotionY;
            }
            
            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
            mIsUnableToDrag = false;
            
            if (isScrolling()) {
            	// Let the user 'catch' the pager as it animates.
                mIsBeingDragged = true;
                recordCurrentMotionEvent(ev);
            } else {
                mIsBeingDragged = false;
            }
            
            debugShow("Intercept: ACTION_DOWN: ", ev);
            break;
        }
        
        case MotionEvent.ACTION_MOVE: {
        	// mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
        	// whether the user has moved far enough from his original down touch.
        	
        	// Locally do absolute value. mLastMotionY is set to the y value of the down event.
        	debugShow("Intercept: ACTION_MOVE", ev);
        	final int activePointerId = mActivePointerId;
        	if (activePointerId == INVALID_POINTER) {
        		// If we don't have a valid id, the touch down wasn't on content.
        		break;
        	}
            
        	final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
        	final float x = MotionEventCompat.getX(ev, pointerIndex);
        	final float dx = x - mLastMotionX;
        	final float xDiff = Math.abs(dx);
        	final float y = MotionEventCompat.getY(ev, pointerIndex);
        	final float dy = y - mLastMotionY;
        	final float yDiff = Math.abs(dy);
        	if (DEBUG) Log.v(TAG, "Intercept: ACTION_MOVE: " + x + "," + y + " diff=" + xDiff 
        			+ "," + yDiff + ", touchSlop: " + mTouchSlop);
            
        	float pos = 0;
        	float delta = 0;
        	float primaryDiff = 0;
        	float secondaryDiff = 0;
        	if (HORIZONTAL == mScrollType) { 
        		pos = x;
        		delta = dx;
        		primaryDiff = xDiff;
        		secondaryDiff = yDiff;
        	} else {
        		pos = y;
        		delta = dy;
        		primaryDiff = yDiff;
        		secondaryDiff = xDiff;
        	}
            
        	// Check child whether can scroll in our scroll direction.
        	// If child can scroll in our scroll direction, we let child scroll first.
        	boolean scrollable = canScroll(mHostView, false, (int) delta, (int) x, (int) y);
        	if (DEBUG) Log.v(TAG, "Intercept: ACTION_MOVE: delta: " + delta + " -- " + x + "," + y 
        			+ " child can scroll: " + scrollable);
        	
        	if (0 != pos && !isGutterDrag(pos, delta) && scrollable) {
        		// Nested view has scrollable area under this point. Let it be handled there.
        		mLastMotionX = x;
        		mLastMotionY = y;
        		if (HORIZONTAL == mScrollType) {
        			mInitialMotionX = x;
        		} else {
        			mInitialMotionY = y;
        		}
                
        		mIsUnableToDrag = true;
        		debugShow("Intercept: ACTION_MOVE: isGutterDrag or child can scroll", ev);
        		return false;
            }
            
        	if (primaryDiff > mTouchSlop && primaryDiff > secondaryDiff) {
        		mIsBeingDragged = true;
        		recordCurrentMotionEvent(ev);
        		
        		if (HORIZONTAL == mScrollType) { 
        			mLastMotionX = dx > 0 ? mInitialMotionX + mTouchSlop : 
        				mInitialMotionX - mTouchSlop;
        		} else {
        			mLastMotionY = dy > 0 ? mInitialMotionY + mTouchSlop :
        				mInitialMotionY - mTouchSlop;
        		}
        		debugShow("Intercept: ACTION_MOVE: Starting drag!", ev);
        		
        	} else {
        		if (secondaryDiff > mTouchSlop) {
        			// The finger has moved enough in the vertical
        			// direction to be counted as a drag...  abort
        			// any attempt to drag horizontally, to work correctly
        			// with children that have scrolling containers.
        			mIsUnableToDrag = true;
        			debugShow("Intercept: ACTION_MOVE: Starting unable to drag!", ev);
        		}
        	}
          	break;
        }
        
        case MotionEventCompat.ACTION_POINTER_UP:
        	debugShow("Intercept: ACTION_POINTER_UP:", ev);
        	onSecondaryPointerUp(ev);
            break;
            
        default:
        	break;
        }
        
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        // The only time we want to intercept motion events is if we are in the
        // drag mode.
        return mIsBeingDragged;
    }
    
    /**
     * 在 Host View {@link View#onTouchEvent(MotionEvent)} 分析滑动事件。
     * 这个是激发滑动回调的基本分析过程，要在 Host View 的 onTouchEvent 中调用。
     * 
     * @param ev see {@link View#onTouchEvent(MotionEvent)}
     * @return True 触摸事件被 Host View 接收，false 不被 Host View 接收。
     */
    public boolean onTouchEvent(MotionEvent ev) {
        if (MotionEvent.ACTION_DOWN == ev.getAction() && 0 != ev.getEdgeFlags()) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
        	if (DEBUG) Log.v(TAG, "Touch act == ACTION_DOWN and down in edge !!");
            return false;
        }
        
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        
        boolean handled = false;
		int pointerIndex;
        float x, y;
        
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
        case MotionEvent.ACTION_DOWN: {
            mIsBeingDragged = true;
            recordCurrentMotionEvent(ev);
            
            // Remember where the motion event started
            mLastMotionX = ev.getX();
            mLastMotionY = ev.getY();
            if (HORIZONTAL == mScrollType) {
            	mInitialMotionX = mLastMotionX;
            } else {
            	mInitialMotionY = mLastMotionY;
            }
            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
            
            debugShow("Touch: ACTION_DOWN", ev);
            handled |= mListener.onDown(ev);
            break;
        }
            
        case MotionEvent.ACTION_MOVE: {
        	debugShow("Touch: ACTION_MOVE", ev);
    		pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
            x = MotionEventCompat.getX(ev, pointerIndex);
            y = MotionEventCompat.getY(ev, pointerIndex);
        	if (!mIsBeingDragged) {
                final float xDiff = Math.abs(x - mLastMotionX);
                final float yDiff = Math.abs(y - mLastMotionY);
                if (DEBUG) Log.v(TAG, "Touch: ACTION_MOVE: " + x + "," + y + " diff=" + xDiff + "," + yDiff);
                
            	float primaryDiff = 0;
            	float secondaryDiff = 0;
            	if (HORIZONTAL == mScrollType) {
            		primaryDiff = xDiff;
            		secondaryDiff = yDiff;
            	} else {
            		primaryDiff = yDiff;
            		secondaryDiff = xDiff;
            	}
                
                if (primaryDiff > mTouchSlop && primaryDiff > secondaryDiff) {
                    mIsBeingDragged = true;
                    recordCurrentMotionEvent(ev);
                    
                    if (HORIZONTAL == mScrollType) {
                    	mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX + mTouchSlop : 
                    		mInitialMotionX - mTouchSlop;
                    } else {
                    	mLastMotionY = y - mInitialMotionY > 0 ? mInitialMotionY + mTouchSlop : 
                    		mInitialMotionY - mTouchSlop;
                    }
                    
                    debugShow("Touch: ACTION_MOVE: Starting darg!", ev);
                }
            }
            // Not else! Note that mIsBeingDragged can be set above.
        	if (mIsBeingDragged) {
        		if (null != mCurrentDownEvent) {
        			final float scrollX = mLastMotionX - x;
        			final float scrollY = mLastMotionY - y;
        			final int deltaX = (int) (x - mCurrentDownEvent.getX());
        			final int deltaY = (int) (y - mCurrentDownEvent.getY());
        			int distance = (deltaX * deltaX) + (deltaY * deltaY);
        			if (distance > mTouchSlopSquare) {
        				handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY);
        				mLastMotionX = x;
                    	mLastMotionY = y;
                	}
        		}
        	}
        	
            break;
        }
            
        case MotionEvent.ACTION_UP: {
        	debugShow("Touch: ACTION_UP", ev);
        	if (mIsBeingDragged) {        		
                // A fling must travel the minimum tap distance
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final float velocityY = velocityTracker.getYVelocity();
                final float velocityX = velocityTracker.getXVelocity();
                
                if ((Math.abs(velocityY) > mMinimumFlingVelocity)
                        || (Math.abs(velocityX) > mMinimumFlingVelocity)){
                    handled = mListener.onFling(mCurrentDownEvent, ev, velocityX, velocityY);
                }
                
                // TODO: onUp 还是要发的
                //if (!handled) {
                handled = mListener.onUp(ev);
                //}
        		
                mActivePointerId = INVALID_POINTER;
                endDrag();
                
                debugShow("Touch: ACTION_UP: end darg!", ev);
            }
            break;
        }
                
        case MotionEventCompat.ACTION_POINTER_DOWN: {
            final int index = MotionEventCompat.getActionIndex(ev);
            x = MotionEventCompat.getX(ev, index);
            y = MotionEventCompat.getY(ev, index);
            if (HORIZONTAL == mScrollType) {
            	mLastMotionX = x;
            } else {
            	mLastMotionY = y;
            }
            mActivePointerId = MotionEventCompat.getPointerId(ev, index);
            if (DEBUG) Log.v(TAG, "Touch: ACTION_POINTER_DOWN: index: " + index + ", activePosId: " + mActivePointerId 
            		+ " , " + x + "," + y);
            break;
        }
            
        case MotionEventCompat.ACTION_POINTER_UP: {
        	debugShow("Touch: ACTION_POINTER_UP", ev);
            onSecondaryPointerUp(ev);
            if (HORIZONTAL == mScrollType) {
            	mLastMotionX = MotionEventCompat.getX(ev, 
            			MotionEventCompat.findPointerIndex(ev, mActivePointerId));
            	if (DEBUG) Log.v(TAG, "Touch: ACTION_POINTER_UP: newLastX: " + mLastMotionX);
            } else {
            	mLastMotionY = MotionEventCompat.getY(ev, 
            			MotionEventCompat.findPointerIndex(ev, mActivePointerId));
            	if (DEBUG) Log.v(TAG, "Touch: ACTION_POINTER_UP: newLastY: " + mLastMotionY);
            }
            break;
        }
        
        case MotionEvent.ACTION_CANCEL: {
        	debugShow("Touch: ACTION_CANCEL", ev);
            if (mIsBeingDragged) {
            	handled = mListener.onUp(ev);
            	
                mActivePointerId = INVALID_POINTER;
                endDrag();
                
                debugShow("Touch: ACTION_CANCEL end drag", ev);
            }
            break;
        }
        
        default:
        	break;
        }
        
        return handled;
    }
    
    /**
     * 根据 Host View 的宽高设定滑动事件分析的临界值。
     * 这个是激发滑动回调的基本分析过程，要在 Host View 的 onMeasure 最后调用。
     * 
     * @param width Host View 宽度（注意这里宽度是要 EXACTLY 的）
     * @param height Host View 高度（注意这里高度是要 EXACTLY 的）
     */
    public void onMeasure(int width, int height) {
    	mHostWidth = width;
    	mHostHeight = height;
    	
        final int maxGutterSizeH = width / 10;
        final int maxGutterSizeV = height / 10;
        mGutterSizeH = Math.min(maxGutterSizeH, mDefaultGutterSize);
        mGutterSizeV = Math.min(maxGutterSizeV, mDefaultGutterSize);
    }
    
    public float getFlingVelocityX() {
    	return mFlingVelocity.x;
    }
    
    public float getFlingVelocityY() {
    	return mFlingVelocity.y;
    }
    
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        
        if (DEBUG) Log.v(TAG, "secondaryPosUp: posId: " + pointerId + ", posIndex: " + pointerIndex 
        		+ ", activePosId: " + mActivePointerId );
        
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            
            if (HORIZONTAL == mScrollType) {
            	mLastMotionX = MotionEventCompat.getX(ev, newPointerIndex);
            } else {
            	mLastMotionY = MotionEventCompat.getY(ev, newPointerIndex);
            }
            
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (null != mVelocityTracker) {
                mVelocityTracker.clear();
            }
            
            debugShow("secondaryPosUp: newActiveId", ev);
        }
    }
    
    private void endDrag() {
        mIsBeingDragged = false;
        mIsUnableToDrag = false;
        
        if (null != mVelocityTracker) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    
    private void recordCurrentMotionEvent(MotionEvent ev) {
        if (null != mCurrentDownEvent) {
            mCurrentDownEvent.recycle();
        }
        mCurrentDownEvent = MotionEvent.obtain(ev);
    }
    
    //private boolean isGutterDrag(float x, float dx) {
    protected final boolean isGutterDrag(float pos, float delta) {
        //return (x < mGutterSize && dx > 0) || (x > mHostWidth - mGutterSize && dx < 0);
    	if (HORIZONTAL == mScrollType) {
    		return (pos < mGutterSizeH && delta > 0) || (pos > mHostWidth - mGutterSizeH && delta < 0);
    	} else {
    		return (pos > mGutterSizeV && delta > 0) || (pos > mHostHeight - mGutterSizeV && delta < 0);
    	}
    }
    
    protected final boolean isScrolling() { 
    	if (null == mListener) {
    		return false;
    	}
    	
    	return mListener.onDetectScrolling();
    }
    
    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v View to test for horizontal scrollability
     * @param checkSelf Whether the view v passed should itself be checked for scrollability (true),
     *               or just its children (false).
     * @param delta Delta scrolled in pixels
     * @param x X coordinate of the active touch point
     * @param y Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected final boolean canScroll(View view, boolean checkSelf, int delta, int x, int y) {
    	if (null == view) {
    		return false;
    	}
    	
        if (view instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) view;
            final int scrollX = view.getScrollX();
            final int scrollY = view.getScrollY();
            final int count = group.getChildCount();
            // Count backwards - let topmost views consume scroll distance first.
            for (int i = count - 1; i >= 0; i--) {
                // TODO: Add versioned support here for transformed views.
                // This will not work for transformed views in Honeycomb+
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() &&
                        y + scrollY >= child.getTop() && y + scrollY < child.getBottom() &&
                        canScroll(child, true, delta, x + scrollX - child.getLeft(),
                                y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }
        
        // dx (-), dy (+)
        boolean scrolled = false;
        if (HORIZONTAL == mScrollType) {
        	scrolled = ViewCompat.canScrollHorizontally(view, -delta);
        } else {
        	scrolled = ViewCompat.canScrollVertically(view, delta);
        }
        
        return checkSelf && scrolled;
        //return checkV && ViewCompat.canScrollHorizontally(v, -dx);
    }
	
    protected void debugShow(String prefix, MotionEvent ev) {
    	if (DEBUG) {
    		int act = -1;
    		if (null != ev) {
    			act = ev.getAction() & MotionEventCompat.ACTION_MASK;
    		}
    		Log.v(TAG, prefix + ", act: " + act + ", lastX: " + mLastMotionX + ", lastY: " + mLastMotionY 
    				+ ", initX: " + mInitialMotionX + ", initY: " + mInitialMotionY
    				+ ", activePosId: " + mActivePointerId 
    				+ ", isBeingDragged: " + mIsBeingDragged + ", isUnableToDrag: " + mIsUnableToDrag
    				);
    	}
    }
    
}
