package jp.ac.titech.itpro.sds.fragile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.GetFriendTask.GetFriendFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetGroupTask.GetGroupFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetShareTimeTask.GetShareTimeFinishListener;
import jp.ac.titech.itpro.sds.fragile.CalendarSaver.GoogleCalendarSaveFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarAdapter;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarUtils;
import jp.ac.titech.itpro.sds.fragile.utils.DayAdapter;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleAccountChecker;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleAccountChecker.GoogleAccountCheckFinishListener;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleCalendarChecker;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleCalendarChecker.GoogleCalendarCheckFinishListener;
import jp.ac.titech.itpro.sds.fragile.utils.TimeAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
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
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleV1Dto;
import com.google.api.client.util.DateTime;

public class ScheduleActivity extends Activity implements
		GetFriendFinishListener, GetShareTimeFinishListener, GetGroupFinishListener,
		GoogleAccountCheckFinishListener, GoogleCalendarCheckFinishListener, 
		GoogleCalendarSaveFinishListener {
	
	private static final String TAG = "ScheduleActivity";
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
	
	private int[] mHistoricalPosition;

	int dayOfWeek;

	private Handler mHandler;

	private GetScheduleTask mCalTask = null;
	private DeleteScheduleTask mDelTask = null;

	private int[] mFriendCheckFlags;
	private int[] mGroupCheckFlags;
	private List<String> mImportantList = null;

	private List<UserV1Dto> mFriendList = null;
	private List<GroupV1Dto> mGroupList = null;

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
		current = (Calendar) data.getCal().clone();

		// ハンドラを取得
		mHandler = new Handler();

		setUpWeek();

		mainFrame = (FrameLayout) findViewById(R.id.calendarMainFrame);
		dayGrid = (GridView) findViewById(R.id.gridView1);
		timeGrid = (GridView) findViewById(R.id.gridView2);
		mainGrid = (GridView) findViewById(R.id.gridView3);

		metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		//timeGrid
		timeAdapter = new TimeAdapter(this, R.layout.time_row);
		for (int i = 0; i < 24; i++) {
			timeData[i] = i < 10 ? "0" : "";
			timeData[i] += Integer.toString(i) + ":00";

			for (int j = 0; j < 7; j++) {
				mainData[i * 7 + j] = "";
			}
		}
		
		for (String d : timeData) {
			timeAdapter.add(d);
		}
		timeGrid.setAdapter(timeAdapter);
		
		//dayGrid
		dayAdapter = new DayAdapter(this, R.layout.day_row);
		for (int i = 0; i < 7; i++) {
			Calendar cal = (Calendar) mBeginOfWeek.clone();
			cal.add(Calendar.DAY_OF_MONTH, i);
			dayData[i] = cal.get(Calendar.DAY_OF_MONTH);
			monthData[i] = cal.get(Calendar.MONTH);
			yearData[i] = cal.get(Calendar.YEAR);
			dayLabel[i] = days[i] + "  " + Integer.toString(dayData[i]);
		}
		
		for (String d : dayLabel) {
			dayAdapter.add(d);
		}
		dayGrid.setAdapter(dayAdapter);
		
		//mainGrid
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

		final GestureDetector gesDetector = new GestureDetector(this, new GestureDetector.OnGestureListener(){
			@Override
			public void onLongPress(MotionEvent e) {
                final int[] position = estimatePosition(e.getX(), e.getY());
				final int width = mainGrid.getWidth();
				final int height = mainGrid.getHeight();

				final TextView schedDrag = new TextView(mainGrid.getContext());
				schedDrag.setBackgroundColor(getResources().getColor(R.color.light_gray));
				
				schedDrag.setOnDragListener(new View.OnDragListener() {
					private int n = 1;

					public boolean onDrag(View v, DragEvent e) {
						final int action = e.getAction();
						boolean result = false;
						switch (action) {
						case DragEvent.ACTION_DRAG_STARTED: 
							result = true;
							break;
						case DragEvent.ACTION_DRAG_ENDED: 
							Intent intent = new Intent(ScheduleActivity.this, ScheduleInputActivity.class);
							Calendar startTime = Calendar.getInstance();
							startTime.set(Calendar.YEAR, yearData[position[0]]);
							startTime.set(Calendar.MONTH, monthData[position[0]]);
							startTime.set(Calendar.DAY_OF_MONTH, dayData[position[0]]);
							startTime.set(Calendar.HOUR_OF_DAY, position[1]);
							startTime.set(Calendar.MINUTE, 0);
							startTime.set(Calendar.SECOND, 0);
							intent.putExtra("startTime", startTime);
							Log.d("myDEBUG", "length = " + n);
			                intent.putExtra("length", n);

			                mainFrame.removeView(schedDrag);
							startActivity(intent);

							result = true;
							break;
						case DragEvent.ACTION_DRAG_LOCATION: 
							n = Math.round(Math.max(e.getY(), 0) / (mainGrid.getHeight() / 24)); 
							ViewGroup.LayoutParams lp = schedDrag.getLayoutParams();
							lp.height = height * (n + 1) / 24;
							schedDrag.setLayoutParams(lp);
							result = true;
							break;
						case DragEvent.ACTION_DROP:
							Log.d("myDEBUG", "drag drop.");
							result = true;
							break;
						}
						return result;
					}
				});

				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width / 7, height / 24);
				lp.leftMargin = (int) Math.ceil(width / 7.0 * (position[0] - 1) + 40
						* metrics.scaledDensity);
				lp.topMargin = (int) Math.ceil(1514 * (position[1] - 1) / 24.0
						* metrics.scaledDensity);
				schedDrag.setLayoutParams(lp);
				mainFrame.addView(schedDrag);

				ClipData data = ClipData.newPlainText("text", "text : " + schedDrag.getText());
				schedDrag.startDrag(data, new DragShadowBuilder(schedDrag), (Object)schedDrag, 0);

				return ;
			}

			@Override
			public boolean onDown(MotionEvent e) { return true; }
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return true; }
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,  float distanceX, float distanceY) { return true; }
			@Override
			public void onShowPress(MotionEvent e) {}
			@Override
			public boolean onSingleTapUp(MotionEvent e) { return true; }
			
			private int[] estimatePosition(float x, float y) {
				int[] array = new int[2];
				array[0] = (int) Math.ceil((x * 7) / mainGrid.getWidth());
				array[1] = (int) Math.ceil((y * 24) / mainGrid.getHeight());

				return array;
			}
		}); 
		
		mainGrid.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gesDetector.onTouchEvent(event);
			}
		});

		displayCalendar(); // スケジュールを四角で表示
	}

	private void setUpWeek(){
		// 現在の時刻情報を色々取得
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

		Log.d(TAG,
				"Current: Year:" + current.get(Calendar.YEAR) + " Month:"
						+ (current.get(Calendar.MONTH) + 1) + " DayOfMonth:"
						+ current.get(Calendar.DAY_OF_MONTH) + " DayOfWeek:"
						+ current.get(Calendar.DAY_OF_WEEK));
		Log.d(TAG,
				"Calendar begin of week: Year:"
						+ mBeginOfWeek.get(Calendar.YEAR) + " Month:"
						+ (mBeginOfWeek.get(Calendar.MONTH) + 1)
						+ " DayOfMonth:"
						+ mBeginOfWeek.get(Calendar.DAY_OF_MONTH)
						+ " DayOfWeek:"
						+ mBeginOfWeek.get(Calendar.DAY_OF_WEEK));
		Log.d(TAG,
				"Calendar end of week: Year:" + mEndOfWeek.get(Calendar.YEAR)
						+ " Month:" + (mEndOfWeek.get(Calendar.MONTH) + 1)
						+ " DayOfMonth:"
						+ mEndOfWeek.get(Calendar.DAY_OF_MONTH) + " DayOfWeek:"
						+ mEndOfWeek.get(Calendar.DAY_OF_WEEK));
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
		// google calendarインポートボタン
	    menu.getItem(7).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// GoogleAccountCheckerを起動
				GoogleAccountChecker gac = 
						new GoogleAccountChecker(ScheduleActivity.this, ScheduleActivity.this);
				gac.run();
				return true;
			}
	    });

		return super.onCreateOptionsMenu(menu);
	}

	private void changeDateAfterClickNextPreviousButton() {
		// remove old views
		for (View view : viewOfSchedule) {
			mainFrame.removeView(view);
		}
		viewOfSchedule.clear();
		
		//set up a new week;
		setUpWeek();
		
		//renew dayGrid
		for (int i = 0; i < 7; i++) {
			Calendar cal = (Calendar) mBeginOfWeek.clone();
			cal.add(Calendar.DAY_OF_MONTH, i);
			dayData[i] = cal.get(Calendar.DAY_OF_MONTH);
			monthData[i] = cal.get(Calendar.MONTH);
			yearData[i] = cal.get(Calendar.YEAR);
			dayLabel[i] = days[i] + "  " + Integer.toString(dayData[i]);
		}
		dayAdapter.clear();
		for (String d : dayLabel) {
			dayAdapter.add(d);
		}
		dayAdapter.notifyDataSetChanged();
//		dayGrid.setAdapter(dayAdapter);
		
		displayCalendar(); 
		
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
			mGroupCheckFlags = new int[mGroupList.size()];
		}
		if (mFriendCheckFlags == null
				|| (mFriendCheckFlags.length != mFriendList.size())) {
			mFriendCheckFlags = new int[mFriendList.size()];
		}
		try {
			// 今のチェック状態を保存
			final int[] groupTempFlags = mGroupCheckFlags.clone();
			final int[] friendTempFlags = mFriendCheckFlags.clone();
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
							List<List<String>> selectedList = new ArrayList<List<String>>();
							for (int i=0; i<mFriendList.size(); i++) {
								if (mFriendCheckFlags[i] > 0) {
									List<String> tmp = new ArrayList<String>();
									tmp.add(mFriendList.get(i).getEmail());
									tmp.add(Integer.toString(mFriendCheckFlags[i]));
									selectedList.add(tmp);
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
	public void displayShareTimeWith(List<List<String>> friendList) {
		try {
			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String userEmail = pref.getString("email", "");
			String emailCSV = userEmail;
			mImportantList = new ArrayList<String>();

			for (List<String> friend : friendList) {
				emailCSV += "," + friend.get(0);
				Log.d("IMPORTANT?", friend.get(1));
				if(Integer.parseInt(friend.get(1)) > 1){
					mImportantList.add(friend.get(0));
				}
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
				boolean unwise = false;
				List<com.appspot.fragile_t.getShareTimeEndpoint.model.UserV1Dto> userList = gs
						.getUserList();
				for (int i = 0; i < userList.size(); i++) {
					com.appspot.fragile_t.getShareTimeEndpoint.model.UserV1Dto user = userList
							.get(i);

					unwise = unwise || mImportantList.contains(user.getEmail());
					Log.d("myDEBUG", user.getEmail());

					temp += user.getLastName() + " " + user.getFirstName();

					if (i != userList.size() - 1) {
						temp += ", ";
					}
				}
				final String userListStr = temp;
				final boolean unwiseFinal = unwise;
				mHandler.post(new Runnable() {
					public void run() {
						displaySchedule(gs.getStartTime(), gs.getFinishTime(),
								"", userListStr, unwiseFinal);
					}
				});
			}
		}
	}

	private void displaySchedule(Long startTime, Long finishTime,
			final String keyS, String scheduleStr) {
		displaySchedule(startTime, finishTime, keyS, scheduleStr, false);
	}

	private void displaySchedule(Long startTime, Long finishTime,
			final String keyS, String scheduleStr, Boolean unwise) {
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
		if(unwise){
			sampleSched.setBackground(getResources().getDrawable(R.drawable.frame_unwise));
			sampleSched.setTextColor(getResources().getColor(R.color.light_gray));
		}else{
			sampleSched.setBackground(getResources().getDrawable(R.drawable.frame));
			sampleSched.setTextColor(getResources().getColor(R.color.dark_gray));
		}
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				(int) Math.ceil(width / 7.0), (int) Math.ceil(scheduleLayout[2]
						* 1514 / 24.0 * metrics.scaledDensity));

		lp.leftMargin = (int) Math.ceil(width / 7.0 * (scheduleLayout[0]) + 40
				* metrics.scaledDensity);
		// * (scheduleLayout[0] + 1) + 40 * metrics.scaledDensity);
		lp.topMargin = (int) Math.ceil(1514 * scheduleLayout[1] / 24.0
				* metrics.scaledDensity);
		sampleSched.setLayoutParams(lp);

		if(!unwise){
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
		}
		mainFrame.addView(sampleSched);

		viewOfSchedule.add(sampleSched);
	}

	public int[] estimateItem(float x, float y){
		int[] result = {0, 0};
		int width = findViewById(R.id.gridView3).getWidth() - 5;
		float leftBaseMargin = 40 * metrics.scaledDensity;

		result[0] = (int)Math.ceil((x - leftBaseMargin) / (width / 7.0));
		result[1] = (int)Math.ceil( y / (1514 / 24));

		return result; 
	}

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
							final Calendar start = (Calendar) mBeginOfWeek
									.clone();
							final Calendar finish = (Calendar) mBeginOfWeek
									.clone();
							// dayは曜日を表す。今週の日曜日からのずれを足し込む
							start.add(Calendar.DAY_OF_MONTH, day);
							finish.add(Calendar.DAY_OF_MONTH, day);

							DateTime ds = new DateTime(repeat.getRepeatBegin());
							DateTime de = new DateTime(repeat.getRepeatEnd());
							//　もし開始が繰り返し期間中の時のみ表示
							if ((start.getTimeInMillis() >= repeat.getRepeatBegin()) &&
									(start.getTimeInMillis() <= repeat.getRepeatEnd())) {
							
								// startTimeとfinishTimeを設定する
								start.add(Calendar.MILLISECOND, repeat
										.getStartTime().intValue());
								finish.add(Calendar.MILLISECOND, repeat
										.getFinishTime().intValue());
	
								// もし、exceptされる日にちじゃなければ表示する
								DateTime date = new DateTime(
										CalendarUtils.getBeginOfDate(start.getTimeInMillis()).getTime());
								if ((repeat.getExcepts() == null) ||
										(!repeat.getExcepts().contains(date))) {
									// exceptsには含まれていない
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
	public void onGoogleAccountCheckFinish(boolean result,
			List<String> accountList) {
		if (result) {
			if (accountList != null && accountList.size() > 0) {
				// アカウントが登録されているのでカレンダーを読み込む
				GoogleCalendarChecker gcc = new GoogleCalendarChecker(this, this);
				gcc.run();
				
			} else {
				// アカウントが登録されていない場合、ダイアログを表示して登録を促す
				new AlertDialog.Builder(ScheduleActivity.this)
					.setTitle("Googleアカウントが登録されていません。Googleアカウントを登録してください。")
					.setNegativeButton("いいえ",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {}
						})
									
					.setPositiveButton("はい",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ScheduleActivity.this.startActivity(
										new Intent(android.provider.Settings.ACTION_SETTINGS));
							}
						})
					.show();
			}
			Log.d("DEBUG", "checking google account success");
		} else {
			Log.d("DEBUG", "checking google account fail");
		}
		
	}


	@Override
	public void onGoogleCalendarCheckFinish(boolean result, Cursor arg1) {
		if (result) {
			// カレンダーがインポートできたのデータベースを書き換える
			CalendarSaver cs = new CalendarSaver(this, this);
			cs.save(arg1);
		}
	}

	@Override
	public void onGoogleCalendarSaveFinish(boolean result) {
		if (result) {
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

