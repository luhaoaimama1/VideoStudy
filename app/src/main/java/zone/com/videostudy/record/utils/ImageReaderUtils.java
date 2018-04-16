package zone.com.videostudy.record.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import java.nio.ByteBuffer;

/**
 * [2017] by Zone
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ImageReaderUtils {

    public   static Bitmap convertBitmap(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        //取PixelStride，这指的是两个像素的距离(就是一个像素头部到相邻像素的头部)，这是字节格式的
        int pixelStride = planes[0].getPixelStride();
        //RowStride是一行占用的距离(就是一行像素头部到相邻行像素的头部)，这个大小和width有关，
        // 这里需要注意，因为内存对齐的原因，所以每行会有一些空余。这个值也是字节格式的
        int rowStride = planes[0].getRowStride();
        //padding用来对齐
        int rowPadding = rowStride - pixelStride * width;
        //必须计算+ rowPadding/pixelStride 因为 需要按照planes的存储格式进行解析。
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        //最后裁剪下
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        return bitmap;
    }
}
