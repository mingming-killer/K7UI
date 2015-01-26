package com.eebbk.mingming.k7ui.demo;

import com.eebbk.mingming.k7ui.demo.help.MachineView;

import android.os.Bundle;
import android.app.Activity;
import android.view.Window;

public class StateMachineActivity extends Activity {

	@SuppressWarnings("unused")
	private final static String TAG = "StateMachineActivity";
	
	@SuppressWarnings("unused")
	private MachineView mMachineView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.state_machine_activity);
	}
	
}
