package com.example.dimitris.wifiassociate;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class WiFiAppWidget extends AppWidgetProvider {
    final static String tag = "*** WiFiAppWidget ***";
    static boolean started = false;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wi_fi_app_widget);

        String bssid = getAssocBssid(context);
        if (bssid == null) {
            views.setTextViewText(R.id.appwidget_text, context.getString(R.string.no_assoc_indicator));
        }
        else {
            if (bssid.equals(context.getString(R.string.no_assoc_indicator))) {
                views.setTextViewText(R.id.appwidget_text, context.getString(R.string.no_assoc_indicator));
            }
            else {
                views.setTextViewText(R.id.appwidget_text, bssid);
            }
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        if (started == false) {
            Intent i = new Intent(context, WiFiService.class);
            context.startService(i);
            started = true;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wi_fi_app_widget);

        String bssid = getAssocBssid(context);
        if (bssid == null) {
            views.setTextViewText(R.id.appwidget_text, context.getString(R.string.no_assoc_indicator));
        }
        else if (bssid.isEmpty() || (bssid.equals(context.getString(R.string.no_assoc_indicator)))) {
                    views.setTextViewText(R.id.appwidget_text, context.getString(R.string.no_assoc_indicator));
            } else {
            views.setTextViewText(R.id.appwidget_text, bssid);
        }

        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WiFiAppWidget.class.getName());
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        onUpdate(context, appWidgetManager, appWidgetIds);

        //updateAppWidget(context, awm, widgId);

        //awm.updateAppWidget(THIS_APPWIDGET, views);

        return;
    }

    public String getAssocBssid(Context context) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String str = new String();
        str = wifiInfo.getBSSID();
        return str;
    }
}
