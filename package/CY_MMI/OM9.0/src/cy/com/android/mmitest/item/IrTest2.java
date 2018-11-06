package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;
import cy.com.android.mmitest.item.FeatureOption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.content.pm.PackageManager;


public class IrTest2 extends BaseActivity implements OnClickListener {
    Button mRightBtn, mWrongBtn, mRestartBtn;
    private static final String TAG = "IrTest2";
  //  IrControl mIda;
    Button send_data, receiver_data;
    TextView ir_code_text, carrier_freq;
    private boolean mHasConsumerIr;
    private ConsumerIrManager mCIR;
    private static final int READED = 1;
    Thread nThread, mThread;

    //Gionee <GN_BSP_MMI> <chengq> <20170217> modify for ID 68092 begin
    //1609 ,1605 发送的红外波形数据
    int Key10[] = {340,170,22,63,22,19,22,19,22,19,22,19,22,19,22,19,22,19,22,19,22,63,22,63,22,63,22,63,22,63,22,63,22,63,22,19,22,63,22,63,22,63,22,19,22,19,22,19,22,19,22,63,22,19,22,19,22,19,22,63,22,63,22,63,22,63,22,756};
    //Google原生红外样例 波形数据
    int[] pattern = {1901, 4453, 625, 1614, 625, 1588, 625, 1614, 625, 442, 625, 442, 625, 468, 625, 442, 625, 494, 572, 1614, 625, 1588, 625, 1614, 625, 494, 572, 442, 651, 442, 625, 442, 625, 442, 625, 1614, 625, 1588, 651, 1588, 625, 442, 625, 494, 598, 442, 625, 442, 625, 520, 572, 442, 625, 442, 625, 442, 651, 1588, 625, 1614, 625, 1588, 625, 1614, 625, 1588, 625, 48958};
    //酷控使用达伦灯泡波形数据
    int[] pattern2 = {9084, 4580, 511, 623, 511, 597, 538, 597, 511, 623, 485, 623, 538, 623, 485, 623, 485, 623, 511, 1757, 485, 1757, 511, 1757, 538, 1757, 485, 1757, 511, 1757, 511, 1757, 511, 1757, 511, 649, 459, 1757, 511, 1757, 511, 623, 511, 649, 511, 597, 512, 623, 511, 623, 485, 1757, 511, 623, 485, 623, 538, 1757, 485, 1757, 511, 1757, 511, 1757, 485, 1757, 511, 40135, 9084, 2337, 511, 95529};
    //Gionee <GN_BSP_MMI> <chengq> <20170217> modify for ID 68092 end
    private static String RERORD_IR_PATH = "/data/misc/gionee/mmi_ir_record";
    private File mRecordFile = new File(RERORD_IR_PATH);


    boolean flag1 = true;
    int Freq_1 = 38000;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mRightBtn.setEnabled(true);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开红外测试2 @" + Integer.toHexString(hashCode()));
        TestUtils.setCurrentAciticityTitle(TAG,this);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        getWindow().setAttributes(lp);
        setContentView(R.layout.consumerirtest);
      //  mIda = new IrControl(this);
        send_data = (Button) findViewById(R.id.send_data);//发射
        receiver_data = (Button) findViewById(R.id.receiver_data);//接收
        receiver_data.setVisibility(View.INVISIBLE);
        ir_code_text = (TextView) findViewById(R.id.ir_code_string);
        carrier_freq = (TextView) findViewById(R.id.carrier_freq);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRightBtn.setEnabled(false);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);

        View checkedTextView01 = findViewById(R.id.CheckedTextView01);
        View checkedTextView1 = findViewById(R.id.checkedTextView1);
        checkedTextView01.setVisibility(View.INVISIBLE);
        checkedTextView1.setVisibility(View.INVISIBLE);

        if (!mRecordFile.exists()) {
            try {
                mRecordFile.createNewFile();
            } catch (Exception e) {
                DswLog.e(TAG, e.getMessage());
            }
        }
        if (mRecordFile.exists()) {
            DswLog.e(TAG, " chmod 666 /data/misc/gionee/mmi_ir_record");
            try {
                Runtime.getRuntime().exec("chmod 666 " + RERORD_IR_PATH);
            } catch (Exception e) {
                DswLog.e(TAG, e.getMessage());
            }
        }


        mHasConsumerIr = this.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CONSUMER_IR);
        if (mHasConsumerIr) {
            mCIR = (ConsumerIrManager) this.getSystemService(
                    Context.CONSUMER_IR_SERVICE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出红外测试2 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onStart() {
        super.onStart();
        //发送红外码

        send_data.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    view.setPressed(true);

                    if (mRecordFile.exists()) {
                        mRecordFile.delete();
                    }
                    //Gionee <GN_BSP_MMI> <chengq> <20170414> modify for ID 112756 begin
   
                    mThread = new MThread();
                    DswLog.d("TestIrTag", "new MThread");

                    if (!mThread.isAlive()) {
                        mThread.start();
                        DswLog.d("TestIrTag", "mThread start");
                    }

                    if (nThread == null || !flag1) {
                        nThread = new NThread();
                        nThread.start();
                        DswLog.d("TestIrTag", "new nThread");
                    }
                    //Gionee <GN_BSP_MMI> <chengq> <20170414> modify for ID 112756 end
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.setPressed(false);
                }
                return true;
            }


        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
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

    @Override
    protected void onStop() {
        super.onStop();
        flag1 = false;
        nThread = null;
        mThread = null;
    }

    private String preprocess(String tag) {
        try {
            FileInputStream fis = new FileInputStream(RERORD_IR_PATH);
            int len = fis.available();
            byte[] bytes = new byte[len];
            fis.read(bytes);
            fis.close();
            String res = new String(bytes);

            int start;
            if (-1 != (start = res.indexOf(tag))) {
                int end = res.indexOf('\n', start);
                String sub = res.substring(start, end + 1);
                return res.replace(sub, "");
            } else {
                return res;
            }
        } catch (FileNotFoundException
                e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void test_transmit() {
        if (!mHasConsumerIr) {
            DswLog.d("ConsumerIr", "mHasConsumerIr is false, break");
            return;
        }

        mCIR.transmit(Freq_1, pattern2);
    }

    private class MThread extends Thread{
        @Override
        public void run() {
            super.run();
            test_transmit();
            DswLog.i("ConsumerIr", "the MThread id is "+ Thread.currentThread().getName()+"  id= "
                    +Thread.currentThread().getId());
            //Gionee <GN_BSP_MMI> <chengq> <20170414> modify for ID 112756 begin
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Gionee <GN_BSP_MMI> <chengq> <20170414> modify for ID 112756 end
        }
    };

    private class NThread extends Thread{
        @Override
        public void run() {
            super.run();

            DswLog.i("ConsumerIr", "the NThread id is "+ Thread.currentThread().getName()+"  id= "
                    +Thread.currentThread().getId());
            flag1 = true;
            while (flag1) {

                String strfile = preprocess("11");
                DswLog.d(TAG, "-------strfile==" + strfile);
                if (null != strfile) {
                    int start = strfile.indexOf("1");
                    DswLog.d(TAG, "-------start==" + strfile);
                    if (start >= 1) {
                        String sub = strfile.substring(
                                start, start + 1);
                        DswLog.d(TAG, "-------sub==" + sub);
                        if (sub.equals("1")) {
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                            flag1 = false;
                            break;
                        }
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    };
}
