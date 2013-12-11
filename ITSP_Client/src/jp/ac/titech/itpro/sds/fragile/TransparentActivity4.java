package jp.ac.titech.itpro.sds.fragile;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.api.constant.FriendConstant;

import com.appspot.fragile_t.friendEndpoint.FriendEndpoint;
import com.appspot.fragile_t.friendEndpoint.FriendEndpoint.FriendV1Endpoint.Friendship;
import com.appspot.fragile_t.friendEndpoint.model.FriendResultV1Dto;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class TransparentActivity4 extends Activity {
	
	private String msg;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	   
	    msg = getIntent().getStringExtra("msg");
	    	    
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(msg+"との友人登録が解除されました")
	    		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    			  dialog.cancel();
	    			  TransparentActivity4.this.finish();
	    			}
	    		});
	    AlertDialog alert = builder.create();
	    alert.show();
	}
}


