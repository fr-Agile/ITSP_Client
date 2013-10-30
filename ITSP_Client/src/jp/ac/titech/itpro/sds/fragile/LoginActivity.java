package jp.ac.titech.itpro.sds.fragile;

import java.util.Calendar;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.utils.CommonUtils;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.appspot.fragile_t.loginEndpoint.LoginEndpoint;
import com.appspot.fragile_t.loginEndpoint.LoginEndpoint.LoginV1Endpoint.Login;
import com.appspot.fragile_t.loginEndpoint.model.LoginResultV1Dto;
import com.appspot.fragile_t.registrationIdEndpoint.RegistrationIdEndpoint;
import com.appspot.fragile_t.registrationIdEndpoint.RegistrationIdEndpoint.RegistrationIdV1Endpoint.RegisterId;
import com.appspot.fragile_t.registrationIdEndpoint.model.RegisterIdResultV1Dto;
import com.google.android.gcm.GCMRegistrar;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	private static String SUCCESS = CommonConstant.SUCCESS;
	private static String FAIL = CommonConstant.FAIL;
	
	private static SharedPreferences pref;
	
	// private String regId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ID取得
		/*
	    regId = GCMRegistrar.getRegistrationId(this);
	    if (regId==null) {
	       
	        // 未登録の場合、登録
	        GCMRegistrar.register(this, CommonUtils.GCM_SENDER_ID);
	       
	    }
	    */


		setContentView(R.layout.activity_login);
		
		pref = getSharedPreferences("user", Activity.MODE_PRIVATE);

		// Set up the login form.
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		//ボタン作成
	    Button register_btn = (Button)findViewById(R.id.go_to_register_from_login);
	    register_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  //新規登録画面へ遷移
	    		startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
	    	}
	    });
	    
	    // 2回目以降の起動時（メールアドレスが保存されているとき）
		if (!pref.getString("email", "").equals("")) {
			// スケジュール画面へ遷移
			Intent intent = new Intent(LoginActivity.this, ScheduleActivity.class);
    		Calendar nowCal = Calendar.getInstance();
//    		nowCal.add(Calendar.DAY_OF_YEAR, 7);
    		StoreData data = new StoreData(nowCal);
    		intent.putExtra("StoreData", data);
    		intent.setAction(Intent.ACTION_VIEW);
	        startActivity(intent);
		}
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
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

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				LoginEndpoint endpoint = RemoteApi.getLoginEndpoint();
				Login login = endpoint.loginV1Endpoint().login(mEmail,
						mPassword);
				
				/*
				RegistrationIdEndpoint endpoint2 = RemoteApi.getRegistrationIdEndpoint();
				RegisterId registerId = endpoint2.registrationIdV1Endpoint().registerId(regId, mEmail);
				RegisterIdResultV1Dto rs = registerId.execute();
				*/
				
				//ログイン中のユーザー情報をpreferenceに格納して用いることができるようにする
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("email",mEmail);  
			    editor.commit();
				
				LoginResultV1Dto result = login.execute();

				if (SUCCESS.equals(result.getResult())) {
					return true;
				} else {
					return false;
				}

			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {

					Log.d("DEBUG", "ログイン成功");
		    		Intent intent = new Intent(LoginActivity.this, ScheduleActivity.class);
		    		Calendar nowCal = Calendar.getInstance();
//		    		nowCal.add(Calendar.DAY_OF_YEAR, 7);
		    		StoreData data = new StoreData(nowCal);
		    		intent.putExtra("StoreData", data);
		    		intent.setAction(Intent.ACTION_VIEW);
			        startActivity(intent);
			        
					finish();
					
					
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
