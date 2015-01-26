package com.eebbk.mingming.k7ui;

import com.eebbk.mingming.k7ui.DragTextView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * 
 * 支持拖动 {@link DragTextView} 的 {@link ListView}。 </br>
 * 
 * </br>
 * 
 * <b>注意：</b> 在 Item 设置中要把 {@link DragTextView} 用 {@link DragTextView#enableDrag(boolean)} 
 * 关掉，否则会影响 ListView 自己的滑动事件处理。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class DragTextListView extends DragItemListView {
	
	//private final static String TAG = "DragTextListView";
	
	public DragTextListView(Context context) {
		super(context);
	}
	
	public DragTextListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public DragTextListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void startDrag(View dragView, int position) {
		// do noting
	}

	@Override
	protected void endDrag(View dragView, int position, float velocityX, float velocityY) {
		DragTextView dragTextView = null;
		try {
			dragTextView = (DragTextView) dragView;
		} catch (Exception e) {
			dragTextView = null;
		}
		
		if (null == dragTextView) {
			return;
		}
		
		dragTextView.restoreDrag();
	}
	
	@Override
	protected boolean performDrag(View dragView, int position, float disX) {
		DragTextView dragTextView = null;
		try {
			dragTextView = (DragTextView) dragView;
		} catch (Exception e) {
			dragTextView = null;
		}
		
		if (null == dragTextView) {
			return false;
		}
		
		return dragTextView.fakeDrag((int) disX);
	}
    
}
