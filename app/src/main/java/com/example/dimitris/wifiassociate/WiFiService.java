package com.example.dimitris.wifiassociate;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Dimitris on 29/10/2014.
 */
public class WiFiService extends Service {
    final static String tag = "*** WiFiService ***";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(tag, "in onCreate()");

        return;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        Log.i(tag, "onStartCommand()");

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
