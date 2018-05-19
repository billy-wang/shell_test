package com.cydroid.autommi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import com.cydroid.util.DswLog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;

public class NetworkSpeedOnWifi5GTest extends BaseActivity {
	private String TAG = "Wifi5GTest";
	private WifiManager wifi;
	private String ssid;
	private String passwdOfAp;
	private String addressOfFtpServer;
	private String userOfFtp;
	private String passwdOfFtp;
	private String uploadPath; // directory
	private String downloadPath; // directory and file name
	private Thread testThread;
	private Boolean toTest = true;
	private BroadcastReceiver receiver;
	//private static String URLFORMAT = "ftp://%s:%s@%s/%s;type=i";
	private static String URLFORMAT = "ftp://%s:%s@%s/%s";
	private String localFile;
	private int downSpeed = -1;
	private int upSeed = -1;
    private int rssi = -1;
    private boolean isConnected = false;

    private String EXTRA_SSID = "ssid";
    private String EXTRA_AP_PASSWD =  "ap_passwd";
    private String EXTRA_FTP_SERVER = "ftp_server";
    private String EXTRA_FTP_USER = "ftp_user";
    private String EXTRA_FTP_PASSWD = "ftp_passwd";
    private String EXTRA_DOWNLOAD_PATH ="dfpath";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		((AutoMMI)getApplication()).recordResult(TAG, "" + downSpeed + "|" + upSeed + "|" + rssi, "2");
		
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				NetworkInfo networkInfo = intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (null != networkInfo && networkInfo.isConnected() && toTest) {
					toTest = false;
					test();
				}
			}
		};
        Intent it = getIntent();
        parseIntent(it);
        isConnected = testConnection();
        if (isConnected) {
            test();
        } else {
		   setupWifi();
        }

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
        if (!isConnected) {
		    this.registerReceiver(receiver, new IntentFilter(
				    WifiManager.NETWORK_STATE_CHANGED_ACTION)); 
        }
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//Gionee <Oveasea_Bug> <tanbotao> <20161230> for 40427 beign
		//wifi.setFrequencyBand(WifiManager.WIFI_FREQUENCY_BAND_AUTO, false);
		//Gionee <Oveasea_Bug> <tanbotao> <20161230> for 40427 end
        if (!isConnected) {
		    this.unregisterReceiver(receiver);
        }
        this.finish();
	}

    private void parseIntent(Intent it) {
        ssid = it.getStringExtra(EXTRA_SSID);
        passwdOfAp = it.getStringExtra(EXTRA_AP_PASSWD);
        //ssid = "Phicomm_2C7170";
        //passwdOfAp = "*#837504#"; 
        addressOfFtpServer = it.getStringExtra(EXTRA_FTP_SERVER);
        userOfFtp = it.getStringExtra(EXTRA_FTP_USER);
        passwdOfFtp = it.getStringExtra(EXTRA_FTP_PASSWD);
        downloadPath = it.getStringExtra(EXTRA_DOWNLOAD_PATH);
    }

	private void test() {
		testThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
				downLoad();
				upLoad();
                WifiInfo i = wifi.getConnectionInfo();
                if ( null !=i ) {
                    rssi = i.getRssi();
                    //wifi.disconnect();
                    //wifi.removeNetwork(i.getNetworkId());
                }
				((AutoMMI)getApplication()).recordResult(TAG, "" + downSpeed + "|" + upSeed + "|" + rssi, "2");
			}
		});
		testThread.start();
	}

    private boolean testConnection() {
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (null != info && info.getSubtype() == ConnectivityManager.TYPE_WIFI
				&& info.isConnected()) {
			return true;
		} else {
            return false;
        }
    }

	private void setupWifi() {
        
		wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
	//	wifi.setFrequencyBand(WifiManager.WIFI_FREQUENCY_BAND_5GHZ, false);
		WifiConfiguration conf = new WifiConfiguration();
		conf.SSID = "\"" + ssid + "\"";
		conf.preSharedKey = "\"" + passwdOfAp + "\"";
		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

		int id = wifi.addNetwork(conf);
		//wifi.enableNetwork(id, true);
        //DswLog.d(TAG, "old id : " + id);
        wifi.saveConfiguration();
        //WifiConfiguration wc = wifi.getConfiguredNetworks().get(0);
        //int new_id = wifi.updateNetwork(wc);
        //DswLog.d(TAG, "new id : " + id);
		//wifi.disconnect();
        //wifi.reconnect();
		wifi.setWifiEnabled(false);
        DswLog.d(TAG, "disable wifi ,  then enable ...");
        try {
                Thread.sleep(500);
        } catch (InterruptedException e) {
                    System.out.println(e);
        }
		wifi.setWifiEnabled(true);
	}

	private void downLoad() {
		try {
			URL url = new URL(String.format(URLFORMAT, userOfFtp, passwdOfFtp,
					addressOfFtpServer, downloadPath));
			BufferedOutputStream os = null;
            DswLog.d(TAG, "in downLoad ... URL is " + url);
			try {
				BufferedInputStream is = new BufferedInputStream(
						url.openStream());

				String[] a = downloadPath.split("/");
				//Gionee zhangke 20160224 modify for CR01633166 start
				String fn = "/data/misc/gionee/" + a[a.length - 1];
				//Gionee zhangke 20160224 modify for CR01633166 end
				localFile = fn;
				DswLog.d(TAG, "localfile : " + localFile);
				os = new BufferedOutputStream(new FileOutputStream(fn));

				DswLog.d(TAG, "download begin ");
				downSpeed = streamCopy(is, os);
				DswLog.d(TAG, "download ok : " + downSpeed);

			} catch (IOException e) {
				DswLog.d(TAG, "IOException #1 error break");
				e.printStackTrace();
			} finally {
				if (null != os)
					try {
						os.close();
					} catch (IOException e) {
						DswLog.d(TAG, "IOException #2 error break");
						e.printStackTrace();
					}
			}
		} catch (MalformedURLException e) {
			DswLog.d(TAG, "MalformedURLException error break");
			e.printStackTrace();
		}
		//Gionee <GN_BSP_MMI> <chengq> <20170424> modify for ID 121459 begin
		catch (NullPointerException e) {
			DswLog.d(TAG, "NullPointerException error break");
			e.printStackTrace();
		}
		//Gionee <GN_BSP_MMI> <chengq> <20170424> modify for ID 121459 end
	}

	private void upLoad() {
		try {
			URL url = new URL(String.format(URLFORMAT, userOfFtp, passwdOfFtp,
					addressOfFtpServer, downloadPath + "_2"));
            DswLog.d(TAG, "in upLoad ... URL is " + url);
			try {
				URLConnection conn = url.openConnection();
				conn.setDoOutput(true);
				BufferedOutputStream os = new BufferedOutputStream(
						conn.getOutputStream());

				/*
				 * BufferedInputStream is = new BufferedInputStream(this
				 * .getResources().openRawResource(R.raw.vim));
				 */
				BufferedInputStream is = new BufferedInputStream(
						new FileInputStream(localFile));
				DswLog.d(TAG, "upload begin ");
				upSeed = streamCopy(is, os);

				DswLog.d(TAG, "upload OK : " + upSeed);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int streamCopy(BufferedInputStream is, BufferedOutputStream os)
			throws IOException {
		long start = System.currentTimeMillis();
		DswLog.d(TAG, "begin : " + start);

		/*
		 * int b; while ((b = is.read()) != -1) { os.write(b); }
		 */
		long size = 0;
		byte[] ba = new byte[1024];
		int i;
		while ((i = is.read(ba)) != -1) {
			size += i;
			os.write(ba, 0, i);
		}
		os.flush();
		long end = System.currentTimeMillis();
		DswLog.d(TAG, "finish : " + end);
        DswLog.d(TAG, "total size : " + size);
        DswLog.d(TAG, "time cost : " + (end - start));

        is.close();
        os.close();

		long duration = end  - start ;
		return (int) (size * 1.0 / duration * 1000);
	}
}
