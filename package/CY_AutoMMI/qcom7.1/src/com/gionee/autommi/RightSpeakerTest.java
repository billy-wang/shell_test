package com.gionee.autommi;

public class RightSpeakerTest extends SpeakerTest {
    @Override
    protected void chooseSpeaker()  {
        mAudioManager.setParameters("MMI_STEREO_SPEAKER=1"); 
    }

    @Override 
    protected void onPause() {
        super.onPause();
        mAudioManager.setParameters("MMI_STEREO_SPEAKER=0");
    }
}
