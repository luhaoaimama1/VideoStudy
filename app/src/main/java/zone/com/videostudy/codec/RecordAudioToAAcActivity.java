package zone.com.videostudy.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import and.utils.data.file2io2data.FileUtils;
import and.utils.data.file2io2data.SDCardUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zone.com.videostudy.R;
import zone.com.videostudy.audiomedia.utilsnow.audio.play.audiotrack.AudioTrackHelper;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecorder;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.Process;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecordConfig;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process.FileProcess;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process.VolumeProcess;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process.WavFileProcess;
import zone.com.videostudy.codec.utils.Callback;
import zone.com.videostudy.codec.utils.MediaCodecHelper;

public class RecordAudioToAAcActivity extends Activity {


    @Bind(R.id.bt_volume)
    TextView btVolume;
    private AudioRecorder mAudioRecorder = new AudioRecorder();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private MediaMuxer mMediaMuxer;
    private MediaCodecHelper helper;
    private int muxIndex;
    @Bind(R.id.video)
    VideoView videoView;

    File muxer = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "muxer_RecordAudioToAAc.mp4");
    File file = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "record.pcm_");
    File wav = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "record.wav");

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.a_record_audio);
        ButterKnife.bind(this);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.bt_start, R.id.bt_end, R.id.bt_start_record, R.id.bt_start_AudioTrack})
    public void onViewClicked(View view) {


        switch (view.getId()) {
            case R.id.bt_start:
                initMediaCode();
                mAudioRecorder
                        .process(
                                new WavFileProcess(wav.getAbsolutePath()),
//                                new AudioTrackProcess(),
                                new FileProcess(file.getAbsolutePath()),
                                new Process() {
                                    @Override
                                    public void init(AudioRecorder audioRecorder) throws Exception {
                                    }

                                    @Override
                                    public int processData(AudioRecorder audioRecord, byte[] pcmData, int readsize) throws Exception {
                                        if (readsize > 0) {
                                            feedData(pcmData, readsize);
                                        }
                                        return readsize;
                                    }


                                    @Override
                                    public void end() throws Exception {
                                        feedData(null, -1);
                                    }

                                    @Override
                                    public void release() throws Exception {

                                    }
                                },
                                new VolumeProcess(new VolumeProcess.VolumeListener() {
                                    @Override
                                    public void onVolume(final int volume, int maxVolume) {
                                        System.out.println("volume:" + volume + "\t maxVolume:" + maxVolume);
                                        btVolume.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                btVolume.setText("音量：" + volume);
                                            }
                                        });

                                    }
                                }))
                        .start();

                break;
            case R.id.bt_end:
                mAudioRecorder.stop();
                break;
            case R.id.bt_start_AudioTrack:
                mAudioRecorder.addOnStopRunnable(new Runnable() {
                    @Override
                    public void run() {
                        audioTrack_();
                    }
                });

                break;
            case R.id.bt_start_record:
                mAudioRecorder.addOnStopRunnable(new Runnable() {
                    @Override
                    public void run() {
                        mediaPlay_();
                    }
                });
                break;
        }

    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initMediaCode() {
        try {
            mMediaMuxer = new MediaMuxer(muxer.getAbsolutePath(),
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            helper = MediaCodecHelper.encode(getFormat())
                    .createByCodecName()
                    .callback(false, true, new Callback() {
                        @Override
                        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

                        }

                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo bufferInfo, boolean isEndOfStream) {

                            if (isEndOfStream) {
                                mMediaMuxer.stop();
                                mMediaMuxer.release();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playMp4();
                                    }
                                });

                            } else {
                                ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                                outputBuffer.position(bufferInfo.offset);
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                                mMediaMuxer.writeSampleData(muxIndex, outputBuffer, bufferInfo);
                                // 处理结束，释放输出缓存区资源
                                codec.releaseOutputBuffer(index, false);
                            }


                        }

                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                        @Override
                        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                            muxIndex = mMediaMuxer.addTrack(format);
                            mMediaMuxer.start();
                        }
                    })
                    .prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void feedData(byte[] pcmData, int readsize) {
        MediaCodec codec2 = helper.getMediaCodec();
        Log.d("helper___3333", " helper dequeueInputBuffer.....");
        int inputBufferIndex = codec2.dequeueInputBuffer(-1);
        Log.d("helper___3333", " helper dequeueInputBuffer inputBufferIndex :" + inputBufferIndex);
        if (inputBufferIndex >= 0) {
            //把取出空InputBuffer的写入
            ByteBuffer inputBuffer = codec2.getInputBuffer(inputBufferIndex);
            // 向输入缓存区写入有效原始数据，并提交到编码器中进行编码处理
            if (readsize <= 0) {
                helper.signalEndOfQueueInputBuffer(inputBufferIndex);
            } else {
                inputBuffer.clear();
                inputBuffer.put(pcmData);
                codec2.queueInputBuffer(inputBufferIndex, 0, readsize, getPTSUs(), 0);
            }
        }
    }

    private long prevPresentationTimes = 0;

    private long getPTSUs() {
        long result = System.nanoTime() / 1000;
        if (result < prevPresentationTimes) {
            result = (prevPresentationTimes - result) + result;
        }
        Log.d("helper___3333", " getPTSUs :" + result);
        return result;
    }

    private MediaFormat getFormat() {
        AudioRecordConfig config = mAudioRecorder.getAudioRecordConfig();
        MediaFormat mediaFormat = new MediaFormat();
        mediaFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
//        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, config.getChannelConfig());
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, config.getSampleRate());
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);// AAC-HE // 64kbps
//        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 16000);// AAC-HE // 64kbps
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, config.getMinBufferSizeAR() * 5);
        return mediaFormat;
    }

    private void mediaPlay_() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();//重置为初始状态
        }
        try {
            mediaPlayer.setDataSource(wav.getAbsolutePath());
            mediaPlayer.prepare();//缓冲
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        mediaPlayer.start();//开始或恢复播放
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.reset();
            }
        });
    }

    private void audioTrack_() {
        try {
            audioTrack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void audioTrack() throws Exception {
        new AudioTrackHelper(mAudioRecorder.getAudioRecordConfig())
                .playPCM(file.getAbsolutePath())
                .release();
    }

    private void playMp4() {
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(Uri.fromFile(muxer));
        videoView.start();
        videoView.requestFocus();
    }


}
