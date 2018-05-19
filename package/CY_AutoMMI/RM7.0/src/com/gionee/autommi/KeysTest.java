package com.gionee.autommi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.widget.TextView;
import android.media.AudioManager;
import android.os.SystemProperties;
//Gionee zhangke 20151019 add for CR01571097 start
import com.gionee.autommi.FeatureOption;
//Gionee zhangke 20151019 add for CR01571097 end
//Gionee <GN_BSP_AutoMMI> <lifeilong> <20170711> modify for ID 168402 begin
import android.app.StatusBarManager;
//Gionee <GN_BSP_AutoMMI> <lifeilong> <20170711> modify for ID 168402 end

public class KeysTest extends BaseActivity {
	public static final String TAG = "KeysTest";
        String[] keyCodes;
        String[] keyValues;
        Map<String,String> keyMap = new HashMap<String,String>();
        Set<String> acceptSet = new HashSet<String>();
        PowerManager pm;
        AudioManager am;
        private boolean pass;
        private boolean getEarPhonePhone;
        private TextView content;
        private String cvalue = new String();
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170711> modify for ID 168402 begin
        private StatusBarManager sbm;
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170711> modify for ID 168402 end
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.display);
            content = (TextView) this.findViewById(R.id.keys);
            preprocessKeys();
            if(keyCodes.length != keyValues.length)
            System.exit(-1);

            for(int i = 0; i < keyCodes.length; i++) {
                keyMap.put(keyCodes[i], keyValues[i]);
            }
            pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170711> modify for ID 168402 begin
            sbm = (StatusBarManager) this.getSystemService(Context.STATUS_BAR_SERVICE);
            sbm.disable(StatusBarManager.DISABLE_RECENT);
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170711> modify for ID 168402 end
            ((AutoMMI)getApplication()).recordResult(TAG, "", "0");
        }
     
        private void preprocessKeys() {
            keyCodes = this.getResources().getStringArray(R.array.key_test_keys);
            keyValues = this.getResources().getStringArray(R.array.key_test_values);
            //Gionee zhangke 20151019 add for CR01571097 start
            if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_APP_SUPPORT){
                removeTestItem("187");
            }
            if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_BACK_SUPPORT){
                removeTestItem("4");
            }
            if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_HOME_SUPPORT){
                removeTestItem("3");
            }
            if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_MENU_SUPPORT){
                removeTestItem("82");
            }
            if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_CAMERA_SUPPORT){
                removeTestItem("27");
            }
            if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_FOCUS_SUPPORT){
                removeTestItem("80");
            }
            if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_HALL_SUPPORT){
            //Gionee <GN_BSP_MMI> <lifeilong> <20170412> modify for ID 111812 begin
                removeTestItem("301");
                removeTestItem("302");
            //Gionee <GN_BSP_MMI> <lifeilong> <20170412> modify for ID 111812 end
            }
        //Gionee zhangke 20151019 add for CR01571097 end
        }

        private void removeTestItem(String key){
            List<String> t = new ArrayList<String>(Arrays.<String>asList(keyCodes));
            int i = t.indexOf(key);
            if (i == -1) {
                return;
            }
            t.remove(i);
            keyCodes = t.toArray(new String[1]);
            t = new ArrayList<String>(Arrays.<String>asList(keyValues));
            t.remove(i);
            keyValues = t.toArray(new String[1]);
        }

        @Override
        protected void onStart() {
            // TODO Auto-generated method stub
            super.onStart();
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170523> modify for ID 147106 begin
            //am.setMode(AudioManager.MODE_IN_CALL);
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170523> modify for ID 147106 end
        }

        @Override
        protected void onStop() {
            // TODO Auto-generated method stub
            super.onStop();
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170523> modify for ID 147106 begin
            //am.setMode(AudioManager.MODE_NORMAL);
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170523> modify for ID 147106 end
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170711> modify for ID 168402 begin
            sbm.disable(StatusBarManager.DISABLE_NONE);
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170711> modify for ID 168402 end
            this.finish();
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            // TODO Auto-generated method stub
            int c = event.getKeyCode();
            if ( 79 == c ) c = 85;
            String code = String.valueOf(c);
            if (!keyMap.keySet().contains(code)) {
                return true;
            }
            if (!acceptSet.contains(code)) {
                acceptSet.add(code);
                cvalue += keyMap.get(code) + " : OK" + "\n";
                content.setText(cvalue);
                if (acceptSet.equals(keyMap.keySet())) {
                        pass = true;
                        ((AutoMMI) getApplication()).recordResult(TAG, "", "1");
                    }
            }
            return true;
        }

}
