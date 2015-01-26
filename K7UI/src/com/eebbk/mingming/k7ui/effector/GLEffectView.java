package com.eebbk.mingming.k7ui.effector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.eebbk.mingming.k7ui.R;
import com.eebbk.mingming.k7utils.LogUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;

/**
 * 
 * 硬件（OpenGL）实现的特效 View。 包含 {@link GLEffector}，专门用于显示特效的。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class GLEffectView extends GLSurfaceView 
	implements GLSurfaceView.Renderer, IEffectView, GLEffector.GLResponser {
	
	private final static String TAG = "GLEffectView";
	
	private final static boolean DEFAULT_NEED_TRAN = false;
	
	/** Flag of OpenGL is ready for access. */
	protected boolean mGLIsReady;
	
	private boolean mIsNeedTran = false;
	
	private View mSrcView = null;
	private View mDstView = null;
	private GLEffector mEffector = null;
	
	
	public GLEffectView(Context context) {
		this(context, null);
	}
	
	public GLEffectView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public GLEffectView(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs, defStyle, null, null, null);
	}
	
	public GLEffectView(Context context, AttributeSet attrs, int defStyle, 
			View srcView, View dstView, Effector effector) {
		super(context, attrs);
		
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EffectView, defStyle, 0);
        mIsNeedTran = a.getBoolean(R.styleable.EffectView_k7uievNeedTran, DEFAULT_NEED_TRAN);
        a.recycle();
		
		init(srcView, dstView, effector);
	}
	
	private void init(View srcView, View dstView, Effector effector) {
		mSrcView = null;
		mDstView = null;
		
		mGLIsReady = false;
		setEffector(effector);
		
		// notice: GLSurfaceView translucent can't change after setRenderer.
		if (mIsNeedTran) {
			needTranslucent();
		}
		
		setRenderer(this);
		
		// set render mode to RENDERMODE_WHEN_DIRTY.
		// this mode don't update continuously.
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}
	
	private void needTranslucent() {
		setZOrderOnTop(true);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		LogUtils.d(TAG, "onPause()");
		
		// when activity is pause the openGL is not ready.
		mGLIsReady = false;
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// below is just the default GL setting, your can custom it in effector onGLReady method
		LogUtils.d(TAG, "onSurfaceCreated()");
		mGLIsReady = true;
		
        // by default, OpenGL enables features that improve quality
        // but reduce performance. One might want to tweak that
        // especially on software renderer.
    	gl.glDisable(GL10.GL_DITHER);
    	
        // color and texture coordinate we use the best render quality.
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        
        // initialize the screen color to white.
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // enable cull face, it can fast render in 2D texture mode.
        // if you are render 3D object, you may disable it.
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        
        // real implement effector custom method
		if (null != mEffector) {
			mEffector.onGLReady(gl, config);
		}
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		LogUtils.d(TAG, "onSurfaceChanged() w=" + width + ", h=" + height);
		
		// we set the default view-port
		// set OpenGL view-port.
    	gl.glViewport(0, 0, width, height);
    	
        // set our projection matrix. this doesn't have to be done
        // each time we draw, but usually a new projection needs to
        // be set when the view-port is resized.
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        
        // set planar projection mode, for more convenient 2D render.
        // this coordinate system is: 
        // 
        //        (-1.0, 1.0, 1.0)                           (1.0, 1.0, 1.0)
        //                          ------------------------ 
        //                       / |                       /|
        //                      /  |                      / |
        //                     /   |                     /  |
        //                    /    |                    /   |
        // (-1.0, 1.0, -1.0) /     |  (1.0, 1.0, -1.0) /    |
        //                   -------------------------      | 
        //                  |      |                  |     |
        //                  |      |                  |     |
        //                  |      |                  |     |
        //                  |      |(-1.0, -1.0, 1.0) |     |
        //                  |      -------------------|---- /  (1.0, -1.0, 1.0)
        //                  |     /                   |    /
        //                  |    /                    |   /
        //                  |   /                     |  /
        //                  |  /                      | /
        //                  | /                       |/
        //                   -------------------------
        // (-1.0, -1.0, -1.0)                         (1.0, -1.0, -1.0)
        //
        gl.glOrthof(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
		
		if (null != mEffector) {
			mEffector.onGLContextChanged(gl, width, height);
			// original thread that created a view hierarchy can touch its views, 
			// so can't update source image from GL thread. update this outside, the user
			// should update it when GL context is changed. eg, the GL view size has changed.
			// if the GL context is changed, we ensure the image, maybe need re-build it.
			//mEffector.ensureImage(mSrcView, mDstView);
			// and then re-render the image.
			//invalidate();
		}
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		if (null != mEffector) {
			mEffector.doEffect(gl);
		}
	}
	
	@Override
	public void invalidate() {
		// the GLSufaceView must call request render to refresh 
		requestRender();
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
		onPause();
	}

	@Override
	public void resumeEffect() {
		onResume();
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
		
		mEffector = (GLEffector)effector;
		if (null != mEffector) {
			mEffector.setGLResponser(this);
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
    	return (null != mEffector);
    }
    
    @Override
	public boolean buildImage(int width, int height, Bitmap.Config config) {
    	return buildImage(width, height, config, Effector.ID_SRC | Effector.ID_DST);
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
    
	@Override
	public boolean isGLReady() {
		return mGLIsReady;
	}
	
    /**
     * 查询 GL 是否已经准备好
     * 
     * @return True: 已经准备好, false: 还没准备好
     */
    public boolean queryGLIsReady() {
    	return mGLIsReady;
    }
    
	/** 
	 * 检测给定 effector 的类型（必须为 {@link GLEffector}）
	 * 
	 * @param effector
	 */
	protected final void checkEffectorType(Effector effector) {
		if (null == effector || effector instanceof GLEffector) {
			return;
		}
		throw new RuntimeException(TAG + ": can only use hardware effector, effector is:" + effector);
	}
	
}
