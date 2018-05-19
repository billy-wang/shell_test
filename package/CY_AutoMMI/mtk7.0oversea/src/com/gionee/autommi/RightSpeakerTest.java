package com.gionee.autommi;
import android.util.Log;

public class RightSpeakerTest extends SpeakerTest {
    //Gionee <GN_BSP_MMI> <chengq> <20170227> modify for ID 74495 begin
    @Override
    protected void chooseSpeaker()  {
        mAudioManager.setParameters("MMITone=2");
        Log.d("SpeakerTest", "audioManager start set MMITone=2");
    }

    @Override 
    protected void onPause() {
        super.onPause();
        mAudioManager.setParameters("MMITone=0");
        Log.d("SpeakerTest", "audioManager end set MMITone=0");
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170227> modify for ID 74495 end
}
