package com.eebbk.mingming.k7ui.bounceview;

import com.eebbk.mingming.k7ui.R;
import com.eebbk.mingming.k7utils.ReflectUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.EdgeEffect;
import android.widget.HorizontalScrollView;

/**
 * 
 * 带 Over Scroll 回弹效果的 {@link HorizontalScrollView} </br>
 * 
 * </br>
 * 稍稍修改下系统自带的 {@link HorizontalScrollView} 就可以实现，因为系统已经实现大部分框架了，
 * 只不过故意屏蔽了而已。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class BounceHorizontalScrollView extends HorizontalScrollView {
	
	@SuppressWarnings("unused")
	private final static String TAG = "HorizontalScrollView"; 
	
	private final static int DEFAULT_MAX_OVERSCROLL_X = 200;
	
	protected int mMaxOverScrollX = DEFAULT_MAX_OVERSCROLL_X;
	protected Context mContext = null;
    
    protected EdgeEffect mSuperEdgeGlowLeft = null;
    protected EdgeEffect mSuperEdgeGlowRight = null;
    
	
	public BounceHorizontalScrollView(Context context) {
		super(context);
		init(context, null, 0);
	}
	
	public BounceHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	
	public BounceHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle) {
		if (null != attrs) {
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.BounceHorizontalScrollView, defStyle, 0);

			mMaxOverScrollX = (int) a.getDimension(
					R.styleable.BounceHorizontalScrollView_k7uibhsvMaxOverScrollX, 
					DEFAULT_MAX_OVERSCROLL_X);
			
			a.recycle();
		} else {
			mMaxOverScrollX = DEFAULT_MAX_OVERSCROLL_X;
		}
		
		mContext = context;
        
        // get the density of the screen and do some maths with it on the max overscroll distance  
        // variable so that you get similar behaviors no matter what the screen size  
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();  
        final float density = metrics.density;  
        mMaxOverScrollX = (int) (density * mMaxOverScrollX);
        
        getSuperPrivateObject();
	}
	
	// 通过反射获取父类的一些 private 对象，android 原生又没提供相应的接口，好像是故意的 >_<
	private void getSuperPrivateObject() {
        // 获取父类的 EdgeGlow 对象，就是画 over scroll 的那个发光的东西（左、右2个）
		try {
			mSuperEdgeGlowLeft = (EdgeEffect) ReflectUtils.getFieldObject(
					HorizontalScrollView.class, this, "mEdgeGlowLeft");
			mSuperEdgeGlowRight = (EdgeEffect) ReflectUtils.getFieldObject(
					HorizontalScrollView.class, this, "mEdgeGlowRight");
		} catch (Exception e) {
			e.printStackTrace();
			mSuperEdgeGlowLeft = null;
			mSuperEdgeGlowRight = null;
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
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, 
        		mMaxOverScrollX, maxOverScrollY, isTouchEvent);    
    }
	
	@Override
	protected int computeHorizontalScrollRange() {        
        if (OVER_SCROLL_ALWAYS != getOverScrollMode()) {
        	return super.computeHorizontalScrollRange();
        }
        
        // 如果是 over scroll always 的话就每次 over scroll 的时候都显示滚动条。
        // 系统判断是要 scrollRange > scrollExtent 才会显示的滚动条的。
        // 但是系统如果滚动的内容少的话， scrollRange 是会 <= scrollExtent 的。
        int superScrollRange = super.computeHorizontalScrollRange();
        int superScrollExtent = super.computeHorizontalScrollExtent();
        int scrollX = getScrollX();
        
		//LogUtils.d(TAG, "superScrollRange: " + superScrollRange 
		//		+ ", superScrollExtent: " + superScrollExtent 
		//		+ ", scrollX: " + getScrollX());
        
        if (superScrollRange > superScrollExtent + Math.abs(scrollX)) {
        	return superScrollRange;
        }
        
        int scrollRange = superScrollExtent + Math.abs(scrollX);
        return scrollRange;
	}
	
	/**
	 * 获取 X 轴最大 over scroll
	 * 
	 * @return
	 */
	public int getMaxOverScrollX() {
		return mMaxOverScrollX;
	}
	
	/** 
	 * 设置 X 轴最大 over scroll
	 * 
	 * @param maxOverScrollX 单位像素（这个会自动设置屏幕的 density）
	 */
	public void setMaxOverScrollX(int maxOverScrollX) {
        final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();  
        final float density = metrics.density;
        mMaxOverScrollX = (int) (density * maxOverScrollX);
	}

	/**
	 * 设置系统默认边界效果图像。 </br>
	 * see {@link #setDefaultEdgeEffect(Drawable, Drawable, boolean)}
	 * 
	 * @param drEdgeLeft
	 * @param drGlowLeft
	 * @param drEdgeRight
	 * @param drGlowRight
	 */
	public void setDefaultEdgeEffect(Drawable drEdgeLeft, Drawable drGlowLeft, 
			Drawable drEdgeRight, Drawable drGlowRight) {
		setDefaultEdgeEffect(drEdgeLeft, drGlowLeft, true);
		setDefaultEdgeEffect(drEdgeRight, drGlowRight, false);
	}
	
	/**
	 * 设置系统默认边界效果图像。</br>
	 * 请在 {@link #setOverScrollMode(int)} 之后调用。
	 * 
	 * @param drEdge 边界效果 {@link Drawable}，如果你只是想取消这个效果的话，找个透明的 drawable 整上去就行了
	 * @param drGlow 发光效果 {@link Drawable}，如果你只是想取消这个效果的话，找个透明的 drawable 整上去就行了
	 * @param isLeft True 左部的，false 右部的
	 */
	public void setDefaultEdgeEffect(Drawable drEdge, Drawable drGlow, boolean isLeft) {
		EdgeEffect edgeEffect = null;
		if (isLeft) {
			edgeEffect = mSuperEdgeGlowLeft;
		} else {
			edgeEffect = mSuperEdgeGlowRight;
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
	
}
