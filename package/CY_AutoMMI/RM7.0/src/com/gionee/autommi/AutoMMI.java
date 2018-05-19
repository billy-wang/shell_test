package com.gionee.autommi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.net.ServerSocket;
import java.net.Socket;
import android.os.PowerManager;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.provider.Settings;
//Gionee zhangke 20160228 add for CR01634523 start
import android.location.LocationManager;
//Gionee zhangke 20160228 add for CR01634523 start
//Gionee zhangke 20160304 add for CR01646450 start
import android.content.ComponentName;
import android.content.Intent;
//Gionee zhangke 20160304 add for CR01646450 end
import com.gionee.autommi.Dumb;
import android.os.Process;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

public class AutoMMI extends Application {
        private static final String TAG = "AutoMMI";
        private WifiManager wifiManager;
        private BluetoothAdapter btAdapter;
        private PowerManager powerManager;
        //private static String RERORD_FILE_PATH = "/data/amt/amt_record"; //data/misc/gionee/amt_record
        private static String RERORD_FILE_PATH = "/data/misc/gionee/amt_record";
        private static String LINESEP = "\n";
        private File record = new File(RERORD_FILE_PATH);
        private ServerSocket server;
        private Socket channel;
        private static int SCREENTIMEOUT = 1800000;
        public static int SCREENTIME = 0;
        //Gionee zhangke 20160307 add for CR01647719 start
        private static final String WCHATKEY_RERORD_FILE_PATH = "/data/misc/gionee/wck_record";
        private File wck_record = new File(WCHATKEY_RERORD_FILE_PATH);
        //Gionee zhangke 20160307 add for CR01647719 end 
        private static final String CPLC_RERORD_FILE_PATH = "/data/misc/gionee/cplc_record";
        private static final String AS_RERORD_FILE_PATH = "/data/misc/gionee/as_record";
        private File cplc_record = new File(CPLC_RERORD_FILE_PATH);
        private File as_record = new File(AS_RERORD_FILE_PATH);
        private static String CLOSE = "com.gionee.autommi.close";
        public static int screentime = 0;
	@Override
	public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        wifiManager.setWifiEnabled(true);
        //Gionee zhangke 20160228 add for CR01634523 start
        Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, true);
        //Gionee zhangke 20160228 add for CR01634523 end
        registerReceiver(mCloseAutoMMIReceiver, new IntentFilter(CLOSE));
        btAdapter.enable();
        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        SCREENTIME = Settings.System.getInt(this.getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, 0);
        Log.d(TAG,"SCREENTIME = " + SCREENTIME);
        Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, SCREENTIMEOUT); 
        // Gionee zhangke 20160104 modify for CR01617603 start
        FeatureOption.initMmiXml();
        // Gionee zhangke 20160104 modify for CR01617603 end
        // Gionee zhangke 20160223 modify for CR01639944 start
        if (!record.exists()) {
            try{
                record.createNewFile();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
        if (record.exists()) {
            Log.e(TAG, " chmod 666 /data/misc/gionee/amt_record");
            try{
                Runtime.getRuntime().exec("chmod 666 " + RERORD_FILE_PATH);
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        } 
        // Gionee zhangke 20160223 modify for CR01639944 end
        // Gionee zhangke 20160307 add for CR01647719 start
        if (!wck_record.exists()) {
            try{
                wck_record.createNewFile();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
        if (wck_record.exists()) {
            Log.e(TAG, " chmod 666 /data/misc/gionee/wck_record");
            try{
                Runtime.getRuntime().exec("chmod 666 " + WCHATKEY_RERORD_FILE_PATH);
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        } 


        if (!cplc_record.exists()) {
            try{
                cplc_record.createNewFile();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
        if (cplc_record.exists()) {
            Log.e(TAG, " chmod 666 /data/misc/gionee/cplc_record");
            try{
                Runtime.getRuntime().exec("chmod 666 " + CPLC_RERORD_FILE_PATH);
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
        if (!as_record.exists()) {
            try{
                as_record.createNewFile();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
        if (as_record.exists()) {
            Log.e(TAG, " chmod 666 /data/misc/gionee/as_record");
            try{
                Runtime.getRuntime().exec("chmod 666 " + AS_RERORD_FILE_PATH);
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }

        // Gionee zhangke 20160307 add for CR01647719 end
        try {
            server = new ServerSocket(9999);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //Gionee zhangke 20160304 add for CR01646450 start
        Intent fingerIntent = new Intent("com.fingerprints.service.FingerprintService");
        ComponentName component = new ComponentName("com.fingerprints.serviceext", "com.fingerprints.service.FingerprintService"); 
        fingerIntent.setComponent(component);
        startService(fingerIntent);
        //Gionee zhangke 20160304 add for CR01646450 end

	}
    public void asResult(String tag, String content, String result) {
        try {
            Log.d(TAG, "---begin to asResult----");
            FileOutputStream fos = new FileOutputStream(as_record);
            fos.write((tag + "," + content + "," + result + LINESEP).getBytes());
            Log.d(TAG, "tag ==" + tag + ";content == " + content  +";result ==" + result);
            fos.close();
            Log.d(TAG, "---finish asResult");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "---FileNotFoundException---");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.d(TAG,"---IOException---");
            e.printStackTrace();
        }
    }
        public void recordResult(String tag, String content, String result) {
            // Gionee zhangke 20160307 add for CR01647719 start
            if(tag.equals("CplcActivity")){
                try {
                    Log.d(TAG, "---begin to record----");
                    FileOutputStream fos = new FileOutputStream(cplc_record);
                    fos.write((tag + "," + content + "," + result + LINESEP).getBytes());
                    Log.d(TAG, "tag ==" + tag + ";result ==" + result);
                    fos.close();
                    Log.d(TAG, "---finish record");
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG, "---FileNotFoundException---");
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG,"---IOException---");
                    e.printStackTrace();
                }
            } else if(tag.equals("WChatKeyTest")){
                try {
                    Log.d(TAG, "---begin to record----");
                    FileOutputStream fos = new FileOutputStream(wck_record);
                    fos.write((tag + "," + content + "," + result + LINESEP).getBytes());
                    Log.d(TAG, "tag ==" + tag + ";result ==" + result);
                    fos.close();
                    Log.d(TAG, "---finish record");
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG, "---FileNotFoundException---");
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG,"---IOException---");
                    e.printStackTrace();
                }
            }else{
                    String strfile = preprocess(tag);
                    if(null == strfile) {
                        strfile = "";
                    }
                    try {
                        Log.d(TAG, "---begin to record----");
                        FileOutputStream fos = new FileOutputStream(record); 
                        fos.write((strfile + tag + "," + content + "," + result + LINESEP).getBytes());
                        Log.d(TAG, "tag ==" + tag + ";result ==" + result);
                        fos.close();
                        Log.d(TAG, "---finish record");
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        Log.d(TAG, "---FileNotFoundException---");
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        Log.d(TAG,"---IOException---");
                        e.printStackTrace();
                    }
            }
            // Gionee zhangke 20160307 add for CR01647719 end
	}
    
	private String preprocess(String tag) {
        // TODO Auto-generated method stub
        try {
            FileInputStream fis = new FileInputStream(RERORD_FILE_PATH);
            int len = fis.available();
            byte[] bytes = new byte[len];
            fis.read(bytes);
            fis.close();
            String res = new String(bytes);
            int start;
            if(-1 != (start = res.indexOf(tag))) {
            int end = res.indexOf('\n', start);
            String sub = res.substring(start, end+1);
            return res.replace(sub, "");
            } else {
                return res;
            }
        } catch (FileNotFoundException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
	}


     BroadcastReceiver mCloseAutoMMIReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CLOSE)){
                Log.d(TAG,"mCloseAutoMMIReceiver = " + CLOSE );
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, AutoMMI.SCREENTIME);
                screentime = Settings.System.getInt(AutoMMI.this.getContentResolver(),
                                Settings.System.SCREEN_OFF_TIMEOUT, 0);
                Log.d(TAG,"screentime = " + screentime);                
                Process.killProcess(Process.myPid());
            }
        }
    };
	public Socket getChannel() {
            if ( null == channel) {
                try {
                    Log.d(TAG, "---Socket begin----");
                    channel = server.accept();
                    Log.d(TAG, "---Socket done !---");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return channel;
        }
}
