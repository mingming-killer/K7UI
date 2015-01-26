package com.eebbk.mingming.k7ui.demo;

import java.util.ArrayList;
import java.util.HashMap;

import com.eebbk.mingming.k7ui.DragItemListView;
import com.eebbk.mingming.k7ui.DragItemListView.DragItemListViewListener;
import com.eebbk.mingming.k7ui.demo.help.MyDragComponent;
import com.eebbk.mingming.k7ui.demo.help.MyDragComponentListView;
import com.eebbk.mingming.k7ui.demo.help.MyDragComponentListView.DragData;
import com.eebbk.mingming.k7utils.AppUtils;
import com.eebbk.mingming.k7utils.LogUtils;

import android.os.Bundle;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class DragComponentActivity extends Activity implements OnClickListener, 
	OnItemClickListener, OnItemLongClickListener, DragItemListViewListener {

	private final static String TAG = "DragComponentActivity";
	
	private final static long DELETE_ITEM_ANIM_DURATION = 1500;
	
	private String mTestStr;
	private ArrayList<DragData> mTestDatas;
	
	private LayoutTransition mLayoutTran = null;
	
	private MyDragComponentListView mListView;
	private ListAdapter mListAdapter;
	
	private CheckBox mCBDragMode;
	
	private class ViewTag {
		@SuppressWarnings("unused")
		View mTargetView;
		int mPosition;
		
		public ViewTag(View targetView, int position) {
			mTargetView = targetView;
			mPosition = position;
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.drag_component_activity);
		
		mTestStr = AppUtils.getStringSafely(this, R.string.test_large_text, "more ");
		mTestDatas = new ArrayList<DragData> ();
		for (int i = 0; i < 50; i++) {
			mTestDatas.add(new DragData(mTestStr, i, 0));
		}
		
		mListView = (MyDragComponentListView) findViewById(R.id.list_view);
		mCBDragMode = (CheckBox) findViewById(R.id.cb_long_press_drag);
		
		mListAdapter = new ListAdapter(this, mTestDatas);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		
		mCBDragMode.setOnClickListener(this);
		
		mListView.setDatas(mTestDatas);
		mListView.setListener(this);
		mListView.setEnterDragMode(DragItemListView.ENTER_DRAG_MODE_DETECT_SCROLL);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (null != mTestDatas) {
			mTestDatas.clear();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		restoreAllDrag();
	}
	
	@Override
	public void onClick(View view) {
		if (R.id.right_component_btn == view.getId()) {
			onBtnDeleteClick(view);
		} else if (view.equals(mCBDragMode)) {
			onChangeDragMode(mCBDragMode);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		LogUtils.d(TAG, "click item: " + position);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		LogUtils.d(TAG, "long click item: " + position);
		return true;
	}
	
	@Override
	public View findDragView(View itemContainer) {
		try {
			return itemContainer.findViewById(R.id.drag_component);
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
	
	private void onBtnDeleteClick(View view) {
		if (null == view) {
			return;
		}
		
		ViewTag tagData = null;
		try {
			tagData = (ViewTag) view.getTag();
		} catch (Exception e) {
			e.printStackTrace();
			tagData = null;
		}
		
		if (null == mTestDatas || null == mListAdapter || null == tagData
				|| tagData.mPosition < 0 || tagData.mPosition >= mTestDatas.size()) {
			return;
		}
		
		if (null != mListView) {
			mListView.setLayoutTransition(mLayoutTran);
		}
		
		mTestDatas.remove(tagData.mPosition);
		mListAdapter.notifyDataSetChanged();
		//animateRemoval(mListView, tagData.mTargetView);
	}
	
	private HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();
	
    @SuppressWarnings("unused")
	private boolean animateRemoval(final ListView listView, View removeView) {
    	// TODO: 这里其实是让每一个 view 都做个移动动画，移动到删除之后的位置
    	// 不过现在还有点问题，删除数据后，view 的排列好像有点不对
    	if (null == listView || null == removeView 
    			|| null == mListAdapter || null == mTestDatas) {
    		return false;
    	}
    	
    	mItemIdTopMap.clear();
    	
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            if (child != removeView) {
                int position = firstVisiblePosition + i;
                long itemId = mListAdapter.getItemId(position);
                mItemIdTopMap.put(itemId, child.getTop());
            }
        }
        // Delete the item from the adapter
        int position = mListView.getPositionForView(removeView);
        mTestDatas.remove(mListAdapter.getItem(position));
        mListAdapter.notifyDataSetChanged();

        final ViewTreeObserver observer = listView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        	@Override
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listView.getFirstVisiblePosition();
                for (int i = 0; i < listView.getChildCount(); ++i) {
                    final View child = listView.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = mListAdapter.getItemId(position);
                    Integer startTop = mItemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(DELETE_ITEM_ANIM_DURATION).translationY(0);
                            if (firstAnimation) {
                                //child.animate().withEndAction(new Runnable() {
                                //    public void run() {
                                //        mListView.setEnabled(true);
                                //    }
                                //});
                                firstAnimation = false;
                            }
                        }
                    } else {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + listView.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(DELETE_ITEM_ANIM_DURATION).translationY(0);
                        if (firstAnimation) {
                            //child.animate().withEndAction(new Runnable() {
                            //    public void run() {
                            //        mListView.setEnabled(true);
                            //    }
                            //});
                            firstAnimation = false;
                        }
                    }
                }
                mItemIdTopMap.clear();
                return true;
            }
        });
        
        return true;
    }
    
	private void restoreAllDrag() {
		if (null == mTestDatas || null == mListAdapter) {
			return;
		}
		
		for (DragData data : mTestDatas) {
			if (null == data) {
				continue;
			}
			data.mDragPosition = 0;
		}
		
		mListAdapter.notifyDataSetChanged();
	}
	
	
	class ListAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private ArrayList<DragData> mDatas;
		
		public ListAdapter(Context context, ArrayList<DragData> datas) {
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
			MyDragComponent container = null;
			TextView tvInfo = null;
			Button btnDelete = null;
			
			if (null == convertView) {
				convertView = (MyDragComponent) mInflater.inflate(R.layout.drag_component_item, null);
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
			
			container = (MyDragComponent) convertView;
			if (null == container) {
				return convertView;
			}
			container.setTag(position);
			
			tvInfo = (TextView) container.findViewById(R.id.main_component_tv);
			btnDelete = (Button) container.findViewById(R.id.right_component_btn);
			
			if (null == mDatas || 
					position < 0 || position >= mDatas.size()) {
				return convertView;
			}
			
			DragData data = mDatas.get(position);
			if (null == data) {
				return convertView;
			}
			
			if (0 == position % 2) {
				if (null != data.mData) {
					tvInfo.setText(data.mIndex + ": " + data.mData);
				} else {
					tvInfo.setText(data.mIndex + ": large text drag drag drag");
				}
			} else {
				tvInfo.setText(data.mIndex + ": small text can't drag");
			}
			
			if (null != btnDelete) {
				ViewTag tagData = null;
				try {
					tagData = (ViewTag) btnDelete.getTag();
				} catch (Exception e) {
					tagData = null;
				}
				if (null == tagData) {
					tagData = new ViewTag(convertView, position);
				} else {
					tagData.mTargetView = convertView;
					tagData.mPosition = position;
				}
				btnDelete.setTag(tagData);
				btnDelete.setOnClickListener(DragComponentActivity.this);
			}
			
			container.setDragPosition(data.mDragPosition);
			return convertView;
		}
		
	}

}
