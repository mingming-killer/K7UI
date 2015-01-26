package com.eebbk.mingming.k7ui.effector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.eebbk.mingming.k7utils.LogUtils;

import android.graphics.Bitmap;
import android.graphics.Paint;

/**
 * 
 * Scroll effector implement by GL. </br> 
 * 
 * </br>
 * <b>Effect: </b> source scroll out, target scroll in. This is just for test. </br>
 * 
 * </br>
 * <b>Normal type: </b> </br>
 * &nbsp;&nbsp; {@link Effector#TYPE_HORIZONTAL}: target scroll in from right side, source scroll out with left side. </br>
 * &nbsp;&nbsp; {@link Effector#TYPE_VERTICAL}: target scroll in from bottom side, source scroll out with top side. </br>
 * 
 * </br>
 * <b>Reverse type: </b> the anti-direction with the normal type. </br>
 * 
 * @author humingming <hmm@dw.gdbbk.com>
 *
 */
public class ScrollEffectorGL extends GLEffector {
	
	private final static String NAME = "ScrollEffectorGL";
	
	private final static int VERTS = 4;
	private final static float Z_NORMAL = 0.1f;
	
    private FloatBuffer mSrcVertexBuff = null;
    private FloatBuffer mDstVertexBuff = null;
    private FloatBuffer mSrcTexCoordBuff = null;
    private FloatBuffer mDstTexCoordBuff = null;
    
	private Paint mPaint = null;
	
	
	public ScrollEffectorGL() {
		this(null, null, true, false, HORIZONTAL);
	}
	
	public ScrollEffectorGL(Bitmap imgSrc, Bitmap imgDst, 
			boolean highQuality, boolean reverse, int type) {
		super(imgSrc, imgDst, highQuality, reverse, type);
		
		mName = NAME;
		
		mPaint = new Paint();
		
		// buffers to be passed to gl*Pointer() functions
        // must be direct, i.e., they must be placed on the
        // native heap where the garbage collector cannot move them.
        // 
        // buffers with multi-byte datatypes (e.g., short, int, float)
        // must have their byte order set to native order
        ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        mSrcVertexBuff = vbb.asFloatBuffer();
        
        vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        mDstVertexBuff = vbb.asFloatBuffer();
        
        ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        mSrcTexCoordBuff = tbb.asFloatBuffer();
        
        tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        mDstTexCoordBuff = tbb.asFloatBuffer();
	}
	
	@Override
	public boolean supportTranBk() {
		// don't support transparent now, otherwise than has some problem.
		//return true;
		return false;
	}
	
	@Override
	protected void setEffectFactorImpl(float doEffectFactor) {
		// noting to do.
	}
	
	@Override
	protected void setEffectTypeImpl(int type) {
		// noting to do.
	}
	
	@Override
	protected void setHighQualityImpl(boolean high) {
    	// high quality turn on AA and AF
    	mPaint.setAntiAlias(high);
    	mPaint.setFilterBitmap(high);
	}
	
	@Override
	protected boolean swapImageImpl() {
		// noting to do.
		return true;
	}
	
	@Override
	protected void reverseEffectImpl(boolean reverse) {
		// nothing to do.
	}
	
	@Override
	protected void freeImpl() {
		if (null != mSrcVertexBuff) {
			mSrcVertexBuff.clear();
		}
		if (null != mDstVertexBuff) {
			mDstVertexBuff.clear();
		}
		if (null != mSrcTexCoordBuff) {
			mSrcTexCoordBuff.clear();
		}
		if (null != mDstTexCoordBuff) {
			mDstTexCoordBuff.clear();
		}
	}
	
	@Override
	protected void onGLReady(GL10 gl, EGLConfig config) {
		super.onGLReady(gl, config);
		
		// custom GL settings.
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		
		gl.glShadeModel(GL10.GL_SMOOTH);
		
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
		gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
		
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_CULL_FACE);
	}
	
	@Override
	protected void onGLContextChanged(GL10 gl, int width, int height) {
		super.onGLContextChanged(gl, width, height);
		
		// custom GL view port and projection.
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
		
		//LogUtils.d(NAME, "factor=" + doEffectFactor);
		
		// calculate the new vertex.
		updateVertex();
		
        // usually, the first thing one might want to do is to clear
        // the screen. The most efficient way of doing this is to use glClear().
		output.glClearColor(mBkColorA, mBkColorR, mBkColorG, mBkColorB);
		output.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        
        // reset 3D object matrix
		output.glMatrixMode(GL10.GL_MODELVIEW);
		output.glLoadIdentity();
        
		//output.glTranslatef(-0.5f, 0.0f, 0.0f);
        //output.glRotatef(80, 0.0f, 0.0f, 0.0f);
        //output.glRotatef(mAngle*0.25f,  1, 0, 0);
        
        // set the front polygon direction is Counterclockwise.
        // when you enable GL_CULL_FACE, the back polygon will cull(not render).
        // it's can improve render speed.
		output.glFrontFace(GL10.GL_CCW);
		output.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        
		// render src image.
		float xTran = -(2.0f * doEffectFactor);
		//float scale = (doEffectFactor * 0.5f) + 1.0f;
		output.glTranslatef(xTran, 0.0f, 0.0f);
		//output.glScalef(scale, scale, 1.0f);
		
		FloatBuffer srcVertexBuff = mSrcVertexBuff;
		FloatBuffer srcTexCoordBuff = mSrcTexCoordBuff;
		FloatBuffer dstVertexBuff = mDstVertexBuff;
		FloatBuffer dstTexCoordBuff = mDstTexCoordBuff;
		
		//if (isReverseEffect()) {
		//	srcVertexBuff = mDstVertexBuff;
		//	srcTexCoordBuff = mDstTexCoordBuff;
		//	dstVertexBuff = mSrcVertexBuff;
		//	dstTexCoordBuff = mSrcTexCoordBuff;
		//}
		
		output.glDisable(GL10.GL_TEXTURE_2D);
		output.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		//output.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
		output.glVertexPointer(3, GL10.GL_FLOAT, 0, srcVertexBuff);
		output.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, VERTS);
		
		output.glEnable(GL10.GL_TEXTURE_2D);
		output.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        
		output.glBindTexture(GL10.GL_TEXTURE_2D, mTexID[0]);
		output.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		output.glTexCoordPointer(2, GL10.GL_FLOAT, 0, srcTexCoordBuff);
		output.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, VERTS);
        
        // disable openGL function. close openGL function after use it done, may be this is a good habit.
        // switch openGL function is not cost time.
        output.glDisable(GL10.GL_TEXTURE_2D);
        output.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        
        // render dst image.
		output.glMatrixMode(GL10.GL_MODELVIEW);
		output.glLoadIdentity();
		
        xTran += mViewRect.width();
		output.glTranslatef(xTran, 0.0f, 0.0f);
		
		output.glDisable(GL10.GL_TEXTURE_2D);
		output.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		//output.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
		output.glVertexPointer(3, GL10.GL_FLOAT, 0, dstVertexBuff);
		output.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, VERTS);
		
		output.glEnable(GL10.GL_TEXTURE_2D);
		output.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		output.glTexCoordPointer(2, GL10.GL_FLOAT, 0, dstTexCoordBuff);
		output.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, VERTS);
        
        // disable openGL function. close openGL function after use it done, may be this is a good habit.
        // switch openGL function is not cost time.
        output.glDisable(GL10.GL_TEXTURE_2D);
        output.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        
        output.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        
        //mDrawTime = System.currentTimeMillis() - mDrawTime;
        //LogUtils.d(TAG, "draw background cost:  " + mDrawTime + " ms");
        
        return true;
	}
	
	@Override
	protected void updateCacheTextureCoordsImpl() {		
        // vertex coordinate is: 
        // 
        // (-1.0, 1.0) [0]                    (1.0, 1.0) [2]
        //           -------------------------
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //           -------------------------
        // (-1.0, -1.0) [1]                   (1.0, -1.0) [3]
        //
        
        // notices: texture coordinate range is: [0, 1] !!
        // and openGLES texture coordinate system is: 
        // 
        // (0.0, 1.0) [3]                     (1.0, 1.0) [2]
        //           -------------------------
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //           -------------------------
        // (0.0, 0.0) [0]                     (1.0, 0.0) [1]
        //
        // and you must provide the origin(0.0, 0.0) is align the vertex (left, top) position.
        // this is means: in the figure above, the texture coordinate point[0] should in vertex point[3].
        //
        float[] srcTexCoords = {
                //0.0f, 0.0f,  // [0]
                //0.0f, 1.0f,  // [3]
                //1.0f, 0.0f,  // [1]
                //1.0f, 1.0f,  // [2]
        		mSrcTexCoordStart.x, mSrcTexCoordStart.y,  // [0]
        		mSrcTexCoordStart.x, mSrcTexCoordEnd.y,    // [3]
        		mSrcTexCoordEnd.x,   mSrcTexCoordStart.y,  // [1]
        		mSrcTexCoordEnd.x,   mSrcTexCoordEnd.y,    // [2]
        };
        
        float[] dstTexCoords = {
        		mDstTexCoordStart.x, mDstTexCoordStart.y,  // [0]
        		mDstTexCoordStart.x, mDstTexCoordEnd.y,    // [3]
        		mDstTexCoordEnd.x,   mDstTexCoordStart.y,  // [1]
        		mDstTexCoordEnd.x,   mDstTexCoordEnd.y,    // [2]
        };
        
        LogUtils.d(NAME, "mSrcTexCoordStart=" + mSrcTexCoordStart + ", mSrcTexCoordEnd=" + mSrcTexCoordEnd
        		+ "mDstTexCoordStart=" + mDstTexCoordStart + ", mDstTexCoordEnd=" + mDstTexCoordEnd);
        
        mSrcTexCoordBuff.position(0);
        mSrcTexCoordBuff.put(srcTexCoords);
        mDstTexCoordBuff.position(0);
        mDstTexCoordBuff.put(dstTexCoords);
        
        // move all buffer cursor to the start position.
        mSrcTexCoordBuff.position(0);
        mDstTexCoordBuff.position(0);
        
        //for (int i = 0; i < mTexCoordBuffer.capacity(); i++) {
        //	LogUtils.d(NAME, String.format("texs[%d]=%f", i, mTexCoordBuffer.get(i)));
        //}
	}
	
	private void updateVertex() {
        // vertex coordinate is: 
        // 
        // (-1.0, 1.0) [0]                    (1.0, 1.0) [2]
        //           -------------------------
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //          |                         |
        //           -------------------------
        // (-1.0, -1.0) [1]                   (1.0, -1.0) [3]
        //
        float[] vertCoords = {
        	mViewRect.left,  mViewRect.top,    Z_NORMAL,  // [0]
            mViewRect.left,  mViewRect.bottom, Z_NORMAL,  // [1]
            mViewRect.right, mViewRect.top,    Z_NORMAL,  // [2]
            mViewRect.right, mViewRect.bottom, Z_NORMAL   // [3]
        };
        
        mSrcVertexBuff.position(0);
        mSrcVertexBuff.put(vertCoords);
        mSrcVertexBuff.position(0);
        
        mDstVertexBuff.position(0);
        mDstVertexBuff.put(vertCoords);
        mDstVertexBuff.position(0);
        
        //LogUtils.d(NAME, "updateVertex mViewRect=" + mViewRect + ", this=" + mViewRect.hashCode()
        //		+ ", viewPortW=" + mViewPortWidth + ", viewPortH=" + mViewPortHeight);
        //for (int i = 0; i < mVertexBuff.capacity(); i++) {
        //	LogUtils.d(NAME, String.format("vert[%d]=%f", i, mVertexBuff.get(i)));
        //}
	}
	
}
