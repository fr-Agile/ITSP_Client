package jp.ac.titech.itpro.sds.fragile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.CreateRepeatScheduleListTask.CreateRepeatScheduleListFinishListener;
import jp.ac.titech.itpro.sds.fragile.CreateScheduleListTask.CreateScheduleListFinishListener;
import jp.ac.titech.itpro.sds.fragile.DeleteAllRepeatScheduleTask.DeleteAllRepeatScheduleFinishListener;
import jp.ac.titech.itpro.sds.fragile.DeleteAllScheduleTask.DeleteAllScheduleFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarUtils;

import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleResultV1Dto;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleResultV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleV1Dto;
import com.google.api.client.util.DateTime;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;


/**
 * インポートしたGoogleCalendarをあじゃ助のサーバに保存する
 * TODO 非同期化した方が良いかも
 */
public class CalendarSaver implements DeleteAllScheduleFinishListener, CreateScheduleListFinishListener, DeleteAllRepeatScheduleFinishListener, CreateRepeatScheduleListFinishListener {
	private static final String SUCCESS = CommonConstant.SUCCESS;
	
	private Context context;
	private GoogleCalendarSaveFinishListener listener;
	private List<ScheduleV1Dto> mCreateScheduleList = null;
	private List<RepeatScheduleV1Dto> mCreateRepeatScheduleList = null;
	private boolean finishFlag = false;
	
	public CalendarSaver(Context context, GoogleCalendarSaveFinishListener listener) {
		this.context = context;
		this.listener = listener;
	}
	
	public void save(Cursor arg1) {
		boolean result = translateToDto(arg1);
		if (result) {
			// まず、登録されているGoogleの予定を削除(2つ目の引数trueでgoogleのみ)
			SharedPreferences pref = context.getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String email = pref.getString("email", "");
			DeleteAllScheduleTask delAllTask = new DeleteAllScheduleTask(this, true);
			delAllTask.execute(email);
			
			DeleteAllRepeatScheduleTask delAllRepTask = new DeleteAllRepeatScheduleTask(this, true);
			delAllRepTask.execute(email);
		}
	}
	
	private boolean translateToDto(Cursor arg1) {
		if (arg1.moveToFirst()) {
			mCreateScheduleList = new ArrayList<ScheduleV1Dto>();
			mCreateRepeatScheduleList = new ArrayList<RepeatScheduleV1Dto>();
			
			do {
				ScheduleV1Dto sche = new ScheduleV1Dto();
				
				GoogleCalendarInstance gci = new GoogleCalendarInstance(arg1);
								
				if (gci.getRrule() != null) {	// 繰り返しの場合
					// Rruleを解析
					Rrule rrule = new Rrule(gci.getRrule());
					
					if (rrule.isWeekly()) {		// weeklyスケジュールだけ登録する
						long startTime;
						long finishTime;
						if (gci.getAllday()) {
							// 終日の予定の場合
							startTime = 0;
							finishTime = CalendarUtils.ONEDAY_INMILLIS;
						} else {
							long beginOfDate = 
									CalendarUtils.getBeginOfDate(gci.getBeginTime()).getTimeInMillis();
							startTime = gci.getBeginTime() - beginOfDate;
							finishTime = gci.getEndTime() - beginOfDate;
						}
						// 繰り返しスケジュールのstartとfinishを登録
						RepeatScheduleV1Dto repsche = new RepeatScheduleV1Dto();
						repsche.setStartTime(startTime);
						repsche.setFinishTime(finishTime);
						
						// Idをセット
						repsche.setGoogleId(gci.getGoogleId());
						// nameを登録
						repsche.setName(gci.getTitle());
						
						
						// 繰り返す曜日を設定
						repsche.setRepeatDays(rrule.getRepeatDays());
						
						// 繰り返しの候補日を列挙
						List<DateTime> exceptCandidates = rrule.getRepeatDateList(gci.getDtstart());
						// 期間を計算(候補日の最初から最後までを候補として登録)
						repsche.setRepeatBegin(exceptCandidates.get(0).getValue());
						repsche.setRepeatEnd(exceptCandidates.get(exceptCandidates.size()-1).getValue());
						// ここから例外日を計算する
						long prev_id = gci.getEventId();
						while (true) {
							// google calendarの予定はevent_id順に並んでいるので
							// 同じないようの繰り返しスケジュールの間、このループの中で繰り返す
							// 実際にスケジュールとして現れた日にちは例外候補から取り除く
							Calendar date = CalendarUtils.getBeginOfDate(gci.getStartDate());
							DateTime dateTime = new DateTime(date.getTime());
							exceptCandidates.remove(dateTime);
							
							// 同じidじゃなくなったらおしまい。
							if (arg1.moveToNext()) {
								gci = new GoogleCalendarInstance(arg1);
								if (gci.getEventId() != prev_id) {
									// 行き過ぎてるので一つ戻す
									arg1.moveToPrevious();
									break;
								}
							} else {
								break;
							}
						}
						// 例外日を設定
						repsche.setExcepts(exceptCandidates);
						mCreateRepeatScheduleList.add(repsche);
					}
					
				} else {	// 繰り返しじゃない場合
					if (gci.getAllday()) {
						// 終日の予定の場合
						// 今のところ予定は一日で終わるのでスタートの日で登録する
						sche.setStartTime(CalendarUtils.getBeginOfDate(gci.getStartDate())
								.getTimeInMillis());						
						sche.setFinishTime(CalendarUtils.getEndOfDate(gci.getStartDate())
								.getTimeInMillis());
					} else {
						sche.setStartTime(gci.getBeginTime());
						sche.setFinishTime(gci.getEndTime());
					}
					sche.setGoogleId(gci.getGoogleId());
					// nameを登録
					sche.setName(gci.getTitle());
					mCreateScheduleList.add(sche);
				}
				
				/* debug 
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(sche.getStartTime());
				long g = cal.getTimeInMillis();
				 degugここまで */
				
			} while (arg1.moveToNext());
			

			return true;
		} else {
			Log.d("DEBUG", "import GoogleCalendar failed?");
			return false;
		}
	}
	


	@Override
	public void onDeleteAllScheduleTaskFinish(ScheduleResultV1Dto result) {
		if ((result != null) && SUCCESS.equals(result.getResult())) {
			// 予定を削除したらリストを追加
			SharedPreferences pref = context.getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String email = pref.getString("email", "");
			CreateScheduleListTask createScheListTask = 
					new CreateScheduleListTask(this, email, mCreateScheduleList);
			createScheListTask.execute();
			Log.d("DEBUG", "delete all schedule success");
		} else {
			Log.d("DEBUG", "delete all schedule failed");
		}
	}

	@Override
	public void onCreateScheduleListTaskFinish(ScheduleResultV1Dto result) {
		if ((result != null) && SUCCESS.equals(result.getResult())) {
			// フラグを建てる
			if (finishFlag) {
				this.finish();
			} else {
				finishFlag = true;
			}
			Log.d("DEBUG", "create schedule list success");
		} else {
			Log.d("DEBUG", "create schedule list failed");
		}
	}

	@Override
	public void onDeleteAllRepeatScheduleTaskFinish(
			RepeatScheduleResultV1Dto result) {
		if ((result != null) && SUCCESS.equals(result.getResult())) {
			// 予定を削除したらリストを追加
			SharedPreferences pref = context.getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String email = pref.getString("email", "");
			CreateRepeatScheduleListTask createRepeatScheListTask = 
					new CreateRepeatScheduleListTask(this, email, mCreateRepeatScheduleList);
			createRepeatScheListTask.execute();
			Log.d("DEBUG", "delete all repeat schedule success");
		} else {
			Log.d("DEBUG", "delete all repeat schedule failed");
		}
	}

	@Override
	public void onCreateRepeatScheduleListTaskFinish(
			RepeatScheduleResultV1Dto result) {
		if ((result != null) && SUCCESS.equals(result.getResult())) {
			// フラグを建てる
			if (finishFlag) {
				this.finish();
			} else {
				finishFlag = true;
			}
			Log.d("DEBUG", "create repeat schedule list success");
		} else {
			Log.d("DEBUG", "create repeat schedule list failed");
		}	
	}
	
	private void finish() {
		if (listener != null) {
			listener.onGoogleCalendarSaveFinish(true);
		}
	}
		
	/**
	 * google calendar保存終了通知用のリスナー 
	 */
	public interface GoogleCalendarSaveFinishListener {
		public void onGoogleCalendarSaveFinish(boolean result);
	}
}
