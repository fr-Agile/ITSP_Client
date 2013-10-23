package jp.ac.titech.itpro.sds.fragile;

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
	private String dtend;
	private String duration;
	private long event_id;
	
	public GoogleCalendarInstance(Cursor arg1) {
		try {
			this.beginTime = Long.parseLong(arg1.getString(2)); 
			this.endTime = Long.parseLong(arg1.getString(3)); 
			this.title = arg1.getString(4); // title. デバッグ用。後々、タイトルとして使う
			this.allday = "1".equals(arg1.getString(5));
			this.startDate = julianDayToTime(Long.parseLong(arg1.getString(6)));
			this.rrule = arg1.getString(8);
			this.rdate = arg1.getString(9);
			this.dtstart = Long.parseLong(arg1.getString(10));
			this.dtend = arg1.getColumnName(11);
			this.duration = arg1.getString(12);
			this.event_id = Long.parseLong(arg1.getString(13));
		} catch (Exception e) {
			Log.d("DEBUG", "GoogleCalendarInstance constructer fail");
			e.printStackTrace();
		}
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
	public long getEventId() {
		return this.event_id;
	}
	
	/**
	 * ユリウス日を1970年1月1日0時UCTからのミリ秒に変換. 日本標準時より9時間ずれている。-9時間すればok
	 * さらにユリウス日は正午に変わる。-12時間する。よって-21時間する。
	 */
	private long julianDayToTime(long startJulianDay) {
		return (long) ((startJulianDay - 2440587.5) * (24.0*60*60*1000) - (21.0*60*60*1000));
	}
}
