package com.eebbk.mingming.k7ui.demo.help;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.eebbk.mingming.k7ui.DragComponent;
import com.eebbk.mingming.k7ui.demo.R;

public class MyDragComponent extends DragComponent {	
	
	public MyDragComponent(Context context) {
		super(context);
	}
	
	public MyDragComponent(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MyDragComponent(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View getLeftDragComponent(View container) {
		if (null == container) {
			return null;
		}
		//return container.findViewById(R.id.left_component);
		return null;
	}

	@Override
	protected View getRightDragComponent(View container) {
		if (null == container) {
			return null;
		}
		return container.findViewById(R.id.right_component);
		//return null;
	}

	@Override
	protected View getMainComponent(View container) {
		if (null == container) {
			return null;
		}
		return container.findViewById(R.id.main_component);
	}

}
