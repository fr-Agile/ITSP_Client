package jp.ac.titech.itpro.sds.fragile;

import java.util.List;

import jp.ac.titech.itpro.sds.fragile.CalendarSaver.GoogleCalendarSaveFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetUserTask.GetUserFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.api.constant.GoogleConstant;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleAccountChecker;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleAccountChecker.GoogleAccountCheckFinishListener;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleCalendarChecker;
import jp.ac.titech.itpro.sds.fragile.utils.GoogleCalendarChecker.GoogleCalendarCheckFinishListener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.appspot.fragile_t.getUserEndpoint.model.GetUserResultV1Dto;
import com.appspot.fragile_t.getUserEndpoint.model.UserV1Dto;

public class GoogleAccountCheckAndImportTask implements GetUserFinishListener, 
	GoogleCalendarCheckFinishListener, GoogleCalendarSaveFinishListener, 
	GoogleAccountCheckFinishListener {
	private Activity mActivity;
	private GetUserTask mGetUserTask = null;
	private UserV1Dto mUser;
	private GoogleAccountCheckAndImportFinishListener mListener;
	private final static String SUCCESS = CommonConstant.SUCCESS;
	
	/**
	 * google account & import 終了通知
	 */
	public interface GoogleAccountCheckAndImportFinishListener {
		public void onGoogleAccountCheckAndImportFinish(boolean result);
	}
	
	public GoogleAccountCheckAndImportTask(Activity activity) {
		this(activity, null);
	}
	
	public GoogleAccountCheckAndImportTask(Activity activity, 
			GoogleAccountCheckAndImportFinishListener listener) {
		this.mActivity = activity;
		this.mListener = listener;
	}
	
	public void run() {
		// googleカレンダーをインポートするかどうか判断するため、user情報を取得する
		if (mGetUserTask == null) {
			SharedPreferences pref = mActivity.getSharedPreferences("user", Activity.MODE_PRIVATE);
			String email = pref.getString("email", "");
			Log.d("DEBUG", "start importing google event");
			mGetUserTask = new GetUserTask(this);
			mGetUserTask.setEmail(email);
			mGetUserTask.execute();
			
		}
	}


	/**
	 * GoogleCalendarのインポート処理
	 */

	@Override
	public void onGoogleAccountCheckFinish(boolean result,
			List<String> accountList, List<String> displayList, List<Long> idList) {
		if (result) {
			if (accountList != null && accountList.size() > 0) {
				// アカウントが登録されているのでカレンダーを読み込む
				GoogleCalendarChecker gcc = 
						new GoogleCalendarChecker(mActivity, this);
				gcc.run();

			} else {
				// アカウントが登録されていない場合、ダイアログを表示して登録を促す
				new GoogleAccountRegistDialogBuilder(mActivity)
					.setDefault()
					.show();
			}
			Log.d("DEBUG", "checking google account success");
		} else {
			Log.d("DEBUG", "checking google account fail");
			finish(false);
		}

	}

	@Override
	public void onGoogleCalendarCheckFinish(boolean result, Cursor arg1) {
		if (result) {
			// カレンダーがインポートできたのデータベースを書き換える
			CalendarSaver cs = new CalendarSaver(mActivity, this);
			cs.save(arg1);
		} else {
			finish(false);
		}
	}

	@Override
	public void onGoogleCalendarSaveFinish(boolean result) {
		if (result) {
			// 予定を書き換え終わったので
			Log.d("DEBUG", "create schedule list success");
			finish(true);
		} else {
			Log.d("DEBUG", "create schedule list failed");
			finish(false);
		}
	}
	/**
	 * GoogleCalendarのインポート処理ここまで
	 */
	
	@Override
	public void onGetUserFinish(GetUserResultV1Dto result) {
		mGetUserTask = null;
		if ((result != null) && SUCCESS.equals(result.getResult())) {
			Log.d("DEBUG", "get user success");
			mUser = result.getUser();
			
			if (mUser.getGoogleAccount().equals(GoogleConstant.UNTIED_TO_GOOGLE)) {
				// google登録されていないのでインポートしない
			} else {
				// インポートする
				// GoogleAccountCheckerを起動
				GoogleAccountChecker gac = new GoogleAccountChecker(
						mActivity, this);
				gac.run();
			}
		} else {
			mUser = null;
			Log.d("DEBUG", "get user fail");
			finish(false);
		}
	}
	private void finish(boolean result) {
		if (mListener != null) {
			mListener.onGoogleAccountCheckAndImportFinish(result);
		}
	}

}
