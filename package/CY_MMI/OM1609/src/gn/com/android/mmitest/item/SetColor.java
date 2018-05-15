package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.GnMMITest;
import gn.com.android.mmitest.SingleTestGridView;
import gn.com.android.mmitest.TestResult;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.SoundEffectConstants;
import android.telephony.TelephonyManager;

import gn.com.android.mmitest.R;

import android.app.Dialog;
import android.app.AlertDialog;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.widget.ArrayAdapter;
import android.os.AsyncResult;
import android.view.IWindowManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.view.Surface;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.*;
import java.io.File;

import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.os.ServiceManager;
import android.view.IWindowManager;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.os.PowerManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*Gionee futao 20151029 modify for CR01573429 begin*/
import android.content.res.Resources;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
        /*Gionee futao 20151029 modify for CR01573429 end*/

public class SetColor extends BaseActivity implements OnClickListener {

    private static final String TAG = "SetColor";
    private boolean ex = false;
    private Button quitBtn;
    TextView textView;
    String[] colorStrings1;
    private String colorSelected;
    /*Gionee futao 20151029 modify for CR01573429 begin*/
    private Resources mRs;
    private boolean isForSale = false;
        /*Gionee futao 20151029 modify for CR01573429 end*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            /*Gionee futao 20151029 modify for CR01573429 begin*/
        setForsaleFlag(getIntent());
        ConfigLanguage();
        mRs = getResources();
            /*Gionee futao 20151029 modify for CR01573429 end*/
        setContentView(R.layout.main_list1);
        ListView lv = (ListView) findViewById(android.R.id.list);
        Log.e("zhangxiaowei", "TestUtils.mIsAutoMode_2 = " + TestUtils.mIsAutoMode_2);
        Hashtable<String, String> table = null;//= GnDymThemePropertiesParser.getDisplayStringsTable();
        try {
            Class<?> Clazz = Class.forName("android.os.GnDymThemePropertiesParser");
                /*Gionee futao 20151029 modify for CR01573429 begin*/
            Method getDisplayStringsTable = null;
            if (isForSale) {
                getDisplayStringsTable = Clazz.getMethod("getThemesTable");
            } else {
                getDisplayStringsTable = Clazz.getMethod("getDisplayStringsTable");
            }
                /*Gionee futao 20151029 modify for CR01573429 end*/
            table = (Hashtable<String, String>) getDisplayStringsTable.invoke(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        Set<String> keySet = table.keySet();
        Iterator<String> it = keySet.iterator();

        List<String> list = new ArrayList<String>();
        List<String> list1 = new ArrayList<String>();
        while (it.hasNext()) {
            String num = it.next();
            list1.add(num);
            String value = table.get(num);
            String unicodeValue = null;//GnDymThemePropertiesParser.unicodeToString(value);//sting--zhongwen
            try {
                Class<?> Clazz = Class.forName("android.os.GnDymThemePropertiesParser");
                Method unicodeToString = Clazz.getMethod("unicodeToString", String.class);
                unicodeValue = (String) unicodeToString.invoke(null, value);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            list.add(unicodeValue);
            Log.d("zhangxiaowei", unicodeValue);
        }

        String[] colorStrings = list.toArray(new String[list.size()]);
        colorStrings1 = list1.toArray(new String[list1.size()]);
        quitBtn = (Button) findViewById(R.id.quit_btn);
        quitBtn.setEnabled(false);
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, colorStrings));
        quitBtn.setOnClickListener(this);
        lv.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                colorSelected = colorStrings1[position];
                String text = mRs.getString(R.string.setcolor_note);
                ;
                String text1 = text + "" + ((TextView) view).getText();
                SetColor.this.setTitle(text1);
                quitBtn.setEnabled(true);
                Log.d(TAG, "setcolor  Select Color =" + colorStrings1[position]);
            }
        });

        SetColor.this.setTitle(R.string.setcolor_title);

    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Log.e(TAG, "onClick  hhhhh");
        switch (v.getId()) {

            case R.id.quit_btn: {
                quitBtn.setEnabled(false);
                Log.e(TAG, "onClick  R.id.quit_btn");

                SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                editor.putString("45", colorSelected);
                editor.commit();
                    /*Gionee futao modify for CR01663888 begin*/
                SystemProperties.set("persist.radio.dynamic.theme", colorSelected);
                    /*Gionee futao modify for CR01663888 end*/

                Log.e(TAG, "onClick colorSelected =" + colorSelected);
                if ("true".equals(SystemProperties.get("persist.radio.setcolor", "false"))) {
                    this.finish();
                        /*Gionee futao 20151029 modify for CR01573429 begin*/
                    Intent it = new Intent(this, TestResult.class);
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    Log.e(TAG, "onClick  forSale" + isForSale + " " + TAG);
                    if (isForSale) {
                        it.putExtra("forSale", true);
                        it.putExtra("isReEnter", false);
                    }
                    startActivity(it);
                        /*Gionee futao 20151029 modify for CR01573429 end*/

                } else {
                    this.finish();
                }
            }
            break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        return true;
    }

    /*Gionee futao 20151029 modify for CR01573429 begin*/
    void setForsaleFlag(Intent intent) {

        Log.e(TAG, "SetForsaleFlag enter");
        if (intent == null) {
            return;
        }
        isForSale = intent.getBooleanExtra("forSale", false);
        Log.e(TAG, "SetForsaleFlag sales:forSale:" + isForSale);
    }

    private void ConfigLanguage() {

        Configuration config = getResources().getConfiguration();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (!isForSale) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            config.locale = Locale.getDefault();
            ;
        }
        Log.e(TAG, "config.locale:" + config.locale);
        getResources().updateConfiguration(config, metrics);
    }
      /*Gionee futao 20151029 modify for CR01573429 end*/
}
