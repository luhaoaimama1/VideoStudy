package zone.com.videostudy.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import and.utils.activity_fragment_ui.ToastUtils;
import and.utils.data.file2io2data.FileUtils;
import and.utils.data.file2io2data.SDCardUtils;
import and.utils.data.file2io2data.SharedUtils;
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
 * <p>
 * wav转MP4  ->https://github.com/tqnst/MP4ParserMergeAudioVideo
 * 原文用的 是file读取 而不是提取轨道。
 * 他的关键代码 是i nt bytesRead = fis.read(tempBuffer, 0, dstBuf.limit()); 用limit读取一次不超量
 * ﻿
 * WAV 转成MP4：不需要WAV解码成pcm然后在编码成MP4,直接一个编码MP4即可！
 * 而MP3转MP4： 这个这不能用一个编码器，需要解码后编码才行！ 这里尝试了 失败！
 */
public class WAVtoMp4Activity extends Activity {

    public static final int COMPRESSED_AUDIO_FILE_BIT_RATE = 64000; // 64kbps
    public static final int SAMPLING_RATE = 48000;
    private static final String LOGTAG = "gagaga";
    @Bind(R.id.video)
    VideoView videoView;
    @Bind(R.id.bt_muxer)
    Button btMuxer;
    private int channelCount = 1;


    final String MP3NAMe = "record_asset.wav";
    File mp3 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP3NAMe);
    //    File mp3 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "test_raw.mp3");
    File muxer = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "muxer_WAVtoMp4.mp4");
    private MediaMuxer mMediaMuxer;
    private int audioTrackIdx;
    private ExtractorWrapper audioWarper;
    private MediaCodecHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_muxer);
        ButterKnife.bind(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                RawUtils.copyFilesFromAssset(WAVtoMp4Activity.this,
                        MP3NAMe, mp3.getAbsolutePath());
            }
        }).start();
    }

    @NonNull
    private MediaFormat getFormat() {
        MediaFormat mediaFormat = new MediaFormat();
        mediaFormat.setString("mime", "audio/mp4a-latm");
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLING_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);// AAC-HE // 64kbps
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        int minBufferSize = AudioTrack.getMinBufferSize(SAMPLING_RATE,
                (channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                AudioFormat.ENCODING_PCM_16BIT);
        int maxInputSize = 0;
        try {
            maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize == 0 ? minBufferSize * 4 : maxInputSize);
        return mediaFormat;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @OnClick(R.id.bt_muxer)
    public void onViewClicked() {
        MediaFormat mediaFormat = getFormat();
        try {
            mMediaMuxer = new MediaMuxer(muxer.getAbsolutePath(),
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            audioWarper = ExtractorWrapper.getExtractor(mp3.getAbsolutePath()
                    , ExtractorWrapper.AUDIO);
            audioWarper.extractor.unselectTrack(audioWarper.trackIndex);
            audioWarper.extractor.selectTrack(audioWarper.trackIndex);
            if (audioWarper.trackIndex == -1) {
                ToastUtils.showShort(this, "音频轨道未找到！");
                return;
            }

            helper = MediaCodecHelper.encode(mediaFormat)
                    .createByCodecName()
                    .callback(new Callback() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onInputBufferAvailable(@NonNull MediaCodec codec, int inputBufferIndex) {
                            ByteBuffer byteBuffer = codec.getInputBuffer(inputBufferIndex);
                            int readSize = audioWarper.extractor.readSampleData(byteBuffer, 0);
                            if (readSize >= 0) {
                                Log.d("hahaha", " input write time:" + audioWarper.extractor.getSampleTime());
                                codec.queueInputBuffer(inputBufferIndex, 0, readSize
                                        , audioWarper.extractor.getSampleTime(), 0);
                                audioWarper.extractor.advance();
                            } else {
                                Log.d("hahaha", " input end");
                                helper.signalEndOfQueueInputBuffer(inputBufferIndex);
                            }

                        }

                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
                                                            @NonNull MediaCodec.BufferInfo bufferInfo,
                                                            boolean isEndOfStream) {
                            Log.d("MediaCodecHelper", "处理数据 outputBufIndex" + index);
                            ByteBuffer encodedData = codec.getOutputBuffer(index);
                            if (bufferInfo.size != 0) {
                                //todo  这里不需要写   如果他为了防止 bufferInfo.offset未设置 可以在bufferInfo里设置
                                Log.d("MediaCodecHelper", "处理数据 --时间戳:" + bufferInfo.presentationTimeUs);
                                mMediaMuxer.writeSampleData(audioTrackIdx, encodedData, bufferInfo);
                            }
                            codec.releaseOutputBuffer(index, false);
                            //有点数据 但是有一半
                            if (isEndOfStream) {
                                helper.release();
                                mMediaMuxer.stop();
                                mMediaMuxer.release();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playMp4();
                                    }
                                });
                            }

                        }

                        @Override
                        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                            format = codec.getOutputFormat();
                            Log.v(LOGTAG, "Output format changed - " + format);
                            audioTrackIdx = mMediaMuxer.addTrack(format);
                            mMediaMuxer.start();
                        }
                    })
                    .prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playMp4() {
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(Uri.fromFile(muxer));
        videoView.start();
        videoView.requestFocus();
    }
}
