/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gn.com.android.mmitest.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioDevicePort;
import android.media.AudioDevicePortConfig;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioManager.OnAudioPortUpdateListener;
import android.media.AudioMixPort;
import android.media.AudioPatch;
import android.media.AudioPort;
import android.media.AudioPortConfig;
import android.media.AudioRecord;
import android.media.AudioSystem;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import gn.com.android.mmitest.item.FmUtils;
import gn.com.android.mmitest.item.FmRecorder;
import gn.com.android.mmitest.item.FmListener;
import gn.com.android.mmitest.item.FmStation;
import gn.com.android.mmitest.item.FmStation.Station;

//Gionee <bug> <jiaoyuan> <2013-09-09> add for CR00889510 begin
//Gionee <bug> <jiaoyuan> <2013-09-09> add for CR00889510 end
//Gionee zhangke 20151030 add for CR01577644 start
//Gionee zhangke 20151030 add for CR01577644 end
/**
 * Background service to control FM or do background tasks.
 */
public class FmService extends Service implements FmRecorder.OnRecorderStateChangedListener {

    private HandlerThread mHandlerThread;
    private Looper mHandlerThreadLooper;
    private int mServiceStartId = -1;
    private FmOnAudioPortUpdateListener mAudioPortUpdateListener = null;
    private Thread mRenderThread = null;
    private volatile AudioRecord mAudioRecord = null;
    private volatile AudioTrack mAudioTrack = null;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORD_BUF_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            CHANNEL_CONFIG, AUDIO_FORMAT);
    private volatile boolean mIsRender = false;
    AudioDevicePort mAudioSource = null;
    AudioDevicePort mAudioSink = null;

    private boolean mIsPlaying192KHz = false;
    private static final String TAG = "FmService";
    // Broadcast messages from clients to FM service.
    public static final String ACTION_TOFMSERVICE_POWERDOWN
    = "com.mediatek.FMRadio.FMRadioService.ACTION_TOFMSERVICE_POWERDOWN";
    // Broadcast messages from other sounder APP to FM service
    private static final String SOUND_POWER_DOWN_MSG = "com.android.music.musicservicecommand";
    private static final String FM_SEEK_PREVIOUS = "fmradio.seek.previous";
    private static final String FM_SEEK_NEXT = "fmradio.seek.next";
    private static final String FM_TURN_OFF = "fmradio.turnoff";
    private static final String CMDPAUSE = "pause";
    //Gionee add for ximalaya
    public static final String ACTION_FM_SERVER_STOP="com.mediatek.FMRadio.server.exit";
    // HandlerThread Keys
    private static final String FM_FREQUENCY = "frequency";
    private static final String OPTION = "option";
    private static final String RECODING_FILE_NAME = "name";

    // RDS events
    // PS
    private static final int RDS_EVENT_PROGRAMNAME = 0x0008;
    // RT
    private static final int RDS_EVENT_LAST_RADIOTEXT = 0x0040;
    // AF
    private static final int RDS_EVENT_AF = 0x0080;

    // Headset
    private static final int HEADSET_PLUG_IN = 1;

    // Notification id
    private static final int NOTIFICATION_ID = 1;

    // ignore audio data
    private static final int AUDIO_FRAMES_TO_IGNORE_COUNT = 3;

    // Set audio policy for FM
    // should check AUDIO_POLICY_FORCE_FOR_MEDIA in audio_policy.h
    private static final int FOR_PROPRIETARY = 1;
    // Forced Use value
    private int mForcedUseForMedia;

    // FM recorder
    FmRecorder mFmRecorder = null;
    private BroadcastReceiver mSdcardListener = null;
    private int mRecordState = FmRecorder.STATE_INVALID;
    private int mRecorderErrorType = -1;
    // If eject record sdcard, should set Value false to not record.
    // Key is sdcard path(like "/storage/sdcard0"), V is to enable record or
    // not.
    private HashMap<String, Boolean> mSdcardStateMap = new HashMap<String, Boolean>();
    // The show name in save dialog but saved in service
    // If modify the save title it will be not null, otherwise it will be null
    private String mModifiedRecordingName = null;
    // record the listener list, will notify all listener in list
    private ArrayList<Record> mRecords = new ArrayList<Record>();
    // record FM whether in recording mode
    private boolean mIsInRecordingMode = false;
    // record sd card path when start recording
    private static String sRecordingSdcard = FmUtils.getDefaultStoragePath();

    // RDS
    // PS String
    private String mPsString = "";
    // RT String
    private String mRtTextString = "";
    // Notification target class name
    private String mTargetClassName = "FmMainActivity";
    // RDS thread use to receive the information send by station
    private Thread mRdsThread = null;
    // record whether RDS thread exit
    private boolean mIsRdsThreadExit = false;

    // State variables
    // Record whether FM is in native scan state
    private boolean mIsNativeScanning = false;
    // Record whether FM is in scan thread
    private boolean mIsScanning = false;
    // Record whether FM is in seeking state
    private boolean mIsNativeSeeking = false;
    // Record whether FM is in native seek
    private boolean mIsSeeking = false;
    // Record whether searching progress is canceled
    private boolean mIsStopScanCalled = false;
    // Record whether is speaker used
    private boolean mIsSpeakerUsed = false;
    // Record whether device is open
    private boolean mIsDeviceOpen = false;
    // Record Power Status
    private volatile int mPowerStatus = POWER_DOWN;

    public static int POWER_UP = 0;
    public static int DURING_POWER_UP = 1;
    public static int POWER_DOWN = 2;
    // Record whether service is init
    private boolean mIsServiceInited = false;
    // Fm power down by loss audio focus,should make power down menu item can
    // click
    private  volatile boolean mIsPowerDown = false;
    // distance is over 100 miles(160934.4m)
    private boolean mIsDistanceExceed = false;
    // FmMainActivity foreground
    private boolean mIsFmMainForeground = true;
    // FmFavoriteActivity foreground
    private boolean mIsFmFavoriteForeground = false;
    // FmRecordActivity foreground
    private boolean mIsFmRecordForeground = false;
    // Instance variables
    private Context mContext = null;
    private AudioManager mAudioManager = null;
    private ActivityManager mActivityManager = null;
    //private MediaPlayer mFmPlayer = null;
    private WakeLock mWakeLock = null;
    // Audio focus is held or not
    private volatile boolean mIsAudioFocusHeld = false;
    // Focus transient lost
    private boolean mPausedByTransientLossOfFocus = false;
    private int mCurrentStation = FmUtils.DEFAULT_STATION;
    // Headset plug state (0:long antenna plug in, 1:long antenna plug out)
    private int mValueHeadSetPlug = 1;
    // For bind service
    private final IBinder mBinder = new ServiceBinder();
    // Broadcast to receive the external event
    private FmServiceBroadcastReceiver mBroadcastReceiver = null;
    // Async handler
    private FmRadioServiceHandler mFmServiceHandler;

    private boolean mPrevBtHeadsetState = false;
    // Lock for lose audio focus and receive SOUND_POWER_DOWN_MSG
    // at the same time
    // while recording call stop recording not finished(status is still
    // RECORDING), but
    // SOUND_POWER_DOWN_MSG will exitFm(), if it is RECORDING will discard the
    // record.
    // 1. lose audio focus -> stop recording(lock) -> set to IDLE and show save
    // dialog
    // 2. exitFm() -> check the record status, discard it if it is recording
    // status(lock)
    // Add this lock the exitFm() while stopRecording()
    private Object mStopRecordingLock = new Object();
    // The listener for exit, should finish favorite when exit FM
    //private static OnExitListener sExitListener = null;
    // The latest status for mute/unmute
    private boolean mIsMuted = false;

    // Audio Patch
    private AudioPatch mAudioPatch = null;
    private Object mRenderLock = new Object();
    private Object mRenderingLock = new Object();
    private boolean mIsParametersSet = false;
    private boolean mIsOutputDeviceChanged = false;

    private Notification.Builder mNotificationBuilder = null;
    private BigTextStyle mNotificationStyle = null;

    //Gionee zhangke 20151030 add for CR01577644 start
    final static String MMI_PACKAGE = "gn.com.android.mmitest";
    //Gionee zhangke 20151030 add for CR01577644 end
    private Toast mToast = null;
    //Gionee jingcl 20161109 add;
    private Object mAidlPowerdownLock = new Object();
    private Object mAidlCloseDeviceLock = new Object();
    private boolean mIsThrdAPPExist = false;
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onBind()");
        //Gionee zhangke 20151030 add for CR01577644 start
        /*if (intent.getAction() != null &&
                intent.getAction().equals("com.android.fmradio.IFmRadioService")) {
            Log.i(TAG," return mAIDLBinder ");
            return mAIDLBinder;
        }*/
        //Gionee zhangke 20151030 add for CR01577644 end
        return mBinder;
    }

    /**
     * class use to return service instance
     */
    public class ServiceBinder extends Binder {
        /**
         * get FM service instance
         *
         * @return service instance
         */
        FmService getService() {
            return FmService.this;
        }
    }

    /**
     * Broadcast monitor external event, Other app want FM stop, Phone shut
     * down, screen state, headset state
     */
    private class FmServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String command = intent.getStringExtra("command");
            Log.i(TAG, "onReceive, action = " + action + " / command = " + command);
            // other app want FM stop, stop FM
            if (ACTION_TOFMSERVICE_POWERDOWN.equals(action)||(SOUND_POWER_DOWN_MSG.equals(action) && CMDPAUSE.equals(command))) {
                //modify for 13822 start
                //              showToast(getString(R.string.fm_shutdown_automatically));//add tips
                //modify for 13822 end
                // need remove all messages, make power down will be execute
                mFmServiceHandler.removeCallbacksAndMessages(null);
                exitFm();
                stopSelf();
                //Gionee add for ximalaya
                sendBroadcast(mFMserviceExit);
                // phone shut down, so exit FM
            } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
                /**
                 * here exitFm, system will send broadcast, system will shut
                 * down, so fm does not need call back to activity
                 */
                mFmServiceHandler.removeCallbacksAndMessages(null);
                exitFm();
                // screen on, if FM play, open rds
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                setRdsAsync(true);
                // screen off, if FM play, close rds
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                setRdsAsync(false);
                // show notification after screen off, because when screen on at landscape,
                // activity will only call onpause, FmMainActivity can't show notificaiton
                updatePlayingNotification();
                // switch antenna when headset plug in or plug out
            } 
            else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                boolean enabled = intent.getBooleanExtra("state", false);
                Log.i(TAG,"airplane enable state is:" + enabled);
                if(enabled){
                    showToast("====001=====");
                    exitFm();
                    stopSelf();
                    //Gionee add for ximalaya
                    sendBroadcast(mFMserviceExit);
                }
            }
            else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                
                // switch antenna should not impact audio focus status
                mValueHeadSetPlug = (intent.getIntExtra("state", -1) == HEADSET_PLUG_IN) ? 0 : 1;
                Log.i(TAG," mValueHeadSetPlug = " + mValueHeadSetPlug);
                if (mIsThrdAPPExist) {
                    Log.i(TAG,"thrsAPP is cunzai  return no respose head");
                    return ; 
                }
                switchAntennaAsync(mValueHeadSetPlug);

                // Avoid Service is killed,and receive headset plug in
                // broadcast again
                if (!mIsServiceInited) {
                    Log.i(TAG, "onReceive, mIsServiceInited is false");
                    return;
                }
                /*
                 * If ear phone insert and activity is
                 * foreground. power up FM automatic
                 */
                Log.i(TAG, "FmServiceBroadcastReceiver, mValueHeadSetPlug: "
                        + mValueHeadSetPlug + "isActivityForeground:" + isActivityForeground());
                if ((0 == mValueHeadSetPlug) && isActivityForeground()) {
                    powerUpAsync(FmUtils.computeFrequency(mCurrentStation));
                } 
                else if(1 == mValueHeadSetPlug&&!isActivityForeground()){
                    //yao
                    Log.i(TAG, "exitFm2");
                    mFmServiceHandler.removeCallbacksAndMessages(null);
                    exitFm();
                    stopSelf();

                }
                else if (1 == mValueHeadSetPlug) {
                    mFmServiceHandler.removeMessages(FmListener.MSGID_SCAN_FINISHED);
                    mFmServiceHandler.removeMessages(FmListener.MSGID_SEEK_FINISHED);
                    mFmServiceHandler.removeMessages(FmListener.MSGID_TUNE_FINISHED);
                    mFmServiceHandler.removeMessages(
                            FmListener.MSGID_POWERDOWN_FINISHED);
                    mFmServiceHandler.removeMessages(
                            FmListener.MSGID_POWERUP_FINISHED);
                    focusChanged(AudioManager.AUDIOFOCUS_LOSS);

                    // Need check to switch to earphone mode for audio will
                    // change to AudioSystem.FORCE_NONE
                    setForceUse(false);

                    // Notify UI change to earphone mode, false means not speaker mode
                    Bundle bundle = new Bundle(2);
                    bundle.putInt(FmListener.CALLBACK_FLAG,
                            FmListener.LISTEN_SPEAKER_MODE_CHANGED);
                    bundle.putBoolean(FmListener.KEY_IS_SPEAKER_MODE, false);
                    notifyActivityStateChanged(bundle);
                }
            }
        }
    }

    /**
     * Handle sdcard mount/unmount event. 1. Update the sdcard state map 2. If
     * the recording sdcard is unmounted, need to stop and notify
     */
    private class SdcardListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If eject record sdcard, should set this false to not record.
            updateSdcardStateMap(intent);

            if (mFmRecorder == null) {
                Log.i(TAG, "SdcardListener.onReceive, mFmRecorder is null");
                return;
            }

            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_EJECT.equals(action) ||
                    Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                // If not unmount recording sd card, do nothing;
                if (isRecordingCardUnmount(intent)) {
                    if (mFmRecorder.getState() == FmRecorder.STATE_RECORDING) {
                        onRecorderError(FmRecorder.ERROR_SDCARD_NOT_PRESENT);
                        mFmRecorder.discardRecording();
                    } else {
                        Bundle bundle = new Bundle(2);
                        bundle.putInt(FmListener.CALLBACK_FLAG,
                                FmListener.LISTEN_RECORDSTATE_CHANGED);
                        bundle.putInt(FmListener.KEY_RECORDING_STATE,
                                FmRecorder.STATE_IDLE);
                        notifyActivityStateChanged(bundle);
                    }
                }
                return;
            }
        }
    }

    //Gionee <bug> <jiaoyuan> <2013-09-09> add for CR00889510 begin
    public void showToast(CharSequence text) {
        //Gionee <bug> <lichao> <2014-9-30> modify for CR01387187 begin
        mToast = Toast.makeText(FmService.this, text, Toast.LENGTH_LONG);
        //Gionee <bug> <lichao> <2014-9-30> modify for CR01387187 end
        mToast.setText(text);
        mToast.show();
        Log.i(TAG, "showToast: toast = " + text);
    };
    //Gionee <bug> <jiaoyuan> <2013-09-09> add for CR00889510 end

    /**
     * whether antenna available
     * @return true, antenna available; false, antenna not available
     */
    public boolean isAntennaAvailable() {
        return mAudioManager.isWiredHeadsetOn();
    }

    public void setForceUse(boolean isSpeaker) {
        Log.i(TAG, "setForceUse: isSpeaker = " + isSpeaker);
        mForcedUseForMedia = isSpeaker ? AudioSystem.FORCE_SPEAKER : AudioSystem.FORCE_NONE;
        AudioSystem.setForceUse(FOR_PROPRIETARY, mForcedUseForMedia);
        mIsSpeakerUsed = isSpeaker;
    }

    /**
     * Set FM audio from speaker or not
     * @param isSpeaker true if set FM audio from speaker
     */
    public void setSpeakerPhoneOn(boolean isSpeaker) {
        Log.i(TAG, "setSpeakerPhoneOn " + isSpeaker);
        setForceUse(isSpeaker);
    }

    public int getForceUse() {
        return AudioSystem.getForceUse(FOR_PROPRIETARY);
    }

    public void notifySpeakerModeChange() {
        Bundle bundle = new Bundle(2);
        bundle.putInt(FmListener.CALLBACK_FLAG,
                FmListener.MSGID_BT_STATE_CHANGED);
        bundle.putBoolean(FmListener.KEY_BT_STATE, mPrevBtHeadsetState);
        notifyActivityStateChanged(bundle);
    }

    /**
     * Check if BT headset is connected
     * @return true if current is playing with BT headset
     */
    public boolean isBluetoothHeadsetInUse() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        int a2dpState = btAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        boolean ret = BluetoothProfile.STATE_CONNECTED == a2dpState
                || BluetoothProfile.STATE_CONNECTING == a2dpState;
        Log.i(TAG, "isBluetoothHeadsetInUse " + ret);
        return ret;
    }

    public synchronized void startRender() {
        Log.i(TAG, "startRender " + AudioSystem.getForceUse(FOR_PROPRIETARY));
        //20161214阿米哥15570 start
        //setForceUse(false);
        //20161214阿米哥15570 end
        
        // need to create new audio record and audio play back track,
        // because input/output device may be changed.
        if (mAudioRecord != null && mAudioRecord.getRecordingState()
                == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        if (mAudioTrack != null) {
            stopAudioTrack();
        }

        if (initAudioRecordSink()) {
            mIsRender = true;
            synchronized (mRenderLock) {
                Log.i(TAG, "startRender: notifying for mRenderLock");
                mRenderLock.notify();
            }
        } else {
            Log.i(TAG, "initAudioRecordSink: fail");
        }
    }

    public synchronized void stopRender() {
        Log.i(TAG, "stopRender");
        synchronized (mRenderingLock) {
            Log.i(TAG, "stopRender_processing, mIsRender = " + isRender());
            boolean localRender = isRender();
            mIsRender = false;
            if (localRender) {
                try {
                    long wait = 200;
                    Log.i(TAG, "stopRender: waiting for mRenderingLock");
                    mRenderingLock.wait(wait);
                } catch (InterruptedException e) {
                    Log.i(TAG, "stopRender, thread is interrupted");
                }
            }
        }
    }

    public synchronized void createRenderThread() {
        Log.i(TAG, "createRenderThread");
        if (mRenderThread == null) {
            mRenderThread = new RenderThread();
            mRenderThread.start();
        }
    }

    public synchronized void exitRenderThread() {
        Log.i(TAG, "exitRenderThread");
        stopRender();
        mRenderThread.interrupt();
        mRenderThread = null;
    }

    public boolean isRendering() {
        return mIsRender;
    }

    public void startAudioTrack() {
        Log.i(TAG, "startAudioTrack, mAudioTrack = " + mAudioTrack);
        if (mAudioTrack == null) {
            return;
        }        
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
            ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
            mAudioManager.listAudioPatches(patches);
            //Gionee add for CR01646963 begin
            try{
                Log.d(TAG,"startAudioTrack mAudioTrack.play begin");
                mAudioTrack.play();
                Log.d(TAG,"startAudioTrack mAudioTrack.play end");
            }
            catch(IllegalStateException e){
                Log.e(TAG, "mAudioTrack.play()  error! ");
                e.printStackTrace();
            }
            catch (NullPointerException e) {
                Log.i(TAG, "startAudioTrack, NullPointerException");
                e.printStackTrace();
            }
            catch (Exception e) {
                Log.i(TAG, "startAudioTrack, Exception");
                e.printStackTrace();
            }
        }
        //gionee add for  CR01646963 begin

    }

    public void stopAudioTrack() {
        Log.i(TAG, "stopAudioTrack, mAudioTrack = " + mAudioTrack);
        if (mAudioTrack == null) {
            return;
        }
        try {
            if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;
            }
        } catch (IllegalStateException e) {
            Log.i(TAG, "stopAudioTrack, IllegalStateException");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.i(TAG, "stopAudioTrack, NullPointerException");
            e.printStackTrace();
        }
    }

    class RenderThread extends Thread {
        public int mCurrentFrame = 0;
        public boolean isAudioFrameNeedIgnore() {
            return mCurrentFrame < AUDIO_FRAMES_TO_IGNORE_COUNT;
        }

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[RECORD_BUF_SIZE];
                Log.i(TAG, "RenderThread, interrupted = " + Thread.interrupted());
                while (!Thread.interrupted()) {
                    Log.i(TAG, "RenderThread: run, isRender = " + isRender());
                    if (isRender() && mAudioRecord != null && mAudioTrack != null) {
                        // Speaker mode or BT a2dp mode will come here and keep reading and writing.
                        // If we want FM sound output from speaker or BT a2dp, we must record data
                        // to AudioRecrd and write data to AudioTrack.
                        try {
                            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED
                                    && mAudioRecord.getRecordingState() ==
                                    AudioRecord.RECORDSTATE_STOPPED) {
                                mAudioRecord.startRecording();
                            }
                            
                            if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED
                                    && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
                                Log.d(TAG," RenderThread mAudioTrack.play begin ");
                                mAudioTrack.play();
                                Log.d(TAG," RenderThread mAudioTrack.play end ");
                            }
                            int size = mAudioRecord.read(buffer, 0, RECORD_BUF_SIZE);
                            // check whether need to ignore first 3 frames audio data from AudioRecord
                            // to avoid pop noise.
                            if (isAudioFrameNeedIgnore()) {
                                mCurrentFrame += 1;
                                synchronized (mRenderingLock) {
                                    Log.i(TAG, "RenderThread: notifying for mRenderingLock");
                                    mRenderingLock.notify();
                                }
                                continue ;
                            }
                            if (size <= 0) {
                                Log.i(TAG, "RenderThread read data from AudioRecord "
                                        + "error size: " + size);
                                synchronized (mRenderingLock) {
                                    Log.i(TAG, "RenderThread: notifying for mRenderingLock");
                                    mRenderingLock.notify();
                                }
                                continue;
                            }
                            byte[] tmpBuf = new byte[size];
                            System.arraycopy(buffer, 0, tmpBuf, 0, size);
                            // Check again to avoid noises, because mIsRender may be changed
                            // while AudioRecord is reading.
                            if (isRender()) {
                                mAudioTrack.write(tmpBuf, 0, tmpBuf.length);
                            }
                            synchronized (mRenderingLock) {
                               // Log.i(TAG, "RenderThread: notifying for mRenderingLock");
                                mRenderingLock.notify();
                            }
                        } catch (IllegalStateException e) {
                            Log.i(TAG,"fm  oncreat IllegalStateException");
                            initAudioRecordSink();
                        }catch (Exception e) {
                            Log.i(TAG,"fm  oncreat Exception  = " + e.toString());
                            e.printStackTrace();
                        }
                    } else {
                        // Earphone mode will come here and wait.
                        mCurrentFrame = 0;
                        try {
                            if (mAudioTrack != null &&
                                    mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                                mAudioTrack.stop();
                            }

                            if (mAudioRecord != null &&
                                    mAudioRecord.getRecordingState() ==
                                    AudioRecord.RECORDSTATE_RECORDING) {
                                mAudioRecord.stop();
                            }
                        } catch (IllegalStateException e) {
                            Log.i(TAG, "RenderThread.run, IllegalStateException");
                        } finally {
                            synchronized (mRenderLock) {
                                Log.i(TAG, "RenderThread: waiting for mRenderLock");
                                mRenderLock.wait();
                            }
                        }
                    }
                } // while
            } catch (InterruptedException e) {
                Log.i(TAG, "RenderThread.run, thread is interrupted, need exit thread");
            } catch (NullPointerException e) {
                Log.i(TAG, "RenderThread.run , NullPointerException");
                e.printStackTrace();
            } finally {
                if (mAudioRecord != null &&
                        (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)) {
                    mAudioRecord.stop();
                }

                if (mAudioTrack != null &&
                        (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)) {
                    mAudioTrack.stop();
                }
            }
        }
    }

    // A2dp or speaker mode should render
    public boolean isRender() {
        Log.i(TAG,"mIsRender = "+mIsRender+"  mIsAudioFocusHeld = "+mIsAudioFocusHeld+"  mPowerStatus == POWER_UP = " + (mPowerStatus == POWER_UP));
        return (mIsRender && (mPowerStatus == POWER_UP) && mIsAudioFocusHeld);
    }

    public boolean isSpeakerPhoneOn() {
        return (mForcedUseForMedia == AudioSystem.FORCE_SPEAKER);
    }

    /**
     * open FM device, should be call before power up
     *
     * @return true if FM device open, false FM device not open
     */
    public boolean openDevice() {
        Log.i(TAG, "openDevice");
        if (!mIsDeviceOpen) {
            mIsDeviceOpen = FmNative.openDev();
        }
        return mIsDeviceOpen;
    }

    /**
     * close FM device
     *
     * @return true if close FM device success, false close FM device failed
     */
    public boolean closeDevice() {
        Log.i(TAG, "closeDevice");
        boolean isDeviceClose = false;
        if (mIsDeviceOpen) {
            isDeviceClose = FmNative.closeDev();
            mIsDeviceOpen = !isDeviceClose;
        }
        
        releaseWakeLock();
        // quit looper
        //阿米哥 88498
//        if(mFmServiceHandler.getLooper() != null) {
//            mFmServiceHandler.getLooper().quit();
//        }
        synchronized (mAidlCloseDeviceLock) {
            Log.i(TAG, "closeDevice()  (mAidlCloseDeviceLock)");
            mAidlCloseDeviceLock.notifyAll();
        }
        Log.i(TAG, "closeDevice()  return");
        return isDeviceClose;
    }

    /**
     * get FM device opened or not
     *
     * @return true FM device opened, false FM device closed
     */
    public boolean isDeviceOpen() {
        Log.i(TAG, "isDeviceOpen" + mIsDeviceOpen);
        return mIsDeviceOpen;
    }

    /**
     * power up FM, and make FM voice output from earphone
     *
     * @param frequency
     */
    public void powerUpAsync(float frequency) {
        Log.i(TAG, "powerUpAsync, frequency = " + frequency);
        final int bundleSize = 1;
        mFmServiceHandler.removeMessages(FmListener.MSGID_POWERUP_FINISHED);
        mFmServiceHandler.removeMessages(FmListener.MSGID_POWERDOWN_FINISHED);
        Bundle bundle = new Bundle(bundleSize);
        bundle.putFloat(FM_FREQUENCY, frequency);
        Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_POWERUP_FINISHED);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    //Gionee fengyao
    public boolean powerUp(float frequency) {
        Log.i(TAG, "powerUp, frequency = " + frequency);

        //--------origin here----
        if (mPowerStatus == POWER_UP) {
            Log.i(TAG, "powerUp, mPowerStatus == POWER_UP return");
            return true;
        }
        acquireWakeLock();
        if (!requestAudioFocus()) {
            // activity used for update powerdown menu
            mPowerStatus = POWER_DOWN;
            mIsPlaying192KHz = false;
            mPausedByTransientLossOfFocus = true;
            return false;
        }
        //----------remove here ---
        // if audio patch is null, need recreate audio patch.
        initAudioRecordSink();
        createAudioPatch();
        //-------------
        mPowerStatus = DURING_POWER_UP;

        // if device open fail when chip reset, it need open device again before
        // power up
        if (!mIsDeviceOpen) {
            openDevice();
        }

        if (!FmNative.powerUp(frequency)) {
            mPowerStatus = POWER_DOWN;
            mIsPlaying192KHz = false;
            releaseWakeLock();
            return false;
        }
        mPowerStatus = POWER_UP;

        // for 192KHz mp3 play, need use render whatever
        // audio is output by earphone or speaker
        if (!mIsRender && mIsPlaying192KHz) {
            releaseAudioPatch();
//            startRender();
        }
        //20170321 jiangxiao 来电fm无声问题
        startRender();
        // need mute after power up
        setMute(true);

        return (mPowerStatus == POWER_UP);
    }

    public boolean playFrequency(float frequency) {
        Log.i(TAG, "playFrequency, frequency = " + frequency);
        mCurrentStation = FmUtils.computeStation(frequency);
        FmStation.setCurrentStation(mContext, mCurrentStation);
        // Add notification to the title bar.
        updatePlayingNotification();

        // Start the RDS thread if RDS is supported.
        if (isRdsSupported()) {
            startRdsThread();
        }

        acquireWakeLock();
        /* if (mIsSpeakerUsed != isSpeakerPhoneOn()) {
            setForceUse(mIsSpeakerUsed);
        } */
        if (mRecordState != FmRecorder.STATE_PLAYBACK) {
            enableFmAudio(true);
        }

        setRds(true);
        setMute(false);

        return (mPowerStatus == POWER_UP);
    }

    /**
     * power down FM
     */
    public void powerDownAsync() {
        Log.i(TAG, "powerDownAsync");
        // if power down Fm, should remove message first.
        // not remove all messages, because such as recorder message need
        // to execute after or before power down
        mFmServiceHandler.removeMessages(FmListener.MSGID_SCAN_FINISHED);
        mFmServiceHandler.removeMessages(FmListener.MSGID_SEEK_FINISHED);
        mFmServiceHandler.removeMessages(FmListener.MSGID_TUNE_FINISHED);
        mFmServiceHandler.removeMessages(FmListener.MSGID_POWERDOWN_FINISHED);
        mFmServiceHandler.removeMessages(FmListener.MSGID_POWERUP_FINISHED);
        mFmServiceHandler.sendEmptyMessage(FmListener.MSGID_POWERDOWN_FINISHED);
    }

    /**
     * Power down FM
     *
     * @return true if power down success
     */
    public boolean powerDown() {
        Log.i(TAG, "powerDown");
        if (mPowerStatus == POWER_DOWN) {
            mIsPowerDown = true;
            synchronized (mAidlPowerdownLock) {
                Log.i(TAG, " aidl PowerDown synchronized (mAidlPowerdownLock) ");
                mAidlPowerdownLock.notifyAll();
            }
            return true;
        }

        setMute(true);
        setRds(false);
        enableFmAudio(false);

        if (!FmNative.powerDown(0)) {

            if (isRdsSupported()) {
                stopRdsThread();
            }

            if (!FmUtils.isFmSuspendSupport()) {
                releaseWakeLock();
            }

            // Remove the notification in the title bar.
            removeNotification();
            return false;
        }
        // activity used for update powerdown menu
        mPowerStatus = POWER_DOWN;
        if (isRdsSupported()) {
            stopRdsThread();
        }

        if (!FmUtils.isFmSuspendSupport()) {
            releaseWakeLock();
        }
        mIsPlaying192KHz = false;

        // Remove the notification in the title bar.
        removeNotification();
        mIsPowerDown = true;
        synchronized (mAidlPowerdownLock) {
            Log.i(TAG, " aidl PowerDown synchronized (mAidlPowerdownLock) ");
            mAidlPowerdownLock.notifyAll();
        }

        return true;
    }

    /**
     * 释放WakeLock
     */
    public void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }
    
    public void acquireWakeLock() {
        boolean supportSuspend = FmUtils.isFmSuspendSupport();
        Log.d(TAG, "supportSuspend supportSuspend=" + supportSuspend);
        if (!supportSuspend) {
            if (mWakeLock != null && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            }
        }
    }

    public int getPowerStatus() {
        return mPowerStatus;
    }

    //Gionee add for ximalaya begin
    public boolean isPowerUp(){
        Log.i(TAG, "addlog mPowerStatus is "+mPowerStatus);
        if(mPowerStatus == POWER_UP){
            return true;
        }
        return false;
    }
    //Gionee add for ximalaya end

    /**
     * Tune to a station
     *
     * @param frequency The frequency to tune
     *
     * @return true, success; false, fail.
     */
    public void tuneStationAsync(float frequency) {
        Log.i(TAG, "tuneStationAsync, frequency = " + frequency);
        mFmServiceHandler.removeMessages(FmListener.MSGID_TUNE_FINISHED);
        final int bundleSize = 1;
        Bundle bundle = new Bundle(bundleSize);
        bundle.putFloat(FM_FREQUENCY, frequency);
        Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_TUNE_FINISHED);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    public boolean tuneStation(float frequency) {
        Log.i(TAG, "tuneStation, frequency = " + frequency);
        // clear ps and rt when switch to new station,
        // so that ps and rt can refresh to UI.
        mPsString = "";
        mRtTextString = "";
        if (mPowerStatus == POWER_UP) {
            setRds(false);
            boolean bRet = FmNative.tune(frequency);
            if (bRet) {
                setRds(true);
                mCurrentStation = FmUtils.computeStation(frequency);
                FmStation.setCurrentStation(mContext, mCurrentStation);
                // update notification on main thread
                Handler mainHandler = new Handler(getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updatePlayingNotification();
                    }
                });
            }
            setMute(false);
            return bRet;
        }

        // if earphone is not insert, not power up
        if (!isAntennaAvailable()) {
            return false;
        }

        // if not power up yet, should powerup first
        boolean tune = false;

        if (powerUp(frequency)) {
            tune = playFrequency(frequency);
        }

        return tune;
    }

    /**
     * Seek station according frequency and direction
     *
     * @param frequency start frequency(100KHZ, 87.5)
     * @param isUp direction(true, next station; false, previous station)
     *
     * @return the frequency after seek
     */
    public void seekStationAsync(float frequency, boolean isUp) {
        Log.i(TAG, "seekStationAsync, frequency = " + frequency + ", isUp = " + isUp);
        mFmServiceHandler.removeMessages(FmListener.MSGID_SEEK_FINISHED);
        final int bundleSize = 2;
        Bundle bundle = new Bundle(bundleSize);
        bundle.putFloat(FM_FREQUENCY, frequency);
        bundle.putBoolean(OPTION, isUp);
        Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_SEEK_FINISHED);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    public float seekStation(float frequency, boolean isUp) {
        Log.i(TAG, "seekStation, frequency = " + frequency + ", isUp = " + isUp);
        if (mPowerStatus != POWER_UP) {
            return -1;
        }

        setRds(false);
        mIsNativeSeeking = true;
        float fRet = FmNative.seek(frequency, isUp);
        mIsNativeSeeking = false;
        // make mIsStopScanCalled false, avoid stop scan make this true,
        // when start scan, it will return null.
        mIsStopScanCalled = false;
        return fRet;
    }

    /**
     * Scan stations
     */
    public void startScanAsync() {
        Log.i(TAG, "startScanAsync");
        mFmServiceHandler.removeMessages(FmListener.MSGID_SCAN_FINISHED);
        mFmServiceHandler.sendEmptyMessage(FmListener.MSGID_SCAN_FINISHED);
    }

    public int[] startScan() {
        Log.i(TAG, "startScan");
        int[] stations = null;

        setRds(false);
        setMute(true);
        short[] stationsInShort = null;
        if (!mIsStopScanCalled) {
            mIsNativeScanning = true;
            stationsInShort = FmNative.autoScan();
            mIsNativeScanning = false;
        }

        setRds(true);
        if (mIsStopScanCalled) {
            // Received a message to power down FM, or interrupted by a phone
            // call. Do not return any stations. stationsInShort = null;
            // if cancel scan, return invalid station -100

            //20170110 jingcl modify for 48333 start
//            stationsInShort = new short[] {
//                    -100
//            };
            //20170110 jingcl modify for 48333 end

            mIsStopScanCalled = false;
        }

        if (null != stationsInShort) {
            int size = stationsInShort.length;
            stations = new int[size];
            for (int i = 0; i < size; i++) {
                stations[i] = stationsInShort[i];
            }
        }
        return stations;
    }

    /**
     * Check FM Radio is in scan progress or not
     *
     * @return if in scan progress return true, otherwise return false.
     */
    public boolean isScanning() {
        return mIsScanning;
    }

    /**
     * Stop scan progress
     *
     * @return true if can stop scan, otherwise return false.
     */
    public boolean stopScan() {
        Log.i(TAG, "stopScan");
        if (mPowerStatus != POWER_UP) {
            return false;
        }

        boolean bRet = false;
        mFmServiceHandler.removeMessages(FmListener.MSGID_SCAN_FINISHED);
        mFmServiceHandler.removeMessages(FmListener.MSGID_SEEK_FINISHED);
        if (mIsNativeScanning || mIsNativeSeeking) {
            mIsStopScanCalled = true;
            bRet = FmNative.stopScan();
        }
        return bRet;
    }

    /**
     * Check FM is in seek progress or not
     *
     * @return true if in seek progress, otherwise return false.
     */
    public boolean isSeeking() {
        return mIsNativeSeeking;
    }

    /**
     * Set RDS
     *
     * @param on true, enable RDS; false, disable RDS.
     */
    public void setRdsAsync(boolean on) {
        Log.i(TAG, "setRdsAsync, on = " + on);
        final int bundleSize = 1;
        mFmServiceHandler.removeMessages(FmListener.MSGID_SET_RDS_FINISHED);
        Bundle bundle = new Bundle(bundleSize);
        bundle.putBoolean(OPTION, on);
        Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_SET_RDS_FINISHED);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    public int setRds(boolean on) {
        Log.i(TAG, "setRds, on = " + on);
        if (mPowerStatus != POWER_UP) {
            return -1;
        }
        int ret = -1;
        if (isRdsSupported()) {
            ret = FmNative.setRds(on);
        }
        return ret;
    }

    /**
     * Get PS information
     *
     * @return PS information
     */
    public String getPs() {
        return mPsString;
    }

    /**
     * Get RT information
     *
     * @return RT information
     */
    public String getRtText() {
        return mRtTextString;
    }

    /**
     * Get AF frequency
     *
     * @return AF frequency
     */
    public void activeAfAsync() {
        mFmServiceHandler.removeMessages(FmListener.MSGID_ACTIVE_AF_FINISHED);
        mFmServiceHandler.sendEmptyMessage(FmListener.MSGID_ACTIVE_AF_FINISHED);
    }

    public int activeAf() {
        if (mPowerStatus != POWER_UP) {
            Log.i(TAG, "activeAf, FM is not powered up");
            return -1;
        }

        int frequency = FmNative.activeAf();
        return frequency;
    }

    /**
     * Mute or unmute FM voice
     *
     * @param mute true for mute, false for unmute
     *
     * @return (true, success; false, failed)
     */
    public void setMuteAsync(boolean mute) {
        Log.i(TAG, "setMuteAsync, mute = " + mute);
        mFmServiceHandler.removeMessages(FmListener.MSGID_SET_MUTE_FINISHED);
        final int bundleSize = 1;
        Bundle bundle = new Bundle(bundleSize);
        bundle.putBoolean(OPTION, mute);
        Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_SET_MUTE_FINISHED);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    /**
     * Mute or unmute FM voice
     *
     * @param mute true for mute, false for unmute
     *
     * @return (1, success; other, failed)
     */
    public int setMute(boolean mute) {
        Log.i(TAG, "setMute, mute = " + mute);
        if (mPowerStatus != POWER_UP) {
            Log.w(TAG, "setMute, FM is not powered up");
            return -1;
        }
        int iRet = FmNative.setMute(mute);
        mIsMuted = mute;
        return iRet;
    }

    /**
     * Check the latest status is mute or not
     *
     * @return (true, mute; false, unmute)
     */
    public boolean isMuted() {
        return mIsMuted;
    }

    /**
     * Check whether RDS is support in driver
     *
     * @return (true, support; false, not support)
     */
    public boolean isRdsSupported() {
        boolean isRdsSupported = (FmNative.isRdsSupport() == 1);
        return isRdsSupported;
    }

    /**
     * Check whether speaker used or not
     *
     * @return true if use speaker, otherwise return false
     */
    public boolean isSpeakerUsed() {
        return mIsSpeakerUsed;
    }

    /**
     * Initial service and current station
     *
     * @param iCurrentStation current station frequency
     */
    public void initService(int iCurrentStation) {
        mIsServiceInited = true;
        mCurrentStation = iCurrentStation;
    }

    /**
     * Check service is initialed or not
     *
     * @return true if initialed, otherwise return false
     */
    public boolean isServiceInited() {
        return mIsServiceInited;
    }

    /**
     * Get FM service current station frequency
     *
     * @return Current station frequency
     */
    public int getFrequency() {
        return mCurrentStation;
    }

    /**
     * Set FM service station frequency
     *
     * @param station Current station
     */
    public void setFrequency(int station) {
        mCurrentStation = station;
    }

    /**
     * resume FM audio
     */
    public void resumeFmAudio() {
        Log.i(TAG, "resumeFmAudio, held = " + mIsAudioFocusHeld + "power = " + mPowerStatus);
        // If not check mIsAudioFocusHeld && power up, when scan canceled,
        // this will be resume first, then execute power down. it will cause
        // nosise.
        if (mIsAudioFocusHeld && (mPowerStatus == POWER_UP)) {
            enableFmAudio(true);
        }
    }

    /**
     * Switch antenna There are two types of antenna(long and short) If long
     * antenna(most is this type), must plug in earphone as antenna to receive
     * FM. If short antenna, means there is a short antenna if phone already,
     * can receive FM without earphone.
     *
     * @param antenna antenna (0, long antenna, 1 short antenna)
     *
     * @return (0, success; 1 failed; 2 not support)
     */
    public void switchAntennaAsync(int antenna) {
        Log.i(TAG, "switchAntennaAsync, antenna = " + antenna);
        final int bundleSize = 1;
        mFmServiceHandler.removeMessages(FmListener.MSGID_SWITCH_ANTENNA);

        Bundle bundle = new Bundle(bundleSize);
        bundle.putInt(FmListener.SWITCH_ANTENNA_VALUE, antenna);
        Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_SWITCH_ANTENNA);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    /**
     * Need native support whether antenna support interface.
     *
     * @param antenna antenna (0, long antenna, 1 short antenna)
     *
     * @return (0, success; 1 failed; 2 not support)
     */
    public int switchAntenna(int antenna) {
        Log.i(TAG, "switchAntenna, antenna = " + antenna);
        // if fm not powerup, switchAntenna will flag whether has earphone
        int ret = FmNative.switchAntenna(antenna);
        return ret;
    }

    public boolean isSdcardReady(String sdcardPath) {
        if (!mSdcardStateMap.isEmpty()) {
            if (mSdcardStateMap.get(sdcardPath) != null && !mSdcardStateMap.get(sdcardPath)) {
                Log.i(TAG, "isSdcardReady, return false");
                return false;
            }
        }
        return true;
    }

    /**
     * Start recording
     */
    public void startRecordingAsync() {
        Log.i(TAG, "startRecordingAsync");
        mFmServiceHandler.removeMessages(FmListener.MSGID_STARTRECORDING_FINISHED);
        mFmServiceHandler.sendEmptyMessage(FmListener.MSGID_STARTRECORDING_FINISHED);
    }

    public void startRecording() {
        Log.i(TAG, "startRecording");
        sRecordingSdcard = FmUtils.getDefaultStoragePath();
        if (sRecordingSdcard == null || sRecordingSdcard.isEmpty()) {
            Log.i(TAG, "startRecording, may be no sdcard");
            onRecorderError(FmRecorder.ERROR_SDCARD_NOT_PRESENT);
            return;
        }

        if (mFmRecorder == null) {
            mFmRecorder = new FmRecorder();
            mFmRecorder.registerRecorderStateListener(FmService.this);
        }

        if (isSdcardReady(sRecordingSdcard)) {
            mFmRecorder.startRecording(mContext);
        } else {
            onRecorderError(FmRecorder.ERROR_SDCARD_NOT_PRESENT);
        }
    }


    /**
     * stop recording
     */
    public void stopRecordingAsync() {
        Log.i(TAG, "stopRecordingAsync");
        mFmServiceHandler.removeMessages(FmListener.MSGID_STOPRECORDING_FINISHED);
        mFmServiceHandler.sendEmptyMessage(FmListener.MSGID_STOPRECORDING_FINISHED);
    }

    public boolean stopRecording() {
        Log.i(TAG, "stopRecording, mFmRecorder" + mFmRecorder);
        if (mFmRecorder == null) {
            Log.i(TAG, "stopRecording, called without a valid recorder!!");
            return false;
        }
        synchronized (mStopRecordingLock) {
            mFmRecorder.stopRecording();
        }
        return true;
    }

    /**
     * Save recording file according name or discard recording file if name is
     * null
     *
     * @param newName New recording file name
     */
    public void saveRecordingAsync(String newName) {
        Log.i(TAG, "saveRecordingAsync, name = " + newName);
        mFmServiceHandler.removeMessages(FmListener.MSGID_SAVERECORDING_FINISHED);
        final int bundleSize = 1;
        Bundle bundle = new Bundle(bundleSize);
        bundle.putString(RECODING_FILE_NAME, newName);
        Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_SAVERECORDING_FINISHED);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    public void saveRecording(String newName) {
        Log.i(TAG, "saveRecording, name = " + newName);
        if (mFmRecorder != null) {
            if (newName != null) {
                mFmRecorder.saveRecording(FmService.this, newName);
                return;
            }
            mFmRecorder.discardRecording();
        }
    }

    /**
     * Get record time
     *
     * @return Record time
     */
    public long getRecordTime() {
        if (mFmRecorder != null) {
            return mFmRecorder.getRecordTime();
        }
        return 0;
    }

    /**
     * Set recording mode
     *
     * @param isRecording true, enter recoding mode; false, exit recording mode
     */
    public void setRecordingModeAsync(boolean isRecording) {
        Log.i(TAG, "setRecordingModeAsync, isRecording = " + isRecording);
        mFmServiceHandler.removeMessages(FmListener.MSGID_RECORD_MODE_CHANED);
        final int bundleSize = 1;
        Bundle bundle = new Bundle(bundleSize);
        bundle.putBoolean(OPTION, isRecording);
        Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_RECORD_MODE_CHANED);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    public void setRecordingMode(boolean isRecording) {
        Log.i(TAG, "setRecordingMode, isRecording = " + isRecording);
        mIsInRecordingMode = isRecording;
        if (mFmRecorder != null) {
            if (!isRecording) {
                if (mFmRecorder.getState() != FmRecorder.STATE_IDLE) {
                    mFmRecorder.stopRecording();
                }
                resumeFmAudio();
                setMute(false);
                return;
            }
            // reset recorder to unused status
            mFmRecorder.resetRecorder();
        }
    }

    /**
     * Get current recording mode
     *
     * @return if in recording mode return true, otherwise return false;
     */
    public boolean getRecordingMode() {
        return mIsInRecordingMode;
    }

    /**
     * Get record state
     *
     * @return record state
     */
    public int getRecorderState() {
        if (null != mFmRecorder) {
            return mFmRecorder.getState();
        }
        return FmRecorder.STATE_INVALID;
    }

    /**
     * Get recording file name
     *
     * @return recording file name
     */
    public String getRecordingName() {
        if (null != mFmRecorder) {
            return mFmRecorder.getRecordFileName();
        }
        return null;
    }

    //Gionee modify for ximalaya
    public Intent mFMserviceExit; 
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        mFMserviceExit= new Intent();
        mFMserviceExit.setAction(ACTION_FM_SERVER_STOP);
        //Gionee modify for ximalaya end
        mContext = getApplicationContext();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        if (!FmUtils.isFmSuspendSupport()) {
            mWakeLock.setReferenceCounted(false);
        }
        sRecordingSdcard = FmUtils.getDefaultStoragePath();

        registerFmBroadcastReceiver();
        registerSdcardReceiver();
        registerAudioPortUpdateListener();

        if(mCurrentStation == FmUtils.DEFAULT_STATION ){
            mCurrentStation=  FmStation.getCurrentStation(mContext);
        }
        
//        mFmServiceHandler = new FmRadioServiceHandler(Looper.getMainLooper());
        mHandlerThread = new HandlerThread("FmRadioServiceThread");
        mHandlerThread.start();
        mHandlerThreadLooper = mHandlerThread.getLooper();
        mFmServiceHandler = new FmRadioServiceHandler(mHandlerThreadLooper);

        openDevice();
        // set speaker to default status, avoid setting->clear data.
        setForceUse(mIsSpeakerUsed);

        initAudioRecordSink();
        createRenderThread();
        //mIsThrdAPPExist = ThrAppsUtils.isExistOtherFM(getApplicationContext());
        
    }

    public void registerAudioPortUpdateListener() {
        Log.i(TAG, "registerAudioPortUpdateListener, list = " + mAudioPortUpdateListener);
        if (mAudioPortUpdateListener == null) {
            mAudioPortUpdateListener = new FmOnAudioPortUpdateListener();
            mAudioManager.registerAudioPortUpdateListener(mAudioPortUpdateListener);
        }
    }

    public void unregisterAudioPortUpdateListener() {
        Log.i(TAG, "unregisterAudioPortUpdateListener, list = " + mAudioPortUpdateListener);
        if (mAudioPortUpdateListener != null) {
            mAudioManager.unregisterAudioPortUpdateListener(mAudioPortUpdateListener);
            mAudioPortUpdateListener = null;
        }
    }

    // This function may be called in different threads.
    // Need to add "synchronized" to make sure mAudioRecord and mAudioTrack are the newest.
    // Thread 1: onCreate() or startRender()
    // Thread 2: onAudioPatchListUpdate() or startRender()
    public synchronized boolean initAudioRecordSink() {
        releaseAudioRecord();
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.RADIO_TUNER,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, RECORD_BUF_SIZE);
        if (mAudioRecord == null) {
            Log.i(TAG,"mAudioRecord == null");
            return false;
        }
        
        releaseAudiotrack();
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, RECORD_BUF_SIZE, AudioTrack.MODE_STREAM);
        Log.i(TAG, "initAudioRecordSink, AudioRecord.state=" + mAudioRecord.getState()
                + " ,mAudioTrack.state=" + mAudioTrack.getState());

        if (mAudioTrack == null) {
            Log.i(TAG,"mAudioTrack == null");
            releaseAudioRecord();
            return false;
        }
        return true;
    }
    public void releaseAudiotrack() {
        if (mAudioTrack != null) {
            mAudioTrack.release();
        }
    }
    
    public void releaseAudioRecord() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    public synchronized void createAudioPatch() {
        Log.i(TAG, "createAudioPatch, mAudioPatch = " + mAudioPatch);
        if (mIsPlaying192KHz) {
            releaseAudioPatch();
            if (!mIsRender) {
                startRender();
            }
            return;
        }
        if (mAudioPatch == null) {
            ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
            mAudioManager.listAudioPatches(patches);
			startRender();
            if (isPatchMixerToEarphone(patches)) {
                stopRender();
                stopAudioTrack();
                createAudioPatchByEarphone();
            } else if (isPatchMixerToSpeaker(patches)) {
                stopRender();
                stopAudioTrack();
                createAudioPatchBySpeaker();
            } else if (isPatchContainSpeakerAndEarphone(patches)) {
                stopRender();
                stopAudioTrack();
                createAudioPatchBySpeakerAndEarphone();
            } else {
                if (isRender()) {
                    stopRender();
                }
                startRender();
            }
        }
    }

    public synchronized void createAudioPatchByEarphone() {
        Log.i(TAG, "createAudioPatchByEarphone " + mIsPlaying192KHz);
        if (mIsPlaying192KHz) {
            releaseAudioPatch();
            if (!mIsRender) {
                startRender();
            }
            return;
        }
        if (mAudioPatch != null) {
            Log.i(TAG, "createAudioPatch, mAudioPatch is not null, return");
            return;
        }

        /* if (mIsSpeakerUsed) {
            // audio system config has been modified by others,
            // so need update this state.
            mIsSpeakerUsed = false;
            notifySpeakerModeChange();
        } */
        mAudioSource = null;
        mAudioSink = null;
        ArrayList<AudioPort> ports = new ArrayList<AudioPort>();
        mAudioManager.listAudioPorts(ports);
        for (AudioPort port : ports) {
            if (port instanceof AudioDevicePort) {
                int type = ((AudioDevicePort) port).type();
                String name = AudioSystem.getOutputDeviceName(type);
                if (type == AudioSystem.DEVICE_IN_FM_TUNER) {
                    mAudioSource = (AudioDevicePort) port;
                } else if (type == AudioSystem.DEVICE_OUT_WIRED_HEADSET ||
                        type == AudioSystem.DEVICE_OUT_WIRED_HEADPHONE) {
                    mAudioSink = (AudioDevicePort) port;
                }
            }
        }
        if (mAudioSource != null && mAudioSink != null) {
            AudioDevicePortConfig sourceConfig = (AudioDevicePortConfig) mAudioSource
                    .activeConfig();
            AudioDevicePortConfig sinkConfig = (AudioDevicePortConfig) mAudioSink.activeConfig();
            AudioPatch[] audioPatchArray = new AudioPatch[] {null};
            int res = mAudioManager.createAudioPatch(audioPatchArray,
                    new AudioPortConfig[] {sourceConfig},
                    new AudioPortConfig[] {sinkConfig});
            if (res == AudioManager.ERROR_INVALID_OPERATION) {
                mIsPlaying192KHz = true;
            }
            mAudioPatch = audioPatchArray[0];
        }
    }

    public synchronized void createAudioPatchBySpeaker() {
        Log.i(TAG, "createAudioPatchBySpeaker");
        if (mIsPlaying192KHz) {
            releaseAudioPatch();
            if (!mIsRender) {
                startRender();
            }
            return;
        }
        if (mAudioPatch != null) {
            Log.i(TAG, "createAudioPatch, mAudioPatch is not null, return");
            return;
        }

        if (!mIsSpeakerUsed) {
            // audio system config has been modified by others,
            // so need update this state.
            mIsSpeakerUsed = true;
            notifySpeakerModeChange();
        }

        mAudioSource = null;
        mAudioSink = null;
        ArrayList<AudioPort> ports = new ArrayList<AudioPort>();
        mAudioManager.listAudioPorts(ports);
        for (AudioPort port : ports) {
            if (port instanceof AudioDevicePort) {
                int type = ((AudioDevicePort) port).type();
                String name = AudioSystem.getOutputDeviceName(type);
                if (type == AudioSystem.DEVICE_IN_FM_TUNER) {
                    mAudioSource = (AudioDevicePort) port;
                } else if (type == AudioSystem.DEVICE_OUT_SPEAKER) {
                    mAudioSink = (AudioDevicePort) port;
                }
            }
        }
        if (mAudioSource != null && mAudioSink != null) {
            AudioDevicePortConfig sourceConfig = (AudioDevicePortConfig) mAudioSource
                    .activeConfig();
            AudioDevicePortConfig sinkConfig = (AudioDevicePortConfig) mAudioSink.activeConfig();
            AudioPatch[] audioPatchArray = new AudioPatch[] {null};
            int res = mAudioManager.createAudioPatch(audioPatchArray,
                    new AudioPortConfig[] {sourceConfig},
                    new AudioPortConfig[] {sinkConfig});
            if (res == AudioManager.ERROR_INVALID_OPERATION) {
                mIsPlaying192KHz = true;
            }
            mAudioPatch = audioPatchArray[0];
        }
    }

    public synchronized void createAudioPatchBySpeakerAndEarphone() {
        Log.i(TAG, "createAudioPatchBySpeakerAndEarphone");
        if (mIsPlaying192KHz) {
            releaseAudioPatch();
            if (!mIsRender) {
                startRender();
            }
            return;
        }
        if (mAudioPatch != null) {
            Log.i(TAG, "createAudioPatchBySpeakerAndEarphone, mAudioPatch is not null, return");
            return;
        }

        mAudioSource = null;
        AudioDevicePort speakerSink = null;
        AudioDevicePort earphoneSink = null;
        ArrayList<AudioPort> ports = new ArrayList<AudioPort>();
        mAudioManager.listAudioPorts(ports);
        for (AudioPort port : ports) {
            if (port instanceof AudioDevicePort) {
                int type = ((AudioDevicePort) port).type();
                String name = AudioSystem.getOutputDeviceName(type);
                if (type == AudioSystem.DEVICE_IN_FM_TUNER) {
                    mAudioSource = (AudioDevicePort) port;
                } else if (type == AudioSystem.DEVICE_OUT_SPEAKER) {
                    speakerSink = (AudioDevicePort) port;
                } else if (type == AudioSystem.DEVICE_OUT_WIRED_HEADSET ||
                        type == AudioSystem.DEVICE_OUT_WIRED_HEADPHONE) {
                    earphoneSink = (AudioDevicePort) port;
                }
            }
        }
        if (mAudioSource != null && speakerSink != null && earphoneSink != null) {
            AudioDevicePortConfig sourceConfig = (AudioDevicePortConfig) mAudioSource
                    .activeConfig();
            AudioDevicePortConfig speakerSinkConfig = (AudioDevicePortConfig) speakerSink
                    .activeConfig();
            AudioDevicePortConfig earphoneSinkConfig = (AudioDevicePortConfig) earphoneSink
                    .activeConfig();
            AudioPatch[] audioPatchArray = new AudioPatch[] {null};
            int res = mAudioManager.createAudioPatch(audioPatchArray,
                    new AudioPortConfig[] {sourceConfig},
                    new AudioPortConfig[] {speakerSinkConfig, earphoneSinkConfig});
            if (res == AudioManager.ERROR_INVALID_OPERATION) {
                mIsPlaying192KHz = true;
            }
            mAudioPatch = audioPatchArray[0];
        }
    }

    public class FmOnAudioPortUpdateListener implements OnAudioPortUpdateListener {
        /**
         * Callback method called upon audio port list update.
         * @param portList the updated list of audio ports
         */
        @Override
        public void onAudioPortListUpdate(AudioPort[] portList) {
            // Ingore audio port update
        }

        /**
         * Callback method called upon audio patch list update.
         *
         * @param patchList the updated list of audio patches
         */
        @Override
        public void onAudioPatchListUpdate(AudioPatch[] patchList) {
            Log.i(TAG, "onAudioPatchListUpdate: entry");
            if (FmService.this.isBluetoothHeadsetInUse() != mPrevBtHeadsetState) {
                mPrevBtHeadsetState = !mPrevBtHeadsetState;

                // if BT state change from disconnected to connected, set speaker
                // state to false, because audio framework will set force use to none
                if (mPrevBtHeadsetState) {
                    mIsSpeakerUsed = false;
                }
                Bundle bundle = new Bundle(2);
                bundle.putInt(FmListener.CALLBACK_FLAG,
                        FmListener.MSGID_BT_STATE_CHANGED);
                bundle.putBoolean(FmListener.KEY_BT_STATE, mPrevBtHeadsetState);
                notifyActivityStateChanged(bundle);
            }
            if (mPowerStatus != POWER_UP) {
                Log.i(TAG, "onAudioPatchListUpdate, not power up");
                return;
            }

            if (!mIsAudioFocusHeld) {
                Log.i(TAG, "onAudioPatchListUpdate no audio focus");
                return;
            }

            if (mIsPlaying192KHz) {
                Log.i(TAG, "onAudioPatchListUpdate inPlaying192Khz " + mIsRender);
                releaseAudioPatch();
                if (!mIsRender) {
                    startRender();
                }
                return;
            }

            if (mAudioPatch != null) {
                ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
                mAudioManager.listAudioPatches(patches);
                // When BT or WFD is connected, native will remove the patch (mixer -> device).
                // Need to recreate AudioRecord and AudioTrack for this case.
                if (isPatchMixerToDeviceRemoved(patches) || isPatchMixerToBt(patches)) {
                    Log.i(TAG, "onAudioPatchListUpdate reinit for BT or WFD connected");
                    notifySpeakerModeChange();
                    stopRender();
                    startRender();
                    if (mIsRender) {
                        releaseAudioPatch();
                    }
                    return;
                }
                if (isPatchMixerToEarphone(patches)) {
                    stopRender();
                    if (isOutputDeviceChanged(patches)) {
                        Log.i(TAG, "DEBUG outputDeviceChanged: re-create audio patch");
                        releaseAudioPatch();
                        createAudioPatchByEarphone();
                        mIsSpeakerUsed = false;
                        notifySpeakerModeChange();
                    }
                } else if (isPatchMixerToSpeaker(patches)) {
                    stopRender();
                    if (isOutputDeviceChanged(patches)) {
                        Log.i(TAG, "DEBUG outputDeviceChanged: re-create audio patch");
                        releaseAudioPatch();
                        createAudioPatchBySpeaker();
                        mIsSpeakerUsed = true;
                        notifySpeakerModeChange();
                    }
                } else if (isPatchContainSpeakerAndEarphone(patches)) {
                    // TODO add this case to avoid noise when play ringtone(output with speaker
                    // and earphone), native audio need FM create FM->Speaker+Earphone patch to
                    // fixed this noise issue when switch output device.
                    stopRender();
                    try {
                        AudioPortConfig[] currentSinks = mAudioPatch.sinks();
                        if (currentSinks.length == 1) {
                            Log.i(TAG, "DEBUG create fm->speaker+earphone patch to avoid noise");
                            releaseAudioPatch();
                            createAudioPatchBySpeakerAndEarphone();
                        }
                    } catch (NullPointerException e) {
                        Log.i(TAG, "mAudioPatch released.");
                    }
                } else {
                    Log.i(TAG, "set mIsOutputDeviceChanged true as none audiopatch is present");
                    mIsOutputDeviceChanged = true;
                }
            } else if (mIsRender) {
                Log.i(TAG, "onAudioPatchListUpdate2");
                ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
                mAudioManager.listAudioPatches(patches);
                if (isPatchMixerToEarphone(patches)) {
                    stopRender();
                    stopAudioTrack();
                    createAudioPatchByEarphone();
                } else if (isPatchMixerToSpeaker(patches)) {
                    stopRender();
                    stopAudioTrack();
                    createAudioPatchBySpeaker();
                } else if (isPatchMixerToDeviceRemoved(patches)) {
                    Log.i(TAG, "onAudioPatchListUpdate: native removed patches, restart render");
                    stopRender();
                    startRender();
                }
            }
            Log.i(TAG, "onAudioPatchListUpdate: exit");
        }

        /**
         * Callback method called when the mediaserver dies
         */
        @Override
        public void onServiceDied() {
            Log.i(TAG, "onServiceDied()");
            enableFmAudio(false);
        }
    }

    public synchronized void releaseAudioPatch() {
        Log.i(TAG, "releaseAudioPatch, mAudioPatch = " + mAudioPatch);
        if (mAudioPatch != null) {
            mAudioManager.releaseAudioPatch(mAudioPatch);
            mAudioPatch = null;
        }
        mAudioSource = null;
        mAudioSink = null;
    }

    public void registerFmBroadcastReceiver() {
        Log.i(TAG, "registerFmBroadcastReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(SOUND_POWER_DOWN_MSG);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        //Gionee <bug> <lichao> <2015-04-22> merge for CR01463450 begin
        //CR01035176
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction(ACTION_TOFMSERVICE_POWERDOWN);
        //CR01256503, CR00844404 
        mBroadcastReceiver = new FmServiceBroadcastReceiver();
        registerReceiver(mBroadcastReceiver, filter);
    }

    public void unregisterFmBroadcastReceiver() {
        Log.i(TAG, "unregisterFmBroadcastReceiver");
        if (null != mBroadcastReceiver) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        notifyThirdAppServiceKilled();
        if (mIsParametersSet) {
            mIsParametersSet = false;
            Log.i(TAG, "AudioFmPreStop=0");
            mAudioManager.setParameters("AudioFmPreStop=0");
        }
        setMute(true);
        // stop rds first, avoid blocking other native method
        if (isRdsSupported()) {
            stopRdsThread();
        }
        unregisterFmBroadcastReceiver();
        unregisterSdcardListener();
        abandonAudioFocus();
        exitFm();
/*        //阿米哥23544
        if (null != mFmRecorder) {
            synchronized (mStopRecordingLock) {
                int fmState = mFmRecorder.getState();
                if (FmRecorder.STATE_RECORDING == fmState) {
                    mFmRecorder.stopRecording();
                    saveRecording(getRecordingName());
                } else if (getRecordingName() != null) {
                    saveRecording(getRecordingName());
                }
            }
        }*/
        if (null != mFmRecorder) {
            mFmRecorder = null;
        }
        exitRenderThread();
        releaseAudioPatch();
        unregisterAudioPortUpdateListener();
        releaseAudioRecord();
        releaseAudiotrack();
        super.onDestroy();
    }

    /**
     * Exit FMRadio application
     */
    public void exitFm() {
        Log.i(TAG, "exitFm");
        notifyThirdAppAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        mIsAudioFocusHeld = false;
        // Stop FM recorder if it is working
        //阿米哥23544
//        if (null != mFmRecorder) {
//            synchronized (mStopRecordingLock) {
//                int fmState = mFmRecorder.getState();
//                if (FmRecorder.STATE_RECORDING == fmState) {
//                    mFmRecorder.stopRecording();
//                    saveRecording(getRecordingName());
//                } else if (getRecordingName() != null) {
//                    saveRecording(getRecordingName());
//                }
//            }
//        }

        // When exit, we set the audio path back to earphone.
        if (mIsNativeScanning || mIsNativeSeeking) {
            stopScan();
        }
        if(mIsSpeakerUsed) {
            Log.i(TAG, "=========exitFm()/mIsSpeakerUsed = false");
            setForceUse(false);
        }

        mFmServiceHandler.removeCallbacksAndMessages(null);
        mFmServiceHandler.removeMessages(FmListener.MSGID_FM_EXIT);
        mFmServiceHandler.sendEmptyMessage(FmListener.MSGID_FM_EXIT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Change the notification string.
        if (mPowerStatus == POWER_UP) {
            showPlayingNotification();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "fm service  onStartCommand" +"intent  = ");
        int ret = super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            String action = intent.getAction();
            Log.i(TAG, "fm service  onStartCommand" +"intent action  = " + action);
            if (FM_SEEK_PREVIOUS.equals(action)) {
                seekStationAsync(FmUtils.computeFrequency(mCurrentStation), false);
            } else if (FM_SEEK_NEXT.equals(action)) {
                seekStationAsync(FmUtils.computeFrequency(mCurrentStation), true);
            } else if (FM_TURN_OFF.equals(action)) {
                powerDownAsync();
            }
        }
        return START_STICKY;
    }

    /**
     * Start RDS thread to update RDS information
     */
    public void startRdsThread() {
        Log.i(TAG, "startRdsThread");
        mIsRdsThreadExit = false;
        if (null != mRdsThread) {
            return;
        }

        mRdsThread = new Thread() {
            public void run() {
                while (true) {
                    if (mIsRdsThreadExit) {
                        break;
                    }

                    int iRdsEvents = FmNative.readRds();
                    if (iRdsEvents != 0) {
                        Log.i(TAG, "startRdsThread, is rds events: " + iRdsEvents);
                    }

                    if (RDS_EVENT_PROGRAMNAME == (RDS_EVENT_PROGRAMNAME & iRdsEvents)) {
                        byte[] bytePS = FmNative.getPs();
                        if (null != bytePS) {
                            String ps = new String(bytePS).trim();
                            if (!mPsString.equals(ps)) {
                                // update notification on main thread
                                Handler mainHandler = new Handler(getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        updatePlayingNotification();
                                    }
                                });
                            }
                            ContentValues values = null;
                            if (FmStation.isStationExist(mContext, mCurrentStation)) {
                                values = new ContentValues(1);
                                values.put(Station.PROGRAM_SERVICE, ps);
                                FmStation.updateStationToDb(mContext, mCurrentStation, values);
                            } else {
                                values = new ContentValues(2);
                                values.put(Station.FREQUENCY, mCurrentStation);
                                values.put(Station.PROGRAM_SERVICE, ps);
                                FmStation.insertStationToDb(mContext, values);
                            }
                            setPs(ps);
                        }
                    }

                    if (RDS_EVENT_LAST_RADIOTEXT == (RDS_EVENT_LAST_RADIOTEXT & iRdsEvents)) {
                        byte[] byteLRText = FmNative.getLrText();
                        if (null != byteLRText) {
                            String rds = new String(byteLRText).trim();
                            if (!mRtTextString.equals(rds)) {
                                updatePlayingNotification();
                            }
                            setLRText(rds);
                            ContentValues values = null;
                            if (FmStation.isStationExist(mContext, mCurrentStation)) {
                                values = new ContentValues(1);
                                values.put(Station.RADIO_TEXT, rds);
                                FmStation.updateStationToDb(mContext, mCurrentStation, values);
                            } else {
                                values = new ContentValues(2);
                                values.put(Station.FREQUENCY, mCurrentStation);
                                values.put(Station.RADIO_TEXT, rds);
                                FmStation.insertStationToDb(mContext, values);
                            }
                        }
                    }

                    if (RDS_EVENT_AF == (RDS_EVENT_AF & iRdsEvents)) {
                        /*
                         * add for rds AF
                         */
                        if (mIsScanning || mIsSeeking) {
                            Log.i(TAG, "startRdsThread, seek or scan going, no need to tune here");
                        } else if (mPowerStatus == POWER_DOWN) {
                            Log.i(TAG, "startRdsThread, fm is power down, do nothing.");
                        } else {
                            int iFreq = FmNative.activeAf();
                            if (FmUtils.isValidStation(iFreq)) {
                                // if the new frequency is not equal to current
                                // frequency.
                                if (mCurrentStation != iFreq) {
                                    if (!mIsScanning && !mIsSeeking) {
                                        Log.i(TAG, "startRdsThread, seek or scan not going,"
                                                + "need to tune here");
                                        tuneStationAsync(FmUtils.computeFrequency(iFreq));
                                    }
                                }
                            }
                        }
                    }
                    // Do not handle other events.
                    // Sleep 500ms to reduce inquiry frequency
                    try {
                        final int hundredMillisecond = 500;
                        Thread.sleep(hundredMillisecond);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mRdsThread.start();
    }

    /**
     * Stop RDS thread to stop listen station RDS change
     */
    public void stopRdsThread() {
        Log.i(TAG, "stopRdsThread");
        if (null != mRdsThread) {
            // Must call closedev after stopRDSThread.
            mIsRdsThreadExit = true;
            mRdsThread = null;
        }
    }

    /**
     * Set PS information
     *
     * @param ps The ps information
     */
    public void setPs(String ps) {
        if (0 != mPsString.compareTo(ps)) {
            mPsString = ps;
            Bundle bundle = new Bundle(3);
            bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.LISTEN_PS_CHANGED);
            bundle.putString(FmListener.KEY_PS_INFO, mPsString);
            notifyActivityStateChanged(bundle);
        } // else New PS is the same as current
    }

    /**
     * Set RT information
     *
     * @param lrtText The RT information
     */
    public void setLRText(String lrtText) {
        if (0 != mRtTextString.compareTo(lrtText)) {
            mRtTextString = lrtText;
            Bundle bundle = new Bundle(3);
            bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.LISTEN_RT_CHANGED);
            bundle.putString(FmListener.KEY_RT_INFO, mRtTextString);
            notifyActivityStateChanged(bundle);
        } // else New RT is the same as current
    }

    /**
     * Open or close FM Radio audio
     *
     * @param enable true, open FM audio; false, close FM audio;
     */
    public void enableFmAudio(boolean enable) {
        Log.i(TAG, "enableFmAudio: " + enable);
        if (enable) {
            if ((mPowerStatus != POWER_UP) || !mIsAudioFocusHeld) {
                Log.i(TAG, "enableFmAudio, current not available return.mIsAudioFocusHeld:"
                        + mIsAudioFocusHeld);
                return;
            }

            startAudioTrack();
            createAudioPatch();
            if (FmUtils.getIsSpeakerModeOnFocusLost(mContext)) {
                setForceUse(true);
                FmUtils.setIsSpeakerModeOnFocusLost(mContext, false);
                notifySpeakerModeChange();
            }
        } else {
            releaseAudioPatch();
            stopRender();
        }
    }

    // Make sure patches count will not be 0
    public boolean isPatchMixerToBt(ArrayList<AudioPatch> patches) {
        for (AudioPatch patch : patches) {
            AudioPortConfig[] sources = patch.sources();
            AudioPortConfig[] sinks = patch.sinks();
            if (sinks.length > 1) {
                continue;
            }
            AudioPortConfig sourceConfig = sources[0];
            AudioPortConfig sinkConfig = sinks[0];
            AudioPort sourcePort = sourceConfig.port();
            AudioPort sinkPort = sinkConfig.port();
            if (sourcePort instanceof AudioMixPort && sinkPort instanceof AudioDevicePort) {
                int type = ((AudioDevicePort) sinkPort).type();
                if (type == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP ||
                        type == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES ||
                        type == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER ||
                        type == AudioSystem.DEVICE_OUT_REMOTE_SUBMIX) {
                    Log.i(TAG, "isPatchMixerToBt: true");
                    return true;
                }
            }
        }
        Log.i(TAG, "isPatchMixerToBt: false");
        return false;
    }


    // Make sure patches count will not be 0
    public boolean isPatchMixerToEarphone(ArrayList<AudioPatch> patches) {
        int deviceCount = 0;
        int deviceEarphoneCount = 0;

        int minSourcePortId = 0;
        AudioPort sourcePort = null;
        AudioPatch minAudioPatch = null;
        if (patches.isEmpty() == false) {
            minSourcePortId = (patches.get(0).sources()[0]).port().id();
        }
        for (AudioPatch aPatch : patches) {
            AudioPortConfig[] sources = aPatch.sources();
            AudioPortConfig sourceConfig = sources[0];
            sourcePort = sourceConfig.port();

            // find patch with min source port id
            if (sourcePort.id() <= minSourcePortId) {
                minSourcePortId = sourcePort.id();
                minAudioPatch = aPatch;
                Log.i(TAG, "minSourcePortId =" + minSourcePortId);
            }
        }

        //for (AudioPatch patch : patches) {
        if (minAudioPatch != null) {
            AudioPortConfig[] sources = minAudioPatch.sources();
            AudioPortConfig[] sinks = minAudioPatch.sinks();
            if (sinks.length <= 1) {
                AudioPortConfig sourceConfig = sources[0];
                AudioPortConfig sinkConfig = sinks[0];
                sourcePort = sourceConfig.port();
                AudioPort sinkPort = sinkConfig.port();
                if (sourcePort instanceof AudioMixPort && sinkPort instanceof AudioDevicePort) {
                    deviceCount++;
                    int type = ((AudioDevicePort) sinkPort).type();
                    if (type == AudioSystem.DEVICE_OUT_WIRED_HEADSET ||
                            type == AudioSystem.DEVICE_OUT_WIRED_HEADPHONE) {
                        deviceEarphoneCount++;
                    }
                }
            }
        }

        if (deviceEarphoneCount >= 1 && deviceCount == deviceEarphoneCount) {
            Log.i(TAG, "isPatchMixerToEarphone: true");
            return false;
        }

        Log.i(TAG, "isPatchMixerToEarphone: false");
        return false;
    }

    // Make sure patches count will not be 0
    public boolean isPatchMixerToSpeaker(ArrayList<AudioPatch> patches) {
        int deviceCount = 0;
        int deviceEarphoneCount = 0;

        int minSourcePortId = 0;
        AudioPort sourcePort = null;
        AudioPatch minAudioPatch = null;
        if (patches.isEmpty() == false) {
            minSourcePortId = (patches.get(0).sources()[0]).port().id();
        }
        for (AudioPatch aPatch : patches) {
            AudioPortConfig[] sources = aPatch.sources();
            AudioPortConfig sourceConfig = sources[0];
            sourcePort = sourceConfig.port();

            // find patch with min source port id
            if (sourcePort.id() <= minSourcePortId) {
                minSourcePortId = sourcePort.id();
                minAudioPatch = aPatch;
                Log.i(TAG, "minSourcePortId =" + minSourcePortId);
            }
        }

        //for (AudioPatch patch : patches) {
        if (minAudioPatch != null) {
            AudioPortConfig[] sources = minAudioPatch.sources();
            AudioPortConfig[] sinks = minAudioPatch.sinks();
            if (sinks.length <= 1) {
                AudioPortConfig sourceConfig = sources[0];
                AudioPortConfig sinkConfig = sinks[0];
                sourcePort = sourceConfig.port();
                AudioPort sinkPort = sinkConfig.port();
                if (sourcePort instanceof AudioMixPort && sinkPort instanceof AudioDevicePort) {
                    deviceCount++;
                    int type = ((AudioDevicePort) sinkPort).type();
                    if (type == AudioSystem.DEVICE_OUT_SPEAKER) {
                        deviceEarphoneCount++;
                    }
                }
            }
        }

        if (deviceEarphoneCount >= 1 && deviceCount == deviceEarphoneCount) {
            Log.i(TAG, "isPatchMixerToSpeaker: true");
            return false;//true;
        }

        Log.i(TAG, "isPatchMixerToSpeaker: false");
        return false;
    }

    public boolean isPatchContainSpeakerAndEarphone(ArrayList<AudioPatch> patches) {
        boolean hasSpeakerSink = false;
        boolean hasEarphoneSink = false;
        for (AudioPatch patch : patches) {
            AudioPortConfig[] sources = patch.sources();
            AudioPortConfig[] sinks = patch.sinks();
            // only when source port is mix with two sink need do next check,
            // return true only when sink is speaker and earphone
            if (sinks.length != 2 || !(sources[0].port() instanceof AudioMixPort)) {
                continue;
            }
            for (AudioPortConfig sink : sinks) {
                AudioPort sinkPort = sink.port();
                if (sinkPort instanceof AudioDevicePort) {
                    int type = ((AudioDevicePort) sinkPort).type();
                    if (type == AudioSystem.DEVICE_OUT_SPEAKER) {
                        hasSpeakerSink = true;
                    } else if (type == AudioSystem.DEVICE_OUT_WIRED_HEADSET ||
                            type == AudioSystem.DEVICE_OUT_WIRED_HEADPHONE) {
                        hasEarphoneSink = true;
                    }
                }
            }
        }
        Log.i(TAG, "isPatchContainSpeakerAndEarphone: " + (hasSpeakerSink && hasEarphoneSink));
        return false;//hasSpeakerSink && hasEarphoneSink;
    }

    // Check whether the patch (mixer -> device) is removed by native.
    // If no patch (mixer -> device), return true.
    public boolean isPatchMixerToDeviceRemoved(ArrayList<AudioPatch> patches) {
        boolean noMixerToDevice = true;
        for (AudioPatch patch : patches) {
            AudioPortConfig[] sources = patch.sources();
            AudioPortConfig[] sinks = patch.sinks();
            AudioPortConfig sourceConfig = sources[0];
            AudioPortConfig sinkConfig = sinks[0];
            AudioPort sourcePort = sourceConfig.port();
            AudioPort sinkPort = sinkConfig.port();

            if (sourcePort instanceof AudioMixPort && sinkPort instanceof AudioDevicePort) {
                noMixerToDevice = false;
                break;
            }
        }
        Log.i(TAG, "isPatchMixerToDeviceRemoved: " + noMixerToDevice);
        return noMixerToDevice;
    }

    public boolean isOutputDeviceChanged(ArrayList<AudioPatch> patches) {
        boolean ret = true;
        int minSourcePortId;
        AudioPortConfig[] origSources = null;
        AudioPortConfig[] origSinks = null;
        synchronized (this) {
            // need synchronized to avoid NPE of mAudioPatch, which
            // is reassigned to null in releaseAudioPatch().
            if (mAudioPatch == null) {
                Log.i(TAG, "isOutputDeviceChanged, mAudioPatch is null, return");
                return false;
            }
            origSources = mAudioPatch.sources();
            origSinks = mAudioPatch.sinks();
        }
        if (mIsOutputDeviceChanged) {
            Log.i(TAG, "patch mixer again set to some output device");
            Log.i(TAG, "isOutputDeviceChanged: true");
            mIsOutputDeviceChanged = false;
            Log.i(TAG, "set mIsOutputDeviceChanged to false");
            return true;
        }
        AudioPort origSrcPort = origSources[0].port();
        AudioPort origSinkPort = origSinks[0].port();
        AudioPort sourcePort = null;
        AudioPatch minAudioPatch = null;
        Log.i(TAG, "DEBUG " + origSinkPort);

        minSourcePortId = (patches.get(0).sources()[0]).port().id();
        for (AudioPatch aPatch : patches) {
            AudioPortConfig[] sources = aPatch.sources();
            AudioPortConfig sourceConfig = sources[0];
            sourcePort = sourceConfig.port();

            // find patch with min source port id
            if (sourcePort.id() <= minSourcePortId) {
                minSourcePortId = sourcePort.id();
                minAudioPatch = aPatch;
            }
        }
        if (minAudioPatch == null) {
            Log.i(TAG, "DEBUG: minAudioPatch==null");
            return true;
        }
        AudioPortConfig[] sources = minAudioPatch.sources();
        AudioPortConfig sourceConfig = sources[0];
        AudioPortConfig[] sinks = minAudioPatch.sinks();
        AudioPort sinkPort = null;
        sourcePort = sourceConfig.port();

        // compare minAudioPatch with mAudioPatch
        if (sourcePort instanceof AudioMixPort) {
            int sinkOR = 0;
            int origSinkOR = 0;
            int sinksLength = sinks.length;
            if (sinksLength == origSinks.length) {
                for (int i = 0; i < sinksLength; i++) {
                    sinkPort = sinks[i].port();
                    origSinkPort = origSinks[i].port();
                    if (sinkPort instanceof AudioDevicePort &&
                            origSinkPort instanceof AudioDevicePort) {
                        sinkOR |= ((AudioDevicePort) sinkPort).type();
                        origSinkOR |= ((AudioDevicePort) origSinkPort).type();
                    } else {
                        Log.i(TAG, "DEBUG1: sink_id: " + ((AudioDevicePort) sinkPort).type()
                                + " orig_sink_id: " + ((AudioDevicePort) origSinkPort).type());
                        return true;
                    }
                }
                if (sinkOR == origSinkOR) {
                    // patches are equal
                    Log.i(TAG, "isOutputDeviceChanged: false");
                    return false;
                }
            }
            else {
                // sinkPort is not an instance of AudioDevicePort
                Log.i(TAG, "DEBUG2: sink lengths not equal");
                return true;
            }
        }

        Log.i(TAG, "isOutputDeviceChanged: " + ret);
        return ret;
    }

    /**
     * Show notification
     */
    public void showPlayingNotification() {
        Log.i(TAG, "showPlayingNotification");
        /*if (isActivityForeground() || mIsScanning
                || (getRecorderState() == FmRecorder.STATE_RECORDING)) {
            Log.i(TAG, "showPlayingNotification, do not show main notification.");
            return;
        }
        Log.i(TAG, "FmRadioService.showNotification");
        Intent notificationIntent = new Intent();
        notificationIntent.setClassName(getPackageName(), FmMainActivity.class.getName());
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, notificationIntent, 0);
        Notification notification = new Notification(R.drawable.fm_title_icon, null,
                System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        String fmUnit = mContext.getString(R.string.fm_unit);
        String text = FmUtils.formatStation(mCurrentStation) + " " + fmUnit;
        notification.setLatestEventInfo(getApplicationContext(),
                getResources().getString(R.string.app_name), text, pendingIntent);
        Log.i(TAG, "Add notification to the title bar.");
        startForeground(NOTIFICATION_ID, notification);*/
    }

    /**
     * Show notification
     */
    public void showRecordingNotification(Notification notification) {
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * Remove notification
     */
    public void removeNotification() {
        stopForeground(true);
    }

    /**
     * Update notification
     */
    public void updatePlayingNotification() {
        if (mPowerStatus == POWER_UP) {
            showPlayingNotification();
        }
    }

    /**
     * Register sdcard listener for record
     */
    public void registerSdcardReceiver() {
        if (mSdcardListener == null) {
            mSdcardListener = new SdcardListener();
        }
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("file");
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        registerReceiver(mSdcardListener, filter);
    }

    public void unregisterSdcardListener() {
        if (null != mSdcardListener) {
            unregisterReceiver(mSdcardListener);
        }
    }

    public void updateSdcardStateMap(Intent intent) {
        String action = intent.getAction();
        String sdcardPath = null;
        Uri mountPointUri = intent.getData();
        if (mountPointUri != null) {
            sdcardPath = mountPointUri.getPath();
            if (sdcardPath != null) {
                if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
                    mSdcardStateMap.put(sdcardPath, false);
                } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                    mSdcardStateMap.put(sdcardPath, false);
                } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                    mSdcardStateMap.put(sdcardPath, true);
                }
            }
        }
    }

    /**
     * Notify FM recorder state
     *
     * @param state The current FM recorder state
     */
    @Override
    public void onRecorderStateChanged(int state) {
        mRecordState = state;
        Bundle bundle = new Bundle(2);
        bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.LISTEN_RECORDSTATE_CHANGED);
        bundle.putInt(FmListener.KEY_RECORDING_STATE, state);
        notifyActivityStateChanged(bundle);
    }

    /**
     * Notify FM recorder error message
     *
     * @param error The recorder error type
     */
    @Override
    public void onRecorderError(int error) {
        // if media server die, will not enable FM audio, and convert to
        // ERROR_PLAYER_INATERNAL, call back to activity showing toast.
        mRecorderErrorType = error;

        Bundle bundle = new Bundle(2);
        bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.LISTEN_RECORDERROR);
        bundle.putInt(FmListener.KEY_RECORDING_ERROR_TYPE, mRecorderErrorType);
        notifyActivityStateChanged(bundle);
    }

    /**
     * Check and go next(play or show tips) after recorder file play
     * back finish.
     * Two cases:
     * 1. With headset  -> play FM
     * 2. Without headset -> show plug in earphone tips
     */
    public void checkState() {
        if (isHeadSetIn()) {
            // with headset
            if (mPowerStatus == POWER_UP) {
                resumeFmAudio();
                setMute(false);
            } else {
                powerUpAsync(FmUtils.computeFrequency(mCurrentStation));
            }
        } else {
            // without headset need show plug in earphone tips
            switchAntennaAsync(mValueHeadSetPlug);
        }
    }

    /**
     * Check the headset is plug in or plug out
     *
     * @return true for plug in; false for plug out
     */
    public boolean isHeadSetIn() {
        return (0 == mValueHeadSetPlug);
    }

    public void focusChanged(int focusState) {
        mIsAudioFocusHeld = false;
        if (mIsNativeScanning || mIsNativeSeeking) {
            // make stop scan from activity call to service.
            // notifyActivityStateChanged(FMRadioListener.LISTEN_SCAN_CANCELED);
            stopScan();
        }

        // using handler thread to update audio focus state
        updateAudioFocusAync(focusState);
    }

    /**
     * Request audio focus
     *
     * @return true, success; false, fail;
     */
    public boolean requestAudioFocus() {
        Log.i(TAG, "requestAudioFocus");
        if (mIsAudioFocusHeld) {
            return true;
        }

        int audioFocus = mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        mIsAudioFocusHeld = (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioFocus);
        if(AudioManager.AUDIOFOCUS_REQUEST_FAILED  == audioFocus) {
            mFmServiceHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendToastMessgeToMediaplayerHandler(0);
                }
            });
            mIsAudioFocusHeld = false;
            Log.i(TAG, "<< play: request audio focus failed");
        }
        return mIsAudioFocusHeld;
    }

    public void sendToastMessgeToMediaplayerHandler(int StringId){
        sendToastMessgeToMediaplayerHandler(StringId,true,500);
    }

    public void sendToastMessgeToMediaplayerHandler(int StringId,boolean removeOldMsg,long delayMillis){
        Message message = mFmServiceHandler.obtainMessage(0);
        message.arg1 = StringId;
        if(removeOldMsg == true){
            mFmServiceHandler.removeMessages(0);
        }
        if(delayMillis ==0){
            mFmServiceHandler.sendMessage(message);
        }else{
            mFmServiceHandler.sendMessageDelayed(message,delayMillis);
        }
    }

    /**
     * Abandon audio focus
     */
    public void abandonAudioFocus() {
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        mIsAudioFocusHeld = false;
    }

    /**
     * Use to interact with other voice related app
     */
    public final OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
        /**
         * Handle audio focus change ensure message FIFO
         *
         * @param focusChange audio focus change state
         */
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.i(TAG, "=======focusChange = " + focusChange);
            switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                synchronized (this) {
                    if (getForceUse() == AudioSystem.FORCE_SPEAKER && mIsSpeakerUsed) {
                        mIsParametersSet = true;
                        Log.i(TAG, "AudioFmPreStop=1");
                        mAudioManager.setParameters("AudioFmPreStop=1");
                    }
                    setMute(true);
                    //Gionee add for auto shutdown begin
                    Log.i(TAG, "fm auto shutdown for loss focus");
                    //modify for 13822 start
                    //                          showToast(getString(R.string.fm_shutdown_automatically));//add tips
                    //modify for 13822 end
                    // need remove all messages, make power down will be execute
                    mFmServiceHandler.removeCallbacksAndMessages(null);
                    //Gionee 20161012 jingcl add for fm no sound begin
                    mAudioManager.setParameters("AudioFmPreStop=0");
                    //Gionee 20161012 jingcl add for fm no sound end
                    exitFm();
                    stopSelf();

                    //Gionee modify 20160317 fengyao for CR01652558 begin
                    focusChanged(AudioManager.AUDIOFOCUS_LOSS);//Gionee delete
                    //Gionee modify 20160317 fengyao for CR01652558 end
                    //Gionee add for auto shutdown end
                    //Gionee add for ximalaya
                    sendBroadcast(mFMserviceExit);
                    //释放音频焦点 20161024 浏览器视频没有声音
                    mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);

                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                synchronized (this) {
                    if (getForceUse() == AudioSystem.FORCE_SPEAKER && mIsSpeakerUsed) {
                        mIsParametersSet = true;
                        Log.i(TAG, "AudioFmPreStop=1");
                        mAudioManager.setParameters("AudioFmPreStop=1");
                    }
                    setMute(true);
                    focusChanged(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
                    notifyThirdAppAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
                }
                break;

            case AudioManager.AUDIOFOCUS_GAIN:
                synchronized (this) {
                    updateAudioFocusAync(AudioManager.AUDIOFOCUS_GAIN);
                    notifyThirdAppAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                synchronized (this) {
                    updateAudioFocusAync(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);
                    notifyThirdAppAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);
                }
                break;

            default:
                break;
            }
        }
    };

    /**
     * Audio focus changed, will send message to handler thread. synchronized to
     * ensure one message can go in this method.
     *
     * @param focusState AudioManager state
     */
    public synchronized void updateAudioFocusAync(int focusState) {
        Log.i(TAG, "updateAudioFocusAync, focusState = " + focusState);
        if(mHandlerThread.isAlive()) {
            Log.i(TAG, "=======updateAudioFocusAync()/mHandlerThread.isAlive() = true");
            final int bundleSize = 1;
            Bundle bundle = new Bundle(bundleSize);
            bundle.putInt(FmListener.KEY_AUDIOFOCUS_CHANGED, focusState);
            Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_AUDIOFOCUS_CHANGED);
            msg.setData(bundle);
            mFmServiceHandler.sendMessage(msg);
        }else {
            Log.i(TAG, "=======updateAudioFocusAync()/mHandlerThread.isAlive() = false");
            mHandlerThread = new HandlerThread("FmRadioServiceThread");
            mHandlerThread.start();
            mHandlerThreadLooper = mHandlerThread.getLooper();
            mFmServiceHandler = new FmRadioServiceHandler(mHandlerThreadLooper);

            final int bundleSize = 1;
            Bundle bundle = new Bundle(bundleSize);
            bundle.putInt(FmListener.KEY_AUDIOFOCUS_CHANGED, focusState);
            Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_AUDIOFOCUS_CHANGED);
            msg.setData(bundle);
            mFmServiceHandler.sendMessage(msg);
        }
    }

    /**
     * Audio focus changed, update FM focus state.
     *
     * @param focusState AudioManager state
     */
    public void updateAudioFocus(int focusState) {
        Log.i(TAG, "updateAudioFocus: " + focusState);
        switch (focusState) {
        case AudioManager.AUDIOFOCUS_LOSS:
            mPausedByTransientLossOfFocus = false;
            // play back audio will output with music audio
            // May be affect other recorder app, but the flow can not be
            // execute earlier,
            // It should ensure execute after start/stop record.
            if (mFmRecorder != null) {
                int fmState = mFmRecorder.getState();
                // only handle recorder state, not handle playback state
                if (fmState == FmRecorder.STATE_RECORDING) {
                    mFmServiceHandler.removeMessages(
                            FmListener.MSGID_STARTRECORDING_FINISHED);
                    mFmServiceHandler.removeMessages(
                            FmListener.MSGID_STOPRECORDING_FINISHED);
                    stopRecording();
                }
            }
            forceToHeadsetMode();
            if (mIsParametersSet) {
                mIsParametersSet = false;
                Log.i(TAG, "AudioFmPreStop=0");
                mAudioManager.setParameters("AudioFmPreStop=0");
            }
            handlePowerDown();
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            if (mPowerStatus == POWER_UP) {
                mPausedByTransientLossOfFocus = true;
            }
            // play back audio will output with music audio
            // May be affect other recorder app, but the flow can not be
            // execute earlier,
            // It should ensure execute after start/stop record.
            if (mFmRecorder != null) {
                int fmState = mFmRecorder.getState();
                if (fmState == FmRecorder.STATE_RECORDING) {
                    mFmServiceHandler.removeMessages(
                            FmListener.MSGID_STARTRECORDING_FINISHED);
                    mFmServiceHandler.removeMessages(
                            FmListener.MSGID_STOPRECORDING_FINISHED);
                    stopRecording();
                }
            }
            forceToHeadsetMode();
            if (mIsParametersSet) {
                mIsParametersSet = false;
                Log.i(TAG, "AudioFmPreStop=0");
                mAudioManager.setParameters("AudioFmPreStop=0");
            }
            handlePowerDown();
            break;

        case AudioManager.AUDIOFOCUS_GAIN:
            /* if (FmUtils.getIsSpeakerModeOnFocusLost(mContext)) {
                    setForceUse(true);
                    FmUtils.setIsSpeakerModeOnFocusLost(mContext, false);
                } */
            Log.i(TAG, "mPowerStatus is "+mPowerStatus+" mPausedByTransientLossOfFocus is:"+mPausedByTransientLossOfFocus);
            if ((mPowerStatus != POWER_UP) && mPausedByTransientLossOfFocus) {
                final int bundleSize = 1;
                mFmServiceHandler.removeMessages(FmListener.MSGID_POWERUP_FINISHED);
                mFmServiceHandler.removeMessages(FmListener.MSGID_POWERDOWN_FINISHED);
                Bundle bundle = new Bundle(bundleSize);
                bundle.putFloat(FM_FREQUENCY, FmUtils.computeFrequency(mCurrentStation));

                handlePowerUp(bundle);
            }
            setMute(false);
            Log.i(TAG, "audio focus re-gain, force use: " + getForceUse() +
                    " isSpeakerUsed: " + mIsSpeakerUsed);
            if (getForceUse() != AudioSystem.FORCE_SPEAKER && mIsSpeakerUsed) {
                AudioSystem.setForceUse(FOR_PROPRIETARY, getForceUse());
                mIsSpeakerUsed = false;
            } else if (getForceUse() == AudioSystem.FORCE_SPEAKER && !mIsSpeakerUsed) {
                AudioSystem.setForceUse(FOR_PROPRIETARY, getForceUse());
                mIsSpeakerUsed = true;
            }
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            setMute(true);
            break;

        default:
            break;
        }
    }

    public void forceToHeadsetMode() {
        Log.i(TAG, "forceToHeadsetMode");
        if (mIsSpeakerUsed && isHeadSetIn()) {
            AudioSystem.setForceUse(FOR_PROPRIETARY, AudioSystem.FORCE_NONE);
            // save user's option to shared preferences.
            FmUtils.setIsSpeakerModeOnFocusLost(mContext, true);
        }
    }

    /**
     * FM Radio listener record
     */
    public static class Record {
        int mHashCode; // hash code
        FmListener mCallback; // call back
    }

    /**
     * Register FM Radio listener, activity get service state should call this
     * method register FM Radio listener
     *
     * @param callback FM Radio listener
     */
    public void registerFmRadioListener(FmListener callback) {
        synchronized (mRecords) {
            // register callback in AudioProfileService, if the callback is
            // exist, just replace the event.
            Record record = null;
            int hashCode = callback.hashCode();
            final int n = mRecords.size();
            for (int i = 0; i < n; i++) {
                record = mRecords.get(i);
                if (hashCode == record.mHashCode) {
                    return;
                }
            }
            record = new Record();
            record.mHashCode = hashCode;
            record.mCallback = callback;
            mRecords.add(record);
        }
    }

    /**
     * Call back from service to activity
     *
     * @param bundle The message to activity
     */
    public void notifyActivityStateChanged(Bundle bundle) {
        if (!mRecords.isEmpty()) {
            synchronized (mRecords) {
                Iterator<Record> iterator = mRecords.iterator();
                while (iterator.hasNext()) {
                    Record record = (Record) iterator.next();

                    FmListener listener = record.mCallback;

                    if (listener == null) {
                        iterator.remove();
                        return;
                    }

                    listener.onCallBack(bundle);
                }
            }
        } else {
            Log.i(TAG, "notifyActivityStateChanged: " + mRecords.isEmpty());
        }
    }

    /**
     * Call back from service to the current request activity
     * Scan need only notify FmFavoriteActivity if current is FmFavoriteActivity
     *
     * @param bundle The message to activity
     */
    public void notifyCurrentActivityStateChanged(Bundle bundle) {
        if (!mRecords.isEmpty()) {
            Log.i(TAG, "notifyCurrentActivityStateChanged = " + mRecords.size());
            synchronized (mRecords) {
                if (mRecords.size() > 0) {
                    Record record  = mRecords.get(mRecords.size() - 1);
                    FmListener listener = record.mCallback;
                    if (listener == null) {
                        mRecords.remove(record);
                        return;
                    }
                    listener.onCallBack(bundle);
                }
            }
        } else {
            Log.i(TAG, "notifyActivityStateChanged: " + mRecords.isEmpty());
        }
    }

    /**
     * Unregister FM Radio listener
     *
     * @param callback FM Radio listener
     */
    public void unregisterFmRadioListener(FmListener callback) {
        remove(callback.hashCode());
    }

    /**
     * Remove call back according hash code
     *
     * @param hashCode The call back hash code
     */
    public void remove(int hashCode) {
        synchronized (mRecords) {
            Iterator<Record> iterator = mRecords.iterator();
            while (iterator.hasNext()) {
                Record record = (Record) iterator.next();
                if (record.mHashCode == hashCode) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Check recording sd card is unmount
     *
     * @param intent The unmount sd card intent
     *
     * @return true or false indicate whether current recording sd card is
     *         unmount or not
     */
    public boolean isRecordingCardUnmount(Intent intent) {
        String unmountSDCard = intent.getData().toString();
        Log.i(TAG, "unmount sd card file path: " + unmountSDCard);
        return unmountSDCard.equalsIgnoreCase("file://" + sRecordingSdcard) ? true : false;
    }

    public int[] updateStations(int[] stations) {
        Log.i(TAG, "updateStations.firstValidstation:" + Arrays.toString(stations));
        int firstValidstation = mCurrentStation;

        int stationNum = 0;
        if (null != stations) {
            int searchedListSize = stations.length;
            if (mIsDistanceExceed) {
                FmStation.cleanSearchedStations(mContext);
                for (int j = 0; j < searchedListSize; j++) {
                    int freqSearched = stations[j];
                    if (FmUtils.isValidStation(freqSearched) &&
                            !FmStation.isFavoriteStation(mContext, freqSearched)) {
                        FmStation.insertStationToDb(mContext, freqSearched, null);
                    }
                }
            } else {
                // get stations from db
                stationNum = updateDBInLocation(stations);
            }
        }

        Log.i(TAG, "updateStations.firstValidstation:" + firstValidstation +
                ",stationNum:" + stationNum);
        return (new int[] {
                firstValidstation, stationNum
        });
    }

    /**
     * update DB, keep favorite and rds which is searched this time,
     * delete rds from db which is not searched this time.
     * @param stations
     * @return number of valid searched stations
     */
    public int updateDBInLocation(int[] stations) {
        int stationNum = 0;
        int searchedListSize = stations.length;
        ArrayList<Integer> stationsInDB = new ArrayList<Integer>();
        Cursor cursor = null;
        try {
            // get non favorite stations
            cursor = mContext.getContentResolver().query(Station.CONTENT_URI,
                    new String[] { FmStation.Station.FREQUENCY },
                    FmStation.Station.IS_FAVORITE + "=0",
                    null, FmStation.Station.FREQUENCY);
            if ((null != cursor) && cursor.moveToFirst()) {

                do {
                    int freqInDB = cursor.getInt(cursor.getColumnIndex(
                            FmStation.Station.FREQUENCY));
                    stationsInDB.add(freqInDB);
                } while (cursor.moveToNext());

            } else {
                Log.i(TAG, "updateDBInLocation, insertSearchedStation cursor is null");
            }
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }

        int listSizeInDB = stationsInDB.size();
        // delete station if db frequency is not in searched list
        for (int i = 0; i < listSizeInDB; i++) {
            int freqInDB = stationsInDB.get(i);
            for (int j = 0; j < searchedListSize; j++) {
                int freqSearched = stations[j];
                if (freqInDB == freqSearched) {
                    break;
                }
                if (j == (searchedListSize - 1) && freqInDB != freqSearched) {
                    // delete from db
                    FmStation.deleteStationInDb(mContext, freqInDB);
                }
            }
        }

        // add to db if station is not in db
        for (int j = 0; j < searchedListSize; j++) {
            int freqSearched = stations[j];
            if (FmUtils.isValidStation(freqSearched)) {
                stationNum++;
                if (!stationsInDB.contains(freqSearched)
                        && !FmStation.isFavoriteStation(mContext, freqSearched)) {
                    // insert to db
                    FmStation.insertStationToDb(mContext, freqSearched, "");
                }
            }
        }
        return stationNum;
    }

    /**
     * Check if current is lock task mode. If in this mode, AMS will cannot destory
     * FmRadioActivity even call finish()
     * Settings->Security->Screen pinning on
     * @return true if current screen pinning on FmRadioActivity
     */
    public boolean isInLockTaskMode() {
        Log.i(TAG, "isInLockTaskMode:" + mActivityManager.isInLockTaskMode());
        return mActivityManager.isInLockTaskMode();
    }

    /**
     * The background handler
     */
    class FmRadioServiceHandler extends Handler {
        public FmRadioServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Integer stringId = msg.arg1;
            Bundle bundle;
            boolean isPowerup = false;
            boolean isSwitch = true;
            Log.i(TAG, "handleMessage: " + msg.what);
            switch (msg.what) {

            // power up
            case FmListener.MSGID_POWERUP_FINISHED:
                bundle = msg.getData();
                handlePowerUp(bundle);
                break;

                // power down
            case FmListener.MSGID_POWERDOWN_FINISHED:
                handlePowerDown();
                break;

                // fm exit
            case FmListener.MSGID_FM_EXIT:
                if (mIsSpeakerUsed) {
                    setForceUse(false);
                }
                powerDown();
                closeDevice();

                bundle = new Bundle(1);
                bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.MSGID_FM_EXIT);
                notifyActivityStateChanged(bundle);
                // Finish favorite when exit FM
                /*if (sExitListener != null) {
                    sExitListener.onExit();
                }*/
                break;

                // switch antenna
            case FmListener.MSGID_SWITCH_ANTENNA:
                Log.i(TAG, "I'm msgid_swich_anteana in service" );//fengyao
                bundle = msg.getData();
                int value = bundle.getInt(FmListener.SWITCH_ANTENNA_VALUE);

                // if ear phone insert, need dismiss plugin earphone
                // dialog
                // if earphone plug out and it is not play recorder
                // state, show plug dialog.
                if (0 == value) {
                    powerUpAsync(FmUtils.computeFrequency(mCurrentStation));
                    bundle.putInt(FmListener.CALLBACK_FLAG,
                            FmListener.MSGID_SWITCH_ANTENNA);
                    bundle.putBoolean(FmListener.KEY_IS_SWITCH_ANTENNA, true);
                    notifyActivityStateChanged(bundle);
                } else {
                    // ear phone plug out, and recorder state is not
                    // play recorder state,
                    // show dialog.
                    if (mRecordState != FmRecorder.STATE_PLAYBACK) {
                        bundle.putInt(FmListener.CALLBACK_FLAG,
                                FmListener.MSGID_SWITCH_ANTENNA);
                        bundle.putBoolean(FmListener.KEY_IS_SWITCH_ANTENNA, false);
                        notifyActivityStateChanged(bundle);
                    }
                }
                break;

                // tune to station
            case FmListener.MSGID_TUNE_FINISHED:
                bundle = msg.getData();
                float tuneStation = bundle.getFloat(FM_FREQUENCY);
                boolean isTune = tuneStation(tuneStation);
                // if tune fail, pass current station to update ui
                if (!isTune) {
                    tuneStation = FmUtils.computeFrequency(mCurrentStation);
                }
                bundle = new Bundle(3);
                bundle.putInt(FmListener.CALLBACK_FLAG,
                        FmListener.MSGID_TUNE_FINISHED);
                bundle.putBoolean(FmListener.KEY_IS_TUNE, isTune);
                bundle.putFloat(FmListener.KEY_TUNE_TO_STATION, tuneStation);
                notifyActivityStateChanged(bundle);
                break;

                // seek to station
            case FmListener.MSGID_SEEK_FINISHED:
                bundle = msg.getData();
                mIsSeeking = true;
                float seekStation = seekStation(bundle.getFloat(FM_FREQUENCY),
                        bundle.getBoolean(OPTION));
                boolean isStationTunningSuccessed = false;
                int station = FmUtils.computeStation(seekStation);
                if (FmUtils.isValidStation(station)) {
                    isStationTunningSuccessed = tuneStation(seekStation);
                }
                // if tune fail, pass current station to update ui
                if (!isStationTunningSuccessed) {
                    seekStation = FmUtils.computeFrequency(mCurrentStation);
                }
                bundle = new Bundle(2);
                bundle.putInt(FmListener.CALLBACK_FLAG,
                        FmListener.MSGID_TUNE_FINISHED);
                bundle.putBoolean(FmListener.KEY_IS_TUNE, isStationTunningSuccessed);
                bundle.putFloat(FmListener.KEY_TUNE_TO_STATION, seekStation);
                notifyActivityStateChanged(bundle);
                mIsSeeking = false;
                break;

                // start scan
            case FmListener.MSGID_SCAN_FINISHED:
                int[] stations = null;
                int[] result = null;
                int scanTuneStation = 0;
                boolean isScan = true;
                mIsScanning = true;
                if (powerUp(FmUtils.DEFAULT_STATION_FLOAT)) {
                    stations = startScan();
                }

                // check whether cancel scan
                if ((null != stations) && stations[0] == -100) {
                    isScan = false;
                    result = new int[] {
                            -1, 0
                    };
                } else {
                    result = updateStations(stations);
                    scanTuneStation = result[0];
                    tuneStation(FmUtils.computeFrequency(mCurrentStation));
                }

                /*
                 * if there is stop command when scan, so it needs to mute
                 * fm avoid fm sound come out.
                 */
                if (mIsAudioFocusHeld) {
                    setMute(false);
                }
                bundle = new Bundle(4);
                bundle.putInt(FmListener.CALLBACK_FLAG,
                        FmListener.MSGID_SCAN_FINISHED);
                //bundle.putInt(FmListener.KEY_TUNE_TO_STATION, scanTuneStation);
                bundle.putInt(FmListener.KEY_STATION_NUM, result[1]);
                bundle.putBoolean(FmListener.KEY_IS_SCAN, isScan);

                mIsScanning = false;
                // Only notify the newest request activity
                notifyCurrentActivityStateChanged(bundle);
                break;

                // audio focus changed
            case FmListener.MSGID_AUDIOFOCUS_CHANGED:
                bundle = msg.getData();
                int focusState = bundle.getInt(FmListener.KEY_AUDIOFOCUS_CHANGED);

                updateAudioFocus(focusState);

                break;

            case FmListener.MSGID_SET_RDS_FINISHED:
                bundle = msg.getData();
                setRds(bundle.getBoolean(OPTION));
                break;

            case FmListener.MSGID_SET_MUTE_FINISHED:
                bundle = msg.getData();
                setMute(bundle.getBoolean(OPTION));
                break;

            case FmListener.MSGID_ACTIVE_AF_FINISHED:
                activeAf();
                break;

                /********** recording **********/
            case FmListener.MSGID_STARTRECORDING_FINISHED:
                startRecording();
                break;

            case FmListener.MSGID_STOPRECORDING_FINISHED:
                stopRecording();
                break;

            case FmListener.MSGID_RECORD_MODE_CHANED:
                bundle = msg.getData();
                setRecordingMode(bundle.getBoolean(OPTION));
                break;

            case FmListener.MSGID_SAVERECORDING_FINISHED:
                bundle = msg.getData();
                saveRecording(bundle.getString(RECODING_FILE_NAME));
                break;

            default:
                break;
            }
        }

    }

    /**
     * handle power down, execute power down and call back to activity.
     */
    public void handlePowerDown() {
        Log.i(TAG, "handlePowerDown");
        Bundle bundle;
        boolean isPowerdown = powerDown();
        mIsPowerDown = isPowerdown;
        bundle = new Bundle(1);
        bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.MSGID_POWERDOWN_FINISHED);
        notifyActivityStateChanged(bundle);
    }

    /**
     * handle power up, execute power up and call back to activity.
     *
     * @param bundle power up frequency
     */
    public void handlePowerUp(Bundle bundle) {
        Log.i(TAG, "handlePowerUp");
        Log.i(TAG, "I'm msgid_swich_anteana in handle powerup" );//fengyao
        boolean isPowerUp = false;
        boolean isSwitch = true;
        float curFrequency = bundle.getFloat(FM_FREQUENCY);

        if (!isAntennaAvailable()) {
            Log.i(TAG, "handlePowerUp, earphone is not ready");
            bundle = new Bundle(2);
            bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.MSGID_SWITCH_ANTENNA);
            bundle.putBoolean(FmListener.KEY_IS_SWITCH_ANTENNA, false);
            notifyActivityStateChanged(bundle);
            return;
        }

        if (powerUp(curFrequency)) {
            if (FmUtils.isFirstTimePlayFm(mContext)) {
                isPowerUp = firstPlaying(curFrequency);
                FmUtils.setIsFirstTimePlayFm(mContext);
            } else {
                isPowerUp = playFrequency(curFrequency);
            }
            mPausedByTransientLossOfFocus = false;
        }
        bundle = new Bundle(2);
        bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.MSGID_POWERUP_FINISHED);
        bundle.putInt(FmListener.KEY_TUNE_TO_STATION, mCurrentStation);
        notifyActivityStateChanged(bundle);
    }

    /**
     * check FM is foreground or background
     */
    /*
    public boolean isActivityForeground() {
        return (mIsFmMainForeground || mIsFmFavoriteForeground || mIsFmRecordForeground);
    }
     */

    /**
     * mark FmMainActivity is foreground or not
     * @param isForeground
     */
    public void setFmMainActivityForeground(boolean isForeground) {
        mIsFmMainForeground = isForeground;
    }

    /**
     * mark FmFavoriteActivity activity is foreground or not
     * @param isForeground
     */
    public void setFmFavoriteForeground(boolean isForeground) {
        mIsFmFavoriteForeground = isForeground;
    }

    /**
     * mark FmRecordActivity activity is foreground or not
     * @param isForeground
     */
    public void setFmRecordActivityForeground(boolean isForeground) {
        mIsFmRecordForeground = isForeground;
    }

    /**
     * Get the recording sdcard path when staring record
     *
     * @return sdcard path like "/storage/sdcard0"
     */
    public static String getRecordingSdcard() {
        return sRecordingSdcard;
    }

    /**
     * Register the listener for exit
     *
     * @param listener The listener want to know the exit event
     */
    //public void registerExitListener(OnExitListener listener) {
        //sExitListener = listener;
    //}

    /**
     * Unregister the listener for exit
     *
     * @param listener The listener want to know the exit event
     */
    //public void unregisterExitListener(OnExitListener listener) {
        //sExitListener = null;
    //}

    /**
     * Get the latest recording name the show name in save dialog but saved in
     * service
     *
     * @return The latest recording name or null for not modified
     */
    public String getModifiedRecordingName() {
        return mModifiedRecordingName;
    }

    /**
     * Set the latest recording name if modify the default name
     *
     * @param name The latest recording name or null for not modified
     */
    public void setModifiedRecordingName(String name) {
        mModifiedRecordingName = name;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "=======onTaskRemoved()");
        Log.i(TAG, "onTaskRemoved()/mPowerStatus = " + mPowerStatus + "/POWER_UP = " + POWER_UP);
        setForceUse(false);
        if (mPowerStatus != POWER_UP) {
            exitFm();
            stopSelf();
        }
        super.onTaskRemoved(rootIntent);
    }

    public boolean firstPlaying(float frequency) {
        Log.i(TAG, "firstPlaying, freq: " + frequency);
        if (mPowerStatus != POWER_UP) {
            Log.i(TAG, "firstPlaying, FM is not powered up");
            return false;
        }
        boolean isSeekTune = false;

        //float seekStation = FmNative.seek(frequency, false);
        //Gionee fengyao 20161223 modify for 51867 mmi slowly begin
        float seekStation = 107.5f;
        if(isActivityForeground(MMI_PACKAGE)) {
            Log.i(TAG, "MMI is foreground ,should not seek ");
        }else {
            seekStation = FmNative.seek(frequency, false);
        }
        //Gionee fengyao 20161223 modify for 51867 mmi slowly end

        int station = FmUtils.computeStation(seekStation);
        if (FmUtils.isValidStation(station)) {
            isSeekTune = FmNative.tune(seekStation);
            if (isSeekTune) {
                playFrequency(seekStation);
            }
        }
        // if tune fail, pass current station to update ui
        if (!isSeekTune) {
            seekStation = FmUtils.computeFrequency(mCurrentStation);
        }
        return isSeekTune;
    }

    /**
     * Set the mIsDistanceExceed
     * @param exceed true is exceed, false is not exceed
     */
    public void setDistanceExceed(boolean exceed) {
        mIsDistanceExceed = exceed;
    }

    /**
     * Set notification class name
     * @param clsName The target class name of activity
     */
    public void setNotificationClsName(String clsName) {
        mTargetClassName = clsName;
    }

    // FM Radio EM start
    /**
     * Inquiry if fm stereo mono(true, stereo; false mono)
     *
     * @return (true, stereo; false, mono)
     */
    public boolean getStereoMono() {
        Log.i(TAG, "FMRadioService.getStereoMono");
        return FmNative.stereoMono();
    }

    /**
     * Force set to stero/mono mode
     *
     * @param isMono
     *            (true, mono; false, stereo)
     * @return (true, success; false, failed)
     */
    public boolean setStereoMono(boolean isMono) {
        Log.i(TAG, "FMRadioService.setStereoMono: isMono=" + isMono);
        return FmNative.setStereoMono(isMono);
    }

    /**
     * set RSSI, desense RSSI, mute gain soft
     * @param index flag which will execute
     * (0:rssi threshold,1:desense rssi threshold,2: SGM threshold)
     * @param value send to native
     * @return execute ok or not
     */
    public boolean setEmth(int index, int value) {
        Log.i(TAG, ">>> FMRadioService.setEmth: index=" + index + ",value=" + value);
        boolean isOk = FmNative.emsetth(index, value);
        Log.i(TAG, "<<< FMRadioService.setEmth: isOk=" + isOk);
        return isOk;
    }

    /**
     * send variables to native, and get some variables return.
     * @param val send to native
     * @return get value from native
     */
    public short[] emcmd(short[] val) {
        Log.i(TAG, ">>FMRadioService.emcmd: val=" + val);
        short[] shortCmds = null;
        shortCmds = FmNative.emcmd(val);
        Log.i(TAG, "<<FMRadioService.emcmd:" + shortCmds);
        return shortCmds;
    }

    /**
     * Get hardware version not need async
     */
    public int[] getHardwareVersion() {
        return FmNative.getHardwareVersion();
    }

    /**
     * Read cap array method not need async
     */
    public int getCapArray() {
        Log.i(TAG, "FMRadioService.readCapArray");
        if (mPowerStatus != POWER_UP) {
            Log.i(TAG, "FM is not powered up");
            return -1;
        }
        return FmNative.readCapArray();
    }

    /**
     * Get rssi not need async
     */
    public int getRssi() {
        Log.i(TAG, "FMRadioService.readRssi");
        if (mPowerStatus != POWER_UP) {
            Log.i(TAG, "FM is not powered up");
            return -1;
        }
        return FmNative.readRssi();
    }

    /**
     * read rds bler not need async
     */
    public int getRdsBler() {
        Log.i(TAG, "FMRadioService.readRdsBler");
        if (mPowerStatus != POWER_UP) {
            Log.i(TAG, "FM is not powered up");
            return -1;
        }
        return FmNative.readRdsBler();
    }
    // FM Radio EM end

    //Gionee zhangke 20151030 add for CR01577644 start
    public boolean isActivityForeground() {
        String fm_package = mContext.getPackageName();
        //boolean bRet = isActivityForeground(fm_package) || isActivityForeground(MMI_PACKAGE);
        boolean bRet = mIsFmMainForeground || mIsFmFavoriteForeground || mIsFmRecordForeground || isActivityForeground(MMI_PACKAGE);
        Log.i(TAG, "isActivityForeground() return " + bRet);
        return bRet;
    }

    public boolean isActivityForeground(String packageName) {
        List<RunningAppProcessInfo> appProcessInfos = mActivityManager.getRunningAppProcesses();
        //Gionee <bug> 20160223 fengyao add for CR01639203 begin.
        if(appProcessInfos==null)
        {
            return false;
        }
        //Gionee <bug> 20160223 fengyao add for CR01639203 end
        for (RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.processName.equals(packageName)) {
                int importance = appProcessInfo.importance;
                if (importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                        importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    return true;
                } 
            }
        }
        return false;
    }

    /*public IFmRadioService.Stub mAIDLBinder = new IFmRadioService.Stub() {
        public boolean mIsPowerUp = false;
        public boolean mIsAFEnabled = false;

        public boolean openDevice() {
            return FmService.this.openDevice();
        }

        public boolean closeDevice() {
            try {
                if (mValueHeadSetPlug == 1) {
                    Log.i(TAG,"aidl closeDevice() mValueHeadSetPlug == 1 " );
                    return true;
                }  
                Log.i(TAG,"aidl closeDevice  mFmServiceHandler = " + mFmServiceHandler );
                if (mFmServiceHandler != null) {
                    mFmServiceHandler.removeMessages(FmListener.MSGID_FM_EXIT);
                    
                    Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_FM_EXIT);
                    mFmServiceHandler.sendMessage(msg);
                }
                synchronized (mAidlCloseDeviceLock) {
                    Log.i(TAG, "aidl closeDevice synchronized  (mAidlCloseDeviceLock)");
                    mAidlCloseDeviceLock.wait();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG,"aidl closeDevice  error = " + e.toString() );
            }
            Log.i(TAG,"aidl closeDevice() = !mIsDeviceOpen = " + !mIsDeviceOpen );
            return !mIsDeviceOpen;
        }

        public boolean isDeviceOpen() {
            return FmService.this.isDeviceOpen();
        }

        public boolean powerUp(float frequency) {
            boolean isPowerup = false;
            boolean isSwitch = true;

            long time = System.currentTimeMillis();
            Log.i(TAG, "performance test from aidl. service handler power up start:" + time);
            isSwitch = (FmService.this.switchAntenna(1) == 0) ? true : false;


            if (!FmService.this.isAntennaAvailable() && !isSwitch) {
                //mIsPowerUping = false;
                Bundle bundle = new Bundle(2);
                bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.MSGID_SWITCH_ANTENNA);
                bundle.putBoolean(FmListener.KEY_IS_SWITCH_ANTENNA, isSwitch);
                FmService.this.notifyActivityStateChanged(bundle);
                return false;
            }
            Log.i(TAG, "addlog firstplay"+FmUtils.isFirstTimePlayFm(mContext) );//fengyao
            if (FmService.this.powerUp(frequency)) {
                isPowerup = FmService.this.firstPlaying(frequency);
                mPausedByTransientLossOfFocus = false;
            }
            //mIsPowerUping = false;
            mIsPowerUp = isPowerup;
            Bundle bundle = new Bundle(2);
            bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.MSGID_POWERUP_FINISHED);
            bundle.putBoolean(FmListener.KEY_IS_POWER_UP, isPowerup);
            FmService.this.notifyActivityStateChanged(bundle);
            time = System.currentTimeMillis();
            Log.i(TAG, "performance test from aidl. service handler power up end:" + time);
            Log.i(TAG, "addlog isPowerup and add Notification"+isPowerup ); 
            FmService.this.updatePlayingNotification();
            return isPowerup;
        }

        public boolean powerDown() {
           // boolean isPowerdown = FmService.this.powerDown();
            try {
                if (mValueHeadSetPlug == 1) {
                    Log.i(TAG,"aidl powerDown() mValueHeadSetPlug == 1 " );
                    return true;
                }               
                Log.i(TAG,"aidl powerDown()  mFmServiceHandler = " + mFmServiceHandler );
                if (mFmServiceHandler != null) {
                    mFmServiceHandler.removeMessages(FmListener.MSGID_POWERDOWN_FINISHED);
                    Message msg = mFmServiceHandler.obtainMessage(FmListener.MSGID_POWERDOWN_FINISHED);
                    mFmServiceHandler.sendMessage(msg);
                }
                Log.i(TAG,"Thread.currentThread().getname = " + Thread.currentThread().getName() );
                Log.i(TAG,"Thread.currentThread().getId() = " + Thread.currentThread().getId() );
               // Thread.currentThread().getId()
                synchronized (mAidlPowerdownLock) {
                    Log.i(TAG, "aidl powerDown() synchronized  (mAidlPowerdownLock)");
                    mAidlPowerdownLock.wait();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG,"aidl powerDown()  error = " + e.toString() );
            }
            Bundle bundle = new Bundle(1);
            bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.MSGID_POWERDOWN_FINISHED);
            //bundle.putBoolean(FmListener.KEY_IS_POWER_DOWN, isPowerdown);
            FmService.this.notifyActivityStateChanged(bundle);
            //Gionee add for ximalaya
            mIsPowerUp = false;
            Log.i(TAG,"aidl powerDown() = mIsPowerDown = " + mIsPowerDown );           
            return mIsPowerDown;
        }

        public boolean isPowerUp() {
            return FmService.this.isPowerUp();//mIsPowerUp;//
        }

        public boolean tune(float frequency) {
            boolean isTune = FmService.this.tuneStation(frequency);
            Bundle bundle = new Bundle(4);
            bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.MSGID_TUNE_FINISHED);
            bundle.putBoolean(FmListener.KEY_IS_TUNE, isTune);
            bundle.putFloat(FmListener.KEY_TUNE_TO_STATION, frequency);
            bundle.putBoolean(FmListener.KEY_IS_POWER_UP, mIsPowerUp);
            FmService.this.notifyActivityStateChanged(bundle);

            return isTune;
        }

        public float seek(float frequency, boolean isUp) {
            float seekStation = FmService.this.seekStation(frequency, isUp);
            boolean isSeekTune = false;
            int station = FmUtils.computeStation(seekStation);
            if (FmUtils.isValidStation(station)) {
                isSeekTune = FmService.this.tuneStation(seekStation);
            }
            Bundle bundle = new Bundle(3);
            bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.MSGID_TUNE_FINISHED);
            bundle.putBoolean(FmListener.KEY_IS_TUNE, isSeekTune);
            bundle.putFloat(FmListener.KEY_TUNE_TO_STATION, seekStation);
            FmService.this.notifyActivityStateChanged(bundle);

            return seekStation;
        }

        public int[] startScan() {
            int[] channels = null;
            int[] result = null;
            boolean isScan = true;
            if (FmService.this.powerUp(FmUtils.DEFAULT_STATION_FLOAT)) {
                channels = FmService.this.startScan();
            }

            // check whether cancel scan
            if ((null != channels) && channels[0] == -100) {
                Log.i(TAG, "user canceled scan:channels[0]=" + channels[0]);
                isScan = false;
                result = new int[] { -1, 0 };
            } else {
                result = FmService.this.updateStations(channels);
                FmService.this.tuneStation(FmUtils.computeFrequency(result[0]));
                FmService.this.resumeFmAudio();
                FmService.this.setMute(false);
            }
            Bundle bundle = new Bundle(4);
            bundle.putInt(FmListener.CALLBACK_FLAG, FmListener.MSGID_SCAN_FINISHED);
            bundle.putInt(FmListener.KEY_TUNE_TO_STATION, result[0]);
            bundle.putInt(FmListener.KEY_STATION_NUM, result[1]);
            bundle.putBoolean(FmListener.KEY_IS_SCAN, isScan);
            FmService.this.notifyActivityStateChanged(bundle);

            return channels;
        }

        public boolean stopScan() {
            return FmService.this.stopScan();
        }


        public String getPS() {
            return FmService.this.getPs();
        }

        public String getLRText() {
            return FmService.this.getRtText();
        }

        public int activeAF() {
            return FmService.this.activeAf();
        }


        public int setMute(boolean mute) {
            return FmService.this.setMute(mute);
        }


        public void useEarphone(boolean use) {
            FmService.this.setSpeakerPhoneOn(use);
        }

        public boolean isEarphoneUsed() {
            return FmService.this.isSpeakerUsed();
        }

        public void initService(int iCurrentStation) {
            FmService.this.initService(iCurrentStation);
        }


        public void enablePSRT(boolean enable) {
            Log.i(TAG, "FmService.enablePSRT: " + enable);
            //mIsPSRTEnabled = enable;
            //if (!mIsPSRTEnabled) {
            //    setPS("");
            //    setLRText("");
            //}
        }

        public void enableAF(boolean enable) {
            Log.i(TAG, "FmService.enableAF: " + enable);
            mIsAFEnabled = enable;
        }

        public void enableTA(boolean enable) {
            Log.i(TAG, ">>> FmService.enableTA: " + enable);
            Log.i(TAG, "<<< FmService.enableTA");
        }

        public boolean isPSRTEnabled() {
            Log.i(TAG, "FmService.isPSRTEnabled: " );//+ mIsPSRTEnabled);
            //return mIsPSRTEnabled;
            return true;
        }

        public boolean isAFEnabled() {
            Log.i(TAG, "FmService.isAFEnabled: " + mIsAFEnabled);
            return mIsAFEnabled;
        }

        public boolean isTAEnabled() {
            Log.i(TAG, ">>> FmService.isTAEnabled");
            return false;
        }

        public int getFrequency() {
            return FmService.this.getFrequency();
        }

        public void setFrequency(int station) {
            FmService.this.setFrequency(station);
        }

        public void resumeFmAudio() {
            FmService.this.resumeFmAudio();
        }


        public void startRecording() {
            FmService.this.startRecording();
        }

        public void stopRecording() {
            FmService.this.stopRecording();
        }

        public void startPlayback() {
            //FmService.this.startPlayback();
        }

        public void stopPlayback() {
            //FmService.this.stopPlayback();
        }

        public void saveRecording(String newName) {
            FmService.this.saveRecording(newName);
        }

        public long getRecordTime() {
            return FmService.this.getRecordTime();
        }

        public void setRecordingMode(boolean isRecording) {
            FmService.this.setRecordingMode(isRecording);
        }

        public boolean getRecordingMode() {
            return FmService.this.getRecordingMode();
        }

        public int getRecorderState() {
            return FmService.this.getRecorderState();
        }

        public int getPlaybackPosition() {
            return 0;
        }

        public String getRecordingName() {
            return FmService.this.getRecordingName();
        }

        public boolean getResumeAfterCall(){
            return false;
        }

        public boolean isSIMCardIdle() {
            return false;
        }

        public void setStopPressed(boolean isStopPressed) {

        }

    };*/

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "==onStart==");
        mServiceStartId = startId;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind()");
        if(isActivityForeground(MMI_PACKAGE)){
            Log.i(TAG, ">>>stopSelf, startId = "+mServiceStartId);
            if(mServiceStartId > 0){
                stopSelf(mServiceStartId);
            }else{
                stopSelf();
            }
        }
        return true;
    }
    //Gionee zhangke 20151030 add for CR01577644 end

    //Gionee jingcl 20161114 add for notity third app when audiofocus change and service killed start
    public void notifyThirdAppAudioFocusChange(int focusState) {
        try {
            Log.i(TAG, "notifyThirdAppAudioFocusChange()/focusState = " + focusState);
                if (isPowerUp() || mPausedByTransientLossOfFocus ) {
                    //mIGioneeFmServiceListener.onAudioFocusChanged(focusState);
                }
            
        } catch (Throwable e) {
            Log.i(TAG, "notifyThirdAppAudioFocusChange()/e = " + e.toString());
        }
    }

    public void notifyThirdAppServiceKilled() {
        try {
            Log.i(TAG, "notifyThirdAppServiceKilled()");
        } catch (Exception e) {
            Log.i(TAG, "notifyThirdAppServiceKilled()/e = " + e.toString());
        }
    }
    //Gionee jingcl 20161114 add for notity third app when audiofocus change and service killed end
}
