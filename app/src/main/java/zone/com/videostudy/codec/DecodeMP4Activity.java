package zone.com.videostudy.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import and.utils.data.file2io2data.FileUtils;
import and.utils.data.file2io2data.SDCardUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zone.com.videostudy.R;
import zone.com.videostudy.codec.utils.Callback;
import zone.com.videostudy.codec.utils.ExtractorWrapper;
import zone.com.videostudy.codec.utils.MediaCodecHelper;
import zone.com.videostudy.utils.RawUtils;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 * 参考：https://github.com/jiyangg/MediaPlaySimpleDemo
 * 参考：https://github.com/RrtoyewxXu/AndroidLiveRecord
 */

public class DecodeMP4Activity extends Activity {
    final String MP4NAME = "record_asset.wav";
    File mp4 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP4NAME);
    final String MP5NAME = "record_asset.mp4";
    File mp5 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP5NAME);
    @Bind(R.id.surface)
    SurfaceView surface;
    private ExtractorWrapper videoWrapper, audioWrapper;
    private MediaCodecHelper videoHelper, audioHelper;
    private AudioTrack audioTrack;
    private byte[] mAudioOutTempBuf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_decode_mp4);
        ButterKnife.bind(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                RawUtils.copyFilesFromAssset(DecodeMP4Activity.this,
                        MP4NAME, mp4.getAbsolutePath());
                RawUtils.copyFilesFromAssset(DecodeMP4Activity.this,
                        MP5NAME, mp5.getAbsolutePath());
            }
        }).start();
    }

    @OnClick(R.id.bt_decode)
    public void onViewClicked() {
        initMediaCodec();
        initMediaCodecAudio();
    }
    @OnClick(R.id.bt_decode_quit)
    public void onViewClicked2() {
        videoHelper.forcedQuit();
    }

    public AudioTrack getAudioTrackByFormat(MediaFormat mediaFormat) {
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
        mAudioOutTempBuf = new byte[audioInputBufferSize];
        return audioTrack;
    }

    private void initMediaCodecAudio() {
        try {
            audioWrapper = ExtractorWrapper.getExtractor(mp4.getAbsolutePath(),
                    ExtractorWrapper.AUDIO);
            audioWrapper.extractor.selectTrack(audioWrapper.trackIndex);
            audioTrack = getAudioTrackByFormat(audioWrapper.format);

            audioHelper = MediaCodecHelper.decode(audioWrapper.format)
//                    .outputSurface(surface.getHolder().getSurface())
                    .createByCodecName()
                    .tag("声音")
                    .callback(new Callback() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onInputBufferAvailable(@NonNull MediaCodec codec, int inputBufferIndex) {
                            ByteBuffer byteBuffer = codec.getInputBuffer(inputBufferIndex);
                            int readSize = audioWrapper.extractor.readSampleData(byteBuffer, 0);
                            if (readSize > 0) {
                                Log.d("声音", " input write time:" + audioWrapper.extractor.getSampleTime());
                                codec.queueInputBuffer(inputBufferIndex, 0, readSize
                                        , audioWrapper.extractor.getSampleTime(), 0);
                                audioWrapper.extractor.advance();
                            } else {
                                Log.d("声音", " input end");
                                audioHelper.signalEndOfQueueInputBuffer(inputBufferIndex);

                            }
                        }

                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info, boolean isEndOfStream) {

                            if (info.size > 0) {
                                ByteBuffer byteBuffer = codec.getOutputBuffer(index);
                                byteBuffer.get(mAudioOutTempBuf, info.offset, info.size);
                                audioTrack.write(mAudioOutTempBuf, info.offset, info.size);
                            }
                            codec.releaseOutputBuffer(index, false);

                            //有的话就得释放
                            if (isEndOfStream) {
                                audioWrapper.extractor.unselectTrack(audioWrapper.trackIndex);
                                audioWrapper.release();
                                audioHelper.release();
                                //注意：此时不要释放 因为写入后播放还需要时间。需要播放完毕才能 关闭 不过没找到此接口
//                                audioTrack.stop();
//                                audioTrack.release();
//                                audioTrack = null;
                            }

                        }

                        @Override
                        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                        }
                    })
                    .prepare();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    long startMs;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initMediaCodec() {
        try {
            videoWrapper = ExtractorWrapper.getExtractor(mp5.getAbsolutePath(),
                    ExtractorWrapper.VIDEO);
            videoWrapper.extractor.selectTrack(videoWrapper.trackIndex);
            Log.d("hahaha", " initMediaCodec mime:" + videoWrapper.format.getString(MediaFormat.KEY_MIME));
            startMs = System.currentTimeMillis();
            videoHelper = MediaCodecHelper.decode(videoWrapper.format)
                    .createByCodecName()
                    .timeoutUs(12000)
                    .outputSurface(surface.getHolder().getSurface())
                    .callback(new Callback() {

                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onInputBufferAvailable(@NonNull MediaCodec codec, int inputBufferIndex) {
                            ByteBuffer byteBuffer = codec.getInputBuffer(inputBufferIndex);
                            int readSize = videoWrapper.extractor.readSampleData(byteBuffer, 0);
                            if (readSize >= 0) {
                                Log.d("hahaha", " input write time:" + videoWrapper.extractor.getSampleTime());
                                codec.queueInputBuffer(inputBufferIndex, 0, readSize
                                        , videoWrapper.extractor.getSampleTime(), 0);
                                videoWrapper.extractor.advance();
                            } else {
                                Log.d("hahaha", " input end");
                                videoHelper.signalEndOfQueueInputBuffer(inputBufferIndex);
                            }
                        }

                        @Override
                        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info, boolean isEndOfStream) {
                            //延时操作
                            //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
                            sleepRender(info, startMs);
                            //直接渲染到Surface时使用不到outputBuffer
                            codec.releaseOutputBuffer(index, true);

                            if (isEndOfStream) {
                                videoWrapper.release();
                                videoHelper.release();
                            }
                        }

                        @Override
                        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                        }
                    })
                    .prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //延迟渲染
    private void sleepRender(MediaCodec.BufferInfo audioBufferInfo, long startMs) {
        while (audioBufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        videoHelper.forcedQuit();
        super.onDestroy();
    }
}
