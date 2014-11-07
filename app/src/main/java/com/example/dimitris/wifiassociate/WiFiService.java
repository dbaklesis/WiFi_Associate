package com.example.dimitris.wifiassociate;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Dimitris on 29/10/2014.
 */
public class WiFiService extends Service {
    private final String tag = "sss WiFiService sss";
    private WifiManager wifiMgr;
    //private int count= 1;
    private WifiReceiver wifiReciever;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(tag, "onCreate()");

        wifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        WifiManager.WifiLock wifiLock = wifiMgr.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, tag); //TODO Lock here or inside loop task?

        if (wifiLock == null) {
            Log.d(tag, "failed to obtain wifilock");
            Log.d(tag, "aborting");
            return;
        }

        List<WifiConfiguration> confList = wifiMgr.getConfiguredNetworks();

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                boolean wifiEnabled = wifiMgr.isWifiEnabled();
                if (wifiEnabled == false) { // Nothing to do
                    Log.d(tag, "wifi is disabled...exiting run");
                    return;
                }

                wifiMgr.startScan();
                Log.d(tag, "Scanning...");

/*
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);  //TODO Is this a must have for all intents after Android 3.0?
                intent.setAction("com.example.dimitris.wifiassociate.Broadcast");  //TODO Can we put any (unique) name?
                if (wifiEenabled) {
                    intent.putExtra("ZZZ", "SCAN RESULTS");
                } else {
                    intent.putExtra("ZZZ", "WIFI IS DISABLED");
                }
                sendBroadcast(intent);
*/
/*
                // Last run
                if (count++ == 2) {
                    this.cancel();
                    Log.d(tag, "***Done***");
                }
*/
            }
        };
        Log.d(tag, "+++ starting timer +++");
        timer.scheduleAtFixedRate(timerTask, 0, 15000);

        //this.stopSelf(); //TODO Is this the right place to stop the Service?.
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


    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {

            List<ScanResult> wifiList = wifiMgr.getScanResults();
            if (wifiList == null) {

                return;
            }


                //Log.d(tag, "*** Received scan list ***");
                /*
                for (ScanResult aWifiList : wifiList) {
                    Log.d(tag, "==========");
                    //Log.d(tag, wifiList.toString());
                    Log.d(tag, "BSSID : " + aWifiList.BSSID);
                    Log.d(tag, "SSID: " + aWifiList.SSID);
                    Log.d(tag, "level: " + aWifiList.level);
                    Log.d(tag, "capabilities:" + aWifiList.capabilities);
                }
            } else {
                //Log.d(tag, "+++ Received EMPTY scan list +++");
            }
            */
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(tag, "onDestroy");

        unregisterReceiver(wifiReciever);
    }
}
