package com.eebbk.mingming.k7ui.demo;

import java.util.ArrayList;
import java.util.Random;

import com.eebbk.mingming.k7ui.effect.view.EffectPageContainer;
import com.eebbk.mingming.k7ui.effect.view.EffectPageContainer.EffectPageContainerListener;
import com.eebbk.mingming.k7ui.demo.R;
import com.eebbk.mingming.k7ui.effector.CurlEffectorGL;
import com.eebbk.mingming.k7ui.effector.Effector;
import com.eebbk.mingming.k7ui.effector.ScrollEffectorGL;
import com.eebbk.mingming.k7utils.AppUtils;
import com.eebbk.mingming.k7utils.LogUtils;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GLEffectPageContainerActivity extends Activity implements 
	OnClickListener, OnCheckedChangeListener, OnItemSelectedListener, 
	OnSeekBarChangeListener, EffectPageContainerListener {

	private final static String TAG = "EffectPageContainerActivity";
	
	private final static int ID_FIRST_CHOOSE = -100;
	private final static int MIN_DURATION = 300;
	
	private final static String[] TEST_DATAS = {
		"haha0",
		"haha1",
		"haha2",
		"haha3",
		"haha4",
		"haha5",
		//"haha6",
		//"haha7",
		//"haha8",
		//"haha9",
		//"haha10",
		//"haha11",
		//"haha12",
		//"haha13",
		//"haha14",
		//"haha15",
		//"haha16",
	};
	
	
	private final static int ID_EFFECT_NONE = 0;
	private final static int ID_EFFECT_SCROLL_GL  = 1;
	private final static int ID_EFFECT_CURL_GL = 2;
	
	private final static String[] EFFECTS = {
		"None",
		"ScrollGL",
		"CurlGL",
	};
	
	private int mCurrentEffect;
	
	
	private final static int ID_TYPE_HORIZONTAL = 0;
	private final static int ID_TYPE_VERTICAL = 1;
	
	private final static String[] TYPES = {
		"Horizontal",
		"Vertical",
	};
	
	private int mCurrentType;
	
	
	private Random mRandom;
	private String mTestStr;
	private ArrayList<String> mTestDatas;
	
	private ArrayAdapter<String> mTypeAdapter;
	private ArrayAdapter<String> mEffectAdapter;
	
	//private Effector mEffector;
	
	private Button mBtnPrev;
	private Button mBtnNext;
	private Button mBtnAdd;
	private Button mBtnDelete;
	private Button mBtnUpdate;
	private SeekBar mSbDuration;
	
	private Spinner mSpEffect;
	private Spinner mSpType;
	private CheckBox mCbLoop;
	
	private EffectPageContainer mPageContainer;
	private PageAdapter mPagerAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gl_effect_page_container_activity);
		
		initConfig();
		
		initView();
	}
	
	private void initConfig() {
		mRandom = new Random(System.currentTimeMillis());
		mTestStr = AppUtils.getStringSafely(this, R.string.test_large_text, "more ");
		
		mTestDatas = new ArrayList<String> ();
		for (String data : TEST_DATAS) {
			mTestDatas.add(data + "\n" + mTestStr);
		}
		
		mEffectAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item,
				EFFECTS);
		
		mTypeAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item,
				TYPES);
		
		mPagerAdapter = new PageAdapter(this, mTestDatas);
		
		mCurrentEffect = ID_FIRST_CHOOSE;
		mCurrentType = ID_FIRST_CHOOSE;
	}
	
	private void initView() {
		mBtnPrev = (Button) findViewById(R.id.btn_prev);
		mBtnNext = (Button) findViewById(R.id.btn_next);
		mBtnAdd = (Button) findViewById(R.id.btn_add);
		mBtnDelete = (Button) findViewById(R.id.btn_delete);
		mBtnUpdate = (Button) findViewById(R.id.btn_update);
		mSbDuration = (SeekBar) findViewById(R.id.sb_duration);
		
		mSpEffect = (Spinner) findViewById(R.id.sp_effect);
		mSpType = (Spinner) findViewById(R.id.sp_type);
		mCbLoop = (CheckBox) findViewById(R.id.cb_loop);
		
		mPageContainer = (EffectPageContainer) findViewById(R.id.page_container);
		
		mPageContainer.setAdapter(mPagerAdapter);
		mSpEffect.setAdapter(mEffectAdapter);
		mSpType.setAdapter(mTypeAdapter);
		
		mPageContainer.setBkColor(0x00ffffff);
		mPageContainer.setOverScrollDrawable(new ColorDrawable(0xff0000ff));
		
		mPageContainer.setEffectPageContainerListener(this);
		//mPageContainer.setScrollLevel(EffectPageContainer.SCROLL_LEVEL_NONE);
		mPageContainer.setSwitchAnimInterpolator(new AccelerateInterpolator());
		
		mBtnPrev.setOnClickListener(this);
		mBtnNext.setOnClickListener(this);
		mBtnAdd.setOnClickListener(this);
		mBtnDelete.setOnClickListener(this);
		mBtnUpdate.setOnClickListener(this);
		mSbDuration.setOnSeekBarChangeListener(this);
		
		mSpEffect.setOnItemSelectedListener(this);
		mSpType.setOnItemSelectedListener(this);
		mCbLoop.setOnCheckedChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (null != mPageContainer) {
			mPageContainer.onPause();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (null != mPageContainer) {
			mPageContainer.onResume();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (null != mPageContainer) {
			Effector effector = mPageContainer.getEffector();
			if (null != effector && effector instanceof CurlEffectorGL) {
				if (Configuration.ORIENTATION_PORTRAIT == newConfig.orientation) {
					effector.setEffectType(CurlEffectorGL.SHOW_ONE_PAGE);
				} else {
					effector.setEffectType(CurlEffectorGL.SHOW_TWO_PAGE);
				}
			}
			mPageContainer.refreshCurrentPage();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (null != mPageContainer) {
			mPageContainer.free();
		}
		
		if (null != mTestDatas) {
			mTestDatas.clear();
		}
	}
	
	@Override
	public void onClick(View view) {
		if (view.equals(mBtnPrev)) {
			onBtnPageChange(false);
		} else if (view.equals(mBtnNext)) {
			onBtnPageChange(true);
		} else if (view.equals(mBtnAdd)) {
			onBtnAdd();
		} else if (view.equals(mBtnDelete)) {
			onBtnDelete();
		} else if (view.equals(mBtnUpdate)) {
			//onBtnUpdate();
			mPageContainer.refreshEffectView();
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		if (view.equals(mCbLoop)) {
			onChangeLoop(isChecked);
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.equals(mSpEffect)) {
			onChangeEffect(position);
		} else if (parent.equals(mSpType)) {
			onChangeType(position);
		}
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int duration = progress + MIN_DURATION;
		mPageContainer.setSwitchAnimDuration(duration);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onEffectPageChangePage(View view, int currentPage) {
		LogUtils.d(TAG, "chang page to: " + currentPage);
	}

	@Override
	public void onEffectPagePreparePage(int targetPage) {
		LogUtils.d(TAG, "prepare page: " + targetPage);
	}
	
	@Override
	public void onEffectPageChangeOrientation(int orientation) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onEffectPageChangeState(int newState) {
		LogUtils.d(TAG, "change state: " + newState);
	}
	
	private void onBtnPageChange(boolean toNext) {
		if (toNext) {
			mPageContainer.showNext(true);
		} else {
			mPageContainer.showPrevious(true);
		}
	}
	
	private void onBtnAdd() {
		String newData = "newData" + mTestDatas.size() 
				+ "\n" + mTestStr;
		mTestDatas.add(newData);
		mPagerAdapter.notifyDataSetChanged();
	}
	
	private void onBtnDelete() {
		if (mTestDatas.isEmpty()) {
			return;
		}
		mTestDatas.remove(0);
		mPagerAdapter.notifyDataSetChanged();
	}
	
	private void onBtnUpdate() {
		if (mTestDatas.size() < 2) {
			return;
		}
		
		String newData = "newData" + 1 + ", " + mRandom.nextInt(9999)
				+ "\n" + mTestStr;
		mTestDatas.set(1, newData);
		mPagerAdapter.notifyDataSetChanged();
	}
	
	private void onChangeLoop(boolean looped) {
		mPageContainer.setLoopShow(looped);
	}
	
	private void onChangeEffect(int position) {
		if (ID_FIRST_CHOOSE == mCurrentEffect) {
			mCurrentEffect = position;
			return;
		}
		
		mCurrentEffect = position;
		Effector effector = null;		
		
		switch (mCurrentEffect) {
		case ID_EFFECT_NONE:
			effector = null;
			break;
			
		case ID_EFFECT_SCROLL_GL:
			effector = new ScrollEffectorGL();
			break;
			
		
		case ID_EFFECT_CURL_GL:
			effector = new CurlEffectorGL();
			break;
			
		default:
			break;
		}
		
		if (null != effector) {
			configuEffector(effector, getResources().getConfiguration().orientation);
			mPageContainer.setEffector(effector);
		}
	}
	
	private void configuEffector(Effector effector, int orientation) {
		if (null == effector) {
			return;
		}
		
		effector.setHighQuality(true);
		
		CurlEffectorGL curlEffector = null;
		
		if (Configuration.ORIENTATION_PORTRAIT == orientation) {			
			if (effector instanceof CurlEffectorGL) {
				//mTvSrc1.setBackgroundResource(R.drawable.test_1);
				//mTvSrc1.setVisibility(View.VISIBLE);
				//mTvSrcLeft.setVisibility(View.GONE);
				//mTvSrcRight.setVisibility(View.GONE);
				
				//mTvDst1.setBackgroundResource(R.drawable.test_2);
				//mTvDst1.setVisibility(View.VISIBLE);
				//mTvDstLeft.setVisibility(View.GONE);
				//mTvDstRight.setVisibility(View.GONE);
				
				curlEffector = (CurlEffectorGL) effector;
				curlEffector.setBkColor(0xffffffff);
				//curlEffector.setMargins(0.15f, 0.1f, 0.15f, 0.1f);
				curlEffector.setEffectType(CurlEffectorGL.SHOW_ONE_PAGE);
			} else {
				//mTvSrc1.setBackgroundResource(R.drawable.test_bk);
				//mTvSrc1.setVisibility(View.VISIBLE);
				//mTvSrcLeft.setVisibility(View.GONE);
				//mTvSrcRight.setVisibility(View.GONE);
				
				//mTvDst1.setBackgroundResource(R.drawable.test_bk);
				//mTvDst1.setVisibility(View.VISIBLE);
				//mTvDstLeft.setVisibility(View.GONE);
				//mTvDstRight.setVisibility(View.GONE);
			}
		} else {
			if (effector instanceof CurlEffectorGL) {
				//mTvSrcLeft.setVisibility(View.VISIBLE);
				//mTvSrcRight.setVisibility(View.VISIBLE);
				//mTvSrc1.setVisibility(View.GONE);
				
				//mTvDstLeft.setVisibility(View.VISIBLE);
				//mTvDstRight.setVisibility(View.VISIBLE);
				//mTvDst1.setVisibility(View.GONE);
				
				curlEffector = (CurlEffectorGL) effector;
				//curlEffector.setMargins(0.2f, 0.05f, 0.2f, 0.05f);
				curlEffector.setBkColor(0xffffffff);
				curlEffector.setEffectType(CurlEffectorGL.SHOW_TWO_PAGE);
			} else {
				//mTvSrc1.setBackgroundResource(R.drawable.test_bk);
				//mTvSrc1.setVisibility(View.VISIBLE);
				//mTvSrcLeft.setVisibility(View.GONE);
				//mTvSrcRight.setVisibility(View.GONE);
				
				//mTvDst1.setBackgroundResource(R.drawable.test_bk);
				//mTvDst1.setVisibility(View.VISIBLE);
				//mTvDstLeft.setVisibility(View.GONE);
				//mTvDstRight.setVisibility(View.GONE);
			}
		}
	}
	
	private void onChangeType(int position) {
		if (ID_FIRST_CHOOSE == mCurrentType) {
			mCurrentType = position;
			return;
		}
		
		switch (position) {
		case ID_TYPE_HORIZONTAL:
			mCurrentType = EffectPageContainer.HORIZONTAL;
			break;
			
		case ID_TYPE_VERTICAL:
			mCurrentType = EffectPageContainer.VERTICAL;
			break;
		
		default:
			break;
		}
		
		mPageContainer.setOrientation(mCurrentType);
	}
	
	
	private class PageAdapter extends BaseAdapter implements OnClickListener {
		
		private LayoutInflater mInflater;
		private ArrayList<String> mDatas;
		
		public PageAdapter(Context context, ArrayList<String> datas) {
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
			if (null == mDatas || 
					position < 0 || position >= mDatas.size()) {
				return null;
			}
			
			return mDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View container;
			TextView tvInfo;
			Button btnInfo;
			
			if (convertView == null) {
				container = mInflater.inflate(R.layout.page_item_scroll_v, null);
			}
			else {
				container = convertView;
			}
			
			if (null == container) {
				return convertView;
			}
			
			container.setTag(position);
			
			if (0 == position % 2) {
				container.setBackgroundColor(0xffff0000);
			} else {
				container.setBackgroundColor(0xffffff00);
			}
			
			tvInfo = (TextView) container.findViewById(R.id.page_item_tv);
			btnInfo = (Button) container.findViewById(R.id.page_item_btn);
			
			if (null == mDatas || 
					position < 0 || position >= mDatas.size()) {
				return null;
			}
			
			String data = mDatas.get(position);
			if (null == data) {
				return convertView;
			}
			
			if (null != tvInfo) {
				tvInfo.setText(data);
			}
			
			if (null != btnInfo) {
				btnInfo.setText("Btn " + position);
				btnInfo.setOnClickListener(this);
			}
			
			return container;
		}
		
		@Override
		public void onClick(View view) {
			Button btn = (Button) view;
			Toast.makeText(GLEffectPageContainerActivity.this, btn.getText(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
}
