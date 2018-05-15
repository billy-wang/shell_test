package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import gn.com.android.mmitest.item.FeatureOption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import tv.peel.service.smartir.IReceiveCallback;
import tv.peel.service.smartir.ISmartIrService;
import tv.peel.service.smartir.ITransmitCallback;
import tv.peel.service.smartir.IntervalType;
import tv.peel.service.smartir.SmartIrFailure;
import tv.peel.service.smartir.SmartIrResponse;
import tv.peel.service.smartir.TransmitMode;
import gn.com.android.mmitest.utils.DswLog;


public class IrTest3 extends BaseActivity implements OnClickListener {
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private static final String TAG = "IrTest3";
    private Button send_data, receiver_data;
    private TextView transmitTextView,receivedTextView;
    private ISmartIrService smartIrService;
    // Smasung TV ON
    private String mPattern = "174,172,24,61,24,62,24,61,24,17,25,17,24,17,24,17,24,18,24,61,24,62,24,61,24,18,24,17,24,17,24,18,24,17,24,17,24,62,24,17,24,17,24,18,24,17,24,17,24,18,24,61,24,17,24,62,24,61,25,61,24,62,24,61,24,62,24,1879,174,172,24,61,24,62,24,61,24,17,25,17,24,17,24,17,24,18,24,61,24,62,24,61,24,18,24,17,24,17,24,18,24,17,24,17,24,62,24,17,24,17,24,18,24,17,24,17,24,18,24,61,24,17,24,62,24,61,25,61,24,62,24,61,24,62,24,1879,174,172,24,61,24,62,24,61,24,17,25,17,24,17,24,17,24,18,24,61,24,62,24,61,24,18,24,17,24,17,24,18,24,17,24,17,24,62,24,17,24,17,24,18,24,17,24,17,24,18,24,61,24,17,24,62,24,61,25,61,24,62,24,61,24,62,24,1879";
    private int mFrequency = 38400;
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

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DswLog.w(TAG, "Client2: connected to service!");
            smartIrService = ISmartIrService.Stub.asInterface(service);

            try {
                smartIrService.registerTransmitCallback(mTransmitCallback);
                smartIrService.registerReceiveCallback(mReceiveCallback);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DswLog.w(TAG, "Client2: disconnected to service!");
            smartIrService = null;

            DswLog.w(TAG, "SmartIr attempt to reconnect...");

            // Wake up SmartIr service again by binding
            boolean bindSuccess = connectService();
            if (!bindSuccess) {
                transmitTextView.setText("Failed to bind to service :(");
            }

            //yy In SmartIr system service, service is never bound or unbound, so this should never be used.
        }
    };

    private final ITransmitCallback mTransmitCallback = new ITransmitCallback.Stub() {
        @Override
        public void onSuccess() throws RemoteException {
            DswLog.w(TAG, "Client2: transmitCallback onSuccess");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    transmitTextView.setText("IR transmitted!");
                }
            });
        }

        @Override
        public void onFailure(final SmartIrFailure failure) throws RemoteException {
            DswLog.w(TAG, "Client2: transmitCallback onFailure");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    transmitTextView.setText("IR transmit failed. " + failure.getStatusCode() + ", " + failure.getMessage());
                }
            });
        }
    };

    private final IReceiveCallback mReceiveCallback = new IReceiveCallback.Stub() {
        @Override
        public void onSuccess(final int frequency, final String pattern) throws RemoteException {
            DswLog.w(TAG, "Client2: receiveCallback onSuccess");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Receiving already succeed\n\n");
                    builder.append("Frequency:\n  " + frequency + "\n");
                    builder.append("Pattern:\n  " + pattern + "\n");
                    receivedTextView.setText(builder.toString());
                    mRightBtn.setEnabled(true);
                    //mFrequency = frequency;
                    //mPattern = pattern;
                    DswLog.v(TAG, "receive frequency=" + frequency);
                    DswLog.v(TAG, "receive pattern=" + pattern);
                }
            });
        }

        @Override
        public void onFailure(final SmartIrFailure failure) throws RemoteException {
            DswLog.w(TAG, "Client2: receiveCallback onFailure");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (failure.getStatusCode()) {
                        case SmartIrFailure.STATUS_RECEIVE_HAL_PROBLEM:
                            receivedTextView.setText("Receive HAL problem!");
                            break;
                        case SmartIrFailure.STATUS_RECEIVE_TIMEOUT:
                            receivedTextView.setText("Timed out!");
                            break;
                        case SmartIrFailure.STATUS_RECEIVE_CANCELED:
                            receivedTextView.setText("Canceled!");
                            break;
                    }
                }
            });
        }
    };


    /*private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mRightBtn.setEnabled(true);
                    break;
            }
        }
    };*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开红外测试2 @" + Integer.toHexString(hashCode()));
        setContentView(R.layout.main_irtest3);

        initView_irtest3();
        initData_irtest3();
    }

    private void initView_irtest3() {
        transmitTextView = (TextView) findViewById(R.id.transmit_text);
        receivedTextView = (TextView) findViewById(R.id.received_text);
        send_data = (Button) findViewById(R.id.send_data);//发射
        receiver_data = (Button) findViewById(R.id.receiver_data);//接收

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRightBtn.setEnabled(false);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);


    }

    private void initData_irtest3() {
     //   StringBuilder builder = new StringBuilder();
     //   builder.append("Frequency:\n  " + mFrequency + "\n");
     //   builder.append("Pattern:\n  " + mPattern + "\n");
     //   receivedTextView.setText(builder.toString());
          receivedTextView.setText("");

        boolean bindSuccess = connectService();
        if (!bindSuccess) {
            transmitTextView.setText("Failed to bind to service");
            return;
        }

        /*if (!mRecordFile.exists()) {
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
        }*/

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
                    try {
                        int response = smartIrService.startTransmitting(
                                mFrequency, mPattern, null,
                                IntervalType.PULSES, TransmitMode.ONCE, 0);
                        switch (response) {
                            case SmartIrResponse.RESPONSE_TRANSMIT_FAILURE:
                                transmitTextView.setText("Immediate transmit failure.");
                                break;
                            case SmartIrResponse.RESPONSE_TRANSMIT_IN_PROGRESS:
                                transmitTextView.setText("Trasmitting already in progress");
                                break;
                            case SmartIrResponse.RESPONSE_RECEIVE_IN_PROGRESS:
                                transmitTextView.setText("Receiving already in progress");
                                break;
                            case SmartIrResponse.RESPONSE_MISSING_CAPABILITY:
                                transmitTextView.setText("startTransmitting capability missing");
                                break;
                        }
                    } catch (RemoteException e) {
                        DswLog.e(TAG, "Send_IR Error:"+e.getMessage());
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.setPressed(false);
                }
                return true;
            }
        });

        receiver_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DswLog.w(TAG, "Client2: started receiving");

                try {
                    int response = smartIrService.startReceiving(10);
                    switch (response) {
                        case SmartIrResponse.RESPONSE_RECEIVE_FAILURE:
                            receivedTextView.setText("Immediate receive failure.");
                            break;
                        case SmartIrResponse.RESPONSE_TRANSMIT_IN_PROGRESS:
                            receivedTextView.setText("Trasmitting already in progress");
                            break;
                        case SmartIrResponse.RESPONSE_RECEIVE_IN_PROGRESS:
                            receivedTextView.setText("Receiving already in progress");
                            break;
                        case SmartIrResponse.RESPONSE_MISSING_CAPABILITY:
                            receivedTextView.setText("startReceiving capability missing");
                            break;
                        default:
                            receivedTextView.setText("Receiving...");
                            break;
                    }
                } catch (RemoteException e) {
                    DswLog.e(TAG, "Reveive_IR Error:"+e.getMessage());
                }
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
    public void onDestroy() {
        super.onDestroy();

        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
        }
        DswLog.d(TAG, "\n****************退出红外测试2 @" + Integer.toHexString(hashCode()));
    }

    private boolean connectService() {
        // Explicit intent for Android 5.0
        Intent intent = new Intent(ISmartIrService.class.getName());
        intent.setPackage("tv.peel.service");
        boolean bindSuccess = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        return bindSuccess;
    }

   /* private String preprocess(String tag) {
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
    }*/

}
