package com.aek.callstatistics;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class MyWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.w("MWP:", "onUpdate method called");
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		NetworkInfo mobNetInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (activeNetInfo != null && activeNetInfo.isConnected()) {
			
			// Get all ids
			ComponentName thisWidget = new ComponentName(context,
					MyWidgetProvider.class);
			int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

			// Build the intent to call the service
			Intent intent = new Intent(context.getApplicationContext(),
					UpdateWidgetService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

			// Update the widgets via the service
			context.startService(intent);
		}else{
			super.onUpdate(context, appWidgetManager, appWidgetIds);
		}
	}

}
