package zone.com.videostudy.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import zone.com.videostudy.utils.RawUtils;

/**
 * MIT License
 * todo  mp3格式 解码后编码成 MP4格式 然后通过mux合成
 * Copyright (c) [2018] [Zone]
 */

public class Extract2MuxerActivity extends Activity {
    private static final String TAG = "Extract2MuxerActivity";
    final String MP4NAME = "record_raw.mp4";
//    final String MP3NAMe = "mkj.mp3";
    final String MP3NAMe = "record.wav";
//    final String MP3NAMe = "test_raw.mp3";
    File mp4 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP4NAME);
    File mp3 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP3NAMe);
    File muxer = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "muxerFile.mp4");

    @Bind(R.id.video)
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_muxer);
        ButterKnife.bind(this);
        SharedUtils.put("exist", false);
        if (!SharedUtils.get("exist", false))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RawUtils.copyFilesFromAssset(Extract2MuxerActivity.this, MP4NAME, mp4.getAbsolutePath());
                    RawUtils.copyFilesFromAssset(Extract2MuxerActivity.this, MP3NAMe, mp3.getAbsolutePath());
                    SharedUtils.put("exist", true);
                }
            }).start();
    }

    @OnClick(R.id.bt_muxer)
    public void onViewClicked() {
        if (!SharedUtils.get("exist", false))
            ToastUtils.showShort(this, "文件未保存入sd卡");
        muxerMedia();
        playMp4();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void muxerMedia() {
        try {
            MediaMuxer mMediaMuxer = new MediaMuxer(muxer.getAbsolutePath(),
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);


            ExtractorWrapper videoWapper = ExtractorWrapper.getExtractor(mp4.getAbsolutePath(), ExtractorWrapper.VIDEO);
            if (videoWapper.trackIndex == -1) {
                ToastUtils.showShort(this, "音频轨道未找到！");
                return;
            }


            ExtractorWrapper audioWarper = ExtractorWrapper.getExtractor(mp3.getAbsolutePath(), ExtractorWrapper.AUDIO);
            if (audioWarper.trackIndex == -1) {
                ToastUtils.showShort(this, "音频轨道未找到！");
                return;
            }

//            videoWapper.addTrackMuxer(mMediaMuxer);
            audioWarper.addTrackMuxer(mMediaMuxer);
            mMediaMuxer.start();

//            addTrack(mMediaMuxer, videoWapper);
            addTrack(mMediaMuxer, audioWarper);

            // 释放MediaMuxer
            mMediaMuxer.stop();
            mMediaMuxer.release();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static class ExtractorWrapper {
        public static final String VIDEO = "video/";
        public static final String AUDIO = "audio/";

        public boolean isAudio;
        public MediaExtractor extractor;
        public int trackIndex = -1;
        public int muxTrackIndex = -1;
        public MediaFormat format;

        private ExtractorWrapper() {
        }

        public void addTrackMuxer(MediaMuxer muxer) {
            muxTrackIndex = muxer.addTrack(format);
        }

        public static ExtractorWrapper getExtractor(String filePath, String contains) throws IOException {
            ExtractorWrapper extractorWrapper = new ExtractorWrapper();
            // 视频的MediaExtractor
            MediaExtractor mVideoExtractor = new MediaExtractor();
            extractorWrapper.extractor = mVideoExtractor;
            mVideoExtractor.setDataSource(filePath);
            for (int i = 0; i < mVideoExtractor.getTrackCount(); i++) {
                MediaFormat format = mVideoExtractor.getTrackFormat(i);
//查找是否支持编解码功能                new MediaCodecList(1).findDecoderForFormat(warper.format)
                if (format.getString(MediaFormat.KEY_MIME).contains(contains)) {
                    extractorWrapper.format = format;
                    extractorWrapper.trackIndex = i;
                    extractorWrapper.isAudio = format.getString(MediaFormat.KEY_MIME).contains(AUDIO);
                    break;
                }
            }
            return extractorWrapper;
        }

    }


    private void playMp4() {
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(Uri.fromFile(muxer));
        videoView.start();
        videoView.requestFocus();
    }
}
