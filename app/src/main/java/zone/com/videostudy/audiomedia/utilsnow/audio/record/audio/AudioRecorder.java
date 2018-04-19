package zone.com.videostudy.audiomedia.utilsnow.audio.record.audio;
import android.media.AudioRecord;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by liming on 15/9/9.
 * 介绍AudioRecorder 规范 http://www.cnblogs.com/ct2011/p/4080193.html
 * Tip:  <uses-permission android:name="android.permission.RECORD_AUDIO"></uses-permission>
 * 构造器参数很多，我们一点一点来看：
 * <p>
 * audioSource : 声源，一般使用MediaRecorder.AudioSource.MIC表示来自于麦克风
 * sampleRateInHz ：官方明确说到只有44100Hz是所有设备都支持的。其他22050、16000和11025只能在某些设备上使用。
 * channelConfig ： 有立体声（CHANNEL_IN_STEREO）和单声道（CHANNEL_IN_MONO）两种。但只有单声道（CHANNEL_IN_MONO）是所有设备都支持的。
 * audioFormat ： 有ENCODING_PCM_16BIT和ENCODING_PCM_8BIT两种音频编码格式。同样的，官方声明只有ENCODING_PCM_16BIT是所有设备都支持的。
 * bufferSizeInBytes ： 录音期间声音数据的写入缓冲区大小（单位是字节）。
 * 其实从上面的解释可以看到，类的参数很多，但为了保证在所有设备上可以使用，
 * 我们真正需要填写的只有一个参数：bufferSizeInBytes，其他都可以使用通用的参数而不用自己费心来选择。
 */
public class AudioRecorder {

    private static final String TAG = "AudioRecorder";

    private AudioRecord audioRecord;
    private AtomicBoolean mQuit;
    private boolean isRest = true;

    private AudioRecordConfig audioRecordConfig;

    public AudioRecorder(AudioRecordConfig audioRecordConfig) {
        this.audioRecordConfig = audioRecordConfig;
    }

    public AudioRecorder() {
        this(new AudioRecordConfig());
    }

    protected void prepareAudioRecord() {
        // 创建AudioRecord对象
        audioRecord = new AudioRecord(audioRecordConfig.getAudioSource(), audioRecordConfig.getSampleRate(),
                audioRecordConfig.getChannelConfig(), audioRecordConfig.getAudioFormat(), audioRecordConfig.getMinBufferSizeAR());
    }

    public AudioRecorder start() {
        if (isRest) {
            isRest = false;
            mQuit = new AtomicBoolean(false);
            if (audioRecord == null)
                prepareAudioRecord();
            audioRecord.startRecording();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        writeDateTOFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        release();
                    }
                }
            }).start();
        }
        return this;
    }


    public Process[] mProcess;

    public AudioRecorder process(Process... process) {
        this.mProcess = process;
        return this;
    }

    private void writeDateTOFile() throws Exception {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] pcmData = new byte[audioRecordConfig.getMinBufferSizeAR()];

        int readsize = 0;
        for (Process mProces : mProcess)
            mProces.init(AudioRecorder.this);

        while (!mQuit.get()) {
            readsize = audioRecord.read(pcmData, 0, audioRecordConfig.getMinBufferSizeAR());
            for (Process mProces : mProcess)
                readsize = mProces.processData(AudioRecorder.this, pcmData, readsize);
        }
        for (Process mProces : mProcess)
            mProces.end();
    }


    private void release() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();//释放资源
            audioRecord = null;
            isRest = true;
            mQuit.set(true);
            for (Process mProces : mProcess)
                try {
                    mProces.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            for (Runnable listener : onStopRunnableList) {
                listener.run();
                onStopRunnableList.remove(listener);
            }
        }

    }

    CopyOnWriteArrayList<Runnable> onStopRunnableList = new CopyOnWriteArrayList<>();

    /**
     * stop2release task
     */
    public final void stop() {
        if (!isRest && mQuit.get() == false)
            mQuit.set(true);
    }

    public void addOnStopRunnable(Runnable runnable) {
        if (isRest && mQuit.get())
            runnable.run();
        else {
            onStopRunnableList.add(runnable);
        }
    }

    public AudioRecordConfig getAudioRecordConfig() {
        return audioRecordConfig;
    }

    public void setAudioRecordConfig(AudioRecordConfig audioRecordConfig) {
        this.audioRecordConfig = audioRecordConfig;
    }


}
