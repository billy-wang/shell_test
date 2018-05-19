package com.gionee.autommi;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.content.Context;

public class SpeakerTest extends BaseActivity {
    private int duration = 2; // seconds
    //Gionee <GN_AutoMMI><lifeilong><20161109> modify for 21285 begin
    private final int sampleRate = 16000;//8000;
	//Gionee <GN_AutoMMI><lifeilong><20161109> modify for 21285 end
    private int numSamples ;
    private double[] sample ;
    private final double freqOfTone = 1000; // hz
    private byte[] generatedSnd ;
	private static final String DURA = "dura";
    protected AudioManager mAudioManager;

	private AudioTrack track;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent it = this.getIntent();
		duration = Integer.parseInt(it.getStringExtra(DURA)) * 2;
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE); 
		compose();
        chooseSpeaker();
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
    	track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        track.write(generatedSnd, 0, generatedSnd.length);
        track.play();
    }


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		track.stop();
		track.release();
		this.finish();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//Gionee zhangke modify 20151204 for CR01603467 start
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				genTone();
				playSound();
			}	
		}.start();
		//Gionee zhangke modify 20151204 for CR01603467 end

	}

    protected void chooseSpeaker()  {
       
    }
}
