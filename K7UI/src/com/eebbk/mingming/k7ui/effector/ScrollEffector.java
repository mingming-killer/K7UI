package com.eebbk.mingming.k7ui.effector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * 
 * 滑动特效器。 </br> 
 * 
 * </br>
 * <b>效果：</b> 源滑出，目标滑入。Android 自带的特效，只是为测试用。 </br>
 * 
 * </br>
 * <b>正常模式：</b> </br>
 * &nbsp;&nbsp; {@link Effector#TYPE_HORIZONTAL}：目标从右边滑入，源从左边滑出。 </br>
 * &nbsp;&nbsp; {@link Effector#TYPE_VERTICAL}：目标从下边滑入，源从上边滑出。 </br>
 * 
 * </br>
 * <b>反转模式：</b> 和正常模式相反 </br>
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class ScrollEffector extends CanvasEffector {
	
	private final static String NAME = "ScrollEffector";
	
	private Rect mRcSrc;
	private Rect mRcDst;
	
	private Paint mPaint;
	private Matrix mMatrix;
	
	
	public ScrollEffector() {
		this(null, null, true, false, HORIZONTAL);
	}
	
	public ScrollEffector(Bitmap imgSrc, Bitmap imgDst, 
			boolean highQuality, boolean reverse, int type) {
		super(imgSrc, imgDst, highQuality, reverse, type);
		
		mName = NAME;
		
		mRcSrc = new Rect();
		mRcDst = new Rect();
		
		mPaint = new Paint();
		mMatrix = new Matrix();
	}
	
	@Override
	protected void setEffectFactorImpl(float doEffectFactor) {
		// nothing to do.
	}
	
	@Override
	protected void setEffectTypeImpl(int type) {
		// nothing to do.
	}
	
	@Override
	protected void setHighQualityImpl(boolean high) {
    	// 高画质开启抗锯齿、线性过滤等效果
    	mPaint.setAntiAlias(high);
    	mPaint.setFilterBitmap(high);
	}
	
	@Override
	protected boolean doEffectImpl(Canvas output, float doEffectFactor) {
		if (null == output) {
			return false;
		}
		
		if (!checkImage(ID_SRC | ID_DST)) {
			return false;
		}
		
		int srcFactor = 0;
		int dstFactor = 0;
		
		Bitmap imgSrc = getImage(ID_SRC); //getUseImage(ID_SRC);
		Bitmap imgDst = getImage(ID_DST); //getUseImage(ID_DST);
		
		// just for test
		mMatrix.reset();
		//mMatrix.setScale(-1f, 1f);
		//mMatrix.postTranslate(imgSrc.getWidth(), 0);
		//output.drawBitmap(imgSrc, m, mPaint);
		
		if (HORIZONTAL == mEffectType) {
			srcFactor = (int)((float)imgSrc.getWidth() * (float)(DST_FACTOR - doEffectFactor));
			dstFactor = (int)((float)imgDst.getWidth() * doEffectFactor);
			
			mRcSrc.set(
					mImgSrc.getWidth() - srcFactor, 
					0, 
					mImgSrc.getWidth(), 
					mImgSrc.getHeight());
			mRcDst.set(
					0, 
					0, 
					srcFactor, 
					mImgSrc.getHeight());
				
			output.drawBitmap(imgSrc, mRcSrc, mRcDst, mPaint);
			
			mRcSrc.set(
					0, 
					0, 
					dstFactor, 
					mImgDst.getHeight());
			mRcDst.set(
					srcFactor, 
					0, 
					srcFactor + dstFactor, 
					mImgDst.getHeight());
			
			output.drawBitmap(imgDst, mRcSrc, mRcDst, mPaint);
				
		} else {
			srcFactor = (int)((float)imgSrc.getHeight() * (float)(DST_FACTOR - doEffectFactor));
			dstFactor = (int)((float)imgDst.getHeight() * doEffectFactor);
			
			mRcSrc.set(
					0, 
					mImgSrc.getHeight() - srcFactor, 
					mImgSrc.getWidth(), 
					mImgSrc.getHeight());
			mRcDst.set(
					0, 
					0, 
					mImgSrc.getWidth(), 
					srcFactor);
				
			output.drawBitmap(imgSrc, mRcSrc, mRcDst, mPaint);
			
			mRcSrc.set(
					0, 
					0, 
					mImgDst.getWidth(), 
					dstFactor);
			mRcDst.set(
					0, 
					srcFactor, 
					mImgDst.getWidth(), 
					srcFactor + dstFactor);
			
			output.drawBitmap(imgDst, mRcSrc, mRcDst, mPaint);
		}

		return true;
	}
	
	@Override
	protected boolean swapImageImpl() {
		// nothing to do.
		return true;
	}
	
	@Override
	protected void reverseEffectImpl(boolean reverse) {
		// nothing to do.
	}
	
	@Override
	protected void freeImpl() {
		// nothing to do.
	}
	
	@Override
	public boolean supportHardAccelerated() {
		return true;
	}
	
}
