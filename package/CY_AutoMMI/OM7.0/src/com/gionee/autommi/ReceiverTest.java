package com.gionee.autommi;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.content.Context;
import com.gionee.util.DswLog;

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
    private static final String TAG = "ReceiverTest";
    Thread tester = new Thread() {
		@Override
		public void run() {
			//Gionee <GN_BSP_AutoMMI> <chengq> <20170510> modify for ID 117948 begin
			String aString = TestUtils.getStreamVoice("ReceiverTest");
			int i = Integer.valueOf(aString).intValue();
			DswLog.e(TAG, "i = " + i);
			if (null != am) {
				int maxVol = am.getStreamMaxVolume(am.STREAM_MUSIC);
				DswLog.e(TAG, " set stream  = music ");
				am.setStreamVolume(am.STREAM_MUSIC, maxVol - i, 0);
				DswLog.e(TAG, "maxVol = " + maxVol + " setStreamVolume = " + (maxVol - i));
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
        DswLog.i(TAG, "mmi playSound()");
    	track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        track.write(generatedSnd, 0, generatedSnd.length);
        track.play();
    }


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        if ( null != track ) {
		    track.stop();
		    track.release();
        }
		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170510> modify for ID 117948 begin
		am.setSpeakerphoneOn(true);
		am.setMode(AudioManager.MODE_NORMAL);
		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170510> modify for ID 117948 end
        //am.setParameters("ADBMMIReceiver=0");
		this.finish();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170510> modify for ID 117948 begin
		am.setSpeakerphoneOn(false);
		am.setMode(AudioManager.MODE_IN_COMMUNICATION);
		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170510> modify for ID 117948 end
        //am.setParameters("ADBMMIReceiver=1");
		tester.start();
	}
}

