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

import com.google.api.services.scheduleEndpoint.ScheduleEndpoint;
import com.google.api.services.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateSchedule;
import com.google.api.services.scheduleEndpoint.model.ScheduleResultV1Dto;

public class ScheduleInputActivity extends Activity{
		
	private static final String TAG = "ScheduleInputActivity";
	private Button doneBtn, showScheduleViewBtn;
	private long scheduleStartTime;
	private long scheduleFinishTime;
	private String mEmail;
	
	private DatePicker mDatepicker;
	private TimePicker mStartTimePicker;
	private TimePicker mFinishTimePicker;

	private View mInputScheduleView;
	private View mSpinView;
//	private TextView mLoginStatusMessageView;
	
	private static String SUCCESS = "success";
//	private static String FAIL = "fail";
	
	private ScheduleInputTask mAuthTask = null;
	
	DateFormat formatDateTime = DateFormat.getDateTimeInstance();
//	Calendar dateTime=Calendar.getInstance();
	Calendar startTime=Calendar.getInstance();
	Calendar finishTime=Calendar.getInstance();
	
//	private TextView startTimeLabel;
//	private TextView finishTimeLabel;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputschedule);
        
        mInputScheduleView = findViewById(R.id.inputScheduleView);
		mSpinView = findViewById(R.id.spinView);
        
//        startTimeLabel=(TextView)findViewById(R.id.startTimeTxt);
//        finishTimeLabel=(TextView)findViewById(R.id.finishTimeTxt);
       
        scheduleStartTime = startTime.getTime().getTime();
//        startTimeLabel.setText(formatDateTime.format(dateTime.getTime()));
		scheduleFinishTime = finishTime.getTime().getTime();
        
        mDatepicker = (DatePicker)findViewById(R.id.datePicker1);
        mStartTimePicker = (TimePicker)findViewById(R.id.timePicker1);
        mFinishTimePicker = (TimePicker)findViewById(R.id.timePicker2);
//        finishTimeLabel.setText(formatDateTime.format(dateTime.getTime()));
                
        mDatepicker.init(startTime.get(Calendar.YEAR), 
        				 startTime.get(Calendar.MONTH), 
        				 startTime.get(Calendar.DAY_OF_MONTH), 
        				 new OnDateChangedListener(){
        	public void onDateChanged(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
        		startTime.set(year, monthOfYear, dayOfMonth);
        		finishTime.set(year, monthOfYear, dayOfMonth);
    			scheduleStartTime = startTime.getTime().getTime();
    			scheduleFinishTime = finishTime.getTime().getTime();
//    			Toast.makeText(getApplicationContext(), "onDateChanged"+ scheduleStartTime + " and " + scheduleFinishTime, Toast.LENGTH_SHORT).show();
        		setButtonEnable();
        		Log.d(TAG,"time:"+ formatDateTime.format(startTime.getTime()) + " and " + formatDateTime.format(finishTime.getTime()));
        	}});
        
        mStartTimePicker.setCurrentHour(startTime.get(Calendar.HOUR_OF_DAY));
        mStartTimePicker.setCurrentMinute(startTime.get(Calendar.MINUTE));
        mStartTimePicker.setOnTimeChangedListener(new OnTimeChangedListener(){
        	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        		
        		startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        		startTime.set(Calendar.MINUTE,minute);
    			scheduleStartTime = startTime.getTime().getTime();
//    			Toast.makeText(getApplicationContext(), "onStartTimeChanged" + scheduleStartTime + " and " + scheduleFinishTime, Toast.LENGTH_SHORT).show();
    			setButtonEnable();
    			Log.d(TAG,"time:"+ formatDateTime.format(startTime.getTime()) + " and " + formatDateTime.format(finishTime.getTime()));
        	}});
        
        mFinishTimePicker.setCurrentHour(finishTime.get(Calendar.HOUR_OF_DAY));
        mFinishTimePicker.setCurrentMinute(finishTime.get(Calendar.MINUTE));
        mFinishTimePicker.setOnTimeChangedListener(new OnTimeChangedListener(){
        	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        		
        		finishTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        		finishTime.set(Calendar.MINUTE,minute);
    			scheduleFinishTime = finishTime.getTime().getTime();
//    			Toast.makeText(getApplicationContext(), "onFinishTimeChanged" + scheduleStartTime + " and " + scheduleFinishTime, Toast.LENGTH_SHORT).show();
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
				startActivity(new Intent(ScheduleInputActivity.this, ScheduleActivity.class));
			}
		});
    }
    

    
    public void clickDoneButton(){
    	showProgress(true);
    	mAuthTask = new ScheduleInputTask();
		mAuthTask.execute((Void) null);
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
	
	public class ScheduleInputTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
				mEmail = pref.getString("email","");	
				Log.d(TAG,"time:"+ scheduleStartTime + " and " + scheduleFinishTime + "email:" +mEmail);
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				CreateSchedule schedule = endpoint.scheduleV1EndPoint().createSchedule(
						scheduleStartTime, scheduleFinishTime, mEmail);
				ScheduleResultV1Dto result = schedule.execute();

				
				if (SUCCESS.equals(result.getResult())) {
//					Toast.makeText(getApplicationContext(),"Successed",Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Successed");
					return true;
				} else {
//					Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Failed");
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


//Debug
//Log.d("vietDebug", "click done button");

///**
// * Shows the progress UI and hides the login form.
// */
//@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//private void showProgress(final boolean show) {
//	// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//	// for very easy animations. If available, use these APIs to fade-in
//	// the progress spinner.
//	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//		int shortAnimTime = getResources().getInteger(
//				android.R.integer.config_shortAnimTime);
//
//		mLoginStatusView.setVisibility(View.VISIBLE);
//		mLoginStatusView.animate().setDuration(shortAnimTime)
//				.alpha(show ? 1 : 0)
//				.setListener(new AnimatorListenerAdapter() {
//					@Override
//					public void onAnimationEnd(Animator animation) {
//						mLoginStatusView.setVisibility(show ? View.VISIBLE
//								: View.GONE);
//					}
//				});
//
//		mLoginFormView.setVisibility(View.VISIBLE);
//		mLoginFormView.animate().setDuration(shortAnimTime)
//				.alpha(show ? 0 : 1)
//				.setListener(new AnimatorListenerAdapter() {
//					@Override
//					public void onAnimationEnd(Animator animation) {
//						mLoginFormView.setVisibility(show ? View.GONE
//								: View.VISIBLE);
//					}
//				});
//	} else {
//		// The ViewPropertyAnimator APIs are not available, so simply show
//		// and hide the relevant UI components.
//		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
//		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//	}
//}


//findViewById(R.id.startTimeBtn).setOnClickListener(
//		new View.OnClickListener() {
//			public void onClick(View view) {
//				chooseStartTime();
//			}
//		});
//
//findViewById(R.id.finishTimeBtn).setOnClickListener(
//		new View.OnClickListener() {
//			public void onClick(View view) {
//				chooseFinishTime();
//			}
//		});
//
//findViewById(R.id.dateBtn).setOnClickListener(
//		new View.OnClickListener() {
//			public void onClick(View view) {
//				chooseDate();
//			}
//		});

//public void chooseDate(){
//new DatePickerDialog(ScheduleInputActivity.this, d, dateTime.get(Calendar.YEAR),dateTime.get(Calendar.MONTH), dateTime.get(Calendar.DAY_OF_MONTH)).show();
//}
//
//public void chooseStartTime(){
//new TimePickerDialog(ScheduleInputActivity.this, st, dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), true).show();
//}
//
//public void chooseFinishTime(){
//new TimePickerDialog(ScheduleInputActivity.this, ft, dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), true).show();
//}

//DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
//	public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
//		dateTime.set(Calendar.YEAR,year);
//		dateTime.set(Calendar.MONTH, monthOfYear);
//		dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//		setButtonEnable();
//	}
//};
//TimePickerDialog.OnTimeSetListener st=new TimePickerDialog.OnTimeSetListener() {
//	
//	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//		// TODO Auto-generated method stub
//		dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
//		dateTime.set(Calendar.MINUTE,minute);
//		startTimeLabel.setText(formatDateTime.format(dateTime.getTime()));
//		scheduleStartTime = dateTime.getTime().getTime();
//		setButtonEnable();
//	}
//};
//
//TimePickerDialog.OnTimeSetListener ft=new TimePickerDialog.OnTimeSetListener() {
//	
//	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//		// TODO Auto-generated method stub
//		dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
//		dateTime.set(Calendar.MINUTE,minute);
//		finishTimeLabel.setText(formatDateTime.format(dateTime.getTime()));
//		scheduleFinishTime = dateTime.getTime().getTime();
//		setButtonEnable();
//	}
//};