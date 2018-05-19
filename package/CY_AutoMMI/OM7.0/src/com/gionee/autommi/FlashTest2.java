package com.gionee.autommi;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import com.gionee.util.DswLog;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FlashTest2 extends BaseActivity {

	static private final String TAG = "FlashTest2";
	//public static final int NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1 = 40; // /sys/class/flashlightdrv/kd_camera_flashlight/torch0
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
        //Gionee <GN_BSP_MMI> <chengq> <20170113> modify for ID 52842 begin
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		DswLog.e(TAG, "onResume");
		 writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH",40);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		DswLog.e(TAG, "onPause");
		writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH",0);
	}
        //Gionee <GN_BSP_MMI> <chengq> <20170113> modify for ID 52842 end

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		DswLog.e(TAG, "onStop");
		this.finish();
	}
}
