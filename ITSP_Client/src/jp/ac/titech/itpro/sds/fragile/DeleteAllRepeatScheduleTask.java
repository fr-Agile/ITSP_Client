package jp.ac.titech.itpro.sds.fragile;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;

import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.DeleteAllGoogleRepeatSchedule;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.DeleteAllRepeatSchedule;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleResultV1Dto;

import android.os.AsyncTask;
import android.util.Log;

public class DeleteAllRepeatScheduleTask extends AsyncTask<String, Void, RepeatScheduleResultV1Dto> {

	private static final String SUCCESS = CommonConstant.SUCCESS;
	
	private DeleteAllRepeatScheduleFinishListener listener = null;
	private boolean onlyGoogle = false;;
	
	public DeleteAllRepeatScheduleTask(DeleteAllRepeatScheduleFinishListener listener) {
		// 結果通知用のリスナーを登録しておく
		this.listener = listener;
		this.onlyGoogle = false;
	}
	public DeleteAllRepeatScheduleTask(DeleteAllRepeatScheduleFinishListener listener,
			boolean onlyGoogle) {
		// 結果通知用のリスナーを登録しておく
		this.listener = listener;
		this.onlyGoogle = onlyGoogle;
	}
	
	@Override
	protected RepeatScheduleResultV1Dto doInBackground(String... args) {
		String userEmail = args[0];
		RepeatScheduleResultV1Dto result = null;
		
		try {
			RepeatScheduleEndpoint endpoint = RemoteApi.getRepeatScheduleEndpoint();
			if (onlyGoogle) {
				DeleteAllGoogleRepeatSchedule delete = 
						endpoint.repeatScheduleV1EndPoint().deleteAllGoogleRepeatSchedule(userEmail);
				result = delete.execute();
			} else {
				DeleteAllRepeatSchedule delete = 
						endpoint.repeatScheduleV1EndPoint().deleteAllRepeatSchedule(userEmail);
				result = delete.execute();
			}
		} catch (Exception e) {
			Log.d("DEBUG", "DeleteAllScheduleTask fail");
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(final RepeatScheduleResultV1Dto result) {
		if (SUCCESS.equals(result.getResult())) {
			Log.d("DEBUG", "DeleteAllRepeatScheduleTask success");
		} else {
			Log.d("DEBUG", "getGroupTask fail");
		}
		if (listener != null) {
			listener.onDeleteAllRepeatScheduleTaskFinish(result);
		}
	}

	/**
	 * DeleteAllRepeatScheduleTaskの終了通知用リスナー
	 */
	public interface DeleteAllRepeatScheduleFinishListener {
		public void onDeleteAllRepeatScheduleTaskFinish(RepeatScheduleResultV1Dto result);
	}
}


