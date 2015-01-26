package com.eebbk.mingming.k7ui.demo.help;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * 
 * 手势容器。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class GestureContainer extends FrameLayout implements OnGestureListener {
	
	/**
	 * 
	 * 手势容器监听器。只是对 {@link OnGestureListener} 简单的封装。
	 * 
	 * @author humingming <humingming@oaserver.dw.gdbbk.com>
	 *
	 */
	public interface GestureContainerListener {
		
		/**
		 * See {@link OnGestureListener#onFling(MotionEvent, MotionEvent, float, float)}.
		 * 
		 * @param e1
		 * @param e2
		 * @param velocityX
		 * @param velocityY
		 * @return
		 */
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
		
	}
	
	private GestureDetector mGestureDetector = null;
	private GestureContainerListener mListener = null;
	
	public GestureContainer(Context context) {
		super(context);
		
		init(context);
	}
	
	public GestureContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}
	
	public GestureContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}
	
	private void init(Context context) {
		mGestureDetector = new GestureDetector(context, this);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		
		if (null != mListener) {
			return mListener.onFling(e1, e2, velocityX, velocityY);
		}
		
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
		//return super.onInterceptTouchEvent(event);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// 不阻挡 UP 事件，否则 ListView 的 item 点击显示有时有些问题
		if (mGestureDetector.onTouchEvent(event) && MotionEvent.ACTION_UP != event.getAction()) {
			return true;
		}
		
		return super.dispatchTouchEvent(event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event) && MotionEvent.ACTION_UP != event.getAction()) {
			return true;
		}
		
		return super.onTouchEvent(event);
	}
	
	/**
	 * 设置手势容器监听器
	 * 
	 * @param listener Object of {@link GestureContainerListener}
	 */
	public void setGestureContainerListener(GestureContainerListener listener) {
		mListener = listener;
	}

}
