package com.eebbk.mingming.k7ui;

import com.eebbk.mingming.k7ui.accessibility.ScrollDetector;
import com.eebbk.mingming.k7ui.accessibility.ScrollDetector.ScrollDetectorListener;
import com.eebbk.mingming.k7ui.bounceview.BounceListView;
import com.eebbk.mingming.k7utils.LogUtils;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * 
 * 支持拖动 Item 的 {@link ListView}。 </br>
 * 在 ListView 这种本身可以滑动的 View 中没办法很好的直接处理 Item 的拖动，
 * 需要在 Parent 这层处理。蛋疼得紧张东西，也许还会存在着一些问题，先凑活用吧。 </br>
 * 
 * </br>
 * <b>注意：</b> 使用 {@link #ENTER_DRAG_MODE_LONG_PRESSED} 模式的时候不要自己
 * 设置 ListView 的 Item 长按监听事件。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public abstract class DragItemListView extends BounceListView implements 
	ScrollDetectorListener, OnItemLongClickListener {
	
	private final static String TAG = "DragItemListView";
	
	/** 长按进入拖动模式 （推荐用这种）*/
	public final static int ENTER_DRAG_MODE_LONG_PRESSED = 0;
	
	/** 检测滑动进入拖动模式 */
	public final static int ENTER_DRAG_MODE_DETECT_SCROLL = 1;
	
	
	protected boolean mInDrag;
	protected boolean mCancelPerformClick;
	
	protected PointF mFlingVelocity;
	
	protected int mEnterDragMode;
	
	protected View mDragView;
	protected View mTargetItemView;
	protected int mTargetItemPosition;
	
	protected ScrollDetector mScrollDetector;
	protected DragItemListViewListener mListener;
	
	
	/**
	 * 
	 * 拖动 ListView 监听器
	 * 
	 * @author humingming <humingming@oaserver.dw.gdbbk.com>
	 *
	 */
	public interface DragItemListViewListener {
		
		/**
		 * 从 ListView 的 Item 中找到需要拖动的 Item 
		 * 
		 * @param itemContainer Item View
		 * @return 需要拖动的 View 
		 */
		public View findDragView(View itemContainer);
		
	}
	
	
	public DragItemListView(Context context) {
		super(context);
		init(context, null, 0);
	}
	
	public DragItemListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	
	public DragItemListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle) {
		mEnterDragMode = ENTER_DRAG_MODE_LONG_PRESSED;
		
		mInDrag = false;
		mCancelPerformClick = false;
		
		mFlingVelocity = new PointF(0, 0);
		
		mTargetItemView = null;
		mTargetItemPosition = -1;
		mDragView = null;
		
		mScrollDetector = new ScrollDetector(context, this, 
				ScrollDetector.HORIZONTAL, this);
		mListener = null;
		
		setEnterDragMode(mEnterDragMode);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		mScrollDetector.onMeasure(getWidth(), getHeight());
	}
	
	public boolean dispatchTouchEvent(MotionEvent event) {
		// 这里不能在 onInterceptTouchEvent 中处理，因为让 ListView 自己的
		// dispatchTouchEvent 处理之后，很多事件 onInterceptTouchEvent 接收不到了。
		
		boolean drag = false;
		boolean oldInDrag = mInDrag;
		
		switch (mEnterDragMode) {
		case ENTER_DRAG_MODE_DETECT_SCROLL:
			mInDrag = mScrollDetector.onInterceptTouchEvent(event);
			break;
		
		default:
		case ENTER_DRAG_MODE_LONG_PRESSED:
			drag = mScrollDetector.onInterceptTouchEvent(event);
			if (mInDrag) {
				mInDrag = drag;
			}
			break;
		}
		
		//LogUtils.d(TAG, "dispatch: drag: " + drag + ", act: " + event.getActionMasked() 
		//		+ ", x:" + event.getX() + " y:" + event.getY());
		
		if (mInDrag && !oldInDrag) {
			// 相当于 onDown 的处理。这里没办法在 onDown 中处理。
			// 因为这里需要滑动一些才能判断出是 ListView 本身的滑动，还是拖动。
			// 这段判断的滑动时间，有可能最后确定是拖动位置与接收到 onDown 的位置不是同一个位置。
			// 所以需要在这里处理。
			if (ENTER_DRAG_MODE_DETECT_SCROLL == mEnterDragMode) {
				startDrag(event);
			}
			
		} else if (!mInDrag && oldInDrag) {
			// 相当于 onUp 的处理。这里也同样没办法在 onUp 中处理。
			// 因为在拖动结束后不能正常的让 ListView 接收到 ACTION_UP 事件，因为在拖动结束后需要取消点击事件的响应。
			// 和上面说的原因一样，在判断拖动的这段 touch 事件中， ListView 会接收到激发点击事件的 ACTION_DOWN 事件。
			// 所以拖动结束后不能让 ACTION_UP 事件发下去。所以在这里 onUp 事件就无法触发了，因为 ACTION_UP 的时候上面的
			// ScrollDetector 就会判断不处于拖动状态了。
			endDrag(event);
		}
		
		return super.dispatchTouchEvent(event);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// 处于拖动状态下直接返回，不让子 View 接收事件
		if (mInDrag) {
			return true;
		}
		
		//LogUtils.d(TAG, "onInterceptTouchEvent: drag: " + mInDrag + " , act: " + event.getActionMasked()
		//		+ ", enterMode: " + mEnterDragMode);
		
		return super.onInterceptTouchEvent(event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//LogUtils.d(TAG, "onTouchEvent: drag: " + mInDrag + " , act: " + event.getActionMasked()
		//		+ ", enterMode: " + mEnterDragMode);
		
		int action = event.getActionMasked();
		
		// 处于拖动状态下， 仅仅让 ScrollDetector 能够接收事件，处理拖动。
		// 禁止掉所有 ListView 的事件。
		switch (mEnterDragMode) {
		case ENTER_DRAG_MODE_DETECT_SCROLL:
			if (mInDrag) {
				// TODO: 这里先简单的这么处理先，不管 View 能不能拖动
				//if (mScrollDetector.onTouchEvent(event)) {
				//	return true;
				//}
				mScrollDetector.onTouchEvent(event);
				return true;
			}
			break;
		
		default:
		case ENTER_DRAG_MODE_LONG_PRESSED:
			mScrollDetector.onTouchEvent(event);
			if (mInDrag) {
				return true;
			}
			break;
		}
		
		if (MotionEvent.ACTION_UP == action) {
			if (mCancelPerformClick) {
				// 如果需要取消点击事件，把 ACTION_UP 事件换成 ACTION_CANCEL，
				// 然后再交给 ListView 处理。
				mCancelPerformClick = false;
				event.setAction(MotionEvent.ACTION_CANCEL);
			}
		}
		
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mInDrag = true;
		
		mTargetItemView = view;
		mTargetItemPosition = position;
		mDragView = getDragView(view);
		
		// TODO: 这里返回 false 不吃掉触发事件
		// 不然的话，会把点击显示效果去掉，感觉怪怪的。
		// 本来不想要点击显示的小效果，但是要触发长按点击，就会有，所以然它一直显示好了。
		return false;
	}
	
	@Override
	public boolean onDetectScrolling() {
		return mInDrag;
	}
	
	@Override
	public boolean onDown(MotionEvent e) {		
		return true;
	}
	
	@Override
	public boolean onUp(MotionEvent e) {
		return true;
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		
		if (!mInDrag) {
			return false;
		}
		
		if (null == mDragView) {
			LogUtils.e(TAG, "onScroll drag view is null, we can't drag !");
			return false;
		}
		
		return performDrag(mDragView, mTargetItemPosition, distanceX);
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	
	/**
	 * 设置监听器。（子类要实现这个监听器才能正确的处理拖动）
	 * 
	 * @param listener
	 */
	public void setListener(DragItemListViewListener listener) {
		mListener = listener;
	}
	
	/**
	 * 设置进入拖动的模式
	 * 
	 * @param enterDragMode {@link #ENTER_DRAG_MODE_LONG_PRESSED}, {@link #ENTER_DRAG_MODE_DETECT_SCROLL}
	 */
	public void setEnterDragMode(int enterDragMode) {		
		mEnterDragMode = enterDragMode;
		
		if (ENTER_DRAG_MODE_LONG_PRESSED == mEnterDragMode) {
			setOnItemLongClickListener(this);
		} else {
			setOnItemLongClickListener(null);
		}
		
		cancelCurrentDrag();
	}
	
	/**
	 * 获取当前设置的进入拖动模式
	 * 
	 * @return
	 */
	public int getEnterDragMode() {
		return mEnterDragMode;
	}
	
	protected final boolean findMotionView(int y) {
		int firstPosition = getFirstVisiblePosition();
		int viewPos = findMotionRow(y) - firstPosition;
		//LogUtils.d("mingming", "findMotionView=" + y + " , with view pos=" + viewPos 
		//		+ ", firstPos=" + firstPosition);
		if (INVALID_POSITION == viewPos) {
			mTargetItemView = null;
			mTargetItemPosition = -1;
			return false;
		}
		
		mTargetItemView = getChildAt(viewPos);
		if (null == mTargetItemView) {
			mTargetItemPosition = -1;
			return false;
		}
		
		mTargetItemPosition = viewPos + firstPosition;
		return true;
	}
	
	// 从 ListView 代码中扣出来的
	protected final int findMotionRow(int y) {
    	int firstPosition = getFirstVisiblePosition();
        int childCount = getChildCount();
        
        if (childCount > 0) {
            if (!isStackFromBottom()) {
                for (int i = 0; i < childCount; i++) {
                    View v = getChildAt(i);
                    if (y <= v.getBottom()) {
                        return firstPosition + i;
                    }
                }
            } else {
                for (int i = childCount - 1; i >= 0; i--) {
                    View v = getChildAt(i);
                    if (y >= v.getTop()) {
                        return firstPosition + i;
                    }
                }
            }
        }
        
        return INVALID_POSITION;
    }
	
	/**
	 * 开始拖动
	 * 
	 * @param dragView
	 * @param position
	 */
	protected abstract void startDrag(View dragView, int position);
	
	/**
	 * 结束拖动
	 * 
	 * @param dragView
	 * @param position
	 * @param velocityX
	 * @param velocityY
	 */
	protected abstract void endDrag(View dragView, int position, float velocityX, float velocityY);
	
	/**
	 * 实现拖动
	 * 
	 * @param dragView
	 * @param position
	 * @param disX
	 */
	protected abstract boolean performDrag(View dragView, int position, float disX);
    
	
	private void startDrag(MotionEvent event) {
		//mTargetItemView = findMotionView((int)event.getY());
		//mDragView = getDragView(mTargetItemView);
		if (!findMotionView((int)event.getY())) {
			LogUtils.e(TAG, "we can't find drag target view !!");
			return;
		}
		
		mDragView = getDragView(mTargetItemView);
		if (null == mDragView) {
			LogUtils.e(TAG, "we can't find drag target view !!");
			return;
		}
		
		mFlingVelocity.set(0, 0);
		startDrag(mDragView, mTargetItemPosition);
	}
	
	private void endDrag(MotionEvent event) {
		if (null != mDragView) {
			endDrag(mDragView, mTargetItemPosition, 
					// 计算下这次 drag 的滑动速度
					// 由于在 dispatchTouchEvent 调用 ScrollDetector 的 onInterceptTouchEvent
					// 所以这里 Scroll Detector 无法正常计算 fling 了，需要在这里手动取一下 
					mScrollDetector.getFlingVelocityX(), 
					mScrollDetector.getFlingVelocityY());
		}
		
		mTargetItemView = null;
		mTargetItemPosition = -1;
		mDragView = null;
		
		// 设置一个标志，是需要取消点击事件的
		mCancelPerformClick = true;
	}
	
    private View getDragView(View targetItemView) {
    	if (null == mListener) {
    		return null;
    	}
    	
    	return mListener.findDragView(targetItemView);
    }
    
    private void cancelCurrentDrag() {
    	if (mInDrag) {
    		mInDrag = false;
    		MotionEvent ev = MotionEvent.obtain(System.currentTimeMillis(), 
    	    		System.currentTimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0);
    		dispatchTouchEvent(ev);
    	}
    }
    
}
