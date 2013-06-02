package jp.ac.titech.itpro.sds.fragile;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;



public class LoggedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logged);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
}
