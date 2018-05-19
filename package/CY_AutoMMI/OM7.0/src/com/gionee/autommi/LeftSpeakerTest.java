package com.gionee.autommi;
import com.gionee.util.DswLog;

public class LeftSpeakerTest extends SpeakerTest {
    //Gionee <GN_BSP_MMI> <chengq> <20170227> modify for ID 74495 begin
    @Override
    protected void chooseSpeaker()  {
        mAudioManager.setParameters("MMITone=1");
        DswLog.d("SpeakerTest", "audioManager start set MMITone=1");
    }

    @Override 
    protected void onPause() {
        super.onPause();
        mAudioManager.setParameters("MMITone=0");
        DswLog.d("SpeakerTest", "audioManager end set MMITone=0");
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170227> modify for ID 74495 end
}
