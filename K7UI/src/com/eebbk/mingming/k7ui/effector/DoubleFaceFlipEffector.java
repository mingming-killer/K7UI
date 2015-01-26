package com.eebbk.mingming.k7ui.effector;

import com.eebbk.mingming.k7utils.BitmapUtils;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;

/**
 * 
 * 双面翻转特效器。 </br> 
 * 
 * </br>
 * <b>效果：</b> 以中心为翻转原点，目标的一半作为源翻转的背面翻入，并随翻转覆盖源的另一半，
 * 源的另外一半随着翻转会逐渐显示出来。 </br>
 * 
 * </br>
 * <b>正常模式：</b> </br>
 * &nbsp;&nbsp; {@link #TYPE_HORIZONTAL}：源右半边开始翻转，目标左半边作为源翻转的背面跟随翻入。 </br>
 * &nbsp;&nbsp; {@link #TYPE_VERTICAL}：源下半边开始翻转，目标上半边作为源翻转的背面跟随翻入。 </br>
 * 
 * </br>
 * <b>反转模式：</b> 和正常模式相反 </br>
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class DoubleFaceFlipEffector extends CanvasEffector {
	
	private final static String NAME = "DoubleFaceFlipEffector";
	
	protected final static float FACTOR_HALF = DST_FACTOR / 2;
	
	protected final static float FLIP_ANGLE_SRC = 0.0f;
	protected final static float FLIP_ANGLE_DST = 180.0f;
	
	protected Rect mRcSrc;
	protected Rect mRcDst;
	protected Bitmap mImgBuffer;
	
	protected Camera mCamera; 
	protected Matrix mMatrix;
	
	protected Paint mPaint;
	protected Canvas mCanvas;
	
	
	public DoubleFaceFlipEffector() {
		this(null, null, true, false, HORIZONTAL);
	}
	
	public DoubleFaceFlipEffector(Bitmap imgSrc, Bitmap imgDst, 
			boolean highQuality, boolean reverse, int type) {
		super(imgSrc, imgDst, highQuality, reverse, type);
		
		mName = NAME;
		
		mImgBuffer = null;
		mRcSrc = new Rect();
		mRcDst = new Rect();
		
		mCamera = new Camera();
		mMatrix = new Matrix();
		
		mPaint = new Paint();
		mCanvas = new Canvas();
	}
	
	@Override
	protected void setEffectFactorImpl(float doEffectFactor) {
		// 本特效器这个功能什么也不用做
	}
	
	@Override
	protected void setEffectTypeImpl(int type) {
		// 特效类型边了（横竖方向变了，重新把缓存图片生成一下）
		ensureImageBuffer();
	}
	
	@Override
	protected void setHighQualityImpl(boolean high) {
    	// 高画质开启抗锯齿、线性过滤等效果
    	mPaint.setAntiAlias(high);
    	mPaint.setFilterBitmap(high);
	}
	
	@Override
	protected boolean doEffectImpl(Canvas output, float doEffectFactor) {		
		if (!checkImage(ID_SRC | ID_DST) || 
				!checkImageValid(mImgBuffer)) {
			return false;
		}
		
		if (HORIZONTAL == mEffectType) {
			doFlipH(output, doEffectFactor);
		} else {
			doFlipV(output, doEffectFactor);
		}

		return true;
	}
	
	private void doFlipH(Canvas output, float doEffectFactor) {
		float angle = 0;
		float centerX = 0;
		float centerY = 0;
		
		Bitmap imgSrc = getImage(ID_SRC); //getUseImage(ID_SRC);
		Bitmap imgDst = getImage(ID_DST); //getUseImage(ID_DST);
		
		
		// 1. 源左半边
		mRcSrc.set(0, 0, imgSrc.getWidth() / 2, imgSrc.getHeight());
		output.drawBitmap(imgSrc, mRcSrc, mRcSrc, mPaint);
		
		
		// 2. 目标右半边			
		mRcSrc.set(imgDst.getWidth() / 2, 0, imgDst.getWidth(), imgDst.getHeight());
		output.drawBitmap(imgDst, mRcSrc, mRcSrc, mPaint);
		
		
		if (doEffectFactor < FACTOR_HALF) {
			// 3. 源的右半边（翻转）
			// 这个时候能看到的是翻转的正面，背面是看不到的，不用画
			mCanvas.setBitmap(mImgBuffer);
			mCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
			
			mRcSrc.set(imgSrc.getWidth() / 2, 0, imgSrc.getWidth(), imgSrc.getHeight());
			mRcDst.set(0, 0, mImgBuffer.getWidth(), mImgBuffer.getHeight());
			mCanvas.drawBitmap(imgSrc, mRcSrc, mRcDst, mPaint);
			
			// 0 ~ -90 (0.0 ~ 0.5)
			angle = -(FLIP_ANGLE_DST * doEffectFactor);
			centerX = 0.0f;
			centerY = imgSrc.getHeight() / 2;
			
			mMatrix.reset();
			
			mCamera.save();  
	        mCamera.rotateY(angle);
	        mCamera.getMatrix(mMatrix);
	        mCamera.restore();
	        
	        // 先移动到旋转中心的反位置 （preXX 将变化插入到变化队列的最开头）
	        mMatrix.preTranslate(-centerX, -centerY);
	        
	        // 旋转后再移动回来（postXX 将变化插入到变化队列的最后）
	        mMatrix.postTranslate(centerX, centerY);
	        
	        // 最后将旋转好的图像，移动到指定的位置
			mMatrix.postTranslate(imgSrc.getWidth() / 2, 0.0f);
			
	        output.drawBitmap(mImgBuffer, mMatrix, mPaint);
			
		} else {			
			// 3. 目标的左半边（翻转）
			// 这个时候能看到的是翻转的背面，正面是看不到的，不用画
			mCanvas.setBitmap(mImgBuffer);
			mCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
			
			mRcSrc.set(0, 0, imgDst.getWidth() / 2, imgDst.getHeight());
			mRcDst.set(0, 0, mImgBuffer.getWidth(), mImgBuffer.getHeight());
			mCanvas.drawBitmap(imgDst, mRcSrc, mRcDst, mPaint);
	        
	        // 90 - 0 (0.5 ~ 1.0)
	        angle = (FLIP_ANGLE_DST * (DST_FACTOR - doEffectFactor));
			centerX = imgDst.getWidth() / 2;
			centerY = imgDst.getHeight() / 2;
	        
	        mMatrix.reset();
	        
			mCamera.save();
	        mCamera.rotateY(angle);
	        mCamera.getMatrix(mMatrix);
	        mCamera.restore();
	        
	        mMatrix.preTranslate(-centerX, -centerY);
	        mMatrix.postTranslate(centerX, centerY);
	        mMatrix.postTranslate(0.0f, 0.0f);
			
	        output.drawBitmap(mImgBuffer, mMatrix, mPaint);
		}
	}
	
	private void doFlipV(Canvas output, float doEffectFactor) {
		float angle = 0;
		float centerX = 0;
		float centerY = 0;
		
		Bitmap imgSrc = getImage(ID_SRC); //getUseImage(ID_SRC);
		Bitmap imgDst = getImage(ID_DST); //getUseImage(ID_DST);
		
		
		// 1. 源左上半边
		mRcSrc.set(0, 0, imgSrc.getWidth(), imgSrc.getHeight() / 2);
		output.drawBitmap(imgSrc, mRcSrc, mRcSrc, mPaint);
		
		
		// 2. 目标下半边			
		mRcSrc.set(0, imgDst.getHeight() / 2, imgDst.getWidth(), imgDst.getHeight());
		output.drawBitmap(imgDst, mRcSrc, mRcSrc, mPaint);
		
		
		if (doEffectFactor < FACTOR_HALF) {
			// 3. 源的下半边（翻转）
			// 这个时候能看到的是翻转的正面，背面是看不到的，不用画
			mCanvas.setBitmap(mImgBuffer);
			mCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
			
			mRcSrc.set(0, imgSrc.getHeight() / 2, imgSrc.getWidth(), imgSrc.getHeight());
			mRcDst.set(0, 0, mImgBuffer.getWidth(), mImgBuffer.getHeight());
			mCanvas.drawBitmap(imgSrc, mRcSrc, mRcDst, mPaint);
			
			// 0 ~ 90 (0.0 ~ 0.5)
			angle = FLIP_ANGLE_DST * doEffectFactor;
			centerX = imgSrc.getWidth() / 2;
			centerY = 0;
			
			mMatrix.reset();
			
			mCamera.save();  
	        mCamera.rotateX(angle);
	        mCamera.getMatrix(mMatrix);
	        mCamera.restore();
	        
	        mMatrix.preTranslate(-centerX, -centerY);
	        mMatrix.postTranslate(centerX, centerY);
			mMatrix.postTranslate(0.0f, imgSrc.getHeight() / 2);
			
	        output.drawBitmap(mImgBuffer, mMatrix, mPaint);
			
		} else {			
			// 3. 目标的上半边（翻转）
			// 这个时候能看到的是翻转的背面，正面是看不到的，不用画
			mCanvas.setBitmap(mImgBuffer);
			mCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
			
			mRcSrc.set(0, 0, imgDst.getWidth(), imgDst.getHeight() / 2);
			mRcDst.set(0, 0, mImgBuffer.getWidth(), mImgBuffer.getHeight());
			mCanvas.drawBitmap(imgDst, mRcSrc, mRcDst, mPaint);
	        
	        // -90 - 0 (0.5 ~ 1.0)
	        angle = -(FLIP_ANGLE_DST * (DST_FACTOR - doEffectFactor));
			centerX = imgDst.getWidth() / 2;
			centerY = imgDst.getHeight() / 2;
	        
	        mMatrix.reset();
	        
			mCamera.save();
	        mCamera.rotateX(angle);
	        mCamera.getMatrix(mMatrix);
	        mCamera.restore();
	        
	        mMatrix.preTranslate(-centerX, -centerY);
	        mMatrix.postTranslate(centerX, centerY);
	        mMatrix.postTranslate(0.0f, 0.0f);
			
	        output.drawBitmap(mImgBuffer, mMatrix, mPaint);
		}
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
		BitmapUtils.freeBitmap(mImgBuffer);
	}
	
	@Override
	public boolean supportHardAccelerated() {
		return true;
	}
	
	@Override
	public boolean buildImage(int width, int height, Bitmap.Config config, int which) {
		boolean ret = super.buildImage(width, height, config, which);
		// 创建特效图片，要同时把缓存图片也创建好。
		ret &= ensureImageBuffer();
		return ret;
	}
	
	private boolean ensureImageBuffer() {
		if (!checkImage(ID_SRC)) {
			return false;
		}
		
		int w, h;
		Bitmap image = getImage(ID_SRC);
		
		if (HORIZONTAL == mEffectType) {
			w = image.getWidth() / 2;
			h = image.getHeight();
		} else {
			w = image.getWidth();
			h = image.getHeight() / 2;
		}
		
		mImgBuffer = reCreateImage(mImgBuffer, w, h, image.getConfig());
		return checkImageValid(mImgBuffer);
	}
}
