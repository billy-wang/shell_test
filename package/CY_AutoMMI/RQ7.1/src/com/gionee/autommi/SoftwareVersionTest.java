package com.gionee.autommi;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.os.Bundle;
import android.os.SystemProperties;
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;
import java.io.IOException;
import android.util.Log;


public class SoftwareVersionTest extends BaseActivity implements QcRilHookCallback {
	private static final String EXTRA_VER = "ver";
    public static final String TAG = "SoftwareVersionTest";
    private String targetVer;
    private String currVer;
    private  String sn ;
    private String factoryResult;
    private char[] content = {'2', '2', '2', '2', '2', '2', '2','2', '2', '2', '2', '2', '2'};
    QcNvItems nvItems ;
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            targetVer = this.getIntent().getStringExtra(EXTRA_VER);
            nvItems = new QcNvItems(this,this);
            try {
                nvItems.getEgmrResult();
                Log.d(TAG, "sn:"+sn);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

            public void onQcRilHookReady(){  
            try {
                sn = nvItems.getEgmrResult();
                Log.d(TAG, "sn:"+sn);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                factoryResult = nvItems.getFactoryResult();
                Log.d(TAG, factoryResult+" : "+factoryResult.length());
            } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }

            currVer = SystemProperties.get("ro.gn.gnznvernumber");
            if ("eng".equals(SystemProperties.get("ro.build.type"))) {
                currVer += "_eng";
            }
            Log.e(TAG," currVer = " + currVer);
            process();
            nvItems.dispose();
	}
	private void process() {
		// TODO Auto-generated method stub
		if(targetVer.equalsIgnoreCase(currVer))
			content[0] = '1';
		else
			content[0] = '0';
		
		if(null != factoryResult) {
			char[] barcodes = factoryResult.toCharArray();
			// GSM BT
			if( barcodes.length > 6 && 'P' == barcodes[5] )
				content[1] = '1';
			if(barcodes.length > 6 && 'F' == barcodes[5])
				content[1] = '0';
			
			//GSM FT
			if(barcodes.length > 7 &&  'P' == barcodes[6])
				content[2] = '1';
			if ( barcodes.length > 7 &&'F' == barcodes[6])
				content[2] = '0';
			
			//WCDMA BT
			if (barcodes.length > 8 &&'P' == barcodes[7])
				content[3] = '1';
			if (barcodes.length > 8 &&'F' == barcodes[7])
				content[3] = '0';
			
			//WCDMA FT
			if(barcodes.length > 9 &&'P' == barcodes[8])
				content[4] = '1';
			if(barcodes.length > 9 &&'F' == barcodes[8])
				content[4] = '0';
			
			//TD BT
			if (barcodes.length > 27 &&'P' == barcodes[26])
				content[5] = '1';
			if (barcodes.length > 27 &&'F' == barcodes[26])
				content[5] = '0';
			//TD FT
			if (barcodes.length > 28 &&'P' == barcodes[27])
				content[6] = '1';
			if (barcodes.length > 28 &&'F' == barcodes[27])
				content[6] = '0';
			
			//LTE TDD BT
			if (barcodes.length > 29 &&'P' == barcodes[28])
				content[7] = '1';
			if (barcodes.length > 29 &&'F' == barcodes[28])
				content[7] = '0';

			//LTE TDD FT
			if (barcodes.length > 30 &&'P' == barcodes[29])
				content[8] = '1';
			if (barcodes.length > 30 &&'F' == barcodes[29])
				content[8] = '0';
                
			//LTE FDD BT
			if (barcodes.length > 31 &&'P' == barcodes[30])
				content[9] = '1';
			if (barcodes.length > 31 &&'F' == barcodes[30])
				content[9] = '0';

			//LTE FDD FT
			if (barcodes.length >= 32 &&'P' == barcodes[31])
				content[10] = '1';
			if (barcodes.length >= 32 &&'F' == barcodes[31])
				content[10] = '0';
			//CDMA BT
			if (barcodes.length > 10 &&'P' == barcodes[9])
				content[11] = '1';
			if (barcodes.length > 10 &&'F' == barcodes[9])
				content[11] = '0';
			//CDMA FT
			if (barcodes.length > 11 &&'P' == barcodes[10])
				content[12] = '1';
			if (barcodes.length > 11 &&'F' == barcodes[10])
				content[12] = '0';

			String cnt = new String(content);
			Toast.makeText(this, cnt, Toast.LENGTH_LONG).show();
			Log.e(TAG, "cnt = "+ cnt);
			Integer result;  
			if(SystemProperties.getBoolean("gn.mmi.tdscdma", false)){
				result = cnt.equalsIgnoreCase("1111111111111")? 1: 0;
			}
			else{
				result =  cnt.equalsIgnoreCase("1111122111111")? 1: 0;
			}
			Log.e(TAG, "result = "+ result);
			if (sn != null && sn.length() > 0) {
				((AutoMMI)getApplication()).recordResult(TAG, cnt + "|" + sn.subSequence(0, 18), result.toString());
			} else {
				Log.v(TAG, "write record result error!");
			}
			
		}
	}
        @Override
        protected void onStop() {
            // TODO Auto-generated method stub
            super.onStop(); 
            finish();
        }

}
