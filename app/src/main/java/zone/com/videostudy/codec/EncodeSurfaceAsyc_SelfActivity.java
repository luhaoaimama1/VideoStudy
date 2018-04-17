package zone.com.videostudy.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;
import android.widget.MediaController;
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
import zone.com.videostudy.codec.utils.Callback;
import zone.com.videostudy.codec.utils.MediaCodecHelper;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */
public class EncodeSurfaceAsyc_SelfActivity extends Activity {


    @Bind(R.id.video)
    VideoView videoView;
    @Bind(R.id.bt_muxer)
    Button btMuxer;
    File muxerFile = FileUtils.getFile(SDCardUtils.getSDCardDir(),
            "VideoStudyHei", "encode_sufacer_mux.mp4");
    private Surface surface;
    private MediaCodec mediaCodec;
    private MediaMuxer mMediaMuxer;
    private int writeTrackIndex;
    private MediaCodecHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_muxer);
        ButterKnife.bind(this);
        btMuxer.setText("开始编码surface!");
    }

    Paint paint;
    int frameIndex = 0;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @OnClick(R.id.bt_muxer)
    public void onViewClicked() {
        init();
        new Thread(new Runnable() {
            @Override
            public void run() {
                frameIndex = 0;
                while (true) {
                    //绘图
                    renderFromSource(frameIndex);
                    //解码
                    // 因为我就想录制5秒  5*25 =125
                    if (computePresentationTimeMs(++frameIndex) > 5 * 1000 * 1000)
                        break;
                }
                //结尾编码
                helper.signalEndOfInputStream();
//                mediaCodec.signalEndOfInputStream();
//                count++;
//                release();
            }

        }).start();

    }

//    int count=0;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void release() {
//        if(count<2)
//            return;
        Log.d("MediaCodecHelper", "release------------------------------------------>");

        if (mMediaMuxer != null) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
        }

        if (surface != null) {
            surface.release();
            surface = null;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playMp4();
            }
        });
    }

    private void playMp4() {
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(Uri.fromFile(muxerFile));
        videoView.start();
        videoView.requestFocus();
    }

    private void renderFromSource(int frameIndex) {
        Canvas canvas = surface.lockCanvas(null);
        renderFrame(canvas, frameIndex);
        //绘制画布
        surface.unlockCanvasAndPost(canvas);
    }

    private void renderFrame(Canvas canvas, int frameIndex) {
        // 绘制背景
        paint.setColor(Color.GREEN);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        //绘制数字
        paint.setColor(Color.WHITE);
        canvas.drawText(frameIndex + "", canvas.getWidth() / 2, canvas.getHeight() / 2, paint);
    }

    private long computePresentationTimeMs(int frameIndex) {
        return frameIndex * 1000 * 1000 / 25;
    }

    private void init() {
        //初始化编码器
        initMediaCodec();
        initMuxer();
        initPaint();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initMuxer() {
        try {
            mMediaMuxer = new MediaMuxer(muxerFile.getAbsolutePath(),
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(50);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initMediaCodec() {
        try {
            //MediaFormat这个类是用来定义视频格式相关信息的
            //video/avc,这里的avc是高级视频编码Advanced Video Coding
            //mWidth和mHeight是视频的尺寸，这个尺寸不能超过视频采集时采集到的尺寸，否则会直接crash
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 640, 480);
            //COLOR_FormatSurface这里表明数据将是一个graphicbuffer元数据
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            //设置码率，通常码率越高，视频越清晰，但是对应的视频也越大，这个值我默认设置成了2000000，
            // 也就是通常所说的2M，这已经不低了，如果你不想录制这么清晰的，你可以设置成500000，也就是500k
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1250000);
            ///设置两个关键帧的间隔，这个值你设置成多少对我们这个例子都没啥影响
            //这个值做视频的朋友可能会懂，反正我不是很懂，大概就是你预览的时候，比如你设置为10，那么你10秒内的预览图都是同一张
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
            //设置帧率，通常这个值越高，视频会显得越流畅，一般默认我设置成30，你最低可以设置成24，不要低于这个值，低于24会明显卡顿
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);


            helper= MediaCodecHelper.encode(mediaFormat)
                    .intputSurface(new MediaCodecHelper.IntputSurface() {
                        @Override
                        public void onCreate(Surface surface2) {
                            surface = surface2;
                        }
                    })
                    .callback(new Callback() {
                        @Override
                        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

                        }

                        @Override
                        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
                                                            @NonNull MediaCodec.BufferInfo bufferInfo
                                , boolean isEndOfStream) {
                            Log.d("MediaCodecHelper", "处理数据 --frame:" + frameIndex + "\t outputBufIndex" + index);
                            ByteBuffer encodedData = codec.getOutputBuffer(index);
                            if (bufferInfo.size != 0) {
                                //todo  这里不需要写   如果他为了防止 bufferInfo.offset未设置 可以在bufferInfo里设置
                                Log.d("MediaCodecHelper", "处理数据 --时间戳:" + bufferInfo.presentationTimeUs);
                                mMediaMuxer.writeSampleData(writeTrackIndex, encodedData, bufferInfo);
                            }
                            mediaCodec.releaseOutputBuffer(index, false);
                            //有点数据 但是有一半
                            if (isEndOfStream){
                                helper.release();
                                release();
                            }
                        }

                        @Override
                        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                            MediaFormat newFormat = mediaCodec.getOutputFormat();
                            Log.d("MediaCodecHelper", "onOutputFormatChanged?: " + newFormat);
                            writeTrackIndex = mMediaMuxer.addTrack(newFormat);
                            mMediaMuxer.start();
                        }
                    }).prepare();
            mediaCodec=helper.getMediaCodec();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
