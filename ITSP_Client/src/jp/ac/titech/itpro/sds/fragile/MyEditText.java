package jp.ac.titech.itpro.sds.fragile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MyEditText extends EditText {
	private PopupWindow mMyPopup;
	private CharSequence mMyError;
	private boolean mShowMyErrorAfterAttach;

	public MyEditText(Context context) {
		super(context);
	}
	public MyEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MyEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setMyError(CharSequence error) {
		if (error == null) {
			resetMyError();
		} else {
			mMyError = error;
			showMyError();
		}
	}
	
	private void showMyError() {
		if ((getWindowToken() == null) || !isShown()) {
			mShowMyErrorAfterAttach = true;
			return;
		}
		if (mMyPopup == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			TextView tv = (TextView) inflater.inflate(R.layout.error_popup_view, null);
			tv.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
			tv.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);			
			
			mMyPopup = new PopupWindow(this);
			mMyPopup.setContentView(tv);
			
			mMyPopup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
			mMyPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);			
			mMyPopup.setFocusable(false);
			mMyPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		}
		TextView tv = (TextView) mMyPopup.getContentView();
		tv.setText(mMyError);
		mMyPopup.showAsDropDown(this, getMyErrorX(), getMyErrorY());
		
	}
	
	private int getMyErrorX() {
		return getWidth() - mMyPopup.getWidth() - getPaddingRight();
	}
	
	private int getMyErrorY() {
		int vspace = getBottom() - getTop() -
				getCompoundPaddingBottom() - getCompoundPaddingTop();
		int icontop = getCompoundPaddingTop() + vspace / 2;
		
		return icontop - getHeight() - 2;
	}
	
	private void resetMyError() {
		if (mMyPopup != null) {
			if (mMyPopup.isShowing()) {
				mMyPopup.dismiss();
			}
			mMyPopup = null;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (isEnabled()) {
			if (keyCode != KeyEvent.KEYCODE_ENTER && keyCode != KeyEvent.KEYCODE_TAB) {
				// If the text changes, hide error.
				resetMyError();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (isShown() && mShowMyErrorAfterAttach) {
			mShowMyErrorAfterAttach = false;
			showMyError();
		}
	}
}
