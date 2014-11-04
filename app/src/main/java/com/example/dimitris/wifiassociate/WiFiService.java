package com.example.dimitris.wifiassociate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Dimitris on 29/10/2014.
 */
public class WiFiService extends Service {
    final String tag = "*** WiFiService ***";
    WifiManager wifiM;
    private int count= 1;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(tag, "in onCreate()");

        wifiM = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(tag, "Wakeup Call - " + count++);

                int state = wifiM.getWifiState();

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);  //TODO Is this a must have for all intents after Android 3.0?
                intent.setAction("com.example.dimitris.wifiassociate.Broadcast");  //TODO Can we put any (unique) name?
                intent.putExtra("ZZZ", "SCAN RESULTS");
                sendBroadcast(intent);

                // Last run
                if (count == 11) {
                    this.cancel();
                    Log.d(tag, "***Done***");
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 5000);

        return;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        Log.d(tag, "onStartCommand()");

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
