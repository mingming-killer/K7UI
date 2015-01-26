package com.eebbk.mingming.k7ui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES10.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.eebbk.mingming.k7utils.LogUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.AttributeSet;

/**
 * 
 * Use {@link GLSurfaceView} to draw a background.
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class GLBkSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
	
	private final static String TAG = "GLBkSurfaceView";
	
	private final static int INVALID_TEXTURE_ID = -1;
	private final static float COLOR_CONVERT_VALUE = 255f;
	
	private final static int DEFAULT_BK_RES_ID = -1;
	private final static int DEFAULT_BK_TEXTURE_X = 0;
	private final static int DEFAULT_BK_TEXTURE_Y = 0;
	private final static int DEFAULT_BK_TEXTURE_W = 0;
	private final static int DEFAULT_BK_TEXTURE_H = 0;
	
	/** Polygon vertex number */
	private final static int VERTS = 6; //6; //3;
	
	
	/** 
	 * Background {@link Bitmap} resource id(now must be <b>raw</b> type). </br>
	 * <b>Notices:</b> the bitmap size must be 2^n.
	 */
	private int mBmpBkResID;
	
	/** 
	 * Background {@link Bitmap}.</br> 
	 * <b>Notices:</b> the bitmap size must be 2^n. 
	 */
	private Bitmap mBmpBk;
	
	/** Background bitmap offset x. */
	private int mBmpBkOffsetX;
	
	/** Background bitmap offset y. */
	private int mBmpBkOffsetY;
	
	/** Background bitmap actual width. */
	private int mBmpBkWidth;
	
	/** Background bitmap actual height. */
	private int mBmpBkHeight;
	
	/** 
	 * Calibrate color red component. </br>
	 * I don't know why some time, the texture don't render full of the region, 
	 * so I use the first actual use background pixel as the background color.
	 */
	private float mCalibrateColorR;
	
	/** Calibrate color green component. */
	private float mCalibrateColorG;
	
	/** Calibrate color blue component. */
	private float mCalibrateColorB;
	
	
	/** Texture start coordinate x. */
	private float mTextureStartX;
	
	/** Texture start coordinate y. */
	private float mTextureStartY;
	
	/** Texture end coordinate x. */
	private float mTextureEndX;
	
	/** Texture end coordinate y. */
	private float mTextureEndY;
	
	
	/** Flag of OpenGL is ready for access. */
	private boolean mGLReady;
	
	private long mDrawTime;
	private Context mContext;
	
	/** Vertex buffer. */
    private FloatBuffer mVertexBuffer = null;
    
    /** Texture buffer. */
    private FloatBuffer mTexBuffer = null;
    
    /** Index buffer. */
    private ShortBuffer mIndexBuffer = null;
    
    
    /** Texture id. */
	private int mTextureID;
	
	
	public GLBkSurfaceView(Context context) {
		this(context, null);
	}
	
	public GLBkSurfaceView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public GLBkSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		
		// not use now, the parent class don't have this constructor.
		//TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GLBkSurfaceView, defStyle, 0);
		
		//mBmpBkResID = a.getInteger(R.styleable.GLBkSurfaceView_glbksvBackground, DEFAULT_BK_RES_ID);
		//mBmpBkOffsetX = a.getInteger(R.styleable.GLBkSurfaceView_glbksvBackgroundX, DEFAULT_BK_TEXTURE_X);
		//mBmpBkOffsetY = a.getInteger(R.styleable.GLBkSurfaceView_glbksvBackgroundY, DEFAULT_BK_TEXTURE_Y);
		//mBmpBkWidth = a.getInteger(R.styleable.GLBkSurfaceView_glbksvBackgroundW, DEFAULT_BK_TEXTURE_W);
		//mBmpBkHeight = a.getInteger(R.styleable.GLBkSurfaceView_glbksvBackgroundH, DEFAULT_BK_TEXTURE_H);
        
        //a.recycle();
		
		mBmpBkResID = DEFAULT_BK_RES_ID;
		mBmpBkOffsetX = DEFAULT_BK_TEXTURE_X;
		mBmpBkOffsetY = DEFAULT_BK_TEXTURE_Y;
		mBmpBkWidth = DEFAULT_BK_TEXTURE_W;
		mBmpBkHeight = DEFAULT_BK_TEXTURE_H;
		
		init(context);
	}
	
	private void init(Context context) {
		// set render
		setRenderer(this);
		
		// set render mode to RENDERMODE_WHEN_DIRTY.
		// this mode don't update continuously.
		setRenderMode(RENDERMODE_WHEN_DIRTY);
		
		mGLReady = false;
		mBmpBk = null;
		
		mContext = context;
		mTextureID = INVALID_TEXTURE_ID;
		
		mCalibrateColorR = 1.0f;
		mCalibrateColorG = 1.0f;
		mCalibrateColorB = 1.0f;
		
		mTextureStartX = 0.0f;
		mTextureStartY = 0.0f;
		mTextureEndX = 0.0f;
		mTextureEndY = 0.0f;
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		
		LogUtils.d(TAG, "GLBkSurfaceView onPause()");
		
		// when activity is pause the openGL is not ready.
		mGLReady = false;
	}
	
	@Override
    public void onDrawFrame(GL10 gl) {
		
		mDrawTime = System.currentTimeMillis();
		
        // if set the background texture, here the OpenGL is ready, we load it.
        //if (mTextureID < 0) {
        //	if (mBmpBkResID > 0) {
        //		mTextureID = loadTexture(mBmpBkResID, 
        //				mBmpBkOffsetX, mBmpBkOffsetY, mBmpBkWidth, mBmpBkHeight);
        //	} else if (null != mBmpBk) {
        //		mTextureID = loadTexture(mBmpBk, 
        //				mBmpBkOffsetX, mBmpBkOffsetY, mBmpBkWidth, mBmpBkHeight);
        //	}
        //}
		
        // usually, the first thing one might want to do is to clear
        // the screen. The most efficient way of doing this is to use glClear().
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        
        // use the calibrate color as background color.
        gl.glClearColor(mCalibrateColorR, mCalibrateColorG, mCalibrateColorB, 1.0f);
        
        // not bind texture, we can't render.
     	if (mTextureID <= INVALID_TEXTURE_ID || 
     			null == mVertexBuffer || null == mTexBuffer || 
     			null == mIndexBuffer) {
     		return;
     	}
        
        // reset 3D object matrix
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        //gl.glTranslatef(0, 0, -3.0f);
        //gl.glRotatef(mAngle,        0, 1, 0);
        //gl.glRotatef(mAngle*0.25f,  1, 0, 0);
        
        // set the front polygon direction is Counterclockwise.
        // when you enable GL_CULL_FACE, the back polygon will cull(not render).
        // it's can improve render speed.
        gl.glFrontFace(GL10.GL_CCW);
        
        // enable openGL texture2D, vertex array and texture coordinate array 
        // to render vertex with texture.
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        
        //gl.glColor4f(1.0f, 0f, 0f, 0.5f);
        
        // render vertex.
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        
        // render texture.
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, VERTS, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
        
        // disable openGL function. close openGL function after use it done, may be this is a good habit.
        // switch openGL function is not cost time.
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        //gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        
        mDrawTime = System.currentTimeMillis() - mDrawTime;
        LogUtils.d(TAG, "draw background cost:  " + mDrawTime + " ms");
    }

	@Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		LogUtils.d(TAG, "GLBkSurfaceView onSurfaceChanged()");
    	
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
    }

	@Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        
		LogUtils.d(TAG, "GLBkSurfaceView onSurfaceCreated()");
		mGLReady = true;
		
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
        
        // if set the background texture, here the OpenGL is ready, we load it.
        if (mTextureID < 0) {
        	if (mBmpBkResID > 0) {
        		mTextureID = loadTexture(mBmpBkResID, 
        				mBmpBkOffsetX, mBmpBkOffsetY, mBmpBkWidth, mBmpBkHeight);
        	} else if (null != mBmpBk) {
        		mTextureID = loadTexture(mBmpBk, 
        				mBmpBkOffsetX, mBmpBkOffsetY, mBmpBkWidth, mBmpBkHeight);
        	}
        }
    }
	
	/**
	 * Set background.
	 * 
	 * @param resID Background resource id.
	 *   <b>Notice:</b> must be raw resource id. 
	 *   And the bitmap size must be 2^n (sorry we are not in powerful GPU like Terge Geforce).
	 * @param x Background x offset.
	 * @param y Background y offset.
	 * @param width  Background actual width.
	 * @param height Background actual height.
	 */
	public void setBackground(int resID, int x, int y, int width, int height) {
		if (mGLReady) {
			// if OpenGL is ready, we load the texture immediately, 
			// and update the render.
			mTextureID = loadTexture(resID, x, y, width, height);
			requestRender();
		} else {
			// if OpenGL is not ready, we save the background setting, 
			// wait for openGL is ready.
			mBmpBkResID = resID;
			mBmpBkOffsetX = x;
			mBmpBkOffsetY = y;
			mBmpBkWidth = width;
			mBmpBkHeight = height;
		}
	}
	
	/**
	 * Set background.
	 * 
	 * @param bitmap Object of {@link Bitmap}.
	 *   <b>Notice:</b> The bitmap size must be 2^n (sorry we are not in powerful GPU like Terge Geforce).
	 * @param x Background x offset.
	 * @param y Background y offset.
	 * @param width Background actual width.
	 * @param height Background actual height.
	 */
	public void setBackground(Bitmap bitmap, int x, int y, int width, int height) {
		if (mGLReady) {
			// if OpenGL is ready, we load the texture immediately, 
			// and update the render.
			mTextureID = loadTexture(bitmap, x, y, width, height);
			requestRender();
		} else {
			// if OpenGL is not ready, we save the background setting, 
			// wait for openGL is ready.
			mBmpBk = bitmap;
			mBmpBkOffsetX = x;
			mBmpBkOffsetY = y;
			mBmpBkWidth = width;
			mBmpBkHeight = height;
		}
	}
	
	/**
	 * Load texture and bind it to OpenGL (include compute the vertex and texture coordinate).
	 * 
	 * @param resID Bitmap resource id.
	 * @param x Bitmap offset x.
	 * @param y y Bitmap offset y.
	 * @param width Bitmap actual width.
	 * @param height Bitmap actual height.
	 * @return Success return texture id, false return {@link #INVALID_TEXTURE_ID}.
	 */
	private int loadTexture(int resID, int x, int y, int width, int height) {
		InputStream is = mContext.getResources()
				.openRawResource(resID);
		Bitmap bitmap = null;
		
		// decode bitmap.
		try { 
			bitmap = BitmapFactory.decodeStream(is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				return INVALID_TEXTURE_ID;
			}
		}
		
		int textureID = INVALID_TEXTURE_ID;
		if (width <= 0 || height <= 0) {
			textureID = loadTexture(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
		} else {
			textureID = loadTexture(bitmap, x, y, width, height);
		}
		
		if (null != bitmap) {
			bitmap.recycle();
		}
		
		return textureID;
	}
	
	/**
	 * Load texture and bind it to OpenGL (include compute the vertex and texture coordinate).
	 * 
	 * @param bitmap Object of {@link Bitmap}.
	 * @param x Bitmap offset x.
	 * @param y Bitmap offset y.
	 * @param width Bitmap actual width.
	 * @param height Bitmap actual height.
	 * @return Success return texture id, false return {@link #INVALID_TEXTURE_ID}.
	 */
	private int loadTexture(Bitmap bitmap, int x, int y, int width, int height) { 
        if (null == bitmap) {
        	return INVALID_TEXTURE_ID;
        }
        
        int textureID = INVALID_TEXTURE_ID;
        int[] textures = new int[1];
        
        
        // create texture.
        glGenTextures(1, textures, 0);
        
        // bind texture.
        textureID = textures[0];
        glBindTexture(GL10.GL_TEXTURE_2D, textureID);
        
        // when render size is small than real texture size, we use nearest method, 
        // the render quality is not good, but render speed is fast.
        glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        
        // when render size is large than real texture size, we use linear filter method, 
        // it will get good render quality, but render speed is poor.
        glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        // when render size is large than real texture we let it to scale not repeat render.
        // S means horizontal and T means vertical.
        glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        // set texture blend mode, GL_REPLACE means only use texture.
        glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
        
        
        // load texture to openGL.
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        
        
        // compute texture coordinate.
        computeTextureCoords(bitmap, x, y, width, height);
        
        // initialize vertex(texture coordinate).
        initVertex();
        
        return textureID;
	}
	
	/**
	 * Compute texture coordinates.
	 * 
	 * @param bitmap Object of {@link Bitmap}.
	 * @param x Bitmap offset x.
	 * @param y Bitmap offset y.
	 * @param width Bitmap actual width.
	 * @param height Bitmap actual height.
	 */
	private void computeTextureCoords(Bitmap bitmap, int x, int y, int width, int height) {
		if (null == bitmap || bitmap.isRecycled()) {
			return;
		}
		
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if (w <= 0 || h <= 0) {
			return;
		}
		
		if (x < 0 || y < 0 || width <= 0 || height <= 0 || 
				x >= width || y >= height) {
			return;
		}
		
		if (width > w) {
			width = w;
		}
		if (height > h) {
			height = h;
		}
		
		// we use the first actual color as calibrate fill color.
		int color = bitmap.getPixel(x, y);
		mCalibrateColorR = (float)Color.red(color) / COLOR_CONVERT_VALUE;
		mCalibrateColorG = (float)Color.green(color) / COLOR_CONVERT_VALUE;
		mCalibrateColorB = (float)Color.blue(color) / COLOR_CONVERT_VALUE;
		
		// compute texture coordinate offset
		mTextureStartX = (float) x / (float) w;
		mTextureStartY = (float) y / (float) h;
		
		mTextureEndX = (float) width / (float) w;
		mTextureEndX += mTextureStartX;
		mTextureEndY = (float) height / (float) h;
		mTextureEndY += mTextureStartY;
	}
	
	/**
	 * Initialize vertex(texture coordinate vertex).
	 */
	private void initVertex() {
		
		// buffers to be passed to gl*Pointer() functions
        // must be direct, i.e., they must be placed on the
        // native heap where the garbage collector cannot
        // move them.
        //
        // buffers with multi-byte datatypes (e.g., short, int, float)
        // must have their byte order set to native order
        ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();

        ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTexBuffer = tbb.asFloatBuffer();

        ByteBuffer ibb = ByteBuffer.allocateDirect(VERTS * 2);
        ibb.order(ByteOrder.nativeOrder());
        mIndexBuffer = ibb.asShortBuffer();
        
        
        // vertex coordinate is: 
        // 
        // (-1.0, 1.0) [3]                    (1.0, 1.0) [2]
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
        // (-1.0, -1.0) [0]                   (1.0, -1.0) [1]
        //
        float[] vertCoords = {
            -1.0f, -1.0f, 0f,  // [0]
             1.0f, -1.0f, 0f,  // [1]
             1.0f,  1.0f, 0f,  // [2]
            -1.0f,  1.0f, 0f   // [3]
        };
        
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
        float[] texCoords = {
                //0.0f, 1.0f,  // [3]
                //1.0f, 1.0f,  // [2]
                //1.0f, 0.0f,  // [1]
                //0.0f, 0.0f,  // [0]
        		
        		mTextureStartX, mTextureEndY,   // [3]
        		mTextureEndX,   mTextureEndY,   // [2]
        		mTextureEndX,   mTextureStartY, // [1] 
        		mTextureStartX, mTextureStartY  // [0]
        };
        
        
        // OpenGLES only have triangle, so you must use triangle to compose other polygon.
        // the vertex coordinate is: 
        // 
        //         [3]        [2]           [2]                [3]                [2]
        //           ----------                                  ------------------
        //          |         /             /|                  |                  |
        //          |        /             / |                  |                  |
        //          |       /             /  |                  |                  |
        //          |      /             /   |                  |                  |
        //          |     /             /    |                  |                  |
        //          |    /       +     /     |         =        |                  |
        //          |   /             /      |                  |                  |
        //          |  /             /       |                  |                  |
        //          | /             /        |                  |                  |
        //          |/             /         |                  |                  |
        //                         ----------                    ------------------ 
        //         [0]           [0]         [1]               [0]                 [1]
        //
        mVertexBuffer.put(vertCoords[0*3 + 0]); mVertexBuffer.put(vertCoords[0*3 + 1]); mVertexBuffer.put(vertCoords[0*3 + 2]);
        mVertexBuffer.put(vertCoords[1*3 + 0]); mVertexBuffer.put(vertCoords[1*3 + 1]); mVertexBuffer.put(vertCoords[1*3 + 2]);
        mVertexBuffer.put(vertCoords[2*3 + 0]); mVertexBuffer.put(vertCoords[2*3 + 1]); mVertexBuffer.put(vertCoords[2*3 + 2]);
        
        mVertexBuffer.put(vertCoords[2*3 + 0]); mVertexBuffer.put(vertCoords[2*3 + 1]); mVertexBuffer.put(vertCoords[2*3 + 2]);
        mVertexBuffer.put(vertCoords[3*3 + 0]); mVertexBuffer.put(vertCoords[3*3 + 1]); mVertexBuffer.put(vertCoords[3*3 + 2]);
        mVertexBuffer.put(vertCoords[0*3 + 0]); mVertexBuffer.put(vertCoords[0*3 + 1]); mVertexBuffer.put(vertCoords[0*3 + 2]);
        
        
        mTexBuffer.put(texCoords[0*2 + 0]); mTexBuffer.put(texCoords[0*2 + 1]);
        mTexBuffer.put(texCoords[1*2 + 0]); mTexBuffer.put(texCoords[1*2 + 1]);
        mTexBuffer.put(texCoords[2*2 + 0]); mTexBuffer.put(texCoords[2*2 + 1]);
        
        mTexBuffer.put(texCoords[2*2 + 0]); mTexBuffer.put(texCoords[2*2 + 1]);
        mTexBuffer.put(texCoords[3*2 + 0]); mTexBuffer.put(texCoords[3*2 + 1]);
        mTexBuffer.put(texCoords[0*2 + 0]); mTexBuffer.put(texCoords[0*2 + 1]);
        
        
        for(int i = 0; i < VERTS; i++) {
            mIndexBuffer.put((short) i);
        }
        
        // move all buffer cursor to the start position.
        mVertexBuffer.position(0);
        mTexBuffer.position(0);
        mIndexBuffer.position(0);
	}
    
}
