package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
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
//Gionee <GN_BSP_MMI><lifeilong><20170106> modify for ID 31754 begin
import java.lang.reflect.Field;
//Gionee <GN_BSP_MMI><lifeilong><20170106> modify for ID 31754 end
//Gionee zhangke 20160309 add for CR01649229 end
import android.content.Intent;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
public class KeysTest extends Activity implements View.OnClickListener{
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
    private boolean mIsNewCount;
    
    private ArrayList<View> mViewHolder = new ArrayList<View> (); 
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
    private static final String HALL_ON = "NODE_TYPE_HALL_SWITCH_STATE";
    //Gionee zhangke 20160309 add for CR01649229 end
    //Gionee <GN_BSP_MMI><lifeilong><20170106> modify for ID 31754 begin
    public static String itemKey;
    public static String keycode;
    //Gionee <GN_BSP_MMI><lifeilong><20170106> modify for ID 31754 end
    private boolean keyFlag = false;
    private String key = "";
    private String NORMAL = "normal";
    private String EAR = "ear";
    private String HALL = "hall";
    private Intent it;
    private String recordKey = "";
    protected static final String PERSIST_RADIO_DISPATCH_ALL_KEY = "persist.radio.dispatchAllKey";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120924 add for CR00693542 start
        //TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        setContentView(R.layout.keys_test);
        //Gionee zhangke 20151215 modify for CR01609753 start
        TestUtils.setWindowFlags(this);      
        it = this.getIntent();      
        //Gionee zhangke 20151215 modify for CR01609753 end
        mRs = this.getResources();
        TEST_COLOR = mRs.getColor(R.color.test_blue);
        TestUtils.writeNodeState(KeysTest.this,HALL_ON,0);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn.setOnClickListener(this);//visibility
        mRightBtn.setVisibility(View.INVISIBLE);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn.setOnClickListener(this);
        mGrid = (GridView) findViewById(R.id.key_gridview);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170912> modify for ID 210075 begin
        keyFlag = it.getBooleanExtra("as", false);
        key = it.getStringExtra("key");
        Log.d(TAG, "keyflag = " + keyFlag + "   ,  key = " + key);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170912> modify for ID 210075 end
        if(keyFlag){
            configKeysTest();
        } else {
            mItems = TestUtils.getKeyItems(this);
            mItemKeys = TestUtils.getKeyItemKeys(this);
        }

    }
    public void configKeysTest(){
        if(!"true".equals(SystemProperties.get(PERSIST_RADIO_DISPATCH_ALL_KEY))) {
            SystemProperties.set(PERSIST_RADIO_DISPATCH_ALL_KEY, "true");
        }
        if(keyFlag){
            mRestartBtn.setVisibility(View.INVISIBLE);
            TestUtils.asResult(TAG,"","2");
        }
        if(EAR.equals(key)){
            recordKey = EAR;
            mItems = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(
                    R.array.key_test_items_ear)));
            mItemKeys = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(
                    R.array.key_test_keys_ear)));
        } else if (HALL.equals(key)){
            recordKey = HALL;
            mItems = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(
                    R.array.key_test_items_hall)));
            mItemKeys = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(
                    R.array.key_test_keys_hall)));
        } else {
            recordKey = NORMAL;
            mItems = TestUtils.getKeyItems(this);
            mItemKeys = TestUtils.getKeyItemKeys(this);
        }
        Map<String, String> valueKeyMap = new HashMap<String, String>();
        Map<String, String> propDefMap = new HashMap<String, String>();
        Map<String, Integer> propToResMap = new HashMap<String, Integer>();
        if(mItems.size() == mItemKeys.size()) {
            int size = mItemKeys.size();
            for(int i =0; i < size; i++ ) {
                valueKeyMap.put(mItems.get(i), mItemKeys.get(i));
            }
        } else {
            Log.e(TAG, "wrong!");
            return;
        }


    }
    
    @Override
    public void onStart() {
        super.onStart();
        // Gionee xiaolin 20120511 add for CR00596984 start
        if(!keyFlag){
            configKeysTest();
        }
        // Gionee xiaolin 20120511 add for CR00596984 end
        mKeyCount = mItems.size();
        mKeyState = new ArrayList<Integer>();        
        mInputMethondManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        for (int i=0; i < mKeyCount; i++) {
            mKeyState.add(i, 0);
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20171009> modify for ID 215729 begin
        TestUtils.configKeyTestArrays(this);
        if(mKeyState.size() != mItemKeys.size() ){
            Log.d(TAG,"mKeyState.size() != mItemKeys.size() ");
            configKeysTest();
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20171009> modify for ID 215729 end
        Log.e(TAG, "mKeyCount = " + mKeyCount);
        Log.e(TAG, "mKeyState.size()" + mKeyState.size() + "mItemKeys ===" + mItemKeys.toString() + "=== mItems ===" + mItems.toString());
        
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
        if(keyFlag){
            this.finish();
            Log.d(TAG,"onStop as_record_finish_self");
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
                if(keyFlag){
                    TestUtils.asResult(TAG, recordKey, "1");
                }                
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }
            
            case R.id.wrong_btn: {
                if(keyFlag){
                    TestUtils.asResult(TAG, recordKey, "0");
                }                
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


        for (int i=0; i < mKeyCount; i++) {
            mKeyState.add(i, 0);
        }
        Log.e(TAG, "mKeyCount = " + mKeyCount);
        Log.e(TAG, "mKeyState.size()" + mKeyState.size());
        //Gionee <GN_BSP_MMI> <lifeilong> <20170912> modify for ID 210075 begin
        Log.e(TAG, "mItemKeys.size()" + mItemKeys.size());
        if(mKeyState.size() != mItemKeys.size() ){
            configKeysTest();
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170912> modify for ID 210075 end
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
            mRightBtn.setVisibility(View.VISIBLE);
            mRightBtn.setEnabled(true);//mRightBtn.setVisibility(View.INVISIBLE);
        }
    }
    
    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        //Log.d(TAG, "--KeyCode-- : " +  "KeyEvent");  
        if (event.getAction() == event.ACTION_DOWN) {
            //Log.d(TAG, "--KeyCode-- : " +  "ACTION_DOWN");
            int code = event.getKeyCode();
            Log.d(TAG, "--KeyCode-- : " + code );          
            int length = mItemKeys.size();
            for (int i = 0; i < length; i++) {
                //Gionee <GN_BSP_MMI><lifeilong><20170106> modify for ID 31754 begin
                itemKey = mItemKeys.get(i);
                keycode = TestUtils.getKeycode(itemKey);
                Log.e(TAG,"====keycode====" + keycode);
                if (keycode.equals(String.valueOf(code))) {
                    //Gionee zhangke 20160309 add for CR01649229 start
                    //checkKeyDiffValue(keycode);
                    //Gionee zhangke 20160309 add for CR01649229 end
                    if (mKeyState.get(i).equals(0)) {
                        mViewHolder.get(i).setBackgroundResource(R.drawable.grid_view_item_press);
                        // Gionee xiaolin 20120903 modify for CR00682434 start
                        mKeyState.set(i, 1);
                        //Log.e(TAG, "--KeyCode-- : " + mKeyState.get(i) );  
                        // Gionee xiaolin 20120903 modify for CR00682434 end
                        //Gionee <GN_BSP_MMI> <lifeilong> <20170912> modify for ID 210075 begin
                    } else if (mKeyState.get(i).equals(1)) {
                            mViewHolder.remove(i);
                            mItemKeys.remove(i);
                            mKeyState.remove(i);
                            mGrid.setAdapter(mKeysAdapter);
                            mKeysAdapter.notifyDataSetChanged();
                            rightShouldEnable();
                    }
                        //Gionee <GN_BSP_MMI> <lifeilong> <20170912> modify for ID 210075 end
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

    void checkKeyDiffValue(String key){
        Log.i(TAG, "key="+key);
        if(key.equals(HOME_KEYCODE)){
            boolean isHomeKeySupport = getPathResult(TP_HOME_KEY_SUPPORT_PATH).equals("1");
            if(isHomeKeySupport){
                if(!mIsHomeKeySuccess){
                    String result = getPathResult(KEY_PATH);
                    Log.i(TAG, "result = "+result);
                    if(result.isEmpty()){
                        mIsHomeKeySuccess = true;
                    }else if(result.equals(KEY_HOME_DIFF)){
                        mIsHomeKeySuccess = true;
                        Toast.makeText(KeysTest.this, getString(R.string.home_key) + getString(R.string.key_diff_ok), Toast.LENGTH_SHORT).show();
                    }else{
                        mIsHomeKeySuccess = false;
                        Toast.makeText(KeysTest.this, getString(R.string.home_key) + getString(R.string.key_diff_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }else {
                mIsHomeKeySuccess = true;
            }
        }else if(key.equals(BACK_KEYCODE)){
            if(!mIsBackKeySuccess){
                String result = getPathResult(KEY_PATH);
                Log.i(TAG, "result = "+result);
                if(result.isEmpty()){
                    mIsBackKeySuccess = true;
                }else if(result.equals(KEY_BACK_DIFF)){
                    mIsBackKeySuccess = true;
                    Toast.makeText(KeysTest.this, getString(R.string.back_key) + getString(R.string.key_diff_ok), Toast.LENGTH_SHORT).show();
                }else{
                    mIsBackKeySuccess = false;
                    Toast.makeText(KeysTest.this, getString(R.string.back_key) + getString(R.string.key_diff_error), Toast.LENGTH_SHORT).show();
                }
            }
        }else if(key.equals(APP_KEYCODE)){
            if(!mIsAppKeySuccess){
                String result = getPathResult(KEY_PATH);
                Log.i(TAG, "result = "+result);
                if(result.isEmpty()){
                    mIsAppKeySuccess = true;
                }else if(result.equals(KEY_APP_DIFF)){
                    mIsAppKeySuccess = true;
                    Toast.makeText(KeysTest.this, getString(R.string.app_key) + getString(R.string.key_diff_ok), Toast.LENGTH_SHORT).show();
                }else{
                    mIsAppKeySuccess = false;
                    Toast.makeText(KeysTest.this, getString(R.string.app_key) + getString(R.string.key_diff_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
	//Gionee zhangke 20160309 add for CR01649229 end
}
