package com.eebbk.mingming.k7ui.effector;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * 
 * 软件特效器抽象类。 </br>
 * 基于软件实现的特效器抽象类。（see {@link Effector}）
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 * 
 */
public abstract class CanvasEffector extends Effector {
	
	private final static String NAME = "CanvasEffector";
	
	protected Rect mViewRect = null; 
	
	/**
	 * see {@link #Effector(Bitmap, Bitmap, boolean, boolean, int)} </br>
	 * 默认参数： null, null. true, false, HORIZONTAL
	 */
	public CanvasEffector() {
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
	public CanvasEffector(Bitmap imgSrc, Bitmap imgDst, 
			boolean highQuality, boolean reverse, int type) {
		super(imgSrc, imgDst, highQuality, reverse, type);
		init();
	}
	
	private void init() {
		mViewRect = new Rect();
	}
	
	@Override
	public boolean doEffect(GL10 output) {
		throw new RuntimeException(NAME + 
				": software effector should nerver call this method, it's for hardware effector !");
	}
	
	@Override
	protected boolean doEffectImpl(GL10 output, float doEffectFactor) {
		throw new RuntimeException(NAME + 
				": software effector should nerver call this method, it's for hardware effector !");
	}
	
	@Override
	public boolean isGL() {
		return false;
	}
	
	@Override
	public boolean supportTranBk() {
		return true;
	}
	
	/**
	 * To tell effect view this effector whether hard accelerated.
	 * See android docs to know which graphic api don't support hard accelerated. 
	 * If you return true the effect view will turn-on the hard accelerated, false the 
	 * effect view will use software render mode.
	 * 
	 * @return True: supported, false: don't supported. 
	 */
	public abstract boolean supportHardAccelerated();
	
    /**
     * The canvas effect view size is changed, so the effector should detail
     * it's draw with the new size.
     * 
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		mViewRect.set(0, 0, w, h);
    }
	
}
