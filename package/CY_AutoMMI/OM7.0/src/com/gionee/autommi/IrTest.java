package com.gionee.autommi;


import com.gionee.autommi.IrControl;
import com.gionee.autommi.R;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Handler;
import com.gionee.util.DswLog;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gionee.util.SystemUtil;
import com.gionee.util.SystemUtil.PNAME;
import android.content.pm.PackageManager;
public class IrTest extends BaseActivity {

    private static final String TAG = "IrTest";
    private static final int READED = 1;
    private byte mKey1[] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    private boolean mIsReceiving = false;
    private IrControl mIRControl;
    private boolean mIsPass;
    //    private Button mSendButton;
//    private Button mReceiveButton;
    private TextView mFrequencyTextView;
    private TextView mDataTextView;
    //Gionee ningsy GNSPR25613 20160704 begin
    ConsumerIrManager cm;
    private boolean mHasConsumerIr;
    boolean flag = true;// 判断是否停止发送红外的标志
    int Freq_1 = 38000, Freq_2 = 40059, Freq_3 = 38000;
    int Key10[] = {340, 170, 22, 63, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 63, 22, 63, 22, 63, 22, 63, 22, 63, 22, 63, 22, 63, 22, 19, 22, 63, 22, 63, 22, 63, 22, 19, 22, 19, 22, 19, 22, 19, 22, 63, 22, 19, 22, 19, 22, 19, 22, 63, 22, 63, 22, 63, 22, 63, 22, 756};
    int Key11[] = {95, 26, 46, 26, 23, 26, 46, 26, 22, 25, 47, 26, 22, 26, 23, 26, 22, 25, 47, 27, 22, 26, 47, 26, 47, 27, 46, 26, 46, 26, 47, 25, 47, 26, 22, 26, 47, 25, 47, 27, 22, 300};
    int Key12[] = {172, 171, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 1777, 172, 171, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 1777, 172, 171, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 3806};
    int[] Key13 = {9084, 4580, 511, 623, 511, 597, 538, 597, 511, 623, 485, 623, 538, 623, 485, 623, 485, 623, 511, 1757, 485, 1757, 511, 1757, 538, 1757, 485, 1757, 511, 1757, 511, 1757, 511, 1757, 511, 649, 459, 1757, 511, 1757, 511, 623, 511, 649, 511, 597, 512, 623, 511, 623, 485, 1757, 511, 623, 485, 623, 538, 1757, 485, 1757, 511, 1757, 511, 1757, 485, 1757, 511, 40135, 9084, 2337, 511, 95529};
    private PNAME mPname;
    private LinearLayout mIRdataLL, mIRfreqLL;
    //Gionee ninsgy GNSPR25613 20160704 end
    Thread mThread;
    Handler mHandler = new Handler();

    protected static final String TEST_FAIL = "0";
    protected static final String TEST_PASS_1 = "1";
    protected static final String TEST_PASS_2 = "2";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ir_item);
        recordResult(TAG, "", TEST_FAIL);
        initView();
        initData();
    }

    private void initData() {
        mIRControl = new IrControl(this);
        mHasConsumerIr = this.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CONSUMER_IR);
        if (mHasConsumerIr)
            cm = (ConsumerIrManager) this.getSystemService(CONSUMER_IR_SERVICE);
        mPname = SystemUtil.getNameInPNAME();
    }

    private void initView() {
        mFrequencyTextView = (TextView) findViewById(R.id.tv_freq);
        mDataTextView = (TextView) findViewById(R.id.tv_data);
//        mSendButton = (Button) findViewById(R.id.btn_send);
//        mReceiveButton = (Button) findViewById(R.id.btn_receive);

    }

    public void onResume() {
        super.onResume();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Gionee ningsy GNSPR25613 20160704 begin
                /*
				 * int ret = mIda.sendData(Key1); LogUtil.i("RemoteIr",
				 * "Sent Bytes : " + ret);
				 */
                switch (mPname) {
                    //Gionee <GN_BSP_MMI> <chengq> <20170217> modify for ID 67978 begin
                    //Gionee <GN_BSP_MMI> <chengq> <20170217> modify for ID 72387 begin
                    case WBL7372:
                    case SWW1627:
                    case SWW1631:
                    //Gionee <GN_BSP_MMI> <chengq> <20170217> modify for ID 72387 end
                    case SW17W05:
                    case SWW1618:
                    case SWW1617:
                        if (cm.hasIrEmitter()) {
                            DswLog.i("ConsumerIr",
                                    "---SWW1617 IR send key10 ------");
                            test_transmit();

                            final Toast toast = Toast.makeText(getApplicationContext(),
                                    R.string.ir_send_data, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            String pattern = "9084, 4580, 511, 623, 511, 597, 538, 597, 511, 623, 485, 623, 538, 623, 485, 623, 485, 623, 511, 1757, 485, 1757, 511, 1757, 538, 1757, 485, 1757, 511, 1757, 511, 1757, 511, 1757, 511, 649, 459, 1757, 511, 1757, 511, 623, 511, 649, 511, 597, 512, 623, 511, 623, 485, 1757, 511, 623, 485, 623, 538, 1757, 485, 1757, 511, 1757, 511, 1757, 485, 1757, 511, 40135, 9084, 2337, 511, 95529";
                            int frequency = 38000;
                            mFrequencyTextView.setText(frequency + "");
                            mDataTextView.setText(pattern);
                            recordResult(TAG, "1" + "|" + "1" + "|" + frequency + "|" + pattern,TEST_PASS_1);
                        }else {
                            DswLog.i("ConsumerIr","Ir is not ok, break");
                        }

                        break;
                    //Gionee <GN_BSP_MMI> <chengq> <20170217> modify for ID 67978 end
                    case SWW1609:
                        if (cm.hasIrEmitter()) {
                            try {

                                mIRControl.sendData(mKey1);
                                DswLog.i("RemoteIr",
                                        "---BBL7337A IR send key10 ------");
                            } catch (Exception e) {

                            }

                            Toast toast = Toast.makeText(getApplicationContext(),
                                    R.string.ir_send_data, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            /*Gionee tanbotao 20160922 add for CR01758735 begin*/
                            String pattern = "174,172,24,61,24,62,24,61,24,17,25,17,24,17,24,17,24,18,24,61,24,62,24,61,24,18,24,17,24,17,24,18,24,17,24,17,24,62";
                            int frequency = 38400;
                            mFrequencyTextView.setText(frequency + "");
                            mDataTextView.setText(pattern);
                            recordResult(TAG, "1" + "|" + "1" + "|" + frequency + "|" + pattern,TEST_PASS_1);
                            /*Gionee tanbotao 20160922 add for CR01758735 end*/

                        }
                        break;
                    default:
                        DswLog.i("ConsumerIr",
                                "default send data ");
                        mIRControl.sendData(mKey1);
                        if (!mIsReceiving) {
                            mIsReceiving = true;
                            ReceiveData();
                        }
                        break;
                }
                //Gionee ningsy GNSPR25613 20160704 end

            }
        }, 500);
//    	mIRControl.sendData(mKey1);

    }

    public void onStop() {
        super.onStop();
        mIRControl.stopIR();
        endReceive();
    }

    private void endReceive() {

//        mSendButton.setEnabled(true);
//        mReceiveButton.setText("Receive");
        mIsReceiving = false;
    }

    private void ReceiveData() {
        mIRControl.Recevied_Init();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (mIsReceiving) {
            if (mIRControl.ReceivedIsRead() == READED) {
                break;
            }
        }

        if (!mIsReceiving) {
            return;
        }

        byte[] buf = new byte[254];
        mIRControl.RecviedIR(buf, buf.length);

        final int[] data = mIRControl.ReaciveDataAnlazy(buf, buf.length);
        if (data.length == 0) {
            mFrequencyTextView.setText("Learn failed");
            endReceive();
            return;
        }

        final StringBuilder builder = new StringBuilder();

        for (int i = 1; i < data.length; ++i) {

            long val = (data[i] & 0xFFFFFFFFL);
            builder.append(val + " ");
        }


        //String freq = String.format("%d Hz", data[0]);
        mFrequencyTextView.setText(data[0] + "");
        mDataTextView.setText(builder.toString());
        recordResult(TAG, "1" + "|" + "1" + "|" + data[0] + "|" + builder.toString(), TEST_PASS_1);
        //((AutoMMI) getApplication()).recordResult(TAG, "1"+"|"+"1"+"|"+freq+"|"+code, "1");
        endReceive();
//                if(!mIsPass){
//                	mIsPass=true;
//                	
//                }
    }

    protected void recordResult(String TAG, String content, String result) {
        ((AutoMMI) getApplication()).recordResult(TAG, content, result);
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170215> modify for ID 69250 begin
    public void test_transmit() {
        if (!mHasConsumerIr) {
            DswLog.d("ConsumerIr", "mHasConsumerIr is false, break");
            return;
        }

        try {
            DswLog.d("ConsumerIr", "send google Ir data");
            for (int i = 0; i < 25; i++) {
                cm.transmit(Freq_1, Key13);
            }

        } catch (Exception e) {
            DswLog.d("ConsumerIr", "error");
            e.printStackTrace();
        }

    }
    //Gionee <GN_BSP_MMI> <chengq> <20170215> modify for ID 69250 end
}
