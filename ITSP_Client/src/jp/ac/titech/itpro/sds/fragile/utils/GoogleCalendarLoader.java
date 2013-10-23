package jp.ac.titech.itpro.sds.fragile.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Instances;
import android.content.CursorLoader;
import android.text.format.Time;

public class GoogleCalendarLoader extends CursorLoader {
	public GoogleCalendarLoader(Context context) {
		super(context);
		
	    String tz = Time.TIMEZONE_UTC;
        Time time = new Time(tz);
        time.setToNow();
        time.allDay = true;
        // 去年の1月1日から
        /*
        time.year = time.year - 1;
        time.month = 0;
        time.monthDay = 1;
        time.hour = 0;
        time.minute = 0;
        time.second = 0;
        */
        time.month = 9;
        time.monthDay = 1;
        time.hour = 0;
        time.minute = 0;
        time.second = 0;
        int begin = Time.getJulianDay(time.toMillis(true), 0);
        // 3年後の11月31日までの予定を取得
        time.year += 4;
        time.month = 11;
        time.monthDay = 31;
        int end = Time.getJulianDay(time.toMillis(true), 0);
        Uri content_by_day_uri;
        String[] instance_projection;
        String selection = null;
        String[] selectionArgs = null;
        String sort_order;
 
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            content_by_day_uri =
                CalendarContract.Instances.CONTENT_BY_DAY_URI;
            instance_projection = new String[] {
                Instances._ID,
                Instances.EVENT_ID,
                Instances.BEGIN,
                Instances.END,
                Instances.TITLE,
                Instances.ALL_DAY,
                Instances.START_DAY,
                Instances.END_DAY,
                Instances.RRULE,
                Instances.RDATE,
                Instances.DTSTART,
                Instances.DTEND,
                Instances.DURATION,
                Instances.EVENT_ID
            };
            /*
            // アドレス?カレンダー?を限定する場合は以下を記述する。
            // そうでない場合、端末に登録されたカレンダー全てが使用されるっぽい
            selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
                    + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                    + Calendars.OWNER_ACCOUNT + " = ?))";
            selectionArgs = new String[] {"a@gmail.com", "com.google",
            	"a@gmail.com"};
            */

            
            sort_order =
            	Instances.EVENT_ID + " ASC, " +
                Instances.BEGIN + " ASC, " + Instances.END + " DESC, "
                    + Instances.TITLE + " ASC";
        } else {
            final String authority = "com.android.calendar";
            content_by_day_uri = Uri.parse("content://" + authority
                + "/instances/whenbyday");
            instance_projection = new String[] {
                "_id",
                "event_id",
                "begin",
                "end",
                "title",
                "allDay",
                "startDay",
                "endDay",
                "rrule",
                "rdate",
                "dtstart",
                "dtend",
                "duration",
                "event_id"
            };
            sort_order = "event_id ASC, begin ASC, end DESC, title ASC";
        }
        Uri baseUri = buildQueryUri(begin, end, content_by_day_uri);
        
        this.setUri(baseUri);
        this.setProjection(instance_projection);
        this.setSelection(selection);
        this.setSelectionArgs(selectionArgs);
        this.setSortOrder(sort_order);
	}

	private Uri buildQueryUri(int start, int end, Uri content_by_day_uri) {
        StringBuilder path = new StringBuilder();
        path.append(start);
        path.append('/');
        path.append(end);
        Uri uri =
            Uri.withAppendedPath(content_by_day_uri, path.toString());
        return uri;
    }
}
