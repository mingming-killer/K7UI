package com.eebbk.mingming.k7ui.effector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 
 * Canvas effector view.
 * 
 * @author humingming <hmm@dw.gdbbk.com>
 *
 */
public class CanvasEffectView extends View implements IEffectView {
	
	private final static String TAG = "EffectView";
	
	private View mSrcView = null;
	private View mDstView = null;
	private CanvasEffector mEffector = null;
	
	
	public CanvasEffectView(Context context) {
		this(context, null);
	}
	
	public CanvasEffectView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CanvasEffectView(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs, defStyle, null, null, null);
	}
	
	public CanvasEffectView(Context context, AttributeSet attrs, int defStyle, 
			View srcView, View dstView, Effector effector) {
		super(context, attrs, defStyle);
		init(srcView, dstView, effector);
	}
	
	private void init(View srcView, View dstView, Effector effector) {
		mSrcView = null;
		mDstView = null;
		setEffector(effector);
	}
	
	@Override	
	protected void onDraw(Canvas canvas) {		
		if (null == mEffector) {
			return;
		}
		
		mEffector.doEffect(canvas);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (null != mEffector) {
			mEffector.onSizeChanged(w, h, oldw, oldh);
		}
	}
	
	@Override
	public void free() {
		freeEffector();
	}
	
	@Override
	public void freeEffector() {
		if (null != mEffector) {
			mEffector.free();
			mEffector = null;
		}
	}
	
	@Override
	public void pauseEffect() {
		// do noting.
	}

	@Override
	public void resumeEffect() {
		// do noting.
	}
	
	@Override
	public void setEffector(Effector effector) {
		setEffector(effector, true);
	}
	
	@Override
	public void setEffector(Effector effector, boolean autoClear) {
		checkEffectorType(effector);
		
		if (null != mEffector) {
			if (mEffector.equals(effector)) {
				return;
			}
		}
		
		if (autoClear) {
			if (null != mEffector) {
				mEffector.free();
			}
		}
		
		mEffector = (CanvasEffector)effector;
		if (null != mEffector) {
			mEffector.onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
			if (!mEffector.supportHardAccelerated()) {
				setLayerType(LAYER_TYPE_SOFTWARE, null);
			} else {
				setLayerType(LAYER_TYPE_HARDWARE, null);
			}
		}
	}
	
	@Override
	public void setImage(Bitmap imgSrc, Bitmap imgDst) {
		if (null == mEffector) {
			return;
		}
		
		mEffector.setImage(imgSrc, imgDst);
	}
	
	@Override
	public void setImage(Bitmap image, int which) {
		if (null == mEffector) {
			return;
		}
		
		mEffector.setImage(image, which);
	}
	
	@Override
    public void setHighQuality(boolean high) {
		if (null == mEffector) {
			return;
		}
		
		mEffector.setHighQuality(high);
    	refresh();
    }
	
	@Override
	public void setBkColor(int color) {
		if (null == mEffector) {
			return;
		}
		
		mEffector.setBkColor(color);
    	refresh();
	}
	
    @Override
	public void setEffectFactor(float factor) {
		if (null == mEffector) {
			return;
		}
		
		// 确保变化图像有效
		mEffector.ensureImage(
				getTargetView(Effector.ID_SRC),
				getTargetView(Effector.ID_DST));
		
		// 改变特效器特效因子
		mEffector.setEffectFactor(factor);
		
		// 改变了特效因子，让 View 重画下
		invalidate();
	}
	
	@Override
	public void setEffectType(int type) {
		if (null == mEffector) {
			return;
		}
		
		//if (getEffectType() == type) {
		//	return;
		//}
		
		mEffector.setEffectType(type);
		refresh();
	}
	
	@Override
	public void setTargetView(View srcView, View dstView) {
		mSrcView = srcView;
		mDstView = dstView;
	}
	
	@Override
	public void setTargetView(View targetView, int which) {
		switch (which) {
		case Effector.ID_SRC:
			mSrcView = targetView;
			break;
			
		case Effector.ID_DST:
			mDstView = targetView;
			break;
		
		default:
			break;
		}
	}
	
	@Override
	public View getTargetView(int which) {
		switch (which) {
		case Effector.ID_SRC:
			return mSrcView;
			
		case Effector.ID_DST:
			return mDstView;
		
		default:
			return null;
		}
	}
	
	@Override
	public Effector getEffector() {
		return mEffector;
	}
	
	@Override
	public String getEffectorName() {
		if (null == mEffector) {
			return null;
		}
		
		return mEffector.getName();
	}
	
	@Override
	public float getEffectFactor() {
		if (null == mEffector) {
			return Effector.SRC_FACTOR;
		}
		
		return mEffector.getEffectFactor();
	}
	
	@Override
	public int getEffectType() {
		if (null == mEffector) {
			return Effector.HORIZONTAL;
		}
		
		return mEffector.getEffectType();
	}
	
    @Override
	public View getUseTargetView(int which) {
		if (isReverseEffect()) {
	    	switch (which) {
	    	case Effector.ID_SRC:
	    		return mDstView;
	    		
	    	case Effector.ID_DST:
	    		return mSrcView;
	    	
	    	default:
	    		return null;
	    	}
		} else {
			return getTargetView(which);
		}
	}
	
    @Override
	public boolean isHighQuality() {
		if (null == mEffector) {
			return false;
		}
		
		return mEffector.isHighQuality();
	}
	
	@Override
    public boolean isReverseEffect() {
		if (null == mEffector) {
			return false;
		}
		
		return mEffector.isReverseEffect();
    }
    
    @Override
    public boolean haveEffector() {
    	return !(null == mEffector);
    }
    
    @Override
	public boolean buildImage(int width, int height, Bitmap.Config config) {
		if (null == mEffector) {
			return false;
		}
		
		return mEffector.buildImage(width, height, config);
	}
	
	@Override
	public boolean buildImage(int width, int height, Bitmap.Config config, int which) {
		if (null == mEffector) {
			return false;
		}
		
		return mEffector.buildImage(width, height, config, which);
	}
	
	@Override
	public boolean captureImage(int which) {
		if (null == mEffector) {
			return false;
		}
		
		boolean ret = true;
		
		if (0 != (Effector.ID_SRC & which)) {
			ret &= mEffector.captureImage(
					getTargetView(Effector.ID_SRC), 
					Effector.ID_SRC);
		}
		
		if (0 != (Effector.ID_DST & which)) {
			ret &= mEffector.captureImage(
					getTargetView(Effector.ID_DST), 
					Effector.ID_DST);
		}
		
		return ret;
	}
	
	@Override
	public boolean captureImage(View srcView, View dstView) {
		if (null == mEffector) {
			return false;
		}
		
		return mEffector.captureImage(srcView, dstView);
	}
	
	@Override
	public boolean captureImage(View targetView, int which) {
		if (null == mEffector) {
			return false;
		}
		
		return mEffector.captureImage(targetView, which);
	}
	
	@Override
	public boolean fillImage(Drawable drawable, int which) {
		if (null == mEffector) {
			return false;
		}
		
		return mEffector.fillImage(drawable, which);
	}
	
	@Override
	public boolean eraseImage(int which) {
		if (null == mEffector) {
			return false;
		}
		
		return mEffector.eraseImage(which);
	}
	
	@Override
	public void swapTarget() {
		// 把目标 View 换一下
		View bmp = mSrcView;
		mSrcView = mDstView;
		mDstView = bmp;
		
		// 再去把图像也换一下
		if (null != mEffector) {
			mEffector.swapImage();
		}
	}
	
    @Override
    public void reverseEffect(boolean reverse) {
    	if (null == mEffector) {
    		return;
    	}
    	
    	//if (isReverseEffect() == reverse) {
    	//	return;
    	//}
    	
    	mEffector.reverseEffect(reverse);
        //refresh();
    }
    
    @Override
    public void refresh() {
    	setEffectFactor(getEffectFactor());
    }
    
    @Override
    public boolean isGL() {
    	if (null == mEffector) {
    		return false;
    	}
    	return mEffector.isGL();
    }
	
	/** 
	 * 检测给定 effector 的类型（必须为 {@link GLEffector}）
	 * 
	 * @param effector
	 */
	protected final void checkEffectorType(Effector effector) {
		if (null == effector || effector instanceof CanvasEffector) {
			return;
		}
		throw new RuntimeException(TAG + ": can only use software effector, effector is:" + effector);
	}
	
}
