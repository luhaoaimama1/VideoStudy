package zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process;

import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecorder;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.Process;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.utils.VolumeUtils;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class VolumeProcess implements Process {
    VolumeUtils mVolumeUtils = new VolumeUtils();

    @Override
    public void init(AudioRecorder audioRecorder) throws Exception {

    }

    @Override
    public int processData(AudioRecorder audioRecord, byte[] pcmData, int readsize) throws Exception {
        if (mVolumeListener != null)
            mVolumeListener.onVolume(mVolumeUtils.calculateRealVolume(pcmData, readsize), mVolumeUtils.getVolume());
        return readsize;
    }

    @Override
    public void end() throws Exception {

    }

    @Override
    public void release() throws Exception {

    }

    private VolumeListener mVolumeListener;

    public VolumeProcess(VolumeListener volumeListener) {
        this.mVolumeListener = volumeListener;
    }

    public interface VolumeListener {
        void onVolume(int volume, int maxVolume);
    }
}
