package zone.com.videostudy.codec.utils;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;

/**
 * MediaCodec callback interface. Used to notify the user asynchronously
 * of various MediaCodec events.
 *
 *
 */
public interface Callback {

    /**
     * Called when an input buffer becomes available.
     *
     * @param codec The MediaCodec object.
     * @param index The index of the available input buffer.
     */
    void onInputBufferAvailable(@NonNull MediaCodec codec, int index);

    /**
     * Called when an output buffer becomes available.
     *
     * @param codec The MediaCodec object.
     * @param index The index of the available output buffer.
     * @param info  Info regarding the available output buffer {@link MediaCodec.BufferInfo}.
     */
    void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
                                 @NonNull MediaCodec.BufferInfo info,
                                 boolean isEndOfStream);


    /**
     * Called when the output format has changed
     * format仅仅是对输出 有关系 输入没有关系！
     *
     * @param codec  The MediaCodec object.
     * @param newFormat The new output format.
     */
    void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat newFormat);

//    void release(@NonNull MediaCodec codec);
}