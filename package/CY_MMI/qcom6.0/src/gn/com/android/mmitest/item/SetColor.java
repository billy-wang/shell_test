package gn.com.android.mmitest.item;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.content.ActivityNotFoundException;

public class SetColor extends Activity implements OnClickListener {
    String TAG = "SetColor";
    Button mRightBtn, mWrongBtn;
    private static boolean msetColor = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        Intent localIntent = new Intent("gn.com.android.mmitest.item.setcolor");

        try {
            if (!msetColor) {
                msetColor = true;
                Log.e("zhangxiaowei", "2222222222222222");
                startActivityForResult(localIntent, 1);
            }
        } catch (ActivityNotFoundException ex) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SetColor.this.setContentView(R.layout.common_textview_norestart);
        msetColor = false;
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mRightBtn.setEnabled(true);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.wrong_btn: {
            TestUtils.wrongPress(TAG, this);
            break;
        }
        case R.id.right_btn: {
            TestUtils.rightPress(TAG, this);
            break;
        }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
