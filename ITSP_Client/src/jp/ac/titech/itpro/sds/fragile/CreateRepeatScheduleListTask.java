package jp.ac.titech.itpro.sds.fragile;

import java.util.List;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;

import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint.RepeatScheduleV1EndPoint.CreateRepeatScheduleList;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleListContainer;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleResultV1Dto;
import com.appspot.fragile_t.repeatScheduleEndpoint.model.RepeatScheduleV1Dto;

import android.os.AsyncTask;
import android.util.Log;

public class CreateRepeatScheduleListTask extends AsyncTask<Void, Void, RepeatScheduleResultV1Dto> {

	private static final String SUCCESS = CommonConstant.SUCCESS;
	
	private CreateRepeatScheduleListFinishListener listener = null;
	
	private String mUserEmail;
	private List<RepeatScheduleV1Dto> mRepScheList;
	
	public CreateRepeatScheduleListTask(CreateRepeatScheduleListFinishListener listener, 
			String userEmail, List<RepeatScheduleV1Dto> repScheList) {
		// 結果通知用のリスナーを登録しておく
		this.listener = listener;
		// アドレスとスケジュールリストをセットする
		mUserEmail = userEmail;
		mRepScheList = repScheList;
	}
	@Override
	protected RepeatScheduleResultV1Dto doInBackground(Void... args) {
		RepeatScheduleResultV1Dto result = null;
		
		try {
			RepeatScheduleListContainer contain = new RepeatScheduleListContainer();
			contain.setList(mRepScheList);
			RepeatScheduleEndpoint endpoint = RemoteApi.getRepeatScheduleEndpoint();
			CreateRepeatScheduleList create = 
					endpoint.repeatScheduleV1EndPoint().createRepeatScheduleList(mUserEmail, contain);
			
			result = create.execute();
		} catch (Exception e) {
			Log.d("DEBUG", "CreateRepeatScheduleListTask fail: exception");
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(final RepeatScheduleResultV1Dto result) {
		if (SUCCESS.equals(result.getResult())) {
			Log.d("DEBUG", "CreateRepeatScheduleListTask success");
		} else {
			Log.d("DEBUG", "CreateRepeatScheduleListTask fail");
		}
		if (listener != null) {
			listener.onCreateRepeatScheduleListTaskFinish(result);
		}
	}

	/**
	 * CreateRepeatScheduleListTaskの終了通知用リスナー
	 */
	public interface CreateRepeatScheduleListFinishListener {
		public void onCreateRepeatScheduleListTaskFinish(RepeatScheduleResultV1Dto result);
	}
}
