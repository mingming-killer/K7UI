package com.eebbk.mingming.k7ui.accessibility;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.opengl.GLUtils;

import com.eebbk.mingming.k7utils.BitmapUtils;
import com.eebbk.mingming.k7utils.LogUtils;

/**
 * 
 * OpenGL utils.
 * 
 * @author humingming <hmm@dw.gdbbk.com>
 * 
 */
public class K7GLUtils {
	
	private final static String TAG = "K7GLUtils";
	
	public final static String DEFAULT_DEBUG_DUMP_PATH = Utils.INTERNAL_SDCARD_PATH + "/" + "dump";
	
	public final static int INVALID_TEXTURE_ID = -1;
	
	private static IntBuffer mMaxTexSize;
	private static IntBuffer mMaxTexUnits;
	static {
		mMaxTexSize = IntBuffer.allocate(1);
		mMaxTexUnits = IntBuffer.allocate(1);
	}
	
	private K7GLUtils() {
	}
	
	/**
	 * Load a bitmap to GL texture. </br>
	 * see {@link #loadTexture(GL10, int[], Bitmap, boolean)} (x, x, x, true)
	 * 
	 * @param gl
	 * @param outTexID
	 * @param image
	 * @return
	 */
	public static int loadTexture(GL10 gl, int[] outTexID, Bitmap image) { 
		return loadTexture(gl, outTexID, image, true);
	}
	
	/**
	 * Load a bitmap to GL texture.
	 * 
	 * @param gl this method <b>must be</b> call in GL thread.
	 * @param outTexID Out put parameter, ID of loaded texture(the first element of array)
	 * @param image Object of {@link Bitmap}.
	 * @param freeOld True delete previous loaded texture in give ID，false don't handle old texture.
	 * @return value of glGetError return, {@link #INVALID_TEXTURE_ID} is other error(e.g invalid parameter)
	 */
	public static int loadTexture(GL10 gl, int[] outTexID, Bitmap image, boolean freeOld) {
		if (!checkImagePO2(image)) {
			LogUtils.e(TAG, "loadTexture: target size of bitmap is not a power of two, we can't load it to GL !!");
			return INVALID_TEXTURE_ID;
		}
		
        if (null == outTexID || outTexID.length <= 0) {
        	LogUtils.e(TAG, "loadTexture: out put texture ID is invalid !!");
        	return INVALID_TEXTURE_ID;
        }
        
        // free old texture name
        if (freeOld) {
        	freeTexture(gl, outTexID);
        }
        
        // create new texture name
        gl.glGenTextures(1, outTexID, 0);
        
        // bind texture
        gl.glBindTexture(GL10.GL_TEXTURE_2D, outTexID[0]);
        
        // when render size is small than real texture size, we use nearest method, 
        // the render quality is not good, but render speed is fast.
        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        
        // when render size is large than real texture size, we use linear filter method, 
        // it will get good render quality, but render speed is poor.
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        
        // when render size is large than real texture we let it to scale not repeat render.
        // S means horizontal and T means vertical.
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        // set texture blend mode, GL_REPLACE means only use texture.
        //gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
        
        // load the texture to GPU
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, image, 0);
		
		// return GL error if any.
		return gl.glGetError();
	}
	
    /**
     * Delete loaded texture.
     * 
     * @param gl this method <b>must be</b> call in GL thread.
     * @param texID delete texture ID(the first element of array)
     */
    public static void freeTexture(GL10 gl, int[] texID) {
    	if (null == texID || texID.length <= 0) {
    		return;
    	}
    	
    	gl.glDeleteTextures(1, texID, 0);
		texID[0] = INVALID_TEXTURE_ID;
    }
    
    /**
     * 取 2^n 幂
     * 
     * @param orgSize 原始大小
     * @return 最接近的 2^n 幂
     */
    public static int PO2(int orgSize) {
    	int checkSize = orgSize;
    	if (checkSize > 1) {
    		checkSize -= 1;
    	}
    	
    	return Integer.highestOneBit(checkSize) << 1;
    }
    
	/**
	 * 检测指定图像大小是否是 2^n 幂
	 * 
	 * @param image Object of {@link Bitmap}
	 * @return True 是，false 否 
	 */
	public static boolean checkImagePO2(Bitmap image) {
		if (!checkImageValid(image)) {
			return false;
		}
		
		int powerW = PO2(image.getWidth());
		int powerH = PO2(image.getHeight());
		
		if (image.getWidth() == powerW && image.getHeight() == powerH) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 检查给定图片是否有效（非空，不被回收）。
	 * 
	 * @param image Object of {@link Bitmap}
	 * @return True 有效，false 无效
	 */
	public static boolean checkImageValid(Bitmap image) {
		if (null != image && !image.isRecycled()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 计算纹理坐标.
	 * 
	 * @param outStartPos 输出参数，纹理起始坐标
	 * @param outEndPos 输出参数，纹理终止坐标
	 * @param image Object of {@link Bitmap}.
	 * @param x 纹理图像的 x.
	 * @param y 纹理图像的 y.
	 * @param width 纹理图像的宽度.
	 * @param height 纹理图像的高度.
	 */
	public static void computeTextureCoords(PointF outStartPos, PointF outEndPos, 
			Bitmap image, int x, int y, int width, int height) {
		
		if (!checkImageValid(image)) {
			LogUtils.e(TAG, "computeTextureCoords: texture image invalid !!");
			return;
		}
		
		if (null == outStartPos || null == outEndPos) {
			LogUtils.e(TAG, "computeTextureCoords: texture image invalid !!");
			return;
		}
		
		int w = image.getWidth();
		int h = image.getHeight();
		if (w <= 0 || h <= 0) {
			return;
		}
		
		if (x < 0 || y < 0 || width <= 0 || height <= 0 || 
				x > w || y > h) {
			return;
		}
		
		if (width > w) {
			width = w;
		}
		if (height > h) {
			height = h;
		}
		
		// compute texture coordinate offset
		outStartPos.x = (float) x / (float) w;
		outStartPos.y = (float) y / (float) h;
		
		outEndPos.x = (float) width / (float) w;
		outEndPos.x += outStartPos.x;
		outEndPos.y = (float) height / (float) h;
		outEndPos.y += outStartPos.y;
	}
	
    public static float d2r(float degree) {
        return degree * (float) Math.PI / 180f;
    }
    
    public static FloatBuffer toFloatBuffer(float[] v) {
        ByteBuffer buff = ByteBuffer.allocateDirect(v.length * 4); 
        buff.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = buff.asFloatBuffer();
        buffer.put(v);
        buffer.position(0);
        return buffer;
    }
    
    public static ShortBuffer toShortBuffer(short[] v) {
        ByteBuffer buff = ByteBuffer.allocateDirect(v.length * 2); 
        buff.order(ByteOrder.nativeOrder());
        ShortBuffer buffer = buff.asShortBuffer();
        buffer.put(v);
        buffer.position(0);
        return buffer;
    }
    
    public static ByteBuffer toByteBuffer(byte[] v) {
        ByteBuffer buff = ByteBuffer.allocateDirect(v.length); 
        buff.order(ByteOrder.nativeOrder());
        buff.put(v);
        buff.position(0);
        return buff;
    }
    
    public static ByteBuffer readImagePixels(Bitmap image) {
    	if (null == image || image.isRecycled()) {
    		LogUtils.e(TAG, "readImagePixels: target image is null or recycled !!");
    		return null;
    	}
    	
        ByteBuffer buff = ByteBuffer.allocateDirect(image.getByteCount()); 
        buff.order(ByteOrder.nativeOrder());
    	image.copyPixelsToBuffer(buff);
    	return buff;
    }
	
	/**
	 * 获取设备上 GL 支持的最大纹理大小
	 * 
	 * @return int[1]: [0]:texture size
	 */
	public static int[] getMaxTextureSize(GL10 gl) {
		gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, mMaxTexSize);
		return mMaxTexSize.array();
	}
	
	/**
	 * 获取设备上 GL 支持的最大纹理单元个数
	 * 
	 * @return int[1]: [0]:unit size
	 */
	public static int[] getMaxTextureUnits(GL10 gl) {
		gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_UNITS, mMaxTexUnits);
		return mMaxTexUnits.array();
	}
	
	/**
	 * 获取设备上 GL 的一些信息
	 * 
	 * @return 拼接好的信息字符串
	 */
	public static String getGLVendorInfo(GL10 gl) {
		int[] maxTexSize = getMaxTextureSize(gl);
		int[] maxTexUnits = getMaxTextureUnits(gl);
		return String.format("GL version: %s\nGL vendor: %s\nGL renderer: %s\nGL extensions: %s\nGL max texture size: %d\nGL max texture unit: %d\n", 
				gl.glGetString(GL10.GL_VERSION), 
				gl.glGetString(GL10.GL_VENDOR), 
				gl.glGetString(GL10.GL_RENDERER), 
				gl.glGetString(GL10.GL_EXTENSIONS), 
				maxTexSize[0], maxTexUnits[0]);
	}
	
	public static void debugDumpImage(Bitmap image) {
		debugDumpImage(image, DEFAULT_DEBUG_DUMP_PATH);
	}
	
	public static void debugDumpImage(Bitmap image, String pathPrefix) {
		String fileName = pathPrefix + File.separator + System.currentTimeMillis() + ".png";
		BitmapUtils.saveBitmapToFile(image, Bitmap.CompressFormat.PNG, fileName);
	}
    
}
