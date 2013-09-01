package jp.ac.titech.itpro.sds.fragile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.GetFriendTask.GetFriendFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetGroupTask.GetGroupFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetShareTimeTask.GetShareTimeFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarAdapter;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarUtils;
import jp.ac.titech.itpro.sds.fragile.utils.DayAdapter;
import jp.ac.titech.itpro.sds.fragile.utils.TimeAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;
import com.appspot.fragile_t.getFriendEndpoint.model.GetFriendResultV1Dto;
import com.appspot.fragile_t.getFriendEndpoint.model.UserV1Dto;
import com.appspot.fragile_t.getShareTimeEndpoint.model.GetShareTimeV1ResultDto;
import com.appspot.fragile_t.getShareTimeEndpoint.model.GroupScheduleV1Dto;
import com.appspot.fragile_t.groupEndpoint.model.GroupV1Dto;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.GetRepeatSchedule;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.DeleteSchedule;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.GetSchedule;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleResultV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleV1Dto;

public class ScheduleActivity extends Activity implements
		GetFriendFinishListener, GetShareTimeFinishListener, GetGroupFinishListener {
	
	private static final long START_OF_DAY = 0;
	private static final long END_OF_DAY = 24 * 60 * 60 * 1000;
	
	
	private final String[] days = { "日", "月", "火", "水", "木", "金", "土" };
	private String[] dayData = new String[7];
	private String[] mainData = new String[7 * 24];
	private String[] timeData = new String[24];

	private FrameLayout mainFrame;
	private DisplayMetrics metrics;
	private GridView dayGrid;
	private GridView timeGrid;
	private GridView mainGrid;
	private DayAdapter dayAdapter;
	private TimeAdapter timeAdapter;
	private CalendarAdapter calendarAdapter;
	
	private List<View> viewOfSchedule = new ArrayList<View>();

	private Calendar mBeginOfWeek;
	private Calendar mEndOfWeek;

	private Handler mHandler;

	private GetScheduleTask mCalTask = null;
	
	private boolean[] mFriendCheckFlags;
	private boolean[] mGroupCheckFlags;
	
	private List<UserV1Dto> mFriendList = null;
	private List<GroupV1Dto> mGroupList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);

		Button scheduleinput_btn = (Button) findViewById(R.id.go_to_inputschedule_from_schedule);
		scheduleinput_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { // スケジュール登録画面へ遷移
				startActivity(new Intent(ScheduleActivity.this,
						ScheduleInputActivity.class));
			}
		});
		initShareTimeBtn();

		// ハンドラを取得
		mHandler = new Handler();

		// 現在の時刻情報を色々取得
		Calendar now = Calendar.getInstance();
		mBeginOfWeek = Calendar.getInstance();
		mEndOfWeek = Calendar.getInstance();
		int dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
		
		// 00:00スタート、23:59終了にする
		mBeginOfWeek.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
		mBeginOfWeek.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
		mBeginOfWeek.set(Calendar.HOUR_OF_DAY, 0);
		mBeginOfWeek.set(Calendar.MINUTE, 0);
		mBeginOfWeek.set(Calendar.SECOND, 0);
		mBeginOfWeek.set(Calendar.MILLISECOND, 0);
		mEndOfWeek.set(Calendar.DAY_OF_MONTH, mBeginOfWeek.get(Calendar.DAY_OF_MONTH));
		mEndOfWeek.add(Calendar.DAY_OF_MONTH, 6);
		mEndOfWeek.set(Calendar.HOUR_OF_DAY, 23);
		mEndOfWeek.set(Calendar.MINUTE, 59);
		mEndOfWeek.set(Calendar.SECOND, 59);
		mEndOfWeek.set(Calendar.MILLISECOND, 999);

		mainFrame = (FrameLayout) findViewById(R.id.calendarMainFrame);
		dayGrid = (GridView) findViewById(R.id.gridView1);
		timeGrid = (GridView) findViewById(R.id.gridView2);
		mainGrid = (GridView) findViewById(R.id.gridView3);

		metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		for (int i = 0; i < 24; i++) {
			timeData[i] = i < 10 ? "0" : "";
			timeData[i] += Integer.toString(i) + ":00";

			for (int j = 0; j < 7; j++) {
				mainData[i * 7 + j] = "";
			}
		}

		for (int i = 0; i < 7; i++) {
			Calendar cal = (Calendar) mBeginOfWeek.clone();
			cal.add(Calendar.DAY_OF_MONTH, i);
			dayData[i] = days[i] + " " + Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		}

		dayAdapter = new DayAdapter(this, R.layout.day_row);
		for (String d : dayData) {
			dayAdapter.add(d);
		}
		dayGrid.setAdapter(dayAdapter);

		timeAdapter = new TimeAdapter(this, R.layout.time_row);
		for (String d : timeData) {
			timeAdapter.add(d);
		}
		timeGrid.setAdapter(timeAdapter);

		mainGrid = (GridView) findViewById(R.id.gridView3);
		CalendarAdapter calendarAdapter = new CalendarAdapter(this,
				R.layout.calendar_row);
		for (String d : mainData) {
			calendarAdapter.add(d);
		}
		mainGrid.setAdapter(calendarAdapter);

		displayCalendar(); // スケジュールを四角で表示
	}

	private void initShareTimeBtn() {
		Button sharetime_btn = (Button) findViewById(R.id.go_to_sharetime_from_schedule);
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
			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
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
				mFriendList = result.getFriendList();
				// friendListの取得が終わったら次はgroupList
				getGroupList();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * groupListを取得する
	 */
	private void getGroupList() {
		try {
			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String userEmail = pref.getString("email", "");

			// groupリストを取得する
			GetGroupTask task = new GetGroupTask(this);
			task.execute(userEmail);

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "get friend fail");
		}
	}
	/**
	 * GetGroupTask終了後の処理
	 */
	@Override
	public void onFinish(List<GroupV1Dto> result) {
		try {
			if (result != null) {
				mGroupList = result;
				// alertDialogを表示
				makeShareTimeAlertDialog();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 共通空き時間のためのアラーとダイアログを生成
	 */
	private void makeShareTimeAlertDialog() {
		// もしチェック済みリストの長さと取得したリストの長さが違ったら初期化
		if (mGroupCheckFlags == null || (mGroupCheckFlags.length != mGroupList.size())) {
			mGroupCheckFlags = new boolean[mGroupList.size()];
		}
		if (mFriendCheckFlags == null || (mFriendCheckFlags.length != mFriendList.size())) {
			mFriendCheckFlags = new boolean[mFriendList.size()];
		}
		try {
			// 今のチェック状態を保存
			final boolean[] groupTempFlags = mGroupCheckFlags.clone();
			final boolean[] friendTempFlags = mFriendCheckFlags.clone();
			new GroupFriendAlertDialogBuilder(ScheduleActivity.this)
					.setGroupAndFriend(mGroupList, mGroupCheckFlags, mFriendList, mFriendCheckFlags)
					.setTitle("友達を選んでください")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {							
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 表示しているスケジュールをクリア
							for (View view : viewOfSchedule) {
								mainFrame.removeView(view);
							}
							viewOfSchedule.clear();
							
							// 選ばれたemailのリストを作成
							List<String> selectedList = new ArrayList<String>();
							for (int i=0; i<mFriendList.size(); i++) {
								if (mFriendCheckFlags[i]) {
									selectedList.add(mFriendList.get(i).getEmail());
								}
							}
							if (selectedList.size() > 0) {
								// 共通空き時間表示
								displayShareTimeWith(selectedList);
							} else {
								// 自分のスケジュールを表示
								displayCalendar();
							}
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 元に戻す
							mGroupCheckFlags = groupTempFlags;
							mFriendCheckFlags = friendTempFlags;
						}
					}).show();
		} catch (Exception e) {
			
		}
		/*
		// friendもgroupも両方つめるListを用意
		final List<String> strList = new ArrayList<String>();
		
		// List<String>につめ直し
		final List<String> groupNameList = new ArrayList<String>();
		for (GroupV1Dto group : mGroupList) {
			groupNameList.add(group.getName());
			strList.add(group.getName());
		}
		
		final List<String> friendEmailList = new ArrayList<String>();
		for (UserV1Dto friend : mFriendList) {
			friendEmailList.add(friend.getEmail());
			strList.add(friend.getEmail());
		}
		
		// もしチェック済みリストの長さと取得したリストの長さが違ったら初期化
		if (mGroupCheckFlags == null || (mGroupCheckFlags.length != mGroupList.size())) {
			mGroupCheckFlags = new boolean[mGroupList.size()];
		}
		if (mFriendCheckFlags == null || (mFriendCheckFlags.length != mFriendList.size())) {
			mFriendCheckFlags = new boolean[mFriendList.size()];
		}
		
		try {
			//String[] strArray = strList
			//		.toArray(new String[strList.size()]);
			String[] strArray = friendEmailList.toArray(new String[friendEmailList.size()]);
			
			// 今のチェック状態を保存
			final boolean[] groupFlags2 = mGroupCheckFlags.clone();
			final boolean[] friendFlags2 = mFriendCheckFlags.clone();
			new AlertDialog.Builder(ScheduleActivity.this)
					.setTitle("友達を選んでください")
					.setMultiChoiceItems(strArray, mFriendCheckFlags,
							new DialogInterface.OnMultiChoiceClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, 
										int which, boolean isChecked) {
									// チェックしたものを配列へ
									//mFriendCheckFlags[which] = isChecked;														
								}
							})
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {							
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 表示しているスケジュールをクリア
							for (View view : viewOfSchedule) {
								mainFrame.removeView(view);
							}
							viewOfSchedule.clear();
							
							// 選ばれたemailのリストを作成
							List<String> selectedList = new ArrayList<String>();
							for (int i=0; i<friendEmailList.size(); i++) {
								if (mFriendCheckFlags[i]) {
									selectedList.add(friendEmailList.get(i));
								}
							}
							if (selectedList.size() > 0) {
								// 共通空き時間表示
								displayShareTimeWith(selectedList);
							} else {
								// 自分のスケジュールを表示
								displayCalendar();
							}
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 元に戻す
							mFriendCheckFlags = friendFlags2;
						}
					}).show();
		} catch (Exception e) {
			
		}
		*/
	}

	/**
	 * 複数との共通空き時間を表示
	 */
	public void displayShareTimeWith(List<String> emailList) {
		try {
			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String userEmail = pref.getString("email", "");

			String emailCSV = userEmail;
			for (String email : emailList) {
				emailCSV += "," + email;
			}

			// 共通空き時間を取得する
			GetShareTimeTask task = new GetShareTimeTask(this);
			task.setEmailCSV(emailCSV);
			task.setStartTime(mBeginOfWeek.getTime().getTime());
			task.setFinishTime(mEndOfWeek.getTime().getTime());
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
				List<com.appspot.fragile_t.getShareTimeEndpoint.model.UserV1Dto> userList 
					= gs.getUserList();
				for (int i=0; i<userList.size(); i++) {
					com.appspot.fragile_t.getShareTimeEndpoint.model.UserV1Dto user 
						= userList.get(i);
					temp += user.getLastName() + " " + user.getFirstName();
					
					if (i != userList.size() - 1) {
						temp += ", ";
					}
				}
				final String userListStr = temp;
				mHandler.post(new Runnable() {
					public void run() {
						displaySchedule(gs.getStartTime(), gs.getFinishTime(),"",
								userListStr);
					}
				});
			}
		}
	}

	private void displaySchedule(Long startTime, Long finishTime,final String keyS,
	String scheduleStr) {
		TextView sampleSched = new TextView(this);
		double[] scheduleLayout = new double[3]; // scheduleの {x, y, height}

		final long startE = startTime;
		final long finishE = finishTime;
		final String keySS = keyS;
		
		Calendar start = Calendar.getInstance();
		Calendar finish = Calendar.getInstance();
		start.setTimeInMillis(startTime);
		finish.setTimeInMillis(finishTime);

		scheduleLayout[0] = CalendarUtils.calcDateDiff(start, mBeginOfWeek);
		scheduleLayout[1] = start.get(Calendar.HOUR_OF_DAY)
				+ start.get(Calendar.MINUTE) / 60.0;
		scheduleLayout[2] = finish.get(Calendar.HOUR_OF_DAY)
				+ finish.get(Calendar.MINUTE) / 60.0 - scheduleLayout[1];

		/* 5: 右側にあるなぞの隙間　たぶんバー？ */
		int width = findViewById(R.id.gridView3).getWidth() - 5;

		sampleSched.setText(scheduleStr);
		//sampleSched.setBackgroundColor(Color.CYAN);
		sampleSched.setBackground(getResources().getDrawable(R.drawable.frame));
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				(int) Math.ceil(width / 7.0), (int) Math.ceil(scheduleLayout[2]
						* 1514 / 24.0 * metrics.scaledDensity));

		lp.leftMargin = (int) Math.ceil(width / 7.0 * (scheduleLayout[0]) + 40
				* metrics.scaledDensity);
		// * (scheduleLayout[0] + 1) + 40 * metrics.scaledDensity);
		lp.topMargin = (int) Math.ceil(1514 * scheduleLayout[1] / 24.0
				* metrics.scaledDensity);
		sampleSched.setLayoutParams(lp);
		sampleSched.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				new AlertDialog.Builder(ScheduleActivity.this)
		        .setTitle("このスケジュールをどうしますか？")
		        .setNegativeButton(
		          "編集する", 
		          new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		            	Intent intentEdit = new Intent(ScheduleActivity.this,ScheduleEditActivity.class);
						intentEdit.putExtra("start", startE);
						intentEdit.putExtra("finish", finishE);
						startActivity(intentEdit);
		            }
		          })
		        .setPositiveButton(
		          "削除する", 
		          new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {  
		            	ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
						try {
							DeleteSchedule deleteSchedule = endpoint.scheduleV1EndPoint().deleteSchedule(keySS);
							ScheduleResultV1Dto result = deleteSchedule.execute();
							Toast.makeText(ScheduleActivity.this, keySS, Toast.LENGTH_SHORT).show();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
		        })		        
		        .show();
			}
		});
		mainFrame.addView(sampleSched);
		
		viewOfSchedule.add(sampleSched);
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
				SharedPreferences pref = getSharedPreferences("user",
						Activity.MODE_PRIVATE);
				String mEmail = pref.getString("email", "");
				GetSchedule getSchedule = endpoint.scheduleV1EndPoint()
						.getSchedule(mBeginOfWeek.getTime().getTime(), 
								mEndOfWeek.getTime().getTime(), mEmail);

				List<ScheduleV1Dto> schedules = getSchedule.execute().getItems();

				if (schedules.size() > 0) {
					for (final ScheduleV1Dto schedule : schedules) {
						mHandler.post(new Runnable() {
							public void run() {
								displaySchedule(schedule.getStartTime(),schedule.getFinishTime(),
										schedule.getKey(),"予定");
							}

						});
					}
				}
				
				// 繰り返しスケジュール
				RepeatScheduleEndpoint repeatEndpoint = RemoteApi.getRepeatScheduleEndpoint();
				GetRepeatSchedule getRepeatSchedule = repeatEndpoint.repeatScheduleV1EndPoint()
						.getRepeatSchedule(START_OF_DAY, END_OF_DAY, mEmail);
				List<RepeatScheduleV1Dto> repeatList = getRepeatSchedule.execute().getItems();
				if (repeatList.size() > 0) {
					for (RepeatScheduleV1Dto repeat : repeatList) {
						for (int day : repeat.getRepeatDays()) {
							// TODO exceptの処理を行う
							final Calendar start = (Calendar) mBeginOfWeek.clone();
							final Calendar finish = (Calendar) mBeginOfWeek.clone();
							
							// dayは曜日を表す。今週の日曜日からのずれを足し込む
							start.add(Calendar.DAY_OF_MONTH, day);
							finish.add(Calendar.DAY_OF_MONTH, day);
							
							// startTimeとfinishTimeを設定する
							start.add(Calendar.MILLISECOND, repeat.getStartTime().intValue());
							finish.add(Calendar.MILLISECOND, repeat.getFinishTime().intValue());
							
							mHandler.post(new Runnable() {
								public void run() {
									displaySchedule(start.getTime().getTime(),
											finish.getTime().getTime(), "予定");
								}
	
							});
						}
					}
				}
				
				
				return true;
			} catch (Exception e) {
				Log.d("DEBUG", "show repeatSchedule fail");
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			mCalTask = null;
			if (success) {
				Log.d("DEBUG", "スケジュール表示成功");
			} else {
				Log.d("DEBUG", "スケジュール表示失敗");
			}
		}
		
		@Override
		protected void onCancelled() {
			mCalTask = null;
		}
	}

}
