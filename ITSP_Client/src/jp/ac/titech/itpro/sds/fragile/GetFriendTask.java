package jp.ac.titech.itpro.sds.fragile;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;

import com.google.api.services.getFriendEndpoint.GetFriendEndpoint;
import com.google.api.services.getFriendEndpoint.GetFriendEndpoint.GetFriendV1Endpoint.GetFriendTo;
import com.google.api.services.getFriendEndpoint.model.GetFriendResultV1Dto;

import android.os.AsyncTask;
import android.util.Log;

public class GetFriendTask extends AsyncTask<String, Void, GetFriendResultV1Dto> {

	private static final String SUCCESS = CommonConstant.SUCCESS;
	
	private GetFriendFinishListener listener = null;
	
	/**
	 * 結果通知用のリスナーを登録しておく
	 */
	public GetFriendTask(GetFriendFinishListener listener) {
		this.listener = listener;
	}
	@Override
	protected GetFriendResultV1Dto doInBackground(String... args) {
		String userEmail = args[0];
		try {
			GetFriendEndpoint endpoint = RemoteApi.getGetFriendEndpoint();
			GetFriendTo getFriendTo = endpoint.getFriendV1Endpoint().getFriendTo(userEmail);
			GetFriendResultV1Dto result = getFriendTo.execute();
			
			return result;
			
		} catch (Exception e) {
			Log.d("DEBUG", "GetFriendTask fail");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onPostExecute(final GetFriendResultV1Dto result) {
		if (result == null) {
			Log.d("DEBUG", "getFriendTask fail");
		} else if (SUCCESS.equals(result.getResult())) {
			Log.d("DEBUG", "getFriendTask success");
		} else {
			Log.d("DEBUG", "getFriendTask fail");
		}
		if (listener != null) {
			listener.onFinish(result);
		}
	}

	/**
	 * GetFriendTaskの終了通知用リスナー
	 */
	public interface GetFriendFinishListener {
		public void onFinish(GetFriendResultV1Dto result);
	}
}