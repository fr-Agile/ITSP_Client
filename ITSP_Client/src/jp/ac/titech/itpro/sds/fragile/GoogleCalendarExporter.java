package jp.ac.titech.itpro.sds.fragile;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.api.constant.GoogleConstant;

import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.GetScheduleByKeyS;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleV1Dto;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract.Events;
import android.util.Log;

public class GoogleCalendarExporter {
	private GoogleCalendarExportFinishListener listener = null;
	private GoogleCalendarInstance gci = null;
	private String keyS = null;
	private Activity activity = null;
	private static final String FAIL = CommonConstant.FAIL;
	private static final int CALENDAR_ID = 1;
	
	public GoogleCalendarExporter(
			GoogleCalendarExportFinishListener listener, Activity activity) {
		this.listener = listener;
		this.activity = activity;
	}
	
	/**
	 * GoogleCalendarExportTaskの終了通知用リスナー
	 */
	public interface GoogleCalendarExportFinishListener {
		public void onGoogleCalendarExportFinish(String googleId);
	}
	
	public void insert(ScheduleV1Dto schedule) {
		gci = new GoogleCalendarInstance(schedule);
		new GoogleExportTask().execute();
	}
	
	public void insert(RepeatScheduleV1Dto schedule) {
		gci = new GoogleCalendarInstance(schedule);
		new GoogleExportTask().execute();
	}
	
	public void delete(ScheduleV1Dto schedule) {
		gci = new GoogleCalendarInstance(schedule);
		new GoogleDeleteTask().execute();
	}
	
	public void edit(String keyS, ScheduleV1Dto newSchedule) {
		this.keyS = keyS;
		gci = new GoogleCalendarInstance(newSchedule);
		new GoogleEditTask().execute();
	}
	
	private class GoogleExportTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... args) {
			try {
				ContentResolver cr = activity.getContentResolver();
				ContentValues values = new ContentValues();
				values.put(Events.DTSTART, gci.getDtstart());
				values.put(Events.TITLE, gci.getTitle());
				values.put(Events.RRULE, gci.getRrule());
				values.put(Events.CALENDAR_ID, CALENDAR_ID);
				values.put(Events.EVENT_TIMEZONE, "Asia/Tokyo");
				
				// dtendかdurationを登録
				if (gci.getDuration() == null) {
					values.put(Events.DTEND, gci.getDtend());
				} else {
					values.put(Events.DURATION, gci.getDuration());
				}
				Uri uri = cr.insert(Events.CONTENT_URI, values);
				
				GoogleCalendarInstance g = gci;
				
				// get the event ID that is the last element in the Uri
				String eventId = uri.getLastPathSegment();
				String googleId = CALENDAR_ID + "_" + eventId;
				
				Log.d("DEBUG", "export: " + googleId);
				return googleId;
			} catch (Exception e) {
				Log.d("DEBUG", "export fail");
				e.printStackTrace();
				return FAIL;
			}
		}
		
		@Override
		protected void onPostExecute(String googleId) {
			if (listener != null) {
				listener.onGoogleCalendarExportFinish(googleId);
				listener = null;
			}
		}
	}
	
	private class GoogleDeleteTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... args) {
			try {
				if ((gci != null) && 
						GoogleConstant.UNTIED_TO_GOOGLE.equals(gci.getGoogleId())) {
					//　スケジュールがgoogleと紐づけられていたら消去する
					ContentResolver cr = activity.getContentResolver();
					Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, gci.getEventId());
					cr.delete(deleteUri, null, null);
					
					Log.d("DEBUG", "delete: " + gci.getGoogleId());
					return gci.getGoogleId();
				} else {
					Log.d("DEBUG", "google delete fail");
					return FAIL;
				}
			} catch (Exception e) {
				Log.d("DEBUG", "google delete fail");
				e.printStackTrace();
				return FAIL;
			}
		}
	
		@Override
		protected void onPostExecute(String googleId) {
			if (listener != null) {
				listener.onGoogleCalendarExportFinish(googleId);
				listener = null;
			}
		}
	}
	
	private class GoogleEditTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... args) {
			try {
				
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				GetScheduleByKeyS getScheduleByKeyS = endpoint.scheduleV1EndPoint()
						.getScheduleByKeyS(keyS);
				ScheduleV1Dto schedule = getScheduleByKeyS.execute();
				
				if ((schedule != null) && 
						!GoogleConstant.UNTIED_TO_GOOGLE.equals(schedule.getGoogleId())) {
					//　スケジュールがgoogleと紐づけられていたら消去する
					ContentResolver cr = activity.getContentResolver();
					
					ContentValues values = new ContentValues();
					values.put(Events.DTSTART, gci.getDtstart());
					values.put(Events.TITLE, gci.getTitle());
					values.put(Events.RRULE, gci.getRrule());
					values.put(Events.EVENT_TIMEZONE, "Asia/Tokyo");
					
					// dtendかdurationを登録
					if (gci.getDuration() == null) {
						values.put(Events.DTEND, gci.getDtend());
					} else {
						values.put(Events.DURATION, gci.getDuration());
					}
					GoogleCalendarInstance origin = new GoogleCalendarInstance(schedule);
					Uri editUri = ContentUris.withAppendedId(Events.CONTENT_URI, origin.getEventId());
					int row = cr.update(editUri, values, null, null);
					
					Log.d("DEBUG", "edit: " + schedule.getGoogleId());
					Log.d("DEBUG", "edit: " + row);
					return schedule.getGoogleId();
				} else {
					Log.d("DEBUG", "edit fail");
					return FAIL;
				}
			} catch (Exception e) {
				Log.d("DEBUG", "edit fail");
				e.printStackTrace();
				return FAIL;
			}
		}
		
		@Override
		protected void onPostExecute(String googleId) {
			if (listener != null) {
				listener.onGoogleCalendarExportFinish(googleId);
				listener = null;
			}
		}
	}
}
