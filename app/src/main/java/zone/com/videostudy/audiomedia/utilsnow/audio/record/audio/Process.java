package zone.com.videostudy.audiomedia.utilsnow.audio.record.audio;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public interface Process {

    void init(AudioRecorder audioRecorder)  throws Exception;

    int  processData(AudioRecorder audioRecord, byte[] pcmData, int readsize) throws Exception;

    void end() throws Exception;

    void release() throws Exception;
}
