package com.aek.callstatistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.CallLog;

public class CallLogCalculations {

	private Context context;
	public long tmobile = 0;
	public long vip = 0;
	public long one = 0;
	public long wired = 0;
	public CallLogMinutes clm;
	public ArrayList<Call> callLog, callLog1, callLog2, callLog3, callLog4;
	public int month;
	public Intent intent;
	public Handler handler;
	public Activity activity;
	private TariffStats selectedTariffStats, myTariffStats;
	public int smsCount, smsCount1, smsCount2, smsCount3, smsCount4;
	public double smsBill, smsBill1, smsBill2, smsBill3, smsBill4;
	public ArrayList<Double> smsBillz;
	public double mySmsBill, mySmsBill1, mySmsBill2, mySmsBill3, mySmsBill4;
	public ArrayList<Double> mySmsBillz;

	public CallLogCalculations(Context c, Intent i, Handler h, Activity a,
			TariffStats tariffStats) {
		context = c;
		callLog = new ArrayList<Call>();
		callLog1 = new ArrayList<Call>();
		callLog2 = new ArrayList<Call>();
		callLog3 = new ArrayList<Call>();
		callLog4 = new ArrayList<Call>();
		intent = i;
		handler = h;
		activity = a;
		selectedTariffStats = tariffStats;
		myTariffStats = null;
		smsCount = smsCount1 = smsCount2 = smsCount3 = smsCount4 = 0;
		smsBill = smsBill1 = smsBill2 = smsBill3 = smsBill4 = 0;
		smsBillz = new ArrayList<Double>();
	}

	public CallLogCalculations(Context c, Intent i, Handler h, Activity a,
			TariffStats tariffStats, TariffStats myTariffStats) {
		context = c;
		callLog = new ArrayList<Call>();
		callLog1 = new ArrayList<Call>();
		callLog2 = new ArrayList<Call>();
		callLog3 = new ArrayList<Call>();
		callLog4 = new ArrayList<Call>();
		intent = i;
		handler = h;
		activity = a;
		selectedTariffStats = tariffStats;
		this.myTariffStats = myTariffStats;
		smsCount = smsCount1 = smsCount2 = smsCount3 = smsCount4 = 0;
		smsBill = smsBill1 = smsBill2 = smsBill3 = smsBill4 = 0;
		smsBillz = new ArrayList<Double>();

		mySmsBill = mySmsBill1 = mySmsBill2 = mySmsBill3 = mySmsBill4 = 0;
		mySmsBillz = new ArrayList<Double>();
	}

	public boolean calculateMinutesMonthRange(Calendar cal) {
		// http://stackoverflow.com/questions/14677645/comparision-of-android-provider-calllog-date-fails
		// http://stackoverflow.com/questions/14660056/how-to-get-the-call-log-from-specific-date-in-android

		// ****Calculate smsBillz ( update )
		calculateSMSMonthRange(cal);

		// Calendar cal = Calendar.getInstance();
		month = cal.get(cal.MONTH);
		cal.add(cal.MONTH, -4);
		cal.set(cal.DAY_OF_MONTH, 1);
		Date resetDateDate = new Date(cal.getTimeInMillis());
		Calendar resetDateCal = Calendar.getInstance();
		resetDateCal.setTime(resetDateDate);
		String resetDate = String.valueOf(resetDateCal.getTimeInMillis());

		String selection = CallLog.Calls.TYPE + "=? AND " + CallLog.Calls.DATE
				+ " >=?";
		String[] selectionArgs = new String[] {
				(String.valueOf(CallLog.Calls.OUTGOING_TYPE)), (resetDate) };

		/* Query Call Log Content Provider */
		Cursor callLogCursor = context.getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI, null, selection,
				selectionArgs,
				android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);

		/* Check if cursor is not null */
		if (callLogCursor != null) {

			/* Loop through the cursor */
			while (callLogCursor.moveToNext()) {

				/* Get ID of call */
				String id = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls._ID));

				/* Get Contact Name */
				String name = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls.CACHED_NAME));

				/* Get Contact Cache Number */
				String cacheNumber = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));

				/* Get Contact Number */
				String number = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls.NUMBER));

				/* Get Date and time information */
				long dateTimeMillis = callLogCursor.getLong(callLogCursor
						.getColumnIndex(CallLog.Calls.DATE));
				Date date = new Date(dateTimeMillis);
				long duration = callLogCursor.getLong(callLogCursor
						.getColumnIndex(CallLog.Calls.DURATION));

				/* Get Call Type */
				int callType = callLogCursor.getInt(callLogCursor
						.getColumnIndex(CallLog.Calls.TYPE));

				if (cacheNumber == null)
					cacheNumber = number;

				if (cacheNumber.startsWith("+389"))
					cacheNumber = cacheNumber.replace("+389", "0");
				if (cacheNumber.startsWith("00389"))
					cacheNumber = cacheNumber.replace("00389", "0");

				if (cacheNumber.length() != 9)
					continue;

				if (name == null)
					name = "No Name";

				/* Create Model Object */
				Call call = new Call(name, cacheNumber, date, duration,
						callType);

				updateCallLog(call, date);

			}
			callLogCursor.close();
			callLogUpdateOperator();

			// clm = new CallLogMinutes(tmobile / 60, vip / 60, one / 60,
			// wired / 60);
			return true;
		}
		return false;
	}

	public void calculateSMSMonthRange(Calendar cal) {
		smsCount = smsCount1 = smsCount2 = smsCount3 = smsCount4 = 0;
		smsBill = smsBill1 = smsBill2 = smsBill3 = smsBill4 = 0;
		if (myTariffStats != null) {
			mySmsBill = mySmsBill1 = mySmsBill2 = mySmsBill3 = mySmsBill4 = 0;
		}

		month = cal.get(cal.MONTH);
		cal.add(cal.MONTH, -4);
		cal.set(cal.DAY_OF_MONTH, 1);
		Date resetDateDate = new Date(cal.getTimeInMillis());
		Calendar resetDateCal = Calendar.getInstance();
		resetDateCal.setTime(resetDateDate);
		String resetDate = String.valueOf(resetDateCal.getTimeInMillis());

		String selection = "type" + "=? AND " + "date >=?";
		String[] selectionArgs = new String[] { ("2"), (resetDate) };

		Uri uriSMSURI = Uri.parse("content://sms");
		Cursor cur = context.getContentResolver().query(uriSMSURI, null,
				selection, selectionArgs, "date ASC");
		/* Check if cursor is not null */
		if (cur != null) {

			/* Loop through the cursor */
			while (cur.moveToNext()) {

				/* Get Contact Number */
				String number = cur.getString(cur.getColumnIndex("address"));

				/* Get Dateinformation */
				long dateTimeMillis = cur.getLong(cur.getColumnIndex("date"));
				Date date = new Date(dateTimeMillis);

				updateSMSCount(date);
			}
		}

		smsBill1 = calculateSMSBill(smsCount1, selectedTariffStats);
		smsBill2 = calculateSMSBill(smsCount2, selectedTariffStats);
		smsBill3 = calculateSMSBill(smsCount3, selectedTariffStats);
		smsBill4 = calculateSMSBill(smsCount4, selectedTariffStats);
		smsBill = calculateSMSBill(smsCount, selectedTariffStats);

		smsBillz.add(smsBill);
		smsBillz.add(smsBill1);
		smsBillz.add(smsBill2);
		smsBillz.add(smsBill3);
		smsBillz.add(smsBill4);

		if (myTariffStats != null) {
			mySmsBill1 = calculateSMSBill(smsCount1, myTariffStats);
			mySmsBill2 = calculateSMSBill(smsCount2, myTariffStats);
			mySmsBill3 = calculateSMSBill(smsCount3, myTariffStats);
			mySmsBill4 = calculateSMSBill(smsCount4, myTariffStats);
			mySmsBill = calculateSMSBill(smsCount, myTariffStats);

			mySmsBillz.add(mySmsBill);
			mySmsBillz.add(mySmsBill1);
			mySmsBillz.add(mySmsBill2);
			mySmsBillz.add(mySmsBill3);
			mySmsBillz.add(mySmsBill4);
		}
	}

	private long calculateSMSBill(int countSMS, TariffStats tariffStats) {
		long result = 0;
		if (tariffStats.freeSMS < countSMS) {
			result += (countSMS - tariffStats.freeSMS) * tariffStats.sms;
		}
		return result;
	}

	private void updateSMSCount(Date date) {
		Calendar callDate = Calendar.getInstance();
		callDate.setTime(date);

		int month1 = month - 1;
		int month2 = month - 2;
		int month3 = month - 3;
		int month4 = month - 4;

		if (month1 < 0)
			month1 += 12;
		if (month2 < 0)
			month2 += 12;
		if (month3 < 0)
			month3 += 12;
		if (month4 < 0)
			month4 += 12;

		if (callDate.get(callDate.MONTH) == month1) {
			smsCount1++;
		} else if (callDate.get(callDate.MONTH) == month2) {
			smsCount2++;
		} else if (callDate.get(callDate.MONTH) == month3) {
			smsCount3++;
		} else if (callDate.get(callDate.MONTH) == month4) {
			smsCount4++;
		} else if (callDate.get(callDate.MONTH) == month) {
			smsCount++;
		}
	}

	private void callLogUpdateOperator() {
		/*
		 * Intent newIntent = new Intent(context, CheckOperatorService.class);
		 */
		// Create a new Messenger for the communication back
		Messenger messenger = new Messenger(handler);
		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("callLog1", callLog1);
		intent.putExtra("callLog2", callLog2);
		intent.putExtra("callLog3", callLog3);
		intent.putExtra("callLog4", callLog4);
		intent.putExtra("callLog", callLog);
		intent.putExtra("ClassPath",
				context.getPackageName() + "." + activity.getLocalClassName());
		intent.putExtra("tariffStats", selectedTariffStats);
		intent.putExtra("smsBillz", smsBillz);
		if(myTariffStats != null){
			intent.putExtra("myTariffStats", myTariffStats);
			intent.putExtra("mySmsBillz", mySmsBillz);
		}
		context.startService(intent);
		// tvInputOperator.setText("FTW");

		// soapCheckOperator();
	}

	private void updateCallLog(Call call, Date date) {
		Calendar callDate = Calendar.getInstance();
		callDate.setTime(date);

		int month1 = month - 1;
		int month2 = month - 2;
		int month3 = month - 3;
		int month4 = month - 4;

		if (month1 < 0)
			month1 += 12;
		if (month2 < 0)
			month2 += 12;
		if (month3 < 0)
			month3 += 12;
		if (month4 < 0)
			month4 += 12;

		if (callDate.MONTH == month1) {
			callLog1.add(call);
		} else if (callDate.MONTH == month2) {
			callLog2.add(call);
		} else if (callDate.MONTH == month3) {
			callLog3.add(call);
		} else if (callDate.MONTH == month4) {
			callLog4.add(call);
		} else if (callDate.MONTH == month) {
			callLog.add(call);
		}
	}

	public long calculateBill(Calendar cal, TariffStats tariffStats) {
		if (calculateMinutesMonthRange(cal)) {
			return calculateForTariff(tariffStats);
		}
		return 0;
	}

	private long calculateForTariff(TariffStats tariffStats) {
		if (tariffStats.operator.contentEquals("T-Mobile")) {
			calculateForTMobile(tariffStats);
		} else if (tariffStats.operator.contentEquals("VIP")) {
			calculateForVIP(tariffStats);
		} else if (tariffStats.operator.contentEquals("ONE")) {
			calculateForONE(tariffStats);
		}
		return calculation(tariffStats);
	}

	private void calculateForONE(TariffStats tariffStats) {
		clm.one -= tariffStats.freeOperator;
		if (clm.one < 0)
			clm.one = 0;
	}

	private void calculateForVIP(TariffStats tariffStats) {
		clm.vip -= tariffStats.freeOperator;
		if (clm.vip < 0)
			clm.vip = 0;
	}

	private void calculateForTMobile(TariffStats tariffStats) {
		clm.tmobile -= tariffStats.freeOperator;
		if (clm.tmobile < 0)
			clm.tmobile = 0;
	}

	private long calculation(TariffStats tariffStats) {
		long bill = 0;
		bill += clm.one * tariffStats.callOne;
		bill += clm.vip * tariffStats.callVip;
		bill += clm.tmobile * tariffStats.callTmobile;
		bill += tariffStats.subscription;
		return bill;
	}
}
