package jp.ac.titech.itpro.sds.fragile.utils;

import java.util.ArrayList;

import jp.ac.titech.itpro.sds.fragile.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DayAdapter extends ArrayAdapter<String> {
	private ArrayList<String> calendars = new ArrayList<String>();
	private LayoutInflater inflater;
	private int layout;

	public DayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layout = textViewResourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (convertView == null) {
			view = this.inflater.inflate(this.layout, null);
		}

		String calendar = this.calendars.get(position);

		((TextView)view.findViewById(R.id.time_text)).setText(calendar);
		view.setBackgroundResource(R.drawable.time_back);
		
		return view;
	}

	@Override
	public void add(String calendar) {
		super.add(calendar);
		this.calendars.add(calendar);
	}

	@Override
	public void clear() {
		super.clear();
		this.calendars.clear();
	}
	
	
}
