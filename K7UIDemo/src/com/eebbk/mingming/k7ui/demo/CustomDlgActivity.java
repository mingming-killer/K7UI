package com.eebbk.mingming.k7ui.demo;

import com.eebbk.mingming.k7ui.AnimationWaitDlg;
import com.eebbk.mingming.k7ui.demo.help.ChoiceDlg;
import com.eebbk.mingming.k7ui.demo.help.WaitDlg;

import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class CustomDlgActivity extends Activity implements OnClickListener, 
	OnSeekBarChangeListener {

	@SuppressWarnings("unused")
	private final static String TAG = "CustomDlgActivity";
	
	private Button mBtnWaitDlg;
	private Button mBtnLeftDlg;
	private Button mBtnRightDlg;
	private Button mBtnTopDlg;
	private Button mBtnBottomDlg;
	private Button mBtnCenterDlg;
	
	private SeekBar mSbAlpha;
	private SeekBar mSbDimAmount;
	
	//private WaitDlg mWaitDlg;
	private AnimationWaitDlg mWaitDlg;
	private ChoiceDlg mChoiceDlg;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.custom_dlg_activity);
		
		initConfig();
		initView();
	}
	
	private void initConfig() {
	}
	
	private void initView() {
		mBtnWaitDlg = (Button) findViewById(R.id.btn_dlg_wait);
		mBtnLeftDlg = (Button) findViewById(R.id.btn_dlg_in_left);
		mBtnRightDlg = (Button) findViewById(R.id.btn_dlg_in_right);
		mBtnTopDlg = (Button) findViewById(R.id.btn_dlg_in_top);
		mBtnBottomDlg = (Button) findViewById(R.id.btn_dlg_in_bottom);
		mBtnCenterDlg = (Button) findViewById(R.id.btn_dlg_in_center);
		
		mSbAlpha = (SeekBar) findViewById(R.id.sb_alpha);
		mSbDimAmount = (SeekBar) findViewById(R.id.sb_dimAmount);
		
		mWaitDlg = new AnimationWaitDlg(this);
		mWaitDlg.setCancelable(true);
		//mWaitDlg.setBackground(R.drawable.dlg_wait_bk);
		mWaitDlg.setMessage(R.string.loading);
		
		mWaitDlg.setPosition(Gravity.CENTER, 0, 0, 380, 160);
		
		mChoiceDlg = new ChoiceDlg(this);
		mChoiceDlg.setCancelable(true);
		mChoiceDlg.setBackground(R.drawable.tran_bk);
		mChoiceDlg.setTitle("Select a choice");
		mChoiceDlg.setMessage("Are you sure to delete this data ?");
		mChoiceDlg.setLeftText("Cancel");
		mChoiceDlg.setRightText("Confirm");
		mChoiceDlg.setPosition(Gravity.CENTER, 0, 0, 450, 250);
		mChoiceDlg.setLeftClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Toast.makeText(CustomDlgActivity.this, 
						"You click a cancel button.", Toast.LENGTH_SHORT)
					.show();
				mChoiceDlg.dismiss();
			}
		}
		);
		mChoiceDlg.setRightClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Toast.makeText(CustomDlgActivity.this, 
						"You click a confirm button.", Toast.LENGTH_SHORT)
					.show();
				mChoiceDlg.dismiss();
			}
		}
		);
		
		mBtnWaitDlg.setOnClickListener(this);
		mBtnLeftDlg.setOnClickListener(this);
		mBtnRightDlg.setOnClickListener(this);
		mBtnTopDlg.setOnClickListener(this);
		mBtnBottomDlg.setOnClickListener(this);
		mBtnCenterDlg.setOnClickListener(this);
		
		mSbAlpha.setOnSeekBarChangeListener(this);
		mSbDimAmount.setOnSeekBarChangeListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mWaitDlg.dismiss();
		mChoiceDlg.dismiss();
	}
	
	@Override
	public void onClick(View view) {
		if (view.equals(mBtnWaitDlg)) {
			mWaitDlg.show();
		} else if (view.equals(mBtnLeftDlg)) {
			//mChoiceDlg.show();
			onBtnLeftDlgClick(view);
		} else if (view.equals(mBtnRightDlg)) {
			onBtnRightDlgClick(view);
		} else if (view.equals(mBtnRightDlg)) {
			onBtnRightDlgClick(view);
		} else if (view.equals(mBtnTopDlg)) {
			onBtnTopDlgClick(view);
		} else if (view.equals(mBtnBottomDlg)) {
			onBtnBottomDlgClick(view);
		} else if (view.equals(mBtnCenterDlg)) {
			onBtnCenterDlgClick(view);
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar.equals(mSbAlpha)) {
			onChangeAlpha(seekBar, progress);
		} else if (seekBar.equals(mSbDimAmount)) {
			onChangeDimAmount(seekBar, progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	private void onChangeAlpha(SeekBar seekBar, int progress) {
		float alpha = (float)progress / (float)seekBar.getMax();
		mWaitDlg.setAlpha(alpha);
		mChoiceDlg.setAlpha(alpha);
	}
	
	private void onChangeDimAmount(SeekBar seekBar, int progress) {
		float dimAmount = (float)progress / (float)seekBar.getMax();
		mWaitDlg.setDimAmount(dimAmount);
		mChoiceDlg.setDimAmount(dimAmount);
	}
	
	private void onBtnLeftDlgClick(View view) {
		mChoiceDlg.showAsDropDown(view, -mChoiceDlg.getWidth(), 0);
	}
	
	private void onBtnRightDlgClick(View view) {
		mChoiceDlg.showAsDropDown(view, view.getWidth(), 0);
	}
	
	private void onBtnTopDlgClick(View view) {
		mChoiceDlg.showAsDropDown(view, 0, -mChoiceDlg.getHeight());
	}
	
	private void onBtnBottomDlgClick(View view) {
		mChoiceDlg.showAsDropDown(view, 0, 0);
	}
	
	private void onBtnCenterDlgClick(View view) {
		mChoiceDlg.showAsDropDown(view, 0, 0);
	}

}
