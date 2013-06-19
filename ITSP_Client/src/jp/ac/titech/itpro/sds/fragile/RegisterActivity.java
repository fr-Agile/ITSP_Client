package jp.ac.titech.itpro.sds.fragile;

import java.util.List;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.services.registerEndpoint.RegisterEndpoint;
import com.google.api.services.registerEndpoint.RegisterEndpoint.RegisterV1Endpoint.Register;
import com.google.api.services.registerEndpoint.model.RegisterV1ResultDto;

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
	private EditText mFirstNameView;
	private EditText mLastNameView;
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mPasswordAgainView;
	private View mRegisterFormView;
	private View mRegisterStatusView;
	private TextView mRegisterStatusMessageView;

    private static String SUCCESS = "success";
    private static String FAIL = "fail";
    private static String NULL_FNAME = "null_fname";
    private static String NULL_LNAME = "null_lname";
    private static String NULL_EMAIL = "null_email";
    private static String NULL_PASS = "null_pass";
    private static String NULL_PASSA = "null_passa";
    private static String INVALID_ADDRESS = "invalid_address";
    private static String EXISTING_ADDRESS = "existing_address";
    private static String SHORT_PASS = "short_pass";
    private static String DIFFERENT_PASS = "different_pass";
    private static String UNEXPECTED_ERROR = "unexpected_error";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		// Set up the register form.
		mFirstNameView = (EditText) findViewById(R.id.register_firstname);
		mFirstNameView.setText(mFirstName);
		
		mLastNameView = (EditText) findViewById(R.id.register_lastname);
		mLastNameView.setText(mLastName);
		
		mEmailView = (EditText) findViewById(R.id.register_email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.register_password);
		mPasswordView.setText(mPassword);

		mPasswordAgainView = (EditText) findViewById(R.id.register_password_again);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
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
		mFirstNameView.setError(null);
		mLastNameView.setError(null);
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mPasswordAgainView.setError(null);

		// Store values at the time of the register attempt.
		mFirstName = mFirstNameView.getText().toString();
		mLastName = mLastNameView.getText().toString();
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mPasswordAgain = mPasswordAgainView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check null elements.
		if (TextUtils.isEmpty(mFirstName)) {
			mFirstNameView.setError(getString(R.string.error_field_required));
			focusView = mFirstNameView;
			//cancel = true;
		}
		if (TextUtils.isEmpty(mLastName)) {
			mLastNameView.setError(getString(R.string.error_field_required));
			focusView = mLastNameView;
			//cancel = true;
		}
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			//cancel = true;
		}
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			//cancel = true;
		}
		if (TextUtils.isEmpty(mPasswordAgain)) {
			mPasswordAgainView.setError(getString(R.string.error_field_required));
			focusView = mPasswordAgainView;
			//cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt register and focus the first
			// form field with an error.
			focusView.requestFocus();
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
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Log.d("DEBUG", "register success");
				startActivity(new Intent(RegisterActivity.this, RegisteredActivity.class));
				finish();
			} else {
				/*
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
				mPasswordView.setError("test");
				mPasswordView.requestFocus();
				
				mEmailView.setError("test");
				mEmailView.requestFocus();
				*/
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
			checkFirstNameError(errorList);
			checkLastNameError(errorList);
			checkEmailError(errorList);
			checkPasswordError(errorList);
		}
		
		private void checkFirstNameError(List<String> errorList) {
			if(errorList.contains(NULL_FNAME)) {
				mFirstNameView.setError(getString(R.string.register_error_field_required));
				//mFirstNameView.requestFocus();
			}
		}
		
		private void checkLastNameError(List<String> errorList) {
			if(errorList.contains(NULL_LNAME)) {
				mLastNameView.setError(getString(R.string.register_error_field_required));
				//mLastNameView.requestFocus();
			}
		}
		
		private void checkEmailError(List<String> errorList) {
			if(errorList.contains(NULL_EMAIL)) {
				mEmailView.setError(getString(R.string.register_error_field_required));
				//mEmailView.requestFocus();
			} else if(errorList.contains(INVALID_ADDRESS)) {
				mEmailView.setError(getString(R.string.register_error_invalid_email));
				//mEmailView.requestFocus();
			} else if(errorList.contains(EXISTING_ADDRESS)) {
				mEmailView.setError(getString(R.string.register_error_existing_email));
				//mEmailView.requestFocus();
			}
		}
		
		private void checkPasswordError(List<String> errorList) {
			if(errorList.contains(NULL_PASS)) {
				mPasswordView.setError(getString(R.string.register_error_field_required));
				//mPasswordView.requestFocus();
			} else if(errorList.contains(SHORT_PASS)) {
				mPasswordView.setError(getString(R.string.register_error_short_password));
				//mPasswordView.requestFocus();
			}
			if(errorList.contains(NULL_PASSA)) {
				mPasswordAgainView.setError(getString(R.string.register_error_field_required));
				//mPasswordAgainView.requestFocus();
			} else if(errorList.contains(DIFFERENT_PASS)) {
				mPasswordAgainView.setError(getString(R.string.register_error_different_password));
				//mPasswordAgainView.requestFocus();
			}
		}
	}
}
