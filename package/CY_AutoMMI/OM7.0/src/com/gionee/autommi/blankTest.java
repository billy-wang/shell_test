package com.gionee.autommi;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import com.gionee.util.DswLog;
import android.view.View;
import android.graphics.Color;

public class blankTest extends BaseActivity {

	private static final String TAG = "blankTest";
    private View mColorView;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		DswLog.i(TAG, "ritColor : ");
        mColorView = new View(this);
		mColorView.setBackgroundColor(Color.BLACK);
        setContentView(mColorView);
	}
	

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DswLog.i(TAG, "onStop()");
		this.finish();
	}
}
