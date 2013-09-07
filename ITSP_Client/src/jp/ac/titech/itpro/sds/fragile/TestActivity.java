package jp.ac.titech.itpro.sds.fragile;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleCalendarLoader;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

import android.app.LoaderManager;
import android.content.Loader;

import com.appspot.fragile_t.loginEndpoint.LoginEndpoint;
import com.appspot.fragile_t.loginEndpoint.LoginEndpoint.LoginV1Endpoint.Login;
import com.appspot.fragile_t.loginEndpoint.model.LoginResultV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateScheduleList;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.DeleteAllSchedule;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleListContainer;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleResultV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleV1Dto;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class TestActivity extends Activity implements LoaderCallbacks<Cursor> {
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */

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
	
	private DeleteTask mDelTask;
	private AddTask mAddTask;
	
	private List<ScheduleV1Dto> scheList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
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
					}
				});
		
		//ボタン作成
	    Button register_btn = (Button)findViewById(R.id.go_to_register_from_login);
	    register_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  //新規登録画面へ遷移
				getLoaderManager().initLoader(0, null, TestActivity.this);
	    	}
	    });
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new GoogleCalendarLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		if (arg1.moveToFirst()) {
			scheList = new ArrayList<ScheduleV1Dto>();
			
			do {
				ScheduleV1Dto sche = new ScheduleV1Dto();
				sche.setKey(arg1.getString(4));
				sche.setStartTime(Long.parseLong(arg1.getString(2)));
				sche.setFinishTime(Long.parseLong(arg1.getString(3)));
				scheList.add(sche);
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(sche.getStartTime());
				long g = cal.getTimeInMillis();
				
			} while (arg1.moveToNext());
		}
		mDelTask = new DeleteTask();
		mDelTask.execute();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
	}
	
	public class DeleteTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				DeleteAllSchedule delete = endpoint.scheduleV1EndPoint().deleteAllSchedule("a@a");
				
				ScheduleResultV1Dto result = delete.execute();
				
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
			mDelTask = null;

			if (success) {
				Log.d("DEBUG", "del作成");
				mAddTask = new AddTask();
				mAddTask.execute();
			} else {
				Log.d("DEBUG", "del失敗");
			}
		}

		@Override
		protected void onCancelled() {
			mDelTask = null;
		}
	}
	
	public class AddTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				ScheduleListContainer contain = new ScheduleListContainer();
				contain.setList(scheList);
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				CreateScheduleList create = 
						endpoint.scheduleV1EndPoint().createScheduleList("a@a", contain);
				
				ScheduleResultV1Dto result = create.execute();
				
				if (SUCCESS.equals(result.getResult())) {
					return true;
				} else {
					return false;
				}

			} catch (Exception e) {
				Log.d("DEBUG", "create失敗" + e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mDelTask = null;

			if (success) {
				Log.d("DEBUG", "create成功");
				
			} else {
			}
		}

		@Override
		protected void onCancelled() {
			mDelTask = null;
		}
	}
}

