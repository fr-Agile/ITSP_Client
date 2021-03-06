package jp.ac.titech.itpro.sds.fragile;

import java.util.List;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.api.constant.RegisterConstant;
import jp.ac.titech.itpro.sds.fragile.utils.AddressChecker;
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
import android.view.View;
import android.widget.TextView;

import com.appspot.fragile_t.registerEndpoint.RegisterEndpoint;
import com.appspot.fragile_t.registerEndpoint.RegisterEndpoint.RegisterV1Endpoint.Register;
import com.appspot.fragile_t.registerEndpoint.model.RegisterV1ResultDto;
import com.google.api.client.util.Lists;

/**
 * Activity which displays a register screen to the user
 * well.
 */
public class RegisterActivity extends Activity {
	/**
	 * Keep track of the register task to ensure we can cancel it if requested.
	 */
	private UserRegisterTask mAuthTask = null;

	// Values for firstName, lastName, email, password at the time of the register attempt.
	private String mFirstName;
	private String mLastName;
	private String mEmail;
	private String mPassword;
	private String mPasswordAgain;

	// UI references.
	private MyEditText mFirstNameView;
	private MyEditText mLastNameView;
	private MyEditText mEmailView;
	private MyEditText mPasswordView;
	private MyEditText mPasswordAgainView;
	private View mRegisterFormView;
	private View mRegisterStatusView;
	private TextView mRegisterStatusMessageView;

    private static final String SUCCESS = CommonConstant.SUCCESS;
    private static final String FAIL = CommonConstant.FAIL;
    private static final String NULL_FNAME = RegisterConstant.NULL_FNAME;
    private static final String NULL_LNAME = RegisterConstant.NULL_LNAME;
    private static final String NULL_EMAIL = RegisterConstant.NULL_EMAIL;
    private static final String NULL_PASS = RegisterConstant.NULL_PASS;
    private static final String NULL_PASSA = RegisterConstant.NULL_PASSA;
    private static final String INVALID_ADDRESS = RegisterConstant.INVALID_ADDRESS;
    private static final String EXISTING_ADDRESS = RegisterConstant.EXISTING_ADDRESS;
    private static final String SHORT_PASS = RegisterConstant.SHORT_PASS;
    private static final String DIFFERENT_PASS = RegisterConstant.DIFFERENT_PASS;
    private static final String UNEXPECTED_ERROR = RegisterConstant.UNEXPECTED_ERROR;
    private static final int PASS_LENGTH = RegisterConstant.PASS_LENGTH;

    private static final int SET_GOOGLE = 100;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		// Set up the register form.
		
		mFirstNameView = (MyEditText) findViewById(R.id.register_firstname);
		mFirstNameView.setText(mFirstName);
		
		mLastNameView = (MyEditText) findViewById(R.id.register_lastname);
		mLastNameView.setText(mLastName);
		
		mEmailView = (MyEditText) findViewById(R.id.register_email);
		mEmailView.setText(mEmail);

		mPasswordView = (MyEditText) findViewById(R.id.register_password);
		mPasswordView.setText(mPassword);

		mPasswordAgainView = (MyEditText) findViewById(R.id.register_password_again);
		mPasswordAgainView.setText(mPasswordAgain);

		mRegisterFormView = findViewById(R.id.register_form);
		mRegisterStatusView = findViewById(R.id.register_status);
		mRegisterStatusMessageView = (TextView) findViewById(R.id.register_status_message);

		findViewById(R.id.register_sign_up_button).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						
						attemptRegister();
					}
				});
	}


	/**
	 * Attempts to sign in or register the account specified by the register form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual register attempt is made.
	 */
	public void attemptRegister() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mFirstNameView.setMyError(null);
		mLastNameView.setMyError(null);
		mEmailView.setMyError(null);
		mPasswordView.setMyError(null);
		mPasswordAgainView.setMyError(null);

		// Store values at the time of the register attempt.
		mFirstName = mFirstNameView.getText().toString();
		mLastName = mLastNameView.getText().toString();
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mPasswordAgain = mPasswordAgainView.getText().toString();

		boolean cancel = false;

		// Validate inputs
		if (!validateFirstNameBeforeConnect()) {
			cancel = true;
		}
		if (!validateLastNameBeforeConnect()) {
			cancel = true;
		}
		if (!validateEmailBeforeConnect()) {
			cancel = true;
		}
		if (!validatePasswordBeforeConnect()) {
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt register
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user register attempt.
			mRegisterStatusMessageView.setText(R.string.register_progress_signing_up);
			showProgress(true);
			mAuthTask = new UserRegisterTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the register form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mRegisterStatusView.setVisibility(View.VISIBLE);
			mRegisterStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegisterStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mRegisterFormView.setVisibility(View.VISIBLE);
			mRegisterFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegisterFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous registration task used to authenticate
	 * the user.
	 */
	public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				RegisterEndpoint endpoint = RemoteApi.getRegisterEndpoint();
				Register register = endpoint.registerV1Endpoint().register(
						mFirstName, mLastName, mEmail, mPassword, mPasswordAgain);
				RegisterV1ResultDto result = register.execute();

				
				if (SUCCESS.equals(result.getResult())) {
					return true;
				} else {
					setErrorMessage(result);
					return false;
				}
			} catch (Exception e) {
				RegisterV1ResultDto result = new RegisterV1ResultDto();
				List<String> errorList = Lists.newArrayList();
				errorList.add(UNEXPECTED_ERROR);
				setErrorMessage(result);
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				// 登録成功
				Log.d("DEBUG", "register success");
				// メールを保存しておく
				SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("email", mEmail);
				editor.commit();
				// googleアカウントを設定させる
				startActivityForResult(new Intent(RegisterActivity.this, SetGoogleActivity.class),
						SET_GOOGLE);
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
		
		private void setErrorMessage(RegisterV1ResultDto result) {
			List<String> errorList = result.getErrorList();
			if (errorList == null) {
				return;
			}
			validateFirstNameAfterConnect(errorList);
			validateLastNameAfterConnect(errorList);
			validateEmailAfterConnect(errorList);
			validatePasswordAfterConnect(errorList);
		}
		
		private void validateFirstNameAfterConnect(List<String> errorList) {
			if (errorList.contains(NULL_FNAME)) {
				mFirstNameView.setMyError(getString(R.string.register_error_field_required));
			}
		}
		
		private void validateLastNameAfterConnect(List<String> errorList) {
			if (errorList.contains(NULL_LNAME)) {
				mLastNameView.setMyError(getString(R.string.register_error_field_required));
			}
		}
		
		private void validateEmailAfterConnect(List<String> errorList) {
			if (errorList.contains(NULL_EMAIL)) {
				mEmailView.setMyError(getString(R.string.register_error_field_required));
			} else if (errorList.contains(INVALID_ADDRESS)) {
				mEmailView.setMyError(getString(R.string.register_error_invalid_email));
			} else if (errorList.contains(EXISTING_ADDRESS)) {
				mEmailView.setMyError(getString(R.string.register_error_existing_email));
			}
		}
		
		private void validatePasswordAfterConnect(List<String> errorList) {
			if (errorList.contains(NULL_PASS)) {
				mPasswordView.setMyError(getString(R.string.register_error_field_required));
			} else if (errorList.contains(SHORT_PASS)) {
				mPasswordView.setMyError(getString(R.string.register_error_short_password));
			}
			if (errorList.contains(NULL_PASSA)) {
				mPasswordAgainView.setMyError(getString(R.string.register_error_field_required));
			} else if (errorList.contains(DIFFERENT_PASS)) {
				mPasswordAgainView.setMyError(getString(R.string.register_error_different_password));
			}
		}
	}
	
	private boolean validateFirstNameBeforeConnect() {
        if (TextUtils.isEmpty(mFirstName)) {
			mFirstNameView.setMyError(getString(R.string.register_error_field_required));
			return false;
		}
        return true;
	}
	
	private boolean validateLastNameBeforeConnect() {
        if (TextUtils.isEmpty(mLastName)) {
			mLastNameView.setMyError(getString(R.string.register_error_field_required));
			return false;
		}
        return true;
	}
	
	private boolean validateEmailBeforeConnect() {
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setMyError(getString(R.string.register_error_field_required));
			return false;
		} else if (!AddressChecker.check(mEmail)) {
			mEmailView.setMyError(getString(R.string.register_error_invalid_email));
			return false;
		}
		return true;
	}
	
	private boolean validatePasswordBeforeConnect() {
		boolean success = true;
		
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setMyError(getString(R.string.register_error_field_required));
			success = false;
		} else if (mPassword.length() < PASS_LENGTH) {
			mPasswordView.setMyError(getString(R.string.register_error_short_password));
			success = false;
		}
		if (TextUtils.isEmpty(mPasswordAgain)) {
			mPasswordAgainView.setMyError(getString(R.string.register_error_field_required));
			success = false;
		} else if (!mPasswordAgain.equals(mPassword)) {
			mPasswordAgainView.setMyError(getString(R.string.register_error_different_password));
			success = false;
		}
		return success;
	}
	
	public void onActivityResult(int reqestCode, int result, Intent intent) {
		if ((reqestCode == SET_GOOGLE) && (result == RESULT_OK)) {
			// SetGoogleActivityから戻ってきたらログイン画面に遷移する
			startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
			finish();
		}
	}
}
