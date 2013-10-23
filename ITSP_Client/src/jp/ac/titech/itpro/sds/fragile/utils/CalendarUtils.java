package jp.ac.titech.itpro.sds.fragile.utils;

import java.util.Calendar;

public class CalendarUtils {
	public static int calcDateDiff(Calendar cal1, Calendar cal2) {
		long diff = cal1.getTime().getTime() - cal2.getTime().getTime();
		return (int) diff / 1000 / 60 / 60 / 24;
	}
	
	public static Calendar getBeginOfDate(long time) {
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(time);
		ret.set(Calendar.HOUR_OF_DAY, 0);
		ret.set(Calendar.MINUTE, 0);
		ret.set(Calendar.SECOND, 0);
		ret.set(Calendar.MILLISECOND, 0);
		
		return ret;
	}
	public static Calendar getEndOfDate(long time) {
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(time);
		ret.set(Calendar.HOUR_OF_DAY, 23);
		ret.set(Calendar.MINUTE, 59);
		ret.set(Calendar.SECOND, 59);
		ret.set(Calendar.MILLISECOND, 999);
		
		return ret;
	}
}
