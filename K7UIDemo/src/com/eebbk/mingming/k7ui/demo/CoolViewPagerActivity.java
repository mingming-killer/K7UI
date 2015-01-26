package com.eebbk.mingming.k7ui.demo;

import java.util.ArrayList;
import java.util.Random;

import com.eebbk.mingming.k7ui.CoolViewPager;
import com.eebbk.mingming.k7ui.RecyclePagerAdapter;
import com.eebbk.mingming.k7utils.AppUtils;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CoolViewPagerActivity extends Activity implements OnClickListener {

	@SuppressWarnings("unused")
	private final static String TAG = "CoolViewPagerActivity";
	
	private final static String[] TEST_DATAS_MORE = {
		"haha1",
		"haha2",
		"haha3",
		"haha4",
		"haha5",
		"haha6",
		"haha7",
		"haha8",
		"haha9",
		"haha10",
		"haha11",
		"haha12",
		"haha13",
		"haha14",
		"haha15",
		"haha16",
		"haha17",
	};
	
	private final static String[] TEST_DATAS_LESS = {
		"No1",
		"No2",
		"No3",
		"No3",
		"No4",
		"No5",
		"No6",
		"No7",
	};
	
	private String mTestStr;
	private Random mRandom;
	private ArrayList<String> mTestDatas;
	
	private Button mBtnUpdate;
	private Button mBtnChange;
	
	CoolViewPager mViewPager;
	SimpleAdapter mPagerAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cool_view_pager_activity);
		
		mTestStr = AppUtils.getStringSafely(this, R.string.test_large_text, "more ");
		
		mViewPager = (CoolViewPager) findViewById(R.id.view_pager);
		mBtnUpdate = (Button) findViewById(R.id.btn_update);
		mBtnChange = (Button) findViewById(R.id.btn_change);
		
		mRandom = new Random(System.currentTimeMillis());
		
		mTestDatas = new ArrayList<String> ();
		mPagerAdapter = new SimpleAdapter(this, mTestDatas);
		mViewPager.setAdapter(mPagerAdapter);
		
		mBtnUpdate.setOnClickListener(this);
		mBtnChange.setOnClickListener(this);
		
		onBtnChange();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (null != mPagerAdapter) {
			mPagerAdapter.free();
		}
		
		if (null != mTestDatas) {
			mTestDatas.clear();
		}
	}
	
	@Override
	public void onClick(View view) {
		if (view.equals(mBtnUpdate)) {
			onBtnUpdate();
		} else if (view.equals(mBtnChange)) {
			onBtnChange();
		}
	}
	
	private void onBtnUpdate() {
		String newStr = String.format("New:%d", mRandom.nextInt(9999));
		mTestDatas.set(3, newStr);
		
		mPagerAdapter.notifyDataSetChanged();
	}
	
	private void onBtnChange() {
		if (mPagerAdapter.getCount() == TEST_DATAS_MORE.length) {
			mTestDatas.clear();
			for (String data : TEST_DATAS_LESS) {
				mTestDatas.add(data + "\n" + mTestStr);
			}
		} else {
			mTestDatas.clear();
			for (String data : TEST_DATAS_MORE) {
				mTestDatas.add(data + "\n" + mTestStr);
			}
		}
		
		mPagerAdapter.notifyDataSetChanged();
	}
	
	
	class SimpleAdapter extends RecyclePagerAdapter {
		
		private LayoutInflater mInflater;
		private ArrayList<String> mDatas;
		
		public SimpleAdapter(Context context, ArrayList<String> datas) {
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
		protected View createNewView() {
			//TextView convertView = new TextView(CoolViewPagerActivity.this);
			//return convertView;
			View convertView = mInflater.inflate(R.layout.page_item_scroll_v, null);
			return convertView;
		}

		@Override
		protected void setupView(View convertView, int position) {
			if (null == convertView) {
				return;
			}
			
			convertView.setTag(position);
			
			if (0 == position % 2) {
				convertView.setBackgroundColor(0xffff0000);
			} else {
				convertView.setBackgroundColor(0xff00ff00);
			}
			
			TextView tvInfo = (TextView) convertView.findViewById(R.id.page_item_tv);
			Button btnInfo = (Button) convertView.findViewById(R.id.page_item_btn);
			
			if (null == mDatas || 
					position < 0 || position >= mDatas.size()) {
				return;
			}
			
			String data = mDatas.get(position);
			if (null == data) {
				return;
			}
			
			if (null != tvInfo) {
				tvInfo.setText(data);
			}
			
			if (null != btnInfo) {
				btnInfo.setText("Btn " + position);
			}
		}
		
	}

}
