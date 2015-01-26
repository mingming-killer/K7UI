package com.eebbk.mingming.k7ui.demo.help;

import com.eebbk.mingming.k7ui.CustomDlg;
import com.eebbk.mingming.k7ui.demo.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * 
 * System choice dialog. For custom tip and choice dialog.
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class ChoiceDlg extends CustomDlg {
	
	@SuppressWarnings("unused")
	private final static String TAG = "SysChoiceDlgDlg";
    
    private TextView mTvTitle;
    private TextView mTvMsg;
    
    private Button mBtnLeft;
    private Button mBtnRight;
    
    private CharSequence mTitle;
    private int mTitleTextID;
    
    private CharSequence mMessage;
    private int mMsgTextID;
    
    private CharSequence mBtnLeftText;
    private int mBtnLeftTextID;
    
    private CharSequence mBtnRightText;
    private int mBtnRightTextID;
    
    private View.OnClickListener mBtnLeftClickListener;
    private View.OnClickListener mBtnRightClickListener;
    
	
	public ChoiceDlg(Context context) {
		super(context);
		init();
	}
	
	public ChoiceDlg(Context context, int theme) {
		super(context, theme);
		init();
	}
	
    private void init() {
    	mMessage = null;
    	mMsgTextID = -1;
        
        mTitle = null;
        mTitleTextID = -1;
        
        mBtnLeftText = null;
        mBtnLeftTextID = -1;
        
        mBtnRightText = null;
        mBtnRightTextID = -1;
        
        mBtnLeftClickListener = null;
        mBtnRightClickListener = null;
        
        // remove suck window title.
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }
    
    public static ChoiceDlg show(Context context, CharSequence message) {
        return show(context, message, false);
    }

    public static ChoiceDlg show(Context context, CharSequence message, boolean cancelable) {
        return show(context, message, cancelable, null);
    }

    public static ChoiceDlg show(Context context, CharSequence message, 
            boolean cancelable, OnCancelListener cancelListener) {
    	ChoiceDlg dialog = new ChoiceDlg(context);
        dialog.setMessage(message);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	View contentView = null;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        
        contentView = inflater.inflate(R.layout.dlg_choice_content, null);
        mTvTitle = (TextView) contentView.findViewById(R.id.dlg_choice_bar_title);
        mTvMsg = (TextView) contentView.findViewById(R.id.dlg_choice_msg);
        mBtnLeft = (Button) contentView.findViewById(R.id.dlg_choice_btn_left);
        mBtnRight = (Button) contentView.findViewById(R.id.dlg_choice_btn_right);
        
        setContentView(contentView);
        
        // here the view of dialog is ready, we set it.
        if (null != mTitle) {
            setTitle(mTitle);
        }
        if (mTitleTextID > 0) {
        	setTitle(mTitleTextID);
        }
        if (null != mMessage) {
            setMessage(mMessage);
        }
        if (mMsgTextID > 0) {
        	setMessage(mMsgTextID);
        }
        if (null != mBtnLeftText) {
            setLeftText(mBtnLeftText);
        }
        if (mBtnLeftTextID > 0) {
        	setLeftText(mBtnLeftTextID);
        }
        if (null != mBtnRightText) {
            setRightText(mBtnRightText);
        }
        if (mBtnRightTextID > 0) {
        	setRightText(mBtnRightTextID);
        }
        if (null != mBtnLeftClickListener) {
            setLeftClickListener(mBtnLeftClickListener);
        }
        if (null != mBtnRightClickListener) {
        	setRightClickListener(mBtnRightClickListener);
        }
        
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }
    
    /*@Override
    public void onBackPressed() {
    	// when set interruptible, user pressed back key,
    	// we cancel the wait dialog.
    	if (mInterruptible) {
    		cancel();
    		
    		if (null != mListener) {
    			mListener.onWaitInterrupt();
    		}
    		
    		return;
    	}
        
        super.onBackPressed();
    }*/
    
    @Override
    protected void onStop() {
        super.onStop();
    }
    
    public void setTitle(CharSequence title) {
        if (null != mTvTitle) {
        	mTvTitle.setText(title);
        } else {
            mTitle = title;
        }
    }
    
    public void setTitle(int titleTextID) {
        if (null != mTvTitle) {
        	mTvTitle.setText(titleTextID);
        } else {
            mTitleTextID = titleTextID;
        }
    }
    
    /**
     * Set wait dialog wait message like: "waiting...", "loading..." and so on.
     * 
     * @param message {@link String} of message.
     */
    public void setMessage(CharSequence message) {
        if (null != mTvMsg) {
        	mTvMsg.setText(message);
        } else {
            mMessage = message;
        }
    }
    
    /**
     * Set wait dialog wait message like: "waiting...", "loading..." and so on.
     * 
     * @param msgID resource ID of message.
     */
    public void setMessage(int msgTextID) {
        if (mTvMsg != null) {
        	mTvMsg.setText(msgTextID);
        } else {
            mMsgTextID = msgTextID;
        }
    }
    
    public void setLeftText(CharSequence leftText) {
        if (null != mBtnLeft) {
        	mBtnLeft.setText(leftText);
        } else {
        	mBtnLeftText = leftText;
        }
    }
    
    public void setLeftText(int leftTextID) {
        if (null != mBtnLeft) {
        	mBtnLeft.setText(leftTextID);
        } else {
        	mBtnLeftTextID = leftTextID;
        }
    }
    
    public void setRightText(CharSequence rightText) {
        if (null != mBtnRight) {
        	mBtnRight.setText(rightText);
        } else {
        	mBtnRightText = rightText;
        }
    }
    
    public void setRightText(int rightTextID) {
        if (null != mBtnRight) {
        	mBtnRight.setText(rightTextID);
        } else {
        	mBtnRightTextID = rightTextID;
        }
    }
    
    public void setLeftClickListener(View.OnClickListener clickListener) {
        if (null != mBtnLeft) {
        	mBtnLeft.setOnClickListener(clickListener);
        } else {
        	mBtnLeftClickListener = clickListener;
        }
    }
    
    public void setRightClickListener(View.OnClickListener clickListener) {
        if (null != mBtnRight) {
        	mBtnRight.setOnClickListener(clickListener);
        } else {
        	mBtnRightClickListener = clickListener;
        }
    }
    
}
