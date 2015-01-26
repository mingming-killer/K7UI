package com.eebbk.mingming.k7ui;

import com.eebbk.mingming.k7ui.R;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * 
 * Wait dialog. For custom wait progress bar. 
 * Base on android 4.0 {@link ProgressDialog}.
 * 
 * @author humingming <humingming@oaserver.dw.gdbbk.com>
 *
 */
public class AnimationWaitDlg extends CustomDlg implements DialogInterface.OnShowListener {
	
	@SuppressWarnings("unused")
	private final static String TAG = "AnimationWaitDlg";
    
	private View mAnimView;
    private TextView mTvMsg;
    
    private CharSequence mMessage;
    private int mMsgId;
    
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
    
	
	public AnimationWaitDlg(Context context) {
		super(context);
		init();
	}
	
	public AnimationWaitDlg(Context context, int theme) {
		super(context, theme);
		init();
	}
	
    private void init() {
    	mMsgId = -1;
        mMessage = null;
        
        mInterruptible = false;
        mListener = null;
        
        setOnShowListener(this);
        
        // remove suck window title.
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }
    
    public static AnimationWaitDlg show(Context context, CharSequence message) {
        return show(context, message, false);
    }

    public static AnimationWaitDlg show(Context context, CharSequence message, boolean cancelable) {
        return show(context, message, cancelable, null);
    }

    public static AnimationWaitDlg show(Context context, CharSequence message, 
            boolean cancelable, OnCancelListener cancelListener) {
    	AnimationWaitDlg dialog = new AnimationWaitDlg(context);
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
        
        contentView = inflater.inflate(R.layout.k7ui_dlg_anim_wait_content, null);
        mAnimView = contentView.findViewById(R.id.k7ui_dlg_anim_wait_anim_view);
        mTvMsg = (TextView) contentView.findViewById(R.id.k7ui_dlg_anim_wait_tv_msg);
        setContentView(contentView);
        
        setBackground(R.drawable.k7ui_dlg_wait_bk);
        
        // here the view of dialog is ready, we set it.
        if (null != mMessage) {
            setMessage(mMessage);
        }
        if (-1 != mMsgId) {
        	setMessage(mMsgId);
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
        startAnimation(false);
    }
    
	@Override
	public void onShow(DialogInterface dialog) {
		startAnimation(true);
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
     * @param msgId resource id of message.
     */
    public void setMessage(int msgId) {
        if (null != mTvMsg) {
        	mTvMsg.setText(msgId);
        } else {
            mMsgId = msgId;
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
    
    private void startAnimation(boolean start) {
    	if (null == mAnimView) {
    		return;
    	}
    	
    	try {
    		AnimationDrawable anim = (AnimationDrawable) mAnimView.getBackground();
    		if (start) {
    			anim.start();
    		} else {
    			anim.stop();
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
}
