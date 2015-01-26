package com.eebbk.mingming.k7ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

/**
 * 
 * 拖动组件，支持左拖动、右拖动的操作
 * 
 * </br>
 * 
 * @author humingming <hmm@dw.gdbbk.com>
 *
 */
public abstract class DragComponent extends ViewGroup implements AnimatorListener {
	
	@SuppressWarnings("unused")
	private final static String TAG = "DragComponent";
	
	//public final static int DRAG_SUPPORT_NONE = 0x00;
	//public final static int DRAG_SUPPORT_LEFT = 0x01;
	//public final static int DRAG_SUPPORT_RIGHT = 0x02;
	//public final static int DRAG_SUPPORT_ALL = DRAG_SUPPORT_LEFT | DRAG_SUPPORT_RIGHT;
	
	public final static String PROPERTY_NAME = "DragPosition";
	
	/** 标示左边的组件 */
	public final static int SHOW_MAIN_COMPONENT = 0;
	/** 标示右边的组件 */
	public final static int SHOW_LEFT_COMPONENT = 1;
	/** 标示主组件 */
	public final static int SHOW_RIGHT_COMPONENT = 2;
	
	public final static long MIN_DRAG_ANIM_DURATION = 100; // 100ms
	public final static long MAX_DRAG_ANIM_DURATION = 500; // 500ms
	public final static int DEFAULT_ANIM_SPEED = 80;       // unit: pixel/100ms
	
	protected int mDragPosition = 0;
	protected int mTargetDragPosition = 0;
	
	private int mAnimSpeed = DEFAULT_ANIM_SPEED;
	private ObjectAnimator mDragAnim = null;
	
	protected View mLeftDragComponent = null;
	protected View mRightDragComponent = null;
	protected View mMainComponent = null;
	
	
	public DragComponent(Context context) {
		super(context);
		init(context, null, 0);
	}
	
	public DragComponent(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	
	public DragComponent(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle) {
		/*if (null != attrs) {
	        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DragTextView, defStyle, 0);
	        
	        mAutoDragRestore = a.getBoolean(R.styleable.DragTextView_k7uidtvAutoRestoreDrag, 
	        		DEFAULT_AUTO_RESTORE_DRAG);
	        mEnableDrag = a.getBoolean(R.styleable.DragTextView_k7uidtvEnableDrag, 
	        		DEFAULT_ENABLE_DRAG);
	        mOverScrollFactor = a.getFloat(R.styleable.DragTextView_k7uidtvOverScrollFactor, 
	        		DEFAULT_OVER_SCROLL_FACTOR);
	        
	        a.recycle();
		} else {
			mAutoDragRestore = DEFAULT_AUTO_RESTORE_DRAG;
			mEnableDrag = DEFAULT_ENABLE_DRAG;
			mOverScrollFactor = DEFAULT_OVER_SCROLL_FACTOR;
		}*/
		
		mDragPosition = 0;
		mTargetDragPosition = 0;
		
		mAnimSpeed = DEFAULT_ANIM_SPEED;
		
		mDragAnim = ObjectAnimator.ofInt(this, PROPERTY_NAME, 0, 0);
		mDragAnim.setInterpolator(new LinearInterpolator());
		mDragAnim.addListener(this);
	}
	
	@Override
	protected void onFinishInflate() {
		mLeftDragComponent = getLeftDragComponent(this);
		mRightDragComponent = getRightDragComponent(this);
		mMainComponent = getMainComponent(this);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;
        
        int componentPos = mDragPosition;
        
        // left drag component 在 main component 的左边
        if (null != mLeftDragComponent) {
        	componentPos -= mLeftDragComponent.getMeasuredWidth();
        	mLeftDragComponent.layout(
        			0 + componentPos,
        			0,
        			0 + componentPos + mLeftDragComponent.getMeasuredWidth(),
        			0 + height);
        	componentPos += mLeftDragComponent.getMeasuredWidth();
        }
        
        // 让 main component 布满整个 view
        if (null != mMainComponent) {
        	mMainComponent.layout(
        			0 + componentPos,
        			0,
        			0 + componentPos + width,
        			0 + height);
        	componentPos += mMainComponent.getMeasuredWidth();
        }
        
        // right drag component 在 main component 的右边
        if (null != mRightDragComponent) {
        	mRightDragComponent.layout(
        			0 + componentPos,
        			0,
        			0 + componentPos + mRightDragComponent.getMeasuredWidth(),
        			0 + height);
        }
	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height =  MeasureSpec.getSize(heightMeasureSpec);
        //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        //int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        
        // 让 main component 布满整个 view
        if (null != mMainComponent) {
        	mMainComponent.measure(widthMeasureSpec, heightMeasureSpec);
        }
        
        // left，right drag component 使用自适应布局，使用者要尽量保证 drag component 的大小
        if (null != mLeftDragComponent) {
			measureChild(mLeftDragComponent, 
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST), 
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST));
        }
        if (null != mRightDragComponent) {
			measureChild(mRightDragComponent, 
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST), 
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST));
        }
        
        setMeasuredDimension(width, height);
    }
	
	protected abstract View getLeftDragComponent(View container);
	
	protected abstract View getRightDragComponent(View container);
	
	protected abstract View getMainComponent(View container);
	
	@Override
	public void onAnimationCancel(Animator animation) {
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		// 动画结束了，刷新一下
		setDragPosition(mTargetDragPosition);
	}
	
	@Override
	public void onAnimationRepeat(Animator animation) {
	}

	@Override
	public void onAnimationStart(Animator animation) {
	}
	
	/**
	 * 显示组件, see {@link #showComponent(int, boolean)}
	 * 
	 * @param which
	 */
	public void showComponent(int which) {
		showComponent(which, true);
	}
	
	/**
	 * 显示组件
	 * 
	 * @param which 组件编号， {@link #SHOW_LEFT_COMPONENT}, {@link #SHOW_RIGHT_COMPONENT} or
	 * 	{@link #SHOW_MAIN_COMPONENT}
	 * @param animation True 显示动画，false 不显示动画
	 */
	public void showComponent(int which, boolean animation) {
		if ((SHOW_LEFT_COMPONENT == which && null == mLeftDragComponent) 
				|| (SHOW_RIGHT_COMPONENT == which && null == mRightDragComponent)) {
			which = SHOW_MAIN_COMPONENT;
		}
		
		int newPos = 0;
		int dix = 0;
		
		switch(which) {
		case SHOW_LEFT_COMPONENT:
			newPos = mLeftDragComponent.getWidth();
			dix = Math.abs(mLeftDragComponent.getWidth() - mDragPosition);
			break;
			
		case SHOW_RIGHT_COMPONENT:
			newPos = -mRightDragComponent.getWidth();
			dix = Math.abs(mRightDragComponent.getWidth() - mDragPosition);
			break;
			
		default:
		case SHOW_MAIN_COMPONENT:
			newPos = 0;
			dix = Math.abs(mDragPosition);
			break;
		}
		
		if (animation) {
			mDragAnim.cancel();
			mTargetDragPosition = newPos;
			
			long duration = 0;
			if (dix > 0) {
				duration = (dix * mAnimSpeed) / 100;
				if (duration <= 0) {
					duration = MIN_DRAG_ANIM_DURATION;
				} else if (duration > MAX_DRAG_ANIM_DURATION) {
					duration = MAX_DRAG_ANIM_DURATION;
				}
			}
			
			mDragAnim.setDuration(duration);
			mDragAnim.setIntValues(mDragPosition, newPos);
			mDragAnim.start();
		} else {
			mTargetDragPosition = newPos;
			setDragPosition(newPos);
		}
	}
	
	public int getLeftComponentLength() {
		if (null == mLeftDragComponent) {
			return 0;
		}
		return mLeftDragComponent.getWidth();
	}
	
	public int getRightComponentLength() {
		if (null == mRightDragComponent) {
			return 0;
		}
		return mRightDragComponent.getWidth();
	}
	
	public int getMainComponentLength() {
		if (null == mMainComponent) {
			return 0;
		}
		return mMainComponent.getWidth();
	}
	
	public void setDragPosition(int position) {
		mDragPosition = position;
		requestLayout();
		invalidate();
	}
	
	public int getDragPosition() {
		return mDragPosition; 
	}
	
	public int getTargetDragPosition() {
		return mTargetDragPosition;
	}
	
	/**
	 * 模拟拖动，在当前的位置基础上进行拖动。
	 * 
	 * @param distance 拖动距离
	 * @return
	 */
	public boolean fakeDrag(int distance) {
		if (0 == distance) {
			return false;
		}
		
		int newPos = mDragPosition + distance;
		if (distance > 0) {
			// 判断能不能继续拖出左组件（向右拖）
			if (null == mLeftDragComponent && mDragPosition < 0) {
				if (newPos > 0) {
					newPos = 0;
				}
			} else { 
				if (null == mLeftDragComponent 
					|| ((mDragPosition - mLeftDragComponent.getWidth() >= 0) 
							&& (mDragPosition > 0))) {
					return false;
				}
				if (newPos - mLeftDragComponent.getWidth() > 0) {
					newPos = mLeftDragComponent.getWidth();
				}
			}
		} else {
			// 判断能不能继续拖出右组件（向左拖）
			if (null == mRightDragComponent && mDragPosition > 0) {
				if (newPos < 0) {
					newPos = 0;
				}
			} else { 
				if (null == mRightDragComponent 
						|| (mRightDragComponent.getWidth() + mDragPosition <= 0) 
						&& (mDragPosition < 0)) {
					return false;
				}
				if (newPos + mRightDragComponent.getWidth() < 0) {
					newPos = -mRightDragComponent.getWidth();
				}
			}
		}
		
		setDragPosition(newPos);
		return true;
	}
	
}
