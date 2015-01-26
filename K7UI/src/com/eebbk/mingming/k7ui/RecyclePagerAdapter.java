package com.eebbk.mingming.k7ui;

import java.util.LinkedList;

import com.eebbk.mingming.k7utils.LogUtils;

import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * This PagerAdapter is extends {@link CoolPagerAdapter}, 
 * and provider a view recycling mechanism. </p>
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public abstract class RecyclePagerAdapter extends CoolPagerAdapter {
	
	private final static String TAG = "RecyclePagerAdapter";
	
	// View cache pool
	protected LinkedList<View> mViewPool;
	
    
    public RecyclePagerAdapter() {
        mViewPool = new LinkedList<View> ();
    }
    
    @Override
    public Object instantiateItem(ViewGroup container, int position) {    	
    	View convertView = getView();
    	if (null == convertView) {
    		LogUtils.d(TAG, "get view is null !");
    		return null;
    	}
    	
    	setupView(convertView, position);
    	
    	container.addView(convertView);
    	
    	return convertView;
    }
    
	@Override
	public void updateItem(ViewGroup container, int position, Object object) {		
		View convertView = null;
		try {
			convertView = (View) object;
		} catch (Exception e) {
			e.printStackTrace();
			convertView = null;
		}
		
		if (null == convertView) {
			LogUtils.d(TAG, "give item view is null, we can't update it !");
			return;
		}
		
		setupView(convertView, position);
	}
    
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {		
		View convertView = null;
		try {
			convertView = (View) object;
		} catch (Exception e) {
			e.printStackTrace();
			convertView = null;
		}
		
		if (null == convertView) {
			LogUtils.d(TAG, "recycle object is null, we can't recycle !");
			return;
		}
		
		container.removeView(convertView);
		
		// put destroy view to view pool.
		if (null != mViewPool) {
			mViewPool.offer(convertView);
		}
	}
	
	/**
	 * Free resource.
	 */
	public void free() {
		if (null != mViewPool) {
			mViewPool.clear();
		}
	}
	
	/**
	 * Get a item view.(Will use cache pool)
	 * 
	 * @return Object of item view (may be dirty).
	 */
	protected View getView() {
		View convertView = null;
		
		do {
			// if don't have pool, we create it.
			if (null == mViewPool) {
				mViewPool = new LinkedList<View> ();
				convertView = createNewView();
				break;
			}
			
			// get view from view pool.
			convertView = mViewPool.poll();
			if (null != convertView) {
				break;
			}
			
			// if pool don't have view cache, we create it.
			convertView  = createNewView();
			
		} while (false);
		
		return convertView;
	}
	
	/**
	 * Create a new item view.
	 * You must override this method to implement create your item view. 
	 * 
	 * @return Object of item view.
	 */
	protected abstract View createNewView();
	
	/**
	 * Setup you item view, when some item view need update, 
	 * you should setup it though adapter data set.
	 * 
	 * @param convertView Your item view.
	 * @param position Item view position.
	 */
	protected abstract void setupView(View convertView, int position);
		
}
