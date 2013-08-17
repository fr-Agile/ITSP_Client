package jp.ac.titech.itpro.sds.fragile;

import java.util.ArrayList;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.GetFriendTask.GetFriendFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetShareTimeTask.GetShareTimeFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;

import com.google.api.services.getFriendEndpoint.model.GetFriendResultV1Dto;
import com.google.api.services.getFriendEndpoint.model.UserV1Dto;
import com.google.api.services.getShareTimeEndpoint.model.GetShareTimeV1ResultDto;
import com.google.api.services.getShareTimeEndpoint.model.GroupScheduleV1Dto;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ShareTimeActivity extends Activity 
							implements GetFriendFinishListener, GetShareTimeFinishListener {

	private String mUserEmail;
	private String mEmailCSV;
	private long mStartTime;
	private long mFinishTime;
	private List<UserV1Dto> mFriendList;
	private List<String> mFriendEmailList;

	// UI references.
	private ListView mFriendListView;
	private TextView mShareTimeTextView;

    private final static String SUCCESS = CommonConstant.SUCCESS;
    private final static String FAIL = CommonConstant.SUCCESS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sharetime);

		// Set up the sharetime form.
		mFriendListView = (ListView) findViewById(R.id.sharetime_list);
		mShareTimeTextView = (TextView) findViewById(R.id.sharetime_text);

		// リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        mFriendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                // クリックされたアイテムを取得します
                String item = (String) listView.getItemAtPosition(position);
                getShareTimeWith(item);
            }
        });

		
		initFriendList();
	}
	
	/**
	 * friend listを取得する
	 */
	private void initFriendList() {
		try {
			SharedPreferences pref = getSharedPreferences("user", Activity.MODE_PRIVATE);
			mUserEmail = pref.getString("email", "");
			
			// friendのemailリストを取得する
			GetFriendTask task = new GetFriendTask(this);
			task.execute(mUserEmail);
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "initFriend fail");
		}
	}
	/**
	 * friend list取得後、List Viewにして表示する
	 */
	@Override
	public void onFinish(GetFriendResultV1Dto result) {
		if (result != null) {
			mFriendEmailList = new ArrayList<String>();
			mFriendList = result.getFriendList();
			for (UserV1Dto friend : mFriendList) {
				mFriendEmailList.add(friend.getEmail());
			}
			
			// ListViewの中身はadapterごしに管理する
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_list_item_1, mFriendEmailList);
			mFriendListView.setAdapter(adapter);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
//		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	/**
	 * 
	 */
	public void getShareTimeWith(String friendEmail) {
		try {
			String emailCSV = mUserEmail + "," + friendEmail;
			
			// 共通空き時間を取得する
			GetShareTimeTask task = new GetShareTimeTask(this);
			task.setEmailCSV(emailCSV);
			task.setStartTime(0);
			task.setFinishTime(20);
			task.execute();
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "GetShareTime fail");
		}
	}
	

	/**
	 * 共通空き時間を取得後の処理
	 */
	@Override
	public void onFinish(GetShareTimeV1ResultDto result) {
		showSharetime(result);
	}
	
	private void showSharetime(GetShareTimeV1ResultDto result) {
		String str = "";
		String BR = System.getProperty("line.separator");
		
		for (GroupScheduleV1Dto gs : result.getGroupScheduleList()) {
			str += gs.getStartTime() + ", " + gs.getFinishTime();
			if (gs.getUserList() != null) {
				for (com.google.api.services.getShareTimeEndpoint.model.UserV1Dto user : gs.getUserList()) {
					str += ", " + user.getEmail();
				}
			}
			str += BR;
		}
        mShareTimeTextView.setText(str);
	}
}
