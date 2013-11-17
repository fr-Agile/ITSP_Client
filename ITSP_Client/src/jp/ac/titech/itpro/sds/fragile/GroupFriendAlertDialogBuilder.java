package jp.ac.titech.itpro.sds.fragile;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.appspot.fragile_t.getFriendEndpoint.model.UserV1Dto;
import com.appspot.fragile_t.groupEndpoint.model.GroupV1Dto;

public class GroupFriendAlertDialogBuilder extends AlertDialog.Builder {
	private Context context;
	public GroupFriendAlertDialogBuilder(Context arg0) {
		super(arg0);
		context = arg0;
	}

	public class MyAdapter extends ArrayAdapter<String> {
		private LayoutInflater inflater;
		private int layoutId;
		private List<GroupV1Dto> groupList;	// 表示するグループのリスト
		private int[] groupFlags;		// チェックボックスの状態を保存するフラグ
		private List<UserV1Dto> friendList;	// 表示するフレンドのリスト
		private int[] friendFlags;		// チェックボックスの状態を保存するフラグ
		
		private final static int GROUP_LINE = 1; // "Group"と表示するための行数
		private final static int FRIEND_LINE = 1; // "Friend"と表示するための行数
		
		public MyAdapter(Context context, int layoutId, 
				List<GroupV1Dto> groupList, int[] groupFlags,
				List<UserV1Dto> friendList, int[] friendFlags) {
			super(context, 0);
			
			this.add("Group");
			for (GroupV1Dto group : groupList) {
				this.add(group.getName());
			}
			this.add("Friend");
			for (UserV1Dto friend : friendList) {
				this.add(friend.getLastName() + " " + friend.getFirstName());
			}
			
			this.inflater = (LayoutInflater) 
					context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.layoutId = layoutId;
			
			this.groupList = groupList;
			this.groupFlags = groupFlags;
			this.friendList = friendList;
			this.friendFlags = friendFlags;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = inflater.inflate(layoutId, null);
			} 
			TextView textView = (TextView) view.findViewById(R.id.checkbox_text);
			textView.setText(this.getItem(position));
			final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox_check); 
			final CheckBox checkBoxImportant = (CheckBox) view.findViewById(R.id.checkbox_important); 
			final int p = position;
			final int lines_of_groups = GROUP_LINE + groupList.size();
			
			// 行によって表示する内容(View)を変える
			if (p < GROUP_LINE) {
				// "Group"と表示する行
				view.setBackgroundColor(context.getResources().getColor(R.color.base_light));
				view.findViewById(R.id.check_text).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.important_text).setVisibility(View.INVISIBLE);
				TextView checkText = (TextView)view.findViewById(R.id.checkbox_text);
				checkText.setTextColor(context.getResources().getColor(R.color.light_gray));
				
				checkBox.setVisibility(View.INVISIBLE);
				checkBoxImportant.setVisibility(View.INVISIBLE);
			} else if (p >= lines_of_groups && p < lines_of_groups + FRIEND_LINE) {
				// "Friend"と表示する行
				view.setBackgroundColor(context.getResources().getColor(R.color.base_light));
				view.findViewById(R.id.check_text).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.important_text).setVisibility(View.INVISIBLE);
				TextView checkText = (TextView)view.findViewById(R.id.checkbox_text);
				checkText.setTextColor(context.getResources().getColor(R.color.light_gray));

				checkBox.setVisibility(View.INVISIBLE);
				checkBoxImportant.setVisibility(View.INVISIBLE);
				
			} else {
				view.setBackgroundColor(Color.WHITE);
				checkBox.setVisibility(View.VISIBLE);
				checkBoxImportant.setVisibility(View.VISIBLE);

				// 必ずsetChecked前にリスナに登録(convertView != nullの場合は既に別行用のリスナが登録されている)
				checkBox.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean isChecked = checkBox.isChecked();
						
						if (p < lines_of_groups) {
							// groupのチェックが変更されたとき
							groupFlags[p - GROUP_LINE] = isChecked ? 1 : 0;
							// groupに属するfriendのチェックも変更する
							GroupV1Dto group = groupList.get(p - GROUP_LINE);
							for (com.appspot.fragile_t.groupEndpoint.model.UserV1Dto member 
									: group.getUserlList()) {
								for (UserV1Dto friend : friendList) {
									if (member.getEmail().equals(friend.getEmail())) {
										int friendIndex = friendList.indexOf(friend);
										if (isChecked) {
											friendFlags[friendIndex] = 1;
										} else if (!checkedByGroup(friend)) {
											// 他のグループにチェックされてなければ
											// チェックを外す
											friendFlags[friendIndex] = 0;
										}
									}
								}
							}
						} else {
							// friendのチェックが変更されたとき
							int index = p - (lines_of_groups + FRIEND_LINE);
							friendFlags[index] = isChecked ? 1 : 0;
							// falseの場合、friendの属するgroupのチェックを外す
							if (isChecked == false) {
								UserV1Dto friend = friendList.get(index);
								for (GroupV1Dto group : groupList) {
									for (com.appspot.fragile_t.groupEndpoint.model.UserV1Dto member 
											: group.getUserlList()) {
										if (member.getEmail().equals(friend.getEmail())) {
											int groupIndex = groupList.indexOf(group);
											groupFlags[groupIndex] = 0;
											break;
										}
									}
								}
							}
						}
						MyAdapter.this.notifyDataSetChanged();
					}
				});
				
				checkBoxImportant.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean isChecked = checkBoxImportant.isChecked();
						
						if (p < lines_of_groups) {
							// groupのチェックが変更されたとき
							groupFlags[p - GROUP_LINE] = isChecked ? 2 : 0;
							// groupに属するfriendのチェックも変更する
							GroupV1Dto group = groupList.get(p - GROUP_LINE);
							for (com.appspot.fragile_t.groupEndpoint.model.UserV1Dto member 
									: group.getUserlList()) {
								for (UserV1Dto friend : friendList) {
									if (member.getEmail().equals(friend.getEmail())) {
										int friendIndex = friendList.indexOf(friend);
										if (isChecked) {
											friendFlags[friendIndex] = 2;
										} else if (!checkedByGroup(friend)) {
											// 他のグループにチェックされてなければ
											// チェックを外す
											friendFlags[friendIndex] = 0;
										}
									}
								}
							}
						} else {
							// friendのチェックが変更されたとき
							int index = p - (lines_of_groups + FRIEND_LINE);
							friendFlags[index] = isChecked ? 2 : 0;
							// falseの場合、friendの属するgroupのチェックを外す
							if (isChecked == false) {
								UserV1Dto friend = friendList.get(index);
								for (GroupV1Dto group : groupList) {
									for (com.appspot.fragile_t.groupEndpoint.model.UserV1Dto member 
											: group.getUserlList()) {
										if (member.getEmail().equals(friend.getEmail())) {
											int groupIndex = groupList.indexOf(group);
											groupFlags[groupIndex] = 0;
											break;
										}
									}
								}
							}
						}
						MyAdapter.this.notifyDataSetChanged();
					}
				});

				if (p < lines_of_groups) {
					checkBox.setChecked(groupFlags[p - GROUP_LINE] > 0);
					checkBoxImportant.setChecked(groupFlags[p - GROUP_LINE] > 1);
				} else {
					checkBox.setChecked(friendFlags[p - (lines_of_groups + FRIEND_LINE)] > 0);
					checkBoxImportant.setChecked(friendFlags[p - (lines_of_groups + FRIEND_LINE)] > 1);
				}
			}
			return view;
		}
		
		private boolean checkedByGroup(UserV1Dto friend) {
			for (int i = 0; i < groupFlags.length; i++) {
				if (groupFlags[i] > 0) {
					for (com.appspot.fragile_t.groupEndpoint.model.UserV1Dto member : 
						groupList.get(i).getUserlList()) {
						if (friend.getEmail().equals(member.getEmail())) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}
	
	public Builder setGroupAndFriend(
			final List<GroupV1Dto> groupList, final int[] groupFlags,
			final List<UserV1Dto> friendList, final int[] friendFlags) {
		
		assert (groupList.size() == groupFlags.length) ;
		assert (friendList.size() == friendFlags.length) ;
		
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		View view = inflater.inflate(R.layout.dialog_groupfriend, null);
		final ListView listView = (ListView) view.findViewById(R.id.dialog_gf_list);
		MyAdapter adapter = new MyAdapter(this.getContext(), R.layout.checkbox_textview,
				groupList, groupFlags, friendList, friendFlags);
		listView.setAdapter(adapter);
	
		return super.setView(view);
		
	}
}
