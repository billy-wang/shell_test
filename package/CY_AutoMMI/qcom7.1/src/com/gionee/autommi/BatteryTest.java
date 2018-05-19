package com.gionee.autommi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.BatteryManager;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.io.File;

public class BatteryTest extends BaseActivity {
	public static final String TAG = "BatteryTest";
	private BroadcastReceiver battInfoRec;
	private String[] result = new String[4];
	private boolean[] flag = new boolean[2];
    private static final String chargeCurNodePath = "/sys/class/power_supply/battery/current_now";
    private static final String charegeVolNodepath = "/sys/class/power_supply/battery/voltage_now";
    private static final String charegeVolNodepathPrefix = "/sys/devices/qpnp-vadc-";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		battInfoRec = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
					Integer a = intent.getIntExtra(BatteryManager.EXTRA_STATUS,  -1);
					Integer b = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
					result[0] = a.toString();
					result[1] = b.toString();
					flag[0] = true;
					getChargeInfo();
					
					if(flag[0] && flag[1]) {
						String content = "";
						for(int i = 0 ; i <  result.length; i++) {
							content += result[i];
	                        content += (i == result.length - 1) ? "" :"|";
						}
						((AutoMMI)getApplication()).recordResult(TAG, content, "2");
					}
				}	
			}
		};
	}

	protected void getChargeInfo() {
		//  starting a new activity cause charge voltage to fluctuate ,
		//  so we must wait for a while  
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	String c = extractNodeInfo(chargeCurNodePath);
		String c = extractNodeInfo();
		Log.e(TAG, "c = " + c);
		int l = c.length();
		if ( l > 3) {
			if (c.startsWith("-")){
				c = c.substring(1, l - 3);
				Log.e(TAG, "c1 = " + c);
			}
			else{
				c = c.substring(0,l - 3);
				Log.e(TAG, "c2 = " + c);
			}
			}
	 	
		//String v = extractNodeInfo(charegeVolNodepath);
        String v = getChargeVol();
    	Log.e(TAG, "v = " + v);
		l = v.length();
		if (l > 4) {
			v = v.substring(0, l - 3);
		}
		
		if(c != null && v != null ) {
			result[2] = c;
			result[3] = v;
			flag[1] = true;
		} else {
			flag[1] = false;
		}
	}

/*    private String getChargeVol() {
        String[] s = new File("/sys/devices/").list();
        String dir = null;
        for (String d : s ) {
           if ( d.startsWith("qpnp-vadc-"))  {
                dir = d;
                break;
            }
        }
        String raw = extractNodeInfo("/sys/devices/" + dir + "/usb_in");
        int idx1 = raw.indexOf(':');
        int idx2 = raw.indexOf(' ');
        return raw.substring(idx1 + 1, idx2);
        
    }*/
    public String getChargeVol() {
        String changeVoltage = null;
        //Gionee xiaolin 20120302 modify for CR00535627 start
        String mFileName = charegeVolNodepath;
        //Gionee xiaolin 20120302 modify for CR00535627 end
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File voltageFilePath = new File(mFileName);
                if (voltageFilePath.exists()) {
                    fileInputStream = new FileInputStream(voltageFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        changeVoltage = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return changeVoltage;
    }

/*	private String extractNodeInfo(String path) {
		// TODO Auto-generated method stub
	    try {
			InputStream is = new FileInputStream(path);
			int len = is.available();
			byte[] bytes = new byte[len];
			is.read(bytes);
			is.close();
			return new String(bytes).trim();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}*/
    public String extractNodeInfo() {
        String chargeCurrent = null;
        //Gionee xiaolin 20120302 modify for CR00535627 start
        String mFileName = chargeCurNodePath;
        //Gionee xiaolin 20120302 modify for CR00535627 end
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File currentFilePath = new File(mFileName);
                if (currentFilePath.exists()) {
                    fileInputStream = new FileInputStream(currentFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        chargeCurrent = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chargeCurrent;
    }
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		this.registerReceiver(battInfoRec, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.unregisterReceiver(battInfoRec);
		this.finish();
	}

}
