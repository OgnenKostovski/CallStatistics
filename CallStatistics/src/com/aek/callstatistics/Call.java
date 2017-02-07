package com.aek.callstatistics;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.provider.CallLog;

public class Call implements Serializable{
	//http://developer.samsung.com/android/technical-docs/CallLogs-in-Android
	
	private String number; //The phone number as the user entered it.
	private String type;  //The type of the call (incoming, outgoing or missed).
	private long duration;  //The duration of the call in seconds
	private Date date;  //The date the call occurred in milliseconds since an epoch (String?)
	private String name;  //The cached name associated with the phone number, if it exists.
	private String operator;
	
	public Call(String name, String number, Date date, long duration, int callType) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.number = number;
		this.date = date;
		this.duration = duration; // in seconds
		if(callType == CallLog.Calls.INCOMING_TYPE) type = "Incoming";
		else if(callType == CallLog.Calls.OUTGOING_TYPE) type = "Outgoing";
		else if(callType == CallLog.Calls.MISSED_TYPE) type = "Missed";
		//operator = Logs.checkOperatorAsync(number);
		operator = "";
	}
	
	/*Contact Name*/
	public String getName() {
	 return name;
	}

	/*Contact Number*/
	public String getNumber() {
	 return number;
	}
		
	/*Duration of call*/
	public String getDuration() {
	 return Long.toString(duration);
	}
		
	/*Date of call*/
	public String getDate() {
		SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy  hh:mm a");
		//String dateString = format.format(Date.parse(date.toString()));
		String dateString = format.format(date);
	 	return dateString;
	}
	
	public String getOperator(){
		return operator;
	}
	
	public void setOperator(String Operator){
		operator = Operator;
	}
}
