package com.hideep.zcalib.standalone.activity;

import java.util.ArrayList;

import com.hideep.zcalib.standalone.activity.ZCalibView.OnTouchEventListener;
import com.hideep.zcalib.standalone.api.ZCalib;
import gn.com.android.mmitest.R;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.util.Log;

/*****************************************************************************
 * ViewActivity.java - ZCalib Application Example Copyright (c) 2013-2017
 * HiDeep, Incorporated. All rights reserved. Software License Agreement
 * 
 * HiDeep, Inc. is supplying this software for Gionee's own development on
 * Gionee's own application software solely and exclusively for Gionee's own
 * products that HiDeep's IC is used. HiDeep, Inc. is the owner of all
 * intellectual property rights in this software and the right is protected
 * under applicable laws.
 *****************************************************************************/
public class ViewActivity extends Activity {
    private ZCalibView m_CanvasView = null;

    private ZCalib m_ZCalib = null;

    // ZCalib Factor
    private final static int ZCALIB_VERIFICATION_POINT_COUNT = 6;
    private final static int ZCALIB_TYPE = 0;

    private int m_Setting_Target_ZValue = 0;
    private int m_Setting_Spec = 0;

    // ZCalib current temp values
    private int m_CurrentZCalibPointIdx = 0;
    private int m_CurrentZCalibStatus = ZCalib.STATUS_IDLE;
    private int m_CurrentZCalibVerifyStatus = ZCalib.RESULT_NON;
    private int m_CurrentZCalibGetteringStatus = ZCalib.STATUS_GETTERING_CAL_VALUE_IDLE;

    // Verification Result
    private ArrayList<Integer> m_ZCalibVerificationResultArray = null;
    private ArrayList<Float> m_ZCalibVerificationSpecResultArray = null;

    ///////////////////////////////////////////
    boolean m_ZCalibNGConditionStatus = false;

    //Gionee zhangke 20151201 add for CR01591381 start
    private boolean mIsAutoMode = false;
    private boolean mIsPass = false;
    private static final String TAG = "ViewActivity";
    //Gionee zhangke 20151201 add for CR01591381 end
    
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hideep_view);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Screen orientation portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        this.m_Setting_Target_ZValue = getResources().getInteger(R.integer.hideep_z_value);
        this.m_Setting_Spec = getResources().getInteger(R.integer.hideep_spec);

        // findViewById for GUI
        this.m_CanvasView = (ZCalibView) findViewById(R.id.CanvasView);

        // set this instance
        this.m_CanvasView.initViewActivity(this);

        this.m_CurrentZCalibVerifyStatus = ZCalib.RESULT_NON;

        this.m_ZCalibVerificationResultArray = new ArrayList<Integer>();
        this.m_ZCalibVerificationSpecResultArray = new ArrayList<Float>();

        // Set instance ZCalib API
        this.m_ZCalib = new ZCalib();
        this.m_ZCalib.setSpecRange(this.m_Setting_Spec);
        this.m_ZCalib.setZTargetValue(this.m_Setting_Target_ZValue);
        this.m_ZCalib.setVerifyPoint(ZCALIB_VERIFICATION_POINT_COUNT);
        this.m_ZCalib.setType(ZCALIB_TYPE);
        
        // Gionee zhangke 20151218 modify for CR01611622 start
        if(intent != null){
            mIsAutoMode = intent.getBooleanExtra("isAutoMode", false);
            Log.i(TAG, "onCreate mIsAutoMode="+mIsAutoMode);
        }
        if (!mIsAutoMode) {
            this.m_CurrentZCalibStatus = ZCalib.STATUS_IDLE;//ZCalib.STATUS_CURRENT_VERIFY_POINT;
        } else {
            this.m_CurrentZCalibStatus = ZCalib.STATUS_CURRENT_CAL_POINT;

            int ids = this.m_ZCalib.CalibGetteringInit();
            if (ids != ZCalib.SUCCESS_CODE) {
                this.m_CurrentZCalibStatus = ZCalib.STATUS_CURRENT_ERROR;
                this.m_CurrentZCalibVerifyStatus = ZCalib.RESULT_NON;

                // finish();
            }
            // set ZCalib point count for GUI
            this.m_CanvasView.setZCalibPointCount(this.m_ZCalib.getPointCount(), ZCALIB_VERIFICATION_POINT_COUNT);
        }
        // Gionee zhangke 20151218 modify for CR01611622 end


        // listener linked by touch event of GUI
        this.m_CanvasView.OnTouchEventListener(new OnTouchEventListener() {
            private int getPressure(float z) throws NullPointerException {
                // Gionee zhangke 20151218 modify for CR01611622 start
                if (!mIsAutoMode) {
                    m_CanvasView.setPressure_Z((float) m_ZCalib.getZValue());
                    return (int) z;
                } else {
                    if (m_ZCalib.isRunning() == true) {
                        m_CanvasView.setPressure_Z((float) m_ZCalib.getZValue());
                        return (int) z;
                    } else {
                        ///////////////////////////////////////// (GUI DEBUG)
                        m_ZCalibNGConditionStatus = true;
                        m_CurrentZCalibVerifyStatus = ZCalib.RESULT_NG;
                        m_CanvasView.setSpecValue(-999F, 3, "CONN ERROR2[" + 0 + "]");
                        /////////////////////////////////////////
                        GoToDisplayNG();
                        return 0;
                    }
                }
                // Gionee zhangke 20151218 modify for CR01611622 end
            }

            @Override
            public void onUpEvent(int flag, int id, float x, float y, float pressure, int count) {
                int z = this.getPressure(pressure);
                // Gionee zhangke 20151218 modify for CR01611622 start
                if (!mIsAutoMode) {
                    return;
                }
                // Gionee zhangke 20151218 modify for CR01611622 start
                if (m_ZCalib.isRunning() == true) {
                    // cal points
                    if (m_CurrentZCalibStatus == ZCalib.STATUS_CURRENT_CAL_POINT) {
                        m_CurrentZCalibGetteringStatus = m_ZCalib.CalibPointGettering(m_CurrentZCalibPointIdx, z);

                        //Gionee zhangke 20160329 modify for CR01662294 start
                        /*
                        if (m_ZCalib.CalibPointGetteringFinish(m_CurrentZCalibPointIdx) != ZCalib.SUCCESS_CODE) {
                            GoToDisplayNG();

                            ///////////////////////////////////////// (GUI
                            ///////////////////////////////////////// DEBUG)
                            m_CanvasView.setSpecValue(-999F, 2, "NOT ENOUGH TO DATA");
                            /////////////////////////////////////////
                        }
                        */

                        int gatheringIds = m_ZCalib.CalibPointGetteringFinish(m_CurrentZCalibPointIdx);
                        if (gatheringIds != ZCalib.SUCCESS_CODE)
                        {
                            GoToDisplayNG();
                            
                            /////////////////////////////////////////
                            if (gatheringIds == ZCalib.RESULT_ERROR_3D_SPEC2)
                            {
                                m_CanvasView.setSpecValue(-999F, 2, "LIMITATION FOR Z DATA");
                            }
                            else
                            {
                               m_CanvasView.setSpecValue(-999F, 2, "NOT ENOUGH TO DATA");
                            }
                        }
                        //Gionee zhangke 20160329 modify for CR01662294 start
                       
                        // next cal point index
                        m_CurrentZCalibPointIdx++;

                        if (m_ZCalib.getPointCount() == m_CurrentZCalibPointIdx) {
                            m_CurrentZCalibStatus = ZCalib.STATUS_CURRENT_VERIFY_POINT;
                            m_CurrentZCalibPointIdx = 0;
                            if (m_ZCalib.CalibPointGetteringTotalFinish() == ZCalib.SUCCESS_CODE) {
                                m_ZCalib.VerifyInit();
                            }
                        }
                    }
                    // verify points
                    else if (m_CurrentZCalibStatus == ZCalib.STATUS_CURRENT_VERIFY_POINT) {
                        m_CurrentZCalibGetteringStatus = m_ZCalib.VerifyPointGettering(m_CurrentZCalibPointIdx, z);
                        if (m_ZCalib.VerifyPointGetteringFinish(m_CurrentZCalibPointIdx) != ZCalib.SUCCESS_CODE) {
                            ///////////////////////////////////////// (GUI
                            ///////////////////////////////////////// DEBUG)
                            m_CanvasView.setSpecValue(-999F, 3, "NOT ENOUGH TO");
                            m_ZCalibNGConditionStatus = true;
                            /////////////////////////////////////////
                            GoToDisplayNG();
                        }

                        if (m_ZCalib.getVerifyResult(m_CurrentZCalibPointIdx) == m_ZCalib.RESULT_NG) {
                            ///////////////////////////////////////// (GUI
                            ///////////////////////////////////////// DEBUG)
                            if (m_ZCalibNGConditionStatus == false) {
                                m_CanvasView.setSpecValue(m_ZCalib.getSpecNGValue(), m_ZCalib.getSpecNGResult(), "");
                            }
                            /////////////////////////////////////////
                            GoToDisplayNG();
                        }

                        // verify
                        setZCalibVerificationResultArray(m_ZCalib.getVerifyResultTotal());
                        setZCalibVerificationSpecResultArray(m_ZCalib.getVerifyValueTotal());

                        // next cal point index
                        m_CurrentZCalibPointIdx++;

                        if (ZCALIB_VERIFICATION_POINT_COUNT == m_CurrentZCalibPointIdx) {
                            m_CurrentZCalibStatus = ZCalib.STATUS_CURRENT_RESULT;
                            m_CurrentZCalibPointIdx = 0;

                            if (m_ZCalib.VerifyPointGetteringTotalFinish() == ZCalib.SUCCESS_CODE) {
                                // call result function
                                m_CurrentZCalibVerifyStatus = m_ZCalib.getVerifyTotalResult();

                                // to be called by test app.
                                GoToAnotherTest();
                            }
                        }
                    }

                    // refresh GUI
                    if (m_CurrentZCalibStatus != ZCalib.STATUS_CURRENT_RESULT) {
                        m_CurrentZCalibGetteringStatus = ZCalib.STATUS_GETTERING_CAL_VALUE_IDLE;
                        m_CanvasView.setCurrentGUICalPoint(m_CurrentZCalibPointIdx);
                        m_CanvasView.postInvalidate();
                    }
                }
            }

            @Override
            public void onMoveEvent(int flag, int id, float x, float y, float pressure, int count) {
                int z = this.getPressure(pressure);
                // Gionee zhangke 20151218 modify for CR01611622 start
                if (!mIsAutoMode) {
                    return;
                }
                // Gionee zhangke 20151218 modify for CR01611622 start

                if (m_ZCalib.isRunning() == true) {
                    // cal points
                    if (m_CurrentZCalibStatus == ZCalib.STATUS_CURRENT_CAL_POINT) {
                        m_CurrentZCalibGetteringStatus = m_ZCalib.CalibPointGettering(m_CurrentZCalibPointIdx, z);
                    }
                    // verify points
                    else if (m_CurrentZCalibStatus == ZCalib.STATUS_CURRENT_VERIFY_POINT) {
                        m_CurrentZCalibGetteringStatus = m_ZCalib.VerifyPointGettering(m_CurrentZCalibPointIdx, z);
                    }

                    m_CanvasView.setCurrentGUICalPoint(m_CurrentZCalibPointIdx);
                    // m_CanvasView.postInvalidate();
                }
            }

            @Override
            public void onDownEvent(int flag, int id, float x, float y, float pressure, int count) {
                int z = this.getPressure(pressure);
                // Gionee zhangke 20151218 modify for CR01611622 start
                if (!mIsAutoMode) {
                    return;
                }
                // Gionee zhangke 20151218 modify for CR01611622 start

                if (m_ZCalib.isRunning() == true) {
                    // cal points
                    if (m_CurrentZCalibStatus == ZCalib.STATUS_CURRENT_CAL_POINT) {
                        m_CurrentZCalibGetteringStatus = m_ZCalib.CalibPointGettering(m_CurrentZCalibPointIdx, z);
                        //
                        if (m_ZCalib.CalibPointGetterinBegin(m_CurrentZCalibPointIdx) != ZCalib.SUCCESS_CODE) {
                            ///////////////////////////////////////// (GUI
                            ///////////////////////////////////////// DEBUG)
                            m_CanvasView.setSpecValue(-999F, 3, "GATHERING ERROR (CAL)");
                            m_ZCalibNGConditionStatus = true;
                            ////////////////////////////////////////
                            GoToDisplayNG();
                        }
                    }
                    // verify points
                    else if (m_CurrentZCalibStatus == ZCalib.STATUS_CURRENT_VERIFY_POINT) {
                        m_CurrentZCalibGetteringStatus = m_ZCalib.VerifyPointGettering(m_CurrentZCalibPointIdx, z);
                        //
                        if (m_ZCalib.VerifyPointGetteringBegin(m_CurrentZCalibPointIdx) != ZCalib.SUCCESS_CODE) {
                            ///////////////////////////////////////// (GUI
                            ///////////////////////////////////////// DEBUG)
                            m_CanvasView.setSpecValue(-999F, 3, "GATHERING ERROR (VERIFY)");
                            m_ZCalibNGConditionStatus = true;
                            ////////////////////////////////////////
                            GoToDisplayNG();
                        }
                    }
                }
            }
        });
    }

    public void GoToAnotherTest() {
        this.m_ZCalib.Close();
        //Gionee zhangke 20151201 add for CR01591381 start 
        mIsPass = true;
        //Gionee zhangke 20151201 add for CR01591381 end

    }

    public int getCurrentZCalibPointIdx() {
        return this.m_CurrentZCalibPointIdx;
    }

    public int getCurrentZCalibStatus() {
        return this.m_CurrentZCalibStatus;
    }

    public int getCurrentZCalibVerifyStstus() {
        return this.m_CurrentZCalibVerifyStatus;
    }

    public int getCurrentZCalibGetteringStatus() {
        return this.m_CurrentZCalibGetteringStatus;
    }

    public ZCalib getZCalib() {
        return this.m_ZCalib;
    }

    public int getZCalibVerificationResultArray(int p) {
        return this.m_ZCalibVerificationResultArray.get(p);
    }

    public float getZCalibVerificationSpecResultArray(int p) {
        return this.m_ZCalibVerificationSpecResultArray.get(p);
    }

    public void setZCalibVerificationResultArray(ArrayList<Integer> p) {
        this.m_ZCalibVerificationResultArray = p;
    }

    public void setZCalibVerificationSpecResultArray(ArrayList<Float> p) {
        this.m_ZCalibVerificationSpecResultArray = p;
    }

    public void GoToDisplayPASS() {
        this.m_CurrentZCalibStatus = ZCalib.STATUS_CURRENT_RESULT;
        this.m_CurrentZCalibVerifyStatus = ZCalib.RESULT_PASS;
        this.m_CanvasView.postInvalidate();
    }

    public void GoToDisplayNG() {
        this.m_CurrentZCalibStatus = ZCalib.STATUS_CURRENT_RESULT;
        this.m_CurrentZCalibVerifyStatus = ZCalib.RESULT_NG;
        this.m_CanvasView.postInvalidate();
        //Gionee zhangke 20151201 add for CR01591381 start 
        mIsPass = false;
        //Gionee zhangke 20151201 add for CR01591381 end 
    }

	//Gionee zhangke 20151201 add for CR01591381 start
    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        Intent intent = new Intent();
        intent.putExtra("isPass", mIsPass);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
    //Gionee zhangke 20151201 add for CR01591381 end

    /**
     * Finish activity
     */
    private void FinishViewActivityEvent() {
        // Finish activity
        this.m_ZCalib.Close();
        finish();
    }

}
