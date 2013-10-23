package jp.ac.titech.itpro.sds.fragile;

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.ac.titech.itpro.sds.fragile.utils.CommonUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.google.android.gcm.GCMBaseIntentService;




/**
* プッシュ通知の受信に関する処理を行うクラス
*/
public class GCMIntentService extends GCMBaseIntentService {
 
    /** コンストラクタ */
    public GCMIntentService() {
        super(CommonUtils.GCM_SENDER_ID);
    }
 
    /** RegistrationID が登録された場合に呼び出されるメソッド */
    @Override
    public void onRegistered(Context context, String registrationId) {
        // [RegistrationID]を本体に保存する処理などを記述
    }
 
    /** RegistrationID が登録解除された場合に呼び出されるメソッド */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        // [RegistrationID]を本体から削除する処理などを記述
    }
 
    /** エラーが発生した場合に呼び出されるメソッド */
    @Override
    public void onError(Context context, String errorId) {
        // エラー処理などを記述
    }
 
    /** メッセージを受信した場合に呼び出されるメソッド */
    @Override
    protected void onMessage(Context context, Intent intent) {
        // メッセージ取得
        String str = intent.getStringExtra("message");
       
        // ステータスバーに通知する情報を生成
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification =
                new Notification(R.drawable.ic_launcher, str, System.currentTimeMillis());
 
 
        // インテント生成
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("GCM_MESSAGE", str);
 
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
        notification.flags =
                Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
        notification.contentIntent = contentIntent;
 
        // 通知
        notificationManager.notify(R.string.app_name, notification);
    }
}


