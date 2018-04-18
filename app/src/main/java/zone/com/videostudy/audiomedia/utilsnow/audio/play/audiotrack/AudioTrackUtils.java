package zone.com.videostudy.audiomedia.utilsnow.audio.play.audiotrack;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaFormat;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class AudioTrackUtils {


    public static AudioTrack getAudioTrackByFormat(MediaFormat mediaFormat) {
        int audioChannels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int audioSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int minBufferSize = AudioTrack.getMinBufferSize(audioSampleRate,
                (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                AudioFormat.ENCODING_PCM_16BIT);
        int maxInputSize = 0;
        try {
            maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int audioInputBufferSize = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;
        int frameSizeInBytes = audioChannels * 2;
        audioInputBufferSize = (audioInputBufferSize / frameSizeInBytes) * frameSizeInBytes;
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                audioSampleRate,
                (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                AudioFormat.ENCODING_PCM_16BIT,
                audioInputBufferSize,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
        return audioTrack;
    }
}
