package jp.ac.titech.itpro.sds.fragile;

import java.util.List;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.fragile_t.groupEndpoint.GroupEndpoint;
import com.appspot.fragile_t.groupEndpoint.GroupEndpoint.GroupV1Endpoint.DeleteGroup;
import com.appspot.fragile_t.groupEndpoint.GroupEndpoint.GroupV1Endpoint.GetMyGroupList;
import com.appspot.fragile_t.groupEndpoint.model.GroupResultV1Dto;
import com.appspot.fragile_t.groupEndpoint.model.GroupV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.DeleteAllSchedule;

public class DeleteGroupTask extends AsyncTask<String, Void, GroupResultV1Dto> {
	
	private DeleteGroupFinishListener listener = null;
	
	/**
	 * 結果通知用のリスナーを登録しておく
	 */
	public DeleteGroupTask(DeleteGroupFinishListener listener) {
		this.listener = listener;
	}
	@Override
	protected GroupResultV1Dto doInBackground(String... args) {
		String keyS = args[0];
		GroupResultV1Dto result = null;
		try {
			GroupEndpoint endpoint = RemoteApi.getGroupEndpoint();
			DeleteGroup delete = endpoint.groupV1Endpoint().deleteGroup(keyS);
			result = delete.execute();
			
		} catch (Exception e) {
			Log.d("DEBUG", "deleteGroupTask fail");
			e.printStackTrace();
		}
		return result;
	}
	

	@Override
	protected void onPostExecute(GroupResultV1Dto result) {
		if (result == null) {
			Log.d("DEBUG", "deleteGroupTask fail");
		} else {
			Log.d("DEBUG", "deleteGroupTask success");
		}
		if (listener != null) {
			listener.onFinish(result);
		}
	}

	/**
	 * DeleteFriendTaskの終了通知用リスナー
	 */
	public interface DeleteGroupFinishListener {
		public void onFinish(GroupResultV1Dto result);
	}
}
