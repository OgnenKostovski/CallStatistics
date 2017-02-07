package com.aek.callstatistics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.CallLog;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {

	private ArrayList<Call> callLog;
	private int countSMS;
	private String myTariff;
	private TariffStats myTariffStats;
	private AppPreferences appPrefs;
	private CallLogMinutes clmNotifications;

	private static String SOAP_ACTION = "http://www.aek.mk/checknumbersstring";
	private static String NAMESPACE = "http://www.aek.mk/";
	private static String METHOD_NAME = "checknumbersstring";
	private static String URL = "http://www.aek.mk/ServiceNP1/checkNumbersOperator_WS.asmx?op=checknumbersstring";

	private int smsLeft = 0;
	private long freeLeft = 0;
	private long freeOperatorLeft = 0;

	AppWidgetManager appWidgetManager;
	int[] allWidgetIds;
	Intent i;
	int sId;

	private class DownloadWebPageTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			/*
			 * callLog = new ArrayList(); countSMS = 0; appPrefs = new
			 * AppPreferences(getApplicationContext());
			 * 
			 * if (appPrefs.isSaved()) { myTariff = appPrefs.getTariff();
			 * getCallLog(Calendar.getInstance()); getTariffStats(myTariff); }
			 */
			try {
				getTariffStats(myTariff);
				finishOnStart();
				return null;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			//finishOnStart();
		}
	}

	// private DownloadWebPageTask downloadAsync = new Download

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		i = intent;
		sId = startId;

		Parse.initialize(this, "zwj9F0yhJzwJBlpqCuZm4oqoCxnBXXd8tcXVTxlc",
				"HQPhZjeDvXPriVAHaaanRD47pROHRKhnqO8n6iVh");

		Log.i("UWS", "Called");
		callLog = new ArrayList();
		countSMS = 0;
		appPrefs = new AppPreferences(getApplicationContext());

		appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		allWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				MyWidgetProvider.class);
		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		Log.w("UWS:", "From Intent" + String.valueOf(allWidgetIds.length));
		Log.w("UWS", "Direct" + String.valueOf(allWidgetIds2.length));

		if (appPrefs.isSaved()) {
			myTariff = appPrefs.getTariff();
			getCallLog(Calendar.getInstance()); // and update(countSMS)
			getTariffStats(myTariff);// call callLogUpdate(callLog)
			// AND CALCULATE NOTIFICATIONS
			// update(clmNotifications)
			// AND udateNotificationVariables()
			//new DownloadWebPageTask().execute("");
			//finishOnStart();
		}

		/*
		 * for (int widgetId : allWidgetIds) {
		 * 
		 * // Create some random data int number = (new Random().nextInt(100));
		 * 
		 * RemoteViews remoteViews = new RemoteViews(this
		 * .getApplicationContext().getPackageName(), R.layout.widget_layout);
		 * Log.w("WidgetExample", "NOVO"); // Set the text
		 * remoteViews.setTextViewText(R.id.tvSMSLeft, String.valueOf(number));
		 * 
		 * // Register an onClickListener Intent clickIntent = new
		 * Intent(this.getApplicationContext(), MyWidgetProvider.class);
		 * 
		 * clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		 * clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
		 * allWidgetIds);
		 * 
		 * PendingIntent pendingIntent = PendingIntent.getBroadcast(
		 * getApplicationContext(), 0, clickIntent,
		 * PendingIntent.FLAG_UPDATE_CURRENT);
		 * remoteViews.setOnClickPendingIntent(R.id.tvSMSLeft, pendingIntent);
		 * appWidgetManager.updateAppWidget(widgetId, remoteViews); }
		 * stopSelf();
		 * 
		 * super.onStart(intent, startId);
		 */
	}

	private void finishOnStart() {
		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(this
					.getApplicationContext().getPackageName(),
					R.layout.widget_layout);
			Log.w("WidgetExample", "NOVO");
			// Set the text
			remoteViews.setTextViewText(R.id.tvSMSLeft,
					"SMS left:" + String.valueOf(smsLeft));
			remoteViews.setTextViewText(R.id.tvOtherOperatorMinutesLeft,
					"Mins to other:" + String.valueOf(freeLeft));
			remoteViews.setTextViewText(
					R.id.tvOperatorMinutesLeft,
					"Mins to " + appPrefs.getOperator() + ":"
							+ String.valueOf(freeOperatorLeft));

			// Register an onClickListener
			Intent clickIntent = new Intent(this.getApplicationContext(),
					MyWidgetProvider.class);

			clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					allWidgetIds);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					getApplicationContext(), 0, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.tvSMSLeft, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		stopSelf();

		UpdateWidgetService.super.onStart(i, sId);
	}

	private void updateNotificationVariables() {
		if (myTariffStats.freeSMS > countSMS)
			smsLeft = myTariffStats.freeSMS - countSMS;
		if (clmNotifications.free > 0)
			freeLeft = clmNotifications.free;
		if (clmNotifications.freeOperator > 0)
			freeOperatorLeft = clmNotifications.freeOperator;
	}

	private CallLogMinutes calculateNotifications(ArrayList<Call> callLog,
			TariffStats tariffStats) {
		CallLogMinutes clm = new CallLogMinutes(0, 0, 0, 0);

		// Dodadi besplatni minuti
		clm.freeOperator = tariffStats.freeOperator;
		clm.free += tariffStats.freeOther;

		for (Call call : callLog) {

			// Ako ne e na ist operator
			if (!((call.getOperator().contentEquals("Т-Мобиле Македонија") && tariffStats.operator
					.contentEquals("T-Mobile"))
					|| (call.getOperator().contentEquals("Космофон") && tariffStats.operator
							.contentEquals("ONE")) || (call.getOperator()
					.contentEquals("ВИП Оператор") && tariffStats.operator
					.contentEquals("VIP")))) {
				if (clm.free > 0) {
					clm.free -= Double.parseDouble(call.getDuration()) / 60;
					if (clm.free < 0) // Drug PAT DODADI OSTATOK
						clm.free = 0;
					continue;
				}
				// Na ist operator
			} else if ((call.getOperator().contentEquals("Т-Мобиле Македонија") && tariffStats.operator
					.contentEquals("T-Mobile"))
					|| (call.getOperator().contentEquals("Космофон") && tariffStats.operator
							.contentEquals("ONE"))
					|| (call.getOperator().contentEquals("ВИП Оператор") && tariffStats.operator
							.contentEquals("VIP"))) {
				if (clm.freeOperator > 0) {
					clm.freeOperator -= Double.parseDouble(call.getDuration()) / 60;
					if (clm.freeOperator < 0) // Drug PAT DODADI OSTATOK
						clm.freeOperator = 0;
					continue;
				}
			}

			// Ako nema besplatni minuti ( ili free ili freeOperator )
			if (call.getOperator().contentEquals("Т-Мобиле Македонија")) {
				clm.tmobile += Double.parseDouble(call.getDuration()) / 60;
			} else if (call.getOperator().contentEquals("Космофон")) {
				clm.one += Double.parseDouble(call.getDuration()) / 60;
			} else if (call.getOperator().contentEquals("ВИП Оператор")) {
				clm.vip += Double.parseDouble(call.getDuration()) / 60;
			} else if (call.getOperator().contentEquals("Македонски Телеком")) {
				clm.wired += Double.parseDouble(call.getDuration()) / 60;
			}
		}

		return clm;
	}

	public void callLogUpdate(ArrayList<Call> CallLog) {
		// ArrayList<Call> callLog = (ArrayList<Call>)
		// extras.getSerializable("callLog");
		List<Call> tempLog;
		for (int i = 0; i < CallLog.size(); i++) {
			if (i % 30 == 29 || i + 1 == CallLog.size()) {
				// poseben sluchaj i == 0 (Broadcast Recever)!! (CallLog.size()
				// == 1). 0/30 =>exception, ISTO I ZA i=60,callLog.size()= 61
				if (i + 1 == CallLog.size()) {
					tempLog = CallLog.subList(i, i + 1);
				} else {
					tempLog = CallLog.subList(((int) (i / 30)) * 30, i);
				}
				String numbers = "";
				int j = 0;
				for (Call call : tempLog) {
					numbers += call.getNumber();
					if (++j != tempLog.size()) {
						numbers += ",";
					}
				}
				if (numbers.contains(""))
					Log.i("WS", "i: " + i + "CallLog.size(): " + CallLog.size());
				SoapObject phonebook = soapGetOperators(numbers);
				updateOperators(tempLog, phonebook);
			}
		}
	}

	public SoapObject soapGetOperators(String numbers) {
		// Initialize soap request + add parameters
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		// Add parameters
		PropertyInfo pinfo = new PropertyInfo();
		pinfo.setName("auth");
		pinfo.setValue("CallStats10_r5eLJfAWT9G5m6TU");
		pinfo.setType(String.class);
		request.addProperty(pinfo);
		// request.addProperty("key", "CallStats10");
		// request.addProperty("secret", "r5eLJfAWT9G5m6TU");
		PropertyInfo pi = new PropertyInfo();
		pi.setName("numbers");
		pi.setValue(numbers);
		pi.setType(String.class);
		request.addProperty(pi);
		Log.d("WS", numbers);
		// Declare the version of the SOAP request
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER12);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		// Needed to make the internet call
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		try {
			// this is the actual part that will call the webservice
			androidHttpTransport.call(SOAP_ACTION, envelope);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Get the SoapResult from the envelope body.
		if (envelope.bodyIn instanceof SoapFault) {
			String str = ((SoapFault) envelope.bodyIn).faultstring;
			Log.i("", str);
		} else {
			SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
			Log.d("WS", String.valueOf(resultsRequestSOAP));
		}
		SoapObject result = (SoapObject) envelope.bodyIn;
		SoapObject result1 = (SoapObject) result.getProperty(0);
		SoapObject phonebook = (SoapObject) result1.getProperty(0);
		SoapObject contact = (SoapObject) phonebook.getProperty(0);

		return phonebook;
	}

	public void updateOperators(List<Call> tempLog, SoapObject phonebook) {
		for (Call call : tempLog) {
			int status = 0;
			Date date = new Date();
			for (int i = 0; i < phonebook.getPropertyCount(); i++) {
				SoapObject contact = (SoapObject) phonebook.getProperty(i);
				if (call.getNumber().contentEquals(
						"0" + contact.getPropertyAsString("celBroj"))) {
					if (contact.getPropertyAsString("status")
							.contentEquals("0")) {
						call.setOperator("");
						status = 0;
					} else if (contact.getPropertyAsString("status")
							.contentEquals("1")) {
						call.setOperator(contact
								.getPropertyAsString("dodelenNa"));
						status = 1;
					} else if (contact.getPropertyAsString("status")
							.contentEquals("2")) {
						DateFormat dateFormat = new SimpleDateFormat(
								"mm/dd/yyyy HH:mm:ss a");
						if (status == 2) {
							Date newDate = new Date();
							try {
								newDate = (Date) dateFormat.parse(contact
										.getPropertyAsString("prefrlenDatum"));
							} catch (java.text.ParseException e) {
								e.printStackTrace();
							}
							if (newDate.compareTo(date) > 0) {
								date = newDate;
								call.setOperator(contact
										.getPropertyAsString("prefrlenVo"));
							}
						} else {
							status = 2;
							try {
								date = (Date) dateFormat.parse(contact
										.getPropertyAsString("prefrlenDatum"));
							} catch (java.text.ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							call.setOperator(contact
									.getPropertyAsString("prefrlenVo"));
						}
					}
				}
			}
		}
	}

	private void getCallLog(Calendar cal) {
		calculateSMSMonthRange(cal);

		// Calendar cal = Calendar.getInstance();
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
		Cursor callLogCursor = this.getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI, null, selection,
				selectionArgs,
				android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);

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

				callLog.add(call);
			}
			callLogCursor.close();
		}
	}

	private void calculateSMSMonthRange(Calendar cal) {
		cal.set(cal.DAY_OF_MONTH, 1);
		Date resetDateDate = new Date(cal.getTimeInMillis());
		Calendar resetDateCal = Calendar.getInstance();
		resetDateCal.setTime(resetDateDate);
		String resetDate = String.valueOf(resetDateCal.getTimeInMillis());

		String selection = "type" + "=? AND " + "date >=?";
		String[] selectionArgs = new String[] { ("2"), (resetDate) };

		Uri uriSMSURI = Uri.parse("content://sms");
		Cursor cur = this.getContentResolver().query(uriSMSURI, null,
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

				countSMS++;
			}
		}
	}

	private void getTariffStats(String tariffName) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("MobileOperators");
		query.whereEqualTo("tariff", tariffName);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject object, ParseException e) {
				if (object == null) {

				} else {
					myTariffStats = new TariffStats(object
							.getString("operator"), object.getString("tariff"),
							object.getLong("subscription"), object
									.getLong("callTmobile"), object
									.getLong("callVip"), object
									.getLong("callOne"), object
									.getLong("callStatic"), object
									.getLong("freeOperator"));
					callLogUpdate(callLog);
					clmNotifications = calculateNotifications(callLog,
							myTariffStats);
					updateNotificationVariables();
					finishOnStart();
				}
			}

		});
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
