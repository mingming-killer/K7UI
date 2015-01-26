package com.eebbk.mingming.k7ui.demo;

import com.eebbk.mingming.k7ui.effector.BezierCurlEffector;
import com.eebbk.mingming.k7ui.effector.CanvasEffectView;
import com.eebbk.mingming.k7ui.effector.DoubleFaceFlipEffector;
import com.eebbk.mingming.k7ui.effector.Effector;
import com.eebbk.mingming.k7ui.effector.ScrollEffector;
import com.eebbk.mingming.k7utils.AppUtils;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class CanvasEffectViewActivity extends Activity implements OnClickListener, 
	OnSeekBarChangeListener, OnItemSelectedListener, OnCheckedChangeListener {

	@SuppressWarnings("unused")
	private final static String TAG = "EffectViewActivity";
	
	
	private final static int ID_EFFECTOR_SCROLL = 0;
	private final static int ID_EFFECTOR_DOUBLE_FACE_FLIP = 1;
	private final static int ID_EFFECTOR_BEZIER_CURL = 2;
	
	private final static String[] EFFECTORS = {
		"Scroll",
		"DoubleFaceFlip",
		"BezierCurl",
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
	
	private Spinner mSpEffector;
	private Spinner mSpType;
	private Spinner mSpFill;
	
	private CheckBox mCbHighQuality;
	private CheckBox mCbReverse;
	private CheckBox mCbFill;
	private SeekBar mSbEffectFactor;
	
	private View mSrcView;
	private View mDstView;
	
	private CanvasEffectView mEffectView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.canvas_effect_view_activity);
		
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
		mDrFillColor = AppUtils.getDrawableSafely(this, R.color.test_fill_color, null);
		mDrFillGradient = AppUtils.getDrawableSafely(this, R.drawable.test_fill_gradient, null);
	}
	
	private void initView() {
		mSpEffector = (Spinner) findViewById(R.id.sp_effector);
		mSpType = (Spinner) findViewById(R.id.sp_type);
		mSpFill = (Spinner) findViewById(R.id.sp_fill);
		mSbEffectFactor = (SeekBar) findViewById(R.id.sb_effect_factor);
		
		mCbHighQuality = (CheckBox) findViewById(R.id.cb_high_quality);
		mCbReverse = (CheckBox) findViewById(R.id.cb_reverse);
		mCbFill = (CheckBox) findViewById(R.id.cb_fill);
		
		mSrcView = findViewById(R.id.src_view);
		mDstView = findViewById(R.id.dst_view);
		
		mEffectView = (CanvasEffectView) findViewById(R.id.effect_view);
		
		mSpEffector.setAdapter(mEffectorAdapter);
		mSpType.setAdapter(mTypeAdapter);
		mSpFill.setAdapter(mFillAdapter);
		
		mEffectView.setTargetView(mSrcView, mDstView);
		
		onChangeEffector(ID_EFFECTOR_SCROLL);
		
		mCbHighQuality.setOnCheckedChangeListener(this);
		mCbReverse.setOnCheckedChangeListener(this);
		mCbFill.setOnCheckedChangeListener(this);
		mSbEffectFactor.setOnSeekBarChangeListener(this);
		mSpEffector.setOnItemSelectedListener(this);
		mSpType.setOnItemSelectedListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (null != mEffectView) {
			mEffectView.free();
		}
	}
	
	@Override
	public void onClick(View view) {
		//if (view.equals(mBtnUpdate)) {
		//	onBtnUpdate();
		//} else if (view.equals(mBtnChange)) {
		//	onBtnChange();
		//}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
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
	
	private void onChangeEffector(int index) {
		Effector effector = null;
		switch (index) {
		case ID_EFFECTOR_SCROLL:
			effector = new ScrollEffector();
			mEffectView.freeEffector();
			mEffectView.setEffector(effector);
			break;
			
		case ID_EFFECTOR_DOUBLE_FACE_FLIP:
			effector = new DoubleFaceFlipEffector();
			mEffectView.freeEffector();
			mEffectView.setEffector(effector);
			break;
			
		case ID_EFFECTOR_BEZIER_CURL:
			effector = new BezierCurlEffector();
			mEffectView.freeEffector();
			mEffectView.setEffector(effector);
			break;
		
		default:
			break;
		}
		
		mEffectView.captureImage(Effector.ID_SRC | Effector.ID_DST);
		mEffectView.invalidate();
	}
	
	private void onChangeType(int index) {
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

}
