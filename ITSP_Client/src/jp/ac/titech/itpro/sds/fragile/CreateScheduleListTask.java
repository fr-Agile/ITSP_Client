package jp.ac.titech.itpro.sds.fragile;

import java.util.List;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;

import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint.ScheduleV1EndPoint.CreateScheduleList;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleListContainer;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleResultV1Dto;
import com.appspot.fragile_t.scheduleEndpoint.model.ScheduleV1Dto;

import android.os.AsyncTask;
import android.util.Log;

public class CreateScheduleListTask extends AsyncTask<Void, Void, ScheduleResultV1Dto> {

	private static final String SUCCESS = CommonConstant.SUCCESS;
	
	private CreateScheduleListFinishListener listener = null;
	
	private String mUserEmail;
	private List<ScheduleV1Dto> mScheList;
	
	public CreateScheduleListTask(CreateScheduleListFinishListener listener, 
			String userEmail, List<ScheduleV1Dto> scheList) {
		// 結果通知用のリスナーを登録しておく
		this.listener = listener;
		// アドレスとスケジュールリストをセットする
		mUserEmail = userEmail;
		mScheList = scheList;
	}
	@Override
	protected ScheduleResultV1Dto doInBackground(Void... args) {
		ScheduleResultV1Dto result = null;
		
		try {
			ScheduleListContainer contain = new ScheduleListContainer();
			contain.setList(mScheList);
			ScheduleEndpoint endpoint = RemoteApi.getScheduleEndpoint();
			CreateScheduleList create = 
					endpoint.scheduleV1EndPoint().createScheduleList(mUserEmail, contain);
			
			result = create.execute();
		} catch (Exception e) {
			Log.d("DEBUG", "CreateScheduleListTask fail: exception");
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(final ScheduleResultV1Dto result) {
		if (SUCCESS.equals(result.getResult())) {
			Log.d("DEBUG", "DeleteAllScheduleTask success");
		} else {
			Log.d("DEBUG", "DeleteAllShceduleTask fail");
		}
		if (listener != null) {
			listener.onCreateScheduleListTaskFinish(result);
		}
	}

	/**
	 * CreateScheduleListTaskの終了通知用リスナー
	 */
	public interface CreateScheduleListFinishListener {
		public void onCreateScheduleListTaskFinish(ScheduleResultV1Dto result);
	}
}
