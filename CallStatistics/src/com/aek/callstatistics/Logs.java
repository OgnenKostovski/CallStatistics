package com.aek.callstatistics;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.CallLog;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Logs extends SherlockActivity implements TabListener {

	private ListView lvLogs;
	private ArrayAdapter listAdapter;
	public ArrayList<Call> callLog;
	private TextView tvInputOperator;
	private EditText etInput;
	private Button bCheck;
	private boolean start = false;
	Toast toast;

	private static String SOAP_ACTION = "http://www.aek.mk/checknumbersstring";
	private static String NAMESPACE = "http://www.aek.mk/";
	private static String METHOD_NAME = "checknumbersstring";
	private static String URL = "http://www.aek.mk/ServiceNP1/checkNumbersOperator_WS.asmx?op=checknumbersstring";

	ConnectivityManager connectivityManager;
	NetworkInfo activeNetInfo;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			// Object operator = message.obj;
			callLog = (ArrayList<Call>) message.obj;
			// callLog = new ArrayList<Call>();
			// callLog.add((Call) message.obj);
			tvInputOperator.setText(callLog.get(0).getName());
			// tvInputOperator.setText("WTF");
			if (message.arg1 == RESULT_OK && callLog != null) {
				// success
				// tvInputOperator.setText(operator.toString());
				// save (number, operator) as HashMap in file
				listAdapter = new CallLogsArrayAdapter(Logs.this,
						R.layout.call_row, callLog);
				lvLogs.setAdapter(listAdapter);
			} else {
				// failed
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logs);

		// Set up the action bar to show tabs.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText("Registration")
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Call Log")
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Statistics")
				.setTabListener(this));
		actionBar.selectTab(actionBar.getTabAt(1));

		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo == null || !activeNetInfo.isConnected()) {
			toast = Toast.makeText(getApplicationContext(),
					"Please connect to the Internet!", Toast.LENGTH_LONG);
			toast.show();
			finish();
		}
		
		callLog = new ArrayList<Call>();
		getCallLog(); // update callLog
		listAdapter = new CallLogsArrayAdapter(this, R.layout.call_row, callLog);
		lvLogs = (ListView) findViewById(R.id.lvLogs);
		lvLogs.setAdapter(listAdapter);

		etInput = (EditText) findViewById(R.id.etInput);
		tvInputOperator = (TextView) findViewById(R.id.tvInputOperator);
		bCheck = (Button) findViewById(R.id.bCheck);

		etInput.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER
							|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						tvInputOperator.setText(checkOperator(etInput.getText()
								.toString()));
						return true;
					}
				}
				return false;
			}
		});

		bCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(v.getContext(),
						CheckOperatorService.class);
				// Create a new Messenger for the communication back
				tvInputOperator.setText("WTF!!");
				Messenger messenger = new Messenger(handler);
				intent.putExtra("MESSENGER", messenger);
				// intent.putExtra("number", etInput.getText().toString());
				intent.putExtra("callLog", callLog);
				tvInputOperator.setText(etInput.getText().toString());
				intent.putExtra("ClassPath", Logs.this.getPackageName() + "."
						+ Logs.this.getLocalClassName());
				startService(intent);
				// tvInputOperator.setText("FTW");

				// soapCheckOperator();
			}
		});
	}

	private void getCallLog() {
		/* Query Call Log Content Provider */
		Cursor callLogCursor = getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI, null, null, null,
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
				long duration = callLogCursor.getLong(callLogCursor
						.getColumnIndex(CallLog.Calls.DURATION));

				/* Get Call Type */
				int callType = callLogCursor.getInt(callLogCursor
						.getColumnIndex(CallLog.Calls.TYPE));

				Date date = new Date(dateTimeMillis);

				if (cacheNumber == null)
					cacheNumber = number;

				if (name == null)
					name = "No Name";

				if (cacheNumber.startsWith("+389"))
					cacheNumber = cacheNumber.replace("+389", "0");
				if (cacheNumber.startsWith("00389"))
					cacheNumber = cacheNumber.replace("00389", "0");

				if (cacheNumber.length() != 9)
					continue;

				/* Create Model Object */
				Call call = new Call(name, cacheNumber, date, duration,
						callType);
				callLog.add(call);

				// if (callLog.size() == 15)
				// break;
			}
			callLogCursor.close();
		}
	}

	public static String checkOperator(String num) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://komuniciraj.mk/getdata.php");
		StringEntity se;
		try {
			se = new StringEntity("");
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/x-www-form-urlencoded"));
			httppost.setEntity(se);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("txtname", num));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();

				String result = convertStreamToString(instream);
				return result;
			}
		} catch (ClientProtocolException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		return null;
	}

	private static String convertStreamToString(InputStream is) {
		// http://stackoverflow.com/questions/6854243/httpresponse-to-string-android

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append((line + "\n"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String htmlResult = sb.toString();
		// return htmlResult;
		/*
		 * XPath xp = XPathFactory.newInstance().newXPath(); try { //
		 * http://stackoverflow
		 * .com/questions/9718626/how-to-scrape-strings-inside-given-tags
		 * 
		 * String result = (String) xp.evaluate("//b/text()", new InputSource(
		 * new StringReader(htmlResult)), XPathConstants.STRING); return result;
		 * // sb = new StringBuilder } catch (Exception e) { // TODO
		 * Auto-generated catch block return "ERROR"; }
		 */
		if (htmlResult.contains("ВИП"))
			return "VIP";
		else if (htmlResult.contains("Т-Мобиле"))
			return "T-Mobile";
		else if (htmlResult.contains("Македонски Телеком"))
			return "Makedonski Telekom";
		else if (htmlResult.contains("Космофон"))
			return "ONE";
		else if (htmlResult.contains("не е доделен"))
			return "Unknown";
		else
			return "Static";

	}

	public static String checkOperatorAsync(String num) {

		String resultString = null;

		URL url = null;
		HttpURLConnection urlConnection = null;

		try {
			url = new URL("http://komuniciraj.mk/getdata.php");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");

			String parameters = "txtname=" + URLEncoder.encode(num, "UTF-8");
			urlConnection
					.setFixedLengthStreamingMode(parameters.getBytes().length);

			// send the POST out
			PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
			out.print(parameters);
			out.close();

			int response = urlConnection.getResponseCode();
			// if resonse = HttpURLConnection.HTTP_OK = 200, then it worked.

			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			resultString = convertStreamToString(in);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			urlConnection.disconnect();
		}

		return resultString;
	}

	public void roboSpiceCheckOperator(String num) {
		// https://github.com/octo-online/robospice
		// http://www.youtube.com/watch?v=ONaD1mB8r-A
		// http://stackoverflow.com/questions/15944660/robospice-file-upload-with-spring-on-android
		// https://gist.github.com/rciovati/4125119
		// najdole(2.7.5) Header
		// http://static.springsource.org/spring-android/docs/1.0.x/reference/html/rest-template.html
	}

	// http://neilgoodman.net/2011/12/26/modern-techniques-for-implementing-rest-clients-on-android-4-0-and-below-part-1/

	public void soapCheckOperator() {
		// without ksoap2
		// http://mobileorchard.com/android-app-development-calling-web-services/
		// with ksoap2
		// http://mobile.dzone.com/news/android-development-tutorial

		// Initialize soap request + add parameters
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		// Add parameters
		request.addProperty("key", "CallStats10");
		request.addProperty("secret", "r5eLJfAWT9G5m6TU");
		PropertyInfo pi = new PropertyInfo();
		pi.setName("numbers");
		pi.setValue("070327343,071570959,071570957,072222222,075235425");
		pi.setType(String.class);
		request.addProperty(pi);

		// Declare the version of the SOAP request
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
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
		SoapObject result = (SoapObject) envelope.bodyIn;
		SoapObject result1 = (SoapObject) result.getProperty(0);
		SoapObject phonebook = (SoapObject) result1.getProperty(0);
		SoapObject contact = (SoapObject) phonebook.getProperty(0);
		// Get the first property and change the label text
		tvInputOperator.setText(contact.getProperty(1).toString());
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		Toast toast = Toast.makeText(getApplicationContext(), tab.getText(),
				Toast.LENGTH_LONG);
		toast.show();
		if (tab.getText().toString().contentEquals("Call Log"))
			start = true;
		if (start) {
			if (tab.getText().toString().contentEquals("Registration")) {
				Intent i = new Intent(this, UserRegistration.class);
				startActivity(i);
			} else if (tab.getText().toString().contentEquals("Statistics")) {
				Intent in = new Intent(this, Statistics.class);
				startActivity(in);
			}
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}
}
