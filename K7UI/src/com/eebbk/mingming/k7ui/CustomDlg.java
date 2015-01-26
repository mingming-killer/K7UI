package com.eebbk.mingming.k7ui;

import com.eebbk.mingming.k7utils.LogUtils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 
 * Custom {@link Dialog}. Custom background, position, size and so on.
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class CustomDlg extends Dialog {
	
	private final static String TAG = "CustomDlg";
	
    protected int mX;
    protected int mY;
    protected int mWidth;
    protected int mHeight;
    protected int mGravity;
    
    private boolean mSetProperty;
    
    protected int mWindowLocation[];
    protected int mScreenLocation[];
    
    protected boolean mAllowScrollingAnchorParent;
    protected boolean mClipToScreen;
	
	protected View mContentView;
	
	
	public CustomDlg(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init();
	}

	public CustomDlg(Context context) {
		this(context, 0);
	}
	
	public CustomDlg(Context context, int theme) {
		super(context, theme);
		init();
	}
	
	private void init() {
		mSetProperty = false;
		
		mX = 0;
		mY = 0;
		mWidth = 0;
		mHeight = 0;
		mGravity = Gravity.CENTER;
		
		mWindowLocation = new int[2];
		mScreenLocation = new int[2];
		
		mAllowScrollingAnchorParent = true;
		mClipToScreen = true;
		
		mContentView = null;
	}
	
	/**
	 * Get dialog set X.
	 * Notice: this is just set x, not lp.x of window in dialog.
	 * 
	 * @return
	 */
	public int getX() {
		return mX;
	}
	
	/**
	 * Get dialog set Y.
	 * Notice: this is just set y, not lp.y of window in dialog.
	 * 
	 * @return
	 */
	public int getY() {
		return mY;
	}
	
	/**
	 * Get dialog set width.
	 * Notice: this is just set width, not lp.width of window in dialog.
	 * 
	 * @return
	 */
	public int getWidth() {
		return mWidth;
	}
	
	/**
	 * Get dialog set height.
	 * Notice: this is just set y, not lp.height of window in dialog.
	 * 
	 * @return
	 */
	public int getHeight() {
		return mHeight;
	}
	
	/**
	 * Get dialog set gravity.
	 * Notice: this is just set gravity, not lp.gravity of window in dialog.
	 * 
	 * @return
	 */
	public int getGravity() {
		return mGravity;
	}
	
	/**
	 * Get the window layout params which in dialog
	 * 
	 * @return {@link WindowManager#LayoutParams}
	 */
	public WindowManager.LayoutParams getWindowLayoutParams() {
		Window window = getWindow();
		if (null == window) {
			return null;
		}
		
		return window.getAttributes();
	}
	
	/**
	 * Get whether allow clip dialog to screen.
	 * 
	 * @return True allow, false disable
	 */
	//public boolean isClipToScreenEnabled() {
	//	return mClipToScreen;
	//}
	
	/**
	 * Set dialog background.
	 * 
	 * @param resID Background resource ID(must be drawable).
	 */
	public void setBackground(int resID) {
		Window window = getWindow();
		if (null == window) {
			LogUtils.d(TAG, "dialog window is null, set background failed !");
			return;
		}
		
		window.setBackgroundDrawableResource(resID);
	}
	
	/**
	 * Set dialog background drawable.
	 * 
	 * @param drBk Background drawable.
	 */
	public void setBackgroundDrawable(Drawable drBk) {
		if (null == drBk) {
			LogUtils.d(TAG, "background drawable is null !");
			return;
		}
		
		Window window = getWindow();
		if (null == window) {
			LogUtils.d(TAG, "dialog window is null, set background drawable failed !");
			return;
		}
		
		window.setBackgroundDrawable(drBk);
	} 
	
	/**
	 * Set dialog content view.
	 * 
	 * @param contentView Content view.
	 */
	public void setContentView(View contentView) {
		if (null == contentView) {
			LogUtils.d(TAG, "background drawable is null !");
			return;
		}
		
		Window window = getWindow();
		if (null == window) {
			LogUtils.d(TAG, "dialog window is null, set background drawable failed !");
			return;
		}
		
		window.setContentView(contentView);
		mContentView = contentView;
		
		// we must wait content view add to window.
		// the set window property can effect.
		if (mSetProperty) {
			setPosition(mGravity, mX, mY, mWidth, mHeight);
		}
	}

	/** 
	 * Set dialog size. see {@link #setPosition(int, int, int, int, int)}.
	 * 
	 * @param width 
	 * @param height 
	 */
	public void setSize(int width, int height) {
		Window window = getWindow();
		if (null == window) {
			LogUtils.d(TAG, "dialog window is null, set size failed !");
			return;
		}
		
		WindowManager.LayoutParams lp = window.getAttributes();
		if (null == lp) {
			lp = new WindowManager.LayoutParams();
		}
		
		setPosition(lp.gravity, lp.x, lp.y, width, height);
	}
	
	/**
	 * Set position. see {@link #setPosition(int, int, int, int, int)}.
	 * 
	 * @param x
	 * @param y
	 */
	public void setPosition(int x, int y) {
		Window window = getWindow();
		if (null == window) {
			LogUtils.d(TAG, "dialog window is null, set position failed !");
			return;
		}
		
		WindowManager.LayoutParams lp = window.getAttributes();
		if (null == lp) {
			lp = new WindowManager.LayoutParams();
		}
		
		setPosition(lp.gravity, x, y, lp.width, lp.height);
	}
	
	/**
	 * Set position. see {@link #setPosition(int, int, int, int, int)}.
	 * 
	 * @param gravity
	 * @param x
	 * @param y
	 */
	public void setPosition(int gravity, int x, int y) {
		Window window = getWindow();
		if (null == window) {
			LogUtils.d(TAG, "dialog window is null, set position failed !");
			return;
		}
		
		WindowManager.LayoutParams lp = window.getAttributes();
		if (null == lp) {
			lp = new WindowManager.LayoutParams();
		}
		
		setPosition(gravity, x, y, lp.width, lp.height);
	}
	
	/**
	 * Set dialog show position.
	 * 
	 * @param gravity see {@link WindowManager.LayoutParams#gravity}.
	 * @param x see {@link WindowManager.LayoutParams#x}.
	 * @param y see {@link WindowManager.LayoutParams#y}.
	 * @param width can be {@link ViewGroup.LayoutParams#MATCH_PARENT} or {@link ViewGroup.LayoutParams#WRAP_CONTENT}.
	 * @param height can be {@link ViewGroup.LayoutParams#MATCH_PARENT} or {@link ViewGroup.LayoutParams#WRAP_CONTENT}.
	 */
	public void setPosition(int gravity, int x, int y, int width, int height) {
		
		mGravity = gravity;
		mX = x;
		mY = y;
		mWidth = width;
		mHeight = height;
		
		if (null != mContentView) {
			Window window = getWindow();
			if (null == window) {
				LogUtils.d(TAG, "dialog window is null, set position failed !");
				return;
			}
			
			WindowManager.LayoutParams lp = window.getAttributes();
			if (null == lp) {
				lp = new WindowManager.LayoutParams();
			}
			
			lp.gravity = gravity;
			lp.x = x;
			lp.y = y;
			lp.width = width;
			lp.height = height;
			
			window.setAttributes(lp);
			mSetProperty = false;
			
		} else {
			mSetProperty = true;
		}
	}
	
	/**
	 * Set drop down position. 
	 * 
	 * @param anchor
	 * @param xoff
	 * @param yoff
	 * 
	 * @return true if the popup is translated upwards to fit on screen
	 */
	public boolean setPositionAsDropDown(View anchor, int xoff, int yoff) {
		Window window = getWindow();
		if (null == window) {
			LogUtils.d(TAG, "dialog window is null, set position failed !");
			return false;
		}
		
		WindowManager.LayoutParams lp = window.getAttributes();
		if (null == lp) {
			lp = new WindowManager.LayoutParams();
		}
		
		boolean onTop = findDropDownPosition(anchor, lp, xoff, yoff);
		setPosition(lp.gravity, lp.x, lp.y, lp.width, lp.height);
		
		return onTop;
	}
	
	/**
	 * Set dialog alpha value.
	 * Please call this method to set alpha after you call {@link #show()}, 
	 * otherwise there is no effect !!
	 * 
	 * @param alpha {@link WindowManager.LayoutParams#alpha}.
	 */
	public void setAlpha(float alpha) {
		Window window = getWindow();
		if (null == window) {
			LogUtils.d(TAG, "dialog window is null, set alpha failed !");
			return;
		}
		
		WindowManager.LayoutParams lp = window.getAttributes();
		if (null == lp) {
			lp = new WindowManager.LayoutParams();
		}
		
		lp.alpha = alpha;
		window.setAttributes(lp);
	}
	
	/**
	 * Set dialog dimAmount.
	 * Please call this method to set dimAmount after you call {@link #show()}, 
	 * otherwise there is no effect !!
	 * 
	 * @param dimAmount {@link WindowManager.LayoutParams#dimAmount}
	 */
	public void setDimAmount(float dimAmount) {
		Window window = getWindow();
		if (null == window) {
			LogUtils.d(TAG, "dialog window is null, set dimAmount failed !");
			return;
		}
		
		WindowManager.LayoutParams lp = window.getAttributes();
		if (null == lp) {
			lp = new WindowManager.LayoutParams();
		}
		
		lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lp.dimAmount = dimAmount;
		window.setAttributes(lp);
	}
	
    /**
     * Clip this popup window to the screen, but not to the containing window.
     *
     * @param enabled True to clip to the screen.
     */
    protected void setClipToScreenEnabled(boolean enabled) {
        mClipToScreen = enabled;
        //setClippingEnabled(!enabled);
    }
    
    /**
     * Allow scroll parent to let dialog fit screen.
     * 
     * @param allow
     */
    protected void setAllowScrollingAnchorParent(boolean allow) {
    	mAllowScrollingAnchorParent = allow;
    }
    
    /**
     * Same as {@link android.widget.PopupWindow#showAtLocation(View, int, int, int)}.
     * Notice: your dialog must in the same window of your set anchor view.
     * 
     * @param anchor
     * @param xoff
     * @param yoff
     * 
     * @return true if the popup is translated upwards to fit on screen
     */
    public boolean showAsDropDown(View anchor, int xoff, int yoff) {
		Window window = getWindow();
		if (null == window) {
			LogUtils.d(TAG, "dialog window is null, show as drop down failed !");
			return false;
		}
		
		WindowManager.LayoutParams lp = window.getAttributes();
		if (null == lp) {
			lp = new WindowManager.LayoutParams();
		}
		
		boolean onTop = findDropDownPosition(anchor, lp, xoff, yoff);
		
		setPosition(lp.gravity, lp.x, lp.y, lp.width, lp.height);
		show();
		
		return onTop;
    }
    
    /**
     * <p>Positions the popup window on screen. When the popup window is too
     * tall to fit under the anchor, a parent scroll view is seeked and scrolled
     * up to reclaim space. If scrolling is not possible or not enough, the
     * popup window gets moved on top of the anchor.</p>
     *
     * <p>The height must have been set on the layout parameters prior to
     * calling this method.</p>
     * 
     * <p>This method is base on android popup window.</p>
     *
     * @param anchor the view on which the popup window must be anchored
     * @param lp the layout parameters used to display the drop down
     * @param xoff
     * @param yoff
     *
     * @return true if the popup is translated upwards to fit on screen
     */
    protected boolean findDropDownPosition(View anchor, WindowManager.LayoutParams lp,
            int xoff, int yoff) {
    	
        final int anchorHeight = anchor.getHeight();
        anchor.getLocationInWindow(mWindowLocation);
        anchor.getLocationOnScreen(mScreenLocation);
        
        final Rect displayFrame = new Rect();
        anchor.getWindowVisibleDisplayFrame(displayFrame);
        
        // the window location include the system status bar rect, oh shit >_<
        // so it if your window is not full screen is not really window location.
        // so we must exclude it.
        mWindowLocation[0] -= displayFrame.left;
        mWindowLocation[1] -= displayFrame.top;
        
        lp.x = mWindowLocation[0] + xoff;
        lp.y = mWindowLocation[1] + anchorHeight + yoff;
        lp.width = mWidth;
        lp.height = mHeight;
        
        boolean onTop = false;
        
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        
        int screenX = mScreenLocation[0] + xoff;
        int screenY = mScreenLocation[1] + anchorHeight + yoff;
        
        final View root = anchor.getRootView();
        if (screenY + lp.height > displayFrame.bottom ||
                screenX + lp.width - root.getWidth() > 0) {
            // if the drop down disappears at the bottom of the screen. we try to
            // scroll a parent scrollview or move the drop down back up on top of
            // the edit box
            if (mAllowScrollingAnchorParent) {
                int scrollX = anchor.getScrollX();
                int scrollY = anchor.getScrollY();
                Rect r = new Rect(scrollX, scrollY,  scrollX + lp.width + xoff,
                        scrollY + lp.height + anchor.getHeight() + yoff);
                anchor.requestRectangleOnScreen(r, true);
            }
            
            // now we re-evaluate the space available, and decide from that
            // whether the pop-up will go above or below the anchor.
            anchor.getLocationInWindow(mWindowLocation);
            mWindowLocation[0] -= displayFrame.left;
            mWindowLocation[1] -= displayFrame.top;
            
            lp.x = mWindowLocation[0] + xoff;
            lp.y = mWindowLocation[1] + anchor.getHeight() + yoff;
            
            // determine whether there is more space above or below the anchor
            anchor.getLocationOnScreen(mScreenLocation);
            
            onTop = (displayFrame.bottom - (mScreenLocation[1] + anchor.getHeight() + yoff)) <
                    (mScreenLocation[1] - yoff - displayFrame.top);
            if (onTop) {
                lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
                // notice that: the root width and height is include system status bar size. -_-||
                //lp.y = root.getHeight() - mWindowLocation[1] + yoff;
                lp.y = root.getHeight() - mScreenLocation[1] + yoff;
            } else {
                lp.y = mWindowLocation[1] + anchor.getHeight() + yoff;
            }
        }

        if (mClipToScreen) {
            final int displayFrameWidth = displayFrame.right - displayFrame.left;
            
            int right = lp.x + lp.width;
            if (right > displayFrameWidth) {
                lp.x -= right - displayFrameWidth;
            }
            if (lp.x < displayFrame.left) {
                lp.x = displayFrame.left;
                lp.width = Math.min(lp.width, displayFrameWidth);
            }

            if (onTop) {
                //int popupTop = mScreenLocation[1] + yoff - lp.height;
            	int popupTop = mWindowLocation[1] + yoff - lp.height;
                if (popupTop < 0) {
                    lp.y += popupTop;
                }
            } else {
            	// notice: the WindowManager.LayoutParams Gravity is relatively 
            	// current window, so the min y is 0.
                //lp.y = Math.max(lp.y, displayFrame.top);
            	lp.y = Math.max(lp.y, 0);
            }
        }
        
        lp.gravity |= Gravity.DISPLAY_CLIP_VERTICAL;
        
        return onTop;
    }
	
}
