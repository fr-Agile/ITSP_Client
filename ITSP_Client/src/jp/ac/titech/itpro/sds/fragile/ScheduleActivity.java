package jp.ac.titech.itpro.sds.fragile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.GetFriendTask.GetFriendFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetShareTimeTask.GetShareTimeFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarAdapter;
import jp.ac.titech.itpro.sds.fragile.utils.DayAdapter;
import jp.ac.titech.itpro.sds.fragile.utils.TimeAdapter;
import android.animation.AnimatorSet.Builder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import com.google.api.services.getFriendEndpoint.model.GetFriendResultV1Dto;
import com.google.api.services.getFriendEndpoint.model.UserV1Dto;
import com.google.api.services.getShareTimeEndpoint.model.GetShareTimeV1ResultDto;
import com.google.api.services.getShareTimeEndpoint.model.GroupScheduleV1Dto;
import com.google.api.services.scheduleEndpoint.ScheduleEndpoint;
import com.google.api.services.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.GetSchedule;
import com.google.api.services.scheduleEndpoint.model.ScheduleV1Dto;

public class ScheduleActivity extends Activity 
	implements GetFriendFinishListener, GetShareTimeFinishListener {
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
	
	private Handler mHandler;
	
	private GetScheduleTask mCalTask = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		
		Button scheduleinput_btn = (Button)findViewById(R.id.go_to_inputschedule_from_schedule);
	    scheduleinput_btn.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {   //スケジュール登録画面へ遷移
	    		startActivity(new Intent(ScheduleActivity.this, ScheduleInputActivity.class));
	    	}
	    });
	    initShareTimeBtn();
		
	    
	    // ハンドラを取得
	    mHandler = new Handler();
		
		// 現在の時刻情報を色々取得
		Calendar now = Calendar.getInstance();
		Calendar sunday = Calendar.getInstance();
		Calendar saturday = Calendar.getInstance();
		dayOfSunday = now.get(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_WEEK) + Calendar.SUNDAY;
		sunday.set(Calendar.DAY_OF_MONTH, dayOfSunday);
		sunday.set(Calendar.HOUR_OF_DAY, 0);
		sunday.set(Calendar.MINUTE, 0);
		sunday.set(Calendar.SECOND, 0);
		sunday.set(Calendar.MILLISECOND, 0);
		saturday.set(Calendar.DAY_OF_MONTH, dayOfSunday + 6);
		saturday.set(Calendar.HOUR_OF_DAY, 23);
		saturday.set(Calendar.MINUTE, 59);
		saturday.set(Calendar.SECOND, 59);
		saturday.set(Calendar.MILLISECOND, 999);
		// 00:00スタート、23:59終了にする
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
	
	private void initShareTimeBtn() {
		Button sharetime_btn = (Button)findViewById(R.id.go_to_sharetime_from_schedule);
	    sharetime_btn.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {   
	    		// 友人リストを取得
	    		getFriendList();
	    	}
	    });
	}
	/**
	 * friend listを取得する
	 */
	private void getFriendList() {
		try {
			SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
			String userEmail = pref.getString("email", "");
			
			// friendのemailリストを取得する
			GetFriendTask task = new GetFriendTask(this);
			task.execute(userEmail);
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "get friend fail");
		}
	}
	/**
	 * GetFriendTask終了後の処理
	 */
	@Override
	public void onFinish(GetFriendResultV1Dto result) {
		try {
			if (result != null) {
				final List<String> friendEmailList = new ArrayList<String>();
				List<UserV1Dto> friendList = result.getFriendList();
				for (UserV1Dto friend : friendList) {
					friendEmailList.add(friend.getEmail());
				}
				String[] emailStrList = friendEmailList.toArray(new String[friendEmailList.size()]);
	    		// 友人リストダイアログを表示
	    		new AlertDialog.Builder(ScheduleActivity.this)
	    		.setTitle("友達を選んでください")
	    		.setItems(emailStrList, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 共通空き時間を検索
						getShareTimeWith(friendEmailList.get(which));
					}
	    		}).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * 
	 */
	public void getShareTimeWith(String friendEmail) {
		try {
			SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
			String userEmail = pref.getString("email", "");
			
			String emailCSV = userEmail + "," + friendEmail;
			
			// 共通空き時間を取得する
			GetShareTimeTask task = new GetShareTimeTask(this);
			task.setEmailCSV(emailCSV);
			task.setStartTime(beginOfWeek);
			task.setFinishTime(endOfWeek);
			task.execute();
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "GetShareTime fail");
		}
	}
	
	/**
	 * 共通空き時間取得後の処理
	 */
	@Override
	public void onFinish(GetShareTimeV1ResultDto result) {
		for (final GroupScheduleV1Dto gs : result.getGroupScheduleList()) {
			if (gs.getUserList() != null) {
				String temp = "";
				for (com.google.api.services.getShareTimeEndpoint.model.UserV1Dto user 
						: gs.getUserList()) {
					temp += user.getEmail() + ",";
				}
				final String userList = temp;
				mHandler.post(new Runnable() {
					public void run() {
						displaySchedule(gs.getStartTime(), gs.getFinishTime(), userList);
					}
				});
			}
		}
	}
	
	private void displaySchedule(Long startTime, Long finishTime, String scheduleStr) {
		TextView sampleSched = new TextView(this);
		double[] scheduleLayout = new double[3]; // scheduleの {x, y, height}

		Calendar start = Calendar.getInstance();
		Calendar finish = Calendar.getInstance();
		start.setTimeInMillis(startTime);
		finish.setTimeInMillis(finishTime);

		scheduleLayout[0] = start.get(Calendar.DAY_OF_MONTH) - dayOfSunday;
		scheduleLayout[1] = start.get(Calendar.HOUR_OF_DAY)
							+ start.get(Calendar.MINUTE) / 60.0;
		scheduleLayout[2] = finish.get(Calendar.HOUR_OF_DAY)
							+ finish.get(Calendar.MINUTE) / 60.0
				- scheduleLayout[1];

		/* 5: 右側にあるなぞの隙間　たぶんバー？ */
		int width = findViewById(R.id.gridView3).getWidth() - 5;

		sampleSched.setText(scheduleStr);
		sampleSched.setBackgroundColor(Color.BLUE);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				(int) Math.ceil(width / 7.0),
				(int) Math.ceil(scheduleLayout[2] * 1514 / 24.0
						* metrics.scaledDensity));
		
		lp.leftMargin = (int) Math.ceil(width / 7.0
				* (scheduleLayout[0]) + 40 * metrics.scaledDensity);
				//* (scheduleLayout[0] + 1) + 40 * metrics.scaledDensity);
		lp.topMargin = (int) Math.ceil(1514 * scheduleLayout[1] / 24.0
				* metrics.scaledDensity);

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
					for(final ScheduleV1Dto schedule: schedules){
						mHandler.post(new Runnable() {
							public void run() {
								displaySchedule(schedule.getStartTime(), schedule.getFinishTime(), "sampleSchedule");
							}
							
						});
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
