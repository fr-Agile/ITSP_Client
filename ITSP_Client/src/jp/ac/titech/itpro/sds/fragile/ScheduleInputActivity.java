package jp.ac.titech.itpro.sds.fragile;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.GetGroupTask.GetGroupFinishListener;
import jp.ac.titech.itpro.sds.fragile.GoogleCalendarExporter.GoogleCalendarExportFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.appspot.fragile_t.groupEndpoint.model.GroupV1Dto;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.CreateRepeatScheduleWithGId;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.CreateRepeatScheduleWithTerm;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleContainer;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleResultV1Dto;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateGroupSchedule;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateSchedule;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateScheduleWithGId;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleResultV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleV1Dto;

public class ScheduleInputActivity extends Activity 
	implements GetGroupFinishListener,
		GoogleAccountCheckFinishListener, GoogleCalendarExportFinishListener {

	private static final String FAIL = CommonConstant.FAIL;
	private static final String TAG = "ScheduleInputActivity";
	private Button doneBtn, showScheduleViewBtn;
	private long scheduleStartTime;
	private long scheduleFinishTime;
	private String mEmail;

	private DatePicker mDatepicker;
	private TimePicker mStartTimePicker;
	private TimePicker mFinishTimePicker;

	private CheckBox repeatChk;
	private CheckBox everydayChk;
	private CheckBox monChk;
	private CheckBox tueChk;
	private CheckBox wedChk;
	private CheckBox thuChk;
	private CheckBox friChk;
	private CheckBox satChk;
	private CheckBox sunChk;
	private CheckBox groupChk;
	private View repeatdaysView;
	private RadioGroup layout;

	private List<Integer> repeats;
	private List<GroupV1Dto> groupList;
	
	private CheckBox googleChk;
	private boolean googleChecked;

	private View mInputScheduleView;
	private View mSpinView;
	// private TextView mLoginStatusMessageView;

	private static String SUCCESS = CommonConstant.SUCCESS;
	// private static String FAIL = "fail";

	private ScheduleInputTask mAuthTask = null;

	DateFormat formatDateTime = DateFormat.getDateTimeInstance();
	Calendar startTime = Calendar.getInstance();
	Calendar finishTime = Calendar.getInstance();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_inputschedule);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		mInputScheduleView = findViewById(R.id.inputScheduleView);
		mSpinView = findViewById(R.id.spinView);

		// 時間指定でスケジュール作成画面に飛んできた場合の、
		// startTime, finishTimeの調整
		if (extras != null) {
			if (extras.containsKey("startTime")) {
				Calendar cal = (Calendar) extras.get("startTime");
				Log.d("myDEBUG", extras.get("startTime").toString());
				startTime = (Calendar) cal.clone();

				if(extras.containsKey("length")){
					cal.add(Calendar.HOUR_OF_DAY, (int) extras.getInt("length"));
				} else {
					cal.add(Calendar.HOUR_OF_DAY, 1);
				}

				finishTime = (Calendar) cal.clone();
			}
		}

		scheduleStartTime = startTime.getTime().getTime();
		scheduleFinishTime = finishTime.getTime().getTime();

		mDatepicker = (DatePicker) findViewById(R.id.datePicker1);
		mStartTimePicker = (TimePicker) findViewById(R.id.timePicker1);
		mFinishTimePicker = (TimePicker) findViewById(R.id.timePicker2);

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
						// Toast.makeText(getApplicationContext(),
						// "onDateChanged"+ scheduleStartTime + " and " +
						// scheduleFinishTime, Toast.LENGTH_SHORT).show();
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
				// Toast.makeText(getApplicationContext(), "onStartTimeChanged"
				// + scheduleStartTime + " and " + scheduleFinishTime,
				// Toast.LENGTH_SHORT).show();
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
				// Toast.makeText(getApplicationContext(), "onFinishTimeChanged"
				// + scheduleStartTime + " and " + scheduleFinishTime,
				// Toast.LENGTH_SHORT).show();
				setButtonEnable();
				Log.d(TAG, "time:" + formatDateTime.format(startTime.getTime())
						+ " and " + formatDateTime.format(finishTime.getTime()));
			}
		});

		doneBtn = (Button) findViewById(R.id.doneBtn);
		if (scheduleStartTime >= scheduleFinishTime) {
			doneBtn.setEnabled(false);
		}
		doneBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				clickDoneButton();
			}
		});

		showScheduleViewBtn = (Button) findViewById(R.id.showScheduleViewBtn);
		showScheduleViewBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				// startActivity(new Intent(ScheduleInputActivity.this,
				// ScheduleActivity.class));

				Intent intent = new Intent(ScheduleInputActivity.this,
						ScheduleActivity.class);
				Calendar nowCal = Calendar.getInstance();
				// nowCal.add(Calendar.DAY_OF_YEAR, 7);
				StoreData data = new StoreData(nowCal);
				intent.putExtra("StoreData", data);
				intent.setAction(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);

			}
		});
		
		
		googleChk = (CheckBox) findViewById(R.id.googleCheckbox);
		googleChk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {}
		});

		repeatdaysView = findViewById(R.id.repeatdaysView);

		repeatChk = (CheckBox) findViewById(R.id.repeartCheckbox);
		repeatChk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					repeatdaysView.setVisibility(View.VISIBLE);
					// Toast.makeText(getApplicationContext(),"Repeat Checked:)",
					// Toast.LENGTH_LONG).show();
				} else {
					repeatdaysView.setVisibility(View.GONE);
				}
			}
		});

		sunChk = (CheckBox) findViewById(R.id.sundayCheckbox);
		monChk = (CheckBox) findViewById(R.id.mondayCheckbox);
		tueChk = (CheckBox) findViewById(R.id.tuesdayCheckbox);
		wedChk = (CheckBox) findViewById(R.id.wednesdayCheckbox);
		thuChk = (CheckBox) findViewById(R.id.thursdayCheckbox);
		friChk = (CheckBox) findViewById(R.id.fridayCheckbox);
		satChk = (CheckBox) findViewById(R.id.saturdayCheckbox);

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

		groupChk = (CheckBox) findViewById(R.id.groupScheduleCheckbox);
		groupChk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					showGroups();
				} else {
					hideGroups();
				}
			}
		});
		
		// 最初グループチェックボックスは見えないようにする
		groupChk.setVisibility(View.INVISIBLE);

		groupList = new ArrayList<GroupV1Dto>();
		layout = (RadioGroup) findViewById(R.id.radioButtonGroup);
		getGroupList();
		

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
		
		if (googleChk.isChecked()) 
			googleChecked = true;

		showProgress(true);
		
		
		if (googleChecked) {
			GoogleAccountChecker gac = 
					new GoogleAccountChecker(ScheduleInputActivity.this, ScheduleInputActivity.this);
			gac.run();
		} else {
			mAuthTask = new ScheduleInputTask();
			mAuthTask.execute((Void) null);
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

	private void showGroups() {
		Log.d("Viet DEBUG", "show groups");
		layout.setVisibility(View.VISIBLE);
	}

	private void hideGroups() {
		Log.d("Viet DEBUG", "hide groups");
		layout.setVisibility(View.GONE);
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

	public class ScheduleInputTask extends AsyncTask<Void, Void, Boolean> {
		private String googleIdInTask = null;
		
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

				if (!repeatChk.isChecked()) {
					if (!groupChk.isChecked()) {
						// schedule
						ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
						ScheduleResultV1Dto result;
						if (this.googleIdInTask != null) {
							// googleにエクスポートした
							CreateScheduleWithGId schedule;
							schedule = endpoint.scheduleV1EndPoint()
									.createScheduleWithGId(scheduleStartTime,
											scheduleFinishTime, this.googleIdInTask, mEmail);
							result = schedule.execute();
						} else {
							CreateSchedule schedule = endpoint.scheduleV1EndPoint()
									.createSchedule(scheduleStartTime,
											scheduleFinishTime, mEmail);
							result = schedule.execute();
						}

						if ((result != null) && SUCCESS.equals(result.getResult())) {
							// Toast.makeText(getApplicationContext(),"Successed",Toast.LENGTH_SHORT).show();
							Log.d(TAG, "Successed");
							return true;
						} else {
							// Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
							Log.d(TAG, "Failed");
							return false;
						}
					} else {
						// group schedule
						int radioButtonID = layout.getCheckedRadioButtonId();
						View radioButton = layout.findViewById(radioButtonID);
						int idx = layout.indexOfChild(radioButton);
						GroupV1Dto group = groupList.get(idx);
						ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
						CreateGroupSchedule groupSchedule = endpoint.scheduleV1EndPoint().createGroupSchedule(scheduleStartTime, scheduleFinishTime, mEmail, group.getKey());
						ScheduleResultV1Dto result = groupSchedule.execute();
						
						if ((result != null) && SUCCESS.equals(result.getResult())) {
							// Toast.makeText(getApplicationContext(),"Successed",Toast.LENGTH_SHORT).show();
							Log.d(TAG, "group schedule Successed");
							return true;
						} else {
							// Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
							Log.d(TAG, "group schedule Failed");
							return false;
						}
					}
					
				} else {
					// repeat schedule
					RepeatScheduleContainer contain = new RepeatScheduleContainer();
					contain.setIntegers(repeats);
					RepeatScheduleV1Dto repscheDto = makeRepeatScheduleDto(
							scheduleStartTime, scheduleFinishTime, repeats);
					
					RepeatScheduleEndpoint endpoint = RemoteApi
							.getRepeatScheduleEndpoint();
					RepeatScheduleResultV1Dto result;
					if (this.googleIdInTask != null) {
						// googleにエクスポートした
						CreateRepeatScheduleWithGId repschedule = endpoint
								.repeatScheduleV1EndPoint()
								.createRepeatScheduleWithGId(
										repscheDto.getStartTime(), repscheDto.getFinishTime(),
										repscheDto.getRepeatBegin(), repscheDto.getRepeatEnd(),
										this.googleIdInTask, mEmail, contain);
						result = repschedule.execute();
					} else {
						CreateRepeatScheduleWithTerm repschedule = endpoint
								.repeatScheduleV1EndPoint().createRepeatScheduleWithTerm(
										repscheDto.getStartTime(), repscheDto.getFinishTime(),
										repscheDto.getRepeatBegin(), repscheDto.getRepeatEnd(),
										mEmail, contain);
						result = repschedule.execute();
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
			mAuthTask = null;
			showProgress(false);
			if (success) {
				Log.d(TAG, "post successed " + scheduleStartTime + " and "
						+ scheduleFinishTime);
				// Toast.makeText(getApplicationContext(),"Successed " +
				// scheduleStartTime + " and " + scheduleFinishTime,
				// Toast.LENGTH_SHORT).show();
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
			mAuthTask = null;
			showProgress(false);
		}
	}
	
	// google e
	private RepeatScheduleV1Dto makeRepeatScheduleDto(long startTime, long finishTime,
			List<Integer> repeats) {
		// startTimeとfinishTimeを00:00からのミリ秒を表すlongにする
		// そのためその日の00:00をCalendar型で作成
		Calendar startOfTheDay = CalendarUtils.getBeginOfDate(startTime);
		
		long repeatBegin = RepeatUtils.getTrueRepeatBegin(startOfTheDay.getTimeInMillis(), repeats);
		long repeatEnd = RepeatUtils.getDefaultRepeatEnd(repeatBegin);
		
		RepeatScheduleV1Dto repsche = new RepeatScheduleV1Dto();
		repsche.setStartTime(startTime - startOfTheDay.getTimeInMillis());
		repsche.setFinishTime(finishTime - startOfTheDay.getTimeInMillis());
		repsche.setRepeatBegin(repeatBegin);
		repsche.setRepeatEnd(repeatEnd);
		repsche.setRepeatDays(repeats);
		return repsche;
	}

	private void getGroupList() {
		try {
			Log.d("Viet DEBUG", "get friend start");
			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String userEmail = pref.getString("email", "");

			// groupのemailリストを取得する
			GetGroupTask task = new GetGroupTask(this);
			task.execute(userEmail);

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "get friend fail");
		}
	}

	@Override
	public void onFinish(List<GroupV1Dto> result) {
		try {
			if (result != null) {
				groupList = result;
				
				// グループが一つ以上取得できたら表示する
				if(!groupList.isEmpty()){
					groupChk.setVisibility(View.VISIBLE);
				}
				
				final List<String> groupNameList = new ArrayList<String>();
				for (GroupV1Dto group : result) {
					groupNameList.add(group.getName());
				}
				String[] nameStrList = groupNameList
						.toArray(new String[groupNameList.size()]);

				for (String name : nameStrList) {
					RadioButton radioBtn = new RadioButton(this);
					radioBtn.setText(name);
					layout.addView(radioBtn);
					Log.d("DEBUG", "added group name :" + name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGoogleAccountCheckFinish(boolean result,
			List<String> accountList) {
		if (result) {
			if (accountList != null && accountList.size() > 0) {
				// アカウントが登録されているのでスケジュールをエクスポートする
				
				// スケジュールを生成
				SharedPreferences pref = getSharedPreferences("user",
						Activity.MODE_PRIVATE);
				mEmail = pref.getString("email", "");
				
				if (repeatChk.isChecked()) {
					RepeatScheduleV1Dto schedule = makeRepeatScheduleDto(
							scheduleStartTime, scheduleFinishTime, repeats);
					GoogleCalendarExporter gce = 
							new GoogleCalendarExporter(this, this);
					gce.insert(schedule);
				} else {
					ScheduleV1Dto schedule = new ScheduleV1Dto();
					schedule.setStartTime(this.scheduleStartTime);
					schedule.setFinishTime(this.scheduleFinishTime);
					GoogleCalendarExporter gce = 
							new GoogleCalendarExporter(this, this);
					gce.insert(schedule);
				}
			} else {
				// アカウントが登録されていない場合、ダイアログを表示して登録を促す
				new GoogleAccountRegistDialogBuilder(ScheduleInputActivity.this)
					.setDefault()
					.show();
			}
			Log.d("DEBUG", "checking google account success");
		} else {
			Log.d("DEBUG", "checking google account fail");
		}
		
	}

	@Override
	public void onGoogleCalendarExportFinish(String googleId) {
		if (FAIL.equals(googleId)) {
			showProgress(false);
		} else {
			mAuthTask = new ScheduleInputTask();
			mAuthTask.setGoogleId(googleId);
			mAuthTask.execute();
		}
	}
}
