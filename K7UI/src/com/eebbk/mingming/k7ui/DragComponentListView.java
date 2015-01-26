package com.eebbk.mingming.k7ui;

import com.eebbk.mingming.k7ui.DragTextView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * 
 * 支持拖动 {@link DragComponent} 的 {@link ListView}。 </br>
 * 
 * </br>
 * 
 * <b>注意：</b> 在 Item 设置中要把 {@link DragTextView} 用 {@link DragTextView#enableDrag(boolean)} 
 * 关掉，否则会影响 ListView 自己的滑动事件处理。
 * 
 * @author humingming <hmm@dw.gdbbk.com>
 *
 */
public class DragComponentListView extends DragItemListView {
	
	//private final static String TAG = "DragTextListView";
	public final static int FLING_THRESHOLD = 1000;
	public final static float DEFAULT_DRAG_POSITION_THRESHOLD = 0.5f;
	
	protected float mDragPositionThreshold;
	
	
	public DragComponentListView(Context context) {
		super(context);
		init(context, null, 0);
	}
	
	public DragComponentListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	
	public DragComponentListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	private void init(Context context, AttributeSet attrs, int defStyle) {
		mDragPositionThreshold = DEFAULT_DRAG_POSITION_THRESHOLD;
	}
	
	@Override
	protected void startDrag(View dragView, int position) {
		//LogUtils.d("mingming", "startDrag: dragView=" + dragView + ", position=" + poisiton);
	}

	@Override
	protected void endDrag(View dragView, int position, float velocityX, float velocityY) {
		//LogUtils.d("mingming", "endDrag: dragView=" + dragView + ", position=" + poisiton);
		DragComponent dragComponent = null;
		try {
			dragComponent = (DragComponent) dragView;
		} catch (Exception e) {
			dragComponent = null;
		}
		
		if (null == dragComponent) {
			return;
		}
		
		// 当滑动速度大于阀值，滑出组件
		int velX = (int)velocityX;
		if (Math.abs(velX) >= FLING_THRESHOLD) {
			if (velX > 0) {
				dragComponent.showComponent(DragComponent.SHOW_LEFT_COMPONENT, true);
			} else {
				dragComponent.showComponent(DragComponent.SHOW_RIGHT_COMPONENT, true);
			}
			return;
		}
		
		// 如果滑动速度达不到阀值，使用滑动距离来判断
		int dragPos = dragComponent.getDragPosition();
		if (dragPos > 0 && (dragPos > (dragComponent.getLeftComponentLength() * mDragPositionThreshold))) {
			dragComponent.showComponent(DragComponent.SHOW_LEFT_COMPONENT, true);
		} else if (dragPos < 0 && (Math.abs(dragPos) > (dragComponent.getRightComponentLength() * mDragPositionThreshold))) {
			dragComponent.showComponent(DragComponent.SHOW_RIGHT_COMPONENT, true);
		} else {
			dragComponent.showComponent(DragComponent.SHOW_MAIN_COMPONENT, true);
		}
	}
	
	@Override
	protected boolean performDrag(View dragView, int position, float disX) {
		//LogUtils.d("mingming", "performDrag: dragView=" + dragView + ", position=" + poisiton);
		DragComponent dragComponent = null;
		try {
			dragComponent = (DragComponent) dragView;
		} catch (Exception e) {
			dragComponent = null;
		}
		
		if (null == dragComponent) {
			return false;
		}
		
		//LogUtils.d("mingming", "disX=" + disX);
		return dragComponent.fakeDrag(-(int)disX);
	}
    
}
