package com.eebbk.mingming.k7ui.accessibility;

import com.eebbk.mingming.k7utils.LogUtils;

import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * 
 * 长按检测器。 </br>
 * 能够检测到长按开始和长按结束。
 * 使用 {@link GestureDetector} 实现，所以会屏蔽原来的点击事件，因此增加了相应的点击接口。
 * 
 * </br>
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class LongPressDetector implements OnGestureListener {
	
	private boolean mInDown;
	private boolean mInLongPress;
	
	private Rect mRect;
	private View mHostView;
	
	private GestureDetector mGestureDetector;
	private LongPressDetectorListener mListener;
	
	
	/**
	 * 
	 * 长按检测器监听器
	 * 
	 * @author humingming <humingming@oaserver.dw.gdbbk.com>
	 *
	 */
	public interface LongPressDetectorListener {
		
		/**
		 * 点击事件
		 * 
		 * @param view 
		 */
		//public void onClickWithoutLongPress(View view);
		
		/**
		 * 长按按下
		 * 
		 * @param view
		 */
		public void onLongPressDown(View view);
		
		/**
		 * 长按松手
		 * 
		 * @param view
		 */
		public void onLongPressUp(View view);
		
	}
	
	
	public LongPressDetector(Context context, View hostView) {
		this(context, hostView, null);
	}
	
	public LongPressDetector(Context context, View hostView, LongPressDetectorListener listener) {
		mInDown = true;
		mInLongPress = false;
		
		mRect = new Rect(0, 0, 0, 0);
		mHostView = hostView;
		
		mGestureDetector = new GestureDetector(context, this);
		mGestureDetector.setIsLongpressEnabled(true);
		
		mListener = listener;
	}
	
    /**
     * 在 Host View {@link View#onTouchEvent(MotionEvent)} 分析长按事件。
     * 这个是激发长按回调的基本分析过程，要在 Host View 的 onTouchEvent 中调用。
     * 
     * @param ev see {@link View#onTouchEvent(MotionEvent)}
     * @return True 触摸事件被 Host View 接收，false 不被 Host View 接收。
     */
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getActionMasked();
		if (mInLongPress) {
			switch (action) {
			case MotionEvent.ACTION_UP: 
			case MotionEvent.ACTION_CANCEL:
			{
				onUp();
				if (null != mListener) {
					mListener.onLongPressUp(mHostView);
				}
				mInLongPress = false;
				return true;
			}
			
			case MotionEvent.ACTION_MOVE: {
				if (null != mHostView) {
					final int x = (int) ev.getX();
					final int y = (int) ev.getY();
					mRect.set(0, 0, mHostView.getWidth(), mHostView.getHeight());
					LogUtils.d("haha", "hostview: " + mRect.toString() + "move: " + x + ", " + y);
					if (!mRect.contains(x, y)) {
						onUp();
						if (null != mListener) {
							mListener.onLongPressUp(mHostView);
						}
						mInLongPress = false;
						return true;
					}
				}
				break;
			}
			
			default:
				break;
			}
		}
		
		boolean handled = mGestureDetector.onTouchEvent(ev);
		if (handled) {
			return true;
		} else {
			if (mInDown) {
				switch (action) {
				case MotionEvent.ACTION_UP: {
					onUp();
				}
				
				default:
					break;
				}
			}
			return false;
		}
	}
	
    /**
     * 根据 Host View 的宽高设定点击，长按事件。
     * 这个是激发滑动回调的基本分析过程，要在 Host View 的 onMeasure 最后调用。
     * 
     * @param width Host View 宽度（注意这里宽度是要 EXACTLY 的）
     * @param height Host View 高度（注意这里高度是要 EXACTLY 的）
     */
    public void onMeasure(int width, int height) {        
        mRect.set(0, 0, width, height);
    }
	
	public void setLongPressDetectorListener(LongPressDetectorListener listener) {
		mListener = listener;
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		onDown();
		return true;
	}
	
	@Override
	public void onLongPress(MotionEvent e) {
		mInLongPress = true;
		
		if (null != mListener) {
			mListener.onLongPressDown(mHostView);
		}
	}
	
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		onUp();
		//if (null != mListener) {
		//	mListener.onClickWithoutLongPress(mHostView);
		//	mHostView.performClick();
		//}
		if (null != mHostView) {
			return false;
		}
		
		// 让 Host view 能够响应点击事件
		return mHostView.performClick();
		//return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	
	private void onDown() {
		mInDown = true;
		
		if (null != mHostView) {
			mHostView.setPressed(true);
		}
	}
	
	private void onUp() {
		mInDown = false;
		
		if (null != mHostView) {
			mHostView.setPressed(false);
		}
	}

}
