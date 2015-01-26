package com.eebbk.mingming.k7ui.effector;

import javax.microedition.khronos.opengles.GL10;

import com.eebbk.mingming.k7utils.BitmapUtils;
import com.eebbk.mingming.k7utils.LogUtils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 
 * 特效器抽象类。 </br>
 * 所有本类子类的特效，全部基于 {@link Bitmap} 图像变化。
 * 那些能用 Android 自带的属性能够完成的效果请用 Android 自带的属性。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public abstract class Effector {
	
	private final static String NAME = "Effector";
	
	public static boolean DEBUG = false;
	
	public final static String PROPERTY_NAME = "EffectFactor";
	
	/** 源标志 */
	public final static int ID_SRC = 0x01;
	
	/** 目标标志 */
	public final static int ID_DST = 0x02;
	
	
	/** 特效类型：水平方向  */
	public final static int HORIZONTAL = 0;
	
	/** 特效类型：竖直方向 */
	public final static int VERTICAL = 1;
	
	
	/** 源特效因子（特效起始图像） */
	public final static float SRC_FACTOR = 0.0f;
	
	/** 目标特效因子（特效目标图像） */
	public final static float DST_FACTOR = 1.0f;
	
	
	/** 截图的背景颜色，用全透明的去刷 */
	protected final static int CAPTURE_IMAGE_BK_COLOR = 0x00000000;
	
	/** 目前使用的图像颜色格式 */
	protected final static Bitmap.Config IMAGE_FORMAT = Bitmap.Config.ARGB_8888;
	
	
	/** 特效器的名字 */
	protected String mName;
	
	/** 变化的源图像 */
	protected Bitmap mImgSrc = null;
	
	/** 变化到的目标图像 */
	protected Bitmap mImgDst = null;
	
	/** 
	 * 特效因子：范围 [{@link #SRC_FACTOR} - {@link #DST_FACTOR}] </br>
	 * 这个值表示当前特效所处的特效变化值。{@link #SRC_FACTOR} 表示源图像，{@link #DST_FACTOR}表示目标图像，中间的值是变化过程。 </br>
	 * 例如：一个翻转特效，如果这值是 0.5 则表示正好翻到一半。
	 * 具体的含义由每个子类的实现决定。 </br></br>
	 * 
	 * 保存的这个是值是正常的数值，不受 {@link #mReverse} 的影响。对外提供也是正常值。
	 * 但是内部用这个值去计算特效图像的时候，需要考虑 {@link #mReverse}，因此内部用来
	 * 计算特效图像的时候，使用 {@link #computeDoEffectFactor()} 来获取该数值。
	 */
	protected float mEffectFactor;
	
	/**
	 * Effect type default is {@link #HORIZONTAL}, {@link #VERTICAL}. </br>
	 * 
	 * This is implement by subclass, maybe subclass define custom effect type.
	 */
	protected int mEffectType;
	
	/** 
	 * 特效画质标志： </br>
	 * True：高画质，false：低画质 。</br>
	 * 高低画质的具体表现又子类具体实现决定。
	 */
	protected boolean mHighQuality;
	
	/**
     * 反转特效标志。 </br>
     * True：反转特效，false：正常。</br>
     * 例如：如果一个特效是从右边滑动到左边，那么反转后，就是从左边滑动到右边。 
     */
	protected boolean mReverse;
	
	protected int mBkColor = 0xffffffff;
	protected Canvas mCaptureCanvas = null;
	
	private Rect mTmpRect = null;
	
	
	/**
	 * see {@link #Effector(Bitmap, Bitmap, boolean, boolean, int)} </br>
	 * 默认参数： null, null. true, false, HORIZONTAL
	 */
	public Effector() {
		this(null, null, true, false, HORIZONTAL);
	}
	
	/**
	 * 创建一个特效器
	 * 
	 * @param imgSrc 源图像
	 * @param imgDst 目标图像
	 * @param highQuality True 高画质，false 低画质
	 * @param reverse True 反转特效，false 正常
	 * @param type {@link #HORIZONTAL}, {@link #VERTICAL}
	 */
	public Effector(Bitmap imgSrc, Bitmap imgDst, 
			boolean highQuality, boolean reverse, int type) {
		mName = NAME;
		
		mImgSrc = imgSrc;
		mImgDst = imgDst;
		
		mHighQuality = highQuality;
		mReverse = reverse;
		mEffectType = type;
		
		mEffectFactor = SRC_FACTOR;
		mCaptureCanvas = new Canvas();
		mTmpRect = new Rect();
	}
	
	@Override
	public String toString() {
		return "Effector: " + mName + ", imgSrc: " + mImgSrc + ", imgDst: " + mImgDst 
				+ ", factor: " + mEffectFactor + ", type: " + mEffectType
				+ ", quality: " + mHighQuality + ", reverse" + mReverse;
	}
	
	/**
	 * 释放特效器资源
	 */
	public void free() {
		freeImage(ID_SRC | ID_DST);
		freeImpl();
	}
	
	/**
	 * 释放用于特效的图像
	 * 
	 * @param which {@link #ID_SRC}, {@link #ID_DST} 的组合
	 */
	public void freeImage(int which) {
		if (0 != (ID_SRC & which)) {
			BitmapUtils.freeBitmap(mImgSrc);
			mImgSrc = null;
		} 
		
		if (0 != (ID_DST & which)) {
			BitmapUtils.freeBitmap(mImgDst);
			mImgDst = null;
		}
	}
	
	/**
	 * Set effector source image.
	 * 
	 * @param imgSrc Source image
	 * @param imgDst Target image
	 */
	public void setImage(Bitmap imgSrc, Bitmap imgDst) {
		setImage(imgSrc, ID_SRC);
		setImage(imgDst, ID_DST);
	}
	
	/**
	 * Set image to effector. <b>Notice:</b> the effector don't use 
	 * that image directly, it's has own cache, so remember to release
	 * that image object.
	 * 
	 * @param image {@link Bitmap} Object
	 * @param which {@link #ID_SRC} or {@link #ID_DST}
	 */
	public void setImage(Bitmap image, int which) {
		switch (which) {
		case ID_SRC:
		case ID_DST:
			break;
		
		default:
			return;
		}
		
		if (!checkImageValid(image) || image.getWidth() <= 0 || 
				image.getHeight() <= 0) {
			return;
		}
		
		Bitmap target = getImage(which);
		target = reCreateImage(target, image.getWidth(), image.getHeight(), image.getConfig());		
		copyImage(image, target);
	}
	
	/**
	 * 设置特效因子。
	 * 
	 * @param factor 特效因子，范围 [{@link #SRC_FACTOR} - {@link #DST_FACTOR}]
	 */
	public void setEffectFactor(float factor) {
		mEffectFactor = factor;
		if (mEffectFactor < SRC_FACTOR) {
			mEffectFactor = SRC_FACTOR;
		} else if (mEffectFactor > DST_FACTOR) {
			mEffectFactor = DST_FACTOR;
		}
		
		setEffectFactorImpl(getUseEffectFactor());
	}
	
	/**
	 * 设置特效类型
	 * 
	 * @param type {@link #HORIZONTAL}, {@link #VERTICAL}
	 */
	public void setEffectType(int type) {
		mEffectType = type;
		setEffectTypeImpl(mEffectType);
	}
	
    /**
     * 设置特效画质。</br>
     * 子类要自己实现这个类的具体表现。
     * 
     * @param high True 高画质，false 低画质
     */
    public void setHighQuality(boolean high) {
    	mHighQuality = high;
    	setHighQualityImpl(mHighQuality);
    }
    
    /**
     * Set a background color to effector, this is use
     * for no support translucent background.
     * 
     * @param color ARGB.
     */
    public void setBkColor(int color) {
    	mBkColor = color;
    }
    
    /**
     * 获取特效器图像。
     * 
     * @param which {@link #ID_SRC} or {@link #ID_DST}
     * @return 图像, null 表示图像没有设置，或是参数错误。
     */
    public Bitmap getImage(int which) {
    	switch (which) {
    	case ID_SRC:
    		return mImgSrc;
    		
    	case ID_DST:
    		return mImgDst;
    	
    	default:
    		return null;
    	}
    }
    
    /**
     * 获取特效器名字
     * 
     * @return 特效器名字
     */
    public final String getName() {
    	return mName;
    }
    
    /**
     * 获取特效因子
     * 
     * @return 当前特效因子
     */
    public final float getEffectFactor() {
    	return mEffectFactor;
    }
    
    /**
     * 获取当前特效类型
     * 
     * @return {@link #HORIZONTAL}, {@link #VERTICAL}
     */
    public final int getEffectType() {
    	return mEffectType;
    }
    
    /**
     * Get background color.
     * 
     * @return ARGB backgrond color.
     */
    public final int getBkColor() {
    	return mBkColor;
    }
    
    /**
     * 获取特效器当前是否开启高画质
     * 
     * @return True 高画质，false 低画质
     */
    public final boolean isHighQuality() {
    	return mHighQuality;
    }
    
    /**
     * 获取特效器当前是否处于反转状态。
     * 
     * @return True 反转特效，false 正常特效
     */
    public final boolean isReverseEffect() {
    	return mReverse;
    }
    
    /**
     * 确保变化图像是有效的。 </br>
     * 这个方法会检查源图像和目标图像 {@link Bitmap} 对象是否有效（不为空，不被回收，大小和给定 View 一样大）。
     * 如果无效的话，就会根据给定 View 大小重新创建，并且重新截取 View 当前图像。
     * 
     * @param srcView 变化源 View
     * @param dstView 变化目标 View
     * @return True 确保图像是有效的，false 图像无效（有可能是创建失败了，有可能截图失败了）。
     */
    public boolean ensureImage(View srcView, View dstView) {
    	boolean ret = true;
    	ret &= ensureImage(srcView, ID_SRC);
    	ret &= ensureImage(dstView, ID_DST);
    	return ret;
    }
	
    /**
     * 截取变化 View 图像。 </br>
     * see {@link #captureImage(View, int)}
     * 
     * @param srcView 变化源 View
     * @param dstView 变化目标 View
     * @return True 截取成功，false 截取失败
     */
	public boolean captureImage(View srcView, View dstView) {
		boolean ret = true;
		ret &= captureImage(srcView, ID_SRC);
		ret &= captureImage(dstView, ID_DST);
		return ret;
	}
	
	/**
	 * Capture the image.
	 * 
	 * @param targetView Target view. This must be layouted, there means width and height is non-zero, 
	 * otherwise is can't capture. In fact, it's let the view draw on our canvas, if view size is zero, 
	 * it can't draw.
	 * @param which {@link #ID_SRC} or {@link #ID_DST}
	 * @return True: success，false: failed.
	 */
	public boolean captureImage(View targetView, int which) {
		switch (which) {
		case ID_SRC:
		case ID_DST:
			break;
		
		default:
			return false;
		}
		
		if (null == targetView) {
			LogUtils.d(NAME, "catpure image failed: target view is null !");
			return false;
		}
		
		int width = targetView.getWidth();
		int height = targetView.getHeight();
		
		if (DEBUG) LogUtils.d(NAME, String.format("targetView: (%d, %d), vis: %d", 
				width, height, targetView.getVisibility()));
		
		if (width <= 0 || height <= 0) {
			LogUtils.d(NAME, "catpure image failed: target view don't have a valid size !");
			return false;
		}
		
		Bitmap image = getImage(which);
		
		if (!checkImage(which)) {
			if (!buildImage(width, height, IMAGE_FORMAT, which)) {
				LogUtils.d(NAME, "catpure image failed: build image failed !");
				return false;
			}
			image = getImage(which);
		}
		
		// if the old image is not matched the target view, we re-create it.
		if (image.getWidth() != width || image.getHeight() != height) {
			if (!buildImage(width, height, IMAGE_FORMAT, which)) {
				LogUtils.d(NAME, "catpure image failed: build image failed !");
				return false;
			}
			image = getImage(which);
		}
		
		mCaptureCanvas.setBitmap(image);
		final int saveID = mCaptureCanvas.save(); 
		{
			// set clip rect
			mCaptureCanvas.clipRect(0, 0, 
					targetView.getWidth(), targetView.getHeight(), 
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
			mCaptureCanvas.translate(-scrollX, -scrollY);
			targetView.draw(mCaptureCanvas);
		} 
		mCaptureCanvas.restoreToCount(saveID);
		mCaptureCanvas.setBitmap(null);
		
		return true;
	}
	
	/**
	 * 填充特效图像。 </br>
	 * 一般用于变化的只有一个 View 的情况下（例如说换页切换到尽头了）。</br>
     * see {@link #fillImage(Drawable, int)}
	 * 
	 * @param drSrc 源图像，外部设置好 Bounds。
	 * @param drDst 目标图像，外部设置好 Bounds。
	 * @return True 填充成功, false 填充失败
	 */
	//public boolean fillImage(Drawable drSrc, Drawable drDst) {
	//	boolean ret = true;
	//	ret &= fillImage(drSrc, ID_SRC);
	//	ret &= fillImage(drDst, ID_DST);
	//	return ret;
	//}
	
	/**
	 * 填充特效图像。 </br>
	 * 一般用于变化的只有一个 View 的情况下（例如说换页切换到尽头了）。
	 * 
	 * @param drawable 填充的 {@link Drawable}，外部设置好 Bounds。
	 * @param which {@link ID_SRC} or {@link ID_DST}
	 * @return True 填充成功，false 填充失败
	 */
	public boolean fillImage(Drawable drawable, int which) {
		if (!checkImage(which) || null == drawable) {
			return false;
		}
		
		Rect drRect = drawable.getBounds();
		Bitmap image = getImage(which);
		
		mCaptureCanvas.setBitmap(image);
		final int saveID = mCaptureCanvas.save(); 
		{
			mCaptureCanvas.clipRect(0, 0, 
					drRect.width(), drRect.height(), 
					Region.Op.REPLACE);
			//mCaptureCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR);
			mCaptureCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
			drawable.draw(mCaptureCanvas);
		} 
		mCaptureCanvas.save(saveID);
		mCaptureCanvas.setBitmap(null);
		
		return true;
	}
	
	/** 
	 * 擦除图像颜色
	 * 
	 * @param which {@link #ID_SRC} or {@link #ID_DST}
	 * @return True 擦除成功，false 擦除失败
	 */
	public boolean eraseImage(int which) {
		if (!checkImage(which)) {
			return false;
		}
		
		Bitmap image = getImage(which);
		if (checkImageValid(image)) {
			return false;
		}
		
		image.eraseColor(CAPTURE_IMAGE_BK_COLOR);
		return true;
	}
	
	/**
	 * 做特效（软件实现版本）。 </br>
	 * see {@link #doEffect(Canvas, float)}.
	 * 
	 * @param output
	 * @return
	 */
	public boolean doEffect(Canvas output) {
		return doEffect(output, mEffectFactor);
	}
	
	/**
	 * 做特效（硬件实现版本）。 </br>
	 * see {@link #doEffect(GL10, float)}.
	 * 
	 * @param output
	 * @return
	 */
	public boolean doEffect(GL10 output) {
		return doEffect(output, mEffectFactor);
	}
	
	/**
	 * 做特效（软件实现版本）。</br>
	 * 具体的效果由子类实现。
	 * 
	 * @param output 输出的画面 {@link Canvas}。
	 * @param doEffectFactor 特效因子（受 {@link #mReverse} 影响）
	 * @return True 输出成功，false 输出失败。
	 */
	public boolean doEffect(Canvas output, float doEffectFactor) {
		setEffectFactor(doEffectFactor);
		
		if (null == output) {
			LogUtils.d(NAME, "do effect failed: output canvas is null !");
			return false;
		}
		return doEffectImpl(output, getUseEffectFactor());
	}
	
	/**
	 * 实现自己的做特效功能（硬件版本）
	 * 
	 * @param output 输出的 {@link GL10}
	 * @param doEffectFactor 特效因子（受 {@link #mReverse} 影响）
	 * @return True 输出成功，false 输出失败
	 */
	public boolean doEffect(GL10 output, float doEffectFactor) {
		setEffectFactor(doEffectFactor);
		
		if (null == output) {
			LogUtils.d(NAME, "do effect failed: output canvas is null !");
			return false;
		}
		return doEffectImpl(output, getUseEffectFactor());
	}
	
	/**
	 * 实现自己的做特效功能（软件版本）
	 * 
	 * @param output 输出的 {@link Canvas}
	 * @param doEffectFactor 特效因子（受 {@link #mReverse} 影响）
	 * @return True 输出成功，false 输出失败
	 */
	protected boolean doEffectImpl(Canvas output, float doEffectFactor) {
		return false;
	}
	
	/**
	 * 实现自己的做特效功能（硬件版本）
	 * 
	 * @param output 输出的 GL context
	 * @param doEffectFactor 特效因子（受 {@link #mReverse} 影响）
	 * @return True 输出成功，false 输出失败
	 */
	protected boolean doEffectImpl(GL10 output, float doEffectFactor) {
		return false;
	}
	
	/** 
	 * 交换源、目标特效图像
	 */
	public void swapImage() {
		Bitmap bmp = mImgSrc;
		mImgSrc = mImgDst;
		mImgDst = bmp;
		
		swapImageImpl();
	}
	
    /**
     * 反转特效。 </br>
     * 例如：如果一个特效是从右边滑动到左边，那么反转后，就是从左边滑动到右边。
     * 
     * @param reverse True 翻转，false 正常
     */
    public void reverseEffect(boolean reverse) {
    	mReverse = reverse;    	
    	reverseEffectImpl(reverse);
    }
	
	/**
	 * 检测特效图像是否有效
	 * 
	 * @param which {@link #ID_SRC}, {@link #ID_DST} 的组合
	 * @return True 有效，false 无效
	 */
	protected boolean checkImage(int which) {
		boolean ret = true;
		Bitmap image = null;
		
		if (0 != (ID_SRC & which)) {
			image = getImage(ID_SRC);
			ret &= checkImageValid(image);
		}
		
		if (0 != (ID_DST & which)) {
			image = getImage(ID_DST);
			ret &= checkImageValid(image);
		}
		
		return ret;
	}
	
	/**
	 * 检查给定图片是否有效（非空，不被回收）。
	 * 
	 * @param image Object of {@link Bitmap}
	 * @return True 有效，false 无效
	 */
	protected final boolean checkImageValid(Bitmap image) {
		if (null != image && !image.isRecycled()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Check give two image size(config) whether mathced. 
	 * 
	 * @param src Source image.
	 * @param dst Target image
	 * @return True: matched, false don't matched.
	 */
	protected final boolean checkImageSizeMatched(Bitmap src, Bitmap dst) {
		if (checkImageValid(src) || checkImageValid(dst)) {
			return false;
		}
		if (src.getWidth() == dst.getWidth() && src.getHeight() == dst.getHeight() && 
				src.getConfig() == dst.getConfig()) {
			return true;
		} else {
			return false;
		}
	}
	
    /**
     * 确保变化图像是有效的。 </br>
     * 这个方法会检查源图像和目标图像 {@link Bitmap} 对象是否有效（不为空，不被回收，大小和给定 View 一样大）。
     * 如果无效的话，就会根据给定 View 大小重新创建，并且重新截取 View 当前图像。
     * 
     * @param targetView 变化 View
     * @param which {@link #ID_SRC} or {@link #ID_DST}
     * @return True 确保图像是有效的，false 图像无效（有可能是创建失败了，有可能截图失败了）。
     */
	protected boolean ensureImage(View targetView, int which) {		
	    Bitmap image = getImage(which);
	    
	    if (null == targetView) {
	    	if (null != image && !image.isRecycled()) {
	    		return true;
	    	} else {
	    		return false;
	    	}
	    }
	    
	    if (null != image && !image.isRecycled() && 
	    		image.getWidth() == targetView.getWidth() && 
	    		image.getHeight() == targetView.getHeight()) {
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
	
	/**
	 * 创建特效图像。
	 * see {@link #buildFlipImage(int, int, android.graphics.Bitmap.Config, boolean)}
	 * 
	 * @param width
	 * @param height 
	 * @param config 
	 */
	public boolean buildImage(int width, int height, Bitmap.Config config) {
		return buildImage(width, height, config, ID_SRC | ID_DST);
	}
	
	/** 
	 * 创建特效图像。
	 * 
	 * @param width 图片宽
	 * @param height 图片高
	 * @param config 图片配置, see {@link Bitmap.Config}
	 * @param which {@link #ID_SRC}, {@link #ID_DST} 的组合
	 */
	public boolean buildImage(int width, int height, Bitmap.Config config, int which) {
		boolean ret = true;
		if (0 != (ID_SRC & which)) {
			mImgSrc = reCreateImage(mImgSrc, width, height, config);
			ret &= checkImageValid(mImgSrc);
		}
		
		if (0 != (ID_DST & which)) {
			mImgDst = reCreateImage(mImgDst, width, height, config);
			ret &= checkImageValid(mImgDst);
		}
		
		return ret;
	}
	
	/**
	 * copy a image pixel from source bitmap to dst bitmap.
	 * 
	 * @param src
	 * @param dst
	 */
	protected void copyImage(Bitmap src, Bitmap dst) {
		if (!checkImageValid(src) || !checkImageValid(dst)) {
			return;
		}
		
		mCaptureCanvas.setBitmap(dst);
		final int saveID = mCaptureCanvas.save(); 
		{
			mCaptureCanvas.clipRect(0, 0, 
					dst.getWidth(), dst.getHeight(), 
					Region.Op.REPLACE);
			mCaptureCanvas.drawColor(CAPTURE_IMAGE_BK_COLOR, PorterDuff.Mode.CLEAR);
			mTmpRect.set(0, 0, src.getWidth(), src.getHeight());
			mCaptureCanvas.drawBitmap(src, null, mTmpRect, null);
		} 
		mCaptureCanvas.restoreToCount(saveID);
		mCaptureCanvas.setBitmap(null);
	}
	
	/**
	 * re-create image with give size and config, if is matched the params, it will return the old one.
	 * 
	 * @param imgOrg Origin image.
	 * @param width New image width.
	 * @param height New image height.
	 * @param config New image config.
	 * @return Matched image(if the old is matched, it's the old object), if is null maybe re-create failed.
	 */
	protected Bitmap reCreateImage(Bitmap imgOrg, int width, int height, Bitmap.Config config) {
		if (width <= 0 || height <= 0) {
			return imgOrg;
		}
		
		if (checkImageValid(imgOrg) &&
				imgOrg.getWidth() == width && imgOrg.getHeight() == height &&
				imgOrg.getConfig() == config) {
			if (DEBUG) LogUtils.d(NAME, "the target image config is same as org, we still use old one !");
			return imgOrg;
		}
		
		// TODO: now don't care view scale.
		// reference android View methods: 
		// Bitmap createSnapshot(Bitmap.Config quality, int backgroundColor, boolean skipChildren)
        //final AttachInfo attachInfo = mAttachInfo;
        //final float scale = attachInfo != null ? attachInfo.mApplicationScale : 1.0f;
        //width = (int) ((width * scale) + 0.5f);
        //height = (int) ((height * scale) + 0.5f);
        Bitmap image = BitmapUtils.createBitmap(width, height, config);
        if (!checkImageValid(image)) {
        	return null;
        }
        
        //Resources res = getResources();
        //if (null != res) {
        //	image.setDensity(res.getDisplayMetrics().densityDpi);
        //}
        
		// replace old image with new one.
		BitmapUtils.freeBitmap(imgOrg);
		imgOrg = image;
		
		return imgOrg;
	}
	
	/**
	 * Get real use effect factor(affect by {@link #mReverse}).
	 * Because reverse effect, so don't reference {@link #mEffectFactor} directly.
	 * You should use this methods to get it.
	 * 
	 * @return real effect factor.
	 */
	protected final float getUseEffectFactor() {
		if (mReverse) {
			return (DST_FACTOR - mEffectFactor);
		} else {
			return mEffectFactor;
		}
	}
	
	/**
	 * Get real use image(affect by {@link #mReverse}).
	 * Because reverse effect, so don't reference image directly.
	 * You should use this methods to get it.
	 * 
	 * @return real effect image.
	 */
	/*protected Bitmap getUseImage(int which) {
		if (mReverse) {
	    	switch (which) {
	    	case ID_SRC:
	    		return mImgDst;
	    		
	    	case ID_DST:
	    		return mImgSrc;
	    	
	    	default:
	    		return null;
	    	}
		} else {
			return getImage(which);
		}
	}*/
	
	/**
	 * Tell us whether this effector is implement by OpenGL.
	 * 
	 * @return True: implement by OpenGL, false: implement by canvas.
	 */
	public abstract boolean isGL();
	
	/**
	 * Tell effect view whether this effector support transparent background.
	 * If support, means the effect view should provider a translucent context. 
	 * This is very useful for hardware effect view(GLSurfaceView) to set translucent context.
	 * 
	 * @return True support, false don't support.
	 */
	public abstract boolean supportTranBk();
	
	/**
	 * Subclass have a chance to implement self set effect factor.
	 * 
	 * @param doEffectFactor Effect factor(affect by {@link #mReverse})
	 */
	protected abstract void setEffectFactorImpl(float doEffectFactor);
	
	/**
	 * Subclass have a chance to implement self set effect type.
	 * 
	 * @param type Effect type.
	 */
	protected abstract void setEffectTypeImpl(int type);
	
	/**
	 * Subclass have a chance to implement self high quality.
	 * 
	 * @param high True: high quality, false: low
	 */
	protected abstract void setHighQualityImpl(boolean high);
	
	/**
	 * Subclass have a chance to implement self image swap.
	 */
	protected abstract boolean swapImageImpl();
	
	/**
	 * Subclass have a chance to implement effect reverse.
	 * 
	 * @param reverse True: reverse effect, false: normal.
	 */
	protected abstract void reverseEffectImpl(boolean reverse);
	
	/**
	 * Subclass have a chance to implement release self resource.
	 */
	protected abstract void freeImpl();
	
}
