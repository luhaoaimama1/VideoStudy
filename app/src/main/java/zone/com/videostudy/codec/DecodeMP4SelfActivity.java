package zone.com.videostudy.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceView;

import java.io.File;
import java.nio.ByteBuffer;

import and.utils.data.file2io2data.FileUtils;
import and.utils.data.file2io2data.SDCardUtils;
import and.utils.data.file2io2data.SharedUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zone.com.videostudy.R;
import zone.com.videostudy.codec.utils.Callback;
import zone.com.videostudy.codec.utils.MediaCodecHelper;
import zone.com.videostudy.utils.RawUtils;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class DecodeMP4SelfActivity extends Activity {

    final String MP4NAME = "record_raw.mp4";
    File mp4 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP4NAME);
    @Bind(R.id.surface)
    SurfaceView surface;
    private MediaCodec mediaCodec;
    private Extract2MuxerActivity.ExtractorWrapper wrapper;
    private MediaCodecHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_decode_mp4);
        ButterKnife.bind(this);
        if (!SharedUtils.get("exist", false))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RawUtils.copyFilesFromAssset(DecodeMP4SelfActivity.this, MP4NAME, mp4.getAbsolutePath());
                    SharedUtils.put("exist", true);
                }
            }).start();
    }

    @OnClick(R.id.bt_decode)
    public void onViewClicked() {
        initMediaCodec();
    }

    private void release() {
        wrapper.release();
        helper.release();
        mediaCodec = null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initMediaCodec() {
        try {
            wrapper = Extract2MuxerActivity.ExtractorWrapper.getExtractor(mp4.getAbsolutePath(),
                    Extract2MuxerActivity.ExtractorWrapper.VIDEO);
            wrapper.extractor.selectTrack(wrapper.trackIndex);
            Log.d("hahaha", " initMediaCodec mime:" + wrapper.format.getString(MediaFormat.KEY_MIME));


            helper = MediaCodecHelper.decode(wrapper.format)
                    .outputSurface(surface.getHolder().getSurface())
                    .callback(new Callback() {

                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onInputBufferAvailable(@NonNull MediaCodec codec, int inputBufferIndex) {
                            ByteBuffer byteBuffer = codec.getInputBuffer(inputBufferIndex);
                            int readSize = wrapper.extractor.readSampleData(byteBuffer, 0);
                            if (readSize >= 0) {
                                Log.d("hahaha", " input write time:" + wrapper.extractor.getSampleTime());
                                codec.queueInputBuffer(inputBufferIndex, 0, readSize
                                        , wrapper.extractor.getSampleTime(), 0);
                                wrapper.extractor.advance();
                            } else {
                                Log.d("hahaha", " input end");
                                helper.signalEndOfQueueInputBuffer(inputBufferIndex);
//                                mediaCodec.signalEndOfInputStream();//仅仅是输入流通过surface创建
//                                codec.queueInputBuffer(inputBufferIndex, 0, 0, 0,
//                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            }
                        }

                        @Override
                        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info, boolean isEndOfStream) {
                            Log.d("hahaha", " outputBufferIndex :" + index);
                            if (index > 0) {
                                Log.d("hahaha", " releaseOutputBuffer index:" + index);
//                        mediaCodec.getOutputBuffer(outputBufferIndex);
                                mediaCodec.releaseOutputBuffer(index, true);
                            }
                            if(isEndOfStream)
                                release();
                        }

                        @Override
                        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                        }
                    })
                    .prepare();
            mediaCodec=helper.getMediaCodec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
