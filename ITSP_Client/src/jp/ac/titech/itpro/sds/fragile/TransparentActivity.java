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

public class TransparentActivity extends Activity {
	
	private String fEmail;
	private SharedPreferences pref;
	
	private FriendTask mAuthTask = null;
	
	private Context context;
	
	private String rs;
	
	private static final String SUCCESS = CommonConstant.SUCCESS;
	private static final String FAIL = CommonConstant.FAIL;

	private static final String NULLMY = FriendConstant.NULLMY;
	private static final String NOFRIEND = FriendConstant.NOFRIEND;
	private static final String ALREADY = FriendConstant.ALREADY;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
	    context = this;
	    fEmail = getIntent().getStringExtra("msg");
	    
	    	    
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(fEmail+"があなたを友人登録しました")
	    		.setPositiveButton("登録する", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    			  dialog.cancel();
	    			  
	    			  mAuthTask = new FriendTask();
	    			  mAuthTask.execute((Void) null);
	    			  
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
	
	
	
	public class FriendTask extends AsyncTask<Void, Void, Void> {
		
		ProgressDialog dialog;
		
		@Override
		protected Void doInBackground(Void... args) {

			try {
				FriendEndpoint endpoint = RemoteApi.getFriendEndpoint();
				Friendship friend = endpoint.friendV1Endpoint().friendship(
						fEmail, pref.getString("email", ""));

				FriendResultV1Dto result = friend.execute();
				rs = result.getResult();

			} catch (Exception e) {
				Log.d("DEBUG", e.toString());
				rs = FAIL;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mAuthTask = null;
			if(dialog != null){
	    	      dialog.dismiss();
	    	}
			String msg;

			if (rs.equals(SUCCESS)) {
				Log.d("DEBUG", "登録成功");
				msg = "登録成功しました";
			}else if(rs.equals(NULLMY)){
				Log.d("DEBUG", "登録失敗");
				msg = "ログインしてください";
			}else if(rs.equals(NOFRIEND)){
				Log.d("DEBUG", "登録失敗");
				msg = "友人が見つかりませんでした";
			}else if(rs.equals(ALREADY)){
				Log.d("DEBUG", "登録失敗");
				msg = "すでに登録済みです";
			} else {
				Log.d("DEBUG", "登録失敗");
				msg = "通信エラー";
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
		    builder.setTitle(msg)
		           .setNegativeButton("OK", new DialogInterface.OnClickListener() {
		              public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();                
		                TransparentActivity.this.finish();
		              }
		            });
		    AlertDialog alert = builder.create();
		    alert.show();
		}
		
		@Override
	    protected void onPreExecute() {
	    	dialog = new ProgressDialog(context);
	    	dialog.setTitle("Please wait");
	    	dialog.setMessage("Conecting...");
	    	dialog.show();
	    } 

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			if(dialog != null){
	    	      dialog.dismiss();
	    	}
			TransparentActivity.this.finish();
		}
	}
	
	
}


