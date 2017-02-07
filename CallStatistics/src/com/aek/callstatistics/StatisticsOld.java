package com.aek.callstatistics;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class StatisticsOld extends Activity {

	private ParseObject tariff;
	TextView tvTM;
	Button bCalculate;
	Spinner sTariffs;
	String selectedTariff;
	TariffStats selectedTariffStats;
	private long tariffBill;
	private CallLogCalculationsOld clc, clc1, clc2, clc3, clc4;
	/** The main dataset that includes all the series that go into a chart. */
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	/** The main renderer that includes all the renderers customizing a chart. */
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	/** The most recently added series. */
	private XYSeries mCurrentSeries;
	/** The most recently created renderer, customizing the current series. */
	private XYSeriesRenderer mCurrentRenderer;
	/** The chart view that displays the data. */
	private GraphicalView mChartView;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);

		initializeMRenderer();

		tvTM = (TextView) findViewById(R.id.tvTM);
		bCalculate = (Button) findViewById(R.id.bCalculate);
		clc = new CallLogCalculationsOld(this);
		clc1 = new CallLogCalculationsOld(this);
		clc2 = new CallLogCalculationsOld(this);
		clc3 = new CallLogCalculationsOld(this);
		clc4 = new CallLogCalculationsOld(this);
		sTariffs = (Spinner) findViewById(R.id.sTariff);

		Parse.initialize(this, "zwj9F0yhJzwJBlpqCuZm4oqoCxnBXXd8tcXVTxlc",
				"HQPhZjeDvXPriVAHaaanRD47pROHRKhnqO8n6iVh");

		// IN MAIN ACTIVITY!!! track statistics
		ParseAnalytics.trackAppOpened(getIntent());

		getAllTariffs();
		sTariffs.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				bCalculate.setEnabled(false);
				selectedTariff = parent.getItemAtPosition(pos).toString();
				getTariffCalculations(selectedTariff);
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
				// TODO Auto-generated method stub
				// tvTM.setText(Long.toString(clc.calculateBill(Calendar.getInstance(),
				// selectedTariffStats)));
				// calculateBill(Calendar.getInstance(), selectedTariff);
				mCurrentSeries = new XYSeries(selectedTariff);
				tvTM.setText("WTF");
				Calendar c = Calendar.getInstance();
				String monthString = new DateFormatSymbols().getMonths()[c.MONTH - 1];
				mRenderer.addXTextLabel(0, monthString);
				mCurrentSeries.add(0, clc1.calculateBill(c = monthDecrement(c),
						selectedTariffStats));
				tvTM.setText("CLC1");

				mCurrentSeries.add(1, 300 + clc2.calculateBill(
						c = monthDecrement(c), selectedTariffStats));
				tvTM.setText("CLC2");
				monthString = new DateFormatSymbols().getMonths()[c.MONTH - 1];
				mRenderer.addXTextLabel(1, monthString);
				mCurrentSeries.add(2, 400 + clc3.calculateBill(
						c = monthDecrement(c), selectedTariffStats));
				tvTM.setText("CLC3");
				monthString = new DateFormatSymbols().getMonths()[c.MONTH - 1];
				mRenderer.addXTextLabel(2, monthString);
				mCurrentSeries.add(3, 200 + clc4.calculateBill(
						c = monthDecrement(c), selectedTariffStats));
				tvTM.setText("CLC4");
				monthString = new DateFormatSymbols().getMonths()[c.MONTH - 1];
				mRenderer.addXTextLabel(3, monthString);

				mDataset.clear();
				mDataset.addSeries(mCurrentSeries);

				XYSeriesRenderer renderer = new XYSeriesRenderer();
				mRenderer.addSeriesRenderer(renderer);
				// set some renderer properties
				// renderer.setPointStyle(PointStyle.CIRCLE);
				renderer.setFillPoints(true);
				renderer.setDisplayChartValues(true);
				renderer.setLineWidth(2);
				mCurrentRenderer = renderer;
				mChartView.repaint();
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

	private void calculateBill(Calendar cal, String tariffName) {
		tvTM.setText("CALCULATION START");
		if (clc.calculateMinutesMonthRange(cal)) {
			tvTM.setText("TARIFF CALCULATIONS");
			getTariffCalculations(tariffName);
		}
	}

	private void getTariffCalculations(String tariffName) {
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
					bCalculate.setEnabled(true);
					selectedTariffStats = new TariffStats(object
							.getString("operator"), object.getString("tariff"),
							object.getLong("subscription"), object
									.getLong("callTmobile"), object
									.getLong("callVip"), object
									.getLong("callOne"), object
									.getLong("callStatic"), object
									.getLong("freeOperator"));
					tvTM.setText("TARIFF FOUND");
					tariffBill = calculateForTariff(tariff);
					tvTM.setText(Long.toString(tariffBill));

				}
			}

		});
	}

	private long calculateForTariff(ParseObject selectedTariff) {
		if (selectedTariff.getString("operator").contentEquals("T-Mobile")) {
			calculateForTMobile(selectedTariff);
		} else if (selectedTariff.getString("operator").contentEquals("VIP")) {
			calculateForVIP(selectedTariff);
		} else if (selectedTariff.getString("operator").contentEquals("ONE")) {
			calculateForONE(selectedTariff);
		}
		return calculation(selectedTariff);
	}

	private void calculateForONE(ParseObject selectedTariff) {
		clc.one -= selectedTariff.getInt("freeOperator");
		if (clc.one < 0)
			clc.one = 0;
	}

	private void calculateForVIP(ParseObject selectedTariff) {
		clc.vip -= selectedTariff.getInt("freeOperator");
		if (clc.vip < 0)
			clc.vip = 0;
	}

	private void calculateForTMobile(ParseObject selectedTariff) {
		clc.tmobile -= selectedTariff.getInt("freeOperator");
		if (clc.tmobile < 0)
			clc.tmobile = 0;
	}

	private long calculation(ParseObject selectedTariff) {
		long bill = 0;
		bill += clc.one * selectedTariff.getLong("callOne");
		bill += clc.vip * selectedTariff.getLong("callVip");
		bill += clc.tmobile * selectedTariff.getLong("callTmobile");
		bill += selectedTariff.getLong("subscribtion");
		return bill;
	}

	private void initializeMRenderer() {
		// set some properties on the main renderer
		// mRenderer.setApplyBackgroundColor(true);
		// mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
		mRenderer.setAxisTitleTextSize(16);
		mRenderer.setChartTitleTextSize(20);
		mRenderer.setLabelsTextSize(15);
		mRenderer.setLegendTextSize(15);
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setPointSize(5);
		mRenderer.setBarSpacing(1);
	}

	private void getAllTariffs() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("MobileOperators");
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> objectList, ParseException e) {
				if (e == null) {
					List<String> listTariffs = new ArrayList<String>();
					for (ParseObject object : objectList) {
						listTariffs.add(object.getString("tariff"));
					}
					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
							StatisticsOld.this,
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

	private Calendar monthDecrement(Calendar c) {
		c.add(Calendar.HOUR, -24);
		Date resetDateDate = new Date(c.getTimeInMillis());
		Calendar resultCal = Calendar.getInstance();
		resultCal.setTime(resetDateDate);
		return resultCal;
		/*
		 * Calendar cal = c; cal.set(Calendar.MONTH, c.MONTH - 1); return cal;
		 */
	}
}
