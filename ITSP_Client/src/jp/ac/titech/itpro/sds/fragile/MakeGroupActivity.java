package jp.ac.titech.itpro.sds.fragile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ac.titech.itpro.sds.fragile.DeleteFriendTask.DeleteFriendFinishListener;
import jp.ac.titech.itpro.sds.fragile.DeleteGroupTask.DeleteGroupFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetFriendTask.GetFriendFinishListener;
import jp.ac.titech.itpro.sds.fragile.GetGroupTask.GetGroupFinishListener;
import jp.ac.titech.itpro.sds.fragile.TransparentActivity2.ScheduleDelTask;
import jp.ac.titech.itpro.sds.fragile.api.RemoteApi;
import jp.ac.titech.itpro.sds.fragile.api.constant.CommonConstant;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appspot.fragile_t.friendEndpoint.model.FriendResultV1Dto;
import com.appspot.fragile_t.getFriendEndpoint.model.GetFriendResultV1Dto;
import com.appspot.fragile_t.getFriendEndpoint.model.UserV1Dto;
import com.appspot.fragile_t.groupEndpoint.GroupEndpoint;
import com.appspot.fragile_t.groupEndpoint.GroupEndpoint.GroupV1Endpoint.MakeGroup;
import com.appspot.fragile_t.groupEndpoint.model.GroupResultV1Dto;
import com.appspot.fragile_t.groupEndpoint.model.GroupV1Dto;
import com.appspot.fragile_t.groupEndpoint.model.StringListContainer;

public class MakeGroupActivity extends Activity implements
		GetFriendFinishListener, GetGroupFinishListener, DeleteGroupFinishListener, DeleteFriendFinishListener {

	LinearLayout layout, group_list;
	Context context;
	EditText group_title;
	List<String> emails; // 友達のメールリスト
	List<String> groups;  // グループのKeySのリスト
	MakeGroupTask task;
	Button makegroup_btn;
	
	public static String buffkeyS = null;  // 削除しようとしているグループのKeyを保存しておく
	public static String buffmail = null;  // 削除しようとしている友人のメールアドレスを保存しておく
	
	public static ProgressDialog dialog = null;

	AlertDialog.Builder alertDialog;
	
	private List<GroupV1Dto> mGroupList = null;

	private static final String SUCCESS = CommonConstant.SUCCESS;
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
		groups = new ArrayList<String>();

		// ボタン作成
		makegroup_btn = (Button) findViewById(R.id.make_group_button);
		makegroup_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// グループ名が設定されているなら
				if((group_title.getText().toString()!=null)&&(!group_title.getText().toString().isEmpty())){
					// DBにグループを作成
					task = new MakeGroupTask();
					task.execute();
				}
			}
		});
		// 通信成功するまでは使用不可
		makegroup_btn.setEnabled(false);
		
		Button back_btn = (Button) findViewById(R.id.go_to_logged_from_makegroup);
		back_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		// 友達一覧のチェックボックスを作成するためのレイアウト
		layout = (LinearLayout) findViewById(R.id.friend_list);

		// グループの名前を入れるテキストボックス
		group_title = (EditText) findViewById(R.id.edit_name);
		
		// グループ一覧のためのレイアウト
		group_list = (LinearLayout) findViewById(R.id.group_list);

		dialog = new ProgressDialog(context);
    	dialog.setTitle("Please wait");
    	dialog.setMessage("Conecting...");
    	dialog.show();
		
		getFriendList();
	}

	// チェックされている友達の名前を取得
	private ArrayList<String> getCheckedFriend() {
		int max = layout.getChildCount();
		ArrayList<String> checked_names = new ArrayList<String>();
		for (int i = 0; i < max; i++) {
			LinearLayout l = (LinearLayout) layout.getChildAt(i);
			CheckBox c = (CheckBox) l.getChildAt(0);
			if (c.isChecked()) {
				checked_names.add((String) c.getText());
			}
		}
		return checked_names;
	}

	// チェックされている友達のメールアドレスのリストを取得
	private List<String> getCheckedMail() {
		int max = layout.getChildCount();
		List<String> checked_emails = new ArrayList<String>();
		for (int i = 0; i < max; i++) {
			LinearLayout l = (LinearLayout) layout.getChildAt(i);
			CheckBox c = (CheckBox) l.getChildAt(0);
			if (c.isChecked()) {
				checked_emails.add(emails.get(i));
			}
		}
		return checked_emails;
	}
	
	// 友人のチェックボックスを削除する
	public void delCheckMail(String mail) {
		int max = layout.getChildCount();
		
		for (int i = 0; i < max; i++) {
			if (emails.get(i).equals(mail)) {
				emails.remove(i);
				layout.removeViewAt(i);
			}
		}
		
		// チェックボックスがなくなったらグループ作成ボタンを使えないようにする
		if(emails.size()<=0) {
			makegroup_btn.setEnabled(false);
		}
	}
	
	// グループのボタンを削除する
	public void delGroupBtn(String keyS) {
		int max = group_list.getChildCount();
		
		for (int i = 0; i < max; i++) {
			if (groups.get(i).equals(keyS)) {
				groups.remove(i);
				group_list.removeViewAt(i);
			}
		}
		
		// グループがなくなったらメッセージを表示
		if(groups.size()<=0) {
			TextView none = new TextView(context);
			none.setText("グループはありません");
			group_list.addView(none);
		}
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
	 * group listを取得する
	 */
	private void getGroupList() {
		try {
			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String userEmail = pref.getString("email", "");

			// groupリストを取得する
			GetGroupTask task = new GetGroupTask(this);
			task.execute(userEmail);

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "get group fail");
		}
	}
	
	/**
	 * group を削除する
	 */
	public void deleteGroup(String KeyS) {
		try {
			Log.d("DEBUG", KeyS+"のグループを削除しようとしています");
			DeleteGroupTask task = new DeleteGroupTask(this);
			task.execute(KeyS);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "delete group fail");
		}
	}
	
	/**
	 * friend を削除する
	 */
	public void deleteFriend(String email) {
		SharedPreferences pref = getSharedPreferences("user",
				Activity.MODE_PRIVATE);
		String userEmail = pref.getString("email", "");
		
		try {
			DeleteFriendTask task = new DeleteFriendTask(this);
			task.execute(email, userEmail);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "delete friend fail");
		}
	}

	/**
	 * GetFriendTask終了後の処理
	 */
	@Override
	public void onFinish(GetFriendResultV1Dto result) {
		
		
		// 終了時に通信中...のダイアログを消す
		if(dialog != null){
  	      dialog.dismiss();
		}
		
		try {
			
			if ((result != null)&&(result.getResult().equals(SUCCESS))) {
				if((result.getFriendList() != null)&&(!result.getFriendList().isEmpty())){
					
					List<UserV1Dto> friendList = result.getFriendList();
					for (final UserV1Dto friend : friendList) {
						
						// グループ作成時のためにemailのリストを保存しておく
						emails.add(friend.getEmail());
					
					    // 取得したネームリストをチェックボックスとしてレイアウトに追加
					
						CheckBox checkbox = new CheckBox(this);
						checkbox.setText(friend.getLastName() + " " + friend.getFirstName() + "（ " + friend.getEmail() + " ）\n");
						
						LinearLayout set = new LinearLayout(this);
						
						Button delbtn = new Button(this);
						delbtn.setText("削除");
						delbtn.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								AlertDialog.Builder builder = new AlertDialog.Builder(context);
							    builder.setTitle("確認").setMessage(friend.getEmail()+"の友人登録を解除しますか？")
							    		.setNegativeButton("はい", new DialogInterface.OnClickListener() {
							    			public void onClick(DialogInterface dialog, int whichButton) {
							    			  dialog.cancel();
							    			  MakeGroupActivity.dialog.show();
							    			  buffmail = friend.getEmail();
							    			  deleteFriend(buffmail);  			  
							    			}
							    		}).setPositiveButton("いいえ", new DialogInterface.OnClickListener() {
								              public void onClick(DialogInterface dialog, int id) {
									                dialog.cancel();                
								              }
									    });
							    AlertDialog alert = builder.create();
							    alert.show();
							}
						});
						
						set.addView(checkbox);
						set.addView(delbtn);
						
						layout.addView(set);
						Log.d("DEBUG", "added to view by " + friend.getLastName() + friend.getFirstName() + "さん");
					}
					
					// グループ作成ボタンを有効化
					makegroup_btn.setEnabled(true);
				
				// 友達がいない場合
				}else{
					
					// ラベルの表示
					Log.d("DEBUG", "友達がいません");
					TextView none = new TextView(context);
					none.setText("相互登録している友人がいません");
					layout.addView(none);
					
				}
				
				// 終了後、次にグループ一覧を取得する
				dialog.show();
				getGroupList();
				
			// 通信に失敗した場合
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
			    builder.setTitle("通信エラー")
			    		.setPositiveButton("再接続", new DialogInterface.OnClickListener() {
			    			public void onClick(DialogInterface dialog, int whichButton) {
			    			  dialog.cancel();
			    			  MakeGroupActivity.dialog.show();
			    			  getFriendList();   			  
			    			}
			    		}).setNegativeButton("戻る", new DialogInterface.OnClickListener() {
				              public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();                
					                MakeGroupActivity.this.finish();
				              }
					    });
			    AlertDialog alert = builder.create();
			    alert.show();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "エラー原因："+e.toString());
		}
	}

	/**
	 * GetGroupTask終了後の処理
	 */
	@Override
	public void onFinish(List<GroupV1Dto> result) {
		
		// 終了時に通信中...のダイアログを消す
		if(dialog != null){
			dialog.dismiss();
		}
		
		try {
			if (result != null) {
				if (result.size() > 0) {
					
					for(final GroupV1Dto group : result) {
						// 削除時のためにグループのKeyを保存
						groups.add(group.getKey());
						
						// 取得したグループをボタンとしてレイアウトに追加
						Button btn = new Button(this);
						btn.setText(group.getName());
						btn.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								// メンバ一覧を表示
								String msg = "";
								List<com.appspot.fragile_t.groupEndpoint.model.UserV1Dto> member = group.getUserlList();
								for(com.appspot.fragile_t.groupEndpoint.model.UserV1Dto user : member){
									msg += user.getLastName()+" "+user.getFirstName() + "（ " + user.getEmail() + " ）\n";
								}
								AlertDialog.Builder builder = new AlertDialog.Builder(context);
								builder.setTitle("メンバー").setMessage(msg)
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										dialog.cancel(); 			  
									}
								});
								AlertDialog alert = builder.create();
								alert.show();
							}
						});
						
						Button delbtn = new Button(this);
						delbtn.setText("削除");
						delbtn.setOnClickListener(new View.OnClickListener() {
							public void onClick(final View v) {
								AlertDialog.Builder builder = new AlertDialog.Builder(context);
							    builder.setTitle("確認").setMessage(group.getName()+"を削除しますか？")
							    		.setNegativeButton("はい", new DialogInterface.OnClickListener() {
							    			public void onClick(DialogInterface dialog, int whichButton) {
							    				dialog.cancel();
							    				((Button)v).setEnabled(false);
							    				MakeGroupActivity.dialog.show();
							    				buffkeyS = group.getKey();
												deleteGroup(buffkeyS); 
							    			}
							    		}).setPositiveButton("いいえ", new DialogInterface.OnClickListener() {
								              public void onClick(DialogInterface dialog, int id) {
									                dialog.cancel();                
								              }
									    });
							    AlertDialog alert = builder.create();
							    alert.show();
							}
						});
						
						LinearLayout setbtn = new LinearLayout(this);
						setbtn.addView(btn);
						setbtn.addView(delbtn);
						group_list.addView(setbtn);
					}
				
				// グループの数が0
				} else {
					TextView none = new TextView(context);
					none.setText("グループはありません");
					group_list.addView(none);
				}
				
			// 通信エラー時
			}else{
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
			    builder.setTitle("通信エラー")
			    		.setPositiveButton("再接続", new DialogInterface.OnClickListener() {
			    			public void onClick(DialogInterface dialog, int whichButton) {
			    			  dialog.cancel();
			    			  MakeGroupActivity.dialog.show();
			    			  getGroupList();   			  
			    			}
			    		}).setNegativeButton("戻る", new DialogInterface.OnClickListener() {
				              public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();                
					                MakeGroupActivity.this.finish();
				              }
					    });
			    AlertDialog alert = builder.create();
			    alert.show();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "エラー原因："+e.toString());
		}
	}
	
	/**
	 * DeleteGroupTask終了後の処理
	 */
	@Override
	public void onFinish(GroupResultV1Dto result) {
		// 終了時に通信中...のダイアログを消す
		if(dialog != null){
			dialog.dismiss();
		}
				
		try {
			if ((result != null)&&(result.getResult().equals(SUCCESS))) {
				
				// 一覧からグループのボタンを削除
				delGroupBtn(buffkeyS);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
			    builder.setTitle("通信成功").setMessage("グループを削除しました")
			    		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			    			public void onClick(DialogInterface dialog, int whichButton) {
			    				dialog.cancel();			  
			    			}
			    		});
			    AlertDialog alert = builder.create();
			    alert.show();

			// 通信失敗した場合
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
			    builder.setTitle("通信エラー")
			    		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			    			public void onClick(DialogInterface dialog, int whichButton) {
			    			  dialog.cancel();  			  
			    			}
			    		});
			    AlertDialog alert = builder.create();
			    alert.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "エラー原因："+e.toString());
		}
	}
	
	/**
	 * DeleteFriendTask終了後の処理
	 */
	@Override
	public void onFinish(FriendResultV1Dto result) {
		// 終了時に通信中...のダイアログを消す
		if(dialog != null){
			dialog.dismiss();
		}
		
		try{
			if((result != null)&&(result.getResult().equals(SUCCESS))){
				
				// その友人のチェックボックスを消す
				if(buffmail != null){
					delCheckMail(buffmail);
				}
				buffmail = null;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("通信成功").setMessage("友人登録を解除しました")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();			  
		    				}
		    			});
				AlertDialog alert = builder.create();
				alert.show();
		    
			// 通信失敗
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("通信エラー").setMessage("友人登録を解除できませんでした")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();			  
		    				}
		    			});
				AlertDialog alert = builder.create();
				alert.show();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DEBUG", "エラー原因："+e.toString());
		}
	}
	
	public class MakeGroupTask extends AsyncTask<Void, Void, Boolean> {
		
		ProgressDialog dialog;
		String error_message;
		
		@Override
		protected Boolean doInBackground(Void... args) {

			SharedPreferences pref = getSharedPreferences("user",
					Activity.MODE_PRIVATE);
			String email = pref.getString("email", "");

			StringListContainer container = new StringListContainer();
			container.setList(MakeGroupActivity.this.getCheckedMail());

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
				error_message = null;
				return false;
			}else if (SUCCESS.equals(result.getResult())) {
				error_message = result.getResult();
				return true;
			} else {
				error_message = result.getResult();
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
				ArrayList<String> names = getCheckedFriend();
				String message = "";

				for(int i=0;i<names.size();i++){
				message += "フレンド名：" + names.get(i) + "\r\n";
				}

				alertDialog.setTitle(title); //タイトル設定
				alertDialog.setMessage(message); //内容(メッセージ)設定
				alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
		    			  dialog.cancel();
		    			  MakeGroupActivity.this.finish();
		    			}
		    		});
				alertDialog.show();

			} else {
				Log.d("DEBUG", "グループ作成失敗");
				alertDialog.setTitle("グループ作成失敗"); //タイトル設定
				if (error_message==null){
					alertDialog.setMessage("通信エラー"); //内容(メッセージ)設定
				} else if(error_message.equals("nullname")) {
					alertDialog.setMessage("グループ名を設定して下さい"); //内容(メッセージ)設定
				} else if(error_message.equals("alreadygroup")) {
					alertDialog.setMessage("同じ名前のグループが存在します"); //内容(メッセージ)設定
				} else if(error_message.equals("nomember")) {
					alertDialog.setMessage("友人を選択して下さい"); //内容(メッセージ)設定
				} else if(error_message.equals("short")) {
					alertDialog.setMessage("グループ名が短すぎます"); //内容(メッセージ)設定
				} else if(error_message.equals("long")) {
					alertDialog.setMessage("グループ名が長すぎます"); //内容(メッセージ)設定
				} else {
					alertDialog.setMessage("通信エラー"); //内容(メッセージ)設定
				}
				alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();		  
					}
				});
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
