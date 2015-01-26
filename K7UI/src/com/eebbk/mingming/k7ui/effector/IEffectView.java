package com.eebbk.mingming.k7ui.effector;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 
 * 特效 View 接口。
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public interface IEffectView {
	
	/** 特效变化属性名字 */
	public final static String PROPERTY_NAME = "EffectFactor";
	
	/**
	 * 释放资源
	 */
	public void free();
	
	/**
	 * 释放特效器资源
	 */
	public void freeEffector();
	
	public void pauseEffect();

	public void resumeEffect();
	
	/**
	 * 设置特效器。 </br>
	 * see {@link #setEffector(Effector, boolean)} (effector, true)
	 * 
	 * @param effector
	 */
	public void setEffector(Effector effector);
	
	/**
	 * 设置特效器。 </br>
	 * 
	 * @param effector Object of {@link Effector}
	 * @param autoClear True 清理之前设置的特效器，false 不清理原来的（自己手动清理）
	 */
	public void setEffector(Effector effector, boolean autoClear);
	
	/**
	 * 设置特效图像源。 </br>
	 * see {@link Effector#setImage(Bitmap, Bitmap)}
	 * 
	 * @param imgSrc
	 * @param imgDst
	 */
	public void setImage(Bitmap imgSrc, Bitmap imgDst);
	
	/**
	 * 设置特效图像源。 </br>
	 * see {@link Effector#setImage(Bitmap, int)}
	 * 
	 * @param image
	 * @param which
	 */
	public void setImage(Bitmap image, int which);
	
    /**
     * 设置特效画质。</br>
     * see {@link Effector#setHighQuality(boolean)}
     * 
     * @param high True 高画质，false 低画质
     */
    public void setHighQuality(boolean high);
    
    /**
     * see {@link Effector#setBkColor(int)}
     * 
     * @param color.
     */
    public void setBkColor(int color);
    
	/**
	 * 设置特效因子。 </br>
	 * see {@link Effector#setEffectFactor(float)}
	 * 
	 * @param factor
	 */
	public void setEffectFactor(float factor);
	
	/**
	 * 设置特效类型。 </br>
	 * see {@link Effector#setEffectType(int)}
	 * 
	 * @param type
	 */
	public void setEffectType(int type);
	
	/**
	 * 设置特效目标 View。 </br>
	 * 你可以不使用这个方法设置目标 View，但是你就需要自己保证 {@link Effector} 的
	 * 变化图像是有效的。用这个方法设置了目标 View 之后，每次改变特效因子的时候，会使用
	 * 这2个目标 View 去检查特效图像是否有效，如果无效就会重新创建。
	 * 
	 * @param srcView 变化源 View
	 * @param dstView 变化目标 View
	 */
	public void setTargetView(View srcView, View dstView);
	
	/**
	 * 设置特效目标 View。 </br>
	 * 你可以不使用这个方法设置目标 View，但是你就需要自己保证 {@link Effector} 的
	 * 变化图像是有效的。用这个方法设置了目标 View 之后，每次改变特效因子的时候，会使用
	 * 这2个目标 View 去检查特效图像是否有效，如果无效就会重新创建。
	 * 
	 * @param targetView 变化源 View
	 * @param which {@link Effector#ID_SRC} or {@link Effector#ID_DST}
	 */
	public void setTargetView(View targetView, int which);
	
	/**
	 * 获取目标 View
	 * 
	 * @param which {@link Effector#ID_SRC} or {@link Effector#ID_DST}.
	 * @return 目标 View 对象
	 */
	public View getTargetView(int which);
	
	/**
	 * 获取当前使用的特效器。
	 * 
	 * @return 当前使用的 {@link Effector}
	 */
	public Effector getEffector();
	
	/**
	 * 获取当前使用的特效器的名字
	 * 
	 * @return 特效器的名字，null 当前没特效器
	 */
	public String getEffectorName();
	
	/**
	 * 获取当前特效因子。 </br>
	 * see {@link Effector#getEffectFactor()}
	 * 
	 * @return
	 */
	public float getEffectFactor();
	
	/**
	 * 获取特效器当前类型。 </br>
	 * see {@link Effector#getEffectType()}
	 * 
	 * @return
	 */
	public int getEffectType();
	
    /**
     * 获取当前的目标 View （受 {@link #reverseEffect(boolean)} 的影响）。
     * 
     * @param which {@link Effector#ID_SRC} or {@link Effector#ID_DST}
     * @return
     */
	public View getUseTargetView(int which);
	
    /**
     * 获取特当前特效是否开启高画质。 </br>
     * see {@link Effector#isHighQuality()}
     * 
     * @return True 高画质，false 低画质
     */
	public boolean isHighQuality();
	
    /**
     * 获取特效器当前是否处于反转状态。</br>
     * see {@link Effector#isReverseEffect()}
     * 
     * @return True 反转特效，false 正常特效
     */
    public boolean isReverseEffect();
    
    /**
     * 查询当前是否有特效器
     * 
     * @return True 有，false 没有
     */
    public boolean haveEffector();
    
    /**
     * 重建所有的变化缓存图像
     * 
     * @param width
     * @param height
     * @param config
     * @return
     */
	public boolean buildImage(int width, int height, Bitmap.Config config);
	
	/**
	 * 重建指定的变化缓存图像
	 * 
	 * @param width
	 * @param height
	 * @param config
	 * @param which
	 * @return
	 */
	public boolean buildImage(int width, int height, Bitmap.Config config, int which);
	
    /**
     * 截取变化 View 图像。</br>
     * 用这个接口，要确保使用 {@link #setTargetView(View, int)} 设置过目标 View。 </br>
     * see {@link Effector#captureImage(View, which)}
     * 
     * @param which {@link Effector#ID_SRC}, {@link Effector#ID_DST} 的组合
     * @return 
     */
	public boolean captureImage(int which);
	
    /**
     * 截取变化 View 图像。 </br>
     * see {@link Effector#captureImage(View, View)}
     * 
     * @param srcView
     * @param dstView
     * @return 
     */
	public boolean captureImage(View srcView, View dstView);
	
	/**
	 * 截取目标 View 的图像作为特效器变化图像源。 </br>
	 * see {@link Effector#captureImage(View, int)}
	 * 
	 * @param targetView
	 * @param which
	 * @return
	 */
	public boolean captureImage(View targetView, int which);
	
	/**
	 * 填充特效图像。 </br>
	 * see {@link Effector#fillImage(Drawable, int)}
	 * 
	 * @param drawable
	 * @param which
	 * @return 
	 */
	public boolean fillImage(Drawable drawable, int which);
	
	/** 
	 * 擦除图像颜色。 </br>
	 * see {@link Effector#eraseImage(int)}
	 * 
	 * @param
	 * @return
	 */
	public boolean eraseImage(int which);
	
	/** 
	 * 交换源、目标特效对象
	 */
	public void swapTarget();
	
    /**
     * 反转特效。 </br>
     * 例如：如果一个特效是从右边滑动到左边，那么反转后，就是从左边滑动到右边。
     * 
     * @param reverse True 翻转，false 正常
     */
    public void reverseEffect(boolean reverse);
    
    /**
     * 刷新 View。 将当前特效重新画一次。
     */
    public void refresh();
    
	/**
	 * Query whether use OpenGL.
	 * 
	 * @return True base OpenGL，false base canvas.
	 */
	public boolean isGL();
	
}
