package com.example.dimitris.wifiassociate;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
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
    private final String wifiStateChange = "android.net.wifi.STATE_CHANGED";
    private final String wifiScanReslults = "android.net.wifi.SCAN_RESULTS";
    //private final String broadcastMessage = "com.example.dimitris.wifiassociate.Broadcast";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(tag, "onCreate()");

        wifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        /*
        WifiManager.WifiLock wifiLock = wifiMgr.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, tag); //TODO Lock here or inside loop task?

        if (wifiLock == null) {
            Log.d(tag, "failed to obtain wifilock");
            Log.d(tag, "aborting");
            return;
        }
        */

        //List<WifiConfiguration> confList = wifiMgr.getConfiguredNetworks();

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                boolean wifiEnabled = wifiMgr.isWifiEnabled();
                if (wifiEnabled == false) { // Nothing to do
                    //Log.d(tag, "wifi is disabled...exiting run");
                    return;
                }

                wifiMgr.startScan();
                //Log.d(tag, "Scanning...");
            }
        };
        Log.d(tag, "+++ starting timer +++");
        if (wifiMgr.isWifiEnabled()) {
            broadcastToWidget(wifiMgr.getConnectionInfo().getBSSID()); //TODO Initial BSSID broadcast;
        }
        timer.scheduleAtFixedRate(timerTask, 0, 15000);

        //this.stopSelf(); //TODO Is this the right place to stop the Service?.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        Log.d(tag, "onStartCommand()");
        if (wifiMgr.isWifiEnabled()) {
            broadcastToWidget(wifiMgr.getConnectionInfo().getBSSID());
        } else {
            broadcastToWidget(null);
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {

            if (intent == null) {
                return;
            }

            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                return;
            }

            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION) == true) {
                handleWifiStateChange(intent);
                return;
            } else {
                Log.d(tag, "Unhandled broadcast");
                return;
            }

            /*
            for (ScanResult aWifiList : wifiList) {
                //Log.d(tag, wifiList.toString());
                Log.d(tag, "BSSID : " + aWifiList.BSSID);
                Log.d(tag, "SSID: " + aWifiList.SSID);
                Log.d(tag, "level: " + aWifiList.level);
                Log.d(tag, "capabilities:" + aWifiList.capabilities);
            }
            */

        }
    }

        private void handleWifiStateChange(Intent intent) {

            if (intent.getAction() == WifiManager.WIFI_STATE_CHANGED_ACTION) {

                if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
                    broadcastToWidget("disabled");
                    return;
                }  else if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
                    return; //TODO something to do here?
                }
                else if ((wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED)) {

                        Timer bssidTimer = new Timer();
                        TimerTask bssidTimerTask = new TimerTask() {
                            @Override
                            public void run() {

                                final WifiInfo wifiinfo = wifiMgr.getConnectionInfo();
                                String bssid = wifiinfo.getBSSID();
                                if (bssid == null) {
                                    return;
                                }

                                if (bssid.equals("00:00:00:00:00:00:00")) {
                                    return;
                                }

                                broadcastToWidget(bssid);

                            }
                        };

                        bssidTimer.schedule(bssidTimerTask, 0, 2000);

                    //Log.d(tag, "ENABLED/ENABLING is: " + wifiMgr.getWifiState());
                            return;
                } else if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                    return; //TODO something to do here?
                } else {
                    Log.d(tag, "WIFI_STATE: " + wifiMgr.getWifiState() + " not handled yet");
                    return; //TODO Process more broadcast message types
                }
            } else {
                Log.d(tag, "ACTION: " + intent.getAction() + " not handled yet");
                return;
            }
        }


    private void broadcastToWidget(String bssid) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);  //TODO Is this a must have for all intents after Android 3.0?
        intent.setAction("com.example.dimitris.wifiassociate.Broadcast");  //TODO Can we put any (unique) name?
        intent.putExtra("BSSID", bssid);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(tag, "onDestroy");

        unregisterReceiver(wifiReciever);
    }
}
