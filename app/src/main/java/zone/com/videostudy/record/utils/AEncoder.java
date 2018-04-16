package zone.com.videostudy.record.utils;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import java.io.IOException;

/**
 * [2017] by Zone
 */

public class AEncoder {

    private static final String TAG = "MediaCodecHelper";

    // parameters for the encoder
    private static String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static int FRAME_RATE = 30; // 30 fps
    private static int IFRAME_INTERVAL = 10; // 10 seconds between I-frames

    private int mBitRate = 2000000;
    private MediaCodec mEncoder;
    private int mHeight, mWidth;

    public AEncoder(int mWidth, int mHeight) {
        this.mWidth = mWidth;
        this.mHeight = mHeight;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public Surface prepareEncoder()  {
        //MediaFormat这个类是用来定义视频格式相关信息的
        //video/avc,这里的avc是高级视频编码Advanced Video Coding
        //mWidth和mHeight是视频的尺寸，这个尺寸不能超过视频采集时采集到的尺寸，否则会直接crash
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);

        //COLOR_FormatSurface这里表明数据将是一个graphicbuffer元数据
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        //设置码率，通常码率越高，视频越清晰，但是对应的视频也越大，这个值我默认设置成了2000000，
        // 也就是通常所说的2M，这已经不低了，如果你不想录制这么清晰的，你可以设置成500000，也就是500k
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        //设置帧率，通常这个值越高，视频会显得越流畅，一般默认我设置成30，你最低可以设置成24，不要低于这个值，低于24会明显卡顿
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);

        ///设置两个关键帧的间隔，这个值你设置成多少对我们这个例子都没啥影响
        //这个值做视频的朋友可能会懂，反正我不是很懂，大概就是你预览的时候，比如你设置为10，那么你10秒内的预览图都是同一张
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        Log.d(TAG, "created video format: " + format);
        //创建一个MediaCodec的实例
        try {
            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //定义这个实例的格式，也就是上面我们定义的format，其他参数不用过于关注
        //第一个参数将我们上面设置的format传进去
        //第二个参数是Surface，如果我们需要读取MediaCodec编码后的数据就要传，但我们这里不需要所以传null
        //第三个参数关于加解密的，我们不需要，传null
        //第四个参数是一个确定的标志位，也就是我们现在传的这个
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        //获取MediaCodec的surface，这个surface其实就是一个入口，
        // 屏幕作为输入源就会进入这个入口，然后交给MediaCodec编码
        Surface mSurface = mEncoder.createInputSurface();
        Log.d(TAG, "created input surface: " + mSurface);
        mEncoder.start();
        return mSurface;
    }

    public void release() {
        mEncoder.stop();
        mEncoder.release();
    }


    public MediaCodec getEncoder() {
        return mEncoder;
    }

}
