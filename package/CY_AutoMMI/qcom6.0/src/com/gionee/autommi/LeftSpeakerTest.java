package com.gionee.autommi;

public class LeftSpeakerTest extends SpeakerTest {
    @Override
    protected void chooseSpeaker()  {
        mAudioManager.setParameters("MMI_STEREO_SPEAKER=2"); 
    }

    @Override 
    protected void onPause() {
        super.onPause();
        mAudioManager.setParameters("MMI_STEREO_SPEAKER=0");
    }
}
