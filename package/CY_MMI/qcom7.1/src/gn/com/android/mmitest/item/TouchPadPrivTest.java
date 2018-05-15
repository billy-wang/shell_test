package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.content.DialogInterface;
import android.telephony.TelephonyManager;
import android.content.Context;


public class TouchPadPrivTest extends Activity implements Button.OnClickListener {
    private Vibrator mVibrator;
    private Button mRightBtn, mWrongBtn, mStopBtn, mRestartBtn;
    private static final String TAG = "TouchPadPrivTest";
    private static final int CAL_ING = 0;
    private static final int CAL_SUCCESS_OK = 1;
    private static final int CAL_FAIL_FAIL = 2;
    private static final int CAL_ING1 = 3;
    private static final int CAL_SUCCESS_OK1 = 4;
    private static final int CAL_FAIL_FAIL1 = 5;
    private static final int CAL_PREPARE = 6;
    //Gionee <GN_BSP_MMI><lifeilong><20161228> modify for ID 55033 being
    private static final int START_THREAD= 7;
    //Gionee <GN_BSP_MMI><lifeilong><20161228> modify for ID  55033 end

    private int mManuFacturer;
    private static final int MANUFACTURER_NORMAL = 0;
    private static final int MANUFACTURER_FT = 1;
    private static final int MANUFACTURER_ST = 2;
    //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45780 begin
    private static final String IMEI_DATA = "NODE_TYPE_IMEI_DATA";///sys/bus/platform/devices/tp_wake_switch/get_imei_data
    //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45780 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.common_textview);
		//Gionee <GN_MMI><lifeilong><20161101> modify for 16405 begin
		createTpDataFolder();
		//Gionee <GN_MMI><lifeilong><20161101> modify for 16405 end

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mRightBtn.setEnabled(true);
        //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45780 begin
        TelephonyManager telephonyManager=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String imei=telephonyManager.getDeviceId();
        Log.e(TAG," imei = " + imei);
        //Gionee <GN_BSP_MMI><lifeilong><20161220> modify for ID 50631 begin
        //if(imei.equals("0")){
        //Gionee <GN_BSP_MMI><lifeilong><20161220> modify for ID 50914 begin
        //if("0".equals(imei)){
        if("0".equals(imei) || imei == null){
        //Gionee <GN_BSP_MMI><lifeilong><20161220> modify for ID 50631 end
            boolean result = TestUtils.writeNodeState(TouchPadPrivTest.this,IMEI_DATA,0);
                if(result){
                    if(imei == null){
                        Log.e(TAG,"imei_node --- > write 0 ===== imei = null");
                    }else if("0".equals(imei)){
                        Log.e(TAG,"imei_node --- > write 0 ===== imei = 0");
                    }
                }
        //Gionee <GN_BSP_MMI><lifeilong><20161220> modify for ID 50914 end
        }else {
            boolean result = TestUtils.writeNodeState(TouchPadPrivTest.this,IMEI_DATA,1);
                if(result){
                    Log.e(TAG,"imei_node --- > write 1 ===== imei = " + imei);
                }				 
        }
        //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45780 end

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "result onResume ");
        String manuFacturer = getManuFacturer();
        Log.e(TAG, "manuFacturer=" + manuFacturer);
        if (manuFacturer == null) {
            mManuFacturer = MANUFACTURER_NORMAL;
        } else if (manuFacturer.equalsIgnoreCase("FT")) {
            mManuFacturer = MANUFACTURER_FT;
        } else if (manuFacturer.equalsIgnoreCase("ST")) {
            mManuFacturer = MANUFACTURER_ST;
        } else {
            mManuFacturer = MANUFACTURER_NORMAL;
        }
        if (mManuFacturer == MANUFACTURER_ST) {
            mUiHandler.sendEmptyMessage(CAL_PREPARE);
        } else {
            testThread.start();
        }

    }

    Thread testThread = new Thread() {
        @Override
        public void run() {
            try {
                //Gionee <GN_BSP_MMI><lifeilong><20161228> modify for ID 55033 being
                mUiHandler.sendEmptyMessage(CAL_ING);
                //Gionee <GN_BSP_MMI><lifeilong><20161228> modify for ID 55033 end

                String result = getTpResult();
                Log.e(TAG, "result Number = " + result);

                if (result == null) {
                    Log.e(TAG, "result == null ");
                    mUiHandler.sendEmptyMessage(CAL_FAIL_FAIL);
                } else if (result.equals("1")) {
                    Log.e(TAG, "result == 1 ");
                    mUiHandler.sendEmptyMessage(CAL_SUCCESS_OK);
                } else {
                    Log.e(TAG, "result other");
                    mUiHandler.sendEmptyMessage(CAL_FAIL_FAIL);
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    };

    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CAL_ING:
                Log.e(TAG, "show dialog CAL_ING");
                if (!isFinishing()) {
                    showDialog(CAL_ING1);
                }
                break;
            case CAL_SUCCESS_OK:
                removeDialog(CAL_ING1);
                if (!isFinishing()) {
                    showDialog(CAL_SUCCESS_OK1);
                }
                mWrongBtn.setEnabled(false);
                break;
            case CAL_FAIL_FAIL:
                // calibsucc = false;
                removeDialog(CAL_ING1);
                if (!isFinishing()) {
                    showDialog(CAL_FAIL_FAIL1);
                }
                mRightBtn.setEnabled(false);
                break;
            case CAL_PREPARE:
                Log.e(TAG, "show dialog CAL_PREPARE");
                if (!isFinishing()) {
                    showDialog(CAL_PREPARE);
                }
                break;
            //Gionee <GN_BSP_MMI><lifeilong><20161228> modify for ID 55033 being
            case START_THREAD:
                Log.e(TAG, "START_THREAD");
                testThread.start();
                break;
            //Gionee <GN_BSP_MMI><lifeilong><20161228> modify for ID 55033 end
            }

        }
    };

    @Override
    public void onStop() {
        super.onStop();
    }

	//Gionee <GN_MMI><lifeilong><20161101> modify for 16405 begin
	private void createTpDataFolder(){
	File tpDataFolder = new File("/mnt/sdcard/tpdata/");
	if (!tpDataFolder.exists()) {
		try {
			tpDataFolder.mkdir(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
	}
	//Gionee <GN_MMI><lifeilong><20161101> modify for 16405 end 


    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case CAL_ING1:
            AlertDialog.Builder builder2 = new AlertDialog.Builder(TouchPadPrivTest.this);
            builder2.setMessage(R.string.touch_priv_pad).setCancelable(false)
                    .setPositiveButton(R.string.touch_priv_pad_ing, null);
            dialog = builder2.create();
            break;
        case CAL_SUCCESS_OK1:
            AlertDialog.Builder builder = new AlertDialog.Builder(TouchPadPrivTest.this);
            builder.setMessage(R.string.touch_priv_pad).setCancelable(false)
                    .setPositiveButton(R.string.touch_priv_pad_ok, null);
            dialog = builder.create();
            break;
        case CAL_FAIL_FAIL1:
            AlertDialog.Builder builder1 = new AlertDialog.Builder(TouchPadPrivTest.this);
            builder1.setMessage(R.string.touch_priv_pad).setCancelable(false)
                    .setPositiveButton(R.string.touch_priv_pad_fail, null);
            dialog = builder1.create();
            break;
        case CAL_PREPARE:
            AlertDialog.Builder builder3 = new AlertDialog.Builder(TouchPadPrivTest.this);
            builder3.setMessage(R.string.touch_priv_pad_pre_note).setCancelable(false).setPositiveButton(
                    R.string.touch_priv_pad_start_test, new android.content.DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Gionee <GN_BSP_MMI><lifeilong><20161228> modify for ID 55033 being
                            //testThread.start();
                            mUiHandler.sendEmptyMessage(CAL_ING);
                            mUiHandler.sendEmptyMessageDelayed(START_THREAD,2000);
                            //Gionee <GN_BSP_MMI><lifeilong><20161228> modify for ID 55033 end
                        }

                    });
            dialog = builder3.create();
            break;

        }
        return dialog;
    }

    public String getTpResult() {
        String tpResult = null;
        // Gionee xiaolin 20120302 modify for CR00535627 start
        String mFileName = "/sys/bus/platform/devices/tp_wake_switch/factory_check";
        // Gionee xiaolin 20120302 modify for CR00535627 end
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
                        tpResult = data;
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
        return tpResult;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

        case R.id.right_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.rightPress(TAG, this);
            break;
        }

        case R.id.wrong_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.wrongPress(TAG, this);
            break;
        }

        case R.id.restart_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.restart(this, TAG);
            break;
        }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private String getManuFacturer() {
        String mFileName = "/sys/devices/platform/tp_wake_switch/manufacturer";
        String result = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;

        try {
            File FilePath = new File(mFileName);
            if (FilePath.exists()) {
                fileInputStream = new FileInputStream(FilePath);
                inputStreamReader = new InputStreamReader(fileInputStream);
                br = new BufferedReader(inputStreamReader);
                String data = null;
                while ((data = br.readLine()) != null) {
                    result = data;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
