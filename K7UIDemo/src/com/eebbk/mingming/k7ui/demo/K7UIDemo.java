package com.eebbk.mingming.k7ui.demo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;

public class K7UIDemo extends ListActivity {
	
	protected final static String TAG = "K7UIDemo";
	
	private final static String ITEM_NAME = "title";
	private final static String ITEM_INTENT = "intent";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setListView();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void setListView() {
    	Map<String, Object> item = new HashMap<String, Object>();
    	List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    	
    	// please add your test activity(title and intent) to arrays.xml
    	String[] testFunctions = getResources().getStringArray(R.array.test_functions);
    	String[] testIntents = getResources().getStringArray(R.array.test_intents);
    	
    	for (int i = 0; i < testFunctions.length; i++) {
    		item = new HashMap<String, Object>();
    		item.put(ITEM_NAME, testFunctions[i]);
    		try {
				item.put(ITEM_INTENT, Intent.parseUri(testIntents[i], 0));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				item.put(ITEM_INTENT, null);
			}
    		data.add(item);
    	}    	
    	
    	setListAdapter(new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_1, new String[] { ITEM_NAME },
                new int[] { android.R.id.text1 }));
        getListView().setTextFilterEnabled(true);
    }
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {		
		@SuppressWarnings("rawtypes")
		Map map = (Map)l.getItemAtPosition(position);
		
        Intent intent = (Intent)map.get("intent");
        if (intent != null) {
        	try {
        		startActivity(intent);
        	} catch (ActivityNotFoundException e) {
        		e.printStackTrace();
        	}
        }
    }

}
