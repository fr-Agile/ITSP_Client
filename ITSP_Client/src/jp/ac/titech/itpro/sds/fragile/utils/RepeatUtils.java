package jp.ac.titech.itpro.sds.fragile.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RepeatUtils {
	public static long getDefaultRepeatEnd(long repeatBegin) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(repeatBegin);
		cal.add(Calendar.YEAR, 5);		// 5年後まで
		return cal.getTimeInMillis();
	}
	
	public static long getTrueRepeatBegin(long repeatBegin, List<Integer> repeats) {
		Calendar begin = Calendar.getInstance();
		begin.setTimeInMillis(repeatBegin);
		int dayOfBegin = begin.get(Calendar.DAY_OF_WEEK) - begin.getFirstDayOfWeek();	//repetBeginの曜日を取得(0==sunday)
		int minDiff = 7;
		// repeatBeginから一番近い繰り返し日までのずれを計算する
		for (int day : repeats) {
			int diff = day - dayOfBegin;	// 曜日のずれを計算
			if (diff < 0) {
				diff += 7;	// 日曜をまたぐ場合は負になってしまうので+7する
			}
			if (diff < minDiff) {
				minDiff = diff;
			}
		}
		begin.add(Calendar.DATE, minDiff);
		begin.set(Calendar.HOUR_OF_DAY, 0);
		begin.set(Calendar.MINUTE, 0);
		begin.set(Calendar.SECOND, 0);
		begin.set(Calendar.MILLISECOND, 0);
		return begin.getTimeInMillis();
	}
}
