package com.aek.callstatistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

public class CallLogCalculationsOld {

	private Context c;
	public long tmobile = 0;
	public long vip = 0;
	public long one = 0;
	public long wired = 0;
	public CallLogMinutes clm;

	public CallLogCalculationsOld(Context context) {
		c = context;
	}

	public boolean calculateMinutesMonthRange(Calendar cal) {
		// http://stackoverflow.com/questions/14677645/comparision-of-android-provider-calllog-date-fails
		// http://stackoverflow.com/questions/14660056/how-to-get-the-call-log-from-specific-date-in-android

		// Calendar cal = Calendar.getInstance();
		int month = cal.HOUR;
		cal.add(Calendar.HOUR, -24);
		Date resetDateDate = new Date(cal.getTimeInMillis());
		Calendar resetDateCal = Calendar.getInstance();
		resetDateCal.setTime(resetDateDate);
		String resetDate = String.valueOf(resetDateCal.getTimeInMillis());

		String selection = CallLog.Calls.TYPE + "=? AND " + CallLog.Calls.DATE
				+ " >=?";
		String[] selectionArgs = new String[] {
				(String.valueOf(CallLog.Calls.OUTGOING_TYPE)), (resetDate) };

		/* Query Call Log Content Provider */
		Cursor callLogCursor = c.getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI, null, selection,
				selectionArgs,
				android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);

		/* Check if cursor is not null */
		if (callLogCursor != null) {

			/* Loop through the cursor */
			while (callLogCursor.moveToNext()) {
				/* Get Contact Cache Number */
				String cacheNumber = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));

				/* Get Contact Number */
				String number = callLogCursor.getString(callLogCursor
						.getColumnIndex(CallLog.Calls.NUMBER));

				/* Get Call Duration */
				long duration = callLogCursor.getLong(callLogCursor
						.getColumnIndex(CallLog.Calls.DURATION));

				/* Get Call Type */
				int callType = callLogCursor.getInt(callLogCursor
						.getColumnIndex(CallLog.Calls.TYPE));
				
				/* Get Date */
				long dateTimeMillis = callLogCursor.getLong(callLogCursor
						.getColumnIndex(CallLog.Calls.DATE));
				Date date = new Date(dateTimeMillis);
				if(date.getHours() > month) break;
				
				if (cacheNumber == null)
					cacheNumber = number;

				String operator = Logs.checkOperator(cacheNumber);
				// String operator = "T-Mobile";

				if (operator.contentEquals("VIP")) {
					vip += duration;
				} else if (operator.contentEquals("T-Mobile")) {
					tmobile += duration;
				} else if (operator.contentEquals("ONE")) {
					one += duration;
				} else if (operator.contentEquals("Makedonski Telekom")) {
					wired += duration;
				} else if (operator.contentEquals("Static")) {
					wired += duration;
				} else if (operator.contentEquals("Unknown")) {
					wired += 0;
				}
			}
			callLogCursor.close();
			clm = new CallLogMinutes(tmobile/60, vip/60, one/60, wired/60);
			return true;
		}
		return false;
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
