package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
//Gionee zhangke 20160309 add for CR01649229 start
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.widget.Toast;
//Gionee zhangke 20160309 add for CR01649229 end

public class KeysTest extends BaseActivity implements View.OnClickListener {
    public TextView mPowerTv;
    private int mPowerInt;
    public TextView mVolumeUpTv;
    private int mVolumeUpInt;
    public TextView mVolumeDownTv;
    private int mVolumeDownInt;
    public TextView mMenuTv;
    private int mMenuInt;
    public TextView mHomeTv;
    private int mHomeInt;
    public TextView mBackTv;
    private int mBackInt;
    public TextView mCameraTv;
    private int mCameraInt;
    private Button mQuitBtn;
    private Resources mRs;
    private int TEST_COLOR;
    private TextView mSearchTv;
    private int mSearchInt;
    private ToneGenerator mToneGenerator;
    private Object mToneGeneratorLock = new Object();
    private static final int TONE_LENGTH_MS = 85;
    private boolean mIsNewCount;

    private ArrayList<View> mViewHolder = new ArrayList<View>();
    private ArrayList<String> mItems;
    private KeysAdapter mKeysAdapter;
    private ArrayList<Integer> mKeyState;
    private GridView mGrid;
    private ArrayList<String> mItemKeys;
    private int mKeyCount;
    private int mKeyPressCount;

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private static final String TAG = "KeysTest";

    // Gionee liss 20111215 add for CR00478802 start
    public InputMethodManager mInputMethondManager;
    // Gionee liss 20111215 add for CR00478802 end
    //Gionee zhangke 20160309 add for CR01649229 start
    private static final String KEY_PATH = "/sys/devices/platform/tp_wake_switch/factory_keydiff_check";
    private static final String KEY_APP_DIFF = "1";
    private static final String KEY_HOME_DIFF = "2";
    private static final String KEY_BACK_DIFF = "3";
    private static final String TP_HOME_KEY_SUPPORT_PATH = "sys/devices/platform/tp_wake_switch/home_key_check";
    private boolean mIsHomeKeySuccess = false;
    private boolean mIsBackKeySuccess = false;
    private boolean mIsAppKeySuccess = false;
    private static final String HOME_KEYCODE = "3";
    private static final String BACK_KEYCODE = "4";
    private static final String APP_KEYCODE = "187";

    //Gionee zhangke 20160309 add for CR01649229 end
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        setContentView(R.layout.keys_test);
        mRs = this.getResources();
        TEST_COLOR = mRs.getColor(R.color.test_blue);

        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn.setOnClickListener(this);
        mGrid = (GridView) findViewById(R.id.key_gridview);


    }

    @Override
    public void onStart() {
        super.onStart();
        newToneGenerator();
        // Gionee xiaolin 20120511 add for CR00596984 start
        TestUtils.configKeyTestArrays(this);
        // Gionee xiaolin 20120511 add for CR00596984 end
        mItems = TestUtils.getKeyItems(this);
        mItemKeys = TestUtils.getKeyItemKeys(this);
        mKeyCount = mItems.size();
        mKeyState = new ArrayList<Integer>();

        mInputMethondManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        for (int i = 0; i < mKeyCount; i++) {
            mKeyState.add(i, 0);
        }
        Log.e(TAG, "mKeyCount = " + mKeyCount);
        Log.e(TAG, "mKeyState.size()" + mKeyState.size());

        for (int i = 0; i < mKeyCount; i++) {
            TextView v = null;
            v = (TextView) KeysTest.this.getLayoutInflater().inflate(
                    R.layout.gridview_item, mGrid, false);

            v.setText(mItems.get(i));
            mViewHolder.add(v);
        }
        mKeysAdapter = new KeysAdapter();
        mGrid.setAdapter(mKeysAdapter);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (null != mViewHolder) {
            mViewHolder.clear();
        }
        releaseToneGenerator();
    }

    private void newToneGenerator() {
        // if the mToneGenerator creation fails, just continue without it.  It is
        // a local audio signal, and is not as important as the dtmf tone itself.
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    // we want the user to be able to control the volume of the dial tones
                    // outside of a call, so we use the stream type that is also mapped to the
                    // volume control keys for this activity
                    // Gionee zhangxiaowei 2013.6.14 modified for CR00825845 start
                    mToneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 240);
                    setVolumeControlStream(AudioManager.STREAM_DTMF);
                    // Gionee zhangxiaowei 2013.6.14 modified for CR00825845 end
                } catch (RuntimeException e) {
                    Log.w(TAG, "Exception caught while creating local tone generator: " + e);
                    mToneGenerator = null;
                }
            }
        }
    }

    private void releaseToneGenerator() {
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator != null) {
                mToneGenerator.release();
                mToneGenerator = null;
            }
        }
    }

    void playTone(int tone) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
                || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            return;
        }

        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.w(TAG, "playTone: mToneGenerator == null, tone: " + tone);
                return;
            }
            mToneGenerator.startTone(tone, TONE_LENGTH_MS);
        }
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.restart_btn: {
                restartKeyTest();
                break;
            }

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }
        }
    }

    void restartKeyTest() {

        mRightBtn.setEnabled(false);
        mKeyPressCount = 0;
        TestUtils.configKeyTestArrays(this);
        mItems = TestUtils.getKeyItems(this);
        mItemKeys = TestUtils.getKeyItemKeys(this);
        mKeyCount = mItems.size();
        mKeyState.clear();
        mViewHolder.clear();


        for (int i = 0; i < mKeyCount; i++) {
            mKeyState.add(i, 0);
        }
        Log.e(TAG, "mKeyCount = " + mKeyCount);
        Log.e(TAG, "mKeyState.size()" + mKeyState.size());

        for (int i = 0; i < mKeyCount; i++) {
            TextView v = null;
            v = (TextView) KeysTest.this.getLayoutInflater().inflate(
                    R.layout.gridview_item, mGrid, false);
            v.setText(mItems.get(i));
            mViewHolder.add(v);
        }
        mGrid.setAdapter(mKeysAdapter);

        //Gionee zhangke 20160309 add for CR01649229 start
        mIsHomeKeySuccess = false;
        mIsAppKeySuccess = false;
        mIsBackKeySuccess = false;
        //Gionee zhangke 20160309 add for CR01649229 end
    }


    public void rightShouldEnable() {
        mKeyPressCount++;
        Log.e(TAG, "mKeyState.size == " + mKeyState.size());
        if (mKeyCount == mKeyPressCount) {
            mRightBtn.setEnabled(true);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "--KeyCode-- : " + "KeyEvent");

        if (event.getAction() == event.ACTION_DOWN) {
            Log.d(TAG, "--KeyCode-- : " + "ACTION_DOWN");
            playTone(ToneGenerator.TONE_DTMF_1);
            int code = event.getKeyCode();
            Log.d(TAG, "--KeyCode-- : " + code);
            int length = mItemKeys.size();
            for (int i = 0; i < length; i++) {

                if (mItemKeys.get(i).equals(String.valueOf(code))) {
                    /*Gionee huangjianqiang 20160414 add for CR01675300 begin*/
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(new long[]{0, 100}, -1);
                    /*Gionee huangjianqiang 20160414 add for CR01675300 end*/
                    //Gionee zhangke 20160309 add for CR01649229 start
                    checkKeyDiffValue(mItemKeys.get(i));
                    //Gionee zhangke 20160309 add for CR01649229 end
                    if (mKeyState.get(i).equals(0)) {
                        mViewHolder.get(i).setBackgroundResource(R.drawable.grid_view_item_press);
                        // Gionee xiaolin 20120903 modify for CR00682434 start
                        mKeyState.set(i, 1);
                        //Log.e(TAG, "--KeyCode-- : " + mKeyState.get(i) );  
                        // Gionee xiaolin 20120903 modify for CR00682434 end
                    } else if (mKeyState.get(i).equals(1)) {
                        //Gionee zhangke 20160309 modify for CR01649229 start
                        boolean isRemoved = true;
                        if (mItemKeys.get(i).equals(HOME_KEYCODE)) {
                            if (!mIsHomeKeySuccess) {
                                isRemoved = false;
                            }
                        } else if (mItemKeys.get(i).equals(BACK_KEYCODE)) {
                            if (!mIsBackKeySuccess) {
                                isRemoved = false;
                            }
                        } else if (mItemKeys.get(i).equals(APP_KEYCODE)) {
                            if (!mIsAppKeySuccess) {
                                isRemoved = false;
                            }
                        }
                        if (isRemoved) {
                            mViewHolder.remove(i);
                            mItemKeys.remove(i);
                            mKeyState.remove(i);
                            mGrid.setAdapter(mKeysAdapter);
                            rightShouldEnable();
                        }
                        //Gionee zhangke 20160309 modify for CR01649229 end
                    }
                    return true;
                }
            }

        }
        return true;
    }


    class KeysAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mViewHolder.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mViewHolder.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            return mViewHolder.get(position);
        }

    }

    //Gionee zhangke 20160309 add for CR01649229 start
    public String getPathResult(String path) {
        String result = "";
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File currentFilePath = new File(path);
                if (currentFilePath.exists()) {
                    fileInputStream = new FileInputStream(currentFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        result = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*Gionee huangjianqiang 20160323 modify for CR01658304 begin*/
    void checkKeyDiffValue(String key) {
        Log.i(TAG, "key=" + key);
        if (key.equals(HOME_KEYCODE)) {
            mIsHomeKeySuccess = true;
//            boolean isHomeKeySupport = getPathResult(TP_HOME_KEY_SUPPORT_PATH).equals("1");
//            if(isHomeKeySupport){
//                if(!mIsHomeKeySuccess){
//                    String result = getPathResult(KEY_PATH);
//                    Log.i(TAG, "result = "+result);
//                    if(result.equals(KEY_HOME_DIFF)){
//                        mIsHomeKeySuccess = true;
//                        Toast.makeText(KeysTest.this, getString(R.string.home_key) + getString(R.string.key_diff_ok), Toast.LENGTH_SHORT).show();
//                    }else{
//                        mIsHomeKeySuccess = false;
//                        Toast.makeText(KeysTest.this, getString(R.string.home_key) + getString(R.string.key_diff_error), Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }else {
//                mIsHomeKeySuccess = true;
//            }
        } else if (key.equals(BACK_KEYCODE)) {
            mIsBackKeySuccess = true;
//            if(!mIsBackKeySuccess){
//                String result = getPathResult(KEY_PATH);
//                Log.i(TAG, "result = "+result);
//                if(result.equals(KEY_BACK_DIFF)){
//                    mIsBackKeySuccess = true;
//                    Toast.makeText(KeysTest.this, getString(R.string.back_key) + getString(R.string.key_diff_ok), Toast.LENGTH_SHORT).show();
//                }else{
//                    mIsBackKeySuccess = false;
//                    Toast.makeText(KeysTest.this, getString(R.string.back_key) + getString(R.string.key_diff_error), Toast.LENGTH_SHORT).show();
//                }
//            }
        } else if (key.equals(APP_KEYCODE)) {
            mIsAppKeySuccess = true;
//            if(!mIsAppKeySuccess){
//                String result = getPathResult(KEY_PATH);
//                Log.i(TAG, "result = "+result);
//                if(result.equals(KEY_APP_DIFF)){
//                    mIsAppKeySuccess = true;
//                    Toast.makeText(KeysTest.this, getString(R.string.app_key) + getString(R.string.key_diff_ok), Toast.LENGTH_SHORT).show();
//                }else{
//                    mIsAppKeySuccess = false;
//                    Toast.makeText(KeysTest.this, getString(R.string.app_key) + getString(R.string.key_diff_error), Toast.LENGTH_SHORT).show();
//                }
//            }
        }
    }
    //Gionee zhangke 20160309 add for CR01649229 end
    /*Gionee huangjianqiang 20160323 modify for CR01658304 end*/
}
