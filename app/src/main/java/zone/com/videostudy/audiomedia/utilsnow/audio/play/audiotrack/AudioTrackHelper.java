package zone.com.videostudy.audiomedia.utilsnow.audio.play.audiotrack;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaFormat;

import java.io.FileInputStream;
import java.io.IOException;

import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.RecordConfig;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class AudioTrackHelper {
    private RecordConfig recordConfig;
    AudioTrack audioTrack;
    int playBufSize, frequency, channel, audioEncoding;
    private int recBufSize;


    public AudioTrackHelper(RecordConfig recordConfig) {
        this.recordConfig = recordConfig;
        frequency = recordConfig.getSampleRate();
        channel = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        audioEncoding = recordConfig.getAudioFormat();
        playBufSize = AudioTrack.getMinBufferSize(frequency, channel, audioEncoding);
        recBufSize = recordConfig.getBufferSizeInBytes();
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                channel, audioEncoding,
                playBufSize, AudioTrack.MODE_STREAM);
        audioTrack.setStereoVolume(0.7f, 0.7f);//设置当前音量大小
    }

    /**
     * 总结起来三步骤
     * audioTrack.play();//开始播放
     * audioTrack.write(tmpBuf, 0, tmpBuf.length);
     * audioTrack.stop2release();
     *
     * @param pcmFile
     * @return
     * @throws IOException
     */

    public AudioTrackHelper playPCM(String pcmFile) throws IOException {
        audioTrack.play();//开始播放
        byte[] buffer = new byte[recBufSize];
        FileInputStream fos = new FileInputStream(pcmFile);// 建立一个可存取字节的文件
        int bufferReadResult;
        while ((bufferReadResult = fos.read(buffer, 0, recBufSize)) != -1) {
            //从MIC保存数据到缓冲区
            byte[] tmpBuf = new byte[bufferReadResult];
            System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
            //写入数据即播放
            audioTrack.write(tmpBuf, 0, tmpBuf.length);
        }
        audioTrack.stop();
        return this;
    }


    public void release() {
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }

    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    public void setAudioTrack(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
    }




}
