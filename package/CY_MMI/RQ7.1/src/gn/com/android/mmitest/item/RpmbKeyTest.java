package gn.com.android.mmitest.item;

import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.content.SharedPreferences;
import android.content.Context;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.pm.PackageManager;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.KeyEvent;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import amigo.app.AmigoProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;

import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
//Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 begin
import gn.com.android.mmitest.item.GnReflectionMethods;
import android.content.ContentResolver;
//Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 end
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class RpmbKeyTest extends Activity  {
    private static final String TAG = "RpmbKeyTest";
    //native byte[] processCmd(Context context, byte[] bytes);
    private boolean mIsWriteSuc = false;
    private boolean mIsReadSuc = false;
    private Context mContext;
    //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 begin
    private final static String AMIGO_SETTING_CC_SWITCH = "control_center_switch";
    private final static int LOCK_CONTROL_CENTER = 0;
    private final static int UNLOCK_CONTOL_CENTER = 1;
    //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 end
    private Object manager;
    private Constructor constructor;
    private Object iFAAManager;
    private byte[] getReadBytes;
    private Object a;// = getIfaaManager();
    private Method processMethod ;//= getProcessMethod();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            TestUtils.setWindowFlags(this);
            //requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            setContentView(R.layout.activity_dialog);
            a = getIfaaManager();
            processMethod = getProcessMethod();
        }

        /*static{
            System.loadLibrary("rpmb_jni");
        }*/

        @Override
        protected void onStart() {
            // TODO Auto-generated method stub
            super.onStart();
        }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setTitle(R.string.set_rpmb_key);
        switch (id){
            case 0:
                builder.setMessage(R.string.rpmb_key_success).setNegativeButton(android.R.string.ok, 
                    new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        RpmbKeyTest.this.finish();
                    }
                }).setCancelable(false);
                dialog = builder.create();
                break;
            case 1 :
                builder.setTitle(R.string.set_rpmb_key)
                .setMessage(getString(R.string.rpmb_key_has_writed))
                .setNegativeButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        RpmbKeyTest.this.finish();
                    }
                }).setCancelable(false);
                dialog = builder.create();          
                break;
            case 2 :
                builder.setTitle(R.string.set_rpmb_key)
                .setMessage(getString(R.string.rpmb_key_note))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, "write rpmb key");
                        writeIfaaKey();
                        if(mIsReadSuc && mIsWriteSuc){
                            TestUtils.writeNodeState(RpmbKeyTest.this,TestUtils.CHARGING_SWITCH_ON,0);
                            Log.e(TAG,"RpmbKeyTest -- > CHARGING_SWITCH_ON -- > 0 ");
                            ContentResolver resolver = getContentResolver();
                            try{
                                GnReflectionMethods gnMethod = new GnReflectionMethods(
                                    "amigo.provider.AmigoSettings",
                                    "putInt", new Class[]{ContentResolver.class, String.class,int.class}, 
                                    new Object[]{resolver, AMIGO_SETTING_CC_SWITCH, UNLOCK_CONTOL_CENTER});
                                gnMethod.getInvokeResult1(RpmbKeyTest.this);
                                Log.i(TAG, "AmigoSettings putInt control_center_switch 1");
                            }catch(Exception e){
                                Log.e(TAG, "Exception = "+e.getMessage());
                            }
                                showDialog(0);
                            }else{
                                Toast.makeText(RpmbKeyTest.this,getString(R.string.rpmb_key_fail),Toast.LENGTH_SHORT).show();
                                RpmbKeyTest.this.finish();
                            }
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i(TAG, "write rpmb key  cancle");
                            RpmbKeyTest.this.finish();
                        }
                }).setCancelable(false);
                dialog = builder.create();
                break;
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 begin
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
        getWindow().getDecorView().getSystemUiVisibility());
        //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 end
        return dialog;
    }

        @Override
        protected void onResume() {
            // TODO Auto-generated method stub
            super.onResume();
            readIfaaKey();
            if(mIsReadSuc){
                Log.i(TAG, "rpmb_key_has_writed");
                showDialog(1);
            }else{
                showDialog(2);
            }
        }

        @Override
        protected void onStop() {
            // TODO Auto-generated method stub
            super.onStop();
        }

        @Override
        protected void onPause() {
            // TODO Auto-generated method stub
            super.onPause();
        }

        @Override
        protected void onDestroy() {
            // TODO Auto-generated method stub
            super.onDestroy();
        }


    private void writeIfaaKey(){
        byte[] writeBytes = new byte[4096];
        writeBytes[4] = 0x0A;
        writeBytes[5] = 0x00;
        writeBytes[6] = 0x60;
        writeBytes[7] = 0x00;
        try{
            Log.i(TAG,"writeIfaaKey"); //getReadBytes = (byte[]) processMethod.invoke(a,context,removeBytes);
            byte[] writeResultBytes = (byte[]) processMethod.invoke(a,RpmbKeyTest.this,writeBytes);
            if(writeResultBytes != null){
                Log.i(TAG, "readIfaaKey ====writeResultBytes[0] "+writeResultBytes[0]);
                Log.i(TAG, "readIfaaKey ====writeResultBytes[1] "+writeResultBytes[1]);
                readIfaaKey();
                if(mIsReadSuc){
                    mIsWriteSuc = true;
                }else{
                    mIsWriteSuc = false;
                }
            }else{
                Log.i(TAG, "getWriteBytes is null");
                mIsWriteSuc = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void readIfaaKey(){
        byte[] readBytes = new byte[]{0,0,0,0, 0x0B,0x00,0x60,0x00};
        try{
            Log.i(TAG,"readIfaaKey"); //getReadBytes = (byte[]) processMethod.invoke(a,context,removeBytes);
            getReadBytes =(byte[]) processMethod.invoke(a,RpmbKeyTest.this,readBytes);;
            //Log.i(TAG, "readIfaaKey ==== "+bytesToInt2(getReadBytes,0));
            Log.i(TAG, "readIfaaKey ====getReadBytes[0] "+getReadBytes[0]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[1] "+getReadBytes[1]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[2] "+getReadBytes[2]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[3] "+getReadBytes[3]);
            if(getReadBytes != null){
                Log.i(TAG, "readIfaaKey = "+getReadBytes.toString().trim());
                //Gionee ningsy 20160715 begin 
                if(getReadBytes[0] == 0){
                    Log.d(TAG,"----------getReadBytes[0] == 0x00-");
                    mIsReadSuc = true;
                }else{
                    mIsReadSuc = false;
                }
                //Gionee ningsy 20160715 end 
                //mIsReadSuc = true;
            }else{
                Log.i(TAG, "getReadBytes is null");
                mIsReadSuc = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


      private Object getIfaaManager(){
        try {
            iFAAManager = null ;
            Class clazz = Class.forName("org.ifaa.android.manager.IFAAManagerFactory");
            Log.e(TAG," oncreate == clazz == " + clazz );
            Constructor[] constructors = clazz.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                constructor = constructors[i];
            }
            manager = constructor.newInstance();
            Method[] methods = clazz.getMethods();
            Method method = null ;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("getIFAAManager")){
                    method = methods[i];
                }
            }
            iFAAManager = method.invoke(manager,RpmbKeyTest.this,0x01);
            Log.d(TAG,"manager = " + manager + "  , method =  "  + method + "  , iFAAManager = " + iFAAManager);
        } catch (ClassNotFoundException e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
        } catch (Exception e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
        }
        return iFAAManager;
    }

    private Method getProcessMethod(){
        Method method = null ;
        try {
            Class clazz = Class.forName("org.ifaa.android.manager.IFAAManager");
            Log.e(TAG," oncreate == clazz == " + clazz );
            Method[] methods = clazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Log.d(TAG," method = " + methods[i]);
                if(methods[i].getName().equals("processCmd")){
                    method = methods[i];
                }//processCmd
            }
        } catch (ClassNotFoundException e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
        } catch (Exception e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
        }
        return method;
    }
}
