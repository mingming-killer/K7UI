package com.eebbk.mingming.k7ui.demo;

import java.util.ArrayList;

import com.eebbk.mingming.k7ui.DragItemListView;
import com.eebbk.mingming.k7ui.DragTextListView;
import com.eebbk.mingming.k7ui.DragTextView;
import com.eebbk.mingming.k7ui.DragItemListView.DragItemListViewListener;
import com.eebbk.mingming.k7ui.demo.help.GestureContainer;
import com.eebbk.mingming.k7ui.demo.help.GestureContainer.GestureContainerListener;
import com.eebbk.mingming.k7utils.AppUtils;
import com.eebbk.mingming.k7utils.LogUtils;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class DragTextViewActivity extends Activity implements OnClickListener, 
	OnItemClickListener, OnItemLongClickListener, 
	GestureContainerListener, DragItemListViewListener {

	private final static String TAG = "DragTextViewActivity";
	
	private String mTestStr;
	private ArrayList<String> mTestDatas;
	
	private DragTextView mDtvMoreStart;
	private DragTextView mDtvMoreEnd;
	//private DragTextView mDtvSmall;
	
	private DragTextListView mListView;
	private ListAdapter mListAdapter;
	
	private CheckBox mCBDragMode;
	
	private GestureContainer mGestureContainer;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.drag_text_view_activity);
		
		mTestStr = AppUtils.getStringSafely(this, R.string.test_large_text, "more ");
		mTestDatas = new ArrayList<String> ();
		for (int i = 0; i < 50; i++) {
			mTestDatas.add(i + " : " + mTestStr);
		}
		
		mDtvMoreStart = (DragTextView) findViewById(R.id.tv_more_start);
		mDtvMoreEnd = (DragTextView) findViewById(R.id.tv_more_end);
		//mDtvSmall = (DragTextView) findViewById(R.id.tv_small);
		mListView = (DragTextListView) findViewById(R.id.list_view);
		mGestureContainer = (GestureContainer) findViewById(R.id.gesture_container);
		
		mCBDragMode = (CheckBox) findViewById(R.id.cb_long_press_drag);
		
		mListAdapter = new ListAdapter(this, mTestDatas);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);
		//mListView.setOnItemLongClickListener(this);
		
		//mDtvMoreStart.setAutoDragRestore(true);
		//mDtvMoreEnd.setAutoDragRestore(false);
		//mDtvSmall.setAutoDragRestore(true);
		//mDtvSmall.setOverScrollFactor(0.02f);
		//mDtvMoreEnd.setOverScrollFactor(0.02f);
		
		mCBDragMode.setOnClickListener(this);
		
		mDtvMoreStart.setOnClickListener(this);		
		mListView.setListener(this);
		mGestureContainer.setGestureContainerListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onClick(View view) {
		if (view.equals(mDtvMoreStart) || 
				view.equals(mDtvMoreEnd)) {
			TextView tv = (TextView) view;
			Toast.makeText(this, tv.getText(), Toast.LENGTH_SHORT).show();
		} else if (view.equals(mCBDragMode)) {
			onChangeDragMode(mCBDragMode);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		LogUtils.d(TAG, "click item: " + position);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		LogUtils.d(TAG, "long click item: " + position);
		return true;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		LogUtils.d(TAG, "onFling: vX: " + velocityX + ", vY: " + velocityY);
		return false;
	}
	
	@Override
	public View findDragView(View itemContainer) {
		try {
			return itemContainer.findViewById(R.id.item_tv);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void onChangeDragMode(CheckBox cb) {
		if (cb.isChecked()) {
			mListView.setEnterDragMode(DragItemListView.ENTER_DRAG_MODE_LONG_PRESSED);
		} else {
			mListView.setEnterDragMode(DragItemListView.ENTER_DRAG_MODE_DETECT_SCROLL);
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
			DragTextView tvInfo = null;
			
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
				//convertView.setBackgroundColor(0xffff0000);
			//	tvInfo.setAutoDragRestore(true);
				tvInfo.setText(data);
			} else {
				//convertView.setBackgroundColor(0xff00ff00);
			//	tvInfo.setAutoDragRestore(false);
				tvInfo.setText("small text can't drag");
			}
			
			//tvInfo.setText(data);
			//tvInfo.setOverScrollFactor(0.02f);
			//tvInfo.enableDrag(false);
			//tvInfo.setAutoDragRestore(true);
			
			return convertView;
		}
		
	}

}
