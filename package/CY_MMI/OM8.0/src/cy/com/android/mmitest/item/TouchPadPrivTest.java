
package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.TestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

/*Gionee huangjianqiang 20160216 add for CR01635455 begin*/
import com.focaltech.tp.test.FT_Test;
import com.focaltech.tp.test.FT_Test_FT5X46;
import com.focaltech.tp.test.FT_Test_FT6X36;
import com.focaltech.tp.test.FT_Test_FT5X36;
//import com.focaltech.tp.test.FT_Test_FT5822;
import com.focaltech.tp.test.FT_Test_FT8606;
import com.focaltech.tp.test.FT_Test_FT8716;

import java.io.FileReader;
import android.os.Environment;
/*Gionee huangjianqiang 20160216 add for CR01635455 end*/

import cy.com.android.mmitest.R;
import android.widget.TextView;
import cy.com.android.mmitest.utils.HelPerformUtil;
import cy.com.android.mmitest.bean.OnPerformListen;

public class TouchPadPrivTest extends BaseActivity implements Button.OnClickListener ,OnPerformListen{
    private static final String TAG = "TouchPadPrivTest";
    private static final int CAL_ING = 0;
    private static final int CAL_SUCCESS_OK = 1;
    private static final int CAL_FAIL_FAIL = 2;
    private static final int CAL_ING1 = 3;
    private static final int CAL_SUCCESS_OK1 = 4;
    private static final int CAL_FAIL_FAIL1 = 5;
    private static final int MSG_GET_TPRESULT = 6;
 //   private static final int  NODE_TYPE_TPWAKESWITCH_FACTORY_CHECK=12;    // /sys/bus/platform/devices/tp_wake_switch/factory_check
 //   private static final int NODE_TYPE_MANUFACTURER = 72;// /sys/devices/platform/tp_wake_switch/manufacturer

    public static final String TPWAKE_FACTORY_CHECK = "NODE_TYPE_TPWAKESWITCH_FACTORY_CHECK";
    public static final String TPWAKE_MANUFACTURER = "NODE_TYPE_MANUFACTURER";

    private Button mRightBtn;
    private Button mWrongBtn;
    private Button mRestartBtn;
    private TextView mTitle;
    /*Gionee huangjianqiang 20160216 add for CR01635455 begin*/
    private FT_Test m_Test = null;
    /*Gionee huangjianqiang 20160216 add for CR01635455 end*/
    //Gionee zhangke 20160418 add for CR01679229 start
    private boolean mIsStoped = false;
    //Gionee zhangke 20160418 add for CR01679229 end

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CAL_ING:
                    showDialog(CAL_ING1);
                    mHandler.sendEmptyMessageDelayed(MSG_GET_TPRESULT, 50);
                    break;
                case CAL_SUCCESS_OK:
                    removeDialog(CAL_ING1);

                    mRightBtn.setEnabled(true);
                    mWrongBtn.setEnabled(false);
                    mRestartBtn.setEnabled(true);
                    if (TestUtils.mIsAutoMode) {
                        HelPerformUtil.getInstance().performDelayed(TouchPadPrivTest.this, HelPerformUtil.delayTime);
                    }else {
                        showDialog(CAL_SUCCESS_OK1);
                    }
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
                        DswLog.e(TAG, "bFTTP true ");
                        int result_FT = getTPResultfor_FT();
                        if (result_FT == -1) {
                            DswLog.e(TAG, "FT result == -1 ");
                            mHandler.sendEmptyMessage(CAL_FAIL_FAIL);
                        } else if (result_FT == 0) {
                            DswLog.e(TAG, "FT result == 1 ");
                            mHandler.sendEmptyMessage(CAL_SUCCESS_OK);
                        } else {
                            DswLog.e(TAG, "FT result other");
                            mHandler.sendEmptyMessage(CAL_FAIL_FAIL);
                        }
                    } else {
                        int rel = TestUtils.getNodeState(TouchPadPrivTest.this, TPWAKE_FACTORY_CHECK);
                        DswLog.e(TAG, "TP rel: " + rel);

                        //String result = getTpResult();
                        //DswLog.e(TAG, "TP result: " + result);
                        //if (result != null && "1".equals(result)) {
                        if (rel == -1) {
                            rel = getTpResult();
                        }
                        if (rel == 1) {
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
        DswLog.d(TAG, "\n\n\n****************打开CTP全屏检测 @" + Integer.toHexString(hashCode()));
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.common_textview);

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mTitle = (TextView) findViewById(R.id.test_title);
        mTitle.setText(R.string.ctp_fulltest_notice);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                mRightBtn.setOnClickListener(TouchPadPrivTest.this);
                mWrongBtn.setOnClickListener(TouchPadPrivTest.this);
                mRestartBtn.setOnClickListener(TouchPadPrivTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出CTP全屏检测 @" + Integer.toHexString(hashCode()));
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
                        .setNegativeButton(R.string.touch_priv_pad_ing,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialog = builder2.create();
                break;
            case CAL_SUCCESS_OK1:
                AlertDialog.Builder builder = new AlertDialog.Builder(TouchPadPrivTest.this);
                builder.setMessage(R.string.touch_priv_pad).setCancelable(false)
                        .setNegativeButton(R.string.touch_priv_pad_ok,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialog = builder.create();
                break;
            case CAL_FAIL_FAIL1:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(TouchPadPrivTest.this);
                builder1.setMessage(R.string.touch_priv_pad).setCancelable(false)
                        .setNegativeButton(R.string.touch_priv_pad_fail,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialog = builder1.create();
                break;
        }
        return dialog;
    }


    public int getTpResult() {
        int rel = -1;
        String tpResult = null;
        //Gionee xiaolin 20120302 modify for CR00535627 start
        String mFileName = "/sys/bus/platform/devices/tp_wake_switch/factory_check";
        //Gionee xiaolin 20120302 modify for CR00535627 end

        try {
            BufferedReader ctpBuffer = null;
            File currentFilePath = new File(mFileName);
            if (currentFilePath.exists()) {

                ctpBuffer = new BufferedReader(new FileReader(currentFilePath));
                String data = null;
                while ((data = ctpBuffer.readLine()) != null) {
                    tpResult = data;

                }

            }
            DswLog.i(TAG, "tpResult="+tpResult);
            rel = Integer.parseInt(tpResult);

            if (ctpBuffer != null) {
                ctpBuffer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DswLog.i(TAG, "getTpResult rel="+rel);
        return rel;
    }

    @Override
    public void onClick(View v) {
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
        File desDir = new File("/mnt/sdcard/tpdata");
        if (!desDir.exists()) {
            desDir.mkdirs();
        }

        String result = TestUtils.getNodeContent(this, TPWAKE_MANUFACTURER);

        boolean isFT = false;
        DswLog.i(TAG, "result 1="+result);
        if (result == null) {
            String mFileName = "/sys/devices/platform/tp_wake_switch/manufacturer";
            try {

                BufferedReader ctpBuffer = null;
                File FilePath = new File(mFileName);
                if (FilePath.exists()) {
                    ctpBuffer = new BufferedReader(new FileReader(FilePath));
                    String data = null;
                    while ((data = ctpBuffer.readLine()) != null) {
                        result = data;

                    }

                }
                if (ctpBuffer != null) {
                    ctpBuffer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        DswLog.i(TAG, "result 2="+result);
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
            DswLog.e(TAG, "getTPResultfor_FT m_Test null ");
            return m_iTestResult;
        }
        /*Gionee huangjianqiang 20160406 add for CR01668685 end*/
        m_bDevice = m_Test.initDevice();

        if (!m_bDevice) {
            DswLog.e(TAG, "m_bDevice=" + m_bDevice);
            return m_iTestResult;
        }

        int iVID = m_Test.readReg(0xA8);
        String VidStr = String.format("_0x%02x.ini", iVID);

        DswLog.e(TAG, "Config patch=" + path + VidStr);
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
        m_Test = null;
        DswLog.e(TAG, "=========m_iTestResult = " + m_iTestResult);
        return m_iTestResult;
    }


    private void initFTTestData() {
        String path = "/system/etc/Conf_MultipleTest.ini";
        String result = null;

        try {
            BufferedReader ctpBuffer = null;
            File FilePath = new File(path);
            if (FilePath.exists()) {
                ctpBuffer = new BufferedReader(new FileReader(FilePath));

                String data = null;
                while ((data = ctpBuffer.readLine()) != null) {
                    if (data.contains("IC_Type")) {
                        result = data.split("=")[1].substring(0);
                        break;
                    }
                }
            }
            if (ctpBuffer != null) {
                ctpBuffer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*Gionee huangjianqiang 20160406 add for CR01668685 begin*/
        if (result == null) {
            DswLog.e(TAG, "initFTTestData new null ");
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
            /*case "FT5822":
                m_Test = new FT_Test_FT5822();
                break;*/
            case "FT8606":
                m_Test = new FT_Test_FT8606();
                break;
            case "FT8716":
                m_Test = new FT_Test_FT8716();
                break;
            default:
                DswLog.e(TAG, "initFTTestData new null ");
                m_Test = null;
                break;
        }
        return;
    }
    /*Gionee huangjianqiang 20160216 add for CR01635455 end */
    @Override
    public void OnButtonPerform() {
        HelPerformUtil.getInstance().unregisterPerformListen();
        DswLog.i(TAG, "OnButtonPerform");
        mRightBtn.performClick();
    }
}
