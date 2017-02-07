package com.aek.callstatistics;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class CheckOperatorService extends IntentService {

	private int result = Activity.RESULT_CANCELED;
	private static String SOAP_ACTION = "http://www.aek.mk/checknumbersstring";
	private static String NAMESPACE = "http://www.aek.mk/";
	private static String METHOD_NAME = "checknumbersstring";
	private static String URL = "http://www.aek.mk/ServiceNP1/checkNumbersOperator_WS.asmx?op=checknumbersstring";
	private ArrayList<Call> callLog, callLog1, callLog2, callLog3, callLog4;
	public double bill, bill1, bill2, bill3, bill4;
	public ArrayList<Double> dollaBillz, smsBillz, mySmsBillz;
	public CallLogMinutes clmNotifications;
	private TariffStats tariffStats, myTariffStats;

	public CheckOperatorService() {
		super("CheckOperatorService");
		bill = bill1 = bill2 = bill3 = bill4 = 0;
		dollaBillz = new ArrayList<Double>();
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (intent.getStringExtra("ClassPath").contentEquals(
				"com.aek.callstatistics.Logs")) {
			callLog = (ArrayList<Call>) intent.getSerializableExtra("callLog");
			callLogUpdate(callLog);
		} else if (intent.getStringExtra("ClassPath").contentEquals(
				"com.aek.callstatistics.Statistics")) {
			callLog = (ArrayList<Call>) intent.getSerializableExtra("callLog");
			callLog1 = (ArrayList<Call>) intent
					.getSerializableExtra("callLog1");
			callLog2 = (ArrayList<Call>) intent
					.getSerializableExtra("callLog2");
			callLog3 = (ArrayList<Call>) intent
					.getSerializableExtra("callLog3");
			callLog4 = (ArrayList<Call>) intent
					.getSerializableExtra("callLog4");
			tariffStats = (TariffStats) intent
					.getSerializableExtra("tariffStats");
			if (intent.getSerializableExtra("tariffStats") != null) {
				myTariffStats = (TariffStats) intent
						.getSerializableExtra("myTariffStats");
			} else {
				myTariffStats = null;
			}

			callLogUpdate(callLog);
			callLogUpdate(callLog1);
			callLogUpdate(callLog2);
			callLogUpdate(callLog3);
			callLogUpdate(callLog4);

			smsBillz = (ArrayList<Double>) intent
					.getSerializableExtra("smsBillz");

			bill = calculateBill(callLog, tariffStats) + smsBillz.get(0);
			bill1 = calculateBill(callLog1, tariffStats) + smsBillz.get(1);
			bill2 = calculateBill(callLog2, tariffStats) + smsBillz.get(2);
			bill3 = calculateBill(callLog3, tariffStats) + smsBillz.get(3);
			bill4 = calculateBill(callLog4, tariffStats) + smsBillz.get(4);

			dollaBillz.add(bill);
			dollaBillz.add(bill1);
			dollaBillz.add(bill2);
			dollaBillz.add(bill3);
			dollaBillz.add(bill4);

			if (myTariffStats != null) {
				mySmsBillz = (ArrayList<Double>) intent
						.getSerializableExtra("mySmsBillz");

				bill = calculateBill(callLog, myTariffStats)
						+ mySmsBillz.get(0);
				bill1 = calculateBill(callLog1, myTariffStats)
						+ mySmsBillz.get(1);
				bill2 = calculateBill(callLog2, myTariffStats)
						+ mySmsBillz.get(2);
				bill3 = calculateBill(callLog3, myTariffStats)
						+ mySmsBillz.get(3);
				bill4 = calculateBill(callLog4, myTariffStats)
						+ mySmsBillz.get(4);

				dollaBillz.add(bill);
				dollaBillz.add(bill1);
				dollaBillz.add(bill2);
				dollaBillz.add(bill3);
				dollaBillz.add(bill4);
			}
		} else if (intent.getStringExtra("ClassPath").contentEquals(
				"com.aek.callstatistics.UserRegistration")) {
			callLog = (ArrayList<Call>) intent.getSerializableExtra("callLog");
			callLogUpdate(callLog);

			TariffStats myTariffStats = (TariffStats) intent
					.getSerializableExtra("myTariffStats");
			clmNotifications = calculateNotifications(callLog, myTariffStats);
		}

		Bundle extras = intent.getExtras();

		result = Activity.RESULT_OK;
		if (extras != null) {
			Messenger messenger = (Messenger) extras.get("MESSENGER");
			Message msg = Message.obtain();
			msg.arg1 = result;
			if (intent.getStringExtra("ClassPath").contentEquals(
					"com.aek.callstatistics.Logs")) {
				msg.obj = callLog;
			} else if (intent.getStringExtra("ClassPath").contentEquals(
					"com.aek.callstatistics.Statistics")) {
				msg.obj = dollaBillz;
			} else if (intent.getStringExtra("ClassPath").contentEquals(
					"com.aek.callstatistics.UserRegistration")) {
				msg.obj = clmNotifications;
			}
			try {
				messenger.send(msg);
			} catch (android.os.RemoteException e1) {
				Log.w(getClass().getName(), "Exception sending message", e1);
			}
		}
		/*
		 * Bundle extras = intent.getExtras(); ArrayList<Call> callLog =
		 * (ArrayList<Call>) intent.getSerializableExtra("callLog"); result =
		 * Activity.RESULT_OK; Messenger messenger = (Messenger)
		 * extras.get("MESSENGER"); Message msg = Message.obtain(); msg.arg1 =
		 * result; msg.obj = callLog; try { messenger.send(msg); } catch
		 * (android.os.RemoteException e1) { Log.w(getClass().getName(),
		 * "Exception sending message", e1); }
		 */
		/*
		 * String number = intent.getStringExtra("number"); String operator =
		 * Logs.checkOperatorAsync(number); // Sucessfuly finished if (operator
		 * != null) result = Activity.RESULT_OK; // save (number, operator) to
		 * DataBase //Bundle extras = intent.getExtras(); if (extras != null) {
		 * Messenger messenger = (Messenger) extras.get("MESSENGER"); Message
		 * msg = Message.obtain(); msg.arg1 = result; msg.obj = operator; try {
		 * messenger.send(msg); } catch (android.os.RemoteException e1) {
		 * Log.w(getClass().getName(), "Exception sending message", e1); }
		 * 
		 * }
		 */
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

	private double calculateBill(ArrayList<Call> callLog,
			TariffStats tariffStats) {
		CallLogMinutes clm = new CallLogMinutes(0, 0, 0, 0);
		double result = 0;

		// Dodadi besplatni minuti
		clm.freeOperator = tariffStats.freeOperator;
		clm.free += tariffStats.freeOther;

		for (Call call : callLog) {

			// Ako ne e na ist operator
			if (!(call.getOperator().contentEquals("Т-Мобиле Македонија") && tariffStats.operator
					.contentEquals("T-Mobile"))
					|| (call.getOperator().contentEquals("Космофон") && tariffStats.operator
							.contentEquals("ONE"))
					|| (call.getOperator().contentEquals("ВИП Оператор") && tariffStats.operator
							.contentEquals("VIP"))) {
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

		result += tariffStats.subscription;
		result += clm.tmobile * tariffStats.callTmobile;
		result += clm.one * tariffStats.callOne;
		result += clm.vip * tariffStats.callVip;
		result += clm.wired * tariffStats.callStatic;
		return result;
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
		//request.addProperty("key", "CallStats10");
		//request.addProperty("secret", "r5eLJfAWT9G5m6TU");
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
			Log.i("WS", "NUMBERS: " + numbers);
			String str = ((SoapFault) envelope.bodyIn).faultstring;
			Log.i("", str);
		} else {
			SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
			Log.d("OK", String.valueOf(resultsRequestSOAP));
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
							} catch (ParseException e) {
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
							} catch (ParseException e) {
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

}