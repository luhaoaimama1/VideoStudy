package zone.com.videostudy.codec.utils;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 * todo 并行 callback 自带那种有问题  并且 api需要Lop 所以不用 范例：EncodeSurfaceAsycActivity 最后应该显示125 串行没问题
 * <p>
 * 方便创建而已,
 * <p>
 * MediaCodecHelper
 * .decode/encode
 * .outSurface(surface)
 * .inputSurface（接口在这里付给自己的引用）//
 * .MediaCrypto(MediaCrypto)
 * todo .callback(callback)
 * .sync 同步 /asyc 异步
 * .prepare();
 * .getMediaCodec()
 * <p>
 * .release();
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MediaCodecHelper {

    private static final String TAG = "MediaCodecHelper";
    private MediaCodec mEncoder;
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

    public void prepare() throws IOException {

        //创建一个MediaCodec的实例
        mEncoder = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME));
        //定义这个实例的格式，也就是上面我们定义的format，其他参数不用过于关注
        //第一个参数将我们上面设置的format传进去
        //第二个参数是Surface，如果我们需要读取MediaCodec编码后的数据就要传，但我们这里不需要所以传null
        //第三个参数关于加解密的，我们不需要，传null
        //第四个参数是一个确定的标志位，也就是我们现在传的这个
        mEncoder.configure(format, outputSurface, mediaCrypto,
                isEncode ? MediaCodec.CONFIGURE_FLAG_ENCODE : 0);
        Log.d(TAG, "MediaCodec ->configure");

        if (intputSurface != null) {
            //获取MediaCodec的surface，这个surface其实就是一个入口，
            // 屏幕作为输入源就会进入这个入口，然后交给MediaCodec编码
            intputSurface.onCreate(mEncoder.createInputSurface());
            Log.d(TAG, "created input surface: ");
        }

        mEncoder.start();
        Log.d(TAG, "MediaCodec ->start");
    }

    public void release() {
        if(mEncoder!=null){
            mEncoder.stop();
            Log.d(TAG, "MediaCodec ->stop");
            mEncoder.release();
            Log.d(TAG, "MediaCodec ->release");
            mEncoder=null;
        }
    }


}
