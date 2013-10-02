package jp.ac.titech.itpro.sds.fragile;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;



public class RegisteredActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registered);
		
		//ボタン作成
	    Button login_btn = (Button)findViewById(R.id.go_to_login_from_registered);
	    login_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  //ログイン画面へ遷移
	    		startActivity(new Intent(RegisteredActivity.this, LoginActivity.class));
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
	    	startActivity(new Intent(RegisteredActivity.this, RegisterActivity.class));
	    	this.onDestroy();
	    	return true;
		}
	    return false;
	}
}
