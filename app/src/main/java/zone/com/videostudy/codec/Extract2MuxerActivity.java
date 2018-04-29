package zone.com.videostudy.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import zone.com.videostudy.codec.utils.ExtractorWrapper;
import zone.com.videostudy.utils.RawUtils;

/**
 * MIT License
 * todo  mp3格式 解码后编码成 MP4格式 然后通过mux合成
 * <p>
 * 当画面 和 声音 时间录入不一致的时候，
 * 声音时间长，画面保留最后一张图；
 * 画面多出的那块没有声音
 * <p>
 * Copyright (c) [2018] [Zone]
 */

public class Extract2MuxerActivity extends Activity {
    private static final String TAG = "Extract2MuxerActivity";
    final String MP4NAME = "record_asset.mp4";
    File mp4 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP4NAME);

    final String MP42_NAMe = "audio_asset.mp4";
    File mp4_2 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP42_NAMe);
    File muxer = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "muxer_Extract2Muxer.mp4");

    @Bind(R.id.video)
    VideoView videoView;
    private MediaMuxer mMediaMuxer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_muxer);
        ButterKnife.bind(this);
        if (!mp4.exists()&&!mp4_2.exists())
        new Thread(new Runnable() {
            @Override
            public void run() {
                RawUtils.copyFilesFromAssset(Extract2MuxerActivity.this,
                        MP4NAME, mp4.getAbsolutePath());
                RawUtils.copyFilesFromAssset(Extract2MuxerActivity.this,
                        MP42_NAMe, mp4_2.getAbsolutePath());
            }
        }).start();
    }

    @OnClick(R.id.bt_muxer)
    public void onViewClicked() {
        if (!mp4.exists()&&!mp4_2.exists())
            ToastUtils.showShort(this, "文件未保存入sd卡  请稍后!");

        muxerMedia();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void muxerMedia() {
        try {
            mMediaMuxer = new MediaMuxer(muxer.getAbsolutePath(),
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);


            ExtractorWrapper videoWapper = ExtractorWrapper.getExtractor(mp4.getAbsolutePath(), ExtractorWrapper.VIDEO);
            if (videoWapper.trackIndex == -1) {
                ToastUtils.showShort(this, "音频轨道未找到！");
                return;
            }


            ExtractorWrapper audioWarper = ExtractorWrapper.getExtractor(mp4_2.getAbsolutePath(), ExtractorWrapper.AUDIO);
            if (audioWarper.trackIndex == -1) {
                ToastUtils.showShort(this, "音频轨道未找到！");
                return;
            }

            videoWapper.addTrackMuxer(mMediaMuxer);
            audioWarper.addTrackMuxer(mMediaMuxer);

            mMediaMuxer.start();

            addTrack(mMediaMuxer, videoWapper);
            addTrack(mMediaMuxer, audioWarper);


            // 释放MediaMuxer
            mMediaMuxer.stop();
            mMediaMuxer.release();
            playMp4();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    File aac = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "heihei.aac");
    private FileOutputStream fos;
    private BufferedOutputStream bos;

    //todo  增加时间功能
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void addTrack(MediaMuxer mMediaMuxer, ExtractorWrapper warper) {

        warper.extractor.selectTrack(warper.trackIndex);

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        //allocate 初始化容量  有利于扩容
        int maxInputSize = 0;
//        try {
//            //得到ByteBuffer最大值
//            maxInputSize = warper.format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
//        } catch (Exception e) {
        maxInputSize = 500 * 1024;
//        }


//        long frameDuration = warper.format.getLong(MediaFormat.KEY_DURATION); //获取时长
        //代表 某个帧的时间是多少毫秒 仅仅视频的用
//        long framePerUs = 0;
//        int useCount = 0;
        ByteBuffer buffer = ByteBuffer.allocate(maxInputSize);
//
//
//        //获取音频帧时长
//        {
//            warper.extractor.readSampleData(buffer, 0);
//            //skip first sample
//            if (warper.extractor.getSampleTime() == 0)
//                warper.extractor.advance();
//            warper.extractor.readSampleData(buffer, 0);
//            long firstAudioPTS = warper.extractor.getSampleTime();
//            warper.extractor.advance();
//            warper.extractor.readSampleData(buffer, 0);
//            long SecondAudioPTS = warper.extractor.getSampleTime();
//            framePerUs = Math.abs(SecondAudioPTS - firstAudioPTS);
//
//            warper.extractor.unselectTrack(warper.trackIndex);
//            warper.extractor.selectTrack(warper.trackIndex);
//            Log.d(TAG, "AudioSampleTime is " + framePerUs);
//        }

        while (true) {
            int sampleSize = warper.extractor.readSampleData(buffer, 0);
            if (sampleSize < 0)
                break;
            info.offset = 0;
            info.size = sampleSize;
            info.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;

            Log.d(TAG, "write info size: " + sampleSize
                    + " \t presentationTimeUs: " + info.presentationTimeUs
                    + "\t getSampleTime :" + warper.extractor.getSampleTime()
                    + "\t 上一个差值 :" + (warper.extractor.getSampleTime() - info.presentationTimeUs)
            );
            if (warper.isAudio)
                info.presentationTimeUs = warper.extractor.getSampleTime();
            else
                info.presentationTimeUs = warper.extractor.getSampleTime();


//            else{
//                //1秒=1000毫秒  1毫秒=1000微秒   frameRate 为一秒多少帧，所以下面的公式就是 一帧多少微秒
//                //presentationTimeUs为当前的时间戳，代表 某个帧的时间是多少；
////                info.presentationTimeUs += 1000 * 1000 / frameRate; 但是发现frameRate 有时候解析不出来
//
//                //防止第一次getSampleTime不从0开始,不过我没见过~ 用下面的算法
//                switch (useCount) {
//                    case 0:
//                        useCount++;
//                        info.presentationTimeUs=warper.extractor.getSampleTime();
//                        break;
//                    case 1:
//                        framePerUs=warper.extractor.getSampleTime()- info.presentationTimeUs;
//                        useCount++;
//                        info.presentationTimeUs=warper.extractor.getSampleTime();
//                        break;
//                    default:
//
//                        Log.d(TAG, "AudioSampleTime2222 is " + framePerUs);
//                        break;
//                }
//            }

            Log.d(TAG, "write  track  info size: " + info.size
                    + " \t offset: " + info.offset
                    + " \t presentationTimeUs: " + info.presentationTimeUs
            );
            /**
             * 第一个参数是之前添加formate的轨道
             * 第二个是要写入的数据
             * 第三个参数是bufferInfo，包含的是encodedData的offset和size,包含
             */
            mMediaMuxer.writeSampleData(warper.muxTrackIndex, buffer, info);
//            info.presentationTimeUs+=framePerUs;
//            Advance to the next sample
            warper.extractor.advance();
        }
        warper.extractor.unselectTrack(warper.trackIndex);
        warper.extractor.release();
    }


    private void playMp4() {
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(Uri.fromFile(muxer));
        videoView.start();
        videoView.requestFocus();
    }
}
