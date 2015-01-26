package com.eebbk.mingming.k7ui.demo;

import com.eebbk.mingming.k7ui.effector.CurlEffectorGL;
import com.eebbk.mingming.k7ui.effector.Effector;
import com.eebbk.mingming.k7ui.effector.GLEffectView;
import com.eebbk.mingming.k7ui.effector.ScrollEffectorGL;
import com.eebbk.mingming.k7utils.AppUtils;

import android.os.Bundle;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class GLEffectViewActivity extends Activity implements OnClickListener, 
	OnSeekBarChangeListener, OnItemSelectedListener, OnCheckedChangeListener, AnimatorListener {

	@SuppressWarnings("unused")
	private final static String TAG = "HardwareEffectViewActivity";
	
	
	private final static int ID_EFFECTOR_SCROLL = 0;
	private final static int ID_EFFECTOR_CURL = 1;
	
	private final static String[] EFFECTORS = {
		"ScrollEffectorGL",
		"CurlEffectorGL",
	};
	
	
	private final static int ID_TYPE_HORIZONTAL = 0;
	private final static int ID_TYPE_VERTICAL = 1;
	
	private final static String[] TYPES = {
		"Horizontal",
		"Vertical",
	};
	
	
	private final static int ID_FILL_BMP = 0;
	private final static int ID_FILL_9_PATCH = 1;
	private final static int ID_FILL_COLOR = 2;
	private final static int ID_FILL_GRADIENT = 3;
	
	private final static String[] FILLS = {
		"Bitmap",
		"9-Patch",
		"Color",
		"Gradient",
	};
	
	private Drawable mDrFillBmp;
	private Drawable mDrFill9Patch;
	private Drawable mDrFillColor;
	private Drawable mDrFillGradient;
	
	private ArrayAdapter<String> mEffectorAdapter;
	private ArrayAdapter<String> mTypeAdapter;
	private ArrayAdapter<String> mFillAdapter;
	
	private ObjectAnimator mAnimator;
	
	private LayoutInflater mInflater;
	
	private Spinner mSpEffector;
	private Spinner mSpType;
	private Spinner mSpFill;
	
	private CheckBox mCbHighQuality;
	private CheckBox mCbReverse;
	private CheckBox mCbFill;
	private CheckBox mCbShow;
	private Button mBtnCapture;
	private SeekBar mSbEffectFactor;
	
	private View mSrcView = null;
	private TextView mTvSrc1 = null;
	private TextView mTvSrcLeft = null;
	private TextView mTvSrcRight = null;
	private View mDstView = null;
	private TextView mTvDst1 = null;
	private TextView mTvDstLeft = null;
	private TextView mTvDstRight = null;
	private ViewGroup mEffectContainer;
	
	private GLEffectView mEffectView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gl_effect_view_activity);
		
		initConfig();
		
		initView();
	}
	
	private void initConfig() {
		mEffectorAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item,
				EFFECTORS);
		
		mTypeAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item,
				TYPES);
		
		mFillAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item,
				FILLS);
		
		mDrFillBmp = AppUtils.getDrawableSafely(this, R.drawable.test_fill_bmp, null);
		mDrFill9Patch = AppUtils.getDrawableSafely(this, R.drawable.test_fill_9, null);
		//mDrFillColor = AppUtils.getDrawableSafely(this, R.color.test_fill_color, null);
		mDrFillColor = new ColorDrawable(0x00ffffff);
		mDrFillGradient = AppUtils.getDrawableSafely(this, R.drawable.test_fill_gradient, null);
		
		mAnimator = ObjectAnimator.ofFloat(null, Effector.PROPERTY_NAME, 0f, 1f);
		mAnimator.setDuration(3000);
		mAnimator.addListener(this);
	}
	
	private void initView() {
		mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		
		mSpEffector = (Spinner) findViewById(R.id.sp_effector);
		mSpType = (Spinner) findViewById(R.id.sp_type);
		mSpFill = (Spinner) findViewById(R.id.sp_fill);
		mSbEffectFactor = (SeekBar) findViewById(R.id.sb_effect_factor);
		
		mCbHighQuality = (CheckBox) findViewById(R.id.cb_high_quality);
		mCbReverse = (CheckBox) findViewById(R.id.cb_reverse);
		mCbFill = (CheckBox) findViewById(R.id.cb_fill);
		mCbShow = (CheckBox) findViewById(R.id.cb_show);
		mBtnCapture = (Button) findViewById(R.id.btn_capture);
		
		mSrcView = findViewById(R.id.src_view);
		mTvSrc1 = (TextView) mSrcView.findViewById(R.id.ev_test_one);
		mTvSrcLeft = (TextView) mSrcView.findViewById(R.id.ev_test_left);
		mTvSrcRight = (TextView) mSrcView.findViewById(R.id.ev_test_right);
		mDstView = findViewById(R.id.dst_view);
		mTvDst1 = (TextView) mDstView.findViewById(R.id.ev_test_one);
		mTvDstLeft =  (TextView) mDstView.findViewById(R.id.ev_test_left);
		mTvDstRight = (TextView) mDstView.findViewById(R.id.ev_test_right);
		
		mTvSrc1.setText("Source Image View");
		mTvSrc1.setTextColor(0xffff0000);
		mTvSrcLeft.setText("Src Left View");
		mTvSrcLeft.setBackgroundResource(R.drawable.test_3_1);
		mTvSrcRight.setText("Src Right View");
		mTvSrcRight.setBackgroundResource(R.drawable.test_3_2);
		
		mTvDst1.setText("Target Image View");
		mTvDst1.setTextColor(0xff00ff00);
		mTvDstLeft.setText("Dst Left View");
		mTvDstLeft.setBackgroundResource(R.drawable.test_4_1);
		mTvDstRight.setText("Dst Right View");
		mTvDstRight.setBackgroundResource(R.drawable.test_4_2);
		
		mEffectContainer = (ViewGroup) findViewById(R.id.effect_view_container);
		
		mSpEffector.setAdapter(mEffectorAdapter);
		mSpType.setAdapter(mTypeAdapter);
		mSpFill.setAdapter(mFillAdapter);
		
		mCbHighQuality.setOnCheckedChangeListener(this);
		mCbReverse.setOnCheckedChangeListener(this);
		mCbFill.setOnCheckedChangeListener(this);
		mSbEffectFactor.setOnSeekBarChangeListener(this);
		mSpEffector.setOnItemSelectedListener(this);
		mSpType.setOnItemSelectedListener(this);
		mCbShow.setOnClickListener(this);
		mBtnCapture.setOnClickListener(this);
		
		//onChangeEffector(0);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (null != mEffectView) {
			mEffectView.free();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (null != mEffectView) {
			mEffectView.onPause();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (null != mEffectView) {
			mEffectView.onResume();
			mEffectView.captureImage(Effector.ID_SRC | Effector.ID_DST);
			mEffectView.invalidate();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (null != mEffectView) {
			configuEffector(mEffectView.getEffector(), newConfig.orientation);
			//mEffectView.captureImage(Effector.ID_SRC | Effector.ID_DST);
			//mEffectView.invalidate();
		}
	}
	
	@Override
	public void onClick(View view) {
		if (view.equals(mCbShow)) {
			onCbShow();
		} else if (view.equals(mBtnCapture)) {
			onBtnCapture();
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_MENU == keyCode) {
			startAnim();
			return true;
		}
		
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (null == mEffectView) {
			return;
		}
		float factor = (float)progress / (float)seekBar.getMax();
		mEffectView.setEffectFactor(factor);
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
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.equals(mSpEffector)) {
			onChangeEffector(position);
		} else if (parent.equals(mSpType)) {
			onChangeType(position);
		}
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		if (view.equals(mCbHighQuality)) {
			onChangeHighQuality(isChecked);
		} else if (view.equals(mCbReverse)) {
			onChangeReverse(isChecked);
		} else if (view.equals(mCbFill)) {
			onChangeFill(isChecked);
		}
	}
	
	@Override
	public void onAnimationStart(Animator animation) {
		mSrcView.setVisibility(View.INVISIBLE);
		mDstView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		mSrcView.setVisibility(View.INVISIBLE);
		mDstView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onAnimationCancel(Animator animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		// TODO Auto-generated method stub
		
	}
	
	private void onCbShow() {
		if (mCbShow.isChecked()) {
			mSrcView.setVisibility(View.VISIBLE);
			mDstView.setVisibility(View.INVISIBLE);
		} else {
			mSrcView.setVisibility(View.INVISIBLE);
			mDstView.setVisibility(View.INVISIBLE);
		}
	}
	
	private void onBtnCapture() {
		mEffectView.captureImage(Effector.ID_SRC | Effector.ID_DST);
		mEffectView.invalidate();
	}
	
	private void onChangeEffector(int index) {
		Effector effector = null;
		
		switch (index) {
		case ID_EFFECTOR_SCROLL:
			effector = new ScrollEffectorGL();
			break;
			
		case ID_EFFECTOR_CURL:
			effector = new CurlEffectorGL();
			break;
		
		default:
			break;
		}
		
		if (null != effector) {
			configuEffector(effector, getResources().getConfiguration().orientation);
			changeEffector(effector);
		}
	}
	
	private void changeEffector(Effector effector) {
		mEffectContainer.removeAllViews();
		if (null != mEffectView) {
			mEffectView.free();
		}
		
		// the hardware effect view don't support dynamic change effector.
		// because hardware effect view is base SufarceView, is must be re-create to change effector.
		int evLayout = com.eebbk.mingming.k7ui.R.layout.k7ui_evgl;
		if (effector.supportTranBk()) {
			//mSrcView.setBackgroundResource(R.drawable.test_bk);
			//mDstView.setBackgroundResource(R.drawable.test_bk);
			evLayout = com.eebbk.mingming.k7ui.R.layout.k7ui_evgl_tran;
		} else {
			//mSrcView.setBackgroundResource(R.drawable.test_1);
			//mDstView.setBackgroundResource(R.drawable.test_2);
		}
		mEffectView = (GLEffectView) mInflater.inflate(evLayout, null);
		mEffectView.setEffector(effector);
		mEffectView.setTargetView(mSrcView, mDstView);
		mEffectView.captureImage(Effector.ID_SRC | Effector.ID_DST);
		mEffectContainer.addView(mEffectView);
	}
	
	private void configuEffector(Effector effector, int orientation) {
		if (null == effector) {
			return;
		}
		
		CurlEffectorGL curlEffector = null;
		
		if (Configuration.ORIENTATION_PORTRAIT == orientation) {			
			/*if (effector instanceof CurlEffectorGL) {
				mTvSrc1.setBackgroundResource(R.drawable.test_1);
				mTvSrc1.setVisibility(View.VISIBLE);
				mTvSrcLeft.setVisibility(View.GONE);
				mTvSrcRight.setVisibility(View.GONE);
				
				mTvDst1.setBackgroundResource(R.drawable.test_2);
				mTvDst1.setVisibility(View.VISIBLE);
				mTvDstLeft.setVisibility(View.GONE);
				mTvDstRight.setVisibility(View.GONE);
				
				curlEffector = (CurlEffectorGL) effector;
				curlEffector.setBkColor(0xffffffff);
				curlEffector.setMargins(0.15f, 0.1f, 0.15f, 0.1f);
				curlEffector.setEffectType(CurlEffectorGL.SHOW_ONE_PAGE);
			} else {
				mTvSrc1.setBackgroundResource(R.drawable.test_bk);
				mTvSrc1.setVisibility(View.VISIBLE);
				mTvSrcLeft.setVisibility(View.GONE);
				mTvSrcRight.setVisibility(View.GONE);
				
				mTvDst1.setBackgroundResource(R.drawable.test_bk);
				mTvDst1.setVisibility(View.VISIBLE);
				mTvDstLeft.setVisibility(View.GONE);
				mTvDstRight.setVisibility(View.GONE);
			}*/
			mTvSrc1.setBackgroundResource(R.drawable.test_1);
			mTvSrc1.setVisibility(View.VISIBLE);
			mTvSrcLeft.setVisibility(View.GONE);
			mTvSrcRight.setVisibility(View.GONE);
			
			mTvDst1.setBackgroundResource(R.drawable.test_2);
			mTvDst1.setVisibility(View.VISIBLE);
			mTvDstLeft.setVisibility(View.GONE);
			mTvDstRight.setVisibility(View.GONE);
			
			effector.setBkColor(0xffffffff);
			if (effector instanceof CurlEffectorGL) {
				curlEffector = (CurlEffectorGL) effector;
				curlEffector.setMargins(0.15f, 0.1f, 0.15f, 0.1f);
				curlEffector.setEffectType(CurlEffectorGL.SHOW_ONE_PAGE);
			}
			
		} else {
			/*if (effector instanceof CurlEffectorGL) {
				mTvSrcLeft.setVisibility(View.VISIBLE);
				mTvSrcRight.setVisibility(View.VISIBLE);
				mTvSrc1.setVisibility(View.GONE);
				
				mTvDstLeft.setVisibility(View.VISIBLE);
				mTvDstRight.setVisibility(View.VISIBLE);
				mTvDst1.setVisibility(View.GONE);
				
				curlEffector = (CurlEffectorGL) effector;
				curlEffector.setMargins(0.2f, 0.05f, 0.2f, 0.05f);
				curlEffector.setEffectType(CurlEffectorGL.SHOW_TWO_PAGE);
			} else {
				mTvSrc1.setBackgroundResource(R.drawable.test_bk);
				mTvSrc1.setVisibility(View.VISIBLE);
				mTvSrcLeft.setVisibility(View.GONE);
				mTvSrcRight.setVisibility(View.GONE);
				
				mTvDst1.setBackgroundResource(R.drawable.test_bk);
				mTvDst1.setVisibility(View.VISIBLE);
				mTvDstLeft.setVisibility(View.GONE);
				mTvDstRight.setVisibility(View.GONE);
			}*/
			
			mTvSrcLeft.setVisibility(View.VISIBLE);
			mTvSrcRight.setVisibility(View.VISIBLE);
			mTvSrc1.setVisibility(View.GONE);
			
			mTvDstLeft.setVisibility(View.VISIBLE);
			mTvDstRight.setVisibility(View.VISIBLE);
			mTvDst1.setVisibility(View.GONE);
			
			effector.setBkColor(0xffffffff);
			if (effector instanceof CurlEffectorGL) {
				curlEffector = (CurlEffectorGL) effector;
				curlEffector.setMargins(0.2f, 0.05f, 0.2f, 0.05f);
				curlEffector.setEffectType(CurlEffectorGL.SHOW_TWO_PAGE);
			}
		}
	}
	
	private void onChangeType(int index) {
		if ("CurlEffectorGL".equals(mEffectView.getEffectorName())) {
			return;
		}
		
		switch (index) {
		case ID_TYPE_HORIZONTAL:
			mEffectView.setEffectType(Effector.HORIZONTAL);
			break;
			
		case ID_TYPE_VERTICAL:
			mEffectView.setEffectType(Effector.VERTICAL);
			break;
		
		default:
			break;
		}
	}
	
	private void onChangeHighQuality(boolean highQuality) {
		mEffectView.setHighQuality(highQuality);
	}
	
	private void onChangeReverse(boolean reverse) {
		mEffectView.reverseEffect(reverse);
	}
	
	private void onChangeFill(boolean fill) {
		if (fill) {
			Drawable drFill = getFillDrawable();
			if (null == drFill) {
				return;
			}
			
			drFill.setBounds(0, 0, mDstView.getWidth(), mDstView.getHeight());
			mEffectView.fillImage(drFill, Effector.ID_DST);
			
		} else {
			mEffectView.captureImage(Effector.ID_DST);
		}
	}
	
	private Drawable getFillDrawable() {
		switch (mSpFill.getSelectedItemPosition()) {
		case ID_FILL_BMP:
			return mDrFillBmp;
			
		case ID_FILL_9_PATCH:
			return mDrFill9Patch;
			
		case ID_FILL_COLOR:
			return mDrFillColor;
			
		case ID_FILL_GRADIENT:
			return mDrFillGradient;
			
		default:
			return null;
		}
	}
	
	private void startAnim() {
		mAnimator.cancel();
		mEffectView.captureImage(Effector.ID_SRC | Effector.ID_DST);
		mAnimator.setTarget(mEffectView);
		mAnimator.setFloatValues(Effector.SRC_FACTOR, Effector.DST_FACTOR);
		mAnimator.start();
	}

}
