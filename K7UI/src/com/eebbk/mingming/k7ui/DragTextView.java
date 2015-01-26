package com.eebbk.mingming.k7ui;

import com.eebbk.mingming.k7ui.accessibility.ScrollDetector;
import com.eebbk.mingming.k7ui.accessibility.ScrollDetector.ScrollDetectorListener;
import com.eebbk.mingming.k7utils.LogUtils;
import com.eebbk.mingming.k7utils.ReflectUtils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * 
 * 可拖拽的 {@link TextView}。 </br>
 * 当文本大于 TextView 宽度，显示 "..." 时候，可以拖拽显示。
 * 用了这个之后不要用 Padding Left 或是 Padding Right 了（因为这个效果是简单的使用 padding 来实现的），
 * 用 Margin 代替吧。也不要使用 left、 right Drawable 。 
 * 
 * TODO: 支持竖直方向的 ？？
 * 
 * </br>
 * 这个目标会吃掉 View 原本的点击事件，并且让 ListView 的 Item 无法点击，所以使用的时候请慎重。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class DragTextView extends TextView implements TextWatcher, ScrollDetectorListener {
	
	private final static String TAG = "DragTextView";
	
	// 方便调用调试（拿反射改，final 其实反射好像也能改的，但是我暂时还没弄成功，降低点难度吧，把 final 去掉）
	private static boolean DEBUG = false;
	
	private final static boolean DEFAULT_AUTO_RESTORE_DRAG = true;
	private final static boolean DEFAULT_ENABLE_DRAG = true;
	private final static float DEFAULT_OVER_SCROLL_FACTOR = 0.1f;
	
	private int mOrgPaddingLeft;
	private int mOrgPaddingRight;
	
	private boolean mAutoDragRestore;
	private boolean mEnableDrag;
	
	private Paint mParentTextPaint = null;
	private int mOverScrollBound;
	private float mOverScrollFactor;
	private Rect mRcTextBound;
	
	private PaddingObject mPaddingObject;
	private ObjectAnimator mDragRestoreAnim;
	
	private ScrollDetector mScrollDetector = null;
	
	
	public DragTextView(Context context) {
		super(context);
		init(context, null, 0);
	}
	
	public DragTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	
	public DragTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle) {
		if (null != attrs) {
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
		}
		
		try {
			mParentTextPaint = (Paint) ReflectUtils.getFieldObject(
					TextView.class, this, "mTextPaint");
		} catch (Exception e) {
			e.printStackTrace();
			mParentTextPaint = null;
		}
		
		mOrgPaddingLeft = 0;
		mOrgPaddingRight = 0;
		
		mPaddingObject = new PaddingObject();
		mDragRestoreAnim = ObjectAnimator.ofInt(mPaddingObject, "PaddingProperty", 0, 1);
		
		mOverScrollBound = 0;
		mRcTextBound = new Rect(0, 0, 0, 0);
		
		mScrollDetector = new ScrollDetector(context, this, ScrollDetector.HORIZONTAL, this);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		addTextChangedListener(this);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		removeTextChangedListener(this);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		mOverScrollBound = (int) (mOverScrollFactor * (float)getMeasuredWidth());
		computeDragBound();
		
		mScrollDetector.onMeasure(getWidth(), getHeight());
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}
	
	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		setPadding(left, top, right, bottom, false);
	}
	
	//public boolean dispatchTouchEvent(MotionEvent event) {
		/*final int action = event.getActionMasked();
		if (DEBUG) LogUtils.d(TAG, "dispatchTouch: act: " + action);
		switch (action) {
		case MotionEvent.ACTION_UP:
			if (mAutoDragRestore) {
				restorePadding();
				return true;
			}
			break;
		
		default:
			break;
		}*/
		
		//if (mScrollDetector.onInterceptTouchEvent(event)) {
		//	return true;
		//}
		
	//	return super.dispatchTouchEvent(event);
	//}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {		
		if (mEnableDrag) {
			if (mScrollDetector.onTouchEvent(event)) {
				return true;
			}
		}
		
		return super.onTouchEvent(event);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterTextChanged(Editable s) {
		computeDragBound();
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}
	
	@Override
	public boolean onDetectScrolling() {
		return false;
	}
	
	@Override
	public boolean onUp(MotionEvent e) {
		if (mAutoDragRestore) {
			restoreDrag();
		}
		
		return true;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		
		if (DEBUG) LogUtils.d(TAG, "disX: " + distanceX);
		
		switch (getEllipsize()) {
		case START:
			return onDragByMoreLeft(distanceX);
		
		case END:
			return onDragByMoreRight(-distanceX);
			
		case MIDDLE:
		case MARQUEE:
		default:
			return false;
		}
	}
	
	private boolean onDragByMoreLeft(float disX) {
		int correct = (int)Math.abs(disX);
		if (0 == correct) {
			if (DEBUG) LogUtils.d(TAG, "distance almost zero, we ignore it");
			return false;
		}
		
		int oldPadding = getPaddingRight();
		int newPadding = getPaddingRight() + (int)disX;
		int width = getWidth();
		int oldShowRange = -oldPadding + width;
		int showRange = -newPadding + width;
		
		if (DEBUG) LogUtils.d(TAG, "oldPadding: " + oldPadding + ", oldShowRange: " + oldShowRange
				+ ", showRange: " + showRange + ", newPadding: " + newPadding 
				+ ", overScrollBound: " + mOverScrollBound + ", textBound: " + mRcTextBound.width() 
				+ ", width: " + width + ", orgPaddngRight: " + mOrgPaddingRight + 
				", disX: " + disX);
		
		if ((oldShowRange >= mRcTextBound.width() + mOverScrollBound) && (disX < 0)) {
			// 左滑 Over scroll
			if (DEBUG) LogUtils.d(TAG, "left scroll over !!, oldPadding: " + oldPadding);
			return false;
		} else if ((oldPadding >= mOrgPaddingRight /*+ mOverScrollBound*/) && (disX > 0)) {
			// 右滑 Over scroll
			if (DEBUG) LogUtils.d(TAG, "right scroll over !!, oldPadding: " + oldPadding);
			return false;
		}
		
		if (showRange >= mRcTextBound.width() + mOverScrollBound) {
			// 左滑 Over scroll
			newPadding = -(mRcTextBound.width() - width + mOverScrollBound);
			if (DEBUG) LogUtils.d(TAG, "left scroll over !!, newPadding: " + newPadding);
		} else if (newPadding >= mOrgPaddingRight /*+ mOverScrollBound*/) {
			// 右滑 Over scroll
			newPadding = mOrgPaddingRight /*+ mOverScrollBound*/;
			if (DEBUG) LogUtils.d(TAG, "right scroll over !!, newPadding: " + newPadding);
		}
		
		setPadding(getPaddingLeft(), getPaddingTop(), newPadding, getPaddingBottom(), true);
		return true;
	}
	
	private boolean onDragByMoreRight(float disX) {
		int correct = (int)Math.abs(disX);
		if (0 == correct) {
			if (DEBUG) LogUtils.d(TAG, "distance almost zero, we ignore it");
			return false;
		}
		
		int oldPadding = getPaddingLeft();
		int newPadding = getPaddingLeft() + (int)disX;
		int width = getWidth();
		int oldShowRange = -oldPadding + width;
		int showRange = -newPadding + width;
		
		if (DEBUG) LogUtils.d(TAG, "oldPadding: " + oldPadding + ", oldShowRange: " + oldShowRange
				+ ", showRange: " + showRange + ", newPadding: " + newPadding 
				+ ", overScrollBound: " + mOverScrollBound + ", textBound: " + mRcTextBound.width() 
				+ ", width: " + width + ", orgPaddngLeft: " + mOrgPaddingLeft
				+ ", disX: " + disX);
		
		if ((oldShowRange >= mRcTextBound.width() + mOverScrollBound) && (disX < 0)) {
			// 左滑 Over scroll
			if (DEBUG) LogUtils.d(TAG, "left scroll over !!, oldPadding: " + oldPadding);
			return false;
		} else if ((oldPadding >= mOrgPaddingLeft /*+ mOverScrollBound*/) && (disX > 0)) {
			// 右滑 Over scroll
			if (DEBUG) LogUtils.d(TAG, "right scroll over !!, oldPadding: " + oldPadding);
			return false;
		}
		
		if (showRange >= mRcTextBound.width() + mOverScrollBound) {
			// 右滑 Over scroll
			newPadding = -(mRcTextBound.width() - width + mOverScrollBound);
			if (DEBUG) LogUtils.d(TAG, "right scroll over !!, newPadding: " + newPadding);
		} else if (newPadding >= mOrgPaddingLeft /*+ mOverScrollBound*/) {
			// 左滑 Over scroll
			newPadding = mOrgPaddingLeft /*+ mOverScrollBound*/;
			if (DEBUG) LogUtils.d(TAG, "left scroll over !!, newPadding: " + newPadding);
		}
		
		setPadding(newPadding, getPaddingTop(), getPaddingRight(), getPaddingBottom(), true);
		return true;
	}
	
	/**
	 * 设置自动恢复拖动。（就是拖动松手的时候自动回弹）
	 * 
	 * @param auto
	 */
	public void setAutoDragRestore(boolean auto) {
		mAutoDragRestore = auto;
	}
	
	/**
	 * 设置能 Over drag 的范围因子（当前 View 大小的百分比）
	 * 
	 * @param factor
	 */
	public void setOverScrollFactor(float factor) {
		if (factor < 0) {
			factor = 0;
		}
		
		mOverScrollFactor = factor;
		mOverScrollBound = (int) (mOverScrollFactor * (float)getMeasuredWidth());
		computeDragBound();
	}
	
	/**
	 * 设置能否被拖动。
	 * 如果设置为 false，则只能通过 {@link #fakeDrag(int)} 用代码来模拟拖动。
	 * 某些情况下很有用。
	 * 
	 * @param enable
	 */
	public void enableDrag(boolean enable) {
		mEnableDrag = enable;
		
		if (!mEnableDrag) {
			restoreDrag();
		}
	}
	
	/**
	 * 模拟拖动
	 * 
	 * @param distance
	 * @return
	 */
	public boolean fakeDrag(int distance) {
		switch (getEllipsize()) {
		case START:
			return onDragByMoreLeft(distance);
		
		case END:
			return onDragByMoreRight(-distance);
			
		case MIDDLE:
		case MARQUEE:
		default:
			return false;
		}
	}
	
	/**
	 * 恢复拖动（回弹）
	 */
	public void restoreDrag() {
		switch (getEllipsize()) {
		case START:
			if (DEBUG) LogUtils.d(TAG, "restore padding: start: " + getPaddingRight() + ", end: " + mOrgPaddingRight);
			mDragRestoreAnim.setIntValues(getPaddingRight(), mOrgPaddingRight);
			// TODO: 根据恢复长度计算动画时间
			mDragRestoreAnim.cancel();
			mDragRestoreAnim.start();
			break;
		
		case END:
			if (DEBUG) LogUtils.d(TAG, "restore padding: start: " + getPaddingLeft() + ", end: " + mOrgPaddingLeft);
			mDragRestoreAnim.setIntValues(getPaddingLeft(), mOrgPaddingLeft);
			// TODO: 根据恢复长度计算动画时间
			mDragRestoreAnim.cancel();
			mDragRestoreAnim.start();
			break;
			
		case MIDDLE:
		case MARQUEE:
		default:
			break;
		}
	}
	
	/**
	 * 获取是否自动恢复拖动
	 * 
	 * @return
	 */
	public boolean isAutoDragRestore() {
		return mAutoDragRestore;
	}
	
	/**
	 * 获取是否能够拖动
	 * 
	 * @return
	 */
	public boolean isEnableDrag() {
		return mEnableDrag;
	}
	
	/**
	 * 获取 Over drag 因子
	 * 
	 * @return
	 */
	public float getOverScrollFactor() {
		return mOverScrollFactor;
	}
	
	//public boolean isInDrag() {
	//	return mInDrag;
	//}
	
	private void setPadding(int left, int top, int right, int bottom, boolean internal) {
		super.setPadding(left, top, right, bottom);
		
		if (!internal) {
			mOrgPaddingLeft = getPaddingLeft();
			mOrgPaddingRight = getPaddingRight();
			if (DEBUG) LogUtils.d(TAG, "ext setPadding: left: " + mOrgPaddingLeft + ", right: " + mOrgPaddingRight);
		}
	}
	
	private void computeDragBound() {
		String text = getText().toString();
		if (null == mParentTextPaint || null == text) {
			return;
		}
		
		mParentTextPaint.getTextBounds(text, 
				0, text.length(), mRcTextBound);
		
		if (DEBUG) LogUtils.d(TAG, "text bound: " + mRcTextBound.width());
	}
	
	
	private class PaddingObject {
		
		public PaddingObject() {
		}
		
		@SuppressWarnings("unused")
		public void setPaddingProperty(int padding) {
			switch (getEllipsize()) {
			case START:
				setPadding(getPaddingLeft(), getPaddingTop(), padding, getPaddingBottom(), true);
				break;
			
			case END:
				setPadding(padding, getPaddingTop(), getPaddingRight(), getPaddingBottom(), true);
				break;
				
			case MIDDLE:
			case MARQUEE:
			default:
				break;
			}
		}
		
		@SuppressWarnings("unused")
		public int getPaddingProperty() {
			switch (getEllipsize()) {
			case START:
				return getPaddingRight();
			
			case END:
				return getPaddingLeft();
				
			case MIDDLE:
			case MARQUEE:
			default:
				return 0;
			}
		}
		
	}
	
}
