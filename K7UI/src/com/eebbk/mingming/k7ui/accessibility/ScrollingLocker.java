package com.eebbk.mingming.k7ui.accessibility;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * 
 * {@link AbsListView} Scrolling locker
 * In order to make scroll smoothing, you should lock some cost time operate.
 * e.g complex view re-draw, start back threads and so on.
 * 
 * 注意，锁定更新的时候，只在数据内容发生锁定，如果 Adapter 的内容数目发生变化，那么还是要通知
 * Adapter 变化的，否则会报异常的。 ListView 更新的时候要自己的保存的 Item 个数和 Adapter 一样的。
 * 
 * </p>
 * TODO: should make the lock more smart, e.g lock until some fast scrolling.
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class ScrollingLocker implements OnScrollListener  {

	@SuppressWarnings("unused")
	private final static String TAG = "ScrollingLocker";
	
	/** Lock when just start scroll. */
	public final static int LOCK_WHEN_SCROLL = 0;
	
	/** Lock only reach fling state(very fast scroll speed) */
	public final static int LOCK_ONLY_FLING = 1;
	
	/** Lock level: {@link #LOCK_WHEN_SCROLL}, {@link #LOCK_WHEN_SCROLL} */
	private int mLockLevel;
	
	/** Scroll state */
	private int mScrollState;
	
	private AbsListView mTagListView = null;
	private ScrollingLockerListener mListener = null;
	
	
	/**
	 * 
	 * Scroll lock listener.
	 * 
	 * @author humingming <humingming@oaserver.dw.gdbbk.com>
	 *
	 */
	public interface ScrollingLockerListener {
		
		/**
		 * Lock update. In this case you should stop cost time operate.
		 */
		public void onLockUpdate();
		
		/**
		 * Unlock update. In this case you can do some cost time operate.
		 * And if some UI update occur in lock state, you should update once.
		 */
		public void onUnlockUpdate();
		
	}
	
	public ScrollingLocker() {
		this(null, null, LOCK_WHEN_SCROLL);
	}
	
	public ScrollingLocker(AbsListView tagListView, ScrollingLockerListener listener) { 
		this(tagListView, listener, LOCK_WHEN_SCROLL);
	}
	
	public ScrollingLocker(AbsListView tagListView, ScrollingLockerListener listener, int lockLevel) {
		mTagListView = tagListView;
		mListener = listener;
		
		mLockLevel = lockLevel;
		mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
		
		if (null != mTagListView) {
			mTagListView.setOnScrollListener(this);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;
		
		switch (scrollState) {
		case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
			//LogUtils.d(TAG, "scroll fling: lock update !");
			if (null != mListener) {
				mListener.onLockUpdate();
			}
			break;
			
		case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
			//LogUtils.d(TAG, "scroll idle: unlock update !");
			if (null != mListener) {
				mListener.onUnlockUpdate();
			}
			break;
			
		case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			//LogUtils.d(TAG, "scroll touch scroll: lock update !");
			if (null != mListener) {
				 if (LOCK_WHEN_SCROLL == mLockLevel) {
					 mListener.onLockUpdate();
				 } else {
					 mListener.onUnlockUpdate();
				 }
			}
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Set scroll lock listener
	 * 
	 * @param listener Object of {@link ScrollingLockerListener}
	 */
	public void setScrollingLockerListener(ScrollingLockerListener listener) {
		mListener = listener;
	}
	
	/**
	 * Set lock level.
	 * 
	 * @param lockLevel {@link #LOCK_WHEN_SCROLL}, or {@link #LOCK_ONLY_FLING}.
	 */
	public void setLockLevel(int lockLevel) {
		mLockLevel = lockLevel;
	}
	
	/**
	 * Get current scroll state
	 * 
	 * @return {@link AbsListView.OnScrollListener.SCROLL_STATE_IDLE} and so on.
	 */
	public int getScrollState() {
		return mScrollState;
	}
}
