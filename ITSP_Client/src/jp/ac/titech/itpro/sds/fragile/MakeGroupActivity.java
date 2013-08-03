package jp.ac.titech.itpro.sds.fragile;



import java.util.ArrayList;
import java.util.List;

import com.google.api.services.getFriendEndpoint.model.GetFriendResultV1Dto;
import com.google.api.services.getFriendEndpoint.model.UserV1Dto;
import com.google.api.services.getShareTimeEndpoint.model.GetShareTimeV1ResultDto;

import jp.ac.titech.itpro.sds.fragile.GetFriendTask.GetFriendFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetShareTimeTask.GetShareTimeFinishListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MakeGroupActivity extends Activity implements
        GetFriendFinishListener {

	LinearLayout layout;
	EditText group_title;
	
	AlertDialog.Builder alertDialog;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_makegroup);
		
		alertDialog = new AlertDialog.Builder(this);
		
		//ボタン作成
	    Button makegroup_btn = (Button)findViewById(R.id.make_group_button);
	    makegroup_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				String title = "タイトル：" + group_title.getText().toString();
				ArrayList<String> names = GetCheckedFriend();
				String message = "";
				
				for(int i=0;i<names.size();i++){
					message += "フレンド名：" + names.get(i) + "\r\n";
				}
				
				alertDialog.setTitle(title);      //タイトル設定
		        alertDialog.setMessage(message);  //内容(メッセージ)設定
				
				alertDialog.show();
		        
	    		//DBにグループを作成(予定)
	    	}
	    });
	    Button back_btn = (Button)findViewById(R.id.go_to_logged_from_makegroup);
	    back_btn.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {   //トップ画面へ遷移
	    		startActivity(new Intent(MakeGroupActivity.this, LoggedActivity.class));
	    	}
	    });
		
		// 友達一覧のチェックボックスを作成するためのレイアウト
		layout = (LinearLayout)findViewById(R.id.friend_list);
		
		//グループの名前を入れるテキストボックス
		group_title = (EditText) findViewById(R.id.edit_name);
		
		getFriendList();
    }
    
    //チェックされている友達の名前を取得
    private ArrayList<String> GetCheckedFriend(){
    	int max = layout.getChildCount();
    	ArrayList<String> checked_names = new ArrayList<String>();
    	for(int i=0;i<max;i++){
    		CheckBox c = (CheckBox) layout.getChildAt(i);
    		if(c.isChecked()){
    			checked_names.add((String)c.getText());
    		}
    	}
    	return checked_names;
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
					friendNameList.add(friend.getLastName()+"　"+friend.getFirstName());
				}
				String[] nameStrList = friendNameList
						.toArray(new String[friendNameList.size()]);
				
				//取得したネームリストをチェックボックスとしてレイアウトに追加
				for (String name : nameStrList){
					CheckBox checkbox = new CheckBox(this);
					checkbox.setText(name);
					layout.addView(checkbox);
					Log.d("DEBUG", "added to view by "+name+"さん");
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




    
}
