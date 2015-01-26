package com.eebbk.mingming.k7ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.PopupWindow;

/**
 * 
 * Popup content wrapper. Implement by {@link PopupWindow}.
 * Let you can popup some content view.
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class PopupContentBuilder {
	
	@SuppressWarnings("unused")
	private Activity mContext = null;
	
	private View mContentView = null;
	private PopupWindow mContainerWindow = null;
	
	
	/**
	 * Build popup content.
	 * 
	 * @param contentView Which view you want to popup.
	 * @param width Popup menu width.
	 * @param height Popup menu height
	 * @return This PopupContentBuilder object
	 */
	public PopupContentBuilder build(View contentView, int width, int height) {
		mContentView = contentView;
		
		// set focusable true, otherwise the content view can't receive input event.
		mContainerWindow = new PopupWindow(mContentView, width, height, true);
		
		return this;
	}
	
	/**
	 * Show this popup menu. see also {@link PopupWindow#showAsDropDown(View, int, int)}.
	 * 
	 * @param anchor Anchor view.
	 * @param xoff Popup menu show x offset the anchor view.
	 * @param yoff Popup menu show y offset the anchor view.
	 * @return This PopupMessageBuilder object
	 */
	public PopupContentBuilder show(View anchor, int xoff, int yoff) {
		if (null != mContainerWindow) {
			mContainerWindow.showAsDropDown(anchor, xoff, yoff);
		}
		return this;
	}
	
	/**
	 * Dismiss this popup menu. see also {@link PopupWindow#dismiss()}.
	 * 
	 * @return This PopupMessageBuilder object
	 */
	public PopupContentBuilder dismiss() {
		if (null != mContainerWindow) {
			mContainerWindow.dismiss();
		}
		return this;
	}
	
	/**
	 * Set popup menu background. see also {@link PopupWindow#setBackgroundDrawable(Drawable)}.
	 * 
	 * @param bkDrawable Background {@link Drawable}.
	 * @return This PopupMessageBuilder object
	 */
	public PopupContentBuilder setBackground(Drawable bkDrawable) {
		if (null != mContainerWindow && null != bkDrawable) {
			mContainerWindow.setBackgroundDrawable(bkDrawable);
		}
		return this;
	}
	
	/**
	 * Get popup menu container window.
	 * 
	 * @return This PopupMenu {@link PopupWindow} object.
	 */
	public PopupWindow create() {
		return mContainerWindow;
	}
	
	/**
	 * Get popup menu content view.
	 * 
	 * @return This PopupMenu content view.
	 */
	public View getContentView() {
		return mContentView;
	}

}

	
