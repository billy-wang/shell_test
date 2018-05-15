package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.util.Arrays;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


public class IrTest extends Activity implements OnClickListener {
    Button mRightBtn, mWrongBtn, mRestartBtn;
    private static final String TAG = "IrTest";
    private static final String LATTICE_IR_SERVICE = "LatticeIrService";
    //private IrSelflearning IrSelflearning_inst;
    private int mSuccessNum = 0;

    Button send_data, receiver_data;
    TextView ir_code_text, carrier_freq;
    //Test data
    int Freq_1 = 38000, Freq_2 = 40059, Freq_3 = 38000;
    int Key1[] = {340, 170, 22, 63, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 63, 22, 63, 22, 63, 22, 63, 22, 63, 22, 63, 22, 63, 22, 19, 22, 63, 22, 63, 22, 63, 22, 19, 22, 19, 22, 19, 22, 19, 22, 63, 22, 19, 22, 19, 22, 19, 22, 63, 22, 63, 22, 63, 22, 63, 22, 756};
    int Key2[] = {95, 26, 46, 26, 23, 26, 46, 26, 22, 25, 47, 26, 22, 26, 23, 26, 22, 25, 47, 27, 22, 26, 47, 26, 47, 27, 46, 26, 46, 26, 47, 25, 47, 26, 22, 26, 47, 25, 47, 27, 22, 300};
    int Key3[] = {172, 171, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 1777, 172, 171, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 1777, 172, 171, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 3806};

    /*read back data*/
    int[] rd_ir_code;
    int rd_c_freq = 0;
    int stop_flag = 0;
    boolean ret_val;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        getWindow().setAttributes(lp);
        setContentView(R.layout.consumerirtest);

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
    }

    @Override
    public void onStart() {
        super.onStart();

        ir_code_text.setMovementMethod(ScrollingMovementMethod.getInstance());//textview垂直滚动
        //  IrSelflearning_inst= (IrSelflearning)getSystemService(LATTICE_IR_SERVICE);
     /*   if(IrSelflearning_inst == null){
            ir_code_text.setText("Error not able to find LatticeIrService, close the app and try again, or check if the LatticeIrService is register properly on your device");
        	Log.e(TAG, "Error calling getSystemService for LatticeIrService");
        	return;
        }*/
        GnReflectionMethods ReflectionMethod = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "hasLatticeIrService", null, null); //包名 方法名 方法参数类型 参数
        //Gionee zhangke 20151014 modify for CR01568233 start
        boolean hasLatticeIrService;
        try {
            hasLatticeIrService = (Boolean) ReflectionMethod.getInvokeResult(this);
        } catch (Exception e) {
            Log.e(TAG, "e=" + e.getMessage());
            stop_flag = 1;
            TestUtils.wrongPress(TAG, this);
            return;
        }
        //Gionee zhangke 20151014 modify for CR01568233 start
        //if(!IrSelflearning_inst.hasLatticeIrService()){
        if (!hasLatticeIrService) {
            ir_code_text.setText("Device didn't load LatticeIrService HAL");
            Log.e(TAG, "Device didn't load LatticeIrService HAL");
            return;
        }

        try {
				/*try {
		            Class c = Class.forName("android.hardware.IrSelfLearningManager");
		            Method m = c.getMethod("DeviceInit", (Class[]) null); 
		         
		            m.invoke(c,(Object[]) null);
		        } catch(ClassNotFoundException e){
		            e.printStackTrace();
		        } catch (NoSuchMethodException e) {
		            e.printStackTrace();
		        } catch(IllegalAccessException e) {
		            e.printStackTrace();
		        } catch (InvocationTargetException e) {
		            e.printStackTrace();
		        }*/

            GnReflectionMethods ReflectionMethods1 = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "DeviceInit", null, null);
            ReflectionMethods1.getInvokeResult(this);
            GnReflectionMethods ReflectionMethods2 = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "PowerOn", null, null);
            ReflectionMethods2.getInvokeResult(this);
            //IrSelflearning_inst.DeviceInit();
            //IrSelflearning_inst.PowerOn();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            ir_code_text.setText("Exception :" + e.getMessage());
            Log.e(TAG, "Error calling DeviceInit()");
            e.printStackTrace();
        }


        //发送红外码
        send_data.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //send_data.setBackgroundColor(Color.YELLOW);
                    try {
                        Log.e(TAG, "send_data");
                        GnReflectionMethods ReflectionMethods3 = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "transmit", new Class[]{int.class, int[].class}, new Object[]{Freq_1, Key1}); //包名 方法名 方法参数类型 参数
                        boolean transmit = (Boolean) ReflectionMethods3.getInvokeResult(IrTest.this);
                        //if(!IrSelflearning_inst.transmit(Freq_1, Key1)){
                        if (!transmit) {
                            ir_code_text.setText(R.string.send_data_fail);
                        } else {
                            ir_code_text.setText(R.string.send_data_ok);
                            mSuccessNum++;
                            send_data.setEnabled(false);
                            Log.e(TAG, "mSuccessNum = " + mSuccessNum);
                        }
                    } catch (Exception e) {
                        ir_code_text.setText("Exception :" + e.getMessage());
                        Log.e(TAG, "Error in transmitting IR  value for key_1");
                    }
                }
				/*	if(event.getAction() == MotionEvent.ACTION_UP){
						send_data.setBackgroundColor(Color.TRANSPARENT);//背景色设置为透明
						
					}*/
                return false;
            }
        });

        receiver_data.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    receiver_data.setEnabled(false);
                    try {
                        //if(!IrSelflearning_inst.hasIrSelfLearning()){
                        GnReflectionMethods ReflectionMethods4 = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "hasIrSelfLearning", null, null);
                        boolean hasIrSelfLearning = (Boolean) ReflectionMethods4.getInvokeResult(IrTest.this);
                        if (!hasIrSelfLearning) {
                            ir_code_text.setText("Device doesn't support Ir Self learning");
                            Log.e(TAG, "Device doesn't support Ir Self learning");
                            return false;
                        }
                        ir_code_text.setText(R.string.receiver_data_ing);
                        carrier_freq.setText("--");
                        stop_flag = 0;


                        GnReflectionMethods ReflectionMethods5 = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "StartLearning", null, null);
                        boolean StartLearning = (Boolean) ReflectionMethods5.getInvokeResult(IrTest.this);
                        //if(!IrSelflearning_inst.StartLearning())
                        if (!StartLearning)
                            ir_code_text.setText("Error StartLearning()");
								/*Calling a AsyncTask*/
                        new read_ir_codeTask().execute();

                    } catch (Exception e) {
                        ir_code_text.setText("Exception :" + e.getMessage());
                        Log.e(TAG, "Error in transmitting value for start_self_learning");
                    }
                }
                return false;
            }
        });


    }

    @Override

    public void onPause() {

        super.onPause();
        try {
            //IrSelflearning_inst.PowerOff();
            //IrSelflearning_inst.DeviceExit();
            GnReflectionMethods ReflectionMethods6 = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "PowerOff", null, null); //包名 方法名 方法参数类型 参数
            ReflectionMethods6.getInvokeResult(IrTest.this);
            GnReflectionMethods ReflectionMethods7 = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "DeviceExit", null, null); //包名 方法名 方法参数类型 参数
            ReflectionMethods7.getInvokeResult(IrTest.this);
        } catch (Exception e) {
            ir_code_text.setText("Exception :" + e.getMessage());
            e.printStackTrace();
        }


    }

    private class read_ir_codeTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... arg0) {
            // TODO Auto-generated method stub

            try {
                while (stop_flag == 0) {
                    Thread.sleep(500);
                    GnReflectionMethods ReflectionMethods8 = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "GetLearningStatus", null, null); //包名 方法名 方法参数类型 参数
                    boolean GetLearningStatus = (Boolean) ReflectionMethods8.getInvokeResult(IrTest.this);
                    //	if(IrSelflearning_inst.GetLearningStatus()){
                    if (GetLearningStatus) {
                        //rd_c_freq=IrSelflearning_inst.ReadIRFrequency();
                        //rd_ir_code=IrSelflearning_inst.ReadIRCode();
                        GnReflectionMethods ReflectionMethods9 = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "ReadIRFrequency", null, null); //包名 方法名 方法参数类型 参数
                        rd_c_freq = (Integer) ReflectionMethods9.getInvokeResult(IrTest.this);
                        GnReflectionMethods ReflectionMethods10 = new GnReflectionMethods("android.hardware.IrSelfLearningManager", "ReadIRCode", null, null); //包名 方法名 方法参数类型 参数
                        rd_ir_code = (int[]) ReflectionMethods10.getInvokeResult(IrTest.this);
                        Log.e(TAG, "111111111111");
                        return null;
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in Reading Rx data");
            }
            return null;
        }

        protected void onPostExecute(Integer result) {
            if (stop_flag == 0) {
                Log.e(TAG, "2222222222222222");
                carrier_freq.setText((String.format("%d Hz", rd_c_freq)));
                mSuccessNum++;
                if (mSuccessNum == 2) {

                    mRightBtn.setEnabled(true);
                }
                if (rd_ir_code != null)
                    ir_code_text.setText(Arrays.toString(rd_ir_code));    //uint8_t data_index;
                else
                    ir_code_text.setText("Returned value is null");    //uint8_t data_index;

            } else {
                carrier_freq.setText(" ");
                ir_code_text.setText("Self-learning Terminated");

                rd_c_freq = 0;
                rd_ir_code = null;
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
                stop_flag = 1;
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                stop_flag = 1;
                TestUtils.wrongPress(TAG, this);
                break;
            }
            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                stop_flag = 1;
                TestUtils.restart(this, TAG);
                break;
            }


        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}


