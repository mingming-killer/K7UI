package com.eebbk.mingming.k7ui.effector;

import com.eebbk.mingming.k7utils.LogUtils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Paint.Style;
import android.graphics.drawable.GradientDrawable;

/**
 * 
 * Page curl base on bezier curve implement by canvas. </br> 
 * 
 * This bezier curve model is base on this blogs: 
 * <a href="http://blog.csdn.net/hmg25/article/details/6306479 ">http://blog.csdn.net/hmg25/article/details/6306479</a> 
 * </br>
 * 
 * </br>
 * <b>Effect: </b> Page curl effector. </br>
 * 
 * @author humingming <hmm@dw.gdbbk.com>
 *
 */
public class BezierCurlEffector extends CanvasEffector {
	
	private final static String NAME = "BezierCurlEffector";
	
	/** This type is useful for portrait */
	public final static int SHOW_ONE_PAGE = VERTICAL;
	/** This type is useful for landscape */
	public final static int SHOW_TWO_PAGE = HORIZONTAL;
	
	private final static boolean DEBUG_DRAW_POLY = false;
		
	private final static float CURL_TRACK_INFLECTION_FACTOR_ONE_PAGE = 0.25f;
	private final static float CURL_TRACK_INFLECTION_FACTOR_TWO_PAGE = 0.35f;
	private final static float CURL_TRACK_INFLECTION_Y_FACTOR_ONE_PAGE = 0.8f;
	private final static float CURL_TRACK_INFLECTION_Y_FACTOR_TWO_PAGE = 0.65f;
	
	// from right: is emulate flip from right to left
	private final static int CURL_FROM_RIGHT = 0;
	// from left: is emulate flip from left to right.(this is only support in two page type)
	@SuppressWarnings("unused")
	private final static int CURL_FROM_LEFT = 1;
	
	// page rectangle.
	private Rect mPageRect;
	
	private boolean mIsRTandLB;
	private int mCurlDirect;
	// shadow max length.
	private float mMaxLength;
	
	private Paint mPaint;
	private Matrix mMatrix;
    
    private Path mPath0;
    private Path mPath1; 
    
	private int mCornerX;
	private int mCornerY;
	private float mMiddleX;
	private float mMiddleY;
	private float mDegrees;
	private float mTouchToCornerDis;
	
	private PointF mTouch;
	private PointF mBezierStart1;
	private PointF mBezierControl1;
	private PointF mBeziervertex1;
	private PointF mBezierEnd1;

	private PointF mBezierStart2;
	private PointF mBezierControl2;
	private PointF mBeziervertex2;
	private PointF mBezierEnd2;
    
	private ColorMatrixColorFilter mColorMatrixFilter;
	private float[] mMatrixArray = { 0, 0, 0, 0, 0, 0, 0, 0, 1.0f };
	
	private int[] mBackShadowColors;
	private int[] mFrontShadowColors;
	private GradientDrawable mBackShadowDrawableLR;
	private GradientDrawable mBackShadowDrawableRL;
	private GradientDrawable mFolderShadowDrawableLR;
	private GradientDrawable mFolderShadowDrawableRL;

	private GradientDrawable mFrontShadowDrawableHBT;
	private GradientDrawable mFrontShadowDrawableHTB;
	private GradientDrawable mFrontShadowDrawableVLR;
	private GradientDrawable mFrontShadowDrawableVRL;
	
	//===================================
	// this is for debug.
	//===================================
	private float[] mBezierPts = new float[8*2];
	private float[] mCornerPts = new float[2];
	private float[] mTouchPts = new float[2];
	private float[] mMiddlePts = new float[2];
	private float[] mLinePts = new float[2 * 2];
	//===================================
	
	// equation for emulate touch track.
	// now we just use linear equation: y = ax + b.
	// the x is mEffectFactor, y is curlPos.
	// so there have 2 linear equation factors:
	//   curlPos.x-le, curlPos.y-le
	// and we found, use a linear equation inflection is a good UX,
	// so we have 2 linear equation with 2 points:
	//
	// y |
	//   |
	//   |     le2
	//   |      /\
	//   |     /  \
	//   |    /    \
	//   |   /      \
	//   |  /        \
	//   | /          \ le1
	//   -------------------- x
	//
	private LinearEquation mCurlPosXLE1 = null;
	private LinearEquation mCurlPosYLE1 = null;
	
	private LinearEquation mCurlPosXLE2 = null;
	private LinearEquation mCurlPosYLE2 = null;
	
	// 2 points can confirm a linear(equation).
	private PointF mTrackP1 = null;
	private PointF mTrackP2 = null;
	
	private class LinearEquation {
		float a = 0f;
		float b = 0f;
		
		@Override
		public String toString() {
			return "a=" + a + ", b=" + b;
		}
	}
    
	public BezierCurlEffector() {
		this(null, null, true, false, SHOW_ONE_PAGE);
	}
	
	public BezierCurlEffector(Bitmap imgSrc, Bitmap imgDst, 
			boolean highQuality, boolean reverse, int type) {
		super(imgSrc, imgDst, highQuality, reverse, type);
		
		mName = NAME;
		
		// TODO: now we only curl from bottom.
		mIsRTandLB = false;
		// TODO: now we only support curl from right.
		mCurlDirect = CURL_FROM_RIGHT;
		
		mCornerX = 0;
		mCornerY = 0;
		mMiddleX = 0f;
		mMiddleY = 0f;
		mDegrees = 0f;
		mTouchToCornerDis = 0f;
	    
	    mPageRect = new Rect();
		
		mPaint = new Paint(Paint.DITHER_FLAG);
		mPaint.setStyle(Paint.Style.FILL);
		mMatrix = new Matrix();
		
		mPath0 = new Path();
		mPath1 = new Path();
		
		mTouch = new PointF();
		mBezierStart1 = new PointF();
		mBezierControl1 = new PointF();
		mBeziervertex1 = new PointF();
		mBezierEnd1 = new PointF();

		mBezierStart2 = new PointF();
		mBezierControl2 = new PointF();
		mBeziervertex2 = new PointF();
		mBezierEnd2 = new PointF();
	    
		ColorMatrix cm = new ColorMatrix();
		float array[] = { 
				//0.55f, 0, 0, 0, 80.0f, 
				//0, 0.55f, 0, 0, 80.0f, 
				//0, 0, 0.55f, 0, 80.0f, 
				//0, 0, 0, 0.2f, 0 
				0.55f, 0, 0, 0, 0, 
				0, 0.55f, 0, 0, 0, 
				0, 0, 0.55f, 0, 0, 
				0, 0, 0, 0.5f, 0 
				};
		cm.set(array);
		mColorMatrixFilter = new ColorMatrixColorFilter(cm);
		
		initShadowRes();
		
		mCurlPosXLE1 = new LinearEquation();
		mCurlPosYLE1 = new LinearEquation();
		
		mCurlPosXLE2 = new LinearEquation();
		mCurlPosYLE2 = new LinearEquation();
		
		mTrackP1 = new PointF();
		mTrackP2 = new PointF();
	}
	
	private void initShadowRes() {
		int[] color = { 0x333333, 0xb0333333 };
		mFolderShadowDrawableRL = new GradientDrawable(
				GradientDrawable.Orientation.RIGHT_LEFT, color);
		mFolderShadowDrawableRL
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFolderShadowDrawableLR = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, color);
		mFolderShadowDrawableLR
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowColors = new int[] { 0xff111111, 0x111111 };
		mBackShadowDrawableRL = new GradientDrawable(
				GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
		mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowDrawableLR = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
		mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowColors = new int[] { 0x80111111, 0x111111 };
		mFrontShadowDrawableVLR = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
		mFrontShadowDrawableVLR
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mFrontShadowDrawableVRL = new GradientDrawable(
				GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
		mFrontShadowDrawableVRL
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHTB = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
		mFrontShadowDrawableHTB
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHBT = new GradientDrawable(
				GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
		mFrontShadowDrawableHBT
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);
	}
	
	@Override
	protected void setEffectFactorImpl(float doEffectFactor) {
		// nothing to do.
	}
	
	@Override
	protected void setEffectTypeImpl(int type) {
		// page show mode changed, we update the page rectangle.
		updatePageRects();
	}
	
	@Override
	protected void setHighQualityImpl(boolean high) {
    	// high quality we will turn on the AA and AF.
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
		
		updateCurlPosByTouchTrack(doEffectFactor);
		calculateBezierCurve();
		
		// draw the background.
		output.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
		
		drawCurrentPageArea(output, mImgSrc);
		drawNextPageAreaAndShadow(output, mImgDst);
		drawCurrentPageShadow(output);
		drawCurrentBackArea(output, mImgSrc);
		if (DEBUG_DRAW_POLY) drawPoly(output);
		//canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
		
		return true;
	}
	
	private void drawPoly(Canvas canvas) {
		int color = mPaint.getColor();
		float strokeW = mPaint.getStrokeWidth();
		Style style = mPaint.getStyle();
		{
			mPaint.setStrokeWidth(2);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(Color.GREEN);
			canvas.drawPath(mPath0, mPaint);
			mPaint.setColor(Color.BLUE);
			canvas.drawPath(mPath1, mPaint);
			
			mBezierPts[0] = mBezierStart1.x;
			mBezierPts[1] = mBezierStart1.y;
			mBezierPts[2] = mBezierControl1.x;
			mBezierPts[3] = mBezierControl1.y;
			mBezierPts[4] = mBeziervertex1.x;
			mBezierPts[5] = mBeziervertex1.y;
			mBezierPts[6] = mBezierEnd1.x;
			mBezierPts[7] = mBezierEnd1.y;
			
			mBezierPts[8] = mBezierStart2.x;
			mBezierPts[9] = mBezierStart2.y;
			mBezierPts[10] = mBezierControl2.x;
			mBezierPts[11] = mBezierControl2.y;
			mBezierPts[12] = mBeziervertex2.x;
			mBezierPts[13] = mBeziervertex2.y;
			mBezierPts[14] = mBezierEnd2.x;
			mBezierPts[15] = mBezierEnd2.y;
			
			mPaint.setColor(Color.RED);
			mPaint.setStrokeWidth(5);
			canvas.drawPoints(mBezierPts, mPaint);
			
			mTouchPts[0] = mTouch.x;
			mTouchPts[1] = mTouch.y;
			mPaint.setColor(Color.MAGENTA);
			mPaint.setStrokeWidth(5);
			canvas.drawPoints(mTouchPts, mPaint);
			
			mCornerPts[0] = mCornerX;
			mCornerPts[1] = mCornerY;
			mPaint.setColor(Color.CYAN);
			mPaint.setStrokeWidth(5);
			canvas.drawPoints(mCornerPts, mPaint);
			
			mMiddlePts[0] = mMiddleX;
			mMiddlePts[1] = mMiddleY;
			mPaint.setColor(Color.YELLOW);
			mPaint.setStrokeWidth(5);
			canvas.drawPoints(mMiddlePts, mPaint);
			
			mLinePts[0] = mTouch.x;
			mLinePts[1] = mTouch.y;
			mLinePts[2] = mCornerX;
			mLinePts[3] = mCornerY;
			mPaint.setColor(Color.YELLOW);
			mPaint.setStrokeWidth(2);
			canvas.drawLines(mLinePts, mPaint);
		}
		mPaint.setColor(color);
		mPaint.setStrokeWidth(strokeW);
		mPaint.setStyle(style);
	}
	
	private void drawCurrentPageArea(Canvas canvas, Bitmap bitmap) {
		mPath0.reset();
		mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x,
				mBezierEnd1.y);
		mPath0.lineTo(mTouch.x, mTouch.y);
		mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x,
				mBezierStart2.y);
		mPath0.lineTo(mCornerX, mCornerY);
		mPath0.close();
		
		canvas.save();
		{
			canvas.clipPath(mPath0, Region.Op.XOR);
			canvas.drawBitmap(bitmap, 0, 0, null);
		}
		canvas.restore();
	}

	private void drawNextPageAreaAndShadow(Canvas canvas, Bitmap bitmap) {
		mPath1.reset();
		mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.lineTo(mCornerX, mCornerY);
		mPath1.close();

		mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x
				- mCornerX, mBezierControl2.y - mCornerY));
		int leftx;
		int rightx;
		GradientDrawable mBackShadowDrawable;
		if (mIsRTandLB) {
			leftx = (int) (mBezierStart1.x);
			rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
			mBackShadowDrawable = mBackShadowDrawableLR;
		} else {
			leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
			rightx = (int) mBezierStart1.x;
			mBackShadowDrawable = mBackShadowDrawableRL;
		}
		canvas.save();
		{
			canvas.clipPath(mPath0);
			canvas.clipPath(mPath1, Region.Op.INTERSECT);
			canvas.drawBitmap(bitmap, 0, 0, null);
			canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
			mBackShadowDrawable.setBounds(leftx, (int) mBezierStart1.y, rightx,
					(int) (mMaxLength + mBezierStart1.y));
			mBackShadowDrawable.draw(canvas);
		}
		canvas.restore();
	}
	
	private void drawCurrentPageShadow(Canvas canvas) {
		double degree;
		if (mIsRTandLB) {
			degree = (Math.PI / 4) - Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x - mBezierControl1.x);
		} else {
			degree = (Math.PI / 4) - Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x - mBezierControl1.x);
		}
		// TODO: the shadow size is 25pixel, may be a dynamic value is better?
		double d1 = (float) 25 * 1.414 * Math.cos(degree);
		double d2 = (float) 25 * 1.414 * Math.sin(degree);
		float x = (float) (mTouch.x + d1);
		float y;
		if (mIsRTandLB) {
			y = (float) (mTouch.y + d2);   
		} else {
			y = (float) (mTouch.y - d2);
		}
		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
		mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.close();
		
		int leftx;
		int rightx;
		float rotateDegrees;
		GradientDrawable mCurrentPageShadow;
		
		canvas.save();
		{
			canvas.clipPath(mPath0, Region.Op.XOR);
			canvas.clipPath(mPath1, Region.Op.INTERSECT);
			if (mIsRTandLB) {
				leftx = (int) (mBezierControl1.x);
				rightx = (int) mBezierControl1.x + 25;
				mCurrentPageShadow = mFrontShadowDrawableVLR;
			} else {
				leftx = (int) (mBezierControl1.x - 25);
				rightx = (int) mBezierControl1.x + 1;
				mCurrentPageShadow = mFrontShadowDrawableVRL;
			}
			
			rotateDegrees = (float) Math.toDegrees(
					Math.atan2(mTouch.x - mBezierControl1.x, mBezierControl1.y - mTouch.y));
			canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
			mCurrentPageShadow.setBounds(leftx,
					(int) (mBezierControl1.y - mMaxLength), rightx,
					(int) (mBezierControl1.y));
			mCurrentPageShadow.draw(canvas);
		}
		canvas.restore();
		
		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.close();
		canvas.save();
		{
			canvas.clipPath(mPath0, Region.Op.XOR);
			canvas.clipPath(mPath1, Region.Op.INTERSECT);
			if (mIsRTandLB) {
				leftx = (int) (mBezierControl2.y);
				rightx = (int) (mBezierControl2.y + 25);
				mCurrentPageShadow = mFrontShadowDrawableHTB;
			} else {
				leftx = (int) (mBezierControl2.y - 25);
				rightx = (int) (mBezierControl2.y + 1);
				mCurrentPageShadow = mFrontShadowDrawableHBT;
			}
			rotateDegrees = (float) Math.toDegrees(
					Math.atan2(mBezierControl2.y - mTouch.y, mBezierControl2.x - mTouch.x));
			canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y);
			float temp;
			if (mBezierControl2.y < 0) {
				temp = mBezierControl2.y - mPageRect.bottom;
			} else {
				temp = mBezierControl2.y;
			}

			int hmg = (int) Math.hypot(mBezierControl2.x, temp);
			if (hmg > mMaxLength) {
				mCurrentPageShadow.setBounds(
						(int) (mBezierControl2.x - 25) - hmg, leftx,
						(int) (mBezierControl2.x + mMaxLength) - hmg, rightx);
			} else {
				mCurrentPageShadow.setBounds(
						(int) (mBezierControl2.x - mMaxLength), leftx,
						(int) (mBezierControl2.x), rightx);
			}
			
			//LogUtils.d(NAME, "mBezierControl2.x   " + mBezierControl2.x 
			//		+ "  mBezierControl2.y  " + mBezierControl2.y);
			mCurrentPageShadow.draw(canvas);
		}
		canvas.restore();
	}
	
	private void drawCurrentBackArea(Canvas canvas, Bitmap bitmap) {
		int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
		float f1 = Math.abs(i - mBezierControl1.x);
		int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
		float f2 = Math.abs(i1 - mBezierControl2.y);
		float f3 = Math.min(f1, f2);
		mPath1.reset();
		mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath1.close();
		GradientDrawable mFolderShadowDrawable;
		int left;
		int right;
		if (mIsRTandLB) {
			left = (int) (mBezierStart1.x - 1);
			right = (int) (mBezierStart1.x + f3 + 1);
			mFolderShadowDrawable = mFolderShadowDrawableLR;
		} else {
			left= (int) (mBezierStart1.x - f3 - 1);
			right= (int) (mBezierStart1.x + 1);
			mFolderShadowDrawable = mFolderShadowDrawableRL;
		}
		
		canvas.save();
		{
			canvas.clipPath(mPath0);
			canvas.clipPath(mPath1, Region.Op.INTERSECT);
			mPaint.setColorFilter(mColorMatrixFilter);
			float dis = (float) Math.hypot(mCornerX - mBezierControl1.x,
					mBezierControl2.y - mCornerY);
			float f8 = (mCornerX - mBezierControl1.x) / dis;
			float f9 = (mBezierControl2.y - mCornerY) / dis;
			mMatrixArray[0] = 1 - 2 * f9 * f9;
			mMatrixArray[1] = 2 * f8 * f9;
			mMatrixArray[3] = mMatrixArray[1];
			mMatrixArray[4] = 1 - 2 * f8 * f8;
			mMatrix.reset();
			mMatrix.setValues(mMatrixArray);
			mMatrix.preTranslate(-mBezierControl1.x, -mBezierControl1.y);
			mMatrix.postTranslate(mBezierControl1.x, mBezierControl1.y);
			canvas.drawBitmap(bitmap, mMatrix, mPaint);
			// canvas.drawBitmap(bitmap, mMatrix, null);
			mPaint.setColorFilter(null);
			canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
			mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, 
					right, (int) (mBezierStart1.y + mMaxLength));
			mFolderShadowDrawable.draw(canvas);
		}
		canvas.restore();
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
		return false;
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		// size changed, we update the page rectangle.
		updatePageRects();
	}
	
	private void updatePageRects() {
		if (SHOW_ONE_PAGE == mEffectType) {
			mPageRect.set(mViewRect.left, mViewRect.top, 
					mViewRect.right, mViewRect.bottom);	
		} else {
			// TODO: may be we should support show two page
			//mPageRect.set(mViewRect.width() / 2, mViewRect.top, 
			//		mViewRect.right, mViewRect.bottom);
			mPageRect.set(mViewRect.left, mViewRect.top, 
					mViewRect.right, mViewRect.bottom);	
		}
		
		// TODO: now use the view diagonal as shadow max length.
		mMaxLength = (float) Math.hypot(mPageRect.width(), mPageRect.height());
		
		// re-generate track equation.
		generateTouchTrackEquation();
		// re-set the start curl position.
		setCurlDirect(mCurlDirect);
	}
	
	private void calculateTrackEquation(LinearEquation outEquation, PointF p1, PointF p2) {
		if (null == outEquation || null == p1 || null == p2) {
			return;
		}
		outEquation.a = (p2.y - p1.y) / (p2.x - p1.x);
		outEquation.b = p1.y - (outEquation.a * p1.x);
	}
	
	private void generateTouchTrackEquation() {
		if (SHOW_ONE_PAGE == mEffectType) {
			// x factor
			mTrackP1.set(SRC_FACTOR, mPageRect.right);
			mTrackP2.set(CURL_TRACK_INFLECTION_FACTOR_ONE_PAGE, 
					(mPageRect.right * (1 - CURL_TRACK_INFLECTION_FACTOR_ONE_PAGE)));
			calculateTrackEquation(mCurlPosXLE1, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorX1=" + mCurlPosXLE1);
			
			mTrackP1.set(mTrackP2);
			mTrackP2.set(DST_FACTOR, (mPageRect.left - mPageRect.width()));
			calculateTrackEquation(mCurlPosXLE2, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorX2=" + mCurlPosXLE2);
			
			// y factor
			mTrackP1.set(SRC_FACTOR, mPageRect.bottom);
			mTrackP2.set(CURL_TRACK_INFLECTION_FACTOR_ONE_PAGE, 
					CURL_TRACK_INFLECTION_Y_FACTOR_ONE_PAGE * mPageRect.bottom);
			calculateTrackEquation(mCurlPosYLE1, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorY1=" + mCurlPosYLE1);
			
			mTrackP1.set(mTrackP2);
			mTrackP2.set(DST_FACTOR, mPageRect.bottom);
			calculateTrackEquation(mCurlPosYLE2, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorY2=" + mCurlPosYLE2);
			
		} else {
			// x factor
			mTrackP1.set(SRC_FACTOR, mPageRect.right);
			mTrackP2.set(CURL_TRACK_INFLECTION_FACTOR_TWO_PAGE, 
					(mPageRect.right * (1 - CURL_TRACK_INFLECTION_FACTOR_TWO_PAGE)));
			calculateTrackEquation(mCurlPosXLE1, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorX1=" + mCurlPosXLE1);
			
			mTrackP1.set(mTrackP2);
			mTrackP2.set(DST_FACTOR, (mPageRect.left - mPageRect.width()));
			calculateTrackEquation(mCurlPosXLE2, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorX2=" + mCurlPosXLE2);
			
			// y factor
			mTrackP1.set(SRC_FACTOR, mPageRect.bottom);
			mTrackP2.set(CURL_TRACK_INFLECTION_FACTOR_TWO_PAGE, 
					CURL_TRACK_INFLECTION_Y_FACTOR_TWO_PAGE * mPageRect.bottom);
			calculateTrackEquation(mCurlPosYLE1, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorY1=" + mCurlPosYLE1);
			
			mTrackP1.set(mTrackP2);
			mTrackP2.set(DST_FACTOR, mPageRect.bottom);
			calculateTrackEquation(mCurlPosYLE2, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorY2=" + mCurlPosYLE2);
			
		}
	}
	
	private void updateCurlPosByTouchTrack(float factor) {
		float Yx = 0f;
		float Yy = 0f;
		float inflection = CURL_TRACK_INFLECTION_FACTOR_ONE_PAGE;
		if (SHOW_TWO_PAGE == mEffectType) {
			inflection = CURL_TRACK_INFLECTION_FACTOR_TWO_PAGE;
		}
		if (factor < inflection) {
			Yx = mCurlPosXLE1.a * factor + mCurlPosXLE1.b;
			Yy = mCurlPosYLE1.a * factor + mCurlPosYLE1.b;
		} else {
			Yx = mCurlPosXLE2.a * factor + mCurlPosXLE2.b;
			Yy = mCurlPosYLE2.a * factor + mCurlPosYLE2.b;
		}
		
		mTouch.set(Yx, Yy);
		
		// TODO: to limit some bug position.
		int iTouchX = (int)mTouch.x;
		int iTouchY = (int)mTouch.y;
		if (iTouchX == 0) {
			mTouch.x += 0.09f;
		}
		if (iTouchY == 0) {
			mTouch.y += 0.09f;
		}
		if (iTouchX == mCornerX) {
			mTouch.x -= 0.09f;
		}
		if (iTouchY == mCornerY) {
			mTouch.y -= 0.09f;
		}
	}
	
	private void getCross(PointF outCross, PointF p1, PointF p2, PointF p3, PointF p4) {
		if (null == outCross) {
			return;
		}
		
		// y=ax+b
		float a1 = (p2.y - p1.y) / (p2.x - p1.x);
		float b1 = ((p1.x * p2.y) - (p2.x * p1.y)) / (p1.x - p2.x);

		float a2 = (p4.y - p3.y) / (p4.x - p3.x);
		float b2 = ((p3.x * p4.y) - (p4.x * p3.y)) / (p3.x - p4.x);
		
		outCross.x = (b2 - b1) / (a1 - a2);
		outCross.y = a1 * outCross.x + b1;
	}
	
	private void setCurlDirect(int direct) {
		mCurlDirect = direct;
		//if (CURL_FROM_RIGHT == direct) {
		//	mCornerX = mPageRightRect.right;
		//	mCornerY = mPageRightRect.bottom;
		//} else {
		//	mCornerX = mPageLeftRect.left;
		//	mCornerY = mPageRightRect.bottom;
		//}
		mCornerX = mPageRect.right;
		mCornerY = mPageRect.bottom;
	}
	
	private void calculateBezierCurve() {
		mMiddleX = (mTouch.x + mCornerX) / 2;
		mMiddleY = (mTouch.y + mCornerY) / 2;
		mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
				* (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
		mBezierControl1.y = mCornerY;
		mBezierControl2.x = mCornerX;
		mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
				* (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

		//LogUtils.d(NAME, " ");
		//LogUtils.d(NAME, "============================");
		//LogUtils.d(NAME, "mTouchX  " + mTouch.x + "  mTouchY  " + mTouch.y
		//		+ " pageRect=" + mPageRect);
		//LogUtils.d(NAME, "mBezierControl1.x  " + mBezierControl1.x
		//		+ "  mBezierControl1.y  " + mBezierControl1.y);
		//LogUtils.d(NAME, "mBezierControl2.x  " + mBezierControl2.x
		//		+ "  mBezierControl2.y  " + mBezierControl2.y);

		mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x)
				/ 2;
		mBezierStart1.y = mCornerY;

		// TODO: may be we can modify here to support show two page mode.
		if (mTouch.x > mPageRect.left && mTouch.x < mPageRect.right) {
			if (mBezierStart1.x < mPageRect.left || mBezierStart1.x > mPageRect.right) {   
				if (mBezierStart1.x < mPageRect.left) {      
					mBezierStart1.x = mPageRect.right - mBezierStart1.x;
				}

				float f1 = Math.abs(mCornerX - mTouch.x);
				float f2 = mPageRect.width() * f1 / mBezierStart1.x;
				mTouch.x = Math.abs(mCornerX - f2);
				
				float f3 = Math.abs(mCornerX - mTouch.x)
						* Math.abs(mCornerY - mTouch.y) / f1;
				mTouch.y = Math.abs(mCornerY - f3);
				mMiddleX = (mTouch.x + mCornerX) / 2;
				mMiddleY = (mTouch.y + mCornerY) / 2;
				
				mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
						* (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
				mBezierControl1.y = mCornerY;

				mBezierControl2.x = mCornerX;
				mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
						* (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
				//LogUtils.d(NAME, "adjust mTouchX --> " + mTouch.x + "  mTouchY-->  "
				//		+ mTouch.y);
				//LogUtils.d(NAME, "adjust mBezierControl1.x--  " + mBezierControl1.x
				//		+ "  mBezierControl1.y -- " + mBezierControl1.y);
				//LogUtils.d(NAME, "adjust mBezierControl2.x -- " + mBezierControl2.x
				//		+ "  mBezierControl2.y -- " + mBezierControl2.y);
				mBezierStart1.x = mBezierControl1.x
						- (mCornerX - mBezierControl1.x) / 2;
			}
		}
		
		mBezierStart2.x = mCornerX;
		mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y)
				/ 2;

		mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX),
				(mTouch.y - mCornerY));

		getCross(mBezierEnd1, mTouch, mBezierControl1, mBezierStart1, mBezierStart2);
		getCross(mBezierEnd2, mTouch, mBezierControl2, mBezierStart1, mBezierStart2);

		LogUtils.d(NAME, "mBezierEnd1.x  " + mBezierEnd1.x + "  mBezierEnd1.y  "
				+ mBezierEnd1.y);
		LogUtils.d(NAME, "mBezierEnd2.x  " + mBezierEnd2.x + "  mBezierEnd2.y  "
				+ mBezierEnd2.y);
		
		mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
		mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
		mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
		mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;
	}
	
}
