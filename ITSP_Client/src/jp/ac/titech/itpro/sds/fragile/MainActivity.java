package jp.ac.titech.itpro.sds.fragile;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {
	private static SharedPreferences pref;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

		pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
		
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
    
}
