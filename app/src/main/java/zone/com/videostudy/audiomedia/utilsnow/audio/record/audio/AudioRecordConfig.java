package zone.com.videostudy.audiomedia.utilsnow.audio.record.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaFormat;
import android.media.MediaRecorder;

/* <pre>
*       录音的配置信息 默认配置为16K采样率 单通道 16位
*      audioSource = MediaRecorder.AudioSource.MIC;
*      sampleRate = SAMPLE_RATE_16K_HZ;
*      channelConfig = AudioFormat.CHANNEL_IN_MONO;
*      audioFormat = AudioFormat.ENCODING_PCM_16BIT;
* </pre>
*/
public class AudioRecordConfig {
    public static final int SAMPLE_RATE_44K_HZ = 44100;
    public static final int SAMPLE_RATE_22K_HZ = 22050;
    public static final int SAMPLE_RATE_16K_HZ = 16000;
    public static final int SAMPLE_RATE_11K_HZ = 11025;
    public static final int SAMPLE_RATE_8K_HZ = 8000;
    // AudioRecord获取缓冲区大小
    private int bufferSizeInAR = -1;
    // AudioTrack 获取缓冲区大小
    private int bufferSizeInAT = -1;
    //k 缓冲区根据最小计算的最大 比率
    private  static final int bufferMaxRadio = 5;
    // 音频获取源
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private int sampleRate = SAMPLE_RATE_44K_HZ;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;


    /**
     * 录音配置的构造方法
     *
     * @param audioSource   the recording source.
     *                      See {@link MediaRecorder.AudioSource} for the recording source definitions.
     *                      recommend {@link MediaRecorder.AudioSource#MIC}
     * @param sampleRate    the sample rate expressed in Hertz. {@link AudioRecordConfig#SAMPLE_RATE_44K_HZ} is Recommended ,
     * @param channelConfig describes the configuration of the audio channels.
     *                      See {@link AudioFormat#CHANNEL_IN_MONO} and
     *                      {@link AudioFormat#CHANNEL_IN_STEREO}.  {@link AudioFormat#CHANNEL_IN_MONO} is guaranteed
     *                      to work on all devices.
     * @param audioFormat   the format in which the audio data is to be returned.
     *                      See {@link AudioFormat#ENCODING_PCM_8BIT}, {@link AudioFormat#ENCODING_PCM_16BIT},
     *                      and {@link AudioFormat#ENCODING_PCM_FLOAT}. @link RecordConfig#SAMPLE_RATE_22K_HZ},@link RecordConfig#SAMPLE_RATE_16K_HZ},@link RecordConfig#SAMPLE_RATE_11K_HZ},@link RecordConfig#SAMPLE_RATE_8K_HZ}
     *                      {@link AudioFormat#SAMPLE_RATE_UNSPECIFIED} means to use a route-dependent value
     *                      which is usually the sample rate of the source.
     */
    public AudioRecordConfig(int audioSource, int sampleRate, int channelConfig, int audioFormat) {
        init(audioSource, sampleRate, channelConfig, audioFormat);
    }

    private void init(int audioSource, int sampleRate, int channelConfig, int audioFormat) {
        this.audioSource = audioSource;
        this.sampleRate = sampleRate;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
    }

    /**
     * audioSource,audioFormat 采取默认的
     * @param mediaFormat
     */
    public void init(MediaFormat mediaFormat) {
        int channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        this.sampleRate =mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        this.channelConfig = getChannelConfig(channelCount);
    }

    /**
     * 录音配置的构造方法
     */
    public AudioRecordConfig() {
        init(audioSource, sampleRate, channelConfig, audioFormat);
    }

    public int getAudioSource() {
        return audioSource;
    }

    /**
     * @param audioSource the recording source.
     *                    See {@link MediaRecorder.AudioSource} for the recording source definitions.
     *                    recommend {@link MediaRecorder.AudioSource#MIC}
     */
    public AudioRecordConfig setAudioSource(int audioSource) {
        this.audioSource = audioSource;
        return this;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * @param sampleRate the sample rate expressed in Hertz. {@link AudioRecordConfig#SAMPLE_RATE_44K_HZ} is Recommended ,
     * @link RecordConfig#SAMPLE_RATE_22K_HZ},@link RecordConfig#SAMPLE_RATE_16K_HZ},@link RecordConfig#SAMPLE_RATE_11K_HZ},@link RecordConfig#SAMPLE_RATE_8K_HZ}
     * {@link AudioFormat#SAMPLE_RATE_UNSPECIFIED} means to use a route-dependent value
     * which is usually the sample rate of the source.
     */
    public AudioRecordConfig setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public int getChannelCount() {
        int channels;
        switch (channelConfig) {
            case AudioFormat.CHANNEL_IN_MONO:
                channels = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                channels = 2;
                break;
            default:
                throw new IllegalStateException("内置无法处理此格式");
        }
        return channels;
    }
    private int getChannelConfig(int channelCount) {
        int channels;
        switch (channelCount) {
            case 1:
                channels = AudioFormat.CHANNEL_IN_MONO;
                break;
            case 2:
                channels = AudioFormat.CHANNEL_IN_STEREO;
                break;
            default:
                throw new IllegalStateException("内置无法处理此格式");
        }
        return channels;
    }

    /**
     * @param channelConfig describes the configuration of the audio channels.
     *                      See {@link AudioFormat#CHANNEL_IN_MONO} and
     *                      {@link AudioFormat#CHANNEL_IN_STEREO}.  {@link AudioFormat#CHANNEL_IN_MONO} is guaranteed
     *                      to work on all devices.
     */
    public AudioRecordConfig setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
        return this;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    /**
     * @param audioFormat the format in which the audio data is to be returned.
     *                    See {@link AudioFormat#ENCODING_PCM_8BIT}, {@link AudioFormat#ENCODING_PCM_16BIT},
     *                    and {@link AudioFormat#ENCODING_PCM_FLOAT}.
     */
    public AudioRecordConfig setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
        return this;
    }

    public int getMinBufferSizeAR() {
        if (bufferSizeInAR == -1)
            // 获得缓冲区字节大小
            return bufferSizeInAR = AudioRecord.getMinBufferSize(sampleRate,
                    channelConfig, audioFormat);
        return bufferSizeInAR;
    }

    public void setBufferSizeInAR(int bufferSizeInAR) {
        this.bufferSizeInAR = bufferSizeInAR;
    }

    public void setBufferSizeInAT(int bufferSizeInAT) {
        this.bufferSizeInAT = bufferSizeInAT;
    }

    public int getMinBufferSizeAT() {
        if (bufferSizeInAT == -1)
            return bufferSizeInAT = AudioTrack.getMinBufferSize(sampleRate,
                    channelConfig, audioFormat);
        return bufferSizeInAT;
    }

    public int getMaxBufferSizeAT() {
        return getMinBufferSizeAT()*bufferMaxRadio;
    }
    public int getMaxBufferSizeAR() {
        return getMinBufferSizeAT()*bufferMaxRadio;
    }

}