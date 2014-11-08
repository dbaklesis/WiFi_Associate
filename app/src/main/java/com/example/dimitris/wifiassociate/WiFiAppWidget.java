package com.example.dimitris.wifiassociate;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;


/**
 * Implementation of App Widget functionality.
 */
public class WiFiAppWidget extends AppWidgetProvider {
    final static String tag = "*** WiFiAppWidget ***";
    static boolean started = false;
    private final String broadcastMessage = "com.example.dimitris.wifiassociate.Broadcast";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.d(tag, "onUpdate");

        // There may be multiple widgets active, so update all of them
        //final int N = appWidgetIds.length;
       // for (int i=0; i<N; i++) {
            //updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
       // }
        updateAppWidget(context, appWidgetManager, appWidgetIds, context.getString(R.string.no_assoc_indicator));
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds, String widgetText) {

        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wi_fi_app_widget);
            views.setTextViewText(R.id.appwidget_text, widgetText);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetIds[i], views);

            Intent msg = new Intent(context, WiFiService.class);
            context.startService(msg);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(tag, ")))Received(((");
        super.onReceive(context, intent);

        if (intent == null) {
            return;
        }

        // If the message is not from our Service component, ignore it
        if (intent.getAction().equals(broadcastMessage) == false) {  //TODO Must handle all other broadcast messages
                return;
        }

        Bundle bundle = intent.getExtras();

        // Message is from the service component
        if (bundle == null) {
            return;
        }

        Log.d(tag, "BSSID is: " + bundle.getString("BSSID"));
        //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wi_fi_app_widget);
        //views.setTextViewText(R.id.appwidget_text, bundle.getString("BSSID"));

        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WiFiAppWidget.class.getName());
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        updateAppWidget(context, appWidgetManager, appWidgetIds, bundle.getString("BSSID"));

        //updateAppWidget(context, awm, widgId);

        //awm.updateAppWidget(THIS_APPWIDGET, views);

        return;
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(tag, "onDeleted");
    }
}