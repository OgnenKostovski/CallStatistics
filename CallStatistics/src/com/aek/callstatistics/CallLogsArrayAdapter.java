package com.aek.callstatistics;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CallLogsArrayAdapter extends ArrayAdapter<Call>{
//http://www.softwarepassion.com/android-series-custom-listview-items-and-adapters/
	
	private ArrayList<Call> items;
	Context c;
	
	public CallLogsArrayAdapter(Context context, int textViewResourceId,
			ArrayList<Call> objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
		//items = new ArrayList(objects.subList(0, 5));
		items = objects;
		c = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.call_row, null);
		final Call call = items.get(position);
		if(call != null){
			TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
			TextView tvNumber = (TextView) convertView.findViewById(R.id.tvNumber);
			TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);
			TextView tvDuration = (TextView) convertView.findViewById(R.id.tvDuration);
			TextView tvOperator = (TextView) convertView.findViewById(R.id.tvOperator);
			
			tvName.setText(call.getName());
			tvNumber.setText(call.getNumber());
			tvDate.setText(call.getDate().toString());
			tvDuration.setText(call.getDuration().toString());
			tvOperator.setText(call.getOperator());
		}
		
		return convertView;
	}

	
}
