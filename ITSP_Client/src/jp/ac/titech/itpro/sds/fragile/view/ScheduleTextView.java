package jp.ac.titech.itpro.sds.fragile.view;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ScheduleTextView extends TextView implements OnClickListener {
	private final String mKey;

	public ScheduleTextView(Context context, String key) {
		super(context);
		this.mKey = key;
	}

	public ScheduleTextView(Context context) {
		this(context, "");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
