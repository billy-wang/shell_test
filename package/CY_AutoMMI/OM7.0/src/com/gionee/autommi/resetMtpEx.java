package com.gionee.autommi;
import android.media.AudioManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class resetMtpEx extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        am.setParameters("resetMtpEx=true");
    }
}
