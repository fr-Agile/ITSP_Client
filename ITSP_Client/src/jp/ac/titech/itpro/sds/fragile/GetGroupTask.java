package jp.ac.titech.itpro.sds.fragile;

import java.util.List;

import com.appspot.fragile_t.groupEndpoint.GroupEndpoint;
import com.appspot.fragile_t.groupEndpoint.GroupEndpoint.GroupV1Endpoint.GetGroupList;
import com.appspot.fragile_t.groupEndpoint.model.GroupV1Dto;


import android.os.AsyncTask;
import android.util.Log;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;

public class GetGroupTask extends AsyncTask<String, Void, List<GroupV1Dto>> {

	private static final String SUCCESS = CommonConstant.SUCCESS;
	
	private GetGroupFinishListener listener = null;
	
	/**
	 * 結果通知用のリスナーを登録しておく
	 */
	public GetGroupTask(GetGroupFinishListener listener) {
		this.listener = listener;
	}
	@Override
	protected List<GroupV1Dto> doInBackground(String... args) {
		String userEmail = args[0];
		List<GroupV1Dto> result = null;
		try {
			GroupEndpoint endpoint = RemoteApi.getGroupEndpoint();
			GetGroupList getGroupList = endpoint.groupV1Endpoint().getGroupList(userEmail);
			result = getGroupList.execute().getItems();
			
		} catch (Exception e) {
			Log.d("DEBUG", "GetGroupTask fail");
			e.printStackTrace();
		}
		return result;
	}
	

	@Override
	protected void onPostExecute(final List<GroupV1Dto> result) {
		if (result == null) {
			Log.d("DEBUG", "getGroupTask fail");
		} else {
			Log.d("DEBUG", "getGroupTask success");
		}
		if (listener != null) {
			listener.onFinish(result);
		}
	}

	/**
	 * GetFriendTaskの終了通知用リスナー
	 */
	public interface GetGroupFinishListener {
		public void onFinish(List<GroupV1Dto> result);
	}
}
