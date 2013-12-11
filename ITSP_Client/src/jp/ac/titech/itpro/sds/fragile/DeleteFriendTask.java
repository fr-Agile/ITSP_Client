package jp.ac.titech.itpro.sds.fragile;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.fragile_t.friendEndpoint.FriendEndpoint;
import com.appspot.fragile_t.friendEndpoint.FriendEndpoint.FriendV1Endpoint.DeleteFriend;
import com.appspot.fragile_t.friendEndpoint.model.FriendResultV1Dto;

public class DeleteFriendTask extends AsyncTask<String, Void, FriendResultV1Dto> {
	
	private DeleteFriendFinishListener listener = null;
	
	/**
	 * 結果通知用のリスナーを登録しておく
	 */
	public DeleteFriendTask(DeleteFriendFinishListener listener) {
		this.listener = listener;
	}
	@Override
	protected FriendResultV1Dto doInBackground(String... args) {
		String toemail = args[0];
		String fromemail = args[1];
		FriendResultV1Dto result = null;
		try {
			FriendEndpoint endpoint = RemoteApi.getFriendEndpoint();
			DeleteFriend delete = endpoint.friendV1Endpoint().deleteFriend(toemail, fromemail);
			result = delete.execute();
			
		} catch (Exception e) {
			Log.d("DEBUG", "deleteFriendTask fail");
			e.printStackTrace();
		}
		return result;
	}
	

	@Override
	protected void onPostExecute(FriendResultV1Dto result) {
		if (result == null) {
			Log.d("DEBUG", "deleteFriendTask fail");
		} else {
			Log.d("DEBUG", "deleteFriendTask success");
		}
		if (listener != null) {
			listener.onFinish(result);
		}
	}

	/**
	 * DeleteFriendTaskの終了通知用リスナー
	 */
	public interface DeleteFriendFinishListener {
		public void onFinish(FriendResultV1Dto result);
	}
}
