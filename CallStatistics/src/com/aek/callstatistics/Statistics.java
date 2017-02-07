package com.aek.callstatistics;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Statistics extends SherlockActivity implements TabListener {

	private TextView tvTM;
	private Button bCalculate;
	private Spinner sTariffs;
	private String selectedTariff, myTariff;
	private ParseObject tariff;
	private TariffStats selectedTariffStats, myTariffStats;
	private CallLogCalculations clc;
	private ArrayList<Call> callLog, callLog1, callLog2, callLog3, callLog4;
	private ArrayList<Double> dollaBillz;
	private Intent i;
	private AppPreferences appPrefs;
	private Toast toast;
	private int tariffNo;
	private boolean start = false;

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private XYSeries mSelectedSeries, mMySeries;
	private XYSeriesRenderer mSelectedRenderer, mMyRenderer;
	private GraphicalView mChartView;

	ConnectivityManager connectivityManager;
	NetworkInfo activeNetInfo;

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			dollaBillz = (ArrayList<Double>) message.obj;
			if (message.arg1 == android.app.Activity.RESULT_OK
					&& dollaBillz != null) {
				// success
				String s = dollaBillz.get(0) + "+" + dollaBillz.get(1) + "+"
						+ dollaBillz.get(2) + "+" + dollaBillz.get(3) + "+"
						+ dollaBillz.get(4);

				mDataset.clear();
				mRenderer.setYAxisMax(Collections.max(dollaBillz) + 300);
				
				Calendar cal = Calendar.getInstance();
				cal.add(cal.MONTH, -4);
				mRenderer.addXTextLabel(0, getMonthForInt(cal.get(cal.MONTH)));
				cal.add(cal.MONTH, 1);
				mRenderer.addXTextLabel(1, getMonthForInt(cal.get(cal.MONTH)));
				cal.add(cal.MONTH, 1);
				mRenderer.addXTextLabel(2, getMonthForInt(cal.get(cal.MONTH)));
				cal.add(cal.MONTH, 1);
				mRenderer.addXTextLabel(3, getMonthForInt(cal.get(cal.MONTH)));

				mSelectedSeries = new XYSeries(selectedTariff);
				for (int i = 1; i < 5; i++) {
					mSelectedSeries.add(i - 1, dollaBillz.get(i));
				}

				mDataset.addSeries(mSelectedSeries);

				mSelectedRenderer = new XYSeriesRenderer();
				mSelectedRenderer.setFillPoints(true);
				mSelectedRenderer.setDisplayChartValues(true);
				mSelectedRenderer.setLineWidth(2);
				mSelectedRenderer.setColor(Color.DKGRAY);

				mRenderer.addSeriesRenderer(mSelectedRenderer);

				if (dollaBillz.size() > 5) {
					s += "\n" + dollaBillz.get(5) + "+" + dollaBillz.get(6)
							+ "+" + dollaBillz.get(7) + "+" + dollaBillz.get(8)
							+ "+" + dollaBillz.get(9);

					mMySeries = new XYSeries(myTariff);
					for (int i = 6; i < 10; i++) {
						mMySeries.add(i - 6, dollaBillz.get(i));
					}

					mDataset.addSeries(mMySeries);

					mMyRenderer = new XYSeriesRenderer();
					mMyRenderer.setFillPoints(true);
					mMyRenderer.setDisplayChartValues(true);
					mMyRenderer.setLineWidth(2);

					mRenderer.addSeriesRenderer(mMyRenderer);
				}

				mChartView.repaint();
				tvTM.setText(s);

			} else {
				// failed
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);

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
		actionBar.selectTab(actionBar.getTabAt(2));

		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo == null || !activeNetInfo.isConnected()) {
			toast = Toast.makeText(getApplicationContext(),
					"Please connect to the Internet!", Toast.LENGTH_LONG);
			toast.show();
			finish();
		}

		initializeMRenderer();

		tvTM = (TextView) findViewById(R.id.tvTM);
		bCalculate = (Button) findViewById(R.id.bCalculate);
		sTariffs = (Spinner) findViewById(R.id.sTariff);

		Parse.initialize(this, "zwj9F0yhJzwJBlpqCuZm4oqoCxnBXXd8tcXVTxlc",
				"HQPhZjeDvXPriVAHaaanRD47pROHRKhnqO8n6iVh");

		// IN MAIN ACTIVITY!!! track statistics
		ParseAnalytics.trackAppOpened(getIntent());

		appPrefs = new AppPreferences(getApplicationContext());
		toast = Toast.makeText(getApplicationContext(), appPrefs.getTariff()
				+ " OP:" + appPrefs.getOperator(), Toast.LENGTH_LONG);
		toast.show();

		tariffNo = 1;
		if (appPrefs.isSaved()) {
			myTariff = appPrefs.getTariff();
		}

		getAllTariffs();
		sTariffs.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				bCalculate.setEnabled(false);
				selectedTariff = parent.getItemAtPosition(pos).toString();

				tvTM.setText("OK1!");

				tariffNo = 1;
				getTariffStats(selectedTariff); // here i will call the Service

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				bCalculate.setEnabled(false);
			}

		});
		
		bCalculate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tvTM.setText(selectedTariff);
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		tvTM.setText("onResume");
		if (mChartView == null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			mChartView = ChartFactory.getBarChartView(this, mDataset,
					mRenderer, Type.DEFAULT);
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		} else {
			mChartView.repaint();
		}
	}

	private void getTariffStats(String tariffName) {
		i = new Intent(getBaseContext(), CheckOperatorService.class);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("MobileOperators");
		query.whereEqualTo("tariff", tariffName);
		tvTM.setText("GET TARIFF STARTS");
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject object, ParseException e) {
				if (object == null) {
					tariff = null;
					tvTM.setText("TARIFF NOT FOUND!");
				} else {
					tariff = object;

					if (tariffNo == 1) {
						selectedTariffStats = new TariffStats(object
								.getString("operator"), object
								.getString("tariff"), object
								.getLong("subscription"), object
								.getLong("callTmobile"), object
								.getLong("callVip"), object.getLong("callOne"),
								object.getLong("callStatic"), object
										.getLong("freeOperator"));
						tvTM.setText("TARIFF 1 FOUND");
					}
					tariffNo++;
					if (appPrefs.isSaved()) {
						if (tariffNo == 2) {
							getTariffStats(myTariff);
						} else if (tariffNo == 3) {
							myTariffStats = new TariffStats(object
									.getString("operator"), object
									.getString("tariff"), object
									.getLong("subscription"), object
									.getLong("callTmobile"), object
									.getLong("callVip"), object
									.getLong("callOne"), object
									.getLong("callStatic"), object
									.getLong("freeOperator"));
							tvTM.setText("TARIFF 2 FOUND");
							tariffNo = 1;

							bCalculate.setEnabled(true);

							clc = new CallLogCalculations(Statistics.this, i,
									handler, Statistics.this,
									selectedTariffStats, myTariffStats);
							clc.calculateMinutesMonthRange(Calendar
									.getInstance());
						}
					} else {
						bCalculate.setEnabled(true);
						clc = new CallLogCalculations(Statistics.this, i,
								handler, Statistics.this, selectedTariffStats);
						clc.calculateMinutesMonthRange(Calendar.getInstance());
					}
				}
			}

		});
	}

	/*
	 * private void calculateBill(Calendar cal, String tariffName) {
	 * tvTM.setText("CALCULATION START"); if
	 * (clc.calculateMinutesMonthRange(cal)) {
	 * tvTM.setText("TARIFF CALCULATIONS"); getTariffCalculations(tariffName); }
	 * }
	 * 
	 * private void getTariffCalculations(String tariffName) {
	 * ParseQuery<ParseObject> query = ParseQuery.getQuery("MobileOperators");
	 * query.whereEqualTo("tariff", tariffName);
	 * tvTM.setText("GET TARIFF STARTS"); query.getFirstInBackground(new
	 * GetCallback<ParseObject>() {
	 * 
	 * @Override public void done(ParseObject object, ParseException e) { if
	 * (object == null) { tariff = null; tvTM.setText("TARIFF NOT FOUND!"); }
	 * else { tariff = object; bCalculate.setEnabled(true); selectedTariffStats
	 * = new TariffStats(object .getString("operator"),
	 * object.getString("tariff"), object.getLong("subscription"), object
	 * .getLong("callTmobile"), object .getLong("callVip"), object
	 * .getLong("callOne"), object .getLong("callStatic"), object
	 * .getLong("freeOperator")); tvTM.setText("TARIFF FOUND"); tariffBill =
	 * calculateForTariff(tariff); tvTM.setText(Long.toString(tariffBill));
	 * 
	 * } }
	 * 
	 * }); }
	 * 
	 * private long calculateForTariff(ParseObject selectedTariff) { if
	 * (selectedTariff.getString("operator").contentEquals("T-Mobile")) {
	 * calculateForTMobile(selectedTariff); } else if
	 * (selectedTariff.getString("operator").contentEquals("VIP")) {
	 * calculateForVIP(selectedTariff); } else if
	 * (selectedTariff.getString("operator").contentEquals("ONE")) {
	 * calculateForONE(selectedTariff); } return calculation(selectedTariff); }
	 * 
	 * private void calculateForONE(ParseObject selectedTariff) { clc.one -=
	 * selectedTariff.getInt("freeOperator"); if (clc.one < 0) clc.one = 0; }
	 * 
	 * private void calculateForVIP(ParseObject selectedTariff) { clc.vip -=
	 * selectedTariff.getInt("freeOperator"); if (clc.vip < 0) clc.vip = 0; }
	 * 
	 * private void calculateForTMobile(ParseObject selectedTariff) {
	 * clc.tmobile -= selectedTariff.getInt("freeOperator"); if (clc.tmobile <
	 * 0) clc.tmobile = 0; }
	 * 
	 * private long calculation(ParseObject selectedTariff) { long bill = 0;
	 * bill += clc.one * selectedTariff.getLong("callOne"); bill += clc.vip *
	 * selectedTariff.getLong("callVip"); bill += clc.tmobile *
	 * selectedTariff.getLong("callTmobile"); bill +=
	 * selectedTariff.getLong("subscribtion"); return bill; }
	 * 
	 * private void initializeMRenderer() { // set some properties on the main
	 * renderer // mRenderer.setApplyBackgroundColor(true); //
	 * mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
	 * mRenderer.setAxisTitleTextSize(16); mRenderer.setChartTitleTextSize(20);
	 * mRenderer.setLabelsTextSize(15); mRenderer.setLegendTextSize(15);
	 * mRenderer.setZoomButtonsVisible(true); mRenderer.setPointSize(5);
	 * mRenderer.setBarSpacing(1); }
	 */private void getAllTariffs() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("MobileOperators");
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> objectList, ParseException e) {
				if (e == null) {
					List<String> listTariffs = new ArrayList<String>();
					for (ParseObject object : objectList) {
						listTariffs.add(object.getString("tariff"));
					}
					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
							Statistics.this,
							android.R.layout.simple_spinner_item, listTariffs);
					dataAdapter
							.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					sTariffs.setAdapter(dataAdapter);
				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
	}

	/*
	 * 
	 * private Calendar monthDecrement(Calendar c) { c.add(Calendar.HOUR, -24);
	 * Date resetDateDate = new Date(c.getTimeInMillis()); Calendar resultCal =
	 * Calendar.getInstance(); resultCal.setTime(resetDateDate); return
	 * resultCal; /* Calendar cal = c; cal.set(Calendar.MONTH, c.MONTH - 1);
	 * return cal;
	 */

	// }

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		if (tab.getText().toString().contentEquals("Statistics"))
			start = true;
		if (start) {
			if (tab.getText().toString().contentEquals("Call Log")) {
				Intent i = new Intent(this, Logs.class);
				startActivity(i);
			} else if (tab.getText().toString().contentEquals("Registration")) {
				Intent in = new Intent(this, UserRegistration.class);
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

	public String getMonthForInt(int num) {
		String month = "wrong";
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] months = dfs.getMonths();
		if (num >= 0 && num <= 11) {
			month = months[num];
		}
		return month;
	}
	
	private void initializeMRenderer() {
		//http://www.truiton.com/2013/04/android-tutorial-drawing-achartengine-bar-chart-with-example/
		// set some properties on the main renderer
		mRenderer.setChartTitle("Сметки во последните 4 месеци:");
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
		mRenderer.setAxisTitleTextSize(10);
		mRenderer.setChartTitleTextSize(15);
		mRenderer.setLabelsTextSize(8);
		mRenderer.setLegendTextSize(8);
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setBarSpacing(1);
		mRenderer.setXLabels(0);
		mRenderer.setXAxisMin(-1);
		mRenderer.setXAxisMax(5);
		mRenderer.setYAxisMin(-10);
		mRenderer.setXTitle("Месец");
		mRenderer.setYTitle("Сметка");
		mRenderer.setBarWidth(13);
		
		//mRenderer.setLegendHeight(mRenderer.getLegendHeight() + 40);
		mRenderer.setShowLegend(true);
	}
	
}
