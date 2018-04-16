package zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecorder;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.Process;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 * todo  编录边放  放的声音会被录进去  导致杂音 所以不用了暂时
 */
public class AudioTrackProcess implements Process {
    AudioTrack audioTrack;
    int playBufSize,frequency, channel, audioEncoding;

    @Override
    public void init(AudioRecorder audioRecorder) throws Exception {
        frequency = audioRecorder.getRecordConfig().getSampleRate();
        channel = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        audioEncoding = audioRecorder.getRecordConfig().getAudioFormat();
        playBufSize = AudioTrack.getMinBufferSize(frequency, channel, audioEncoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                channel, audioEncoding,
                playBufSize, AudioTrack.MODE_STREAM);
        audioTrack.setStereoVolume(0.7f, 0.7f);//设置当前音量大小
        audioTrack.play();//开始播放
    }

    @Override
    public int processData(AudioRecorder audioRecord, byte[] pcmData, int readsize) throws Exception {
        audioTrack.write(pcmData, 0, pcmData.length);
        return readsize;
    }

    @Override
    public void end() throws Exception {
        audioTrack.stop();
    }

    @Override
    public void release() throws Exception {
        audioTrack.release();
    }

}
