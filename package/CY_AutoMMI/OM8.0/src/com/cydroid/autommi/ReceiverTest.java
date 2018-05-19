package com.cydroid.autommi;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.content.Context;
import com.cydroid.util.DswLog;

public class ReceiverTest extends BaseActivity {

    private int duration = 2; // seconds
    //Gionee <GN_AutoMMI><lifeilong><20161109> modify for 21279 begin
    private final int sampleRate = 16000;//8000
	//Gionee <GN_AutoMMI><lifeilong><20161109> modify for 21279 end
    private int numSamples ;
    private double[] sample ;
    private final double freqOfTone = 1000; // hz
    private byte[] generatedSnd ;
	private static final String DURA = "dura";
    private AudioManager am;

    private int mAudioMode;
    private boolean SpeakerphoneOn = false, MusicActive = false, WiredHeadsetOn = false, BluetoothScoOn = false, BluetoothA2dpOn = false, MicrophoneMute = false;

    private static final String TAG = "ReceiverTest_billy";
    Thread tester = new Thread() {
		@Override
		public void run() {
			//Gionee <GN_BSP_AutoMMI> <chengq> <20170510> modify for ID 117948 begin
			String aString = TestUtils.getStreamVoice("ReceiverTest");
			int i = Integer.valueOf(aString).intValue();
			DswLog.e(TAG, "i = " + i);
			if (null != am) {
				//int maxVol = am.getStreamMaxVolume(am.STREAM_VOICE_CALL);
				int maxVol = am.getStreamMaxVolume(am.STREAM_VOICE_CALL);
				am.setStreamVolume(am.STREAM_VOICE_CALL, maxVol - i, 0);
				DswLog.e(TAG, "maxVol = " + maxVol + " STREAM_VOICE_CALL= " + (maxVol - i));
			}
			//Gionee <GN_BSP_AutoMMI> <chengq> <20170510> modify for ID 117948 begin
			genTone();
			playSound();
		}	
    };
	private AudioTrack track;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		Intent it = this.getIntent();
		duration = Integer.parseInt(it.getStringExtra(DURA)) * 2;
		compose();
	}

	
    private void compose() {
		// TODO Auto-generated method stub
    	numSamples = duration * sampleRate;
    	sample = new double[numSamples];
    	generatedSnd = new byte[2 * numSamples];		
	}


	void genTone() {
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    void playSound() {
        /* add by Billy.Wang */
        //DswLog.d(TAG, "setMode AudioManager.MODE_IN_COMMUNICATION");
        //am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        DswLog.d(TAG, "setMode AudioManager.MODE_IN_CALL");
        am.setMode(AudioManager.MODE_IN_CALL);
        
        DswLog.i(TAG, "new AudioTrack STREAM_VOICE_CALL");
    	track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        track.write(generatedSnd, 0, generatedSnd.length);


        mAudioMode = am.getMode();
        DswLog.d(TAG, "getMode " + mAudioMode);

        SpeakerphoneOn = am.isSpeakerphoneOn();
        BluetoothScoOn = am.isBluetoothScoOn();
        BluetoothA2dpOn = am.isBluetoothA2dpOn();
        WiredHeadsetOn = am.isWiredHeadsetOn();
        MusicActive = am.isMusicActive();
        DswLog.d(TAG, "SpeakerphoneOn " + SpeakerphoneOn + " BluetoothScoOn " + BluetoothScoOn + " BluetoothA2dpOn " + BluetoothA2dpOn + " WiredHeadsetOn " + WiredHeadsetOn + " MusicActive " + MusicActive);
        MicrophoneMute = am.isMicrophoneMute();
        DswLog.d(TAG, "MicrophoneMute " + MicrophoneMute);

        if(SpeakerphoneOn)
           am.setSpeakerphoneOn(false);


        DswLog.i(TAG, "playSound");
        track.play();
    }


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        if ( null != track ) {
            DswLog.i(TAG, "stop tarck and release");
		    track.stop();
		    track.release();
        }

		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170510> modify for ID 117948 begin
		/* disable by Billy.Wang */
        //am.setSpeakerphoneOn(true);
		am.setMode(AudioManager.MODE_NORMAL);
		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170510> modify for ID 117948 end
        //am.setParameters("ADBMMIReceiver=0");

        mAudioMode = am.getMode();
        DswLog.d(TAG, "getMode " + mAudioMode);

        SpeakerphoneOn = am.isSpeakerphoneOn();
        BluetoothScoOn = am.isBluetoothScoOn();
        BluetoothA2dpOn = am.isBluetoothA2dpOn();
        WiredHeadsetOn = am.isWiredHeadsetOn();
        MusicActive = am.isMusicActive();
        DswLog.d(TAG, "SpeakerphoneOn " + SpeakerphoneOn + " BluetoothScoOn " + BluetoothScoOn + " BluetoothA2dpOn " + BluetoothA2dpOn + " WiredHeadsetOn " + WiredHeadsetOn + " MusicActive " + MusicActive);
        MicrophoneMute = am.isMicrophoneMute();
        DswLog.d(TAG, "MicrophoneMute " + MicrophoneMute);

        //if(!SpeakerphoneOn)
        //    am.setSpeakerphoneOn(true);

		this.finish();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170510> modify for ID 117948 begin
		/* disable by Billy.Wang */
		//am.setSpeakerphoneOn(false);
		//am.setMode(AudioManager.MODE_IN_COMMUNICATION);
		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170510> modify for ID 117948 end
        //am.setParameters("ADBMMIReceiver=1");
		tester.start();
	}
}

