package com.eebbk.mingming.k7ui.bounceview;

import com.eebbk.mingming.k7ui.R;
import com.eebbk.mingming.k7utils.LogUtils;
import com.eebbk.mingming.k7utils.ReflectUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.GridView;
import android.widget.OverScroller;

/**
 * 
 * 带 Over Scroll 回弹效果的 {@link GridView} </br>
 * 
 * </br>
 * 稍稍修改下系统自带的 {@link GridView} 就可以实现，因为系统已经实现大部分框架了，
 * 只不过故意屏蔽了而已。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class BounceGridView extends GridView {
	
	private final static String TAG = "BounceGridView"; 
	
	// *************************************
    // copy from origin AbsListView.java
	// *************************************
	
	/**
	 * Indicates that we are not in the middle of a touch gesture
	 */
    protected final static int TOUCH_MODE_REST = -1;

    /**
     * Indicates we just received the touch event and we are waiting to see if the it is a tap or a
     * scroll gesture.
     */
    protected final static int TOUCH_MODE_DOWN = 0;

    /**
     * Indicates the touch has been recognized as a tap and we are now waiting to see if the touch
     * is a longpress
     */
    protected final static int TOUCH_MODE_TAP = 1;

    /**
     * Indicates we have waited for everything we can wait for, but the user's finger is still down
     */
    protected final static int TOUCH_MODE_DONE_WAITING = 2;

    /**
     * Indicates the touch gesture is a scroll
     */
    protected final static int TOUCH_MODE_SCROLL = 3;

    /**
     * Indicates the view is in the process of being flung
     */
    protected final static int TOUCH_MODE_FLING = 4;

    /**
     * Indicates the touch gesture is an overscroll - a scroll beyond the beginning or end.
     */
    protected final static int TOUCH_MODE_OVERSCROLL = 5;

    /**
     * Indicates the view is being flung outside of normal content bounds
     * and will spring back.
     */
    protected final static int TOUCH_MODE_OVERFLING = 6;
    
    protected final static int SCROLL_DURATION = 200;
    
	// *************************************
	// *************************************

	
	private static final int DEFAULT_MAX_OVERSCROLL_Y = 200;
	
	protected int mMaxOverScrollY = DEFAULT_MAX_OVERSCROLL_Y;
	protected Context mContext = null;
    
	protected static Class<?> mFlingRunnableClass = null;
	protected static Class<?> mSplineOverScrollerClass = null; 
	protected static Class<?>[] mSpringBackParamsClass = new Class[] {
		Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE };
	
    protected /*FlingRunnable*/ Object mSuperFlingRunnable = null;
    protected OverScroller mSuperScroller = null;
    
    protected EdgeEffect mSuperEdgeGlowTop = null;
    protected EdgeEffect mSuperEdgeGlowBottom = null;
    
	
	public BounceGridView(Context context) {
		super(context);
		init(context, null, 0);
	}
	
	public BounceGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	
	public BounceGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle) {
		if (null != attrs) {
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.BounceGridView, defStyle, 0);

			mMaxOverScrollY = (int) a.getDimension(
					R.styleable.BounceGridView_k7uibgvMaxOverScrollY, 
					DEFAULT_MAX_OVERSCROLL_Y);
			
			a.recycle();
		} else {
			mMaxOverScrollY = DEFAULT_MAX_OVERSCROLL_Y;
		}
		
		mContext = context;
		
        //get the density of the screen and do some maths with it on the max overscroll distance  
        //variable so that you get similar behaviors no matter what the screen size  
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();  
        final float density = metrics.density; 
        mMaxOverScrollY = (int) (density * mMaxOverScrollY);
        
        getSuperPrivateObject();
	}
	
	// 通过反射获取父类的一些 private 对象，android 原生又没提供相应的接口，好像是故意的 >_<
	private void getSuperPrivateObject() {
        // 获取父类的 EdgeGlow 对象，就是画 over scroll 的那个发光的东西（上、下2个）
		try {
			mSuperEdgeGlowTop = (EdgeEffect) ReflectUtils.getFieldObject(
					AbsListView.class, this, "mEdgeGlowTop");
			mSuperEdgeGlowBottom = (EdgeEffect) ReflectUtils.getFieldObject(
					AbsListView.class, this, "mEdgeGlowBottom");
		} catch (Exception e) {
			e.printStackTrace();
			mSuperEdgeGlowTop = null;
			mSuperEdgeGlowBottom = null;
		}
		
		// 获取父类的 FlingRunnable Class，这个类是用来处理 over scroll 的各种事件的。
		// 不过它的对象是在运行时才创建的，所以在不要这里获取。
		if (null == mFlingRunnableClass) {
			try {
				mFlingRunnableClass = Class.forName(
						"android.widget.AbsListView$FlingRunnable");
			} catch (Exception e) {
				e.printStackTrace();
				mFlingRunnableClass = null;
			}
		}
	}
	
	@Override  
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, 
    		int scrollRangeX, int scrollRangeY, 
    		int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent){
		
		//LogUtils.d(TAG, "deltaX: " + deltaX + ", deltaY: " + deltaY
		//		+ ", scrollY: " + scrollY + ", scrollRangeY: " + scrollRangeY 
		//		+ ", maxOverScrollY: " + maxOverScrollY + ", isTouchEvent: " + isTouchEvent 
		//		+ ", getScrollY: " + getScrollY());
		
		// 这里只要简单的把 max over scroll 的值改一下，就可以有 over scroll 的效果了。
		// 系统原来这个值是个很小的值的，几乎看不到效果。
		boolean clamped = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, 
				maxOverScrollX, mMaxOverScrollY, isTouchEvent); 
		
		// 某些情况下 ListView 的 over scroll 的回弹会不对，这种情况下手动修正一下。
		int touchMode = getSuperTouchMode();
		if (TOUCH_MODE_OVERFLING == touchMode) {
			if (0 == deltaY && 0 != scrollY) {
				checkOverScrollSpringback();
			}
		}
		
		return clamped;
    }
	
	@Override
	protected int computeVerticalScrollRange() {     
        if (OVER_SCROLL_ALWAYS != getOverScrollMode()) {
        	return super.computeVerticalScrollRange();
        }
        
        // 如果是 over scroll always 的话就每次 over scroll 的时候都显示滚动条。
        // 系统判断是要 scrollRange > scrollExtent 才会显示的滚动条的。
        // 但是系统如果滚动的内容少的话， scrollRange 是会 <= scrollExtent 的。
        int superScrollRange = super.computeVerticalScrollRange();
        int superScrollExtent = super.computeVerticalScrollExtent();
        int scrollY = getScrollY();
        
		//LogUtils.d(TAG, "superScrollRange: " + superScrollRange 
		//		+ ", superScrollExtent: " + superScrollExtent 
		//		+ ", scrollY: " + scrollY);
        
        if (superScrollRange > superScrollExtent + Math.abs(scrollY)) {
        	return superScrollRange;
        }
        
        int scrollRange = superScrollExtent + Math.abs(scrollY);
        return scrollRange;
	}
	
	/*@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		LogUtils.d(TAG, "dispatchTouchEvent: action: " + ev.getActionMasked() 
			+ ", before: touchMode: " + getSuperTouchMode());
		
		boolean handle = super.dispatchTouchEvent(ev);
		
		int action  = ev.getActionMasked();
		LogUtils.d(TAG, "dispatchTouchEvent: action: " + ev.getActionMasked() 
				+ ", after: touchMode: " + getSuperTouchMode() + ", scrollY: " + getScrollY()
				+ ", y: " + ev.getY());
		
		return handle;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		LogUtils.d(TAG, "onTouchEvent: action: " + ev.getActionMasked() 
				+ ", before: touchMode: " + getSuperTouchMode());
		
		boolean handle = super.onTouchEvent(ev);
		
		LogUtils.d(TAG, "onTouchEvent: action: " + ev.getActionMasked() 
				+ ", after: touchMode: " + getSuperTouchMode() + ", scrollY: " + getScrollY() 
				+ ", y: " + ev.getY());
		
		return handle;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		LogUtils.d(TAG, "onInterceptTouchEvent: action: " + ev.getActionMasked() 
				+ ", before: touchMode: " + getSuperTouchMode());
		
		boolean handle = super.onInterceptTouchEvent(ev);
		
		LogUtils.d(TAG, "onInterceptTouchEvent: action: " + ev.getActionMasked() 
				+ ", after: touchMode: " + getSuperTouchMode() + ", scrollY: " + getScrollY()
				+ ", y: " + ev.getY());
		
		return handle;
	}*/
	
	/**
	 * 获取 Y 轴最大 over scroll
	 * 
	 * @return
	 */
	public int getMaxOverScrollY() {
		return mMaxOverScrollY;
	}
	
	/** 
	 * 设置 Y 轴最大 over scroll
	 * 
	 * @param maxOverScrollY 单位像素（这个会自动设置屏幕的 density）
	 */
	public void setMaxOverScrollY(int maxOverScrollY) {
        final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();  
        final float density = metrics.density;
        mMaxOverScrollY = (int) (density * maxOverScrollY);
	}
	
	/**
	 * 设置系统默认边界效果图像。 </br>
	 * see {@link #setDefaultEdgeEffect(Drawable, Drawable, boolean)}
	 * 
	 * @param drEdgeTop
	 * @param drGlowTop
	 * @param drEdgeBottom
	 * @param drGlowBottom
	 */
	public void setDefaultEdgeEffect(Drawable drEdgeTop, Drawable drGlowTop, 
			Drawable drEdgeBottom, Drawable drGlowBottom) {
		setDefaultEdgeEffect(drEdgeTop, drGlowTop, true);
		setDefaultEdgeEffect(drEdgeBottom, drGlowBottom, false);
	}
	
	/**
	 * 设置系统默认边界效果图像。</br>
	 * 请在 {@link #setOverScrollMode(int)} 之后调用。
	 * 
	 * @param drEdge 边界效果 {@link Drawable}，如果你只是想取消这个效果的话，找个透明的 drawable 整上去就行了
	 * @param drGlow 发光效果 {@link Drawable}，如果你只是想取消这个效果的话，找个透明的 drawable 整上去就行了
	 * @param isTop True 顶部的，false 底部的
	 */
	public void setDefaultEdgeEffect(Drawable drEdge, Drawable drGlow, boolean isTop) {
		EdgeEffect edgeEffect = null;
		if (isTop) {
			edgeEffect = mSuperEdgeGlowTop;
		} else {
			edgeEffect = mSuperEdgeGlowBottom;
		}
		
		if (null == edgeEffect) {
			return;
		}
		
		setEdgeEffectDrawable(edgeEffect, drEdge, drGlow);
	}
	
	/**
	 * 设置默认边界效果图像。(使用反射方法设置)
	 * 
	 * @param edgeEffect Object of {@link EdgeEffect}
	 * @param drEdge
	 * @param drGlow
	 */
	protected void setEdgeEffectDrawable(EdgeEffect edgeEffect, Drawable drEdge, Drawable drGlow) {
		if (null == edgeEffect) {
			return;
		}
		
		ReflectUtils.setFieldObject(EdgeEffect.class, edgeEffect, "mEdge", drEdge);
		ReflectUtils.setFieldObject(EdgeEffect.class, edgeEffect, "mGlow", drGlow);
	}
	
	// 获取父类的 mTouchMode 变量
	private int getSuperTouchMode() {
		try {
			return (Integer) ReflectUtils.getFieldObject(
					AbsListView.class, this, "mTouchMode");
		} catch (Exception e) {
			e.printStackTrace();
			return TOUCH_MODE_REST;
		}
	}
	
	private void checkOverScrollSpringback() {
		if (null == mFlingRunnableClass) {
			return;
		}
		
		// 必须在运行时获取，因为父类这些变量是在运行时才创建的。 >_<
		// 这里感觉反射是不是用得有点过了。 -_-||
		if (null == mSuperFlingRunnable) {
			try {
				mSuperFlingRunnable = ReflectUtils.getFieldObject(
						AbsListView.class, this, "mFlingRunnable");
			} catch (Exception e) {
				e.printStackTrace();
				mSuperFlingRunnable = null;
			}
		}
		
		if (null == mSuperFlingRunnable) {
			return;
		}
		
		if (null == mSuperScroller) {
			try {
				mSuperScroller = (OverScroller) ReflectUtils.getFieldObject(
						mFlingRunnableClass, mSuperFlingRunnable, "mScroller");
			} catch (Exception e) {
				e.printStackTrace();
				mSuperScroller = null;
			}
		}
		
		if (null != mSuperScroller) {
			if (/*mSuperScroller.isFinished() &&*/ 0 != mSuperScroller.getFinalY()) {
				LogUtils.d(TAG, "checkOverScrollSpringback: overscroll see don't align, we correct it manual !");
				// 这里直接设置这个终点变量的话，回弹动画就只剩下最后一帧了，太突兀了。
				//ReflectUtils.invokeMethod(OverScroller.class, mSuperScroller, 
				//		"setFinalY", mFakeParamsClass, 0);
				
				// 当然这里重新设置动画，也有点小问题，就是开始时候的速度和前面停下来的速度接不上，
				// 也有点不自然，不过这个后面再调了。
				ReflectUtils.invokeMethod(OverScroller.class, mSuperScroller, 
						"springBack", mSpringBackParamsClass, 
						0, getScrollY(), 0, 0, 0, 0);
			}
		}
	}
	
}
