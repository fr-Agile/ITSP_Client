package jp.ac.titech.itpro.sds.fragile;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.google.api.services.scheduleEndpoint.ScheduleEndpoint;
import com.google.api.services.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateSchedule;
import com.google.api.services.scheduleEndpoint.model.ScheduleResultV1Dto;

import jp.ac.titech.itpro.sds.fragile.RegisterActivity.UserRegisterTask;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

public class ScheduleInputActivity extends Activity{
		
	private Button doneBtn;
	private long scheduleStartTime;
	private long scheduleFinishTime;
	private String mEmail = "aaa123@gmail.com";
	
	private static String SUCCESS = "success";
	private static String FAIL = "fail";
	
	private ScheduleInputTask mAuthTask = null;
	
	DateFormat formatDateTime = DateFormat.getDateTimeInstance();
	Calendar dateTime=Calendar.getInstance();
	private TextView startTimeLabel;
	private TextView finishTimeLabel;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputschedule);
        startTimeLabel=(TextView)findViewById(R.id.startTimeTxt);
        finishTimeLabel=(TextView)findViewById(R.id.finishTimeTxt);
        
        findViewById(R.id.startTimeBtn).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						chooseStartTime();
					}
				});
        
        findViewById(R.id.finishTimeBtn).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						chooseFinishTime();
					}
				});
        
        findViewById(R.id.dateBtn).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						chooseDate();
					}
				});
        doneBtn = (Button)findViewById(R.id.doneBtn);
        doneBtn.setEnabled(false);
        findViewById(R.id.doneBtn).setOnClickListener(
        		new View.OnClickListener() {
        			public void onClick(View view) {
        				clickDoneButton();
        			}
        		});
    }
    
    public void chooseDate(){
    	new DatePickerDialog(ScheduleInputActivity.this, d, dateTime.get(Calendar.YEAR),dateTime.get(Calendar.MONTH), dateTime.get(Calendar.DAY_OF_MONTH)).show();
    }
    
    public void chooseStartTime(){
    	new TimePickerDialog(ScheduleInputActivity.this, st, dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), true).show();
    }
    
    public void chooseFinishTime(){
    	new TimePickerDialog(ScheduleInputActivity.this, ft, dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), true).show();
    }
    
    public void clickDoneButton(){
    	mAuthTask = new ScheduleInputTask();
		mAuthTask.execute((Void) null);
    }
    
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
			dateTime.set(Calendar.YEAR,year);
			dateTime.set(Calendar.MONTH, monthOfYear);
			dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			setButtonEnable();
		}
	};
	TimePickerDialog.OnTimeSetListener st=new TimePickerDialog.OnTimeSetListener() {
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			dateTime.set(Calendar.MINUTE,minute);
			startTimeLabel.setText(formatDateTime.format(dateTime.getTime()));
			scheduleStartTime = dateTime.getTime().getTime();
			setButtonEnable();
		}
	};
	
	TimePickerDialog.OnTimeSetListener ft=new TimePickerDialog.OnTimeSetListener() {
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			dateTime.set(Calendar.MINUTE,minute);
			finishTimeLabel.setText(formatDateTime.format(dateTime.getTime()));
			scheduleFinishTime = dateTime.getTime().getTime();
			setButtonEnable();
		}
	};
	
	private void setButtonEnable() {
		if (scheduleStartTime == 0 || scheduleFinishTime == 0 || scheduleStartTime >= scheduleFinishTime){
			doneBtn.setEnabled(false);
		} else {
			doneBtn.setEnabled(true);
		}
	}
	
	public class ScheduleInputTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {

			try {
				Log.d("vietDebug","time:"+ scheduleStartTime + " and " + scheduleFinishTime + "email:" +mEmail);
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				CreateSchedule schedule = endpoint.scheduleV1EndPoint().createSchedule(
						scheduleStartTime, scheduleFinishTime, mEmail);
				ScheduleResultV1Dto result = schedule.execute();
				
				if (SUCCESS.equals(result.getResult())) {
					Log.d("vietDebug", "successed");
					return true;
				} else {
					Log.d("vietDebug", "failed");
					return false;
				}

			} catch (Exception e) {
				Log.d("vietDebug", "failed with exception:" + e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;

			if (success) {
				Log.d("vietDebug", "post successed");
			} else {
				Log.d("vietDebug", "post failed");
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}
	}
}


//Debug
//Log.d("vietDebug", "click done button");