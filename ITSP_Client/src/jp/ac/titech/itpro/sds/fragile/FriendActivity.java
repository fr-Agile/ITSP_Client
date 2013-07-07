package jp.ac.titech.itpro.sds.fragile;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import com.google.api.services.friendEndpoint.FriendEndpoint;
import com.google.api.services.friendEndpoint.FriendEndpoint.FriendV1Endpoint.Friendship;
import com.google.api.services.friendEndpoint.model.FriendResultV1Dto;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class FriendActivity extends Activity {
	/**
	 * Keep track of the register task to ensure we can cancel it if requested.
	 */
	private FriendTask mAuthTask = null;

	private String fEmail;
	private View focusView = null;
	
	// UI references.
	private EditText fEmailView;

	private static String SUCCESS = "success";
	private static String FAIL = "fail";

    private static String NULLMY = "nullmy";
    private static String NOFRIEND = "nofriend";
    private static String ALREADY = "already";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_friend);

		fEmailView = (EditText) findViewById(R.id.femail);
		findViewById(R.id.friend_regist).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						attemptFriend();
						}
					});
		}

		/**
		 * Attempts to sign in or register the account specified by the login form.
		 * If there are form errors (invalid email, missing fields, etc.), the
		 * errors are presented and no actual login attempt is made.
		 */
		public void attemptFriend() {
			if (mAuthTask != null) {
				return;
			}

			// Reset errors.
			fEmailView.setError(null);
			// Store values at the time of the login attempt.
			fEmail = fEmailView.getText().toString();

			boolean cancel = false;

			// Check for a valid email address.
			if (TextUtils.isEmpty(fEmail)) {
				fEmailView.setError(getString(R.string.error_f_empty));
				focusView = fEmailView;
				cancel = true;
			} else if (!fEmail.contains("@")) {
				fEmailView.setError(getString(R.string.error_f_email));
				focusView = fEmailView;
				cancel = true;
			}

			if (cancel) {
				focusView.requestFocus();
			} else {
				mAuthTask = new FriendTask();
				mAuthTask.execute((Void) null);
			}
		}

		/**
		 * Represents an asynchronous login/registration task used to authenticate
		 * the user.
		 */
		public class FriendTask extends AsyncTask<Void, Void, Boolean> {
			@Override
			protected Boolean doInBackground(Void... args) {

				try {
					SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
				
					FriendEndpoint endpoint = RemoteApi.getFriendEndpoint();
					Friendship friend = endpoint.friendV1Endpoint().friendship(fEmail,
							pref.getString("email",""));
					
					FriendResultV1Dto result = friend.execute();

					if (SUCCESS.equals(result.getResult())) {
						return true;
					}else if (NULLMY.equals(result.getResult())){
						fEmailView.setError(getString(R.string.error_f_nullmy));
						focusView = fEmailView;
						return false;
					}else if (NOFRIEND.equals(result.getResult())){
						fEmailView.setError(getString(R.string.error_f_nof));
						focusView = fEmailView;
						return false;
					}else if (ALREADY.equals(result.getResult())){
						fEmailView.setError(getString(R.string.error_f_already));
						focusView = fEmailView;
						return false;
					}else{
						return false;
					}

				} catch (Exception e) {
					return false;
				}
			}

			@Override
			protected void onCancelled() {
				mAuthTask = null;
			}
		}
}
