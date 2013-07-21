package jp.ac.titech.itpro.sds.fragile;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.services.friendEndpoint.FriendEndpoint;
import com.google.api.services.friendEndpoint.FriendEndpoint.FriendV1Endpoint.Friendship;
import com.google.api.services.friendEndpoint.model.FriendResultV1Dto;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class FriendActivity extends Activity {
	private static final int REQUEST_PICK_CONTACT = 1;

	/**
	 * Keep track of the register task to ensure we can cancel it if requested.
	 */
	private FriendTask mAuthTask = null;

	private String fEmail;
	private View focusView = null;
	
	//フレンド登録成功かどうかのメッセージを渡すためのラベル
	public final static String EXTRA_MESSAGE = "Friend_register_check_message";
	
	// UI references.
	private EditText fEmailView;

	//ProgressDialog用
	private View mFriendFormView;
	private View mFriendStatusView;
	private TextView mFriendStatusMessageView;
	
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
		
		//ボタン作成
	    Button logged_btn = (Button)findViewById(R.id.go_to_logged_from_friend);
	    logged_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  //メニュー画面へ遷移
	    		startActivity(new Intent(FriendActivity.this, LoggedActivity.class));
	    	}
	    });
	    
	    //ProgressDialogのView作成
	    mFriendFormView = findViewById(R.id.friendreg_form);  //レイアウト全体
	    mFriendStatusView = findViewById(R.id.friendreg_status);
	    mFriendStatusMessageView = (TextView) findViewById(R.id.friendreg_status_message);
		//ボタン作成
	    Button email_btn = (Button)findViewById(R.id.friend_email);
	    email_btn.setOnClickListener(new View.OnClickListener() {
			@Override
	    	public void onClick(View v) {  //ログイン画面へ遷移
				Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
				startActivityForResult(intent, REQUEST_PICK_CONTACT);
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
				//ProgressDialogを表示
				mFriendStatusMessageView.setText(R.string.friend_progress_registering);
				showProgress(true);
				
				mAuthTask = new FriendTask();
				mAuthTask.execute((Void) null);
			}
		}
		
		/**
		 * Shows the progress UI and hides the friend form.
		 */
		@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
		private void showProgress(final boolean show) {
			// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
			// for very easy animations. If available, use these APIs to fade-in
			// the progress spinner.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				int shortAnimTime = getResources().getInteger(
						android.R.integer.config_shortAnimTime);

				mFriendStatusView.setVisibility(View.VISIBLE);
				mFriendStatusView.animate().setDuration(shortAnimTime)
						.alpha(show ? 1 : 0)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mFriendStatusView.setVisibility(show ? View.VISIBLE
										: View.GONE);
							}
						});

				mFriendFormView.setVisibility(View.VISIBLE);
				mFriendFormView.animate().setDuration(shortAnimTime)
						.alpha(show ? 0 : 1)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								mFriendFormView.setVisibility(show ? View.GONE
										: View.VISIBLE);
							}
						});
			} else {
				// The ViewPropertyAnimator APIs are not available, so simply show
				// and hide the relevant UI components.
				mFriendStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
				mFriendFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			}
		}
		
		public void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
			super.onActivityResult(requestCode, resultCode, returnedIntent);
			String email = "";
			Uri result = returnedIntent.getData();
			String id = result.getLastPathSegment();
			if (resultCode == Activity.RESULT_OK) {
				ContentResolver resolver = getContentResolver();
				Cursor mailCursor = resolver.query(
						ContactsContract.CommonDataKinds.Email.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? ",
						new String[]{id}, null);
						
				while(mailCursor.moveToNext()) {
					email = mailCursor.getString(mailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA1));
				}
				mailCursor.close();
			}
			fEmailView.setText(email);
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
			protected void onPostExecute(final Boolean success) {
				mAuthTask = null;
				showProgress(false);

				if (success) {
					Log.d("DEBUG", "登録成功");
					//成功した場合、トップ画面に戻る
					Intent next_intent = new Intent(FriendActivity.this, LoggedActivity.class);
					next_intent.putExtra(EXTRA_MESSAGE, getString(R.string.friend_register_ok));
					startActivity(next_intent);
					finish();
				} else {
					Log.d("DEBUG", "登録失敗");
				}
			}

			@Override
			protected void onCancelled() {
				mAuthTask = null;
				showProgress(false);
			}
		}
}
