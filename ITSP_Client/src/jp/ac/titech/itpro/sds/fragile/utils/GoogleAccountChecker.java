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
import android.util.Log;

public class GoogleAccountChecker implements LoaderCallbacks<Cursor> {
	private GoogleAccountCheckFinishListener listener;
	private Activity activity;
	private List<String> accountList;
	private List<String> displayList;
	private List<Long> idList;
	private boolean onlyWritable;
	
	public GoogleAccountChecker(Activity activity, GoogleAccountCheckFinishListener listener)	{
		this.activity = activity;
		this.listener = listener;
	}
	
	public void run() {
		this.run(false);
	}
	
	public void run(boolean onlyWritable) {
		this.onlyWritable = onlyWritable;
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
		displayList = new ArrayList<String>();
		idList = new ArrayList<Long>();
		if (arg1.moveToFirst()) {
			do {
				long calID = 0;
			    String displayName = null;
			    String accountName = null;        
			    String accessLevel = null;
			    
			    // Get the field values
			    calID = arg1.getLong(0);
			    accountName = arg1.getString(1);
			    displayName = arg1.getString(2);
			    accessLevel = arg1.getString(3);
			    
			    
			    if ((displayName != null) && (accountName != null)) {
			    	if (!onlyWritable || accessLevel.equals("700")) {	// 権限を気にする場合は、700しか許さない
				    	accountList.add(accountName);
				    	displayList.add(displayName);
				    	idList.add(calID);
			    	}
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
			listener.onGoogleAccountCheckFinish(result, accountList, displayList, idList);
			listener = null;
		}
	}
		
	/**
	 * google account check 終了通知
	 */
	public interface GoogleAccountCheckFinishListener {
		public void onGoogleAccountCheckFinish(boolean result, 
				List<String> accountList, List<String> displayList, List<Long> idList);
	}
	
	private static class GoogleAccountLoader extends CursorLoader {
	
		public GoogleAccountLoader(Context context) {
			super(context);
			
			Uri uri = Calendars.CONTENT_URI;
			String[] event_projection = new String[] {
			    Calendars._ID,                          // 0
			    Calendars.ACCOUNT_NAME,                 // 1
			    Calendars.CALENDAR_DISPLAY_NAME,        // 2
				Calendars.CALENDAR_ACCESS_LEVEL			// 3
			    };
			// googleのアカウントのみ検索
			String selection = "(" + Calendars.ACCOUNT_TYPE + " = ?)";
			String[] selectionArgs = new String[] {"com.google"};
			//Submit the query and get a Cursor object back. 
	        this.setUri(uri);
	        this.setProjection(event_projection);
	        this.setSelection(selection);
	        this.setSelectionArgs(selectionArgs);
		}
	}
	
}
