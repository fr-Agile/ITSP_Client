package jp.ac.titech.itpro.sds.fragile;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.widget.GridView;

public class ScheduleActivity extends Activity {
	private String[] dayData = {"日", "月", "火", "水", "木", "金", "土"}; 
	private String[] mainData = new String[7*24];
	private String[] timeData = new String[24];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		
		// ここでuserのスケジュール情報を取得する。

		//FrameLayout mainFrame = (FrameLayout)findViewById(R.id.calendarMainFrame);
		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);	

		for(int i=0; i<24; i++){
			timeData[i] = Integer.toString(i);

			for(int j=0; j<7; j++){
				mainData[i*7 + j] = i + ":" + j;
			}
		}

		GridView dayGrid = (GridView) findViewById(R.id.gridView1);
		DayAdapter dayAdapter= new DayAdapter(this, R.layout.day_row);
		for(String d: dayData){
			dayAdapter.add(d);
		}
		dayGrid.setAdapter(dayAdapter);

		GridView timeGrid = (GridView) findViewById(R.id.gridView2);
		TimeAdapter timeAdapter= new TimeAdapter(this, R.layout.time_row);
		for(String d: timeData){
			timeAdapter.add(d);
		}
		timeGrid.setAdapter(timeAdapter);

		GridView mainGrid = (GridView) findViewById(R.id.gridView3);
		CalendarAdapter calendarAdapter= new CalendarAdapter(this, R.layout.calendar_row);
		for(String d: mainData){
			calendarAdapter.add(d);
		}
		mainGrid.setAdapter(calendarAdapter);
		
		/*
		TextView sampleSched = new TextView(this);
		double[] scheduleLayout = new double[2];
		scheduleLayout[0] = 3; scheduleLayout[1] = 5; //3日 5時

		sampleSched.setText("sampleSchedule");
		sampleSched.setBackgroundColor(Color.BLUE);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int)Math.ceil(78*metrics.scaledDensity), (int)Math.ceil(100*metrics.scaledDensity));
		lp.leftMargin = (int)Math.ceil(78*metrics.scaledDensity*(scheduleLayout[0] + 1) + 40*metrics.scaledDensity);
		lp.topMargin = (int)Math.ceil(60*scheduleLayout[1]*metrics.scaledDensity);
		Log.v("myDEBUG", Integer.toString(lp.leftMargin));
		sampleSched.setLayoutParams(lp);
		mainFrame.addView(sampleSched);
		*/
	}
}
