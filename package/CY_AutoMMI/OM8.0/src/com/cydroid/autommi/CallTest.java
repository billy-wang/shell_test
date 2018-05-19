package com.cydroid.autommi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class CallTest extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
		callIntent.setData(Uri.parse("tel:112"));
		this.startActivity(callIntent);
		this.finish();
	}
}
