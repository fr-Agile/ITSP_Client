package jp.ac.titech.itpro.sds.fragile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class GoogleAccountRegistDialogBuilder extends AlertDialog.Builder {
	private Context context;

	public GoogleAccountRegistDialogBuilder(Context arg0) {
		super(arg0);
		this.context = arg0;
	}
	
	public AlertDialog.Builder setDefault() {
		return 
		this.setTitle("Googleアカウントが登録されていません。Googleアカウントを登録してください。")
		.setNegativeButton("いいえ",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				})
									
		.setPositiveButton("はい",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						GoogleAccountRegistDialogBuilder.this.context.startActivity(
							new Intent(android.provider.Settings.ACTION_SETTINGS));
					}
				});
	}

}
