package com.aek.callstatistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.*;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.CallLog;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class UserRegistration extends SherlockActivity implements TabListener {

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	Spinner sUserOperator;
	Spinner sUserTariff;
	CheckBox cbSave;
	Button bGetNotifications;
	String userOperator = "VIP";
	String userTariff = "VIP Free S";
	private AppPreferences appPrefs;
	Toast toast;
	private ArrayList<Call> callLog;
	private int countSMS;
	private TariffStats myTariffStats;
	private CallLogMinutes clmNotifications;

	ConnectivityManager connectivityManager;
	NetworkInfo activeNetInfo;

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			clmNotifications = (CallLogMinutes) message.obj;
			if (message.arg1 == android.app.Activity.RESULT_OK
					&& clmNotifications != null) {
				// success
				bGetNotifications.setEnabled(true);
			} else {
				// failed
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_registration);

		// Set up the action bar to show tabs.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText("Resgistration")
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Call Log")
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Statistics")
				.setTabListener(this));

		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo == null || !activeNetInfo.isConnected()) {
			toast = Toast.makeText(getApplicationContext(),
					"Please connect to the Internet!", Toast.LENGTH_LONG);
			toast.show();
			finish();
		}

		Parse.initialize(this, "zwj9F0yhJzwJBlpqCuZm4oqoCxnBXXd8tcXVTxlc",
				"HQPhZjeDvXPriVAHaaanRD47pROHRKhnqO8n6iVh");

		sUserOperator = (Spinner) findViewById(R.id.sUserOperator);
		sUserTariff = (Spinner) findViewById(R.id.sUserTariff);
		cbSave = (CheckBox) findViewById(R.id.cbSave);
		bGetNotifications = (Button) findViewById(R.id.bGetNotifications);
		bGetNotifications.setEnabled(false);

		sUserOperator.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				// Ako ne e preku getAllOperators ( bilo preku selekcija )
				if (!parent.getItemAtPosition(pos).toString()
						.contentEquals(userOperator)) {
					userOperator = parent.getItemAtPosition(pos).toString();
					appPrefs.saveOperator("");
					appPrefs.saveTariff("");
					appPrefs.changeSave(false);
					cbSave.setChecked(false);
					toast = Toast
							.makeText(getApplicationContext(),
									"SPINNER O:" + appPrefs.getTariff()
											+ " OP:" + appPrefs.getOperator(),
									Toast.LENGTH_LONG);
					toast.show();
					getAllTariffs();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}

		});

		sUserTariff.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				// Ako ne e preku getAllTariffs ( bilo preku selekcija )
				if (!parent.getItemAtPosition(pos).toString()
						.contentEquals(userTariff)) {
					userTariff = parent.getItemAtPosition(pos).toString();
					appPrefs.saveOperator("");
					appPrefs.saveTariff("");
					appPrefs.changeSave(false);
					toast = Toast
							.makeText(getApplicationContext(),
									"SPINNER T:" + appPrefs.getTariff()
											+ " OP:" + appPrefs.getOperator(),
									Toast.LENGTH_LONG);
					toast.show();
					cbSave.setChecked(false);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}

		});

		cbSave.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				appPrefs.changeSave(isChecked);
				bGetNotifications.setEnabled(false);
				if (isChecked == true) {
					appPrefs.saveTariff(userTariff);
					appPrefs.saveOperator(userOperator);

					callLog = new ArrayList<Call>();
					countSMS = 0;
					getCallLog(Calendar.getInstance()); // and update(countSMS)
					getTariffStats(userTariff); // I will call the Service
												// here!!

				} else {
					appPrefs.saveTariff("");
					appPrefs.saveOperator("");
				}
			}
		});

		bGetNotifications.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int smsLeft = 0;
				long freeLeft = 0;
				long freeOperatorLeft = 0;
				if (myTariffStats.freeSMS > countSMS)
					smsLeft = myTariffStats.freeSMS - countSMS;
				if (clmNotifications.free > 0)
					freeLeft = clmNotifications.free;
				if (clmNotifications.freeOperator > 0)
					freeOperatorLeft = clmNotifications.freeOperator;
				String s = "Free SMS left: " + smsLeft + "\n"
						+ "Free minutes to other operators: " + freeLeft + "\n"
						+ "Free minutes to " + userOperator + ": "
						+ freeOperatorLeft;
				toast = Toast.makeText(getApplicationContext(), s,
						Toast.LENGTH_LONG);
				toast.show();
			}
		});

		appPrefs = new AppPreferences(getApplicationContext());
		toast = Toast.makeText(getApplicationContext(), appPrefs.getTariff()
				+ " OP:" + appPrefs.getOperator(), Toast.LENGTH_LONG);
		toast.show();

		if (appPrefs.isSaved()) {
			userTariff = appPrefs.getTariff();
			userOperator = appPrefs.getOperator();
			cbSave.setChecked(true);
		}
		getAllOperators();
		getAllTariffs();
	}

	private void getAllTariffs() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("MobileOperators");
		query.setLimit(300);
		if (userOperator != null)
			query.whereEqualTo("operator", userOperator);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> objectList, ParseException e) {
				if (e == null) {
					List<String> listTariffs = new ArrayList<String>();
					for (ParseObject object : objectList) {
						listTariffs.add(object.getString("tariff"));
					}
					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
							UserRegistration.this,
							android.R.layout.simple_spinner_item, listTariffs);
					dataAdapter
							.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					sUserTariff.setAdapter(dataAdapter);
					if (appPrefs.isSaved()) {
						ArrayAdapter sAdapter = (ArrayAdapter) sUserTariff
								.getAdapter();
						int spinnerPosition = sAdapter.getPosition(userTariff);

						// set the default according to value
						sUserTariff.setSelection(spinnerPosition);
					}
				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
	}

	private void getAllOperators() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("MobileOperators");
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> objectList, ParseException e) {
				if (e == null) {
					List<String> listOperators = new ArrayList<String>();
					for (ParseObject object : objectList) {
						if (!listOperators.contains(object
								.getString("operator")))
							listOperators.add(object.getString("operator"));
					}
					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
							UserRegistration.this,
							android.R.layout.simple_spinner_item, listOperators);
					dataAdapter
							.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					sUserOperator.setAdapter(dataAdapter);
					if (appPrefs.isSaved()) {
						ArrayAdapter sAdapter = (ArrayAdapter) sUserOperator
								.getAdapter();
						int spinnerPosition = sAdapter
								.getPosition(userOperator);

						// set the default according to value
						sUserOperator.setSelection(spinnerPosition);
					}
				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
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
					callLogUpdateOperator(); // Call Service!!!
				}
			}

		});
	}

	// Calls Service, called in getTariffStats(..)
	private void callLogUpdateOperator() {
		Intent intent = new Intent(this, CheckOperatorService.class);
		Messenger messenger = new Messenger(handler);
		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("callLog", callLog);
		intent.putExtra("ClassPath",
				this.getPackageName() + "." + this.getLocalClassName());
		intent.putExtra("myTariffStats", myTariffStats);
		intent.putExtra("countSMS", countSMS); // Nema potreba ->
												// myTariffStats.freeSMS -=
												// countSMS (Notification)
		startService(intent);
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
		
		// countSMS = cur.getCount(); podobro reshenie!!!

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

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		if (tab.getText().toString().contentEquals("Call Log")) {
			Intent i = new Intent(this, Logs.class);
			startActivity(i);
		} else if (tab.getText().toString().contentEquals("Statistics")) {
			Intent in = new Intent(this, Statistics.class);
			startActivity(in);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

}
