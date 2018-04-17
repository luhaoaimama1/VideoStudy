package zone.com.videostudy.codec.utils;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import and.utils.executor.ExecutorUtils;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 * <p>
 * todo 并行 callback 自带那种有问题  并且 api需要Lop 所以不用 范例：EncodeSurfaceAsycActivity 最后应该显示125 串行没问题
 * <p>
 * 方便创建而已,
 * <p>
 * MediaCodecHelper
 * .decode/encode
 * .outSurface(surface)
 * .inputSurface（接口在这里付给自己的引用）// tips：如果结束标示后 还往surface输入数据，也没什么作用
 * .MediaCrypto(MediaCrypto)
 * todo .callback(callback)
 * .sync 同步 /asyc 异步
 * .prepare(); 返回MediaCodec；
 * <p>
 * .release();
 * <p>
 * // FIXME: 2018/4/17  注意：输入的退出标示，一定要用本类中的方法 signalEndOfInputStream， signalEndOfQueueInputBuffer
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MediaCodecHelper {

    private static final String TAG = "MediaCodecHelper";
    private MediaCodec mediaCodec;
    private boolean isEncode;
    private MediaFormat format;
    private MediaCrypto mediaCrypto;
    private IntputSurface intputSurface;
    private Surface outputSurface;

    private MediaCodecHelper(boolean isEncode, MediaFormat format) {
        this.isEncode = isEncode;
        this.format = format;
    }

    public static MediaCodecHelper encode(MediaFormat format) {
        return new MediaCodecHelper(true, format);
    }

    public static MediaCodecHelper decode(MediaFormat format) {
        return new MediaCodecHelper(false, format);
    }

    public MediaCodecHelper outputSurface(Surface outputSurface) {
        this.outputSurface = outputSurface;
        return this;
    }

    public MediaCodecHelper intputSurface(IntputSurface intputSurface) {
        this.intputSurface = intputSurface;
        return this;
    }

    public interface IntputSurface {
        void onCreate(Surface surface);
    }

    public MediaCodecHelper mediaCrypto(MediaCrypto mediaCrypto) {
        this.mediaCrypto = mediaCrypto;
        return this;
    }

    public MediaCodec getMediaCodec() {
        return mediaCodec;
    }


    public MediaCodecHelper prepare() throws IOException {
        String type;
        //创建一个MediaCodec的实例
        if (isEncode) {
            //todo 去找是否支持
            mediaCodec = MediaCodec.createEncoderByType(type = format.getString(MediaFormat.KEY_MIME));
            log("createEncoder  type:" + type);
        } else {

            mediaCodec = MediaCodec.createDecoderByType(type = format.getString(MediaFormat.KEY_MIME));
            log("createDecoder  type:" + type);
        }
        //定义这个实例的格式，也就是上面我们定义的format，其他参数不用过于关注
        //第一个参数将我们上面设置的format传进去
        //第二个参数是Surface，如果我们需要读取MediaCodec编码后的数据就要传，但我们这里不需要所以传null
        //第三个参数关于加解密的，我们不需要，传null
        //第四个参数是一个确定的标志位 编码为CONFIGURE_FLAG_ENCODE 解码是0
        mediaCodec.configure(format, outputSurface, mediaCrypto,
                isEncode ? MediaCodec.CONFIGURE_FLAG_ENCODE : 0);
        log("configure");

        if (intputSurface != null) {
            //获取MediaCodec的surface，这个surface其实就是一个入口，
            // 屏幕作为输入源就会进入这个入口，然后交给MediaCodec编码
            intputSurface.onCreate(mediaCodec.createInputSurface());
            log("created input surface: ");
        }
        mediaCodec.start();
        log("start");

        if (callback != null) {
            if (intputSurface == null)
                ExecutorUtils.execute(inputRunable);
            ExecutorUtils.execute(outputRunalbe);
        }

        return this;
    }

    private volatile AtomicBoolean isEndOf = new AtomicBoolean(false);

    Runnable inputRunable = new Runnable() {
        @Override
        public void run() {

            while (true) {
                //todo  什么时候退出呢？
                if (isEndOf.get()) {
                    logInput("遇到 退出标示！");
                    break;
                }
                logInput("dequeueInputBuffer before:");
                int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                logInput("inputBufferIndex:" + inputBufferIndex);
                //  == MediaCodec.INFO_TRY_AGAIN_LATER：  请求超时  dequeueOutputBuffer 有超时时间的算
                if (inputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = mediaCodec.getOutputFormat();
                    logInput("encoder input format changed: " + newFormat);
                    callback.onOutputFormatChanged(mediaCodec, newFormat);
                } else if (inputBufferIndex >= 0) {
                    logInput("available inputBufferIndex: " + inputBufferIndex);
                    callback.onInputBufferAvailable(mediaCodec, inputBufferIndex);
                } else {
                    logInput("other.... ");
                }

            }
        }
    };
    Runnable outputRunalbe = new Runnable() {
        @Override
        public void run() {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            while (true) {
                int outputBufIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, -1);
//                int outputBufIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 12000);
                logIOutput("outputBufIndex:" + outputBufIndex);

//              outputBufIndex == MediaCodec.INFO_TRY_AGAIN_LATER：  请求超时  dequeueOutputBuffer 有超时时间的算

                if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = mediaCodec.getOutputFormat();
                    logIOutput("encoder output format changed: " + newFormat);
                    callback.onOutputFormatChanged(mediaCodec, newFormat);
                } else if (outputBufIndex >= 0) {
                    logIOutput("available   outputBufIndex: " + outputBufIndex);
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0;
                        //逻辑不懂的话  看MediaCodec.BUFFER_FLAG_CODEC_CONFIG注释
                        logIOutput("Tip:initialization / codec specific data instead of media data！");
                    }
                    boolean isEndOfStream = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                    callback.onOutputBufferAvailable(mediaCodec, outputBufIndex, bufferInfo, isEndOfStream);
                    //有点数据 但是有一半
                    if (isEndOfStream) {
                        logIOutput("Tip:BUFFER_FLAG_END_OF_STREAM！");
                        checkEndSign();
                        break;
                    }
                } else {
                    logIOutput("other.... ");
                }
            }
        }
    };

    private void checkEndSign() {
        if (!isEndOf.get()) {
            //请使用本类中方法去标示结束  不然输入线程会堵塞，因为read无线等待 并且不知道如何退出while循环
            throw new IllegalStateException("please use method:@ MediaCodecHelper's" + intputSurface != null ?
                    "signalEndOfInputStream" : "signalEndOfQueueInputBuffer");
        }
    }

    private void log(String msg) {
        Log.d(TAG, "MediaCodec -> " + msg);
    }

    private void logInput(String msg) {
        Log.d(TAG, "InputBuffer -> " + msg);
    }

    private void logIOutput(String msg) {
        Log.d(TAG, "OutputBuffer -> " + msg);
    }

    /**
     * 调用的时机,就是输出流遇到 isEndOfStream 。此标示,标示这个流已经是结尾了
     */
    public void release() {
        if(intputSurface==null)
            checkEndSign();
        if (mediaCodec != null) {
            mediaCodec.stop();
            log("stop");
            mediaCodec.release();
            log("release");
            mediaCodec = null;
        }
    }


    private Callback callback;

    public MediaCodecHelper callback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public MediaCodecHelper signalEndOfInputStream() {
        if (intputSurface != null)
            mediaCodec.signalEndOfInputStream();
        else
            throw new IllegalStateException("intputSurface not found  please use method :signalEndOfQueueInputBuffer");
        signalEnd();
        return this;
    }

    private void signalEnd() {
        isEndOf.set(true);
    }

    public MediaCodecHelper signalEndOfQueueInputBuffer(int inputBufferIndex) {
        if (intputSurface == null)
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        else
            throw new IllegalStateException("intputSurface found  please use method :signalEndOfInputStream");
        signalEnd();
        return this;
    }


}
