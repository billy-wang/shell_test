package com.cydroid.autommi;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.os.SystemProperties;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class InfoTest extends BaseActivity {
	
    private final String TAG = "InfoTest";
	private TelephonyManager teleMgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		teleMgr  = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); 
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String softVer = SystemProperties.get("ro.cy.znvernumber");
        if("eng".equals(SystemProperties.get("ro.build.type"))) {
            softVer += "_eng";
        }   
        String devID = teleMgr.getDeviceId();
        String sn = getSN();
        String info = softVer + "|" + devID + "|" + sn;
		((AutoMMI)getApplication()).recordResult(TAG, info, "2");
		Toast.makeText(this, info, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		this.finish();
	}

    private String getSN() {
        try {
            Class c = Class.forName("android.telephony.TelephonyManager");
            Method m = c.getMethod("getSN", (Class[]) null); 
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            return (String) m.invoke(tm, (Object[]) null);
        } catch(ClassNotFoundException e){ 
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return SystemProperties.get("gsm.serial");
    }
	
}
