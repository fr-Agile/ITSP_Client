package jp.ac.titech.itpro.sds.fragile;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import jp.ac.titech.itpro.sds.fragile.api.constant.GoogleConstant;
import jp.ac.titech.itpro.sds.fragile.utils.CalendarUtils;

import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleV1Dto;

import android.database.Cursor;
import android.util.Log;

public class GoogleCalendarInstance {
	private long beginTime;
	private long endTime;
	private String title;
	private boolean allday;
	private long startDate;
	private String rrule;
	private String rdate;
	private long dtstart;
	private long dtend;
	private String duration;
	private long event_id;
	private long cal_id;
	
	public GoogleCalendarInstance(Cursor arg1) {
		try {
			this.event_id = Long.parseLong(arg1.getString(1));
			this.beginTime = Long.parseLong(arg1.getString(2)); 
			this.endTime = Long.parseLong(arg1.getString(3)); 
			this.title = arg1.getString(4); // title. デバッグ用。後々、タイトルとして使う
			this.allday = "1".equals(arg1.getString(5));
			this.startDate = julianDayToTime(Long.parseLong(arg1.getString(6)));
			this.rrule = arg1.getString(8);
			this.rdate = arg1.getString(9);
			this.dtstart = Long.parseLong(arg1.getString(10));
			if ((arg1.getString(11) != null) && !arg1.getString(11).isEmpty()) {
				this.dtend = Long.parseLong(arg1.getString(11));
			}
			this.duration = arg1.getString(12);
			this.cal_id = Long.parseLong(arg1.getString(13));
			if ((this.rrule!=null) && this.rrule.contains("UNTIL")) {
				Log.d("DEBUG", this.rrule);
			}
 			
		} catch (Exception e) {
			Log.d("DEBUG", "GoogleCalendarInstance constructer fail");
			e.printStackTrace();
		}
	}	
	
	public GoogleCalendarInstance(ScheduleV1Dto schedule) {
		this.dtstart = schedule.getStartTime();
		this.dtend = schedule.getFinishTime();
		this.title = "test";
		this.allday = ((this.dtend - this.dtstart) == CalendarUtils.ONEDAY_INMILLIS);
		this.rrule = null;
		
		String googleId = schedule.getGoogleId();
		if ((googleId != null)
				&& !GoogleConstant.UNTIED_TO_GOOGLE.equals(googleId)) {
			String[] strArray = googleId.split("_");
			this.cal_id = Long.parseLong(strArray[0]);
			this.event_id = Long.parseLong(strArray[1]);
		}
	}
	
	public GoogleCalendarInstance(RepeatScheduleV1Dto schedule) {
		this.dtstart = schedule.getRepeatBegin() + schedule.getStartTime();
		this.dtend = schedule.getRepeatEnd() + schedule.getFinishTime();
		
		this.duration = "P" 
				+ Long.toString((schedule.getFinishTime() - schedule.getStartTime()) / 1000)
				+ "S" ;
		this.title = "testrep";
		this.allday 
			= ((schedule.getFinishTime() - schedule.getStartTime()) 
					== CalendarUtils.ONEDAY_INMILLIS);
		
		Calendar until = Calendar.getInstance();
		until.setTimeInMillis(this.dtend);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		this.rrule = "FREQ=WEEKLY;UNTIL=" + sdf.format(until.getTime()) + ";WKST=MO;BYDAY=";
		this.rrule += getStringDays(schedule);
		
		String googleId = schedule.getGoogleId();
		if ((googleId != null)
				&& !GoogleConstant.UNTIED_TO_GOOGLE.equals(googleId)) {
			String[] strArray = googleId.split("_");
			this.cal_id = Long.parseLong(strArray[0]);
			this.event_id = Long.parseLong(strArray[1]);
		}
	}
	
	private String getStringDays(RepeatScheduleV1Dto schedule) {
		StringBuffer buf = new StringBuffer();
		if (schedule.getRepeatDays().contains(0)) {
			buf.append("SU,");
		} 
		if (schedule.getRepeatDays().contains(1)) {
			buf.append("MO,");
		} 
		if (schedule.getRepeatDays().contains(2)) {
			buf.append("TU,");
		} 
		if (schedule.getRepeatDays().contains(3)) {
			buf.append("WE,");
		} 
		if (schedule.getRepeatDays().contains(4)) {
			buf.append("TH,");
		} 
		if (schedule.getRepeatDays().contains(5)) {
			buf.append("FR,");
		} 
		if (schedule.getRepeatDays().contains(6)) {
			buf.append("SA,");
		} 
		// 最後の,を取り除く
		buf.deleteCharAt(buf.length() - 1);
		return buf.toString();
	}
	
	public long getBeginTime() {
		return this.beginTime;
	}
	public long getEndTime() {
		return this.endTime;
	}
	public boolean getAllday() {
		return this.allday;
	}
	public long getStartDate() {
		return this.startDate;
	}
	public String getRrule() {
		return this.rrule;
	}
	public long getDtstart() {
		return this.dtstart;
	}
	public long getDtend() {
		return this.dtend;
	}
	public long getEventId() {
		return this.event_id;
	}
	public String getTitle() {
		return this.title;
	}
	public String getDuration() {
		return this.duration;
	}
	public long getCalendarId() {
		return this.cal_id;
	}
	public String getGoogleId() {
		return Long.toString(this.cal_id) + "_" + Long.toString(this.event_id);
	}
	
	/**
	 * ユリウス日を1970年1月1日0時UCTからのミリ秒に変換. 日本標準時より9時間ずれている。-9時間すればok
	 * さらにユリウス日は正午に変わる。-12時間する。よって-21時間する。
	 */
	private long julianDayToTime(long startJulianDay) {
		return (long) ((startJulianDay - 2440587.5) * (24.0*60*60*1000) - (21.0*60*60*1000));
	}
}
