package jp.ac.titech.itpro.sds.fragile;

import java.io.UnsupportedEncodingException;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.api.constant.FriendConstant;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.fragile_t.friendEndpoint.FriendEndpoint;
import com.appspot.fragile_t.friendEndpoint.FriendEndpoint.FriendV1Endpoint.Friendship;
import com.appspot.fragile_t.friendEndpoint.model.FriendResultV1Dto;
import com.appspot.fragile_t.getUserEndpoint.GetUserEndpoint;
import com.appspot.fragile_t.getUserEndpoint.GetUserEndpoint.GetUserV1Endpoint.GetUser;
import com.appspot.fragile_t.getUserEndpoint.model.GetUserResultV1Dto;
import com.appspot.fragile_t.pushMessageEndpoint.PushMessageEndpoint;
import com.appspot.fragile_t.pushMessageEndpoint.PushMessageEndpoint.PushMessageV1Endpoint.SendMessageFromRegisterId;
import com.appspot.fragile_t.pushMessageEndpoint.model.PushMessageResultV1Dto;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class FriendActivity extends Activity implements
		CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private static final int REQUEST_PICK_CONTACT = 1;

	/**
	 * Keep track of the register task to ensure we can cancel it if requested.
	 */
	private FriendTask mAuthTask = null;

	private String fEmail;
	private View focusView = null;
	private NfcAdapter mNfcAdapter;
	private SharedPreferences pref;

	// フレンド登録成功かどうかのメッセージを渡すためのラベル
	public final static String EXTRA_MESSAGE = "Friend_register_check_message";

	// UI references.
	private EditText fEmailView;

	// ProgressDialog用
	private View mFriendFormView;
	private View mFriendStatusView;
	private TextView mFriendStatusMessageView;

	private static final String SUCCESS = CommonConstant.SUCCESS;
	private static final String FAIL = CommonConstant.FAIL;

	private static final String NULLMY = FriendConstant.NULLMY;
	private static final String NOFRIEND = FriendConstant.NOFRIEND;
	private static final String ALREADY = FriendConstant.ALREADY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_friend);
		pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
		fEmailView = (EditText) findViewById(R.id.femail);
		findViewById(R.id.friend_regist).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						attemptFriend();
					}
				});

		// ボタン作成
		Button logged_btn = (Button) findViewById(R.id.go_to_logged_from_friend);
		logged_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { 
				finish();
			}
		});

		// ProgressDialogのView作成
		mFriendFormView = findViewById(R.id.friendreg_form); // レイアウト全体
		mFriendStatusView = findViewById(R.id.friendreg_status);
		mFriendStatusMessageView = (TextView) findViewById(R.id.friendreg_status_message);
		// ボタン作成
		Button email_btn = (Button) findViewById(R.id.friend_email);
		email_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { // ログイン画面へ遷移
				Intent intent = new Intent(Intent.ACTION_PICK,
						Contacts.CONTENT_URI);
				startActivityForResult(intent, REQUEST_PICK_CONTACT);
			}
		});

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter != null) {
			mNfcAdapter.setNdefPushMessageCallback(this, this);
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		}
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
			// ProgressDialogを表示
			mFriendStatusMessageView
					.setText(R.string.friend_progress_registering);
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
			if(returnedIntent == null) {
				return;
			}
			Uri result = returnedIntent.getData();
			String id = result.getLastPathSegment();
			if (requestCode == REQUEST_PICK_CONTACT && resultCode == Activity.RESULT_OK) {
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
				FriendEndpoint endpoint = RemoteApi.getFriendEndpoint();
				Friendship friend = endpoint.friendV1Endpoint().friendship(
						fEmail, pref.getString("email", ""));

				FriendResultV1Dto result = friend.execute();

				if (SUCCESS.equals(result.getResult())) {
					
					try{
						// プッシュ通知を行う
						PushMessageEndpoint endpoint2 = RemoteApi.getPushMessageEndpoint();
						SendMessageFromRegisterId pushmsg = endpoint2.pushMessageV1Endpoint().sendMessageFromRegisterId(pref.getString("email", ""), fEmail);
						PushMessageResultV1Dto result2 = pushmsg.execute();
						Log.d("DEBUG", "プッシュリザルト："+result2.getResult());	
					} catch (Exception e) {
						Log.d("DEBUG", e.toString());
						Log.d("DEBUG", "プッシュ送信に失敗しました");
					}
					return true;
				} else if (NULLMY.equals(result.getResult())) {
					fEmailView.setError(getString(R.string.error_f_nullmy));
					focusView = fEmailView;
					return false;
				} else if (NOFRIEND.equals(result.getResult())) {
					fEmailView.setError(getString(R.string.error_f_nof));
					focusView = fEmailView;
					return false;
				} else if (ALREADY.equals(result.getResult())) {
					fEmailView.setError(getString(R.string.error_f_already));
					focusView = fEmailView;
					return false;
				} else {
					return false;
				}

			} catch (Exception e) {
				Log.d("DEBUG", e.toString());
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Log.d("DEBUG", "登録成功");
				// 成功した場合、トースト表示
				Toast.makeText(getApplicationContext(),"友人申請を行いました", Toast.LENGTH_SHORT).show();
				// メール入力欄を空に
				fEmailView.setText("");

				
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

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
	}

	// android beam を受け取った時の処理
	private void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];

		byte[] data = msg.getRecords()[0].getPayload();
		try {
			fEmail = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fEmailView.setText(fEmail);
	}

	public void onNdefPushComplete(NfcEvent event) {
		Log.d("DEBUG", "onNdefComplete");
	}

	/*
	 * android beam が起動した時の処理
	 */
	public NdefMessage createNdefMessage(NfcEvent event) {
		NdefMessage msg = null;
		String myemail = pref.getString("email", "");
		if (myemail != null) {
			byte[] data;
			try {
				data = myemail.getBytes("UTF-8");
				msg = new NdefMessage(NdefRecord.createMime(
						"application/jp.ac.titech.itpro.sds.fragile", data));
			} catch (Exception e) {
				Log.d("DEBUG", "serialize fail");
			}
		}
		return msg;
	}
}
