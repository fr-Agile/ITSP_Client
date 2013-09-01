package jp.ac.titech.itpro.sds.fragile;

import java.text.DateFormat;
import java.util.Calendar;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
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
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

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
	private View mInputScheduleView;
	private View mSpinView;
	private static String SUCCESS = "success";
	private EditScheduleTask mAuthTask = null;
	
	DateFormat formatDateTime = DateFormat.getDateTimeInstance();
	Calendar startTime=Calendar.getInstance();
	Calendar finishTime=Calendar.getInstance();
	String keyS="";
	
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
		keyS=intentE.getStringExtra("key");
        
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
        doneBtn.setEnabled(false);
        doneBtn.setOnClickListener(
        		new View.OnClickListener() {
        			public void onClick(View view) {
        				clickDoneButton();
        			}
        		});
        
        showScheduleViewBtn = (Button)findViewById(R.id.showScheduleViewBtn);
        showScheduleViewBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(ScheduleEditActivity.this, ScheduleActivity.class));
			}
		});
    }
        
    public void clickDoneButton(){
    	showProgress(true);
    	mAuthTask = new EditScheduleTask();
		mAuthTask.execute();
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
			mAuthTask = null;
			showProgress(false);
			if (success) {
				Log.d(TAG, "post successed " + scheduleStartTime + " and " + scheduleFinishTime);
//				Toast.makeText(getApplicationContext(),"Successed " + scheduleStartTime + " and " + scheduleFinishTime, Toast.LENGTH_SHORT).show();
				Toast.makeText(getApplicationContext(),"スケジュール登録成功", Toast.LENGTH_SHORT).show();
			} else {
				Log.d(TAG, "post failed " + scheduleStartTime + " and " + scheduleFinishTime);
				Toast.makeText(getApplicationContext(),"スケジュール登録失敗", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}