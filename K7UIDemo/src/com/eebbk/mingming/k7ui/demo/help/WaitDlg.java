package com.eebbk.mingming.k7ui.demo.help;

import com.eebbk.mingming.k7ui.CustomDlg;
import com.eebbk.mingming.k7ui.demo.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 
 * Wait dialog. For custom wait progress bar. 
 * Base on android 4.0 {@link ProgressDialog}.
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class WaitDlg extends CustomDlg {
	
	@SuppressWarnings("unused")
	private final static String TAG = "WaitDlg";
    
    private ProgressBar mProgress;
    private TextView mMessageView;
    
    private Drawable mIndeterminateDrawable;
    private CharSequence mMessage;
    private int mMsgID;
    
    private int mPbBkResID;
    private Drawable mPbDrBk;
    
    @SuppressWarnings("unused")
	private boolean mHasStarted;
    private boolean mInterruptible;
    
    private WaitDlgListener mListener;
    
    /**
     * 
     * Wait dialog listener.
     * 
     * @author humingming <humingming@oaserver.dw.gdbbk.com>
     *
     */
    public interface WaitDlgListener {
    	
    	/**
    	 * Use interrupt the waiting dialog by pressed back key.
    	 */
    	public void onWaitInterrupt();
    	
    }
    
	
	public WaitDlg(Context context) {
		super(context);
		init();
	}
	
	public WaitDlg(Context context, int theme) {
		super(context, theme);
		init();
	}
	
    private void init() {
        mPbBkResID = -1;
        mMsgID = -1;
        mPbDrBk = null;
        mMessage = null;
        
        mInterruptible = false;
        mListener = null;
        
        // remove suck window title.
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }
    
    public static WaitDlg show(Context context, CharSequence message) {
        return show(context, message, false);
    }

    public static WaitDlg show(Context context, CharSequence message, boolean cancelable) {
        return show(context, message, cancelable, null);
    }

    public static WaitDlg show(Context context, CharSequence message, 
            boolean cancelable, OnCancelListener cancelListener) {
    	WaitDlg dialog = new WaitDlg(context);
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
        
        contentView = inflater.inflate(R.layout.dlg_wait_content, null);
        mProgress = (ProgressBar) contentView.findViewById(R.id.dlg_wait_pb);
        mMessageView = (TextView) contentView.findViewById(R.id.dlg_wait_message);
        setContentView(contentView);
        
        // here the view of dialog is ready, we set it.
        if (null != mIndeterminateDrawable) {
            setIndeterminateDrawable(mIndeterminateDrawable);
        }
        if (null != mMessage) {
            setMessage(mMessage);
        }
        if (-1 != mMsgID) {
        	setMessage(mMsgID);
        }
        if (mPbBkResID > 0) {
        	setProgressBackground(mPbBkResID);
        }
        if (null != mPbDrBk) {
        	setProgressBackground(mPbDrBk);
        }
        
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mHasStarted = true;
    }
    
    @Override
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
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        mHasStarted = false;
    }
    
    /**
     * Wrapper of {@link ProgressBar#setIndeterminateDrawable(Drawable)}.
     * 
     * @param pbDr
     */
    public void setIndeterminateDrawable(Drawable pbDr) {
        if (null != mProgress) {
            mProgress.setIndeterminateDrawable(pbDr);
        } else {
            mIndeterminateDrawable = pbDr;
        }
    }
    
    /**
     * Wrapper of {@link ProgressBar#setBackgroundResource(int)}.
     * 
     * @param resID
     */
    public void setProgressBackground(int resID) {
    	if (null != mProgress) {
    		mProgress.setBackgroundResource(resID);
    	} else {
    		mPbBkResID = resID;
    	}
    }
    
    /**
     * Wrapper of {@link ProgressBar#setProgressDrawable(Drawable)}.
     * 
     * @param pbDrBk
     */
    public void setProgressBackground(Drawable pbDrBk) {
    	if (null != mProgress) {
    		mProgress.setBackgroundDrawable(pbDrBk);
    	} else {
    		mPbDrBk = pbDrBk;
    	}
    }
    
    /**
     * Set wait dialog wait message like: "waiting...", "loading..." and so on.
     * 
     * @param message {@link String} of message.
     */
    public void setMessage(CharSequence message) {
        if (mProgress != null) {
        	mMessageView.setText(message);
        } else {
            mMessage = message;
        }
    }
    
    /**
     * Set wait dialog wait message like: "waiting...", "loading..." and so on.
     * 
     * @param msgID resource ID of message.
     */
    public void setMessage(int msgID) {
        if (mProgress != null) {
        	mMessageView.setText(msgID);
        } else {
            mMsgID = msgID;
        }
    }
    
    /**
     * Set whether this waiting dialog can interrupt by press back key.
     * Event it can't cancel. But if will send a notify: 
     * 
     * @param interruptible
     */
    public void setInterruptible(boolean interruptible) {
    	mInterruptible = interruptible;
    }
    
    /**
     * Set wait dialog listener.
     * 
     * @param listener Object of {@link WaitDlgListener}.
     */
    public void setWaitDlgListener(WaitDlgListener listener) {
    	mListener = listener;
    }
    
    /**
     * Query whether wait dialog can interruptable.
     * 
     * @return True can interruptible, false 
     */
    public boolean isInterruptible() {
    	return mInterruptible;
    }
}
