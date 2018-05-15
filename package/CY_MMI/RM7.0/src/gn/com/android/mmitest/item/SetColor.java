package gn.com.android.mmitest.item;

import gn.com.android.mmitest.TestResult;
import gn.com.android.mmitest.TestUtils;
import gn.com.android.mmitest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.TextView;
import android.view.WindowManager;
import gn.com.android.mmitest.TestResult;
import android.view.KeyEvent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.os.SystemProperties;
import android.os.Message;

public class SetColor extends Activity
        implements View.OnClickListener
{

    private Button mButton1,mButton2,mButton3;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private TextView mTv1,mTv2;
    private TestResult tr;
    private String black,blue,gold,theme;
    private Handler mHandler;
    String str;
    String flag;
    private static String theme_black = "/system/app/GN_Graf_black.gnz";
    private static String theme_blue= "/system/app/GN_Graf_blue.gnz";
    private static String theme_gold = "/system/app/GN_Graf_gold.gnz";
    private static String theme_default = "/system/app/GN_Graf.gnz";
    private String TAG = "SetColor";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_color);
        Log.d(TAG,"onCreate");
        TestUtils.setWindowFlags(this);
        str = "";
        flag = "";
        theme = new String();
        initView();
    }

    private void initView() {
        mTv1 = (TextView) findViewById(R.id.tv1);
        mTv2 = (TextView) findViewById(R.id.tv2);
        mTv2.setText("");
        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);
        mButton3 = (Button) findViewById(R.id.button3);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        mButton3.setOnClickListener(this);
        mRightBtn.setVisibility(View.INVISIBLE);
        String chooseColor = getResources().getString(R.string.chooseColor);
        black = getResources().getString(R.string.black);
        blue = getResources().getString(R.string.blue);
        gold = getResources().getString(R.string.gold);
        mTv1.setText(chooseColor);
        mButton1.setText(black.toString());
        mButton2.setText(blue.toString());
        mButton3.setText(gold.toString());
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String str = new String ();
                int i = msg.arg1;
                switch (msg.what){
                    case 1:
                        showResult(black,i);
                        break;
                    case 2:
                        showResult(blue,i);
                        break;
                    case 3:
                        showResult(gold,i);
                        break;
                    default :
                        break;
                }
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);
                mRightBtn.setOnClickListener(SetColor.this);
                mWrongBtn.setOnClickListener(SetColor.this);
                mRestartBtn.setOnClickListener(SetColor.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);        
    }
    private void showResult(String s,int a) {
        tr = new TestResult();
        byte[] sn_buff = new byte[TestResult.SN_LENGTH];
        System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, TestResult.SN_LENGTH);
        mTv2.setText("您选择的颜色是: " + s);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170817> modify for ID 189673 begin
        if(a == 1){
            flag = "0";
        } else if (a == 2){
            flag = "1";
        } else if (a == 3){
            flag = "2";
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170817> modify for ID 189673 end
        sn_buff = tr.getNewSN(TestResult.MMI_COLOR_TAG, flag, sn_buff);
        tr.writeToProductInfo(sn_buff);
        System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, TestResult.SN_LENGTH);
        Log.d(TAG,"  sn_buff[TestResult.MMI_COLOR_TAG]  = " +  sn_buff[TestResult.MMI_COLOR_TAG] + "  , flag = " + flag);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        str = "";
        switch(id){
            case 1:
                str = black;
                flag = "0";
                break;
            case 2:
                str = blue;
                flag = "1";
                break;
            case 3:
                str = gold;
                flag = "2";
                break;
            default :
                break;
        }
        Dialog dialog = null;
        AlertDialog.Builder buider = new AlertDialog.Builder(this);
        buider.setCancelable(false).setTitle("选择颜色");
        Log.d(TAG,"str= " + str);      
        buider.setMessage("您选择的颜色是: " + str).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Message m = Message.obtain();
                m.what = id;
                m.arg1 = id;
                mHandler.sendMessage(m);
                mRightBtn.setEnabled(true);
                Log.d(TAG,"theme ==" + theme);
                SystemProperties.set("persist.sys.gntheme", theme);
                mRightBtn.setVisibility(View.VISIBLE);
            }
        }).setNegativeButton("取消",null).create();
        dialog = buider.create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
        getWindow().getDecorView().getSystemUiVisibility());
        buider = null;
        return dialog;

    }
    
    @Override
    protected void onStart() {
        Log.d(TAG,"onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    
    @Override
    public void onClick(View v) {
            // TODO Auto-generated method stub
            
            switch (v.getId()) {
        
                case R.id.right_btn: {
                    Log.d(TAG,"id.right_btn");
                    mRightBtn.setEnabled(false);
                    mRestartBtn.setEnabled(false);
                    mWrongBtn.setEnabled(false);
                    TestUtils.rightPress(TAG, SetColor.this);
                    break;
                }
        
                case R.id.wrong_btn: {
                    Log.d(TAG,"id.wrong_btn");
                    mRightBtn.setEnabled(false);
                    mRestartBtn.setEnabled(false);
                    mWrongBtn.setEnabled(false);
                    TestUtils.wrongPress(TAG, SetColor.this);
                    break;
                }
        
                case R.id.restart_btn: {
                    Log.d(TAG,"id.restart_btn");
                    mRightBtn.setEnabled(false);
                    mWrongBtn.setEnabled(false);
                    mRestartBtn.setEnabled(false);
                    TestUtils.restart(this, TAG);
                    break;
                }

                case R.id.button1: {
                    Log.d(TAG,"id.button1");
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170817> modify for ID 189673 begin
                    theme = theme_default;
                    showDialog(1);
                    break;
                }

                case R.id.button2: {
                    Log.d(TAG,"id.button2");
                    theme = theme_default;
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170817> modify for ID 189673 end
                    showDialog(2);
                    break;
                }
                case R.id.button3: {
                    Log.d(TAG,"id.button3");
                    theme = theme_gold;
                    showDialog(3);
                    break;
                }                
            }

    }

}
