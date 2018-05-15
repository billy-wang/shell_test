package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Html;
import gn.com.android.mmitest.utils.DswLog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import gn.com.android.mmitest.R;

public class TemperatureTest extends BaseActivity implements SensorEventListener,
        OnClickListener {
    private final String TAG = "TemperatureTest";
    SensorManager mSensorManager = null;
    Sensor mTempSensor = null;

    TextView mChipView = null;
    TextView mObjectView = null;
    TextView mHumanView = null;

    Button mPassButton = null;
    Button mRetestButton = null;
    Button mFailButton = null;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开温度检测 @" + Integer.toHexString(hashCode()));

        setContentView(R.layout.temperature_test);
        initialViews();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mTempSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        mSensorManager.registerListener(this, mTempSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this, mTempSensor);
            mTempSensor = null;
            mSensorManager = null;
        }
        DswLog.d(TAG, "\n****************退出温度检测 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values != null) {
            for (float val : event.values) {
                DswLog.v(TAG, "Temp:" + Float.toString(val));
            }
        }
        updateTemperatureViews(event.values);
    }

    void initialViews() {
        mChipView = (TextView) findViewById(R.id.chip_value);
        mObjectView = (TextView) findViewById(R.id.object_value);
        mHumanView = (TextView) findViewById(R.id.human_value);
        updateTemperatureViews(new float[3]);

        mPassButton = (Button) findViewById(R.id.right_btn);
        mPassButton.setOnClickListener(this);
        mPassButton.setEnabled(true);
        mFailButton = (Button) findViewById(R.id.wrong_btn);
        mFailButton.setOnClickListener(this);
        mRetestButton = (Button) findViewById(R.id.restart_btn);
        mRetestButton.setOnClickListener(this);
    }

    void updateTemperatureViews(float[] temps) {
        if (temps == null)
            return;
        final int LENGTH = temps.length;
        int i = 0;
        String showText = "";
        if (i < LENGTH) {
            showText = String
                    .format(getString(R.string.chip_value), temps[i++]);
            mChipView.setText(Html.fromHtml(showText));
            mChipView.animate();
        }

        if (i < LENGTH) {
            showText = String.format(getString(R.string.object_value),
                    temps[i++]);
            mObjectView.setText(Html.fromHtml(showText));
            mObjectView.animate();
        }

        if (i < LENGTH) {
            showText = String.format(getString(R.string.human_value),
                    temps[i++]);
            mHumanView.setText(Html.fromHtml(showText));
            mHumanView.animate();
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.right_btn:
                TestUtils.rightPress(TAG, this);
                break;
            case R.id.wrong_btn:
                TestUtils.wrongPress(TAG, this);
                break;
            case R.id.restart_btn:
                TestUtils.restart(this, TAG);
                break;

            default:
                break;
        }
    }
}
