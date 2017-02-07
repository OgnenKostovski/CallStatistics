package com.aek.callstatistics;

import java.io.Serializable;

public class TariffStats implements Serializable{

	public String operator;
	public String tariff;
	public long subscription;
	public long callTmobile;
	public long callVip;
	public long callOne;
	public long callStatic;
	public long sms;
	public long freeOperator;
	public long freeOther;
	public int freeSMS;

	public TariffStats(String o, String t, long s, long ct, long cv, long co,
			long cs, long fo) {
		// TODO Auto-generated constructor stub
		operator = o;
		tariff = t;
		subscription = s;
		callTmobile = ct;
		callVip = cv;
		callOne = co;
		callStatic = cs;
		freeOperator = fo;
		freeOther = 0;
		freeSMS = 0;
		sms = 10;
	}
}
