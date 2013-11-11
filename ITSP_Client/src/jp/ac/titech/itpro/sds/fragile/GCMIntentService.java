package jp.ac.titech.itpro.sds.fragile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.utils.CommonUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.appspot.fragile_t.registrationIdEndpoint.RegistrationIdEndpoint;
import com.appspot.fragile_t.registrationIdEndpoint.RegistrationIdEndpoint.RegistrationIdV1Endpoint.RegisterId;
import com.appspot.fragile_t.registrationIdEndpoint.model.RegisterIdResultV1Dto;
import com.google.android.gcm.GCMBaseIntentService;



/**
* プッシュ通知の受信に関する処理を行うクラス
*/
public class GCMIntentService extends GCMBaseIntentService {
	
	private SharedPreferences pref;

	
    /** コンストラクタ */
    public GCMIntentService() {
        super(CommonUtils.GCM_SENDER_ID);
    }
 
    /** RegistrationID が登録された場合に呼び出されるメソッド */
    @Override
    public void onRegistered(Context context, String registrationId) {
        // [RegistrationID]を本体に保存する処理などを記述
    	Log.d("DEBUG", "IDが登録されようとしています");
    	
    	try {
    		pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
    		String mEmail = pref.getString("email", "");
    		RegistrationIdEndpoint endpoint = RemoteApi.getRegistrationIdEndpoint();
    		RegisterId registerId = endpoint.registrationIdV1Endpoint().registerId(registrationId, mEmail);
			RegisterIdResultV1Dto rs = registerId.execute();
			Log.d("DEBUG", rs.toString());
			
		} catch (IOException e) {

			e.printStackTrace();
		}
    }
 
    /** RegistrationID が登録解除された場合に呼び出されるメソッド */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        // [RegistrationID]を本体から削除する処理などを記述
    	Log.d("DEBUG", "登録解除されました");
    }
 
    /** エラーが発生した場合に呼び出されるメソッド */
    @Override
    public void onError(Context context, String errorId) {
        // エラー処理などを記述
    }
 
    /** メッセージを受信した場合に呼び出されるメソッド */
    @Override
    protected void onMessage(Context context, Intent intent) {
    	
    	// アプリサーバから送信されたPushメッセージの受信。
        // Message.data が Intent.extra になるらしい。
        CharSequence value = intent.getCharSequenceExtra("value");
        Log.d("DEBUG", "onMessage: value = " + value);
        
        if(value.equals("addFriend")){
        	CharSequence msg = intent.getCharSequenceExtra("msg");
        	Log.d("DEBUG", "onMessage: msg = " + msg);
        	
        	Intent in = new Intent(getApplicationContext(),TransparentActivity.class);
        	in.putExtra("msg", msg);
        	in.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        	startActivity(in);
        } else if(value.equals("groupSchedule")){
        	CharSequence email = intent.getCharSequenceExtra("email");
        	CharSequence address = intent.getCharSequenceExtra("address");
        	CharSequence start = intent.getCharSequenceExtra("start");
        	Date startTime = new Date(Long.parseLong(start.toString()));
        	CharSequence end = intent.getCharSequenceExtra("end");
        	Date endTime = new Date(Long.parseLong(end.toString()));
        	CharSequence key = intent.getCharSequenceExtra("key");
        	Log.d("DEBUG", "onMessage: address = " + email);
        	Log.d("DEBUG", "onMessage: address = " + address);
        	Log.d("DEBUG", "onMessage: start = " + startTime.toString());
        	Log.d("DEBUG", "onMessage: end = " +  endTime.toString());
        	Log.d("DEBUG", "onMessage: key = " + key);
        	
        	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
        	
        	Intent in = new Intent(getApplicationContext(),TransparentActivity2.class);
        	in.putExtra("email", email);
        	in.putExtra("address", address);
        	in.putExtra("startTime", sdf.format(startTime));
        	in.putExtra("endTime", sdf.format(endTime));
        	in.putExtra("key", key);
        	
        	in.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        	startActivity(in);
        } else if(value.equals("noJoin")) {
        	CharSequence msg = intent.getCharSequenceExtra("msg");
        	Log.d("DEBUG", "onMessage: msg = " + msg);
        	
        	Intent in = new Intent(getApplicationContext(),TransparentActivity3.class);
        	in.putExtra("msg", msg);
        	in.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        	startActivity(in);
        }
        
    	Log.d("DEBUG", "メッセージを受信しました");
    }
}


