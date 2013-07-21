package jp.ac.titech.itpro.sds.fragile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoggedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logged);
		
		//遷移元からメッセージを受け取っている場合
		Intent intent = getIntent();
		if(intent.getStringExtra(FriendActivity.EXTRA_MESSAGE) != null){
			TextView msg = (TextView)findViewById(R.id.menu_message);
			msg.setText(intent.getStringExtra(FriendActivity.EXTRA_MESSAGE));
		}
		
		//ボタン作成
	    Button friend_btn = (Button)findViewById(R.id.go_to_friend_from_logged);
	    friend_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  //友達登録画面へ遷移
	    		startActivity(new Intent(LoggedActivity.this, FriendActivity.class));
	    	}
	    });
	    Button schedule_btn = (Button)findViewById(R.id.go_to_schedule_from_logged);
	    schedule_btn.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {   //スケジュール表示画面へ遷移
	    		startActivity(new Intent(LoggedActivity.this, ScheduleActivity.class));
	    	}
	    });
	    Button sharetime_btn = (Button)findViewById(R.id.go_to_sharetime_from_logged);
	    sharetime_btn.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {   //共通空き時間表示画面へ遷移
	    		startActivity(new Intent(LoggedActivity.this, ShareTimeActivity.class));
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
