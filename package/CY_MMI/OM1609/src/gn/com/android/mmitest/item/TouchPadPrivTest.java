
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.TestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/*Gionee huangjianqiang 20160216 add for CR01635455 begin*/
import com.focaltech.tp.test.FT_Test;
import com.focaltech.tp.test.FT_Test_FT5X46;
import com.focaltech.tp.test.FT_Test_FT6X36;
import com.focaltech.tp.test.FT_Test_FT5X36;
import com.focaltech.tp.test.FT_Test_FT5822;
import com.focaltech.tp.test.FT_Test_FT8606;
import com.focaltech.tp.test.FT_Test_FT8716;

import android.os.Environment;
/*Gionee huangjianqiang 20160216 add for CR01635455 end*/

import gn.com.android.mmitest.R;

public class TouchPadPrivTest extends BaseActivity implements Button.OnClickListener {
    private static final String TAG = "TouchPadPrivTest";
    private static final int CAL_ING = 0;
    private static final int CAL_SUCCESS_OK = 1;
    private static final int CAL_FAIL_FAIL = 2;
    private static final int CAL_ING1 = 3;
    private static final int CAL_SUCCESS_OK1 = 4;
    private static final int CAL_FAIL_FAIL1 = 5;
    private static final int MSG_GET_TPRESULT = 6;

    private Button mRightBtn;
    private Button mWrongBtn;
    private Button mStopBtn;
    private Button mRestartBtn;
    /*Gionee huangjianqiang 20160216 add for CR01635455 begin*/
    private FT_Test m_Test = null;
    /*Gionee huangjianqiang 20160216 add for CR01635455 end*/
    //Gionee zhangke 20160418 add for CR01679229 start
    private boolean mIsStoped = false;
    //Gionee zhangke 20160418 add for CR01679229 end

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case CAL_ING:
                    showDialog(CAL_ING1);
                    mHandler.sendEmptyMessageDelayed(MSG_GET_TPRESULT, 50);
                    break;
                case CAL_SUCCESS_OK:
                    removeDialog(CAL_ING1);
                    showDialog(CAL_SUCCESS_OK1);
                    mRightBtn.setEnabled(true);
                    mWrongBtn.setEnabled(false);
                    mRestartBtn.setEnabled(true);
                    break;
                case CAL_FAIL_FAIL:
                    removeDialog(CAL_ING1);
                    showDialog(CAL_FAIL_FAIL1);
                    mRightBtn.setEnabled(false);
                    mWrongBtn.setEnabled(true);
                    mRestartBtn.setEnabled(true);
                    break;
                case MSG_GET_TPRESULT:
                    /*Gionee huangjianqiang 20160216 modify for CR01635455 begin*/
                    boolean bFTTP = isFTTP();
                    if (bFTTP) {
                        Log.e(TAG, "bFTTP true ");
                        int result_FT = getTPResultfor_FT();
                        if (result_FT == -1) {
                            Log.e(TAG, "FT result == -1 ");
                            mHandler.sendEmptyMessage(CAL_FAIL_FAIL);
                        } else if (result_FT == 0) {
                            Log.e(TAG, "FT result == 1 ");
                            mHandler.sendEmptyMessage(CAL_SUCCESS_OK);
                        } else {
                            Log.e(TAG, "FT result other");
                            mHandler.sendEmptyMessage(CAL_FAIL_FAIL);
                        }
                    } else {
                        String result = getTpResult();
                        Log.e(TAG, "TP result: " + result);
                        if (result != null && "1".equals(result)) {
                            mHandler.sendEmptyMessage(CAL_SUCCESS_OK);
                        } else {
                            mHandler.sendEmptyMessage(CAL_FAIL_FAIL);
                        }
                    }
                    /*Gionee huangjianqiang 20160216 modify for CR01635455 end*/
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.common_textview);

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                mRightBtn.setOnClickListener(TouchPadPrivTest.this);
                mWrongBtn.setOnClickListener(TouchPadPrivTest.this);
                mRestartBtn.setOnClickListener(TouchPadPrivTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(CAL_ING);
    }

    @Override
    public void onStop() {
        super.onStop();
        //Gionee zhangke 20160418 add for CR01679229 start
        mIsStoped = true;
        //Gionee zhangke 20160418 add for CR01679229 end
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        //Gionee zhangke 20160418 add for CR01679229 start
        if(mIsStoped){
            return dialog;
        }
        //Gionee zhangke 20160418 add for CR01679229 end
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
        }
        return dialog;
    }

    public String getTpResult() {
        String tpResult = null;
        //Gionee xiaolin 20120302 modify for CR00535627 start
        String mFileName = "/sys/bus/platform/devices/tp_wake_switch/factory_check";
        //Gionee xiaolin 20120302 modify for CR00535627 end
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
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
            closeStreamQuiet(fileInputStream);
            closeReaderQuiet(inputStreamReader);
            closeReaderQuiet(br);
        }
        return tpResult;
    }

    void closeStreamQuiet(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream = null;
            }
        }
    }

    void closeReaderQuiet(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                reader = null;
            }
        }
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

    /*Gionee huangjianqiang 20160216 add for CR01635455 beign */
    private boolean isFTTP() {
        String mFileName = "/sys/devices/platform/tp_wake_switch/manufacturer";
        String result = null;
        boolean isFT = false;
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
            closeStreamQuiet(fileInputStream);
            closeReaderQuiet(inputStreamReader);
            closeReaderQuiet(br);
        }

        if (result == null) {
            isFT = false;
        } else if (result.equals("FT")) {
            isFT = true;
        } else {
            isFT = false;
        }

        return isFT;
    }

    public int getTPResultfor_FT() {
        int m_iTestResult = -1;
        boolean m_bDevice = false;
        String path = "/system/etc/Conf_MultipleTest";
        initFTTestData();
        /*Gionee huangjianqiang 20160406 add for CR01668685 begin*/
        if (m_Test == null) {
            Log.e(TAG, "getTPResultfor_FT m_Test null ");
            return m_iTestResult;
        }
        /*Gionee huangjianqiang 20160406 add for CR01668685 end*/
        m_bDevice = m_Test.initDevice();

        if (!m_bDevice) {
            Log.e(TAG, "m_bDevice=" + m_bDevice);
            return m_iTestResult;
        }

        int iVID = m_Test.readReg(0xA8);
        String VidStr = String.format("_0x%02x.ini", iVID);

        Log.e(TAG, "Config patch=" + path + VidStr);
        try {
            File FilePath = new File(path + VidStr);
            if (FilePath.exists()) {
                m_Test.loadConfig(path + VidStr);
            } else {
                m_Test.loadConfig(path + ".ini");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        m_Test.createReport(Environment.getExternalStorageDirectory().getPath());
        try {
            m_iTestResult = m_Test.startTestTP();
        } catch (Exception e) {
            e.printStackTrace();
        }
        m_Test.closeReport();
        m_Test.releaseDevice();

        Log.e(TAG, "=========m_iTestResult = " + m_iTestResult);
        return m_iTestResult;
    }


    private void initFTTestData() {
        String path = "/system/etc/Conf_MultipleTest.ini";
        String result = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            File FilePath = new File(path);
            if (FilePath.exists()) {
                fileInputStream = new FileInputStream(FilePath);
                inputStreamReader = new InputStreamReader(fileInputStream);
                br = new BufferedReader(inputStreamReader);
                String data = null;
                while ((data = br.readLine()) != null) {
                    if (data.contains("IC_Type")) {
                        result = data.split("=")[1].substring(0);
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStreamQuiet(fileInputStream);
            closeReaderQuiet(inputStreamReader);
            closeReaderQuiet(br);
        }
        /*Gionee huangjianqiang 20160406 add for CR01668685 begin*/
        if (result == null) {
            Log.e(TAG, "initFTTestData new null ");
            m_Test = null;
            return;
        }
        /*Gionee huangjianqiang 20160406 add for CR01668685 end*/
        switch (result) {
            case "FT5X36":
                m_Test = new FT_Test_FT5X36();
                break;
            /*Gionee huangjianqiang 20160530 add for CR01710010 begin*/
            case "FT3X27":
            /*Gionee huangjianqiang 20160530 add for CR01710010 end*/
            case "FT5X46":
                m_Test = new FT_Test_FT5X46();
                break;
            case "FT6X36":
            case "FT3X07":
                m_Test = new FT_Test_FT6X36();
                break;
            case "FT5822":
                m_Test = new FT_Test_FT5822();
                break;
            case "FT8606":
                m_Test = new FT_Test_FT8606();
                break;
            case "FT8716":
                m_Test = new FT_Test_FT8716();
                break;
            default:
                Log.e(TAG, "initFTTestData new null ");
                m_Test = null;
                break;
        }
        return;
    }
    /*Gionee huangjianqiang 20160216 add for CR01635455 end */
}
