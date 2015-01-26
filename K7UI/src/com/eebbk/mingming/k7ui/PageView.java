package com.eebbk.mingming.k7ui;

import com.eebbk.mingming.k7ui.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * Page view. A unit as a child view in {@link ScrollContainer}.
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class PageView extends ViewGroup {
	
	@SuppressWarnings("unused")
	private final static String TAG = "PageView";
	
	protected final static int DEFAULT_COUNT_X = 3;
	protected final static int DEFAULT_COUNT_Y = 2;
	
	protected final static int DEFAULT_SPACE = 0;
	
	protected final static int INDEX_X = 0;
	protected final static int INDEX_Y = 1;
	
	
	/** 
	 * Count of item in horizontal.
	 * if set to -1, it will layout child with child count.
	 */
	protected int mCountX;
	
	/** Count of item in vertical. */
	protected int mCountY;
	
	/** Horizontal item space padding. */
	protected int mSpaceX;
	
	/** Vertical item space padding. */
	protected int mSpaceY;
	
	/** Left padding.(not effect child) */
	protected int mSpaceLeft;
	
	/** Right padding.(not effect child) */
	protected int mSpaceRight;
	
	/** Top padding.(not effect child) */
	protected int mSpaceTop;
	
	/** Bottom padding.(not effect child) */
	protected int mSpaceBottom;
	
	private int[] mItemIndex;
	
	protected int mItemWidth;
	protected int mItemHeight;
	
	protected int mChildMaxWidth;
	protected int mChildMaxHeight;
	

	public PageView(Context context) {
		this(context, null);
	}
	
	public PageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageView, defStyle, 0);

        mCountX = a.getInteger(R.styleable.PageView_k7uipvCountX, DEFAULT_COUNT_X);
        mCountY = a.getInteger(R.styleable.PageView_k7uipvCountY, DEFAULT_COUNT_Y);
        
        mSpaceX = (int) a.getDimension(R.styleable.PageView_k7uipvSpaceX, DEFAULT_SPACE);
        mSpaceY = (int) a.getDimension(R.styleable.PageView_k7uipvSpaceY, DEFAULT_SPACE);
        
        mSpaceLeft = (int) a.getDimension(R.styleable.PageView_k7uipvSpaceLeft, DEFAULT_SPACE);
        mSpaceRight = (int) a.getDimension(R.styleable.PageView_k7uipvSpaceRight, DEFAULT_SPACE);
        mSpaceTop = (int) a.getDimension(R.styleable.PageView_k7uipvSpaceTop, DEFAULT_SPACE);
        mSpaceBottom = (int) a.getDimension(R.styleable.PageView_k7uipvSpaceBottom, DEFAULT_SPACE);

        a.recycle();
        
        mItemIndex = new int[2];
        
    	mItemWidth = 0;
    	mItemHeight = 0;;
        
		mChildMaxWidth = 0;
		mChildMaxHeight = 0;
	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height =  MeasureSpec.getSize(heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        
    	if ((widthMode == MeasureSpec.UNSPECIFIED && 0 == width) || 
    			(heightMode == MeasureSpec.UNSPECIFIED && 0 == height)) {
    		//throw new RuntimeException("PageView cannot have UNSPECIFIED dimensions");
    		
    		computeChildMaxSize();
    			
    		// TODO: now only support horizontal.
    		if (widthMode == MeasureSpec.UNSPECIFIED && 0 == width) {
    			width = mChildMaxWidth * getChildCount();
    		}
    			
    		if (heightMode == MeasureSpec.UNSPECIFIED && 0 == height) {
    			height = mChildMaxHeight;
    		}
        }
        
        int validWidth = width - getPaddingLeft() - getPaddingRight();
        int validHeight = height - getPaddingBottom() - getPaddingTop();
        
        int countX = mCountX;
        if (countX <= -1) {
        	countX = getChildCount();
        }
        
        if (countX <= 0) {
        	setMeasuredDimension(width, height);
        	return;
        }
        
        int countY = mCountY;
        if (countY <= 0) {
        	countY = 1;
        }
        
        int itemTotalWidth = (validWidth - mSpaceLeft - mSpaceRight) - (mSpaceX * (countX - 1));
        int itemTotalHeight = (validHeight - mSpaceTop - mSpaceBottom) - (mSpaceY * (countY - 1));
        
        int itemWidth = itemTotalWidth / countX;
        int itemHeight = itemTotalHeight / countY;
        
        // save the measure item size, for child class use.
        mItemWidth = itemWidth;
        mItemHeight = itemHeight;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY);
            int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;
        
        int validWidth = width - getPaddingLeft() - getPaddingRight();
        int validHeight = height - getPaddingBottom() - getPaddingTop();
        
        int countX = mCountX;
        if (countX <= -1) {
        	countX = getChildCount();
        }
        
        if (countX <= 0) {
        	return;
        }
        
        int countY = mCountY;
        if (countY <= 0) {
        	countY = 1;
        }
        
        int itemTotalWidth = (validWidth - mSpaceLeft - mSpaceRight) - (mSpaceX * (countX - 1));
        int itemTotalHeight = (validHeight - mSpaceTop - mSpaceBottom) - (mSpaceY * (countY - 1));
        
        int itemWidth = itemTotalWidth / countX;
        int itemHeight = itemTotalHeight / countY;

        int itemBaseLeft = mSpaceLeft + getPaddingLeft();
        int itemBaseTop = mSpaceTop + getPaddingTop();
        
        int itemLeft = itemBaseLeft;
        int itemTop = itemBaseTop;
        
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            
            computeItemIndex(i, mItemIndex);
            itemLeft = itemBaseLeft + ((itemWidth + mSpaceX) * mItemIndex[INDEX_X]); 
            itemTop = itemBaseTop + ((itemHeight + mSpaceY)* mItemIndex[INDEX_Y]);
            
            child.layout(itemLeft, itemTop, itemLeft + itemWidth, itemTop + itemHeight);
        }
	}
	
	/**
	 * Compute give item cell position index.
	 * 
	 * @param index One-dimensional item index .
	 * @param output int[2] array output.
	 */
	protected final void computeItemIndex(int index, int[] output) {
		if (null == output || output.length < 2) {
			return;
		}
		
        int countX = mCountX;
        if (countX <= -1) {
        	countX = getChildCount();
        }
		
		if (countX <= 0) {
			output[INDEX_X] = 0;
			output[INDEX_Y] = 0;
		} else {
			output[INDEX_X] = index % countX;
			output[INDEX_Y] = index / countX;
		}
	}
	
	/**
	 * Compute child max size, for measure not not specified size.
	 */
	protected final void computeChildMaxSize() {
		int maxWidth = 0;
		int maxHeight = 0;
		int measured = MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST);
		View child = null;
		
		for (int i = 0; i < getChildCount(); i++) {
			child = getChildAt(i);
			if (null == child) {
				continue;
			}
			
			child.measure(measured, measured);
			
			if (child.getMeasuredWidth() > maxWidth) {
				maxWidth = child.getMeasuredWidth();
			}
			
			if (child.getMeasuredHeight() > maxHeight) {
				maxHeight = child.getMeasuredHeight();
			}
		}
		
		mChildMaxWidth = maxWidth;
		mChildMaxHeight = maxHeight;
	}
	
	public int getCountX() {
		return mCountX;
	}
	
	public int getCountY() {
		return mCountY;
	}
	
	public int getSpaceX() {
		return mSpaceX;
	}
	
	public int getSpaceY() {
		return mSpaceY;
	}
	
	public int getSpaceLeft() {
		return mSpaceLeft;
	}
	
	public int getSpaceRight() {
		return mSpaceRight;
	}
	
	public int getSpaceTop() {
		return mSpaceTop;
	}
	
	public int getSpaceBottom() {
		return mSpaceBottom;
	}
	
	
	public void setCountX(int countX) {
		if (countX <= 0 || 
				mCountX == countX) {
			return;
		}
		
		mCountX = countX;
		requestLayout();
	}
	
	public void setCountY(int countY) {
		if (countY <= 0 || 
				mCountY == countY) {
			return;
		}
		
		mCountY = countY;
		requestLayout();
	}
	
	public void setSpaceX(int spaceX) {
		if (mSpaceX == spaceX) {
			return;
		}
		
		mSpaceX = spaceX;
		requestLayout();
	}
	
	public void setSpaceY(int spaceY) {
		if (mSpaceY == spaceY) {
			return;
		}
		
		mSpaceY = spaceY;
		requestLayout();
	}
	
	public void setSpaceLeft(int spaceLeft) {
		if (mSpaceLeft == spaceLeft) {
			return;
		}
		
		mSpaceLeft = spaceLeft;
		requestLayout();
	}
	
	public void setSpaceRight(int spaceRight) {
		if (mSpaceRight == spaceRight) {
			return;
		}
		
		mSpaceRight = spaceRight;
		requestLayout();
	}
	
	public void setSpaceTop(int spaceTop) {
		if (mSpaceTop == spaceTop) {
			return;
		}
		
		mSpaceTop = spaceTop;
		requestLayout();
	}
	
	public void setSpaceBottom(int spaceBottom) {
		if (mSpaceBottom == spaceBottom) {
			return;
		}
		
		mSpaceBottom = spaceBottom;
		requestLayout();
	}
	
	
	// TODO: not use yet.
	public static class PageViewLayoutParams extends ViewGroup.MarginLayoutParams {
		
		/** horizontal item space padding. */
		private int mSpaceX;
		
		/** vertical item space padding. */
		private int mSpaceY;
		

        public PageViewLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            
            mSpaceX = 10;
            mSpaceY = 10;
        }

        public PageViewLayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            
            mSpaceX = 10;
            mSpaceY = 10;
        }

        public PageViewLayoutParams(PageViewLayoutParams source) {
            super(source);
            
            this.mSpaceX = source.mSpaceX;
            this.mSpaceY = source.mSpaceY;
        }

        public PageViewLayoutParams(int spaceX, int spaceY) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            
            this.mSpaceX = spaceX;
            this.mSpaceY = spaceY;
        }
        
        public int getSpaceX() {
            return this.mSpaceX;
        }

        public int getSpaceY() {
            return this.mSpaceY;
        }

        public void setup(int spaceX, int spaceY) {
            this.mSpaceX = spaceX;
            this.mSpaceY = spaceY;
        }

        public String toString() {
            return "(" + this.mSpaceX + ", " + this.mSpaceY + ")";
        }

        public void setSpaceX(int spaceX) {
            this.mSpaceX = spaceX;
        }

        public void setSpaceY(int spaceY) {
            this.mSpaceY = spaceY;
        }
    }

}
