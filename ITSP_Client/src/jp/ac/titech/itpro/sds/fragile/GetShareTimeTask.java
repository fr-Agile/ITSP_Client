package jp.ac.titech.itpro.sds.fragile;

import java.util.Map;

import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;

import com.google.api.services.getShareTimeEndpoint.GetShareTimeEndpoint;
import com.google.api.services.getShareTimeEndpoint.GetShareTimeEndpoint.GetShareTimeV1Endpoint.GetShareTime;
import com.google.api.services.getShareTimeEndpoint.model.GetShareTimeV1ResultDto;

import android.os.AsyncTask;
import android.util.Log;

public class GetShareTimeTask extends AsyncTask<Void, Void, GetShareTimeV1ResultDto> {

	private static final String SUCCESS = CommonConstant.SUCCESS;
	
	private GetShareTimeFinishListener listener = null;
	
	private String emailCSV;
	private long startTime;
	private long finishTime;
	
	/**
	 * 結果通知用のリスナーを登録しておく
	 */
	public GetShareTimeTask(GetShareTimeFinishListener listener) {
		this.listener = listener;
	}
	@Override
	protected GetShareTimeV1ResultDto doInBackground(Void... args) {
		try {
			GetShareTimeEndpoint endpoint = RemoteApi.getGetShareTimeEndpoint();
			GetShareTime getShareTime = endpoint.getShareTimeV1Endpoint()
					.getShareTime(emailCSV, startTime, finishTime);
			GetShareTimeV1ResultDto result = getShareTime.execute();
			
			return result;
			
		} catch (Exception e) {
			Log.d("DEBUG", "GetFriendTask fail");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onPostExecute(final GetShareTimeV1ResultDto result) {
		if (result == null) {
			Log.d("DEBUG", "getShareTimeTask fail");
		} else if (SUCCESS.equals(result.getResult())) {
			Log.d("DEBUG", "getShareTimeTask success");
		} else {
			Log.d("DEBUG", "getShareTimeTask fail");
		}
		if (listener != null) {
			listener.onFinish(result);
		}
	}

	public void setEmailCSV(String emailCSV) {
		this.emailCSV = emailCSV;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	/**
	 * GetShareTimeTaskの終了通知用リスナー
	 */
	public interface GetShareTimeFinishListener {
		public void onFinish(GetShareTimeV1ResultDto result);
	}
}
