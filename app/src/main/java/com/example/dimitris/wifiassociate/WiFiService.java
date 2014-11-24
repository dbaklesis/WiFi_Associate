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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Dimitris on 29/10/2014.
 */
public class WiFiService extends Service {
    private final String tag = "sss WiFiService sss";
    private WifiManager wifiMgr;
    private WifiReceiver wifiReciever;
	private final static int NUMBER_OF_PASSES = 3;
	private int scanPass = 0;
    private List<ScanResult> scanResultList;
    private List<AP> trackedAPList;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(tag, "onCreate()");

        wifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        WifiManager.WifiLock wifiLock = wifiMgr.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, tag); //TODO Lock here or inside loop task?

        if (wifiLock == null) {
            Log.d(tag, "failed to obtain wifilock");
            Log.d(tag, "aborting");
            return;
        }

        //List<WifiConfiguration> confList = wifiMgr.getConfiguredNetworks();

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if (wifiMgr.isWifiEnabled() == false) { // Nothing to do
                    return;
                }

                wifiMgr.startScan();
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

        return Service.START_STICKY;
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
				handleScanResultsAvailable(intent);
				scanPass++;
				if (scanPass == NUMBER_OF_PASSES) {
                    processScanResults();
                    scanResultList = null; //Destroy list of APs
                    scanPass = 0;
				}
                return;
            }

            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION) == true) {
                handleWifiStateChange(intent);
                return;
            }

            Log.d(tag, "Unhandled broadcast action " + intent.getAction() );
            return;

        }
    }

    private class AP {
        private int level;
		private int pass;
        private ScanResult ap;

        AP (ScanResult newap) {
            level = 0;
			pass = 0;
            ap = newap;
        }

        protected String getBSSID() {
            return ap.BSSID;
        }

        protected int getLevel() {
            return level;
        }
		
		protected void addLevel(int lvl) {
			level += lvl;
		}
		
		protected int getPass() {
			return pass;
		}
		
		protected void addPass() {
			pass++;
		}
    }

    private void handleScanResultsAvailable(Intent intent) {

        List<ScanResult> scanResultList = wifiMgr.getScanResults();

        if (scanResultList == null) {
            return;
        }

        if (scanResultList == null) {  // Nothing to do
            return;
        }
        // Scan for matching SSIDS and add them to a list

        // Prepare my ssid(remove unwanted characters)
        String mySsid = wifiMgr.getConnectionInfo().getSSID();
        String cleanMySsid = mySsid.replace("\"", "");

        if (scanPass == 0) {
            trackedAPList = null;
        }

        for (ScanResult scanResultAP : scanResultList) {
            if (cleanMySsid.equals(scanResultAP.SSID)) { // My SSID matches another's AP.
                if (trackedAPList == null) {
                    trackedAPList = new ArrayList<AP>();
                    AP trackedAP = new AP(scanResultAP);
                    trackedAP.addLevel(scanResultAP.level);
                    trackedAP.addPass();
                    trackedAPList.add(trackedAP);
                    return;
                }

                for (AP trackedAP : trackedAPList) {  // Is the matched AP in the tracked list?
                    if (scanResultAP.BSSID.equals(trackedAP.getBSSID())) { // Scan result AP is in the tracked list
                        trackedAP.addLevel(scanResultAP.level);
                        trackedAP.addPass();
                        break;
                    }

                    if (trackedAP == trackedAPList.get(trackedAPList.size()-1)) {  // If this is the last iteration, then the AP is not in the tracked list.
                        trackedAP.addLevel(scanResultAP.level);
                        trackedAP.addPass();
                        trackedAPList.add(trackedAP);
                    }
                }

        for (ScanResult scanResultAP : scanResultList) {
            if (cleanMySsid.equals(scanResultAP.SSID)) { // If the AP from the scan is in the tracked list.
                if (trackedAPList == null) {  // If tracked access point list is empty.
                    trackedAPList = new ArrayList<AP>();
                }
                // Is this access point being tracked?
                // If not, add it to the tracked access point list.
                boolean found = false;
                for (AP trackedAP : trackedAPList) {  //Match scanresult AP against tracked APs.
                    if (scanResultAP.BSSID.equals(trackedAP.getBSSID())) { //Scan result AP is in the tracked list
                        trackedAP.addLevel(scanResultAP.level);
                        trackedAP.addPass();
                        found = true;
                        break;
                    }
                }

                if (found == false) {
                    AP newTrackedAP = new AP(scanResultAP);
                    newTrackedAP.addLevel(scanResultAP.level);
                    newTrackedAP.addPass();
                    trackedAPList.add(newTrackedAP);
                }

>>>>>>> 46748173abda24ac41149ad87339a3bcd93b67e7
            }
        }
    }

    private void handleWifiStateChange(Intent intent) {

        if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            broadcastToWidget("disabled");
            return;
        } else if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
            return; //TODO something to do here?
        } else if ((wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED)) {

            Timer bssidTimer = new Timer();
            TimerTask bssidTimerTask = new TimerTask() {
                @Override
                public void run() {

                    delayWiFiStateBroadcast();
                }
            };

            bssidTimer.schedule(bssidTimerTask, 0, 3000);

            //Log.d(tag, "ENABLED/ENABLING is: " + wifiMgr.getWifiState());
            return;
        } else if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            return; //TODO something to do here?
        } else {
            Log.d(tag, "WIFI_STATE_CHANGED_ACTION State " + wifiMgr.getWifiState() + " not handled.");
            return; //TODO Process more broadcast message types
        }
    }

    protected void processScanResults() {


    }

    private void delayWiFiStateBroadcast() {

        final WifiInfo wifiinfo = wifiMgr.getConnectionInfo();
        String bssid = wifiinfo.getBSSID();
        if (bssid == null) {
            return;
        }

        if (bssid.equals("00:00:00:00:00:00")) {
            return;
        }
        broadcastToWidget(bssid);

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
