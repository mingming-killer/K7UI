package com.eebbk.mingming.k7ui.demo;

import java.util.ArrayList;

import com.eebbk.mingming.k7ui.bounceview.BounceHorizontalScrollView;
import com.eebbk.mingming.k7ui.bounceview.BounceListView;
import com.eebbk.mingming.k7ui.bounceview.BounceScrollView;
import com.eebbk.mingming.k7ui.bounceview.BounceGridView;
import com.eebbk.mingming.k7ui.DragTextView;
import com.eebbk.mingming.k7utils.AppUtils;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class BounceListViewActivity extends Activity implements OnClickListener {

	@SuppressWarnings("unused")
	private final static String TAG = "BounceListViewActivity";
	
	private String mTestStr;
	
	private ToggleButton mBtnIsMore;
	private Button mBtnChanageView;
	
	private BounceScrollView mScrollView;
	private TextView mScrollTv;
	
	private ArrayList<String> mListDatas;
	private BounceListView mListView;
	private ListAdapter mListAdapter;
	
	private BounceHorizontalScrollView mHScrollView;
	private TextView mHScrollTv;
	
	private ArrayList<String> mGridDatas;
	private BounceGridView mGridView;
	private GridAdapter mGridAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.bounce_list_view_activity);
		
		initConfig();
		initView();
		
		changeContentData(true);
		onBtnChangeViewClick(mBtnChanageView);
	}
	
	private void initConfig() {
		mTestStr = AppUtils.getStringSafely(this, R.string.test_large_text, "more ");
		
		mListDatas = new ArrayList<String> ();
		mListAdapter = new ListAdapter(this, mListDatas);
		
		mGridDatas = new ArrayList<String> ();
		mGridAdapter = new GridAdapter(this, mGridDatas);
	}
	
	private void initView() {
		mBtnIsMore = (ToggleButton) findViewById(R.id.toggle_is_more);
		mBtnChanageView = (Button) findViewById(R.id.btn_change_view);
		
		mScrollView = (BounceScrollView) findViewById(R.id.scroll_bounce);
		mScrollTv = (TextView) findViewById(R.id.scroll_tv_large);
		
		mListView = (BounceListView) findViewById(R.id.lv_bounce);
		
		mHScrollView = (BounceHorizontalScrollView) findViewById(R.id.h_scroll_bounce);
		mHScrollTv = (TextView) findViewById(R.id.h_scroll_tv_large);
		
		mGridView = (BounceGridView) findViewById(R.id.gv_bounce);
		
		mListView.setAdapter(mListAdapter);
		mGridView.setAdapter(mGridAdapter);
		
		ColorDrawable drTran = new ColorDrawable(0x00ffffff);
		mScrollView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		mScrollView.setDefaultEdgeEffect(drTran, drTran, drTran, drTran);
		
		mListView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		mListView.setDefaultEdgeEffect(drTran, drTran, drTran, drTran);
		
		mHScrollView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		mHScrollView.setDefaultEdgeEffect(drTran, drTran, drTran, drTran);
		
		mGridView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		mGridView.setDefaultEdgeEffect(drTran, drTran, drTran, drTran);
		
		mBtnIsMore.setOnClickListener(this);
		mBtnIsMore.setChecked(true);
		mBtnChanageView.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (null != mListDatas) {
			mListDatas.clear();
		}
		
		if (null != mGridDatas) {
			mGridDatas.clear();
		}
	}
	
	@Override
	public void onClick(View view) {
		if (view.equals(mBtnIsMore)) {
			onBtnIsMoreClick(mBtnIsMore);
		} else if (view.equals(mBtnChanageView)) {
			onBtnChangeViewClick(mBtnChanageView);
		}
	}
	
	private void onBtnIsMoreClick(ToggleButton toggle) {
		if (null == toggle) {
			return;
		}
		
		changeContentData(toggle.isChecked());
	}
	
	private void onBtnChangeViewClick(View view) {
		int changeMode = 0;
		try {
			changeMode = (Integer) view.getTag();
		} catch (Exception e) {
			changeMode = 0;
		}
		
		if (0 == changeMode) {
			mScrollView.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.VISIBLE);
			mHScrollView.setVisibility(View.GONE);
			mGridView.setVisibility(View.GONE);
		} else {
			mScrollView.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			mHScrollView.setVisibility(View.VISIBLE);
			mGridView.setVisibility(View.VISIBLE);
		}
		
		changeMode += 1;
		if (changeMode > 1) {
			changeMode = 0;
		}
		
		view.setTag(changeMode);
	}
	
	private void changeContentData(boolean isMore) {
		if (null != mScrollTv) {
			if (isMore) {
				mScrollTv.setText(R.string.test_very_large_text);
			} else {
				mScrollTv.setText(R.string.test_large_text);
			}
		}
		
		if (null != mListDatas) {
			mListDatas.clear();
			int total = 5;
			if (isMore) {
				total = 50;
			}
			
			for (int i = 0; i < total; i++) {
				mListDatas.add(i + " : " + mTestStr);
			}
			
			if (null != mListAdapter) {
				mListAdapter.notifyDataSetChanged();
			}
		}
		
		
		if (null != mHScrollTv) {
			if (isMore) {
				mHScrollTv.setText(R.string.test_very_large_text);
			} else {
				mHScrollTv.setText("This is just a small text view haha !");
			}
		}
		
		if (null != mGridDatas) {
			mGridDatas.clear();
			int total = 5;
			if (isMore) {
				total = 50;
			}
			
			for (int i = 0; i < total; i++) {
				mGridDatas.add(i + " : " + " Grid Item");
			}
			
			if (null != mGridAdapter) {
				mGridAdapter.notifyDataSetChanged();
			}
		}
	}
	
	
	class ListAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private ArrayList<String> mDatas;
		
		public ListAdapter(Context context, ArrayList<String> datas) {
			super();
			
			mInflater = LayoutInflater.from(context);
			mDatas = datas;
		}
		
		@Override
		public int getCount() {
			if (null == mDatas) {
				return 0;
			}
			
			return mDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return mDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewGroup container = null;
			TextView tvInfo = null;
			
			if (null == convertView) {
				convertView = (ViewGroup) mInflater.inflate(R.layout.drag_text_item, null);
				// 设置 item 的高度
				if (null != convertView) {
					AbsListView.LayoutParams params = null;
					try {
						params = (AbsListView.LayoutParams) convertView.getLayoutParams();
					} catch (Exception e) {
						e.printStackTrace();
						params = null;
					}
					
					if (null == params) {
						params = new AbsListView.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT, 60);
					} else {
						params.height = 60;
					}
					convertView.setLayoutParams(params); 
				}
			}
			
			container = (ViewGroup) convertView;
			container.setTag(position);
			
			tvInfo = (DragTextView) container.findViewById(R.id.item_tv);
			
			if (null == mDatas || 
					position < 0 || position >= mDatas.size()) {
				return convertView;
			}
			
			String data = mDatas.get(position);
			if (null == data) {
				return convertView;
			}
			
			if (0 == position % 2) {
				tvInfo.setText(data);
			} else {
				tvInfo.setText("small text can't drag");
			}
			
			return convertView;
		}
		
	}
	
	class GridAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private ArrayList<String> mDatas;
		
		public GridAdapter(Context context, ArrayList<String> datas) {
			super();
			
			mInflater = LayoutInflater.from(context);
			mDatas = datas;
		}
		
		@Override
		public int getCount() {
			if (null == mDatas) {
				return 0;
			}
			
			return mDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return mDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressWarnings("unused")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View container = null;
			TextView tvInfo = null;
			
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.grid_item, null);
			}
			
			if (null == mDatas || 
					position < 0 || position >= mDatas.size()) {
				return convertView;
			}
			
			tvInfo = (TextView) container;
			
			String data = mDatas.get(position);
			if (null == data) {
				return convertView;
			}
			
			if (null != tvInfo) {
				tvInfo.setText(data);
			}
			
			return convertView;
		}
		
	}

}
