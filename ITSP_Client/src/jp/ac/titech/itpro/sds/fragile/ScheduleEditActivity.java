package jp.ac.titech.itpro.sds.fragile;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.CreateRepeatSchedule;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.EditRepeatSchedule;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleContainer;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleResultV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateSchedule;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.EditSchedule;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleResultV1Dto;

public class ScheduleEditActivity extends Activity{
	private static final String TAG = "ScheduleEditActivity";
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
				startActivity(intent);
			}
		});
        repeatdaysView = findViewById(R.id.repeatdaysView);

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
		if(repeat){
			mRepeatAuthTask = new EditRepeatScheduleTask();
			mRepeatAuthTask.execute((Void) null);
		}else{
			mAuthTask = new EditScheduleTask();
			mAuthTask.execute((Void) null);
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
		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
				mEmail = pref.getString("email","");	
				Log.d(TAG,"time:"+ scheduleStartTime + " and " + scheduleFinishTime + "email:" +mEmail);
				// repeat schedule
				RepeatScheduleContainer contain = new RepeatScheduleContainer();
				contain.setIntegers(repeats);

				// startTimeとfinishTimeを今日の00:00からのミリ秒を表すlongにする
				// そのため今日の00:00をCalendar型で作成
				Calendar startOfToday = Calendar.getInstance();
				startOfToday.set(Calendar.HOUR_OF_DAY, 0);
				startOfToday.set(Calendar.MINUTE, 0);
				startOfToday.set(Calendar.SECOND, 0);
				startOfToday.set(Calendar.MILLISECOND, 0);

				RepeatScheduleEndpoint endpoint = RemoteApi
						.getRepeatScheduleEndpoint();
				EditRepeatSchedule repeatschedule = endpoint
						.repeatScheduleV1EndPoint().editRepeatSchedule(
								keyS,
								scheduleStartTime
										- startOfToday.getTimeInMillis(),
								scheduleFinishTime
										- startOfToday.getTimeInMillis(),
								contain);
				repeatschedule.execute();	
				return true;
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
		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
				mEmail = pref.getString("email","");	
				Log.d(TAG,"time:"+ scheduleStartTime + " and " + scheduleFinishTime + "email:" +mEmail);
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				EditSchedule schedule = endpoint.scheduleV1EndPoint().editSchedule(
						keyS,scheduleStartTime, scheduleFinishTime);
				schedule.execute();	
				return true;
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
}
