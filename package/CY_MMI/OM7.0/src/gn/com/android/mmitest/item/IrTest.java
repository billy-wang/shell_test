package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import gn.com.android.mmitest.item.FeatureOption;

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
import gn.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


public class IrTest extends BaseActivity implements OnClickListener {
    Button mRightBtn, mWrongBtn, mRestartBtn;
    private static final String TAG = "IrTest";

    Button send_data, receiver_data;
    TextView ir_code_text, carrier_freq;
    private static final int READED = 1;
    IrControl mIda;
    byte Key1[] = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
    byte Key2[] = { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };
    byte Key3[] = { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 };
    byte Key4[] = { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 };
    byte Key5[] = { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 };
    byte Key6[] = { 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 };
    byte Key7[] = { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 };
    byte Key8[] = { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 };
    byte Key9[] = { 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9 };

    boolean mIsReceiving = false;
    //Gionee <Oversea_Bug> <tanbotao> <20161202> for 32854 beign
    private static String RERORD_IR_PATH = "/data/misc/gionee/mmi_ir_record";
    private File mRecordFile = new File(RERORD_IR_PATH);
    boolean flag1 = true;
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
    //Gionee <Oversea_Bug> <tanbotao> <20161202> for 32854 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开红外测试 @" + Integer.toHexString(hashCode()));
        TestUtils.setCurrentAciticityTitle(TAG,this);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        getWindow().setAttributes(lp);
        setContentView(R.layout.consumerirtest);
        mIda = new IrControl(this);
        send_data = (Button) findViewById(R.id.send_data);//发射
        receiver_data = (Button) findViewById(R.id.receiver_data);//接收

        ir_code_text = (TextView) findViewById(R.id.ir_code_string);
        carrier_freq = (TextView) findViewById(R.id.carrier_freq);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRightBtn.setEnabled(false);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);

        //Gionee <Oversea_Bug> <tanbotao> <20161202> for 32854 beign
        if (FeatureOption.GN_RW_GN_MMI_IRTEST_NO_RECEIVED_SUPPORT){
            View checkedTextView01 = findViewById(R.id.CheckedTextView01);
            View checkedTextView1 = findViewById(R.id.checkedTextView1);
            checkedTextView01.setVisibility(View.INVISIBLE);
            checkedTextView1.setVisibility(View.INVISIBLE);

        }
        if (!mRecordFile.exists()) {
            try{
                mRecordFile.createNewFile();
            }catch(Exception e){
                DswLog.e(TAG, e.getMessage());
            }
        }
        if (mRecordFile.exists()) {
            DswLog.e(TAG, " chmod 666 /data/misc/gionee/mmi_ir_record");
            try{
                Runtime.getRuntime().exec("chmod 666 " + RERORD_IR_PATH);
            }catch(Exception e){
                DswLog.e(TAG, e.getMessage());
            }
        }
        //Gionee <Oversea_Bug> <tanbotao> <20161202> for 32854 end
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出红外测试 @" + Integer.toHexString(hashCode()));
    }

    @Override
    protected void setWindowFlags() {

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

                    int ret = mIda.sendData(Key1);
                    DswLog.i("RemoteIr", "Sent Bytes : " + ret);

                    //Gionee <Oversea_Bug> <tanbotao> <20161202> for 32854 beign
                    if (FeatureOption.GN_RW_GN_MMI_IRTEST_NO_RECEIVED_SUPPORT){
                        if (mRecordFile.exists()) {
                            mRecordFile.delete();
                        }
                        Thread mThread = new Thread(new Runnable() {
                            public void run() {
                                flag1 = true;
                                while (flag1) {
                                    String strfile = preprocess("11");
                                    DswLog.d("ningsy", "-------strfile=="+ strfile);
                                    if (null != strfile) {
                                        int start = strfile.indexOf("1");
                                        DswLog.d("ningsy", "-------start=="+ strfile);
                                        if (start >= 1) {
                                            String sub = strfile.substring(
                                                    start, start + 1);
                                            DswLog.d("ningsy", "-------sub=="+ sub);
                                            if (sub.equals("1")) {
                                                Message message = new Message();
                                                message.what = 1;
                                                handler.sendMessage(message);
                                                flag1 = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        mThread.start();
                    }
                    //Gionee <Oversea_Bug> <tanbotao> <20161202> for 32854 end

                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.setPressed(false);
                    mIda.stopIR();
                }

                return true;
            }
        });
//Gionee <Oversea_Bug> <tanbotao> <20161124> for #28369 beign
        if (FeatureOption.GN_RW_GN_MMI_IRTEST_NO_RECEIVED_SUPPORT){
            //Gionee <Oversea_Bug> <tanbotao> <20161202> for 32854 begin
            receiver_data.setVisibility(View.INVISIBLE);
            //Gionee <Oversea_Bug> <tanbotao> <20161202> for 32854 end
        }else {
            receiver_data.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!mIsReceiving) {

                        send_data.setEnabled(false);
                        receiver_data.setText("Receiving...");
                        mIsReceiving = true;

                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mIda.Recevied_Init();
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                while (mIsReceiving){
                                /*Gionee huangjianqiang 20160822 add for CR01747166 begin*/
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                /*Gionee huangjianqiang 20160822 add for CR01747166 end*/
                                    if (mIda.ReceivedIsRead() == READED){
                                        break;
                                    }
                                }

                                if (!mIsReceiving){
                                    return;
                                }

                                byte[] buf = new byte[254];
                                mIda.RecviedIR(buf, buf.length);

                                final int[] data = mIda.ReaciveDataAnlazy(buf,
                                        buf.length);
                                if (data.length == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            carrier_freq.setText("Learn failed");
                                            endReceive();
                                        }
                                    });
                                    return;
                                }

                                final StringBuilder builder = new StringBuilder();

                                for (int i = 1; i < data.length; ++i) {

                                    long val = (data[i] & 0xFFFFFFFFL);
                                    builder.append(val + " ");
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        carrier_freq.setText(data[0] + "");
                                        ir_code_text.setText(builder.toString());
                                        endReceive();
                                        mRightBtn.setEnabled(true);
                                    }
                                });
                            }

                        });
                        t.start();

                    } else {
                        endReceive();
                    }
                }
            });
        }
//Gionee <Oversea_Bug> <tanbotao> <20161124> for #28369 end

    }
    void endReceive() {

        send_data.setEnabled(true);
        receiver_data.setText("Receive");
        mIsReceiving = false;
    }
    @Override

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
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

    //Gionee <Oversea_Bug> <tanbotao> <20161202> for 32854 begin
    private String preprocess(String tag) {
        // TODO Auto-generated method stub
        try {
            FileInputStream fis = new FileInputStream(RERORD_IR_PATH);
            int len = fis.available();
            byte[] bytes = new byte[len];
            fis.read(bytes);
            fis.close();
            String res = new String(bytes);

            int start;
            if(-1 != (start = res.indexOf(tag))) {
                int end = res.indexOf('\n', start);
                String sub = res.substring(start, end+1);
                return res.replace(sub, "");
            } else {
                return res;
            }
        } catch (FileNotFoundException
                e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    //Gionee <Oversea_Bug> <tanbotao> <20161202> for 32854 end
}


