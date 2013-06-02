package jp.ac.titech.itpro.sds.fragile;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import java.text.DateFormat;

import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

public class ScheduleInputActivity extends Activity{
	
//	private Button timeBtn;
//	private Button dateBtn;
	DateFormat formatDateTime = DateFormat.getDateTimeInstance();
	Calendar dateTime=Calendar.getInstance();
	private TextView timeLabel;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputschedule);
        timeLabel=(TextView)findViewById(R.id.timeTxt);
        updateLabel();
//        timeBtn = (Button)findViewById(R.id.timeBtn);
        findViewById(R.id.startTimeBtn).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						chooseTime();
					}
				});
//        dateBtn = (Button)findViewById(R.id.dateBtn);
        findViewById(R.id.dateBtn).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						chooseDate();
					}
				});
        
    }
    public void chooseDate(){
    	new DatePickerDialog(ScheduleInputActivity.this, d, dateTime.get(Calendar.YEAR),dateTime.get(Calendar.MONTH), dateTime.get(Calendar.DAY_OF_MONTH)).show();
    }
    public void chooseTime(){
    	new TimePickerDialog(ScheduleInputActivity.this, t, dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), true).show();
    }
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
			dateTime.set(Calendar.YEAR,year);
			dateTime.set(Calendar.MONTH, monthOfYear);
			dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateLabel();
		}
	};
	TimePickerDialog.OnTimeSetListener t=new TimePickerDialog.OnTimeSetListener() {
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			dateTime.set(Calendar.MINUTE,minute);
			updateLabel();
		}
	};
	private void updateLabel() {
		timeLabel.setText(formatDateTime.format(dateTime.getTime()));
	}
	
}
