package com.eebbk.mingming.k7ui.effector;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.eebbk.mingming.k7ui.accessibility.K7GLUtils;
import com.eebbk.mingming.k7ui.accessibility.Utils;
import com.eebbk.mingming.k7utils.BitmapUtils;
import com.eebbk.mingming.k7utils.LogUtils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.PorterDuff;

import android.opengl.GLU;
import android.view.View;

/**
 * 
 * Curl effector implement by GL. </br>
 * This base on an open source page curl project: 
 * <a href="https://github.com/harism/android_page_curl.git/">https://github.com/harism/android_page_curl.git</a> 
 * <b>Notice: </b>this effector don't support SetImage() method, don't use it !!  </br>
 * 
 * </br>
 * <b>Effect: </b> Page curl effector. </br>
 * 
 * </br>
 * <b>Normal type: </b> </br>
 * &nbsp;&nbsp; {@link #SHOW_ONE_PAGE}: source and target is show as one page. </br>
 * &nbsp;&nbsp; {@link #SHOW_TWO_PAGE}: source and target is show in tow pages. </br>
 * 
 * </br>
 * <b>Reverse type: </b> the anti-direction with the normal type. </br>
 * 
 * @author humingming <hmm@dw.gdbbk.com>
 *
 */
public class CurlEffectorGL extends GLEffector {
	
	private final static String NAME = "CurlEffectorGL";
	
	/** This type is useful for portrait */
	public static final int SHOW_ONE_PAGE = VERTICAL;
	/** This type is useful for landscape */
	public static final int SHOW_TWO_PAGE = HORIZONTAL;
	
	// from right: is emulate flip from right to left
	//private final static int CURL_FROM_RIGHT = 0;
	// from left: is emulate flip from left to right.(this is only support in two page type)
	//private final static int CURL_FROM_LEFT = 1;
	
	// Curl translate parameters.
	private final static int CURL_SPRITE = 10;
	private final static float CURL_TRACK_INFLECTION_FACTOR = 0.5f;
	private final static float CURL_TRACK_INFLECTION_MAX_ONE_PAGE = 0.75f;
	private final static float CURL_TRACK_INFLECTION_MAX_TWO_PAGE = 0.65f;
	private final static float CURL_PRESSURE_FACTOR = 0.8f;
	
	private final static int PAGE_FRONT_COLOR = 0xffffffff;
	private final static int PAGE_BACK_COLOR = 0x50ffffff;
		
	private PointF mCurlStartPos = null;
	private PointF mCurlPos = null;
	private PointF mCurlDir = null;
	
	private PointF mLeftPos = null;
	private PointF mLeftDir = null;
	private PointF mRightPos = null;
	private PointF mRightDir = null;
    
	// Page meshes. Left and right meshes are 'static' while curl is used to show page flip.
	private CurlMesh mPageCurl = null;
	private CurlMesh mPageLeft = null;
	private CurlMesh mPageRight = null;
	
	// Curl meshes used for static and dynamic rendering.
	private Vector<CurlMesh> mCurlMeshes = null;
    
	// Page rectangles.
	private RectF mPageRectLeft = null;
	private RectF mPageRectRight = null;
	
	private RectF mMargins = null;
	
	//private int mCurlFromDir = CURL_FROM_RIGHT;
	
	// sub image for two page type.
	// TODO: i can't optimize it, it will cost more memory use -_-|| .
	private Bitmap mImgSrcLeft = null;
	private Bitmap mImgSrcRight = null;
	private Bitmap mImgDstLeft = null;
	private Bitmap mImgDstRight = null;
	
	private RectF mRcSrc = null;
	private RectF mRcDst = null;
	private RectF mRcSrcLeft = null;
	private RectF mRcSrcRight = null;
	private RectF mRcDstLeft = null;
	private RectF mRcDstRight = null;
	
	private Paint mPaint = null;
	private Matrix mMatrix = null;
	private Rect mTmpRect1 = null;
	private Rect mTmpRect2 = null;
	
	// equation for emulate touch track.
	// now we just use linear equation: y = ax + b.
	// the x is mEffectFactor, y is 2 points: curlPos and curlDir.
	// so there have 4 linear equation factors:
	//   curlPos.x-le, curlPos.y-le
	//   curlDir.x-le, curlDir.y-le
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
	private LinearEquation mCurlDirXLE1 = null;
	private LinearEquation mCurlDirYLE1 = null;
	
	private LinearEquation mCurlPosXLE2 = null;
	private LinearEquation mCurlPosYLE2 = null;
	private LinearEquation mCurlDirXLE2 = null;
	private LinearEquation mCurlDirYLE2 = null;
	
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
	
	public CurlEffectorGL() {
		this(null, null, true, false, SHOW_ONE_PAGE);
	}
	
	public CurlEffectorGL(Bitmap imgSrc, Bitmap imgDst, 
			boolean highQuality, boolean reverse, int type) {
		super(imgSrc, imgDst, highQuality, reverse, type);
		
		mName = NAME;
		
		mPaint = new Paint();
		mMatrix = new Matrix();
		mTmpRect1 = new Rect();
		mTmpRect2 = new Rect();
		
		mRcSrc = new RectF();
		mRcDst = new RectF();
		mRcSrcLeft = new RectF();
		mRcSrcRight = new RectF();
		mRcDstLeft = new RectF();
		mRcDstRight = new RectF();
		
		mCurlPosXLE1 = new LinearEquation();
		mCurlPosYLE1 = new LinearEquation();
		mCurlDirXLE1 = new LinearEquation();
		mCurlDirYLE1 = new LinearEquation();
		
		mCurlPosXLE2 = new LinearEquation();
		mCurlPosYLE2 = new LinearEquation();
		mCurlDirXLE2 = new LinearEquation();
		mCurlDirYLE2 = new LinearEquation();
		
		mTrackP1 = new PointF();
		mTrackP2 = new PointF();
		
		mCurlStartPos = new PointF();
		mLeftPos = new PointF();
		mLeftDir = new PointF();
		mRightPos = new PointF();
		mRightDir = new PointF();
		mCurlDir = new PointF();
		mCurlPos = new PointF();
        
		// Even though left and right pages are static we have to allocate room
		// for curl on them too as we are switching meshes. Another way would be
		// to swap texture ids only.
		mPageLeft = new CurlMesh(CURL_SPRITE);
		mPageRight = new CurlMesh(CURL_SPRITE);
		mPageCurl = new CurlMesh(CURL_SPRITE);
		
		mCurlMeshes = new Vector<CurlMesh>();
		
		mPageRectLeft = new RectF();
		mPageRectRight = new RectF();
		mMargins = new RectF(0f, 0f, 0f, 0f);
	}
	
	@Override
	public boolean supportTranBk() {
		return false;
	}
	
	@Override
	public boolean buildImage(int width, int height, Bitmap.Config config, int which) {
		// hack: we make sure to build a PO2 size image.
		int PO2W = Integer.highestOneBit(width - 1) << 1;
		int PO2H = Integer.highestOneBit(height - 1) << 1;
		if (PO2W <= 0 || PO2H <=0) {
			return false;
		}
		
		float fw = (float)width / (float)PO2W;
		//float fwHalf = fw * 0.5f;
		float fh = (float)height / (float)PO2H;
		int subW = PO2W / 2;
		
		if (0 != (ID_SRC & which)) {
			mRcSrc.set(0f, 0f, fw, fh);
			mRcSrcLeft.set(0f, 0f, fw, fh);
			mRcSrcRight.set(0f, 0f, fw, fh);
		}
		if (0 != (ID_DST & which)) {
			mRcDst.set(0f, 0f, fw, fh);
			mRcDstLeft.set(0f, 0f, fw, fh);
			mRcDstRight.set(0f, 0f, fw, fh);
		}
		
		boolean ret = true;
		if (SHOW_ONE_PAGE == mEffectType) {
			if (0 != (ID_SRC & which)) {
				mImgSrc = reCreateImage(mImgSrc, PO2W, PO2H, config);
				ret &= checkImageValid(mImgSrc);
			}
			if (0 != (ID_DST & which)) {
				mImgDst = reCreateImage(mImgDst, PO2W, PO2H, config);
				ret &= checkImageValid(mImgDst);
			}
		} else {
			if (0 != (ID_SRC & which)) {
				mImgSrc = reCreateImage(mImgSrc, width, height, config);
				ret &= checkImageValid(mImgSrc);
			}
			if (0 != (ID_DST & which)) {
				mImgDst = reCreateImage(mImgDst, width, height, config);
				ret &= checkImageValid(mImgDst);
			}
			if (ret) {
				if (0 != (ID_SRC & which)) {
					mImgSrcLeft = reCreateImage(mImgSrcLeft, subW, PO2H, config);
					ret &= checkImageValid(mImgSrcLeft);
					mImgSrcRight = reCreateImage(mImgSrcRight, subW, PO2H, config);
					ret &= checkImageValid(mImgSrcRight);
				}
				if (0 != (ID_DST & which)) {
					mImgDstLeft = reCreateImage(mImgDstLeft, subW, PO2H, config);
					ret &= checkImageValid(mImgDstLeft);
					mImgDstRight = reCreateImage(mImgDstRight, subW, PO2H, config);
					ret &= checkImageValid(mImgSrcRight);
				}
			}
		}
		
		return ret;
	}
	
	@Override
	protected boolean ensureImage(View targetView, int which) {
		// hack: check image match the PO2 size image.
	    Bitmap image = getImage(which);
	    
	    if (null == targetView) {
	    	if (null != image && !image.isRecycled()) {
	    		return true;
	    	} else {
	    		return false;
	    	}
	    }
	    
	    int targetW = targetView.getWidth();
	    int targetH = targetView.getHeight();
		int PO2W = Integer.highestOneBit(targetW - 1) << 1;
		int PO2H = Integer.highestOneBit(targetH - 1) << 1;
		
		boolean matched = false;
		if (SHOW_ONE_PAGE == mEffectType) {
		    if (null != image && !image.isRecycled() && 
		    		PO2W == image.getWidth() && 
		    		PO2H == image.getHeight()) {
		    	matched = true;;
		    }
		} else {
		    if (null != image && !image.isRecycled() && 
		    		targetW == image.getWidth() && 
		    		targetH == image.getHeight()) {
		    	matched = true;;
		    }
		}
		
	    if (matched) {
	    	return true;
	    }
	    
	    if (!buildImage(targetView.getWidth(), 
	    		targetView.getHeight(), 
	    		IMAGE_FORMAT, 
	    		which)) {
	    	return false;
	    }
	    
	    return captureImage(targetView, which);
	}
	
	@Override
	protected void setEffectFactorImpl(float doEffectFactor) {
		// noting to do.
	}
	
	@Override
	protected void setEffectTypeImpl(int type) {
		if (SHOW_ONE_PAGE == type) {
			// one page we don't need the sub image, free it to save memory use.
			BitmapUtils.freeBitmap(mImgSrcLeft);
			BitmapUtils.freeBitmap(mImgSrcRight);
			BitmapUtils.freeBitmap(mImgDstLeft);
			BitmapUtils.freeBitmap(mImgDstRight);
		}
		BitmapUtils.freeBitmap(mImgSrc);
		BitmapUtils.freeBitmap(mImgDst);
		
		// change type, re-start the curl and re-load the texture.
		markUpdateTexture();
		
		// TODO: emulate touch track or curl track ?
		generateTouchTrackEquation();
		resetCurl();
	}
	
	@Override
	protected void setHighQualityImpl(boolean high) {
    	// high quality turn on AA and AF
    	mPaint.setAntiAlias(high);
    	mPaint.setFilterBitmap(high);
	}
	
	@Override
	protected boolean swapImageImpl() {
		// if swap the image, we must re-load the texture
		LogUtils.d(NAME, "swapImageImpl call !!");
		markUpdateTexture();
		return true;
	}
	
	@Override
	protected void reverseEffectImpl(boolean reverse) {
	}
	
	@Override
	protected void freeImpl() {
		if (null != mPageLeft) {
			mPageLeft.recycle();
		}
		if (null != mPageRight) {
			mPageRight.recycle();
		}
		if (null != mPageCurl) {
			mPageCurl.recycle();
		}
		
		BitmapUtils.freeBitmap(mImgSrcLeft);
		BitmapUtils.freeBitmap(mImgSrcRight);
		BitmapUtils.freeBitmap(mImgDstLeft);
		BitmapUtils.freeBitmap(mImgDstRight);
	}
	
	@Override
	protected void onGLReady(GL10 gl, EGLConfig config) {
		super.onGLReady(gl, config);
		    
		// custom GL settings.
		gl.glShadeModel(GL10.GL_SMOOTH);
		
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
		gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);
		gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
		
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_CULL_FACE);
		
		// when the GL is ready(create), we reset the texture.
		// all of the old texture is invalid.
		// you think the GL not ready you don't have texture ??
		// when your activity is onPause the old GL context is lose, 
		// and onResume the new GL context is create, so we must reset the texture.
		mPageLeft.resetTexture();
		mPageRight.resetTexture();
		mPageCurl.resetTexture();
	}
	
	@Override
	protected void onGLContextChanged(GL10 gl, int width, int height) {
		super.onGLContextChanged(gl, width, height);
		
		// custom GL view port and projection.
		// notices: the page curl view rect must set matched the ratio,
		// otherwise the curl dir is not correct !!
		if (height <= 0) {
			return;
		}
		
		mViewPortWidth = width;
		mViewPortHeight = height;
		
		float ratio = (float) width / height;
		mViewRect.top = 1.0f;
		mViewRect.bottom = -1.0f;
		mViewRect.left = -ratio;
		mViewRect.right = ratio;
		
        LogUtils.d(NAME, "onGLContextChanged mViewRect=" + mViewRect + ", this=" + mViewRect.hashCode()
        		+ ", viewPortW=" + mViewPortWidth + ", viewPortH=" + mViewPortHeight);
        
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluOrtho2D(gl, mViewRect.left, mViewRect.right, mViewRect.bottom, mViewRect.top);
		
		// upate the view page rect.
		updatePageRects();
	}

	@Override
	protected boolean doEffectImpl(GL10 output, float doEffectFactor) {
		if (null == output) {
			LogUtils.d(NAME, "the GL context is invalid, we can't render image !");
			return false;
		}
		
		if (!checkTextureValid()) {
			LogUtils.d(NAME, "the target texture is invalid, we don't render the image this time !");
			return false;
		}
		
		// TODO: emulate touch track or curl track ?
		// calculate page curl position.
		updateCurlPosByTouchTrack(doEffectFactor);
		
        // usually, the first thing one might want to do is to clear
        // the screen. The most efficient way of doing this is to use glClear().
		output.glClearColor(mBkColorA, mBkColorR, mBkColorG, mBkColorB);
		output.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        
        // reset 3D object matrix
		output.glMatrixMode(GL10.GL_MODELVIEW);
		output.glLoadIdentity();
		
		// render the curl mesh.
		for (int i = 0; i < mCurlMeshes.size(); ++i) {
			mCurlMeshes.get(i).onDrawFrame(output);
		}
        
        return true;
	}
	
	@Override
	protected boolean autoLoadTexture() {
		// we load texture by ourself.
		return false;
	}
	
	@Override
    protected boolean checkTextureValidImpl() {
		// we check our texture ourself.
		if (SHOW_ONE_PAGE == mEffectType) {
			return checkImage(ID_SRC | ID_DST);
		} else {
			boolean ret = true;
			ret &= K7GLUtils.checkImagePO2(mImgDstLeft);
			ret &= K7GLUtils.checkImagePO2(mImgDstRight);
			ret &= K7GLUtils.checkImagePO2(mImgDstLeft);
			ret &= K7GLUtils.checkImagePO2(mImgDstRight);
			return ret;
		}
    }
	
	@Override
	protected boolean updateTextureImpl(GL10 gl) {
		LogUtils.d(NAME, "updateTextureImpl !!");
		if (!checkImage(ID_SRC | ID_DST)) {
			LogUtils.d(NAME, "updateTextureImpl failed, the origin image is invaild !!");
			return false;
		}
		
		//K7GLUtils.debugDumpImage(mImgSrc);
		//K7GLUtils.debugDumpImage(mImgDst);
		
		Bitmap imgSrc = getImage(ID_SRC);
		Bitmap imgDst = getImage(ID_DST);
		
		CurlPage pageTexture = null;
		if (SHOW_ONE_PAGE == mEffectType) {
			pageTexture = mPageRight.getTexturePage();
			pageTexture.setTexture(imgDst, mRcDst, CurlPage.SIDE_FRONT);
			pageTexture.setTexture(null, mRcDst, CurlPage.SIDE_BACK);
			pageTexture.setColor(PAGE_FRONT_COLOR, CurlPage.SIDE_BOTH);
			
			pageTexture = mPageCurl.getTexturePage();
			pageTexture.setTexture(imgSrc, mRcSrc, CurlPage.SIDE_FRONT);
			pageTexture.setTexture(null, mRcSrc, CurlPage.SIDE_BACK);
			pageTexture.setColor(PAGE_BACK_COLOR, CurlPage.SIDE_BACK);
		} else {
			updateTextureForTwoPage(ID_SRC);
			updateTextureForTwoPage(ID_DST);
			//K7GLUtils.debugDumpImage(mImgSrc);
			//K7GLUtils.debugDumpImage(mImgSrcLeft);
			//K7GLUtils.debugDumpImage(mImgSrcRight);
			
			pageTexture = mPageLeft.getTexturePage();
			pageTexture.setTexture(mImgSrcLeft, mRcSrcLeft, CurlPage.SIDE_FRONT);
			pageTexture.setTexture(null, mRcSrcLeft, CurlPage.SIDE_BACK);
			pageTexture.setColor(PAGE_FRONT_COLOR, CurlPage.SIDE_BOTH);
			
			pageTexture = mPageRight.getTexturePage();
			pageTexture.setTexture(mImgDstRight, mRcDstRight, CurlPage.SIDE_FRONT);
			pageTexture.setTexture(null, mRcDstRight, CurlPage.SIDE_BACK);
			pageTexture.setColor(PAGE_FRONT_COLOR, CurlPage.SIDE_BOTH);
			
			pageTexture = mPageCurl.getTexturePage();
			pageTexture.setTexture(mImgSrcRight, mRcSrcRight, CurlPage.SIDE_FRONT);
			pageTexture.setTexture(mImgDstLeft, mRcDstLeft, CurlPage.SIDE_BACK);
			pageTexture.setColor(PAGE_FRONT_COLOR, CurlPage.SIDE_BOTH);
		}
		
		return true;
	}
	
	private boolean updateTextureForTwoPage(int which) {
		Bitmap imgLeft = null;
		Bitmap imgRight = null;
		Bitmap imgOrg = null;
		
		Bitmap imgSrc = getImage(ID_SRC);
		Bitmap imgDst = getImage(ID_DST);
		
		if (ID_SRC == which) {
			imgOrg = imgSrc;
			imgLeft = mImgSrcLeft;
			imgRight = mImgSrcRight;
		} else {
			imgOrg = imgDst;
			imgLeft = mImgDstLeft;
			imgRight = mImgDstRight;
		}
		
		if (!checkImageValid(imgOrg)) {
			LogUtils.d(NAME, "updateTextureForTwoPage: which=" + which + " is failed: origin image invaild !");
			return false;
		}
		
		// first we check the image whether matched the origin.
		int targetW = imgOrg.getWidth() / 2;
		int targetH = imgOrg.getHeight();
		 
		// we don't re-create sub image here, the sub image should create in build origin image 
		// which current type is SHOW_TWO_PAGE, and must be 2^n size.
		//imgLeft = reCreateImage(imgLeft, targetW, targetH, imgOrg.getConfig());
		if (!checkImageValid(imgLeft)) {
			LogUtils.d(NAME, "updateTextureForTwoPage: which=" + which + " is failed: sub left image invaild !");
			return false;
		}
		//imgRight = reCreateImage(imgRight, targetW, targetH, imgOrg.getConfig());
		if (!checkImageValid(imgRight)) {
			LogUtils.d(NAME, "updateTextureForTwoPage: which=" + which + " is failed: sub right image invaild !");
			return false;
		}
		
		mTmpRect1.set(0, 0, targetW, targetH);
		mTmpRect2.set(targetW, 0, targetW * 2, targetH);
		
		// update the new image to two page texture.
		mCaptureCanvas.setBitmap(imgLeft);
		int saveID = mCaptureCanvas.save(); {
			
			mCaptureCanvas.clipRect(mTmpRect1, Region.Op.REPLACE);
			mCaptureCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
			if (ID_SRC == which) {
				mCaptureCanvas.drawBitmap(imgOrg, mTmpRect1, mTmpRect1, null);
			} else {
				// we get half image of origin.
				mMatrix.reset();
				mMatrix.setScale(-1f, 1f);
				
				mMatrix.postTranslate(targetW, 0);
				mCaptureCanvas.drawBitmap(imgOrg, mMatrix, null);
				//K7GLUtils.debugDumpImage(imgLeft);
			}
			
		} mCaptureCanvas.restoreToCount(saveID);
		mCaptureCanvas.setBitmap(null);
		
		mCaptureCanvas.setBitmap(imgRight);
		saveID = mCaptureCanvas.save(); {
			
			mCaptureCanvas.clipRect(mTmpRect1, Region.Op.REPLACE);
			mCaptureCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
			mCaptureCanvas.drawBitmap(imgOrg, mTmpRect2, mTmpRect1, null);
			
		} mCaptureCanvas.restoreToCount(saveID);
		mCaptureCanvas.setBitmap(null);
		
		// save the new two page texture image.
		if (ID_SRC == which) {
			mImgSrcLeft = imgLeft;
			mImgSrcRight = imgRight;
		} else {
			mImgDstLeft = imgLeft;
			mImgDstRight = imgRight;
		}
		
		return true;
	}
	
	@Override
	protected void updateCacheTextureCoordsImpl() {		
        // we not use the auto-load texture so this method we do noting.
	}
	
	/**
	 * Set page margins, this is useful for show page curl over view rect effect. 
	 * The factor unit is weight, not pixel.
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void setMargins(float left, float top, float right, float bottom) {
		mMargins.set(left, top, right, bottom);
		updatePageRects();
	}
	
	/**
	 * Get page margins. The factor unit is weight, not pixel.
	 * 
	 * @return don't modify it.
	 */
	public final RectF getMargins() {
		return mMargins;
	}
	
	/**
	 * Switches meshes and loads new bitmaps if available. 
	 * Updated to support 2 pages in landscape
	 */
	private void resetCurl() {
		// Remove meshes from renderer.
		removeCurlMesh(mPageLeft);
		removeCurlMesh(mPageRight);
		removeCurlMesh(mPageCurl);
			
		if (SHOW_ONE_PAGE == mEffectType) {
			mPageRight.setRect(mPageRectRight);
			mPageRight.setFlipTexture(false);
			mPageRight.reset();
			addCurlMesh(mPageRight);
				
			mPageCurl.setRect(mPageRectRight);
			mPageCurl.setFlipTexture(false);
			mPageCurl.reset();
			addCurlMesh(mPageCurl);
				
			mCurlStartPos.set(mPageRectRight.right, mPageRectRight.bottom);
			
		} else {
			mPageLeft.setRect(mPageRectLeft);
			mPageLeft.setFlipTexture(false);
			mPageLeft.reset();
			addCurlMesh(mPageLeft);
				
			mPageRight.setRect(mPageRectRight);
			mPageRight.setFlipTexture(false);
			mPageRight.reset();
			addCurlMesh(mPageRight);
				
			mPageCurl.setRect(mPageRectRight);
			mPageCurl.setFlipTexture(false);
			mPageCurl.reset();
			addCurlMesh(mPageCurl);
				
			mCurlStartPos.set(mPageRectRight.right, mPageRectRight.bottom);
		}
	}
	
	/**
	 * Recalculates page rectangles.
	 */
	private void updatePageRects() {
		if (mViewRect.width() == 0 || mViewRect.height() == 0) {
			return;
		}
		
		if (SHOW_ONE_PAGE == mEffectType) {
			mPageRectRight.set(mViewRect);
			mPageRectRight.left += mViewRect.width() * mMargins.left;
			mPageRectRight.right -= mViewRect.width() * mMargins.right;
			mPageRectRight.top += mViewRect.height() * mMargins.top;
			mPageRectRight.bottom -= mViewRect.height() * mMargins.bottom;

			mPageRectLeft.set(mPageRectRight);
			mPageRectLeft.offset(-mPageRectRight.width(), 0);
			
			mPageRight.setRect(mPageRectRight);
			mPageCurl.setRect(mPageRectRight);
			
		} else {
			mPageRectRight.set(mViewRect);
			mPageRectRight.left += mViewRect.width() * mMargins.left;
			mPageRectRight.right -= mViewRect.width() * mMargins.right;
			mPageRectRight.top += mViewRect.height() * mMargins.top;
			mPageRectRight.bottom -= mViewRect.height() * mMargins.bottom;
			
			mPageRectLeft.set(mPageRectRight);
			mPageRectLeft.right = (mPageRectLeft.right + mPageRectLeft.left) / 2;
			mPageRectRight.left = mPageRectLeft.right;
			
			mPageLeft.setRect(mPageRectLeft);
			mPageRight.setRect(mPageRectRight);
			mPageCurl.setRect(mPageRectRight);
		}
		
		// TODO: emulate touch track or curl track ?
		generateTouchTrackEquation();
		resetCurl();
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
			mCurlStartPos.set(mPageRectRight.right, mPageRectRight.bottom);
		
			// x factor
			mTrackP1.set(SRC_FACTOR, mPageRectRight.right);
			mTrackP2.set(CURL_TRACK_INFLECTION_FACTOR, 0f);
			calculateTrackEquation(mCurlPosXLE1, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorX1=" + mCurlPosXLE1);
		
			mTrackP1.set(mTrackP2);
			mTrackP2.set(DST_FACTOR, mPageRectRight.left);
			calculateTrackEquation(mCurlPosXLE2, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorX2=" + mCurlPosXLE2);
			
			// y factor
			mTrackP1.set(SRC_FACTOR, mPageRectRight.bottom);
			mTrackP2.set(CURL_TRACK_INFLECTION_FACTOR, 
					CURL_TRACK_INFLECTION_MAX_ONE_PAGE * mPageRectRight.bottom);
			calculateTrackEquation(mCurlPosYLE1, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorY1=" + mCurlPosYLE1);
			
			mTrackP1.set(mTrackP2);
			mTrackP2.set(DST_FACTOR, mPageRectRight.bottom);
			calculateTrackEquation(mCurlPosYLE2, mTrackP1, mTrackP2);
			//LogUtils.d("test", "trackP1=" + mTrackP1 + ", trackP2=" + mTrackP2
			//		+ "; factorY2=" + mCurlPosYLE2);
			
			mRightPos.set(mCurlStartPos);
			mRightDir.set(0f, 0f);
		
		} else {
			mCurlStartPos.set(mPageRectRight.right, mPageRectRight.bottom);
			
			// x factor
			mTrackP1.set(SRC_FACTOR, mPageRectRight.right);
			mTrackP2.set(CURL_TRACK_INFLECTION_FACTOR, mPageRectRight.width() / 2f);
			calculateTrackEquation(mCurlPosXLE1, mTrackP1, mTrackP2);
			
			mTrackP1.set(mTrackP2);
			mTrackP2.set(DST_FACTOR, mPageRectRight.left);
			calculateTrackEquation(mCurlPosXLE2, mTrackP1, mTrackP2);
			
			// y factor
			mTrackP1.set(SRC_FACTOR, mPageRectRight.bottom);
			mTrackP2.set(CURL_TRACK_INFLECTION_FACTOR, 
					CURL_TRACK_INFLECTION_MAX_TWO_PAGE * mPageRectRight.bottom);
			calculateTrackEquation(mCurlPosYLE1, mTrackP1, mTrackP2);
			
			mTrackP1.set(mTrackP2);
			mTrackP2.set(DST_FACTOR, mPageRectRight.bottom);
			calculateTrackEquation(mCurlPosYLE2, mTrackP1, mTrackP2);
			
			mLeftPos.set(mPageRectLeft.right, mPageRectLeft.bottom);
			mLeftDir.set(0f, 0f);
			mRightPos.set(mCurlStartPos);
			mRightDir.set(0f, 0f);
		}
	}
	
	@SuppressWarnings("unused")
	private void generateCurlTrackEquation() {
		
	}
	
	/**
	 * Updates curl position.
	 */
	private void updateCurlPosByTouchTrack(float factor) {
		// Default curl radius.
		double radius = mPageRectRight.width() / 3;
		// TODO: This is not an optimal solution. Based on feedback received so
		// far; pressure is not very accurate, it may be better not to map
		// coefficient to range [0f, 1f] but something like [.2f, 1f] instead.
		// Leaving it as is until get my hands on a real device. On emulator
		// this doesn't work anyway.
		//radius *= Math.max(1f - pointerPos.mPressure, 0f);
		radius *= Math.max(1f - CURL_PRESSURE_FACTOR, 0f);
		
		float Yx = 0f;
		float Yy = 0f;
		if (factor < CURL_TRACK_INFLECTION_FACTOR) {
			Yx = mCurlPosXLE1.a * factor + mCurlPosXLE1.b;
			Yy = mCurlPosYLE1.a * factor + mCurlPosYLE1.b;
		} else {
			Yx = mCurlPosXLE2.a * factor + mCurlPosXLE2.b;
			Yy = mCurlPosYLE2.a * factor + mCurlPosYLE2.b;
		}
		
		mCurlPos.set(Yx, Yy);
		mCurlDir.x = mCurlPos.x - mCurlStartPos.x;
		mCurlDir.y = mCurlPos.y - mCurlStartPos.y;
		
		//LogUtils.d("test", "setup-1: factor: " + factor 
		//		+ ", pos: " + mCurlPos.toString() + ", dir: " + mCurlDir.toString() 
		//		+ ", factorX1=" + mCurlPosXLE1 + ", factorY1=" + mCurlPosYLE1 
		//		+ ", factorX2=" + mCurlPosXLE2 + ", factorY2=" + mCurlPosYLE2);
		
		// Adjust curl radius so that if page is dragged far enough on
		// opposite side, radius gets closer to zero.
		float pageWidth = mPageRectRight.width();
		double curlLen = radius * Math.PI;
		float dist = (float) Math.sqrt(mCurlDir.x * mCurlDir.x + mCurlDir.y * mCurlDir.y);
		if (dist > (pageWidth * 2) - curlLen) {
			curlLen = Math.max((pageWidth * 2) - dist, 0f);
			radius = curlLen / Math.PI;
		}
		
		// Actual curl position calculation.
		if (!Utils.equalZeroF(dist)) {
			if (dist >= curlLen) {
				double translate = (dist - curlLen) / 2;
				//if (SHOW_TWO_PAGE == mEffectType) {
				//	mCurlPos.x -= mCurlDir.x * translate / dist;
				//} else {
				//	float pageLeftX = mPageRectRight.left;
				//	radius = Math.max(Math.min(mCurlPos.x - pageLeftX, radius), 0f);
				//}
				float pageLeftX = mPageRectRight.left;
				radius = Math.max(Math.min(mCurlPos.x - pageLeftX, radius), 0f);
				mCurlPos.y -= mCurlDir.y * translate / dist;				
			} else {
				double angle = Math.PI * Math.sqrt(dist / curlLen);
				double translate = radius * Math.sin(angle);
				mCurlPos.x += mCurlDir.x * translate / dist;
				mCurlPos.y += mCurlDir.y * translate / dist;
			}
		}
		
		if (SHOW_TWO_PAGE == mEffectType) {
			setCurlPosByTouchTrack(mPageLeft, mPageRectLeft, mLeftPos, mLeftDir, radius);
		}
		setCurlPosByTouchTrack(mPageRight, mPageRectRight, mRightPos, mRightDir, radius);
		setCurlPosByTouchTrack(mPageCurl, mPageRectRight, mCurlPos, mCurlDir, radius);
	}
	
	/**
	 * Updates curl position.
	 */
	@SuppressWarnings("unused")
	private void updateCurlPosByCurlTrack(float factor) {
		// Default curl radius.
		double radius = mPageRectRight.width() / 3;
		// TODO: This is not an optimal solution. Based on feedback received so
		// far; pressure is not very accurate, it may be better not to map
		// coefficient to range [0f, 1f] but something like [.2f, 1f] instead.
		// Leaving it as is until get my hands on a real device. On emulator
		// this doesn't work anyway.
		//radius *= Math.max(1f - pointerPos.mPressure, 0f);
		radius *= Math.max(1f - 0.8f, 0f);
		
		float Yx = 0f;
		float Yy = 0f;
		if (factor < CURL_TRACK_INFLECTION_FACTOR) {
			Yx = mCurlPosXLE1.a * factor + mCurlPosXLE1.b;
			Yy = mCurlPosYLE1.a * factor + mCurlPosYLE1.b;
		} else {
			Yx = mCurlPosXLE2.a * factor + mCurlPosXLE2.b;
			Yy = mCurlPosYLE2.a * factor + mCurlPosYLE2.b;
		}
		mCurlPos.set(Yx, Yy);
		
		if (factor < CURL_TRACK_INFLECTION_FACTOR) {
			Yx = mCurlDirXLE1.a * factor + mCurlDirXLE1.b;
			Yy = mCurlDirYLE1.a * factor + mCurlDirYLE1.b;
		} else {
			Yx = mCurlDirXLE2.a * factor + mCurlDirXLE2.b;
			Yy = mCurlDirYLE2.a * factor + mCurlDirYLE2.b;
		}
		mCurlDir.set(Yx, Yy);
		
		if (SHOW_TWO_PAGE == mEffectType) {
			setCurlPosByCurlTrack(mPageLeft, mLeftPos, mLeftDir, radius);
		}
		setCurlPosByCurlTrack(mPageRight, mRightPos, mRightDir, radius);
		setCurlPosByCurlTrack(mPageCurl, mCurlPos, mCurlDir, radius);
	}
	
	/**
	 * Sets mPageCurl curl position.
	 */
	private void setCurlPosByTouchTrack(CurlMesh page, RectF pageRect, 
			PointF curlPos, PointF curlDir, double radius) {
		if (null == page || null == pageRect) {
			return;
		}
		
		// First reposition curl so that page doesn't 'rip off' from book.
		if (curlPos.x >= pageRect.right) {
			page.reset();
			return;
		}
		if (curlPos.x < pageRect.left) {
			curlPos.x = pageRect.left;
		}
		if (curlDir.y != 0) {
			float diffX = curlPos.x - pageRect.left;
			float leftY = curlPos.y + (diffX * curlDir.x / curlDir.y);
			if (curlDir.y < 0 && leftY < pageRect.top) {
				curlDir.x = curlPos.y - pageRect.top;
				curlDir.y = pageRect.left - curlPos.x;
			} else if (curlDir.y > 0 && leftY > pageRect.bottom) {
				curlDir.x = pageRect.bottom - curlPos.y;
				curlDir.y = curlPos.x - pageRect.left;
			}
		}
		
		// Finally normalize direction vector and do rendering.
		double dist = Math.sqrt(curlDir.x * curlDir.x + curlDir.y * curlDir.y);
		if (dist != 0) {
			curlDir.x /= dist;
			curlDir.y /= dist;
			page.curl(curlPos, curlDir, radius);
		} else {
			page.reset();
		}
		
		page.curl(curlPos, curlDir, radius);
	}
	
	/**
	 * Sets mPageCurl curl position.
	 */
	private void setCurlPosByCurlTrack(CurlMesh page, PointF curlPos, PointF curlDir, double radius) {
		if (null == page) {
			return;
		}
		page.curl(curlPos, curlDir, radius);
	}
	
	/**
	 * Adds CurlMesh to this renderer.
	 */
	private synchronized void addCurlMesh(CurlMesh mesh) {
		removeCurlMesh(mesh);
		mCurlMeshes.add(mesh);
	}
	
	/**
	 * Removes CurlMesh from this renderer.
	 */
	private synchronized void removeCurlMesh(CurlMesh mesh) {
		while (mCurlMeshes.remove(mesh))
			;
	}
	
}
