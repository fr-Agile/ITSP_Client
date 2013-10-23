package jp.ac.titech.itpro.sds.fragile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.google.api.client.util.DateTime;

public class Rrule {
	private static final int FOREVER_REPEAT_NUM = 100;
	private List<Integer> daysList;
	private int count = 0;
	private DateTime until = null;
	private boolean isWeekly;
	
	public Rrule(String rrule) {
		if (validateWeeklyRepeat(rrule)) {
			for (String r : rrule.split(";")) {
				if (r.contains("BYDAY")) {
					this.daysList = parseBYDAY(r);
				} else if (r.contains("COUNT")) {
					this.count = Integer.parseInt(r.split("=")[1]);
				} else if (r.contains("UNTIL")) {
					this.until = parseUntil(r);
				}
			}
			this.isWeekly = true;
			
			if (this.count == 0 && this.until == null) {
				this.count = FOREVER_REPEAT_NUM;	// 繰り返し回数が設定されていない場合は適当な値に設定する
			}
		} else {
			this.daysList = null;
			this.count = 0;
			this.isWeekly = false;
			Log.d("DEBUG", "Not Weekly Schedule");
		}
	}
	
	public boolean isWeekly() {
		return this.isWeekly;
	}
	
	public List<Integer> getRepeatDays() {
		return this.daysList;
	}
	
	/**
	 * 繰り返しスケジュール日のリストを返す(例外日は除かない)
	 */
	public List<DateTime> getRepeatDateList(long dtstart) {
		List<DateTime> repeatDateList = new ArrayList<DateTime>();
		if (this.isWeekly) {
			// 繰り返しスケジュールの初日を求める
			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(dtstart);
			date.set(Calendar.HOUR_OF_DAY, 0);
			date.set(Calendar.MINUTE, 0);
			date.set(Calendar.SECOND, 0);
			date.set(Calendar.MILLISECOND, 0);
			
			int day = date.get(Calendar.DAY_OF_WEEK) - date.getFirstDayOfWeek();	// 初日の曜日を求める
			int index = daysList.indexOf(day);
			
			int repeatNum = 0;
			while (!isRepeatFinish(repeatNum, new DateTime(date.getTime()))) {
				repeatDateList.add(new DateTime(date.getTime()));
				
				// 曜日を順番にずらす。同時にdateもずらした日にち分ずらす
				index++;
				int newDay = this.daysList.get(index % daysList.size());
				int diff = newDay - day;
				if (diff <= 0) {
					diff += 7;
				}
				repeatNum++;
				date.add(Calendar.DATE, diff);
				day = newDay;
			}
		}
		return repeatDateList;
	}
	
	private boolean isRepeatFinish(int repeatNum, DateTime nowDate) {
		if (count > 0) {
			return !(repeatNum < count);
		} else if (until != null) {
			return !(nowDate.getValue() <= until.getValue());
		} else {
			return true;
		}
	}
	
	/**
	 * rruleが週繰り返しの予定であることを確かめる
	 */
	private boolean validateWeeklyRepeat(String rrule) {
		if (rrule == null) {
			return false;
		}
		if (!rrule.contains("WEEKLY")) {
			return false;
		}
		return true;
	}
	
	/**
	 * BYDAYパラメータから曜日を取り出す
	 */
	private List<Integer> parseBYDAY(String r) {
		List<Integer> daysList = new ArrayList<Integer>();
		String days = r.split("=")[1];
		for (String day : days.split(",")) {
			int iDay = getIntDayFromString(day);
			if (iDay >= 0) {
				daysList.add(iDay);
			}
		}
		return daysList;
	}
	
	private DateTime parseUntil(String r) {
		String strDate = r.split("=")[1];
		if (strDate.length() < 8) {
			return null;
		}
		int year = Integer.parseInt(strDate.substring(0, 4));
		int month = Integer.parseInt(strDate.substring(4, 6));
		int day = Integer.parseInt(strDate.substring(6, 8));
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);	// monthは0から始まる
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return new DateTime(cal.getTime());
	}
	
	/**
	 * Stringの曜日をintに直す
	 */
	private int getIntDayFromString(String day) {
		int iDay;
		if (day.equals("SU")) {
			iDay = 0;
		} else if (day.equals("MO")) {
			iDay = 1;
		} else if (day.equals("TU")) {
			iDay = 2;
		} else if (day.equals("WE")) {
			iDay = 3;
		} else if (day.equals("TH")) {
			iDay = 4;
		} else if (day.equals("FR")) {
			iDay = 5;
		} else if (day.equals("SA")) {
			iDay = 6;
		} else {
			iDay = -1;
		}	
		return iDay;
	}
}
