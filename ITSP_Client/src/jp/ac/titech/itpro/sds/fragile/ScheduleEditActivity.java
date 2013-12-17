package jp.ac.titech.itpro.sds.fragile;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.GetUserTask.GetUserFinishListener;
import jp.ac.titech.itpro.sds.fragile.GoogleCalendarExporter.GoogleCalendarExportFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.api.constant.GoogleConstant;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarUtils;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleAccountChecker;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleAccountChecker.GoogleAccountCheckFinishListener;
import jp.ac.titech.itpro.sds.fragile.utils.RepeatUtils;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.appspot.fragile_t.getUserEndpoint.model.GetUserResultV1Dto;
import com.appspot.fragile_t.getUserEndpoint.model.UserV1Dto;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.CreateRepeatScheduleWithGId;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.CreateRepeatScheduleWithTerm;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.EditRepeatScheduleWithGId;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleContainer;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleResultV1Dto;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateGroupSchedule;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateSchedule;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateScheduleWithGId;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.EditScheduleWithGId;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleResultV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.model.StringListContainer;

public class ScheduleEditActivity extends Activity 
	implements 
		GoogleAccountCheckFinishListener, GoogleCalendarExportFinishListener,
		GetUserFinishListener {

	private static final String TAG = "ScheduleEditActivity";
	private static final String FAIL = CommonConstant.FAIL;
	private Button doneBtn, showScheduleViewBtn;
	private long scheduleStartTime;
	private long scheduleFinishTime;
	private String mEmail;
	private DatePicker mDatepicker;
	private TimePicker mStartTimePicker;
	private TimePicker mFinishTimePicker;
	private UserV1Dto mUser;

	private CheckBox repeatChk;
	private CheckBox everydayChk;
	private CheckBox monChk;
	private CheckBox tueChk;
	private CheckBox wedChk;
	private CheckBox thuChk;
	private CheckBox friChk;
	private CheckBox satChk;
	private CheckBox sunChk;
	private View repeatdaysView;

	private List<Integer> repeats;
	
	private boolean googleChecked;

	private View mInputScheduleView;
	private EditText mScheduleNameView;
	private View mSpinView;
	private static String SUCCESS = CommonConstant.SUCCESS;

	private EditRepeatScheduleTask mRepeatAuthTask = null;
	private EditScheduleTask mAuthTask = null;
	private GetUserTask mGetUserTask = null;

	private DateFormat formatDateTime = DateFormat.getDateTimeInstance();
	private Calendar startTime = Calendar.getInstance();
	private Calendar finishTime = Calendar.getInstance();
	private String keyS = "";
	private String name = "";
	private boolean repeat;
	private boolean create;
	private int[] rd;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_inputschedule);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		mInputScheduleView = findViewById(R.id.inputScheduleView);
		mScheduleNameView = (EditText) findViewById(R.id.nameInput);
		mSpinView = findViewById(R.id.spinView);
		repeatChk = (CheckBox) findViewById(R.id.repeartCheckbox);

		if (extras != null) {
			if (extras.containsKey("startTime")) {
				Calendar cal = (Calendar) extras.get("startTime");
				startTime = (Calendar) cal.clone();

				if (extras.containsKey("length")) {
					cal.add(Calendar.HOUR_OF_DAY, (int) extras.getInt("length"));
				} else {
					cal.add(Calendar.HOUR_OF_DAY, 1);
				}

				if (extras.containsKey("finishTime")) {
					finishTime = (Calendar) extras.get("finishTime");
				} else {
					finishTime = (Calendar) cal.clone();
				}
			} else {
				Calendar cal = Calendar.getInstance();
				startTime = (Calendar) cal.clone();
				cal.add(Calendar.HOUR_OF_DAY, 1);
				finishTime = (Calendar) cal.clone();
			}
		}

		scheduleStartTime = startTime.getTime().getTime();
		scheduleFinishTime = finishTime.getTime().getTime();

		mDatepicker = (DatePicker) findViewById(R.id.datePicker1);
		mStartTimePicker = (TimePicker) findViewById(R.id.timePicker1);
		mFinishTimePicker = (TimePicker) findViewById(R.id.timePicker2);

		keyS = extras.getString("key", "");
		rd = extras.getIntArray("repeatdays");
		repeat = extras.containsKey("repeat") && (Boolean) extras.get("repeat");
		create = extras.containsKey("create") && (Boolean) extras.get("create");

		if (!create) {
			repeatChk.setChecked(false);
			repeatChk.setEnabled(false);
		}
		
		if (extras.containsKey("name")) {
			name = extras.getString("name");
			mScheduleNameView.setText(name);
		} else {
			name = "新しいイベント";
		}

		mScheduleNameView.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					name = s.toString();
				} else {
					name = "新しいイベント";
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		mDatepicker.init(startTime.get(Calendar.YEAR),
				startTime.get(Calendar.MONTH),
				startTime.get(Calendar.DAY_OF_MONTH),
				new OnDateChangedListener() {
					public void onDateChanged(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						startTime.set(year, monthOfYear, dayOfMonth);
						finishTime.set(year, monthOfYear, dayOfMonth);
						scheduleStartTime = startTime.getTime().getTime();
						scheduleFinishTime = finishTime.getTime().getTime();
						setButtonEnable();
						Log.d(TAG,
								"time:"
										+ formatDateTime.format(startTime
												.getTime())
										+ " and "
										+ formatDateTime.format(finishTime
												.getTime()));
					}
				});

		mStartTimePicker.setCurrentHour(startTime.get(Calendar.HOUR_OF_DAY));
		mStartTimePicker.setCurrentMinute(startTime.get(Calendar.MINUTE));
		mStartTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
				startTime.set(Calendar.MINUTE, minute);
				scheduleStartTime = startTime.getTime().getTime();
				setButtonEnable();
				Log.d(TAG, "time:" + formatDateTime.format(startTime.getTime())
						+ " and " + formatDateTime.format(finishTime.getTime()));
			}
		});

		mFinishTimePicker.setCurrentHour(finishTime.get(Calendar.HOUR_OF_DAY));
		mFinishTimePicker.setCurrentMinute(finishTime.get(Calendar.MINUTE));
		mFinishTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				finishTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
				finishTime.set(Calendar.MINUTE, minute);
				scheduleFinishTime = finishTime.getTime().getTime();
				setButtonEnable();
				Log.d(TAG, "time:" + formatDateTime.format(startTime.getTime())
						+ " and " + formatDateTime.format(finishTime.getTime()));
			}
		});

		doneBtn = (Button) findViewById(R.id.doneBtn);
		doneBtn.setEnabled(scheduleStartTime <= scheduleFinishTime);
		doneBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				clickDoneButton();
			}
		});
		SharedPreferences pref = getSharedPreferences("user",
				Activity.MODE_PRIVATE);
		if (pref.getString("selectEmails", "").length() > 0) {
			repeatChk.setVisibility(View.INVISIBLE);
			doneBtn.setText("イベントの開催日を提案");
		}
		showScheduleViewBtn = (Button) findViewById(R.id.showScheduleViewBtn);
		showScheduleViewBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(ScheduleEditActivity.this,
						ScheduleActivity.class);
				Calendar nowCal = Calendar.getInstance();
				// nowCal.add(Calendar.DAY_OF_YEAR, 7);
				StoreData data = new StoreData(nowCal);
				intent.putExtra("StoreData", data);
				intent.setAction(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				finish();
				onDestroy();

				startActivity(intent);
			}
		});
        repeatdaysView = findViewById(R.id.repeatdaysView);
        

		sunChk = (CheckBox) findViewById(R.id.sundayCheckbox);
		monChk = (CheckBox) findViewById(R.id.mondayCheckbox);
		tueChk = (CheckBox) findViewById(R.id.tuesdayCheckbox);
		wedChk = (CheckBox) findViewById(R.id.wednesdayCheckbox);
		thuChk = (CheckBox) findViewById(R.id.thursdayCheckbox);
		friChk = (CheckBox) findViewById(R.id.fridayCheckbox);
		satChk = (CheckBox) findViewById(R.id.saturdayCheckbox);

		if (repeat) {
			repeatChk = (CheckBox) findViewById(R.id.repeartCheckbox);
			repeatdaysView.setVisibility(View.VISIBLE);
			repeatChk.setChecked(true);
			if (DayCheck(rd, 0)) {
				sunChk.setChecked(true);
			}
			if (DayCheck(rd, 1)) {
				monChk.setChecked(true);
			}
			if (DayCheck(rd, 2)) {
				tueChk.setChecked(true);
			}
			if (DayCheck(rd, 3)) {
				wedChk.setChecked(true);
			}
			if (DayCheck(rd, 4)) {
				thuChk.setChecked(true);
			}
			if (DayCheck(rd, 5)) {
				friChk.setChecked(true);
			}
			if (DayCheck(rd, 6)) {
				satChk.setChecked(true);
			}
		}
		repeatChk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					repeatdaysView.setVisibility(View.VISIBLE);
					repeat = true;
					// Toast.makeText(getApplicationContext(),"Repeat Checked:)",
					// Toast.LENGTH_LONG).show();
				} else {
					repeatdaysView.setVisibility(View.GONE);
					repeat = false;
				}
			}
		});

		everydayChk = (CheckBox) findViewById(R.id.everydayCheckbox);
		everydayChk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					sunChk.setChecked(true);
					monChk.setChecked(true);
					tueChk.setChecked(true);
					wedChk.setChecked(true);
					thuChk.setChecked(true);
					friChk.setChecked(true);
					satChk.setChecked(true);
				} else {
					sunChk.setChecked(false);
					monChk.setChecked(false);
					tueChk.setChecked(false);
					wedChk.setChecked(false);
					thuChk.setChecked(false);
					friChk.setChecked(false);
					satChk.setChecked(false);
				}
			}
		});
	}

	// 戻るボタン時に、スケジュールの変更が反映されるように新しいActivityとして遷移させる
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

				Intent intent = new Intent(ScheduleEditActivity.this,
						ScheduleActivity.class);
				Calendar nowCal = Calendar.getInstance();
				// nowCal.add(Calendar.DAY_OF_YEAR, 7);
				StoreData data = new StoreData(nowCal);
				intent.putExtra("StoreData", data);
				intent.setAction(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				finish();
				onDestroy();

				startActivity(intent);

				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.schedule_edit_activity_actions, menu);

		menu.getItem(0).setOnMenuItemClickListener(
				new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Intent intent = new Intent(ScheduleEditActivity.this,
								ScheduleActivity.class);
						Calendar nowCal = Calendar.getInstance();
						// nowCal.add(Calendar.DAY_OF_YEAR, 7);
						StoreData data = new StoreData(nowCal);
						intent.putExtra("StoreData", data);
						intent.setAction(Intent.ACTION_VIEW);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						return true;
					}
				});

		return super.onCreateOptionsMenu(menu);
	}

	public void clickDoneButton() {

		repeats = new ArrayList<Integer>();
		if (sunChk.isChecked())
			repeats.add(0);
		if (monChk.isChecked())
			repeats.add(1);
		if (tueChk.isChecked())
			repeats.add(2);
		if (wedChk.isChecked())
			repeats.add(3);
		if (thuChk.isChecked())
			repeats.add(4);
		if (friChk.isChecked())
			repeats.add(5);
		if (satChk.isChecked())
			repeats.add(6);
		
		// user情報を手に入れる(user情報をもとにgoogleにエクスポートするかを決める)
		if (mGetUserTask == null) {
			SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
			mEmail = pref.getString("email","");	
			mGetUserTask = new GetUserTask(this);
			mGetUserTask.setEmail(mEmail);
			mGetUserTask.execute();
		}
	}

	private void setButtonEnable() {
		if (scheduleStartTime == 0 || scheduleFinishTime == 0
				|| scheduleStartTime >= scheduleFinishTime) {
			doneBtn.setEnabled(false);
		} else {
			doneBtn.setEnabled(true);
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

			mSpinView.setVisibility(View.VISIBLE);
			mSpinView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mSpinView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mInputScheduleView.setVisibility(View.VISIBLE);
			mInputScheduleView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mInputScheduleView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mSpinView.setVisibility(show ? View.VISIBLE : View.GONE);
			mInputScheduleView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public class EditRepeatScheduleTask extends AsyncTask<Void, Void, Boolean> {
		private String googleIdInTask = GoogleConstant.UNTIED_TO_GOOGLE;

		public void setGoogleId(String googleId) {
			this.googleIdInTask = googleId;
		}

		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				SharedPreferences pref = getSharedPreferences("user",
						Activity.MODE_PRIVATE);
				mEmail = pref.getString("email", "");

				Log.d(TAG, "time:" + scheduleStartTime + " and "
						+ scheduleFinishTime + "email:" + mEmail);
				// repeat schedule
				RepeatScheduleContainer contain = new RepeatScheduleContainer();
				contain.setIntegers(repeats);
				RepeatScheduleV1Dto repscheDto = makeRepeatScheduleDto(name,
						scheduleStartTime, scheduleFinishTime, repeats);

				RepeatScheduleEndpoint endpoint = RemoteApi
						.getRepeatScheduleEndpoint();
				RepeatScheduleResultV1Dto result;
				if (!GoogleConstant.UNTIED_TO_GOOGLE
						.equals(this.googleIdInTask)) {
					// googleにエクスポートした
					if (create) {
						CreateRepeatScheduleWithGId repschedule = endpoint
								.repeatScheduleV1EndPoint()
								.createRepeatScheduleWithGId(
										repscheDto.getName(),
										repscheDto.getStartTime(),
										repscheDto.getFinishTime(),
										repscheDto.getRepeatBegin(),
										repscheDto.getRepeatEnd(),
										this.googleIdInTask, mEmail, contain);
						result = repschedule.execute();
					} else {
						EditRepeatScheduleWithGId repschedule = endpoint
								.repeatScheduleV1EndPoint()
								.editRepeatScheduleWithGId(keyS,
										repscheDto.getName(),
										repscheDto.getStartTime(),
										repscheDto.getFinishTime(),
										this.googleIdInTask, contain);
						result = repschedule.execute();
					}
				} else {
					if (create) {
						CreateRepeatScheduleWithTerm repschedule = endpoint
								.repeatScheduleV1EndPoint()
								.createRepeatScheduleWithTerm(
										repscheDto.getName(),
										repscheDto.getStartTime(),
										repscheDto.getFinishTime(),
										repscheDto.getRepeatBegin(),
										repscheDto.getRepeatEnd(), mEmail,
										contain);
						result = repschedule.execute();
					} else {
						// googleと紐づけずにデータベースをedit
						EditRepeatScheduleWithGId repschedule = endpoint
								.repeatScheduleV1EndPoint()
								.editRepeatScheduleWithGId(keyS,
										repscheDto.getName(),
										repscheDto.getStartTime(),
										repscheDto.getFinishTime(),
										this.googleIdInTask, contain);
						result = repschedule.execute();
					}
				}

				if ((result != null) && SUCCESS.equals(result.getResult())) {
					// Toast.makeText(getApplicationContext(),"Successed",Toast.LENGTH_SHORT).show();
					Log.d(TAG, "RS Successed");
					return true;
				} else {
					// Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
					Log.d(TAG, "RS Failed");
					return false;
				}
			} catch (Exception e) {
				// Toast.makeText(getApplicationContext(),"Failed with exception:"
				// + e,Toast.LENGTH_SHORT).show();
				Log.d(TAG, "failed with exception:" + e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mRepeatAuthTask = null;
			showProgress(false);
			if (success) {
				Log.d(TAG, "post successed " + scheduleStartTime + " and "
						+ scheduleFinishTime);
				Toast.makeText(getApplicationContext(), "スケジュール登録成功",
						Toast.LENGTH_SHORT).show();
			} else {
				Log.d(TAG, "post failed " + scheduleStartTime + " and "
						+ scheduleFinishTime);
				Toast.makeText(getApplicationContext(), "スケジュール登録失敗",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
			mRepeatAuthTask = null;
			showProgress(false);
		}
	}

	public class EditScheduleTask extends AsyncTask<Void, Void, Boolean> {
		private String googleIdInTask = GoogleConstant.UNTIED_TO_GOOGLE;

		public void setGoogleId(String googleId) {
			this.googleIdInTask = googleId;
		}

		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				SharedPreferences pref = getSharedPreferences("user",
						Activity.MODE_PRIVATE);
				mEmail = pref.getString("email", "");
				String selectEmails = pref.getString("selectEmails", "");

				Log.d(TAG, "time:" + scheduleStartTime + " and "
						+ scheduleFinishTime + "email:" + mEmail);
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				ScheduleResultV1Dto result;

				if (selectEmails.length() > 0) {
					endpoint = RemoteApi.getScheduleEndpoint();
					StringListContainer container = new StringListContainer();
					container.setList(Arrays.asList(selectEmails.split(",")));
					CreateGroupSchedule groupSchedule = endpoint
							.scheduleV1EndPoint().createGroupSchedule(name,
									scheduleStartTime, scheduleFinishTime,
									mEmail, container);
					result = groupSchedule.execute();
				} else if (!GoogleConstant.UNTIED_TO_GOOGLE
						.equals(googleIdInTask)) {
					// googleにエクスポートした
					if (create) {
						CreateScheduleWithGId schedule;
						schedule = endpoint.scheduleV1EndPoint()
								.createScheduleWithGId(name, scheduleStartTime,
										scheduleFinishTime,
										this.googleIdInTask, mEmail);
						result = schedule.execute();
					} else {
						EditScheduleWithGId schedule = endpoint
								.scheduleV1EndPoint().editScheduleWithGId(keyS,
										name, scheduleStartTime,
										scheduleFinishTime, googleIdInTask);
						result = schedule.execute();
					}
				} else {
					if (create) {
						CreateSchedule schedule = endpoint.scheduleV1EndPoint()
								.createSchedule(name, scheduleStartTime,
										scheduleFinishTime, mEmail);
						result = schedule.execute();
					} else {
						// googleと紐づけずにedit
						EditScheduleWithGId schedule = endpoint
								.scheduleV1EndPoint().editScheduleWithGId(keyS,
										name, scheduleStartTime,
										scheduleFinishTime, googleIdInTask);
						result = schedule.execute();
					}
				}

				if ((result != null) && SUCCESS.equals(result.getResult())) {
					Log.d(TAG, "Successed");
					return true;
				} else {
					Log.d(TAG, "failed");
					return false;
				}
			} catch (Exception e) {
				// Toast.makeText(getApplicationContext(),"Failed with exception:"
				// + e,Toast.LENGTH_SHORT).show();
				Log.d(TAG, "failed with exception:" + e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			showProgress(false);
			if (success) {
				Log.d(TAG, "post successed " + scheduleStartTime + " and "
						+ scheduleFinishTime);
				Toast.makeText(getApplicationContext(), "スケジュール登録成功",
						Toast.LENGTH_SHORT).show();
			} else {
				Log.d(TAG, "post failed " + scheduleStartTime + " and "
						+ scheduleFinishTime);
				Toast.makeText(getApplicationContext(), "スケジュール登録失敗",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
			showProgress(false);
		}
	}

	// google e
	private RepeatScheduleV1Dto makeRepeatScheduleDto(String name,
			long startTime, long finishTime, List<Integer> repeats) {
		// startTimeとfinishTimeを00:00からのミリ秒を表すlongにする
		// そのためその日の00:00をCalendar型で作成
		Calendar startOfTheDay = CalendarUtils.getBeginOfDate(startTime);

		long repeatBegin = RepeatUtils.getTrueRepeatBegin(
				startOfTheDay.getTimeInMillis(), repeats);
		long repeatEnd = RepeatUtils.getDefaultRepeatEnd(repeatBegin);
		RepeatScheduleV1Dto repsche = new RepeatScheduleV1Dto();
		repsche.setName(name);
		repsche.setStartTime(startTime - startOfTheDay.getTimeInMillis());
		repsche.setFinishTime(finishTime - startOfTheDay.getTimeInMillis());
		repsche.setRepeatBegin(repeatBegin);
		repsche.setRepeatEnd(repeatEnd);
		repsche.setRepeatDays(repeats);
		return repsche;
	}

	@Override
	public void onGoogleAccountCheckFinish(boolean result,
			List<String> accountList, List<String> displayList, List<Long> idList) {
		if (result) {
			if (accountList != null && accountList.size() > 0) {
				// エクスポート先のカレンダーidを探す
				SharedPreferences pref = getSharedPreferences("user",
						Activity.MODE_PRIVATE);
				mEmail = pref.getString("email", "");

				String accountName = mUser.getGoogleAccount().split("_")[0];
				String displayName = mUser.getGoogleAccount().split("_")[1];
				
				Long calID = null;
				for (int i = 0; i < accountList.size(); i++) {
					if (accountList.get(i).equals(accountName) && 
							displayList.get(i).equals(displayName)) {
						// アカウントネームとディスプレイネームが一致するidを取得
						calID = idList.get(i);
						break;

					}
				}

				if (calID != null) {
					if (repeatChk.isChecked()) {
						// 繰り返しの場合
						RepeatScheduleV1Dto newSchedule = 
								makeRepeatScheduleDto(name, scheduleStartTime, scheduleFinishTime, repeats);
						GoogleCalendarExporter gce = 
								new GoogleCalendarExporter(this, this);
						if (create) {
							gce.insert(calID, newSchedule);
						} else {
							gce.edit(keyS, newSchedule);
						}
					} else {
						ScheduleV1Dto newSchedule = new ScheduleV1Dto();
						newSchedule.setStartTime(scheduleStartTime);
						newSchedule.setFinishTime(scheduleFinishTime);
						newSchedule.setName(name);
						GoogleCalendarExporter gce = 
								new GoogleCalendarExporter(this, this);
						if (create) {
							gce.insert(calID, newSchedule);
						} else {
							gce.edit(keyS, newSchedule);
						}
					}
				}
			} else {
				// アカウントが登録されていない場合、ダイアログを表示して登録を促す
				new GoogleAccountRegistDialogBuilder(ScheduleEditActivity.this)
						.setDefault().show();
			}
			Log.d("DEBUG", "checking google account success");
		} else {
			Log.d("DEBUG", "checking google account fail");
		}
	}

	@Override
	public void onGoogleCalendarExportFinish(String googleId) {
		Log.d("DEBUG", "edit: " + googleId);
		if ((googleId == null) || FAIL.equals(googleId)) {
			showProgress(false);
		} else {
			if (repeatChk.isChecked()) {
				mRepeatAuthTask = new EditRepeatScheduleTask();
				mRepeatAuthTask.setGoogleId(googleId);
				mRepeatAuthTask.execute();
			} else {
				mAuthTask = new EditScheduleTask();
				mAuthTask.setGoogleId(googleId);
				mAuthTask.execute();
			}
		}
	}

	public boolean DayCheck(int[] rd, int n) {
		for (int i = 0; i < rd.length; i++) {
			if (rd[i] == n) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onGetUserFinish(GetUserResultV1Dto result) {
		mGetUserTask = null;
		if ((result != null) && SUCCESS.equals(result.getResult())) {
			mUser = result.getUser();
			Log.d("DEBUG", "get user success");
			if(mUser!=null){
				Log.d("DEBUG","mUser："+mUser.toString());
			}else{
				Log.d("DEBUG","mUserはnull");
			}
			
			if (mUser.getGoogleAccount().equals(GoogleConstant.UNTIED_TO_GOOGLE)) {
				googleChecked = false;
			} else {
				googleChecked = true;
			}
			
			showProgress(true);
			if (googleChecked) {
				GoogleAccountChecker gac = 
						new GoogleAccountChecker(ScheduleEditActivity.this, ScheduleEditActivity.this);
				gac.run();
			} else { 
				if(repeat){
					mRepeatAuthTask = new EditRepeatScheduleTask();
					mRepeatAuthTask.execute((Void) null);
				}else{
					mAuthTask = new EditScheduleTask();
					mAuthTask.execute((Void) null);
				}
			}
			
		} else {
			mUser = null;
			Log.d("DEBUG", "get user fail");
		}
	}
}
