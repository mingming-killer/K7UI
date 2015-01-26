package com.eebbk.mingming.k7ui.demo.help;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.eebbk.mingming.k7ui.DragComponent;
import com.eebbk.mingming.k7ui.DragComponentListView;

public class MyDragComponentListView extends DragComponentListView {

	private ArrayList<DragData> mDatas = null;
	
	public static class DragData {
		public String mData;
		public int mIndex;
		public int mDragPosition;
		
		public DragData() {
			this("", 0, 0);
		}
		
		public DragData(String data, int index, int dragPosition) {
			mData = data;
			mIndex = index;
			mDragPosition = dragPosition;
		}
	}
	
	public MyDragComponentListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public MyDragComponentListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public MyDragComponentListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public void setDatas(ArrayList<DragData> datas) {
		mDatas = datas;
	}
	
	@Override
	protected void startDrag(View dragView, int position) {
		super.startDrag(dragView, position);
	}
	
	@Override
	protected boolean performDrag(View dragView, int position, float disX) {
		boolean ret = super.performDrag(dragView, position, disX);
		setPressed(false);
		return ret;
	}

	@Override
	protected void endDrag(View dragView, int position, float velocityX, float velocityY) {
		super.endDrag(dragView, position, velocityX, velocityY);
		
		//LogUtils.d("mingming", "my endDrag: position=" + position);
		if (null == mDatas || position < 0 || position >= mDatas.size()) {
			return;
		}
		
		DragData data = mDatas.get(position);
		if (null == data) {
			return;
		}
		
		DragComponent dragComponent = null;
		try {
			dragComponent = (DragComponent) dragView;
		} catch (Exception e) {
			dragComponent = null;
		}
		
		if (null == dragComponent) {
			return;
		}
		
		data.mDragPosition = dragComponent.getTargetDragPosition();
		//LogUtils.d("mingming", "my endDrag: targetDragPos=" + dragComponent.getTargetDragPosition() 
		//		+ ", dragPos=" + dragComponent.getDragPosition());
	}

}
