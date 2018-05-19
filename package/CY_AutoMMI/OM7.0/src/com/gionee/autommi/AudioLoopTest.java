package com.gionee.autommi;

import java.io.IOException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.gionee.util.DswLog;
import android.view.KeyEvent;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaPlayer; 
import android.media.AudioManager;


public class AudioLoopTest extends BaseActivity { 
	private static  String TAG = "AudioLoopTest";//音频回路
	protected static final int START_AUDIO_CAP = 0;
	protected static final int STOP_AUDIO_CAP = 1;
	protected static final int PLAY_BACK = 2;
	private MediaRecorder recorder;
	private MediaPlayer player;
	private AudioManager am;
	private String filePath = "/sdcard/audio.wav";
	private int duration = 2000;
	private static final String DURA = "dura";

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case START_AUDIO_CAP:
				startCaptureAudio();
				break;
			case STOP_AUDIO_CAP:
				stopCaptureAudio();
				break;
			case PLAY_BACK:
				playBack();
				break;
			}
		}
	};
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent it = this.getIntent();
		duration = Integer.parseInt(it.getStringExtra(DURA)) * 1000;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		handler.sendEmptyMessage(START_AUDIO_CAP);
		handler.sendEmptyMessageDelayed(STOP_AUDIO_CAP, duration);
		handler.sendEmptyMessageDelayed(PLAY_BACK, duration + 500);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		handler.removeMessages(START_AUDIO_CAP);
		handler.removeMessages(STOP_AUDIO_CAP);
		handler.removeMessages(PLAY_BACK);
		if (null != player) {
			player.stop();
			player.release();
		}
		this.finish();
	}

	private void startCaptureAudio() {
		DswLog.d(TAG, "startCaptureAudio......");
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setOutputFile(filePath);//生成MP4文件
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			recorder.prepare();
			DswLog.d(TAG, "recorder.prepare......");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
		
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			recorder.start();
			
		} catch (IllegalStateException e) {
			// TODO: handle exception
			DswLog.d(TAG, "11111111111111");
		}
	}

	private void stopCaptureAudio() {
		// TODO Auto-generated method stub
		if (null != recorder) {
			try {
				recorder.stop();
				recorder.release();
				
			} catch (IllegalStateException e) {
				// TODO: handle exception
				DswLog.d(TAG, "2222222222222");
			}
		
		}
	}

	private void playBack() {
		player = new MediaPlayer();	
		try {
			player.setDataSource(filePath);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		try {
			player.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DswLog.d(TAG, "---player.start()---");
		player.start();
		DswLog.d(TAG, "---player  end---");
		
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_CALL:
		case KeyEvent.KEYCODE_ENDCALL:
		case KeyEvent.KEYCODE_HEADSETHOOK:
			return true;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}

}
