package jp.ac.titech.itpro.sds.fragile.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;

public class GoogleAccountChecker implements LoaderCallbacks<Cursor> {
	private GoogleAccountCheckFinishListener listener;
	private Activity activity;
	private List<String> accountList;
	
	public GoogleAccountChecker(Activity activity, GoogleAccountCheckFinishListener listener)	{
		this.activity = activity;
		this.listener = listener;
	}
	
	public void run() {
		activity.getLoaderManager().initLoader(1, null, this);
	}
	
	/**
	 * GoogleCalendarのアカウントいんぽーと
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new GoogleAccountLoader(activity);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		accountList = new ArrayList<String>();
		if (arg1.moveToFirst()) {
			do {
				long calID = 0;
			    String displayName = null;
			    String accountName = null;        
			      
			    // Get the field values
			    calID = arg1.getLong(0);
			    displayName = arg1.getString(1);
			    accountName = arg1.getString(2);
			    
			    if ((displayName != null) && (accountName != null)) {
			    	accountList.add(accountName);
			    }
			} while (arg1.moveToNext());
		}
		finish(true);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// ロードキャンセル
		finish(false);
	}
	
	private void finish(boolean result) {
		if (listener != null) {
			listener.onGoogleAccountCheckFinish(result, accountList);
			listener = null;
		}
	}
		
	/**
	 * google account check 終了通知
	 */
	public interface GoogleAccountCheckFinishListener {
		public void onGoogleAccountCheckFinish(boolean result, List<String> accountList);
	}
	
	private static class GoogleAccountLoader extends CursorLoader {
	
		public GoogleAccountLoader(Context context) {
			super(context);
			
			Uri uri = Calendars.CONTENT_URI;
			String[] event_projection = new String[] {
			    Calendars._ID,                           // 0
			    Calendars.ACCOUNT_NAME,                  // 1
			    Calendars.CALENDAR_DISPLAY_NAME          // 2
			    };
			String selection = null;
			String[] selectionArgs = null;
			//Submit the query and get a Cursor object back. 
	        this.setUri(uri);
	        this.setProjection(event_projection);
	        this.setSelection(selection);
	        this.setSelectionArgs(selectionArgs);
		}
	}
	
}
