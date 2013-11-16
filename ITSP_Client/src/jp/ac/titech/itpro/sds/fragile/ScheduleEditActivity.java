package jp.ac.titech.itpro.sds.fragile;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.EditRepeatSchedule;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.EditRepeatScheduleWithGId;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleContainer;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleResultV1Dto;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.EditSchedule;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.EditScheduleWithGId;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleResultV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleV1Dto;

public class ScheduleEditActivity extends Activity implements GoogleAccountCheckFinishListener, GoogleCalendarExportFinishListener{
	private static final String TAG = "ScheduleEditActivity";
	private static final String FAIL = CommonConstant.FAIL;
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
	private View repeatdaysView;

	private List<Integer> repeats;
	
	private View mInputScheduleView;
	private View mSpinView;
	private static String SUCCESS = CommonConstant.SUCCESS;
	
	private CheckBox googleChk;
	
	private EditRepeatScheduleTask mRepeatAuthTask = null;
	private EditScheduleTask mAuthTask = null;

	private DateFormat formatDateTime = DateFormat.getDateTimeInstance();
	private Calendar startTime=Calendar.getInstance();
	private Calendar finishTime=Calendar.getInstance();
	private String keyS="";
	private boolean repeat;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_inputschedule);
        
        mInputScheduleView = findViewById(R.id.inputScheduleView);
		mSpinView = findViewById(R.id.spinView);
               
        scheduleStartTime = startTime.getTime().getTime();
		scheduleFinishTime = finishTime.getTime().getTime();
        
        mDatepicker = (DatePicker)findViewById(R.id.datePicker1);
        mStartTimePicker = (TimePicker)findViewById(R.id.timePicker1);
        mFinishTimePicker = (TimePicker)findViewById(R.id.timePicker2);
        
        Calendar startE = Calendar.getInstance();
		Calendar finishE = Calendar.getInstance();
		Intent intentE = getIntent();
		
		startE.setTimeInMillis(intentE.getLongExtra("start", 0));
		finishE.setTimeInMillis(intentE.getLongExtra("finish", 0));
		keyS = intentE.getStringExtra("key");
		repeat = intentE.getBooleanExtra("repeat", false);
		
        mDatepicker.init(startE.get(Calendar.YEAR),startE.get(Calendar.MONTH),startE.get(Calendar.DAY_OF_MONTH), 		 
        		new OnDateChangedListener(){
        	public void onDateChanged(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
        		startTime.set(year, monthOfYear, dayOfMonth);
        		finishTime.set(year, monthOfYear, dayOfMonth);
    			scheduleStartTime = startTime.getTime().getTime();
    			scheduleFinishTime = finishTime.getTime().getTime();
        		setButtonEnable();
        		Log.d(TAG,"time:"+ formatDateTime.format(startTime.getTime()) + " and " + formatDateTime.format(finishTime.getTime()));
        	}});
        
        mStartTimePicker.setCurrentHour(startE.get(Calendar.HOUR_OF_DAY));
        mStartTimePicker.setCurrentMinute(startE.get(Calendar.MINUTE));
        mStartTimePicker.setOnTimeChangedListener(new OnTimeChangedListener(){
        	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        		startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        		startTime.set(Calendar.MINUTE,minute);
    			scheduleStartTime = startTime.getTime().getTime();
    			setButtonEnable();
    			Log.d(TAG,"time:"+ formatDateTime.format(startTime.getTime()) + " and " + formatDateTime.format(finishTime.getTime()));
        	}});
        
        mFinishTimePicker.setCurrentHour(finishE.get(Calendar.HOUR_OF_DAY));
        mFinishTimePicker.setCurrentMinute(finishE.get(Calendar.MINUTE));
        mFinishTimePicker.setOnTimeChangedListener(new OnTimeChangedListener(){
        	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        		finishTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        		finishTime.set(Calendar.MINUTE,minute);
    			scheduleFinishTime = finishTime.getTime().getTime();
    			setButtonEnable();
    			Log.d(TAG,"time:"+ formatDateTime.format(startTime.getTime()) + " and " + formatDateTime.format(finishTime.getTime()));
        	}});
        
        doneBtn = (Button)findViewById(R.id.doneBtn);
        doneBtn.setEnabled(true);
        doneBtn.setOnClickListener(
        		new View.OnClickListener() {
        			public void onClick(View view) {
        				clickDoneButton();
        			}
        		});
        
        showScheduleViewBtn = (Button)findViewById(R.id.showScheduleViewBtn);
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
				startActivity(intent);
			}
		});
        repeatdaysView = findViewById(R.id.repeatdaysView);
        
		googleChk = (CheckBox) findViewById(R.id.googleCheckbox);
		googleChk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {}
		});
        
		

		repeatChk = (CheckBox) findViewById(R.id.repeartCheckbox);
		if(repeat){
			repeatdaysView.setVisibility(View.VISIBLE);
			repeatChk.setChecked(true);
		}
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

		showProgress(true);
		if (googleChk.isChecked()) {
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
	}
	private void setButtonEnable() {
		if (scheduleStartTime == 0 || scheduleFinishTime == 0 || scheduleStartTime >= scheduleFinishTime){
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
			mSpinView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
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
				SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
				mEmail = pref.getString("email","");	
				Log.d(TAG,"time:"+ scheduleStartTime + " and " + scheduleFinishTime + "email:" +mEmail);
				// repeat schedule
				RepeatScheduleContainer contain = new RepeatScheduleContainer();
				contain.setIntegers(repeats);
				RepeatScheduleV1Dto repscheDto = makeRepeatScheduleDto(
						scheduleStartTime, scheduleFinishTime, repeats);


				RepeatScheduleEndpoint endpoint = RemoteApi
						.getRepeatScheduleEndpoint();
				RepeatScheduleResultV1Dto result;
				if (!GoogleConstant.UNTIED_TO_GOOGLE.equals(this.googleIdInTask)) {
					// googleにエクスポートした
					EditRepeatScheduleWithGId repschedule = endpoint
							.repeatScheduleV1EndPoint()
							.editRepeatScheduleWithGId(
									keyS,
									repscheDto.getStartTime(), repscheDto.getFinishTime(),
									this.googleIdInTask, contain);
					result = repschedule.execute();
				} else {
					// googleと紐づけずにデータベースをedit
					EditRepeatScheduleWithGId repschedule = endpoint
							.repeatScheduleV1EndPoint()
							.editRepeatScheduleWithGId(
									keyS,
									repscheDto.getStartTime(), repscheDto.getFinishTime(),
									this.googleIdInTask, contain);
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
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),"Failed with exception:" + e,Toast.LENGTH_SHORT).show();
				Log.d(TAG, "failed with exception:" + e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mRepeatAuthTask = null;
			showProgress(false);
			if (success) {
				Log.d(TAG, "post successed " + scheduleStartTime + " and " + scheduleFinishTime);
				Toast.makeText(getApplicationContext(),"スケジュール登録成功", Toast.LENGTH_SHORT).show();
			} else {
				Log.d(TAG, "post failed " + scheduleStartTime + " and " + scheduleFinishTime);
				Toast.makeText(getApplicationContext(),"スケジュール登録失敗", Toast.LENGTH_SHORT).show();
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
				SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
				mEmail = pref.getString("email","");	
				Log.d(TAG,"time:"+ scheduleStartTime + " and " + scheduleFinishTime + "email:" +mEmail);
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				ScheduleResultV1Dto result;
				
				if (!GoogleConstant.UNTIED_TO_GOOGLE.equals(googleIdInTask)) {
					// googleにエクスポートした
					EditScheduleWithGId schedule = endpoint.scheduleV1EndPoint()
							.editScheduleWithGId(keyS,scheduleStartTime, scheduleFinishTime,
									googleIdInTask);
					result = schedule.execute();
				} else { 
					// googleと紐づけずにedit
					EditScheduleWithGId schedule = endpoint.scheduleV1EndPoint()
							.editScheduleWithGId(keyS,scheduleStartTime, scheduleFinishTime,
									googleIdInTask);
					result = schedule.execute();
				}
				if ((result != null) && SUCCESS.equals(result.getResult())) {
					Log.d(TAG, "Successed");
					return true;
				} else {
					Log.d(TAG, "failed");
					return false;
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),"Failed with exception:" + e,Toast.LENGTH_SHORT).show();
				Log.d(TAG, "failed with exception:" + e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			showProgress(false);
			if (success) {
				Log.d(TAG, "post successed " + scheduleStartTime + " and " + scheduleFinishTime);
				Toast.makeText(getApplicationContext(),"スケジュール登録成功", Toast.LENGTH_SHORT).show();
			} else {
				Log.d(TAG, "post failed " + scheduleStartTime + " and " + scheduleFinishTime);
				Toast.makeText(getApplicationContext(),"スケジュール登録失敗", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
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
					// 繰り返しの場合
					RepeatScheduleV1Dto newSchedule = 
							makeRepeatScheduleDto(scheduleStartTime, scheduleFinishTime, repeats);
					GoogleCalendarExporter gce = 
							new GoogleCalendarExporter(this, this);
					gce.edit(keyS, newSchedule);
					
				} else {
					ScheduleV1Dto newSchedule = new ScheduleV1Dto();
					newSchedule.setStartTime(scheduleStartTime);
					newSchedule.setFinishTime(scheduleFinishTime);
					GoogleCalendarExporter gce = 
							new GoogleCalendarExporter(this, this);
					gce.edit(keyS, newSchedule);
				}
			} else {
				// アカウントが登録されていない場合、ダイアログを表示して登録を促す
				new GoogleAccountRegistDialogBuilder(ScheduleEditActivity.this)
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
}
