package com.eebbk.mingming.k7ui.bounceview;

import com.eebbk.mingming.k7ui.R;
import com.eebbk.mingming.k7utils.ReflectUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.EdgeEffect;
import android.widget.ScrollView;;

/**
 * 
 * 带 Over Scroll 回弹效果的 {@link ScrollView} </br>
 * 
 * </br>
 * 稍稍修改下系统自带的 {@link ScrollView} 就可以实现，因为系统已经实现大部分框架了，
 * 只不过故意屏蔽了而已。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class BounceScrollView extends ScrollView {
	
	@SuppressWarnings("unused")
	private final static String TAG = "BounceScrollView"; 
	
	private final static int DEFAULT_MAX_OVERSCROLL_Y = 200;
	
	protected int mMaxOverScrollY = DEFAULT_MAX_OVERSCROLL_Y;
	protected Context mContext = null;
    
    protected EdgeEffect mSuperEdgeGlowTop = null;
    protected EdgeEffect mSuperEdgeGlowBottom = null;
    
	
	public BounceScrollView(Context context) {
		super(context);
		init(context, null, 0);
	}
	
	public BounceScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	
	public BounceScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle) {
		if (null != attrs) {
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.BounceScrollView, defStyle, 0);

			mMaxOverScrollY = (int) a.getDimension(
					R.styleable.BounceScrollView_k7uibsvMaxOverScrollY, 
					DEFAULT_MAX_OVERSCROLL_Y);
			
			a.recycle();
		} else {
			mMaxOverScrollY = DEFAULT_MAX_OVERSCROLL_Y;
		}
		
		mContext = context;
        
        // get the density of the screen and do some maths with it on the max overscroll distance  
        // variable so that you get similar behaviors no matter what the screen size  
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
					ScrollView.class, this, "mEdgeGlowTop");
			mSuperEdgeGlowBottom = (EdgeEffect) ReflectUtils.getFieldObject(
					ScrollView.class, this, "mEdgeGlowBottom");
		} catch (Exception e) {
			e.printStackTrace();
			mSuperEdgeGlowTop = null;
			mSuperEdgeGlowBottom = null;
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
        		maxOverScrollX, mMaxOverScrollY, isTouchEvent);    
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
		//		+ ", scrollY: " + getScrollY());
        
        if (superScrollRange > superScrollExtent + Math.abs(scrollY)) {
        	return superScrollRange;
        }
        
        int scrollRange = superScrollExtent + Math.abs(scrollY);
        return scrollRange;
	}
	
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
	
}
