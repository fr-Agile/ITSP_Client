package jp.ac.titech.itpro.sds.fragile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.GetFriendTask.GetFriendFinishListener;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.appspot.fragile_t.getFriendEndpoint.model.GetFriendResultV1Dto;
import com.appspot.fragile_t.getFriendEndpoint.model.UserV1Dto;
import com.appspot.fragile_t.groupEndpoint.GroupEndpoint;
import com.appspot.fragile_t.groupEndpoint.GroupEndpoint.GroupV1Endpoint.MakeGroup;
import com.appspot.fragile_t.groupEndpoint.model.GroupResultV1Dto;
import com.appspot.fragile_t.groupEndpoint.model.StringListContainer;

public class MakeGroupActivity extends Activity implements
		GetFriendFinishListener {

	LinearLayout layout;
	Context context;
	EditText group_title;
	List<String> emails; // 友達のメールリスト
	MakeGroupTask task;

	AlertDialog.Builder alertDialog;

	private static String SUCCESS = "success";
	private static String FAIL = "fail";

	private static String NULLNAME = "nullname";
	private static String NULLOWNER = "nullowner";
	private static String ALREADYGROUP = "alreadygroup";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_makegroup);

		context = this;
		alertDialog = new AlertDialog.Builder(this);
		emails = new ArrayList<String>();

		// ボタン作成
		Button makegroup_btn = (Button) findViewById(R.id.make_group_button);
		makegroup_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// DBにグループを作成
				task = new MakeGroupTask();
				task.execute();

			}
		});
		Button back_btn = (Button) findViewById(R.id.go_to_logged_from_makegroup);
		back_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { // トップ画面へ遷移
				startActivity(new Intent(MakeGroupActivity.this,
						LoggedActivity.class));
			}
		});

		// 友達一覧のチェックボックスを作成するためのレイアウト
		layout = (LinearLayout) findViewById(R.id.friend_list);

		// グループの名前を入れるテキストボックス
		group_title = (EditText) findViewById(R.id.edit_name);

		getFriendList();
	}

	// チェックされている友達の名前を取得
	private ArrayList<String> GetCheckedFriend() {
		int max = layout.getChildCount();
		ArrayList<String> checked_names = new ArrayList<String>();
		for (int i = 0; i < max; i++) {
			CheckBox c = (CheckBox) layout.getChildAt(i);
			if (c.isChecked()) {
				checked_names.add((String) c.getText());
			}
		}
		return checked_names;
	}

	// チェックされている友達のメールアドレスのリストを取得
	private List<String> GetCheckedMail() {
		int max = layout.getChildCount();
		List<String> checked_emails = new ArrayList<String>();
		for (int i = 0; i < max; i++) {
			CheckBox c = (CheckBox) layout.getChildAt(i);
			if (c.isChecked()) {
				checked_emails.add(emails.get(i));
			}
		}
		return checked_emails;
	}

	/**
	 * friend listを取得する
	 */
	private void getFriendList() {
		try {
			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String userEmail = pref.getString("email", "");

			// friendのemailリストを取得する
			GetFriendTask task = new GetFriendTask(this);
			task.execute(userEmail);

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "get friend fail");
		}
	}

	/**
	 * GetFriendTask終了後の処理
	 */
	@Override
	public void onFinish(GetFriendResultV1Dto result) {
		try {
			if (result != null) {
				final List<String> friendNameList = new ArrayList<String>();
				List<UserV1Dto> friendList = result.getFriendList();
				for (UserV1Dto friend : friendList) {
					friendNameList.add(friend.getLastName() + "　"
							+ friend.getFirstName());
					emails.add(friend.getEmail());
				}
				String[] nameStrList = friendNameList
						.toArray(new String[friendNameList.size()]);

				// 取得したネームリストをチェックボックスとしてレイアウトに追加
				for (String name : nameStrList) {
					CheckBox checkbox = new CheckBox(this);
					checkbox.setText(name);
					layout.addView(checkbox);
					Log.d("DEBUG", "added to view by " + name + "さん");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class MakeGroupTask extends AsyncTask<Void, Void, Boolean> {
		
		ProgressDialog dialog;
		
		@Override
		protected Boolean doInBackground(Void... args) {

			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String email = pref.getString("email", "");

			StringListContainer container = new StringListContainer();
			container.setList(emails);

			GroupEndpoint endpoint = RemoteApi.getGroupEndpoint();
			MakeGroup group;
			GroupResultV1Dto result;
			try {
				group = endpoint.groupV1Endpoint().makeGroup(
						group_title.getText().toString(), email, container);
				result = group.execute();
			} catch (IOException e) {
				Log.d("DEBUG", e.toString());
				result = null;
			}

			if (result == null) {
				return false;
			}else if (SUCCESS.equals(result.getResult())) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if(dialog != null){
	    	      dialog.dismiss();
	    	}
			if (success) {
				Log.d("DEBUG", "グループ作れました");
				String title = "タイトル：" + group_title.getText().toString();
				ArrayList<String> names = GetCheckedFriend();
				String message = "";

				for(int i=0;i<names.size();i++){
				message += "フレンド名：" + names.get(i) + "\r\n";
				}

				alertDialog.setTitle(title); //タイトル設定
				alertDialog.setMessage(message); //内容(メッセージ)設定
				alertDialog.show();

			} else {
				Log.d("DEBUG", "グループ作成失敗");
				alertDialog.setTitle("エラー"); //タイトル設定
				alertDialog.setMessage("グループ作成失敗"); //内容(メッセージ)設定
				alertDialog.show();
			}
		}
		
		@Override
	    protected void onPreExecute() {
	      dialog = new ProgressDialog(context);
	      dialog.setTitle("Please wait");
	      dialog.setMessage("Conecting...");
	      dialog.show();
	    } 

	}

}
