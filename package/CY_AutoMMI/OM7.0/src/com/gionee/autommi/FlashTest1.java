package com.gionee.autommi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.os.Bundle;
import com.gionee.util.DswLog;

public class FlashTest1 extends BaseActivity {

	static private final String TAG = "FlashTest1";
//	public static final int NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_0 = 39; // /sys/class/flashlightdrv/kd_camera_flashlight/torch0
	String targeF;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	 public  void writeGestureNodeValue( String nodeType ,int value) {    
	        Object  pm = (Object)(getSystemService("amigoserver"));
	        try{
	            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");	   
	            Method method = cls.getMethod("SetNodeState", int.class,int.class);
	            Field f = cls.getField(nodeType);  
	            method.invoke(pm,f.get(null),value);
	        } catch (Exception e) {
	            DswLog.e(TAG, "Exception :" + e);
	        }
	       
	    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		DswLog.e(TAG, "onResume");
		 writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_0",40);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		DswLog.e(TAG, "onPause");
		 writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_0",0);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		DswLog.e(TAG, "onStop");
		this.finish();
	}
}
