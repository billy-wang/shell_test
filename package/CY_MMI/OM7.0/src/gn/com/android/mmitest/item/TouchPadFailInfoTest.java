
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
import gn.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import gn.com.android.mmitest.R;

//Gionee zhangke 20151210 add for CR01607303 start
import android.widget.TextView;
//Gionee zhangke 20151210 add for CR01607303 end


public class TouchPadFailInfoTest extends BaseActivity implements Button.OnClickListener {
    private static final String TAG = "TouchPadFailInfoTest";
    private static final int CAL_ING = 0;
    private static final int CAL_SUCCESS_OK = 1;
    private static final int CAL_FAIL_FAIL = 2;
    private static final int CAL_ING1 = 3;
    private static final int CAL_SUCCESS_OK1 = 4;
    private static final int CAL_FAIL_FAIL1 = 5;
    private static final int MSG_GET_TPRESULT = 6;
    //Gionee zhangke 20151210 add for CR01607303 start
    private static final int MSG_SHOW_CODEDATA = 7;
    //Gionee zhangke 20151210 add for CR01607303 end

    private Button mRightBtn;
    private Button mWrongBtn;
    private Button mStopBtn;
    private Button mRestartBtn;

    //Gionee zhangke 20151210 add for CR01607303 start
    private TextView mContent;
    private String mCodeData;
    //Gionee zhangke 20151210 add for CR01607303 start

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
                    mWrongBtn.setEnabled(false);
                    break;
                case CAL_FAIL_FAIL:
                    removeDialog(CAL_ING1);
                    showDialog(CAL_FAIL_FAIL1);
                    mRightBtn.setEnabled(false);
                    break;
                case MSG_GET_TPRESULT:
                    String result = getTpResult();
                    DswLog.e(TAG, "TP result: " + result);
                    if ("1".equals(result)) {
                        mHandler.sendEmptyMessage(CAL_SUCCESS_OK);
                    } else {
                        mHandler.sendEmptyMessage(CAL_FAIL_FAIL);
                    }
                    break;
                //Gionee zhangke 20151210 add for CR01607303 start
                case MSG_SHOW_CODEDATA:
                    mContent.setText(mCodeData);
                    break;
                //Gionee zhangke 20151210 add for CR01607303 end
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.touch_fail_info_test);

        mContent = (TextView) findViewById(R.id.test_content);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mRightBtn.setEnabled(true);

        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);

        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Gionee zhangke 20160116 modify for CR01624132 start
        //mHandler.sendEmptyMessage(CAL_ING);
        //Gionee zhangke 20160116 modify for CR01624132 end
        //Gionee zhangke 20151210 add for CR01607303 start
        new Thread(new Runnable() {
            public void run() {
                mCodeData = getCodeData("proc/rawdata");
                mHandler.sendEmptyMessage(MSG_SHOW_CODEDATA);
            }
        }).start();
        //Gionee zhangke 20151210 add for CR01607303 end

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case CAL_ING1:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(TouchPadFailInfoTest.this);
                builder2.setMessage(R.string.touch_priv_pad).setCancelable(false)
                        .setPositiveButton(R.string.touch_priv_pad_ing, null);
                dialog = builder2.create();
                break;
            case CAL_SUCCESS_OK1:
                AlertDialog.Builder builder = new AlertDialog.Builder(TouchPadFailInfoTest.this);
                builder.setMessage(R.string.touch_priv_pad).setCancelable(false)
                        .setPositiveButton(R.string.touch_priv_pad_ok, null);
                dialog = builder.create();
                break;
            case CAL_FAIL_FAIL1:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(TouchPadFailInfoTest.this);
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

    //Gionee zhangke 20151210 add for CR01607303 start
    public String getCodeData(String fileName) {
        StringBuffer codeData = new StringBuffer("");
        String mFileName = null;
        mFileName = fileName;
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
                        codeData.append(data);
                        codeData.append("\n");
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
        DswLog.d(TAG, fileName + ":codeData=" + codeData.toString());
        return codeData.toString();
    }
    //Gionee zhangke 20151210 add for CR01607303 end
}
