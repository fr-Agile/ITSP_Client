package jp.ac.titech.itpro.sds.fragile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.CreateScheduleListTask.CreateScheduleListFinishListener;
import jp.ac.titech.itpro.sds.fragile.DeleteAllScheduleTask.DeleteAllScheduleFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetFriendTask.GetFriendFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetGroupTask.GetGroupFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetShareTimeTask.GetShareTimeFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarAdapter;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarUtils;
import jp.ac.titech.itpro.sds.fragile.utils.DayAdapter;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleCalendarLoader;
import jp.ac.titech.itpro.sds.fragile.utils.TimeAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
		GetFriendFinishListener, GetShareTimeFinishListener, GetGroupFinishListener,
		LoaderCallbacks<Cursor>, DeleteAllScheduleFinishListener, CreateScheduleListFinishListener {
	
	private static final long START_OF_DAY = 0;
	private static final long END_OF_DAY = 24 * 60 * 60 * 1000;
	private static final long ONE_DAY = 24 * 60 * 60 * 1000;
	private static final String SUCCESS = CommonConstant.SUCCESS;
	
	
	private final String[] days = { "日", "月", "火", "水", "木", "金", "土" };
	private int[] dayData = new int[7];
	private int[] monthData = new int[7];
	private int[] yearData = new int[7];
	private String[] dayLabel = new String[7];
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

	private Calendar current;
	private Calendar mBeginOfWeek;
	private Calendar mEndOfWeek;
	private StoreData data;

	int dayOfWeek;

	private Handler mHandler;

	private GetScheduleTask mCalTask = null;
	private DeleteScheduleTask mDelTask = null;

	private boolean[] mFriendCheckFlags;
	private boolean[] mGroupCheckFlags;

	private List<UserV1Dto> mFriendList = null;
	private List<GroupV1Dto> mGroupList = null;
	private List<ScheduleV1Dto> mCreateScheduleList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);

		Intent intent = getIntent();
		data = (StoreData) intent.getSerializableExtra("StoreData");
		if (data == null) {
			Calendar nowCal = Calendar.getInstance();
			data = new StoreData(nowCal);
		}
		// Calendar now = (Calendar)data.getCal().clone();
		current = (Calendar) data.getCal().clone();

		// ハンドラを取得
		mHandler = new Handler();

		// 現在の時刻情報を色々取得

		mBeginOfWeek = (Calendar) current.clone();
		mEndOfWeek = (Calendar) current.clone();

		dayOfWeek = current.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;

		// 00:00スタート、23:59終了にする
		mBeginOfWeek.set(Calendar.DAY_OF_MONTH,
				current.get(Calendar.DAY_OF_MONTH));
		mBeginOfWeek.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
		mBeginOfWeek.set(Calendar.HOUR_OF_DAY, 0);
		mBeginOfWeek.set(Calendar.MINUTE, 0);
		mBeginOfWeek.set(Calendar.SECOND, 0);
		mBeginOfWeek.set(Calendar.MILLISECOND, 0);
		mEndOfWeek.set(Calendar.DAY_OF_MONTH,
				mBeginOfWeek.get(Calendar.DAY_OF_MONTH));
		mEndOfWeek.add(Calendar.DAY_OF_MONTH, 6);
		mEndOfWeek.set(Calendar.HOUR_OF_DAY, 23);
		mEndOfWeek.set(Calendar.MINUTE, 59);
		mEndOfWeek.set(Calendar.SECOND, 59);
		mEndOfWeek.set(Calendar.MILLISECOND, 999);

		Log.d("Calendar",
				"Year: " + current.get(Calendar.YEAR) + "Month: "
						+ (current.get(Calendar.MONTH) + 1) + "Days: "
						+ current.get(Calendar.DAY_OF_MONTH) + "Day of week: "
						+ current.get(Calendar.DAY_OF_WEEK));
		Log.d("Calendar begin of week",
				"Year: " + mBeginOfWeek.get(Calendar.YEAR) + "Month: "
						+ (mBeginOfWeek.get(Calendar.MONTH) + 1) + "Days: "
						+ mBeginOfWeek.get(Calendar.DAY_OF_MONTH)
						+ "Day of week: "
						+ mBeginOfWeek.get(Calendar.DAY_OF_WEEK));
		Log.d("Calendar end of week", "Year: " + mEndOfWeek.get(Calendar.YEAR)
				+ "Month: " + (mEndOfWeek.get(Calendar.MONTH) + 1) + "Days: "
				+ mEndOfWeek.get(Calendar.DAY_OF_MONTH) + "Day of week: "
				+ mEndOfWeek.get(Calendar.DAY_OF_WEEK));

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
			dayData[i] = cal.get(Calendar.DAY_OF_MONTH);
			monthData[i] = cal.get(Calendar.MONTH);
			yearData[i] = cal.get(Calendar.YEAR);
			dayLabel[i] = days[i] + "  " + Integer.toString(dayData[i]);
		}

		dayAdapter = new DayAdapter(this, R.layout.day_row);
		for (String d : dayLabel) {
			dayAdapter.add(d);
		}
		dayGrid.setAdapter(dayAdapter);

		timeAdapter = new TimeAdapter(this, R.layout.time_row);
		for (String d : timeData) {
			timeAdapter.add(d);
		}
		timeGrid.setAdapter(timeAdapter);

		mainGrid = (GridView) findViewById(R.id.gridView3);
		mainGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ScheduleActivity.this, ScheduleInputActivity.class);
                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.YEAR, yearData[position % 7]);
                startTime.set(Calendar.MONTH, monthData[position % 7]);
                startTime.set(Calendar.DAY_OF_MONTH, dayData[position % 7]);
                startTime.set(Calendar.HOUR_OF_DAY, position / 7);
                startTime.set(Calendar.MINUTE, 0);
                startTime.set(Calendar.SECOND, 0);
                intent.putExtra("startTime", startTime);

                startActivity(intent);
            }
        });
		calendarAdapter = new CalendarAdapter(this, R.layout.calendar_row);
		for (String d : mainData) {
			calendarAdapter.add(d);
		}
		mainGrid.setAdapter(calendarAdapter);

		displayCalendar(); // スケジュールを四角で表示
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.schdule_activity_actions, menu);
		
	    menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				current.setTimeInMillis(current.getTimeInMillis() - 7 * ONE_DAY);
				changeDateAfterClickNextPreviousButton();
		        return true;
			}
		});

	    menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				current.setTimeInMillis(current.getTimeInMillis() + 7 * ONE_DAY);
				changeDateAfterClickNextPreviousButton();
		        return true;
			}
		});
	    
		menu.getItem(2).setOnMenuItemClickListener(
				new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						// 友人リストを取得
						getFriendList();
						return true;
					}
				});

		menu.getItem(3).setOnMenuItemClickListener(
				new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						startActivity(new Intent(ScheduleActivity.this,
								ScheduleInputActivity.class));

						return true;
					}
				});

		menu.getItem(4).setOnMenuItemClickListener(
				new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						startActivity(new Intent(ScheduleActivity.this,
								MakeGroupActivity.class));

						return true;
					}
				});

		menu.getItem(5).setOnMenuItemClickListener(
				new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						startActivity(new Intent(ScheduleActivity.this,
								FriendActivity.class));

						return true;
					}
				});

		menu.getItem(6).setOnMenuItemClickListener(
				new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						SharedPreferences pref = getSharedPreferences("user",
								Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = pref.edit();
						editor.remove("email");
						editor.commit();

						startActivity(new Intent(ScheduleActivity.this,
								LoginActivity.class));
						
						ScheduleActivity.this.onDestroy();
						return true;
					}
				});
	    menu.getItem(7).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				getLoaderManager().initLoader(0, null, ScheduleActivity.this);
				
				return true;
			}
	    });

		return super.onCreateOptionsMenu(menu);
	}

	private void changeDateAfterClickNextPreviousButton() {
		dayOfWeek = current.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;

		// 00:00スタート、23:59終了にする
		mBeginOfWeek = (Calendar) current.clone();
		mBeginOfWeek.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
		mBeginOfWeek.set(Calendar.HOUR_OF_DAY, 0);
		mBeginOfWeek.set(Calendar.MINUTE, 0);
		mBeginOfWeek.set(Calendar.SECOND, 0);
		mBeginOfWeek.set(Calendar.MILLISECOND, 0);
		mEndOfWeek = (Calendar) mBeginOfWeek.clone();
		mEndOfWeek.add(Calendar.DAY_OF_MONTH, 6);
		mEndOfWeek.set(Calendar.HOUR_OF_DAY, 23);
		mEndOfWeek.set(Calendar.MINUTE, 59);
		mEndOfWeek.set(Calendar.SECOND, 59);
		mEndOfWeek.set(Calendar.MILLISECOND, 999);

		Log.d("Calendar",
				"Year:" + current.get(Calendar.YEAR) + " Month:"
						+ (current.get(Calendar.MONTH) + 1) + "Days:"
						+ current.get(Calendar.DAY_OF_MONTH) + " Day of week:"
						+ current.get(Calendar.DAY_OF_WEEK));
		Log.d("Calendar begin of week",
				"Year:" + mBeginOfWeek.get(Calendar.YEAR) + " Month:"
						+ (mBeginOfWeek.get(Calendar.MONTH) + 1) + " Days:"
						+ mBeginOfWeek.get(Calendar.DAY_OF_MONTH)
						+ " Day of week:"
						+ mBeginOfWeek.get(Calendar.DAY_OF_WEEK));
		Log.d("Calendar end of week", "Year:" + mEndOfWeek.get(Calendar.YEAR)
				+ " Month:" + (mEndOfWeek.get(Calendar.MONTH) + 1) + " Days:"
				+ mEndOfWeek.get(Calendar.DAY_OF_MONTH) + " Day of week:"
				+ mEndOfWeek.get(Calendar.DAY_OF_WEEK));

		// for (int i = 0; i < 24; i++) {
		// timeData[i] = i < 10 ? "0" : "";
		// timeData[i] += Integer.toString(i) + ":00";
		//
		// for (int j = 0; j < 7; j++) {
		// mainData[i * 7 + j] = "";
		// }
		// }

		for (int i = 0; i < 7; i++) {
			Calendar cal = (Calendar) mBeginOfWeek.clone();
			cal.add(Calendar.DAY_OF_MONTH, i);
			dayLabel[i] = days[i] + "  "
					+ Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
			Log.d("day of month:", " " + dayData[i]);
		}

		dayAdapter.calendars.clear();
		for (String d : dayLabel) {
			dayAdapter.add(d);
		}
		dayAdapter.notifyDataSetChanged();
		// dayGrid.invalidateViews();

		// timeAdapter.clear();
		// for (String d : timeData) {
		// timeAdapter.add(d);
		// }
		// timeAdapter.notifyDataSetChanged();
		// timeGrid.invalidateViews();

		// calendarAdapter.calendars.clear();
		// for (String d : mainData) {
		// calendarAdapter.add(d);
		// }
		// calendarAdapter.notifyDataSetChanged();
		// mainGrid.invalidateViews();

		// remove old schedule
		int count = mainFrame.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = mainFrame.getChildAt(i);
			if (v instanceof TextView) {
				mainFrame.removeView(v);
			}
		}
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
		if (mGroupCheckFlags == null
				|| (mGroupCheckFlags.length != mGroupList.size())) {
			mGroupCheckFlags = new boolean[mGroupList.size()];
		}
		if (mFriendCheckFlags == null
				|| (mFriendCheckFlags.length != mFriendList.size())) {
			mFriendCheckFlags = new boolean[mFriendList.size()];
		}
		try {
			// 今のチェック状態を保存
			final boolean[] groupTempFlags = mGroupCheckFlags.clone();
			final boolean[] friendTempFlags = mFriendCheckFlags.clone();
			new GroupFriendAlertDialogBuilder(ScheduleActivity.this)
					.setGroupAndFriend(mGroupList, mGroupCheckFlags,
							mFriendList, mFriendCheckFlags)
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
			e.printStackTrace();
			Log.d("DEBUG", "GroupFriendAlertDialog fail");
		}
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
				List<com.appspot.fragile_t.getShareTimeEndpoint.model.UserV1Dto> userList = gs
						.getUserList();
				for (int i = 0; i < userList.size(); i++) {
					com.appspot.fragile_t.getShareTimeEndpoint.model.UserV1Dto user = userList
							.get(i);
					temp += user.getLastName() + " " + user.getFirstName();

					if (i != userList.size() - 1) {
						temp += ", ";
					}
				}
				final String userListStr = temp;
				mHandler.post(new Runnable() {
					public void run() {
						displaySchedule(gs.getStartTime(), gs.getFinishTime(),
								"", userListStr);
					}
				});
			}
		}
	}

	private void displaySchedule(Long startTime, Long finishTime,
			final String keyS, String scheduleStr) {
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
		// sampleSched.setBackgroundColor(Color.CYAN);
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
		sampleSched.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(ScheduleActivity.this)
						.setTitle("このスケジュールをどうしますか？")
						.setNegativeButton("編集する",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intentEdit = new Intent(
												ScheduleActivity.this,
												ScheduleEditActivity.class);
										intentEdit.putExtra("key", keySS);
										intentEdit.putExtra("start", startE);
										intentEdit.putExtra("finish", finishE);
										startActivity(intentEdit);
									}
								})
						.setPositiveButton("削除する",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										new AlertDialog.Builder(
												ScheduleActivity.this)
												.setTitle("本当に削除しますか？")
												.setNegativeButton(
														"はい",
														new DialogInterface.OnClickListener() {
															@Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {
																DeleteScheduleTask task = new DeleteScheduleTask();
																task.execute(keySS);
																Toast.makeText(
																		ScheduleActivity.this,
																		"削除しました",
																		Toast.LENGTH_SHORT)
																		.show();
															}
														})
												.setPositiveButton(
														"いいえ",
														new DialogInterface.OnClickListener() {
															@Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {
															}
														}).show();
									}
								}).show();
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

				List<ScheduleV1Dto> schedules = getSchedule.execute()
						.getItems();

				if (schedules.size() > 0) {
					for (final ScheduleV1Dto schedule : schedules) {
						mHandler.post(new Runnable() {
							public void run() {
								displaySchedule(schedule.getStartTime(),
										schedule.getFinishTime(),
										schedule.getKey(), "予定");
							}

						});
					}
				}

				// 繰り返しスケジュール
				RepeatScheduleEndpoint repeatEndpoint = RemoteApi
						.getRepeatScheduleEndpoint();
				GetRepeatSchedule getRepeatSchedule = repeatEndpoint
						.repeatScheduleV1EndPoint().getRepeatSchedule(
								START_OF_DAY, END_OF_DAY, mEmail);
				List<RepeatScheduleV1Dto> repeatList = getRepeatSchedule
						.execute().getItems();
				if (repeatList.size() > 0) {
					for (RepeatScheduleV1Dto repeat : repeatList) {
						for (int day : repeat.getRepeatDays()) {
							// TODO exceptの処理を行う
							final Calendar start = (Calendar) mBeginOfWeek
									.clone();
							final Calendar finish = (Calendar) mBeginOfWeek
									.clone();

							// dayは曜日を表す。今週の日曜日からのずれを足し込む
							start.add(Calendar.DAY_OF_MONTH, day);
							finish.add(Calendar.DAY_OF_MONTH, day);

							// startTimeとfinishTimeを設定する
							start.add(Calendar.MILLISECOND, repeat
									.getStartTime().intValue());
							finish.add(Calendar.MILLISECOND, repeat
									.getFinishTime().intValue());

							mHandler.post(new Runnable() {
								public void run() {
									displaySchedule(start.getTime().getTime(),
											finish.getTime().getTime(), "",
											"予定");
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

	public class DeleteScheduleTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... args) {
			String keySS = args[0];
			try {
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				DeleteSchedule deleteSchedule = endpoint.scheduleV1EndPoint()
						.deleteSchedule(keySS);
				deleteSchedule.execute();
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mDelTask = null;
			if (success) {
				Intent intentDel = new Intent(ScheduleActivity.this,
						ScheduleActivity.class);
				startActivity(intentDel);
				Log.d("DEBUG", "スケジュール表示成功");
			} else {
				Log.d("DEBUG", "スケジュール表示失敗");
			}
		}

		@Override
		protected void onCancelled() {
			mDelTask = null;
		}
	}

	/**
	 * GoogleCalendarのインポート処理
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new GoogleCalendarLoader(this);
	}

	/**
	 * ロードした予定をリストに保存
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		if (arg1.moveToFirst()) {
			mCreateScheduleList = new ArrayList<ScheduleV1Dto>();
			
			do {
				ScheduleV1Dto sche = new ScheduleV1Dto();
				sche.setKey(arg1.getString(4)); // 意味ない。デバッグ用。後々、タイトルとして使う
				
				String test = arg1.getString(5);
				if ("1".equals(arg1.getString(5))) {
					// 終日の予定の場合
					// 今のところ予定は一日で終わるのでスタートの日で登録する
					long startJulianDay = Long.parseLong(arg1.getString(6));
					Calendar time = Calendar.getInstance();
					time.setTimeInMillis(julianDayToTime(startJulianDay));
					sche.setStartTime(time.getTimeInMillis());
					
					time.add(Calendar.DAY_OF_MONTH, 1);
					time.add(Calendar.MILLISECOND, -1);
					sche.setFinishTime(time.getTimeInMillis());
				} else {
					sche.setStartTime(Long.parseLong(arg1.getString(2)));
					sche.setFinishTime(Long.parseLong(arg1.getString(3)));
				}
				mCreateScheduleList.add(sche);
				
				/* debug */
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(sche.getStartTime());
				long g = cal.getTimeInMillis();
				/* degugここまで */
				
			} while (arg1.moveToNext());
			// まず、登録されている予定を削除
			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String email = pref.getString("email", "");
			DeleteAllScheduleTask delAllTask = new DeleteAllScheduleTask(this);
			delAllTask.execute(email);
		} else {
			Log.d("DEBUG", "import GoogleCalendar failed?");
		}
	}

	/**
	 * ユリウス日を1970年1月1日0時UCTからのミリ秒に変換. 日本標準時より9時間ずれている。-9時間すればok
	 * さらにユリウス日は正午に変わる。-12時間する。よって-21時間する。
	 */
	private long julianDayToTime(long startJulianDay) {
		return (long) ((startJulianDay - 2440587.5) * (24.0*60*60*1000) - (21.0*60*60*1000));
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d("DEBUG", "import GoogleCalendar canceled");
	}

	@Override
	public void onDeleteAllScheduleTaskFinish(ScheduleResultV1Dto result) {
		if (SUCCESS.equals(result.getResult())) {
			// 予定を削除したらリストを追加
			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String email = pref.getString("email", "");
			CreateScheduleListTask createScheListTask = 
					new CreateScheduleListTask(this, email, mCreateScheduleList);
			createScheListTask.execute();
		} else {
			Log.d("DEBUG", "delete all schedule failed");
		}
	}

	@Override
	public void onCreateScheduleListTaskFinish(ScheduleResultV1Dto result) {
		if (SUCCESS.equals(result.getResult())) {
			// 予定を書き換え終わったので再描画
			// 表示しているスケジュールをクリア
			for (View view : viewOfSchedule) {
				mainFrame.removeView(view);
			}
			viewOfSchedule.clear();
			displayCalendar();
		} else {
			Log.d("DEBUG", "create schedule list failed");
		}
	}
	
	/**
	 * GoogleCalendarのインポート処理ここまで
	 */
}
