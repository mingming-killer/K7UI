package com.eebbk.mingming.k7ui.effector;

import static android.opengl.GLES10.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.eebbk.mingming.k7ui.accessibility.K7GLUtils;
import com.eebbk.mingming.k7utils.BitmapUtils;
import com.eebbk.mingming.k7utils.LogUtils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.opengl.GLU;
import android.view.View;

/**
 * 
 * OpenGL base effector. (see {@link Effector}) </br>
 * 
 * </br>
 * TODO: usually it don't need to consider the texture is invalid, because when
 * activity is killed in background, all of the resource will be re-create.
 * 
 * @author humingming <hmm@dw.gdbbk.com>
 *
 */
public abstract class GLEffector extends Effector {
	
	private final static String NAME = "GLEffector";
	
	/** max support image size */
	protected final static int MAX_IMAGE_SIZE = 2048;
	
	/** max support texture size (2 x MAX_IMAGE_SIZE) */
	protected final static int MAX_TEXTURE_SIZE = MAX_IMAGE_SIZE * 2;
	
	/** 
	 * 2^n align texture cache. In OpenGLES except nvidia tegra, other GPU is non support no-power of two texture EXT.
	 * So you must align the texture image, now we put the source and target image horizontal(src left, dst right).
	 */
	protected Bitmap mTexCache = null;
	
	/** image texture Id, the src and dst all in one. */
	protected int mTexID[] = null;
	
	protected PointF mSrcTexCoordStart = null;
	protected PointF mSrcTexCoordEnd = null;
	
	protected PointF mDstTexCoordStart = null;
	protected PointF mDstTexCoordEnd = null;
	
	protected int mViewPortWidth = 0;
	protected int mViewPortHeight = 0;
	protected RectF mViewRect = null; 
	
	protected float mBkColorR = 0f;
	protected float mBkColorG = 0f;
	protected float mBkColorB = 0f;
	protected float mBkColorA = 0f;
	
	private boolean mNeedUpdateTexture = false;
	private Rect mTmpRect1 = null;
	private Rect mTmpRect2 = null;
	private Point mCacheSize = null;
	
	protected GLResponser mGLResponser = null;
	
	/**
	 * 
	 * GL states responser.
	 * 
	 * @author humingming <hmm@dw.gdbbk.com>
	 *
	 */
	public interface GLResponser {
		
		/**
		 * Query GL whether is ready to render.
		 * 
		 * @return True: ready, false: not yeah.
		 */
		public boolean isGLReady();
		
	}
	
	/**
	 * see {@link #Effector(Bitmap, Bitmap, boolean, boolean, int)} </br>
	 * default params： null, null. true, false, HORIZONTAL
	 */
	public GLEffector() {
		this(null, null, true, false, HORIZONTAL);
	}
	
	/**
	 * Create a hardware effector.
	 * 
	 * @param imgSrc Source image.
	 * @param imgDst Target image
	 * @param highQuality True: high quality effect，false: low
	 * @param reverse True: reveres effect，false: normal
	 * @param type {@link #HORIZONTAL}, {@link #VERTICAL}
	 */
	public GLEffector(Bitmap imgSrc, Bitmap imgDst, 
			boolean highQuality, boolean reverse, int type) {
		super(imgSrc, imgDst, highQuality, reverse, type);
		
		mTexID = new int[1];
		mTexID[0] = K7GLUtils.INVALID_TEXTURE_ID;
		
		// set texture
		setImage(imgSrc, imgDst);
		
		mSrcTexCoordStart = new PointF(0, 0);
		mSrcTexCoordEnd = new PointF(0, 0);
		
		mDstTexCoordStart = new PointF(0, 0);
		mDstTexCoordEnd = new PointF(0, 0);
		
		mViewPortWidth = 0;
		mViewPortHeight = 0;
		mViewRect = new RectF();
		
		mTmpRect1 = new Rect();
		mTmpRect2 = new Rect();
		mCacheSize = new Point();
	}
	
	@Override
	public boolean doEffect(Canvas output) {
		throw new RuntimeException(NAME + 
				": hardware effector should nerver call this method, it's for software effector !");
	}
	
	@Override
	protected boolean doEffectImpl(Canvas output, float doEffectFactor) {
		throw new RuntimeException(NAME + 
				": hardware effector should nerver call this method, it's for software effector !");
	}
	
	@Override
	public boolean doEffect(GL10 output, float factor) {
		if (!isGLReady()) {
			LogUtils.d(NAME, "doEffect: GL is not ready we can't render graphic !");
			return false;
		}
		
		setEffectFactor(factor);
		
		if (null == output) {
			LogUtils.d(NAME, "do effect failed: output GL handler is null !");
			return false;
		}
		
		// if the texture update flag is marked, we update it.
		if (isNeedUpdateTexture()) {
			updateTexture(output);
		}
		
		return doEffectImpl(output, getUseEffectFactor());
	}
	
	@Override
	public boolean isGL() {
		return true;
	}
	
	@Override
	public String toString() {
		try {
			return "Effector: " + mName + ", imgTex: " + mTexCache 
					+ ", texID: " + mTexID[0] 
					+ ", texSrcStartPos: " + mSrcTexCoordStart + ", texSrcEndPos: " + mSrcTexCoordEnd 
					+ ", texDstStartPos: " + mDstTexCoordStart + ", texDstEndPos: " + mDstTexCoordEnd  
					+ ", factor: " + mEffectFactor + ", type: " + mEffectType
					+ ", quality: " + mHighQuality + ", reverse" + mReverse;
		} catch (Exception e) {
			return "";
		}
	}
	
	@Override
	public void freeImage(int which) {
		super.freeImage(which);
		
		// release texture cache.
		BitmapUtils.freeBitmap(mTexCache);
		mTexCache = null;
		
		// release bind texture.
		//if (isGLReady()) {
		//	if (0 != (ID_SRC & which) || 0 != (ID_DST & which)) {
		//		K7GLUtils.freeTexture(mTexID);
		//	} 
		//}
	}
	
	@Override
	public void setImage(Bitmap imgSrc, Bitmap imgDst) {
		// if subclass don't need auto load texture, we keep the origin image.
		if (!autoLoadTexture()) {
			super.setImage(imgSrc, ID_SRC);
			super.setImage(imgSrc, ID_DST);
			markUpdateTexture();
		} else {
			// auto load texture, we make the texture cache.
			if (!checkImageValid(imgSrc) || !checkImageValid(imgDst)) {
				LogUtils.e(NAME, "target image invalid, we ignore it !!");
				return;
			}
			
			// TODO: we assume src and dst image is in the same size.
			cachePolicy(imgSrc.getWidth(), imgSrc.getHeight());
			ensureImageSizeWithPO2(mTexCache, mCacheSize.x, mCacheSize.y, IMAGE_FORMAT);
			cacheImage(imgSrc, ID_SRC);
			cacheImage(imgDst, ID_DST);
		}
	}
	
	@Override
	public void setImage(Bitmap image, int which) {
		// if subclass don't need auto load texture, we keep the origin image.
		if (!autoLoadTexture()) {
			super.setImage(image, which);
		} else {
			// auto load texture, we make the texture cache.
			if (!checkImageValid(image)) {
				LogUtils.e(NAME, "target image invalid, we ignore it !!");
				return;
			}
			
			// TODO: we assume src and dst image is in the same size.
			cachePolicy(image.getWidth(), image.getHeight());
			ensureImageSizeWithPO2(mTexCache, mCacheSize.x, mCacheSize.y, IMAGE_FORMAT);
			cacheImage(image, which);
		}
	}
	
	@Override
	public void setBkColor(int color) {
		super.setBkColor(color);
		mBkColorR = Color.red(color) / 255f;
		mBkColorG = Color.green(color) / 255f;
		mBkColorB = Color.blue(color) / 255f;
		mBkColorA = Color.alpha(color) / 255f;
	}
	
    /**
     * Set GL state responser
     * 
     * @param responser
     */
    public void setGLResponser(GLResponser responser) {
    	mGLResponser = responser;
    }
    
	/**
	 * Ensure the source image is valid(texture cache). </br>
	 * When set {@link #autoLoadTexture()}, it will check 2^n texture cache whether non-recycle, 2^n align, 
	 * large then 2*view size. if is invalid, it will re-create texture cache.
	 * 
	 * @param srcView Source view.
	 * @param dstView Target view.
	 * @return True: image is ensure valid，false: image invalid(maybe re-create failed, maybe capture failed).
	 */
    @Override
    public boolean ensureImage(View srcView, View dstView) {
    	// if subclass don't need auto load texture, we keep the origin image.
    	if (!autoLoadTexture()) {
    		return super.ensureImage(srcView, dstView);
    	} else {
    		// auto load texture, we make the texture cache.
        	// select a non-null view as target
        	View targetView = srcView;
        	if (null == targetView) {
        		targetView = dstView;
        	}
        	
    		if (null == targetView) {
    			return K7GLUtils.checkImagePO2(mTexCache);
    		}
    		
        	int oldW = -1;
        	int oldH = -1;
        	if (null != mTexCache) {
        		oldW = mTexCache.getWidth();
        		oldH = mTexCache.getHeight();
        	}
        	
        	// ensure our texture size is 2^n
        	cachePolicy(targetView.getWidth(), targetView.getHeight());
        	mTexCache = ensureImageSizeWithPO2(mTexCache, 
        			mCacheSize.x, mCacheSize.y, IMAGE_FORMAT);
        	if (!K7GLUtils.checkImagePO2(mTexCache)) {
        		LogUtils.e(NAME, "ensure image failed !!");
        		return false;
        	}
    	    
    	    // if the target image is not changed, we don't update the texture.
        	boolean updated = true;
    	    if (mTexCache.getWidth() == oldW && mTexCache.getHeight() == oldH) {
    	    	updated = false;
    	    } else {
    	    	updated = true;
    	    }
    	    
        	// if target image is changed, we re-capture the image as texture cache.
        	if (updated) {
        		captureImage(srcView, dstView);
        		//updateCacheTextureCoords(targetView.getWidth(), targetView.getHeight());
        	}
        	
        	return true;
    	}
    }
	
	@Override
	public boolean captureImage(View targetView, int which) {		
    	// if subclass don't need auto load texture, we keep the origin image.
    	if (!autoLoadTexture()) {
    		boolean ret = super.captureImage(targetView, which);
    		// if capture a new origin image, than means the texture source is change,
    		// the subclass should reload it.
    		if (ret) {
    			markUpdateTexture();
    		}
    		return ret;
    	} else {
    		// auto load texture, we make the texture cache.
    		if (null == targetView) {
    			LogUtils.d(NAME, "catpure image failed: target view is null !");
    			return false;
    		}
    		
    		int targetW = targetView.getWidth();
    		int targetH = targetView.getHeight();
    		cachePolicy(targetW, targetH);
    		if (!K7GLUtils.checkImagePO2(mTexCache)) {
    			if (!buildImage(mCacheSize.x, mCacheSize.y, IMAGE_FORMAT, which)) {
    				LogUtils.d(NAME, "catpure image failed: build image failed !");
    				return false;
    			}
    		}
    		
    		// TODO:
    		// if old image size is different with target view, we re-create texture cache.
    		if (mTexCache.getWidth() < mCacheSize.x || mTexCache.getHeight() < mCacheSize.y) {
    			if (!buildImage(mCacheSize.x, mCacheSize.y, IMAGE_FORMAT, which)) {
    				LogUtils.d(NAME, "catpure image failed: build image failed !");
    				return false;
    			}
    		}
    		
    		int startX = 0;
    		int startY = 0;
    		// TODO: 
    		int targetPO2W = (int) (mTexCache.getWidth() / 2f);
    		int targetPO2H = mTexCache.getHeight();
    		if (ID_DST == which) {
    			startX = targetPO2W;
    		}
    		
    		mCaptureCanvas.setBitmap(mTexCache);
    		int saveID = mCaptureCanvas.save(); 
    		{	
    			// limit clip rect, don't draw to another side.
    			mCaptureCanvas.clipRect(startX, startY, 
    					startX + targetPO2W, startY + targetPO2H, 
    					Region.Op.REPLACE);
    			
    			// easer with background color.
    			mCaptureCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
    			
    			// sometime the scroll of view is not correct, it will cost view clip rect to 0.
    			// reference android View methods: 
    			// Bitmap createSnapshot(Bitmap.Config quality, int backgroundColor, boolean skipChildren)
    			targetView.computeScroll();
    			int scrollX = targetView.getScrollX();
    			int scrollY = targetView.getScrollY();
    			// TODO: now don't care view scale.
    			// mCaptureCanvas.scale(scale, scale);
    			if (ID_DST == which) {
    				mCaptureCanvas.translate((-scrollX) + targetPO2W, -scrollY);
    			} else {
    				mCaptureCanvas.translate(-scrollX, -scrollY);
    			}
    			
    			targetView.draw(mCaptureCanvas);
    		} 
    		mCaptureCanvas.restoreToCount(saveID);
    		mCaptureCanvas.setBitmap(null);
    		
    		// update texture coords.
    		updateTexture(targetW, targetH);
    		
    		//K7GLUtils.debugDumpImage(mTexCache); 
    		
    		return true;
    	}
	}
	
	/**
	 * 填充特效图像。 </br>
	 * 一般用于变化的只有一个 View 的情况下（例如说换页切换到尽头了）。
	 * 
	 * 
	 * @param drawable 填充的 {@link Drawable}，外部设置好 Bounds。
	 * @param which {@link ID_SRC} or {@link ID_DST}
	 * @return True 填充成功，false 填充失败
	 */
	@Override
	public boolean fillImage(Drawable drawable, int which) {		
    	// if subclass don't need auto load texture, we use the super method.
    	if (!autoLoadTexture()) {
    		boolean ret = super.fillImage(drawable, which);
    		// if fill a new origin image, than means the texture source is change,
    		// the subclass should reload it.
    		if (ret) {
    			markUpdateTexture();
    		}
    		return ret;
    	} else {
    		// auto load texture, we make the texture cache.
    		if (null == drawable) {
    			LogUtils.d(NAME, "fill image failed: fill drawable is null !");
    			return false;
    		}
    		
    		if (!K7GLUtils.checkImagePO2(mTexCache) || 
    				mTexCache.getWidth() < mCacheSize.x || mTexCache.getHeight() < mCacheSize.y) {
    			LogUtils.d(NAME, "fill image failed: texture cache image invalid !");
    			return false;
    		}
    		
    		mTmpRect1.set(drawable.getBounds());
    		mTmpRect2.set(mTmpRect1);
    		
    		int targetW = mTmpRect1.width();
    		int targetH = mTmpRect1.height();
    		cachePolicy(targetW, targetH);
    		
    		int startX = 0;
    		int startY = 0;
    		// TODO: 
    		int targetPO2W = (int) (mTexCache.getWidth() / 2f);
    		int targetPO2H = mTexCache.getHeight();
    		if (ID_DST == which) {
    			startX = targetPO2W;
    			mTmpRect2.left += startX;
    			mTmpRect2.right += startX;
    		}
    		
    		mCaptureCanvas.setBitmap(mTexCache);
    		int saveID = mCaptureCanvas.save(); {
    			
    			// limit clip rect, don't draw to another side.
    			mCaptureCanvas.clipRect(startX, startY, 
    					startX + targetPO2W, startY + targetPO2H, 
    					Region.Op.REPLACE);
    			
    			// easer with background color.
    			mCaptureCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
    			
    			// fill drawable.
    			drawable.setBounds(mTmpRect2);
    			drawable.draw(mCaptureCanvas);
    			drawable.setBounds(mTmpRect1);
    			//K7GLUtils.debugDumpImage(mTexCache);
    			
    		} mCaptureCanvas.restoreToCount(saveID);
    		mCaptureCanvas.setBitmap(null);
    		
    		// update texture coords.
    		updateTexture(targetW, targetH);
    	}
		
		return true;
	}
	
	/** 
	 * 擦除图像颜色。</br>
	 * 注意，这个方法不会自动更新 GL 纹理坐标，调用者需要更新所有图像后，
	 * 自己手动调用 {@link #updateCacheTextureCoords(int, int)} 更新纹理坐标。
	 * 
	 * @param which {@link #ID_SRC} or {@link #ID_DST}
	 * @return True 擦除成功，false 擦除失败
	 */
	@Override
	public boolean eraseImage(int which) {
    	// if subclass don't need auto load texture, we keep the origin image.
    	if (!autoLoadTexture()) {
    		return super.eraseImage(which);
    	} else {
    		// auto load texture, we make the texture cache.
    		if (!K7GLUtils.checkImagePO2(mTexCache)) {
    			return false;
    		}
    		
    		int startX = 0;
    		int startY = 0;
    		// TODO:
    		int targetPO2W = (int) (mTexCache.getWidth() / 2f);
    		int targetPO2H = mTexCache.getHeight();
    		if (ID_DST == which) {
    			startX = targetPO2W;
    		}
    		
    		mCaptureCanvas.setBitmap(mTexCache);
    		final int saveID = mCaptureCanvas.save(); {
    			
    			// limit the clip rect(don't draw another side).
    			mCaptureCanvas.clipRect(startX, startY, 
    					startX + targetPO2W, startY + targetPO2H, 
    					Region.Op.REPLACE);
    			// use Canvas clear mode to clear old background.
    			mCaptureCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
    			
    		} mCaptureCanvas.restoreToCount(saveID);
    		mCaptureCanvas.setBitmap(null);
    		
    		return true;
    	}
	}
	
	/**
	 * GL context is ready(created). see {@link GLSurfaceView#onSurfaceCreated}.
	 * 
	 * @param gl
	 * @param config
	 */
    protected void onGLReady(GL10 gl, EGLConfig config) {
		// print GL info.
		LogUtils.d(NAME, K7GLUtils.getGLVendorInfo(gl));
    }
    
    /**
     * GL context is changed. see {@link GLSurfaceView#onSurfaceChanged}. </br>
     * Base class will set the default GL view port(view size) and view rect(-1.0 ~ 1.0).
     * 
     * @param gl
     * @param width
     * @param height
     */
    protected void onGLContextChanged(GL10 gl, int width, int height) {
		mViewPortWidth = width;
		mViewPortHeight = height;
        
		mViewRect.left = -1.0f;
		mViewRect.right = 1.0f;
		mViewRect.top = 1.0f;
		mViewRect.bottom = -1.0f;
		
        if (DEBUG) LogUtils.d(NAME, "onGLContextChanged mViewRect=" + mViewRect + ", this=" + mViewRect.hashCode()
        		+ ", viewPortW=" + mViewPortWidth + ", viewPortH=" + mViewPortHeight);
        
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        //gl.glOrthof(mViewRect.left, mViewRect.right, mViewRect.bottom, mViewRect.top, -1.0f, 1.0f);
        GLU.gluOrtho2D(gl, mViewRect.left, mViewRect.right, mViewRect.bottom, mViewRect.top);
        //GLU.gluLookAt(gl, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
	}
	
	/** 
	 * 交换源、目标特效图像。 </br>
	 * 硬件的纹理是在一起的，所以交互其实就是把纹理坐标交换一下。
	 */
	@Override
	public void swapImage() {
		if (!autoLoadTexture()) {
			super.swapImage();
		} else {
			PointF tmp1 = mSrcTexCoordStart;
			PointF tmp2 = mSrcTexCoordEnd;
			mSrcTexCoordStart = mDstTexCoordStart;
			mSrcTexCoordEnd = mDstTexCoordEnd;
			mDstTexCoordStart = tmp1; 
			mDstTexCoordEnd = tmp2;
			swapImageImpl();
		}
	}
	
	/**
	 * 硬件的 Effector <b>禁止</b>使用这个接口（抛异常）。
	 * 这个操作使用 {@link #ensureImage(View, View)} 这个接口来完成。
	 */
    @Override
	protected boolean ensureImage(View targetView, int which) {
    	// if subclass don't need auto load texture, we keep the origin image.
    	if (!autoLoadTexture()) {
    		return super.ensureImage(targetView, which);
    	} else {
    		// auto load texture, we forbid this methods.
    		throw new RuntimeException(NAME + ": ensureImage should use ensureImage(Bitmap, Bitmap) to do !!");
    	}
	}
	
	/**
	 * 创建特效图像。</br>
	 * 这里创建的是纹理的缓存图像，所以 which 这个参数设置 {@link Effector#ID_SRC} 还是 {@link Effector#ID_DST} 都是一样的。
	 */
	@Override
	public boolean buildImage(int width, int height, Bitmap.Config config, int which) {
    	// if subclass don't need auto load texture, we keep the origin image.
    	if (!autoLoadTexture()) {
    		return super.buildImage(width, height, config, which);
    	} else {
    		// auto load texture, we make the texture cache.
			//mTexCache = reCreateImage(mTexCache, mCacheSize.x, mCacheSize.y, config);
    		mTexCache = ensureImageSizeWithPO2(mTexCache, width, height, config);
			return K7GLUtils.checkImagePO2(mTexCache);
    	}
	}
	
	//@Override
	//protected Bitmap reCreateImage(Bitmap imgOrg, int width, int height, Bitmap.Config config) {
	//	return ensureImagePowerSize(imgOrg, width, height, config);
	//}
	
    /**
     * 查询特效器支持的最大图像大小（这个其实是 GL 纹理的大小限制）
     * 
     * @return 最大的图像大小
     */
    public final int queryMaxImageSize() {
    	return MAX_IMAGE_SIZE;
    }
    
    public boolean isNeedUpdateTexture() {
    	return mNeedUpdateTexture;
    }
    
    public void markUpdateTexture() {
    	mNeedUpdateTexture = true;
    }
    
    /**
     * 更新纹理（重新绑定），并且更新纹理坐标
     * 
     * @param newTexWidth 新纹理的宽
     * @param newTexHeight 新纹理的高
     * @param gl GL 实例
     * @return True 更新成功, false 更新失败
     */
    /*public boolean updateTexture(int newTexWidth, int newTexHeight, GL10 gl) {
    	boolean ret = updateTexture(gl);
    	if (ret) {
    		updateCacheTextureCoords(newTexWidth, newTexHeight);
    	}
    	return ret;
    }*/
    
    /**
     * 更新纹理，并且更新纹理坐标 </br>
     * 注意，这里只是 mark 纹理需要重新绑定，真正的绑定是在 GL 线程里面的
     * 
     * @param newTexWidth 新纹理的宽
     * @param newTexHeight 新纹理的高
     */
    public void updateTexture(int newTexWidth, int newTexHeight) {
    	markUpdateTexture();
    	updateCacheTextureCoords(newTexWidth, newTexHeight);
    }
    
    /**
     * 更新纹理（真正的绑定）
     * 
     * @param gl GL 实例
     * @return True 更新成功, false 更新失败
     */
    public final boolean updateTexture(GL10 gl) {
		if (!isGLReady()) {
			LogUtils.d(NAME, "updateTexture: GL is not ready we can't load texture !");
			return false;
		}
    	
    	if (!autoLoadTexture()) {
			boolean ret = updateTextureImpl(gl);
			if (ret) {
				mNeedUpdateTexture = false;
			}
			return ret;
    	} else {
			int retVal = GL_NO_ERROR;
			retVal = K7GLUtils.loadTexture(gl, mTexID, mTexCache);
			if (DEBUG) LogUtils.d(NAME, "updateTexture: GL ret val is: " + Integer.toHexString(retVal));
			if (GL_NO_ERROR == retVal) {
				mNeedUpdateTexture = false;
			}
			return GL_NO_ERROR == retVal ? true : false;
		}
    }
    
    /**
     * Check the texture whether valid
     * 
     * @return True: valid, false: inavlid
     */
    protected final boolean checkTextureValid() {
    	if (!autoLoadTexture()) {
    		return checkTextureValidImpl();
    	} else {    		
    		if (mTexID[0] <= K7GLUtils.INVALID_TEXTURE_ID) {
    			return false;
    		}
    		return true;
    	}
    }
	
	/**
	 * 更新缓冲纹理中真正使用到的图像的坐标（更新计算）。 </br>
	 * TODO: 这里只支持源和目标图像大小一样。
	 * 
	 * @param newTexWidth 新纹理的宽
	 * @param newTexHeight 新纹理的高
	 */
	public void updateCacheTextureCoords(int newTexWidth, int newTexHeight) {
		if (newTexWidth <=0 || newTexHeight <= 0 || 
				!checkImageValid(mTexCache)) {
			return;
		}
		
		int textureW = mTexCache.getWidth();
		//int textureH = mTexCache.getHeight();
		
		// TODO: now src is in left, dst is in right
		K7GLUtils.computeTextureCoords(mSrcTexCoordStart, mSrcTexCoordEnd, 
				mTexCache, 0, 0, newTexWidth, newTexHeight);
		K7GLUtils.computeTextureCoords(mDstTexCoordStart, mDstTexCoordEnd, 
				mTexCache, 0 + (int)(textureW / 2f), 0, newTexWidth, newTexHeight);
		
		// subclass implement.
		updateCacheTextureCoordsImpl();
	}
	
	/**
	 * 更新自己的坐标
	 */
	protected abstract void updateCacheTextureCoordsImpl();
	
	/**
	 * Tell hardware effector whether auto load texture to GL.
	 * The subclass can override this method to disable auto load texture(default is enable).
	 * May be the subclass have a another good way to use the texture, in this way hardware 
	 * effector only provider origin bitmap.
	 * 
	 * @return True: enable auto load, false: disable
	 */
	protected boolean autoLoadTexture() {
		return true;
	}
	
	/**
	 * When override {@link #autoLoadTexture()} to disable auto load texture,
	 * subclass should implement this method to load texture self.
	 * 
	 * @param gl GL context.
	 * @return True: load success, false: error.
	 */
	protected boolean updateTextureImpl(GL10 gl) {
		return true;
	}
	
	/**
	 * When override {@link #autoloadTexture()} to disable auto load texture,
	 * subclass should implement this method to check texture self.
	 * 
	 * @return True: texture valid, false: invalid.
	 */
	protected boolean checkTextureValidImpl() {
		return true;
	}
	
	/**
	 * Ensure image is matched target size and make sure is 2^n align, otherwise will re-create to align it. 
	 * <b>Notice: </b> if re-create, it will release the origin image.
	 * 
	 * @param imgOrg Origin image.
	 * @param width Target width.
	 * @param height Target height.
	 * @param config Bitmap config.
	 * @return After match align bitmap, or maybe is origin bitmap.
	 */
	protected Bitmap ensureImageSizeWithPO2(Bitmap imgOrg, int width, int height, Bitmap.Config config) {
		// below calculate 2^n size, if the size is 1, it will become 0, so we ignore this size 1.
		if (width <= 1 || height <= 1) {
			LogUtils.e(NAME, "target w or h is invalid, can't use hardware effector !");
			return imgOrg;
		}
		
		// if check image is align 2^n and size >= target, we don't change it.
		if (K7GLUtils.checkImagePO2(imgOrg)) {
			if (imgOrg.getWidth() >= width && imgOrg.getHeight() >= height && 
					imgOrg.getConfig() == config) {
				if (DEBUG) LogUtils.v(NAME, "the target image config is same as org and valid 2^n, we still use old one !");
				return imgOrg;
			}
		}
		
		// TODO: now don't care scale
		// reference android View method： 
		// Bitmap createSnapshot(Bitmap.Config quality, int backgroundColor, boolean skipChildren)
        //final AttachInfo attachInfo = mAttachInfo;
        //final float scale = attachInfo != null ? attachInfo.mApplicationScale : 1.0f;
        //width = (int) ((width * scale) + 0.5f);
        //height = (int) ((height * scale) + 0.5f);
		
		int PO2W = Integer.highestOneBit(width - 1) << 1;
		int PO2H = Integer.highestOneBit(height - 1) << 1;
		
		if (PO2W > MAX_TEXTURE_SIZE || PO2H > MAX_TEXTURE_SIZE) {
			LogUtils.e(NAME, "the power of 2 size: " + PO2W + ", " + PO2H + 
					" is large than limit bound: " + MAX_TEXTURE_SIZE);
			return imgOrg;
		}
		
		// create a new 2^n align image.
        Bitmap imgNew = BitmapUtils.createBitmap(PO2W, PO2H, config);
        if (!checkImageValid(imgNew)) {
        	return imgOrg;
        }
		// free origin image
		BitmapUtils.freeBitmap(imgOrg);
		return imgNew;
	}
	
	/**
	 * Save the target image to texture cache.
	 *  
	 * @param targetImg Target image
	 * @param which {@link Effector#ID_SRC} or {@link Effector#ID_DST}
	 * @return True success，false failed.
	 */
	protected boolean cacheImage(Bitmap targetImg, int which) {
		if (!checkImageValid(targetImg) || checkImageValid(mTexCache)) {
			return false;
		}
		
		int startX = 0;
		int startY = 0;
		// TODO: now we just put the cache horizontal.
		int boundW = mTexCache.getWidth() / 2;
		int boundH = mTexCache.getHeight();
		
		switch (which) {
		case ID_SRC:
			startX = 0;
			startY = 0;
			break;
			
		case ID_DST:
			startX = boundW;
			startY = 0;
			break;
		
		default:
			return false;
		}
		
		mCaptureCanvas.setBitmap(mTexCache);
		final int saveID = mCaptureCanvas.save(); {
			
			// limit clip rect, don't draw to another cache side.
			mCaptureCanvas.clipRect(startX, startY, 
					startX + boundW, startY + boundH, 
					Region.Op.REPLACE);
			// erase the background color.
			mCaptureCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
			mTmpRect1.set(startX, startY, 
					startX + targetImg.getWidth(), 
					startY + targetImg.getHeight());
			mCaptureCanvas.drawBitmap(targetImg, null, mTmpRect1, null);
			
		} mCaptureCanvas.restoreToCount(saveID);
		mCaptureCanvas.setBitmap(null);
		
		// update cache texture coords.
		updateTexture(targetImg.getWidth(), targetImg.getHeight());
		
		return true;
	}
    
    /**
     * 查询 GL 是否已经准备好
     * 
     * @return True: 已经准备好，false: 还没准备好
     */
    protected boolean isGLReady() {
    	if (null == mGLResponser) {
    		return false;
    	}
    	return mGLResponser.isGLReady();
    }
	
    private void cachePolicy(int srcW, int srcH) {
    	// TODO: now we just put the cache horizontal.
    	mCacheSize.x = srcW * 2;
    	mCacheSize.y = srcH;
    }
}
