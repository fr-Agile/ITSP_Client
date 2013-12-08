package jp.ac.titech.itpro.sds.fragile;

import java.util.Calendar;
import java.util.List;

import com.appspot.fragile_t.getUserEndpoint.model.GetUserResultV1Dto;
import com.appspot.fragile_t.getUserEndpoint.model.UserV1Dto;

import jp.ac.titech.itpro.sds.fragile.CalendarSaver.GoogleCalendarSaveFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetUserTask.GetUserFinishListener;
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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity implements 
	GoogleCalendarSaveFinishListener, GoogleCalendarCheckFinishListener, 
	GoogleAccountCheckFinishListener, GetUserFinishListener {
	private static SharedPreferences mPref;
	
	private GetUserTask mGetUserTask = null;
	private UserV1Dto mUser;
	private final static String SUCCESS = CommonConstant.SUCCESS;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mPref = getSharedPreferences("user", Activity.MODE_PRIVATE);
		
		
		// googleカレンダーをインポートするかどうか判断するため、user情報を取得する
		if (mGetUserTask == null) {
			String email = mPref.getString("email", "");
			Log.d("DEBUG", "start importing google event");
			mGetUserTask = new GetUserTask(this);
			mGetUserTask.setEmail(email);
			mGetUserTask.execute();
		}
		
		
		String nonstr = "";
		if (!nonstr.equals(mPref.getString("email", ""))) {
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
	
	@Override
	public void onGetUserFinish(GetUserResultV1Dto result) {
		mGetUserTask = null;
		if ((result != null) && SUCCESS.equals(result.getResult())) {
			Log.d("DEBUG", "get user success");
			mUser = result.getUser();
			
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
}
