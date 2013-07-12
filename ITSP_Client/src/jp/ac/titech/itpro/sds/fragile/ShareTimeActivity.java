package jp.ac.titech.itpro.sds.fragile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.GetFriendTask.GetFriendFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;

import com.google.api.services.getFriendEndpoint.model.GetFriendResultV1Dto;
import com.google.api.services.getFriendEndpoint.model.UserV1Dto;
import com.google.api.services.getShareTimeEndpoint.GetShareTimeEndpoint;
import com.google.api.services.getShareTimeEndpoint.GetShareTimeEndpoint.GetShareTimeV1Endpoint.GetShareTime;
import com.google.api.services.getShareTimeEndpoint.model.GetShareTimeV1ResultDto;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ShareTimeActivity extends Activity implements GetFriendFinishListener {
	/**
	 * Keep track of the register task to ensure we can cancel it if requested.
	 */
	private UserRegisterTask mAuthTask = null;

	// Values for firstName, lastName, email, password at the time of the register attempt.
	private String mUserEmail;
	private String mEmailCSV;
	private long mStartTime;
	private long mFinishTime;
	private List<UserV1Dto> mFriendList;
	private List<String> mFriendEmailList;

	// UI references.
	private ListView mFriendListView;
	private TextView mShareTimeTextView;

    private final static String SUCCESS = "success";
    private final static String FAIL = "fail";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sharetime);

		// Set up the sharetime form.
		mFriendListView = (ListView) findViewById(R.id.sharetime_list);
		mShareTimeTextView = (TextView) findViewById(R.id.sharetime_text);

		// リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        mFriendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                // クリックされたアイテムを取得します
                String item = (String) listView.getItemAtPosition(position);
                mShareTimeTextView.setText(item);
            }
        });

		
		initFriendList();
	}
	
	/**
	 * friend listを取得する
	 */
	private void initFriendList() {
		try {
			/*
			SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
			String userEmail = pref.getString("email", "");
			*/
			mUserEmail = "test@test.com";
			
			// friendのemailリストを取得する
			GetFriendTask task = new GetFriendTask(this);
			task.execute(mUserEmail);
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "initFriend fail");
		}
	}
	/**
	 * friend list取得後、List Viewにして表示する
	 */
	@Override
	public void onFinish(GetFriendResultV1Dto result) {
		if (result != null) {
			mFriendEmailList = new ArrayList<String>();
			mFriendList = result.getFriendList();
			for (UserV1Dto friend : mFriendList) {
				mFriendEmailList.add(friend.getEmail());
			}
			
			// ListViewの中身はadapterごしに管理する
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_list_item_1, mFriendEmailList);
			mFriendListView.setAdapter(adapter);
		}
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

		mAuthTask = new UserRegisterTask();
		mAuthTask.execute((Void) null);
	}
	

	
	/**
	 * Represents an asynchronous registration task used to authenticate
	 * the user.
	 */
	public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				GetShareTimeEndpoint endpoint = RemoteApi.getGetShareTimeEndpoint();
				GetShareTime getShareTime = endpoint.getShareTimeV1Endpoint().getShareTime(
						mEmailCSV, mStartTime, mFinishTime);
				GetShareTimeV1ResultDto result = getShareTime.execute();

				
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

			if (success) {
				Log.d("DEBUG", "getShareTime success");
				//startActivity(new Intent(RegisterActivity.this, RegisteredActivity.class));
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}
	}

}
