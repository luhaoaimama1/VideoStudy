package zone.com.videostudy.codec.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;

/**
 * 仅仅是提示，可以查看paint关注更多API
 * 关于drawText的文章：
 * https://luhaoaimama1.github.io/2016/12/18/drawText/
 * <p>
 * DrawUtils.Text.with(canvas, content, 0, y, paint)
 * .align(align)
 * .drawBound(paintBounds)
 * .show(showType);
 */
public class Text {


    public static float getTextHeight(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.top - fontMetrics.bottom;
    }

    /**
     * 仅仅是提示，可以查看paint关注更多API
     *
     * @param paint
     * @param str
     * @return
     */
    public static float getTextWidth(Paint paint, String str) {
        float width = paint.measureText(str);
        return width;
    }

    Canvas canvas;
    String content;
    float x;
    float y;
    Paint paint;
    float degrees;
    Paint.Align align;
    Paint.Align oldAlign;
    boolean alignIsRestore;
    Paint drawBoundPaint;
    ShowType showType;

    private Text(Canvas canvas, String content, float x, float y, @NonNull Paint paint) {
        this.canvas = canvas;
        this.content = content;
        this.x = x;
        this.y = y;
        this.paint = paint;
        oldAlign = paint.getTextAlign();
    }

    public static Text with(Canvas canvas, String content, float x, float y, @NonNull Paint paint) {
        return new Text(canvas, content, x, y, paint);
    }

    public Text rotate(float degrees) {
        this.degrees = degrees;
        return this;
    }

    public Text align(Paint.Align align) {
        return align(align, false);
    }

    public Text align(Paint.Align align, boolean alignIsRestore) {
        this.align = align;
        this.alignIsRestore = alignIsRestore;
        return this;
    }

    public Text drawBound(@NonNull Paint paint) {
        drawBoundPaint = paint;
        if (drawBoundPaint == this.paint)
            throw new IllegalStateException("drawBound 's paint is not drawText's paint!");
        return this;
    }

    public RectF show(@NonNull ShowType showType) {
        this.showType = showType;
        if (align != null)
            paint.setTextAlign(align);

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float baseLineY = getLineYByShowType(fontMetrics);


        int width = (int) paint.measureText(content);
        RectF rect = new RectF(0, fontMetrics.top, width, fontMetrics.bottom);
        switch (paint.getTextAlign()) {
            case LEFT:
                rect.offset(x, baseLineY);
                break;
            case RIGHT:
                rect.offset(x - width, baseLineY);
                break;
            case CENTER:
                rect.offset(x - width / 2, baseLineY);
                break;
        }

        if (degrees != 0) {
            canvas.save();
            canvas.rotate(degrees, x, y);
        }

        if (drawBoundPaint != null)
            canvas.drawRect(rect, drawBoundPaint);
        canvas.drawText(content, x, baseLineY, paint);

        if (degrees != 0)
            canvas.restore();

        if (alignIsRestore)
            paint.setTextAlign(oldAlign);
        return rect;
    }

    private float getLineYByShowType(Paint.FontMetrics fontMetrics) {
        switch (showType) {
            case TopOfPoint:
                return y - fontMetrics.bottom;
            case CenterIsPoint:
                return y - (fontMetrics.top + fontMetrics.bottom) / 2;
            case bottomOfPoint:
                return y - fontMetrics.top;
            case baseLineIsPoint:
                return y;
            default:
                return y - fontMetrics.bottom;

        }
    }

    public enum ShowType {
        TopOfPoint, CenterIsPoint, bottomOfPoint, baseLineIsPoint;
    }
}