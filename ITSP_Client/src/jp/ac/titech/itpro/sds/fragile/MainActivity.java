package jp.ac.titech.itpro.sds.fragile;

import java.util.Calendar;
import java.util.List;

import com.appspot.fragile_t.getUserEndpoint.GetUserEndpoint;
import com.appspot.fragile_t.getUserEndpoint.GetUserEndpoint.GetUserV1Endpoint.GetUser;
import com.appspot.fragile_t.getUserEndpoint.model.GetUserResultV1Dto;
import com.appspot.fragile_t.getUserEndpoint.model.UserV1Dto;

import jp.ac.titech.itpro.sds.fragile.CalendarSaver.GoogleCalendarSaveFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.api.constant.GoogleConstant;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleAccountChecker;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleAccountChecker.GoogleAccountCheckFinishListener;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleCalendarChecker;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleCalendarChecker.GoogleCalendarCheckFinishListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity implements 
	GoogleCalendarSaveFinishListener, GoogleCalendarCheckFinishListener, 
	GoogleAccountCheckFinishListener {
	private static SharedPreferences pref;
	
	private GetUserTask mGetUserTask = null;
	private UserV1Dto mUser;
	private final static String SUCCESS = CommonConstant.SUCCESS;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
		
		
		// googleカレンダーをインポートするかどうか判断するため、user情報を取得する
		if (mGetUserTask == null) {
			Log.d("DEBUG", "start importing google event");
			mGetUserTask = new GetUserTask();
			mGetUserTask.execute();
		}
		
		
		
		if (!pref.getString("email", "").equals("")) {
			// 2回目以降の起動時（メールアドレスが保存されているとき）
			// スケジュール画面へ遷移
			Intent intent = new Intent(this, ScheduleActivity.class);
			Calendar nowCal = Calendar.getInstance();
			// nowCal.add(Calendar.DAY_OF_YEAR, 7);
			StoreData data = new StoreData(nowCal);
			intent.putExtra("StoreData", data);
			intent.setAction(Intent.ACTION_VIEW);
			startActivity(intent);
		} else {
			// 1回目の起動時（メールアドレスが保存されていないとき）
	        Intent intent = new Intent(this, LoginActivity.class);
	        startActivity(intent);
		}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	/**
	 * GoogleCalendarのインポート処理
	 */

	@Override
	public void onGoogleAccountCheckFinish(boolean result,
			List<String> accountList, List<String> displayList, List<Long> idList) {
		if (result) {
			if (accountList != null && accountList.size() > 0) {
				// アカウントが登録されているのでカレンダーを読み込む
				GoogleCalendarChecker gcc = 
						new GoogleCalendarChecker(this, this);
				gcc.run();

			} else {
				// アカウントが登録されていない場合、ダイアログを表示して登録を促す
				new GoogleAccountRegistDialogBuilder(MainActivity.this)
					.setDefault()
					.show();
			}
			Log.d("DEBUG", "checking google account success");
		} else {
			Log.d("DEBUG", "checking google account fail");
		}

	}

	@Override
	public void onGoogleCalendarCheckFinish(boolean result, Cursor arg1) {
		if (result) {
			// カレンダーがインポートできたのデータベースを書き換える
			CalendarSaver cs = new CalendarSaver(this, this);
			cs.save(arg1);
		}
	}

	@Override
	public void onGoogleCalendarSaveFinish(boolean result) {
		if (result) {
			// 予定を書き換え終わったので
			Log.d("DEBUG", "create schedule list success");
		} else {
			Log.d("DEBUG", "create schedule list failed");
		}
	}
	/**
	 * GoogleCalendarのインポート処理ここまで
	 */
	
	public class GetUserTask extends AsyncTask<Void, Void, GetUserResultV1Dto> {
		@Override
		protected GetUserResultV1Dto doInBackground(Void... args) {
			try {
				SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
				String email = pref.getString("email","");	
				
				GetUserEndpoint endpoint = RemoteApi.getGetUserEndpoint();
				GetUser getUser = endpoint.getUserV1Endpoint().getUser(email);
				GetUserResultV1Dto result = getUser.execute();
				return result;
			} catch (Exception e) {
				Log.d("DEBUG", "get user fail: " + e);
				return null;
			}
		}
	
		@Override
		protected void onPostExecute(GetUserResultV1Dto result) {
			mGetUserTask = null;
			if ((result != null) && SUCCESS.equals(result.getResult())) {
				mUser = result.getUser();
				Log.d("DEBUG", "get user success");
				
				if (mUser.getGoogleAccount().equals(GoogleConstant.UNTIED_TO_GOOGLE)) {
					// google登録されていないのでインポートしない
				} else {
					// インポートする
					// GoogleAccountCheckerを起動
					GoogleAccountChecker gac = new GoogleAccountChecker(
							MainActivity.this, MainActivity.this);
					gac.run();
				}
				
			} else {
				mUser = null;
				Log.d("DEBUG", "get user fail");
			}
		}

		@Override
		protected void onCancelled() {
			mGetUserTask = null;
		}
	}
}
