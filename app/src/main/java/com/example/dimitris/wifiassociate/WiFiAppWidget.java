package com.example.dimitris.wifiassociate;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class WiFiAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String bssid = getAssocBssid(context);
        if (bssid.equals(context.getString(R.string.no_assoc_indicator))) {
            bssid = context.getString(R.string.appwidget_text);
        }

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wi_fi_app_widget);
        views.setTextViewText(R.id.appwidget_text, bssid);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public void onReceive(Context context, Intent intent) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wi_fi_app_widget);
        String bssid = getAssocBssid(context);
        if (bssid.equals(context.getString(R.string.no_assoc_indicator))) {
            bssid = context.getString(R.string.appwidget_text);
        }
        views.setTextViewText(R.id.appwidget_text, bssid);
    }

    public String getAssocBssid(Context context) {
        /*
        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
            ssid = wifiInfo.getSSID();
        }

        return ssid;
        */
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getBSSID();
    }
}




