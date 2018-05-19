package com.gionee.autommi;


import java.util.Arrays;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.hardware.*;
public class IrTest extends BaseActivity {

	private static final String TAG = "IrTest";
	private static final String LATTICE_IR_SERVICE = "LatticeIrService";
	//private IrSelflearning IrSelflearning_inst;
	private int mSuccessNum = 0;

	// Button send_data,receiver_data;
	// TextView ir_code_text,carrier_freq;
	// Test data
	int Freq_1 = 38000, Freq_2 = 40059, Freq_3 = 38000;
	int Key1[] = { 340, 170, 22, 63, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19,
			22, 19, 22, 19, 22, 19, 22, 63, 22, 63, 22, 63, 22, 63, 22, 63, 22,
			63, 22, 63, 22, 19, 22, 63, 22, 63, 22, 63, 22, 19, 22, 19, 22, 19,
			22, 19, 22, 63, 22, 19, 22, 19, 22, 19, 22, 63, 22, 63, 22, 63, 22,
			63, 22, 756 };
	int Key2[] = { 95, 26, 46, 26, 23, 26, 46, 26, 22, 25, 47, 26, 22, 26, 23,
			26, 22, 25, 47, 27, 22, 26, 47, 26, 47, 27, 46, 26, 46, 26, 47, 25,
			47, 26, 22, 26, 47, 25, 47, 27, 22, 300 };
	int Key3[] = { 172, 171, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19,
			22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19, 22,
			19, 22, 19, 23, 62, 23, 62, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19,
			22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23,
			62, 22, 1777, 172, 171, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22,
			19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22, 19,
			22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 23, 62, 23, 62, 22, 19, 22,
			19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23, 62,
			23, 62, 22, 1777, 172, 171, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19,
			22, 19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 22, 19, 22, 19, 22,
			19, 22, 19, 22, 19, 23, 62, 23, 62, 23, 62, 23, 62, 23, 62, 22, 19,
			22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 22, 19, 23, 62, 23,
			62, 23, 62, 22, 3806 };

	// read back data 
	int[] rd_ir_code;
	int rd_c_freq = 0;
	int stop_flag = 0;
	boolean ret_val;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		((AutoMMI) getApplication()).recordResult(TAG, "0"+"|"+"0"+"|"+"0"+"|"+"0", "0");
		//IrSelflearning_inst = (IrSelflearning) getSystemService(LATTICE_IR_SERVICE);
	/*	if (IrSelflearning_inst == null) {
			Toast.makeText(
					this,
					"Error not able to find LatticeIrService, close the app and try again, or check if the LatticeIrService is register properly on your device",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, "Error calling getSystemService for LatticeIrService");
			return;
		}*/
		GnReflectionMethods gnMethod = new GnReflectionMethods(
				"android.hardware.IrSelfLearningManager",
				"hasLatticeIrService", null, null); // 包名 方法名 方法参数类型 参数
		boolean hasLatticeIrService = false;
		Object result =  gnMethod.getInvokeResult(this);
		if (result != null) {
			hasLatticeIrService = (Boolean)result;
		}

		if (!hasLatticeIrService) {
			Toast.makeText(this, "Device didn't load LatticeIrService HAL",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, "Device didn't load LatticeIrService HAL");
			return;
		}

		try {
			GnReflectionMethods ReflectionMethods1=new GnReflectionMethods("android.hardware.IrSelfLearningManager","DeviceInit",null,null); //包名 方法名 方法参数类型 参数
			ReflectionMethods1.getInvokeResult(this);
			GnReflectionMethods ReflectionMethods2=new GnReflectionMethods("android.hardware.IrSelfLearningManager","PowerOn",null,null); //包名 方法名 方法参数类型 参数
			ReflectionMethods2.getInvokeResult(this);
			//IrSelflearning_inst.DeviceInit();
			//IrSelflearning_inst.PowerOn();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Exception :" + e.getMessage(),
					Toast.LENGTH_LONG).show();
			Log.e(TAG, "Error calling DeviceInit()");
			e.printStackTrace();
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		try {
			Log.d(TAG, "send_data");
			GnReflectionMethods ReflectionMethods3=new GnReflectionMethods("android.hardware.IrSelfLearningManager","transmit",new Class[]{int.class,int[].class},new Object[]{Freq_1,Key1}); //包名 方法名 方法参数类型 参数
			boolean transmit = (Boolean)ReflectionMethods3.getInvokeResult(IrTest.this);
			if(!transmit){
		//	if (!IrSelflearning_inst.transmit(Freq_1, Key1)) {
				Toast.makeText(this, "send_data_fail", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(this, "send_data_ok", Toast.LENGTH_LONG)
						.show();
				mSuccessNum++;
				Log.d(TAG, "mSuccessNum = " + mSuccessNum);
			}
		} catch (Exception e) {
			Toast.makeText(this, "Exception :" + e.getMessage(),
					Toast.LENGTH_LONG).show();
			Log.e(TAG, "Error in transmitting IR  value for key_1");
		}

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            Log.d(TAG, ""+e);
        }

		try {
			//if (!IrSelflearning_inst.hasIrSelfLearning()) {
			GnReflectionMethods ReflectionMethods4=new GnReflectionMethods("android.hardware.IrSelfLearningManager","hasIrSelfLearning",null,null); //包名 方法名 方法参数类型 参数
			boolean hasIrSelfLearning = (Boolean)ReflectionMethods4.getInvokeResult(IrTest.this);
			if(!hasIrSelfLearning){	
			Log.e(TAG, "Device doesn't support Ir Self learning");

			}

			stop_flag = 0;

		//	if (!IrSelflearning_inst.StartLearning()) {
			GnReflectionMethods ReflectionMethods5=new GnReflectionMethods("android.hardware.IrSelfLearningManager","StartLearning",null,null); //包名 方法名 方法参数类型 参数
			boolean StartLearning = (Boolean)ReflectionMethods5.getInvokeResult(IrTest.this);
			if(!StartLearning){
			Log.e(TAG, "Error StartLearning()");
				return;
			}
			/*Calling a AsyncTask*/
			new read_ir_codeTask().execute();

		} catch (Exception e) {
			Log.e(TAG, "Error in transmitting value for start_self_learning");
		}

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        try {
          //  IrSelflearning_inst.PowerOff();
          //  IrSelflearning_inst.DeviceExit();
        	GnReflectionMethods ReflectionMethods6=new GnReflectionMethods("android.hardware.IrSelfLearningManager","PowerOff",null,null); //包名 方法名 方法参数类型 参数
    		ReflectionMethods6.getInvokeResult(IrTest.this);
    		GnReflectionMethods ReflectionMethods7=new GnReflectionMethods("android.hardware.IrSelfLearningManager","DeviceExit",null,null); //包名 方法名 方法参数类型 参数
    		ReflectionMethods7.getInvokeResult(IrTest.this);
        } catch (Exception e) {
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
					GnReflectionMethods ReflectionMethods8=new GnReflectionMethods("android.hardware.IrSelfLearningManager","GetLearningStatus",null,null); //包名 方法名 方法参数类型 参数
					boolean GetLearningStatus = (Boolean)ReflectionMethods8.getInvokeResult(IrTest.this);
					//if (IrSelflearning_inst.GetLearningStatus()) {
					if(GetLearningStatus){
				//	rd_c_freq = IrSelflearning_inst.ReadIRFrequency();
				//		rd_ir_code = IrSelflearning_inst.ReadIRCode();
						GnReflectionMethods ReflectionMethods9=new GnReflectionMethods("android.hardware.IrSelfLearningManager","ReadIRFrequency",null,null); //包名 方法名 方法参数类型 参数
						 rd_c_freq = (Integer)ReflectionMethods9.getInvokeResult(IrTest.this);
						GnReflectionMethods ReflectionMethods10=new GnReflectionMethods("android.hardware.IrSelfLearningManager","ReadIRCode",null,null); //包名 方法名 方法参数类型 参数
						 rd_ir_code = (int[])ReflectionMethods10.getInvokeResult(IrTest.this);
						return null;
					}
				}

			} catch (Exception e) {
				Log.e(TAG, "Error in Reading Rx data : " + e.getMessage());
			}
			return null;
		}

		protected void onPostExecute(Integer result) {
			if (stop_flag == 0) {
//				carrier_freq.setText((String.format("%d Hz", rd_c_freq)));
				Toast.makeText(IrTest.this, "receive_data_ok", Toast.LENGTH_LONG).show();
				String freq = String.format("%d Hz", rd_c_freq);
				String code = null;
				if (rd_ir_code != null)
					code = Arrays.toString(rd_ir_code); // uint8_t
			
				mSuccessNum++;
				if (mSuccessNum == 2) {
					((AutoMMI) getApplication()).recordResult(TAG, "1"+"|"+"1"+"|"+freq+"|"+code, "1");
				}

			} else {
				rd_c_freq = 0;
				rd_ir_code = null;
			}
		}
	}

}
