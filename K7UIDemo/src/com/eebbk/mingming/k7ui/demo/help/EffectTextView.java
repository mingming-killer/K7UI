package com.eebbk.mingming.k7ui.demo.help;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class EffectTextView extends TextView implements View.OnClickListener {
	
	private boolean mShowEffect = false;
	
	public EffectTextView(Context context) {
		super(context);
		init(context);
	}
	
	public EffectTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public EffectTextView(Context context, AttributeSet attrs, int style) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		setOnClickListener(this);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return super.dispatchTouchEvent(event);
	}
	
	public void setShowEffect(boolean show) {
		mShowEffect = show;
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		if (mShowEffect) {
			drawTranslucentContent(canvas);
		} else {
			super.onDraw(canvas);
		}
	}
	
	@Override
	public void onClick(View v) {
		Toast.makeText(getContext(), "Click by: " + this.toString(), Toast.LENGTH_SHORT).show();
	}
	
	private void drawTranslucentContent(Canvas canvas) {
		// easer with background color.
		canvas.drawColor(0xffff0000, PorterDuff.Mode.CLEAR);
	}

}
