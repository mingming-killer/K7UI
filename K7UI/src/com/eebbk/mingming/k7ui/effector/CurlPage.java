package com.eebbk.mingming.k7ui.effector;

import com.eebbk.mingming.k7utils.BitmapUtils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * 
 * Storage class for page textures, blend colors and possibly some other values
 * in the future.
 * 
 * @author humingming <hmm@dw.gdbbk.com>
 * 
 */
public class CurlPage {

	public static final int SIDE_BACK = 2;
	public static final int SIDE_BOTH = 3;
	public static final int SIDE_FRONT = 1;
	
	private int mColorBack;
	private int mColorFront;
	private Bitmap mTextureBack;
	private Bitmap mTextureFront;
	private Bitmap mTextureColorBack;
	private Bitmap mTextureColorFront;
	private boolean mTexturesChanged;
	
	private RectF mTextureRectBack;
	private RectF mTextureRectFront;

	/**
	 * Default constructor.
	 */
	public CurlPage() {
		// 2 is the minimum PO2 size.
		mTextureColorFront = BitmapUtils.createBitmap(2, 2, Bitmap.Config.RGB_565);
		mTextureColorBack = BitmapUtils.createBitmap(2, 2, Bitmap.Config.RGB_565);
		mTextureRectBack = new RectF();
		mTextureRectFront = new RectF();
		reset();
	}

	/**
	 * Getter for color.
	 */
	public int getColor(int side) {
		switch (side) {
		case SIDE_FRONT:
			return mColorFront;
		default:
			return mColorBack;
		}
	}

	/**
	 * Calculates the next highest power of two for a given integer.
	 */
	private int getNextHighestPO2(int n) {
		n -= 1;
		n = n | (n >> 1);
		n = n | (n >> 2);
		n = n | (n >> 4);
		n = n | (n >> 8);
		n = n | (n >> 16);
		n = n | (n >> 32);
		return n + 1;
	}

	/**
	 * Generates nearest power of two sized Bitmap for give Bitmap. Returns this
	 * new Bitmap using default return statement + original texture coordinates
	 * are stored into RectF.
	 */
	@SuppressWarnings("unused")
	private Bitmap getTexture(Bitmap bitmap, RectF textureRect) {
		// Bitmap original size.
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		// Bitmap size expanded to next power of two. This is done due to
		// the requirement on many devices, texture width and height should
		// be power of two.
		int newW = getNextHighestPO2(w);
		int newH = getNextHighestPO2(h);

		// TODO: Is there another way to create a bigger Bitmap and copy
		// original Bitmap to it more efficiently? Immutable bitmap anyone?
		Bitmap bitmapTex = Bitmap.createBitmap(newW, newH, bitmap.getConfig());
		Canvas c = new Canvas(bitmapTex);
		c.drawBitmap(bitmap, 0, 0, null);

		// Calculate final texture coordinates.
		float texX = (float) w / newW;
		float texY = (float) h / newH;
		textureRect.set(0f, 0f, texX, texY);

		return bitmapTex;
	}

	/**
	 * Getter for textures. Creates Bitmap sized to nearest power of two, copies
	 * original Bitmap into it and returns it. RectF given as parameter is
	 * filled with actual texture coordinates in this new upscaled texture
	 * Bitmap.
	 */
	public Bitmap getTexture(RectF textureRect, int side) {
		/*switch (side) {
		case SIDE_FRONT:
			if (null == mTextureFront) {
				return getTexture(mTextureColorFront, textureRect);
			} else {
				return getTexture(mTextureFront, textureRect);
			}
		default:
			if (null == mTextureBack) {
				return getTexture(mTextureColorBack, textureRect);
			} else {
				return getTexture(mTextureBack, textureRect);
			}
		}*/
		switch (side) {
		case SIDE_FRONT:
			if (null == mTextureFront) {
				textureRect.set(0, 0, mTextureColorFront.getWidth(), mTextureColorFront.getHeight());
				return mTextureColorFront;
			} else {
				textureRect.set(mTextureRectFront);
				return mTextureFront;
			}
			
		default:
			if (null == mTextureBack) {
				textureRect.set(0, 0, mTextureColorBack.getWidth(), mTextureColorBack.getHeight());
				return mTextureColorBack;
			} else {
				textureRect.set(mTextureRectBack);
				return mTextureBack;
			}
		}
	}

	/**
	 * Returns true if textures have changed.
	 */
	public boolean getTexturesChanged() {
		return mTexturesChanged;
	}

	/**
	 * Returns true if back siding texture exists and it differs from front
	 * facing one.
	 */
	public boolean hasBackTexture() {
		if (null == mTextureFront || null == mTextureBack) {
			return false;
		} else {
			return !mTextureFront.equals(mTextureBack);
		}
	}
	
	/**
	 * Mark this texture is not changed.
	 */
	public void touchTexture() {
		mTexturesChanged = false;
	}

	/**
	 * Recycles and frees underlying Bitmaps.
	 */
	public void recycle() {
		//if (mTextureFront != null) {
		//	mTextureFront.recycle();
		//}
		//mTextureFront = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
		//mTextureFront.eraseColor(mColorFront);
		//if (mTextureBack != null) {
		//	mTextureBack.recycle();
		//}
		//mTextureBack = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
		//mTextureBack.eraseColor(mColorBack);
		
		BitmapUtils.freeBitmap(mTextureColorBack);
		BitmapUtils.freeBitmap(mTextureColorFront);
		
		mTexturesChanged = false;
	}

	/**
	 * Resets this CurlPage into its initial state.
	 */
	public void reset() {
		mColorBack = 0xffffffff;
		mColorFront = 0xffffffff;
		
		mTextureColorBack.eraseColor(mColorBack);
		mTextureColorFront.eraseColor(mColorFront);
		
		mTextureRectBack.set(0f, 0f, 0f, 0f);
		mTextureRectFront.set(0f, 0f, 0f, 0f);
		
		mTexturesChanged = false;
		//recycle();
	}

	/**
	 * Setter blend color.
	 */
	public void setColor(int color, int side) {
		switch (side) {
		case SIDE_FRONT:
			mColorFront = color;
			break;
		case SIDE_BACK:
			mColorBack = color;
			break;
		default:
			mColorFront = mColorBack = color;
			break;
		}
	}

	/**
	 * Setter for textures.
	 */
	/*public void setTexture(Bitmap texture, int side) {
		//if (texture == null) {
		//	texture = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
		//	if (side == SIDE_BACK) {
		//		texture.eraseColor(mColorBack);
		//	} else {
		//		texture.eraseColor(mColorFront);
		//	}
		//}
		switch (side) {
		case SIDE_FRONT:
			//if (mTextureFront != null)
			//	mTextureFront.recycle();
			mTextureFront = texture;
			break;
		case SIDE_BACK:
			//if (mTextureBack != null)
			//	mTextureBack.recycle();
			mTextureBack = texture;
			break;
		case SIDE_BOTH:
			//if (mTextureFront != null)
			//	mTextureFront.recycle();
			//if (mTextureBack != null)
			//	mTextureBack.recycle();
			mTextureFront = mTextureBack = texture;
			break;
		}
		mTexturesChanged = true;
	}*/
	public void setTexture(Bitmap texture, RectF textureRect, int side) {
		switch (side) {
		case SIDE_FRONT:
			mTextureFront = texture;
			mTextureRectFront.set(textureRect);
			break;
			
		case SIDE_BACK:
			mTextureBack = texture;
			mTextureRectBack.set(textureRect);
			break;
			
		case SIDE_BOTH:
			mTextureFront = mTextureBack = texture;
			mTextureRectFront.set(textureRect);
			mTextureRectBack.set(textureRect);
			break;
		}
		mTexturesChanged = true;
	}
	
}
