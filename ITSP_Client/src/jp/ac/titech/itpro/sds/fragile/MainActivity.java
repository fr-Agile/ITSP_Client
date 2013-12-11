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

public class MainActivity extends Activity {
	private static SharedPreferences mPref;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mPref = getSharedPreferences("user", Activity.MODE_PRIVATE);
		
		
		// googleアカウントをチェックして、可能ならばインポートする
		GoogleAccountCheckAndImportTask gaciTask = new GoogleAccountCheckAndImportTask(this);
		gaciTask.run();
		
		
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
}
