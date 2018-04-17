package zone.com.videostudy.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
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
import zone.com.videostudy.codec.utils.MediaCodecHelper;
import zone.com.videostudy.utils.RawUtils;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class DecodeMP4Activity extends Activity {

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
                    RawUtils.copyFilesFromAssset(DecodeMP4Activity.this, MP4NAME, mp4.getAbsolutePath());
                    SharedUtils.put("exist", true);
                }
            }).start();
    }

    @OnClick(R.id.bt_decode)
    public void onViewClicked() {
        initMediaCodec();
        new Thread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                MediaCodec.BufferInfo bufferInfo=new MediaCodec.BufferInfo();
                boolean inputWrite = true;
                while (true) {
                    if (inputWrite) {
                        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                        Log.d("hahaha"," input inputBufferIndex:"+inputBufferIndex);
                        if (inputBufferIndex >=0) {
                            ByteBuffer byteBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                            int readSize = wrapper.extractor.readSampleData(byteBuffer, 0);
                            if (readSize >= 0) {
                                Log.d("hahaha"," input write time:"+ wrapper.extractor.getSampleTime());
                                mediaCodec.queueInputBuffer(inputBufferIndex, 0, readSize
                                        , wrapper.extractor.getSampleTime(), 0);
                                wrapper.extractor.advance();
                            } else {
                                Log.d("hahaha"," input end");
//                                mediaCodec.signalEndOfInputStream();//仅仅是输入流通过surface创建
                            mediaCodec.queueInputBuffer(inputBufferIndex,0,0,0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                inputWrite=false;
                            }
                        }
                    }

                    int outputBufferIndex =mediaCodec.dequeueOutputBuffer(bufferInfo,12000);
                    Log.d("hahaha"," outputBufferIndex :"+outputBufferIndex);
                    if(outputBufferIndex>0){
                        Log.d("hahaha"," releaseOutputBuffer index:"+outputBufferIndex);
//                        mediaCodec.getOutputBuffer(outputBufferIndex);
                        mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    }
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.d("hahaha"," output end");
                        break;
                    }

                }
                release();
            }
        }).start();
    }

    private void release() {
        wrapper.release();
        helper.release();
        mediaCodec=null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initMediaCodec() {
        try {
            wrapper = Extract2MuxerActivity.ExtractorWrapper.getExtractor(mp4.getAbsolutePath(),
                    Extract2MuxerActivity.ExtractorWrapper.VIDEO);
            wrapper.extractor.selectTrack(wrapper.trackIndex);
            Log.d("hahaha"," initMediaCodec mime:"+ wrapper.format.getString(MediaFormat.KEY_MIME));


            helper=MediaCodecHelper.decode(wrapper.format)
                    .outputSurface( surface.getHolder().getSurface())
                    .prepare();
            mediaCodec=helper.getMediaCodec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
