package jp.ac.titech.itpro.sds.fragile;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;

import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.DeleteAllGoogleSchedule;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.DeleteAllSchedule;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleResultV1Dto;

import android.os.AsyncTask;
import android.util.Log;

public class DeleteAllScheduleTask extends AsyncTask<String, Void, ScheduleResultV1Dto> {

	private static final String SUCCESS = CommonConstant.SUCCESS;
	private DeleteAllScheduleFinishListener listener = null;
	private boolean onlyGoogle = false;;
	
	public DeleteAllScheduleTask(DeleteAllScheduleFinishListener listener) {
		// 結果通知用のリスナーを登録しておく
		this.listener = listener;
	}
	
	public DeleteAllScheduleTask(DeleteAllScheduleFinishListener listener, boolean onlyGoogle) {
		// 結果通知用のリスナーを登録しておく
		this.listener = listener;
		this.onlyGoogle = onlyGoogle;
	}
	
	@Override
	protected ScheduleResultV1Dto doInBackground(String... args) {
		String userEmail = args[0];
		ScheduleResultV1Dto result = null;
		
		try {
			ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
			if (onlyGoogle) {
				DeleteAllGoogleSchedule delete = 
						endpoint.scheduleV1EndPoint().deleteAllGoogleSchedule(userEmail);
				result = delete.execute();
			} else {
				DeleteAllSchedule delete = endpoint.scheduleV1EndPoint().deleteAllSchedule(userEmail);
				result = delete.execute();
			}
			return result;
		} catch (Exception e) {
			Log.d("DEBUG", "DeleteAllScheduleTask fail");
			e.printStackTrace();
			return result;
		}
	}

	@Override
	protected void onPostExecute(final ScheduleResultV1Dto result) {
		if (SUCCESS.equals(result.getResult())) {
			Log.d("DEBUG", "DeleteAllScheduleTask success");
		} else {
			Log.d("DEBUG", "getGroupTask fail");
		}
		if (listener != null) {
			listener.onDeleteAllScheduleTaskFinish(result);
			listener = null;
		}
	}

	/**
	 * DeleteAllScheduleTaskの終了通知用リスナー
	 */
	public interface DeleteAllScheduleFinishListener {
		public void onDeleteAllScheduleTaskFinish(ScheduleResultV1Dto result);
	}
}

