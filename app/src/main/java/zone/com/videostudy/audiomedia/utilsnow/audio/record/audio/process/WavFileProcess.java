package zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process;

import android.media.AudioRecord;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecorder;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.Process;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.utils.PCMtoWav;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class WavFileProcess implements Process {

    private final String filePath;
    private  PCMtoWav mPCMtoWav;

    public WavFileProcess(String filePath) {
        this.filePath = filePath;
        mPCMtoWav = new PCMtoWav();
    }


    @Override
    public void init(AudioRecorder audioRecord) throws Exception {
        mPCMtoWav.pcm2wav(audioRecord.getAudioRecordConfig(),filePath);
        mPCMtoWav.initFile();
    }

    @Override
    public int processData(AudioRecorder audioRecord, byte[] pcmData, int readsize) throws Exception {
        if (AudioRecord.ERROR_INVALID_OPERATION != readsize)
            mPCMtoWav.write(pcmData);
        return readsize;
    }

    @Override
    public void end() throws Exception {
        mPCMtoWav.closeFile();
    }

    @Override
    public void release() throws Exception {
    }
}
