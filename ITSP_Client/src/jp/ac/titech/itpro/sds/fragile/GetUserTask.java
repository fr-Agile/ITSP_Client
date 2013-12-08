package jp.ac.titech.itpro.sds.fragile;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import jp.ac.titech.itpro.sds.fragile.api.constant.GoogleConstant;

import com.appspot.fragile_t.getUserEndpoint.GetUserEndpoint;
import com.appspot.fragile_t.getUserEndpoint.GetUserEndpoint.GetUserV1Endpoint.GetUser;
import com.appspot.fragile_t.getUserEndpoint.model.GetUserResultV1Dto;
import com.appspot.fragile_t.getUserEndpoint.model.UserV1Dto;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class GetUserTask extends AsyncTask<Void, Void, GetUserResultV1Dto> {
	private String mEmail = null;
	private UserV1Dto mUser = null;
	private final static String SUCCESS = CommonConstant.SUCCESS;
	
	private GetUserFinishListener listener = null;
	
	/**
	 * 結果通知用のリスナーを登録しておく
	 */
	public GetUserTask(GetUserFinishListener listener) {
		this.listener = listener;
	}
	
	public void setEmail(String email) {
		this.mEmail = email;
	}
	
	@Override
	protected GetUserResultV1Dto doInBackground(Void... args) {
		try {
			
			GetUserEndpoint endpoint = RemoteApi.getGetUserEndpoint();
			GetUser getUser = endpoint.getUserV1Endpoint().getUser(mEmail);
			GetUserResultV1Dto result = getUser.execute();
			return result;
		} catch (Exception e) {
			Log.d("DEBUG", "get user fail: " + e);
			return null;
		}
	}

	@Override
	protected void onPostExecute(GetUserResultV1Dto result) {
		listener.onGetUserFinish(result);
	}

	@Override
	protected void onCancelled() {
		listener.onGetUserFinish(null);
	}
	
	/**
	 * GetUserTaskの終了通知用リスナー
	 */
	public interface GetUserFinishListener {
		public void onGetUserFinish(GetUserResultV1Dto result);
	}
}
