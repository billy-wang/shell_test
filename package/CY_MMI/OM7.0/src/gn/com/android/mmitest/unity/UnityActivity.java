package gn.com.android.mmitest.unity;

import android.app.Activity;
import android.os.Bundle;
import gn.com.android.mmitest.utils.DswLog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import gn.com.android.mmitest.R;
import android.view.KeyEvent;
import gn.com.android.mmitest.BaseActivity;
import android.widget.Toast;
import gn.com.android.mmitest.unity.bean.User;
import gn.com.android.mmitest.unity.presenter.UserLoginPresenter;
import gn.com.android.mmitest.unity.view.IUserLoginView;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.content.Intent;
import gn.com.android.mmitest.GnMMITest;
import gn.com.android.mmitest.TestUtils;
import gn.com.android.mmitest.AdjvService;
import android.graphics.Color;
/**
 * Created by qiang on 4/14/17.
 */
public class UnityActivity extends BaseActivity implements AdapterView.OnItemClickListener,IUserLoginView {

    private static final String TAG = UnityActivity.class.getName();
    private Button btn_revertFac;
    private TextView tv_title;
    private ListView lv_area;
    private UserLoginPresenter mUserLoginPresenter;
    private static final int WAIT_DLG = 1;
    private SharedPreferences preference;
    private User user;
    private  final String NV_BACKUP_END = "android.intent.action.NV_BACKUP_END";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开一体化检测**************");

        setContentView(R.layout.unity_activity);

        initUnityView();
        initUnityData();
        initAreaTag();
        mUserLoginPresenter = new UserLoginPresenter((IUserLoginView)this);
    }

    @Override
    public void showUnityResult(User user)
    {
        //消除弹框
        Toast.makeText(this,
                ""+user.getUnityResult(), Toast.LENGTH_SHORT).show();
        if (user.getError() == user.ERROR_IS_SUCCED) {
         //   saveLocalTag(user);
            btn_revertFac.setClickable(true);
            btn_revertFac.setTextColor(Color.GREEN);
        }else {
            btn_revertFac.setClickable(false);
            btn_revertFac.setTextColor(Color.WHITE);
        }
    }

    @Override
    public void showLoading()
    {
        startNvService();
      //  showDialog(WAIT_DLG);
    }

    @Override
    public void showrevertResult(String msg) {
        Toast.makeText(this,"标志位写入成功", Toast.LENGTH_SHORT).show();
        revertFactory();
    }

    @Override
    public void hideLoading()
    {

       // removeDialog(WAIT_DLG);
    }
    private void initUnityView() {
        btn_revertFac = (Button) findViewById(R.id.unity_btn_startmmi);
        tv_title  = (TextView) findViewById(R.id.unity_tv_title);
        lv_area   = (ListView) findViewById(R.id.unity_lv_arear);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        user.setIdCountry(str);
        mUserLoginPresenter.unityStart(user);
    }

    private void saveLocalTag(User user) {
        Editor editor = preference.edit();
        editor.putString("AreaTag", user.getIdCountry());
        //暂时不切换，mmi应用配置
        //editor.putString("AreaTagConfigPath", user.getIdConfigPath());
        editor.commit();
    }

    private void initUnityData() {
        String[] couArray = this.getResources().getStringArray(R.array.list_country);
        lv_area.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, couArray));
        lv_area.setOnItemClickListener(this);
        btn_revertFac.setText("恢复出厂设置");
        btn_revertFac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserLoginPresenter.revertFactory(user);
            }
        });
        btn_revertFac.setClickable(false);
    }

    private void initAreaTag() {
        user = new User();
        preference = TestUtils.getSharedPreferences(this);
        String preTag = null;
        String curTag = SystemProperties.get("persist.sys.gn.area");
        if (curTag == null || curTag.length() < 1 ) {
            user.setError(User.ERROR_IS_NOT_SUPPORT_UNITY);//版本不支持一体化
            tv_title.setText("当前版本不支持一体化切换");
        }else {
            //地区标志位不一样
            user.setCurCountry(curTag);
            tv_title.setText("当前版本："+curTag+"\n可选择版本如下：");
        }
    }

    private void revertFactory() {
        Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);
        intent.putExtra("eraseInternalData", false);
        this.sendBroadcast(intent);
        finish();
    }

    /**
     * Start FactoryRest service
     */
    private void startNvService() {
        Intent intent = new Intent(this,
                gn.com.android.mmitest.item.NvService.class);
        startService(intent);
    }

}
