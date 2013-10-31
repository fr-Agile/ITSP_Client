package jp.ac.titech.itpro.sds.fragile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class TransparentActivity extends Activity {
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    String msg = getIntent().getStringExtra("msg");
	    
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(msg)
	    		.setPositiveButton("登録する", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    			  dialog.cancel();
	    			  TransparentActivity.this.finish();
	    			}
	    		})
	           .setNegativeButton("登録しない", new DialogInterface.OnClickListener() {
	              public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();                
	                TransparentActivity.this.finish();
	              }
	            });
	    AlertDialog alert = builder.create();
	    alert.show();
	}
}
