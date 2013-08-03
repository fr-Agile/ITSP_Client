package jp.ac.titech.itpro.sds.fragile.utils;

import java.util.Calendar;

public class CalendarUtils {
	public static int calcDateDiff(Calendar cal1, Calendar cal2) {
		long diff = cal1.getTime().getTime() - cal2.getTime().getTime();
		return (int) diff / 1000 / 60 / 60 / 24;
	}
}
