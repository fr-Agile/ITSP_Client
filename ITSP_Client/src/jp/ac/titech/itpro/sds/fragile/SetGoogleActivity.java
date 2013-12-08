package jp.ac.titech.itpro.sds.fragile;

import java.util.ArrayList;
import java.util.List;

import com.appspot.fragile_t.registerEndpoint.RegisterEndpoint;
import com.appspot.fragile_t.registerEndpoint.RegisterEndpoint.RegisterV1Endpoint.SetGoogleAccount;
import com.appspot.fragile_t.registerEndpoint.model.UserV1Dto;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.GoogleConstant;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleAccountChecker;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleAccountChecker.GoogleAccountCheckFinishListener;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SetGoogleActivity extends Activity implements GoogleAccountCheckFinishListener {
	private List<String> mAccountList = new ArrayList<String>();
	private List<String> mDisplayList = new ArrayList<String>();
	private ArrayAdapter<String> mAdapter;
	
	private ListView mListView;
	
	private View mSetGoogleFormView;
	private View mSetGoogleStatusView;
	private TextView mSetGoogleStatusMessageView;
	
	private String mGoogleAccount;
	private SetGoogleTask mAuthTask = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_google);
        
        mListView = (ListView) findViewById(R.id.set_google_list);

        mAdapter = new ArrayAdapter<String>(
        		this, android.R.layout.simple_list_item_single_choice, mDisplayList);
        mListView.setAdapter(mAdapter);
        
        Button btn = (Button) findViewById(R.id.set_google_button);
        btn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		// 選択アイテムを取得
        		String selected = null;
        		int pos = mListView.getCheckedItemPosition();
        		if (pos != ListView.INVALID_POSITION) {
        			if (pos < mAccountList.size()) {
	        			selected = mAccountList.get(pos) + "_" + mDisplayList.get(pos);
	        			// 全てのイベントをエクスポートするか聞くような処理
        			} else {
        				selected = GoogleConstant.UNTIED_TO_GOOGLE;
        			}
        		}
        		if (selected != null) {
        			mGoogleAccount = selected;
        			if (mAuthTask == null) { 
        				mAuthTask = new SetGoogleTask();
        				mAuthTask.execute();
        			}
        		}
        	}
        });
        Button cancelBtn = (Button) findViewById(R.id.set_google_cancel_button);
        cancelBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		// キャンセルしたときの動作
				SetGoogleActivity.this.finish();	// 元のアクティビティに戻る
        	}
        });
        
		mSetGoogleFormView = findViewById(R.id.set_google_form);
		mSetGoogleStatusView = findViewById(R.id.set_google_status);
		mSetGoogleStatusMessageView = (TextView) findViewById(R.id.set_google_status_message);
        
        // 登録されているgoogleアカウントを取得
		mSetGoogleStatusMessageView.setText(R.string.getting_google_account);
		showProgress(true);
		GoogleAccountChecker gac = new GoogleAccountChecker(
				SetGoogleActivity.this, SetGoogleActivity.this);
		gac.run(true);		// 書き込み権限があるアカウントのみ表示
    }
    
    
	@Override
	public void onGoogleAccountCheckFinish(boolean result,
			List<String> accountList, List<String> displayList, List<Long> idList) {
		showProgress(false);
		
		if (result && (accountList != null) && (accountList.size() > 0)) {
			// ListViewを更新
			mDisplayList.clear();
			mDisplayList.addAll(displayList);
			mDisplayList.add("Googleにエクスポートしない");
			mAdapter.notifyDataSetChanged();
			
			// account_nameのリストも更新
			mAccountList = accountList;
		} else {
			// アカウントが登録されていない場合、ダイアログを表示して登録を促す
			new GoogleAccountRegistDialogBuilder(SetGoogleActivity.this)
				.setDefault()
				.show();
		}
	}
    
	
	
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mSetGoogleStatusView.setVisibility(View.VISIBLE);
			mSetGoogleStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mSetGoogleStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mSetGoogleFormView.setVisibility(View.VISIBLE);
			mSetGoogleFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mSetGoogleFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mSetGoogleStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mSetGoogleFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	public class SetGoogleTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {
			SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
			String email = pref.getString("email", "");
			try {
				Log.d("DEBUG", mGoogleAccount);
				Log.d("DEBUG", email);
				RegisterEndpoint endpoint = RemoteApi.getRegisterEndpoint();
				SetGoogleAccount setGoogle = endpoint.registerV1Endpoint()
						.setGoogleAccount(email, mGoogleAccount);
				
				UserV1Dto userDto = setGoogle.execute();

				
				if (userDto != null) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			if (success) {
				Log.d("DEBUG", "set google success");
				SetGoogleActivity.this.finish();	// 元のアクティビティに戻る
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			SetGoogleActivity.this.finish();	// 元のアクティビティに戻る
		}
	}
}
