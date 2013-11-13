package jp.ac.titech.itpro.sds.fragile;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;

import com.appspot.fragile_t.getUserEndpoint.GetUserEndpoint;
import com.appspot.fragile_t.getUserEndpoint.GetUserEndpoint.GetUserV1Endpoint.GetUser;
import com.appspot.fragile_t.getUserEndpoint.model.GetUserResultV1Dto;
import com.appspot.fragile_t.pushMessageEndpoint.PushMessageEndpoint;
import com.appspot.fragile_t.pushMessageEndpoint.PushMessageEndpoint.PushMessageV1Endpoint.SendMessageFromRegisterId;
import com.appspot.fragile_t.pushMessageEndpoint.model.PushMessageResultV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.DeleteSchedule;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleResultV1Dto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class TransparentActivity2 extends Activity {
	
	private String email;
	private String address;
	private String startTime;
	private String endTime;
	private String key;
	
	private ScheduleDelTask mAuthTask = null;
	
	private Context context;
	
	private SharedPreferences pref;
	
	private String rs;
	
	private static final String SUCCESS = CommonConstant.SUCCESS;
	private static final String FAIL = CommonConstant.FAIL;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    context = this;
	    pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
	    email = getIntent().getStringExtra("email");
	    address = getIntent().getStringExtra("address");
	    startTime = getIntent().getStringExtra("startTime");
	    endTime = getIntent().getStringExtra("endTime");
	    key = getIntent().getStringExtra("key");
	    	    
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(address+"さんがあなたを"+startTime+"〜"+endTime+"の予定に招待しています。参加しますか？")
	    		.setPositiveButton("参加する", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    			  dialog.cancel();
	    			  TransparentActivity2.this.finish();	    			  
	    			}
	    		})
	           .setNegativeButton("参加しない", new DialogInterface.OnClickListener() {
	              public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();                
	                
	                mAuthTask = new ScheduleDelTask();
	    			mAuthTask.execute((Void) null);
	              }
	            });
	    AlertDialog alert = builder.create();
	    alert.show();
	}
	
	
	
	public class ScheduleDelTask extends AsyncTask<Void, Void, Void> {
		
		ProgressDialog dialog;
		
		@Override
		protected Void doInBackground(Void... args) {

			try {
				ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
				DeleteSchedule delschedule = endpoint.scheduleV1EndPoint().deleteSchedule(key);
				Log.d("DEBUG", "key="+key);	

				ScheduleResultV1Dto result = delschedule.execute();
				rs = result.getResult();
				
				if (rs.equals(SUCCESS)) {
					try{
						GetUserEndpoint endpoint2 = RemoteApi.getGetUserEndpoint();
						GetUser getuser = endpoint2.getUserV1Endpoint().getUser(pref.getString("email", ""));
						GetUserResultV1Dto result2 = getuser.execute();
						
						PushMessageEndpoint endpoint3 = RemoteApi.getPushMessageEndpoint();
						SendMessageFromRegisterId pushmsg = endpoint3.pushMessageV1Endpoint().sendMessageFromRegisterId("noJoin", 
																					result2.getUser().getFirstName()+result2.getUser().getLastName(),
																					email);
						PushMessageResultV1Dto result3 = pushmsg.execute();
						Log.d("DEBUG", "プッシュリザルト："+result3.getResult());
					} catch (Exception e) {
						Log.d("DEBUG", "プッシュ通知の配信に失敗しました");
						Log.d("DEBUG", e.toString());
					}
				}
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

			if (rs.equals(SUCCESS)) {
				Log.d("DEBUG", "削除成功");
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
			    builder.setTitle("参加拒否しました")
			           .setNegativeButton("OK", new DialogInterface.OnClickListener() {
			              public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();                
			                TransparentActivity2.this.finish();
			              }
			            });
			    AlertDialog alert = builder.create();
			    alert.show();
				
			} else {
				Log.d("DEBUG", "削除失敗："+rs.toString());
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
			    builder.setTitle("通信エラー")
			    		.setPositiveButton("再接続", new DialogInterface.OnClickListener() {
			    			public void onClick(DialogInterface dialog, int whichButton) {
			    			  dialog.cancel();
			    			  mAuthTask = new ScheduleDelTask();
				    		  mAuthTask.execute((Void) null);    			  
			    			}
			    		})
			           .setNegativeButton("やめる", new DialogInterface.OnClickListener() {
			              public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();                
			                TransparentActivity2.this.finish();	
			              }
			            });
			    AlertDialog alert = builder.create();
			    alert.show();
				
			}
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
			TransparentActivity2.this.finish();
		}
	}
	
	
}


