package jp.ac.titech.itpro.sds.fragile;

import java.util.ArrayList;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.fragile_t.groupEndpoint.GroupEndpoint;
import com.appspot.fragile_t.groupEndpoint.GroupEndpoint.GroupV1Endpoint.GetMyGroupList;
import com.appspot.fragile_t.groupEndpoint.model.GroupV1Dto;
import com.appspot.fragile_t.groupEndpoint.model.GroupV1DtoCollection;

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
			GetMyGroupList getGroupList = endpoint.groupV1Endpoint().getMyGroupList(userEmail);
			GroupV1DtoCollection collection = getGroupList.execute();
			if((collection != null)&&(collection.getItems() != null)){
				result = collection.getItems();
			}else{
				result = new ArrayList<GroupV1Dto>();
			}
			
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
