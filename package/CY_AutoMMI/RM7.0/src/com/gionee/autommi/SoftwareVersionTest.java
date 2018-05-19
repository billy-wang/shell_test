package com.gionee.autommi;

import android.app.Activity;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.os.Bundle;
import android.os.SystemProperties;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import android.os.Message;
import android.os.AsyncResult;
import java.io.UnsupportedEncodingException;
import android.os.SystemProperties;
import android.os.Handler;
import com.android.internal.util.HexDump;
import android.content.Intent;

//import amigo.provider.AmigoSettings;

public class SoftwareVersionTest extends BaseActivity {
    private static final String EXTRA_VER = "ver";
    public static final String TAG = "SoftwareVersionTest";
    private String targetVer;
    private String currVer;
    private  String sn ;
    private char[] content = {'2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2'};
    //Gionee zhangke 20151019 add for CR01571097 start 
    private char[] mSuccessResult = {'1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1'};
    //Gionee zhangke 20151019 add for CR01571097 end
    private boolean flag = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        targetVer = this.getIntent().getStringExtra(EXTRA_VER);
        //获取sn
        sn = getSN();
        Log.e(TAG," sn = " + sn);
        Intent intent = getIntent();
        if(intent != null){
            flag = intent.getBooleanExtra("as", false);
        }
        Log.d(TAG,"flag = " + flag);
        if(flag){
            ((AutoMMI)getApplication()).asResult(TAG, "", "2");
        }
        // 合成软件版本号
        currVer = SystemProperties.get("ro.gn.gnznvernumber");
        if ("eng".equals(SystemProperties.get("ro.build.type"))) {
            currVer += "_eng";
        }
        Log.e(TAG," currVer11 = " + currVer);
        //Gionee zhangke 20151216 modify start
        if(sn == null || sn.isEmpty()){
            Toast.makeText(this, "Error: SN is null", Toast.LENGTH_LONG).show();
        }else{
            process();
        }
        //Gionee zhangke 20151216 modify end
        //AmigoSettings.putInt(this.getContentResolver(), AmigoSettings.Button_Light_State, 0);
    }

    private String getSN() {
        return SystemProperties.get("gsm.serial");
    }

        private void process() {
            // TODO Auto-generated method stub
            if(targetVer.equalsIgnoreCase(currVer)){
                content[0] = '1';
             } else{
                content[0] = '0';
            }

            if(null != sn) {
                    char[] barcodes = sn.toCharArray();
                // GSM BT
                if(barcodes.length > 62 && '1' == barcodes[60] && '0' == barcodes[61])
                    content[1] = '1';
                if(barcodes.length > 62 && '0' == barcodes[60] && '1' == barcodes[61])
                    content[1] = '0';

                //GSM FT
                if( barcodes.length >= 63 && 'P' == barcodes[62])
                    content[2] = '1';
                if ( barcodes.length >= 63 && 'F' == barcodes[62])
                    content[2] = '0';

                //WCDMA BT
                if (barcodes.length >= 59 && 'P' == barcodes[58])
                    content[3] = '1';
                if (barcodes.length >= 59 && 'F' == barcodes[58])
                    content[3] = '0';

                //WCDMA FT
                if(barcodes.length >=60 && 'P' == barcodes[59])
                    content[4] = '1';
                if(barcodes.length >=60 && 'F' == barcodes[59])
                    content[4] = '0';

                //TD BT
                if (barcodes.length >= 47 && 'P' == barcodes[46])
                    content[5] = '1';
                if (barcodes.length >= 47 && 'F' == barcodes[46])
                    content[5] = '0';
                //TD FT
                if (barcodes.length >= 48 && 'P' == barcodes[47])
                    content[6] = '1';
                if (barcodes.length >= 48 && 'F' == barcodes[47])
                    content[6] = '0';

                //LTE TDD BT,不进行对比，直接显示接口，没有的默认是2。只对软件版本号进行对比
                if (barcodes.length >= 43 &&'P' == barcodes[42])
                    content[7] = '1';
                if (barcodes.length >= 43 &&'F' == barcodes[42])
                    content[7] = '0';

                //LTE TDD FT
                if (barcodes.length >= 44 &&'P' == barcodes[43])
                    content[8] = '1';
                if (barcodes.length >= 44 &&'F' == barcodes[43])
                    content[8] = '0';

                //LTE FDD BT
                if (barcodes.length >= 41 &&'P' == barcodes[40])
                    content[9] = '1';
                if (barcodes.length >= 41 &&'F' == barcodes[40])
                    content[9] = '0';

                //LTE FDD FT
                if (barcodes.length >= 42 &&'P' == barcodes[41])
                    content[10] = '1';
                if (barcodes.length >= 42 &&'F' == barcodes[41])
                    content[10] = '0';

                //Gionee zhangke 20151116 add for CR01591932 start
                //GSMC2 BT
                if (barcodes.length >= 39 &&'P' == barcodes[38])
                    content[11] = '1';
                if (barcodes.length >= 39 &&'F' == barcodes[38])
                    content[11] = '0';
                //GSMC2 FT
                if (barcodes.length >= 40 &&'P' == barcodes[39])
                    content[12] = '1';
                if (barcodes.length >= 40 &&'F' == barcodes[39])
                    content[12] = '0';

                //CDMA BT
                if (barcodes.length >= 36 &&'P' == barcodes[35])
                    content[13] = '1';
                if (barcodes.length >= 36 &&'F' == barcodes[35])
                    content[13] = '0';
                //CDMA FT
                if (barcodes.length >= 37 &&'P' == barcodes[36])
                    content[14] = '1';
                if (barcodes.length >= 37 &&'F' == barcodes[36])
                    content[14] = '0';
                //GPSCOC
                if (barcodes.length >= 34 &&'P' == barcodes[33])
                    content[15] = '1';
                if (barcodes.length >= 34 &&'F' == barcodes[33])
                    content[15] = '0';
                //GM 3.0
                if (barcodes.length >= 26 &&'P' == barcodes[25])
                    content[16] = '1';
                if (barcodes.length >= 26 &&'F' == barcodes[25])
                    content[16] = '0';

                //Gionee zhangke 20151116 add for CR01591932 end

                //Gionee zhangke 20151116 modify for CR01591932 start
                Integer result;  
                if(!FeatureOption.GN_RW_GN_MMI_WCDMA_SUPPORT){
                    mSuccessResult[3] = '2';
                    mSuccessResult[4] = '2';
                }
                if(!FeatureOption.GN_RW_GN_MMI_TDSCDMA_SUPPORT){
                    mSuccessResult[5] = '2';
                    mSuccessResult[6] = '2';
                }
                if(!FeatureOption.GN_RW_GN_MMI_LTETDD_SUPPORT){
                    mSuccessResult[7] = '2';
                    mSuccessResult[8] = '2';
                }
                if(!FeatureOption.GN_RW_GN_MMI_LTEFDD_SUPPORT){
                    mSuccessResult[9] = '2';
                    mSuccessResult[10] = '2';
                }
                if(!FeatureOption.GN_RW_GN_MMI_GSMC2_SUPPORT){
                    mSuccessResult[11] = '2';
                    mSuccessResult[12] = '2';
                }
                if(!FeatureOption.GN_RW_GN_MMI_CDMA_SUPPORT){
                    mSuccessResult[13] = '2';
                    mSuccessResult[14] = '2';
                }
                if(!FeatureOption.GN_RW_GN_MMI_GPS_COCLOCK_SUPPORT){
                    mSuccessResult[15] = '2';
                }
                if(!FeatureOption.GN_RW_GN_MMI_GM3_SUPPORT){
                    mSuccessResult[16] = '2';
                }

                String cnt = new String(content);
                String successResult = new String(mSuccessResult);

                result =  cnt.equalsIgnoreCase(successResult)? 1: 0;
                Toast.makeText(this, cnt, Toast.LENGTH_LONG).show();
                Log.i(TAG,"content="+ cnt +";mSuccessResult=" + successResult +";result="+result);
                //Gionee zhangke 20151116 add for CR01591932 end
                if(flag){
                    ((AutoMMI)getApplication()).asResult(TAG, cnt + "|" + sn.subSequence(0, 18), result.toString());
                } else {
                    ((AutoMMI)getApplication()).recordResult(TAG, cnt + "|" + sn.subSequence(0, 18), result.toString());
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
