package zone.com.videostudy.record.utils;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * [2017] by Zone
 * <p>
 * todo 这个根本不用封装。 因为他会变,例如轨道
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MediaMuxerExample {

    private static final int TIMEOUT_US = 10000;
    private int videoTrackIndex = -1;
    private boolean muxerStarted = false;
    private AtomicBoolean mQuit = new AtomicBoolean(false);
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    private MediaMuxer mediaMuxer;
    private MediaCodec mediaCodec;
    private Callback callback;

    public MediaMuxerExample(String filePath, AEncoder aEncoder, Callback callback) {
        this.callback = callback;
        this.mediaCodec = aEncoder.getEncoder();
        //第一个参数是输出的地址
        //第二个参数是输出的格式，我们设置的是mp4格式
        try {
            mediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //录制视频不能再主线程 不然anr!
    public void startRecord() {
        //退出了才可以new线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                record();
                if (callback != null)
                    callback.quit();
            }
        }).start();
    }

    public final void quit() {
        mQuit.set(true);
    }

    private void record() {
        while (!mQuit.get()) {
            //dequeueOutputBuffer方法你可以这么理解，它会出列一个输出buffer(你可以理解为一帧画面),返回值是这一帧画面的顺序位置(类似于数组的下标)
            //第二个参数是超时时间，如果超过这个时间了还没成功出列，那么就会跳过这一帧，去出列下一帧，并返回INFO_TRY_AGAIN_LATER标志位
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
            //当格式改变的时候吗，我们需要重新设置格式
            //在本例中，只第一次开始的时候会返回这个值
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat();
            } else if (index >= 0) {//这里说明dequeueOutputBuffer执行正常
                if (!muxerStarted) {
                    throw new IllegalStateException("MediaMuxer dose not call addTrack(format) ");
                }
                //这里执行我们转换成mp4的逻辑
                encodeToVideoTrack(index);
                mediaCodec.releaseOutputBuffer(index, false);
            }
        }
    }
    //这里是将数据传给MediaMuxer，将其转换成mp4
    private void encodeToVideoTrack(int index) {
        //通过index获取到ByteBuffer(可以理解为一帧)
        ByteBuffer encodedData = mediaCodec.getOutputBuffer(index);
        //当bufferInfo返回这个标志位时，就说明已经传完数据了，我们将bufferInfo.size设为0，准备将其回收
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            bufferInfo.size = 0;
        }
        if (bufferInfo.size == 0) {
            encodedData = null;
        }
        if (encodedData != null) {
            encodedData.position(bufferInfo.offset);//设置我们该从哪个位置读取数据
            encodedData.limit(bufferInfo.offset + bufferInfo.size);//设置我们该读多少数据
            //这里将数据写入
            //第一个参数是每一帧画面要放置的顺序
            //第二个是要写入的数据
            //第三个参数是bufferInfo，这个数据包含的是encodedData的offset和size
            mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo);

        }
    }

    //这个方法其实就是设置MediaMuxer的Format
    private void resetOutputFormat() {
        //将MediaCodec的Format设置给MediaMuxer
        MediaFormat newFormat = mediaCodec.getOutputFormat();
        //获取videoTrackIndex，这个值是每一帧画面要放置的顺序
        videoTrackIndex = mediaMuxer.addTrack(newFormat);
        mediaMuxer.start();
        muxerStarted = true;
    }

    public void release() {
        mediaMuxer.stop();
        mediaMuxer.release();
    }

    public interface Callback {
        void quit();
    }

}
