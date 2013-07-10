package jp.ac.titech.itpro.sds.fragile;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;


public class LoggedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logged);
		
		//ボタン作成
	    Button friend_btn = (Button)findViewById(R.id.go_to_friend_from_logged);
	    friend_btn.setOnClickListener(new View.OnClickListener() {
			@Override
	    	public void onClick(View v) {  //友達登録画面へ遷移
	    		startActivity(new Intent(LoggedActivity.this, FriendActivity.class));
	    	}
	    });
	    Button schedule_btn = (Button)findViewById(R.id.go_to_inputschedule_from_logged);
	    schedule_btn.setOnClickListener(new View.OnClickListener() {
	    	@Override
	    	public void onClick(View v) {   //スケジュール登録画面へ遷移
	    		startActivity(new Intent(LoggedActivity.this, ScheduleInputActivity.class));
	    	}
	    });
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//戻るボタンの動作変更
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
	    	startActivity(new Intent(LoggedActivity.this, LoginActivity.class));
	    	return true;
		}
	    return false;
	}
}