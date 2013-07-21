package jp.ac.titech.itpro.sds.fragile;

import java.util.Calendar;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarAdapter;
import jp.ac.titech.itpro.sds.fragile.utils.DayAdapter;
import jp.ac.titech.itpro.sds.fragile.utils.TimeAdapter;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import com.google.api.services.scheduleEndpoint.ScheduleEndpoint;
import com.google.api.services.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.GetSchedule;
import com.google.api.services.scheduleEndpoint.model.ScheduleV1Dto;

public class ScheduleActivity extends Activity {
	private final String[] days = {"日", "月", "火", "水", "木", "金", "土"}; 
	private String[] dayData = new String[7];
	private String[] mainData = new String[7*24];
	private String[] timeData = new String[24];

	private FrameLayout mainFrame;
	private DisplayMetrics metrics;
	private GridView dayGrid;
	private GridView timeGrid;
	private GridView mainGrid;
	private DayAdapter dayAdapter;
	private TimeAdapter timeAdapter;
	private CalendarAdapter calendarAdapter;
	
	private Long beginOfWeek;
	private Long endOfWeek;
	private int dayOfSunday;
	
	private GetScheduleTask mCalTask = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		
		// 現在の時刻情報を色々取得
		Calendar now = Calendar.getInstance();
		Calendar sunday = Calendar.getInstance();
		Calendar saturday = Calendar.getInstance();
		dayOfSunday = now.get(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
		sunday.set(Calendar.DAY_OF_MONTH, dayOfSunday);
		saturday.set(Calendar.DAY_OF_MONTH, dayOfSunday + 6);
		beginOfWeek = sunday.getTime().getTime();
		endOfWeek = saturday.getTime().getTime();

		mainFrame = (FrameLayout)findViewById(R.id.calendarMainFrame);
		dayGrid = (GridView) findViewById(R.id.gridView1);
		timeGrid = (GridView) findViewById(R.id.gridView2);
		mainGrid = (GridView) findViewById(R.id.gridView3);

		metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);	

		for(int i=0; i<24; i++){
			timeData[i] = i < 10 ? "0" : "";
			timeData[i] += Integer.toString(i) + ":00";

			for(int j=0; j<7; j++){
				mainData[i*7 + j] = "";
			}
		}

		for(int i=0; i<7; i++){
			dayData[i] = days[i] + " " + Integer.toString(dayOfSunday + i);
		}

		dayAdapter= new DayAdapter(this, R.layout.day_row);
		for(String d: dayData){
			dayAdapter.add(d);
		}
		dayGrid.setAdapter(dayAdapter);

		timeAdapter = new TimeAdapter(this, R.layout.time_row);
		for(String d: timeData){
			timeAdapter.add(d);
		}
		timeGrid.setAdapter(timeAdapter);

		mainGrid = (GridView) findViewById(R.id.gridView3);
		CalendarAdapter calendarAdapter= new CalendarAdapter(this, R.layout.calendar_row);
		for(String d: mainData){
			calendarAdapter.add(d);
		}
		mainGrid.setAdapter(calendarAdapter);
		
		displayCalendar(); // スケジュールを四角で表示
	}
	
	private void displaySchedule(Long startTime, Long finishTime){
		TextView sampleSched = new TextView(this);
		int[] scheduleLayout = new int[3]; // scheduleの {x, y, height}
		
		Calendar start = Calendar.getInstance();
		Calendar finish = Calendar.getInstance();
		start.setTimeInMillis(startTime);
		finish.setTimeInMillis(finishTime);

		scheduleLayout[0] = start.get(Calendar.DAY_OF_MONTH) - dayOfSunday;
		scheduleLayout[1] = start.get(Calendar.HOUR_OF_DAY) - 1; 
		scheduleLayout[2] = finish.get(Calendar.HOUR_OF_DAY) - start.get(Calendar.HOUR_OF_DAY);

		sampleSched.setText("sampleSchedule");
		sampleSched.setBackgroundColor(Color.BLUE);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				(int)Math.ceil(78*metrics.scaledDensity), 
				(int)Math.ceil(scheduleLayout[2]*50*metrics.scaledDensity));
		lp.leftMargin = (int)Math.ceil(
				78*metrics.scaledDensity*(scheduleLayout[0] + 1) + 40*metrics.scaledDensity);
		lp.topMargin = (int)Math.ceil(
				60*scheduleLayout[1]*metrics.scaledDensity);

		sampleSched.setLayoutParams(lp);
		mainFrame.addView(sampleSched);
	}
	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void displayCalendar() {
		if (mCalTask != null) {
			return;
		}

		boolean cancel = false;

		mCalTask = new GetScheduleTask();
		mCalTask.execute((Void) null);
	}
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class GetScheduleTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... args) {
			try {
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
				String mEmail = pref.getString("email", "");
				GetSchedule getSchedule = endpoint.scheduleV1EndPoint().getSchedule(beginOfWeek, endOfWeek, mEmail);
				
				List<ScheduleV1Dto> schedules = getSchedule.execute().getItems();
				
				if(schedules.size() > 0){
					for(ScheduleV1Dto schedule: schedules){
						displaySchedule(schedule.getStartTime(), schedule.getFinishTime());
					}

					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}

	}
}
