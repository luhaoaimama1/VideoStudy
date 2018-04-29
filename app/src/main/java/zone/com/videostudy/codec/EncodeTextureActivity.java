package zone.com.videostudy.codec;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zone.com.videostudy.R;
/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class EncodeTextureActivity extends Activity {


    @Bind(R.id.textureView)
    TextureView textureView;

    public Surface surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_decode_texture);
        ButterKnife.bind(this);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d("hahaha", " onSurfaceTextureAvailable!");
                EncodeTextureActivity.this.surface = new Surface(surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        initPaint();
    }

    @OnClick(R.id.bt_decode)
    public void onViewClicked() {
        Log.d("hahaha", " onViewClicked");
        if (surface == null)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textureView.animate().rotationBy(360 * 2).setDuration(5000);
                    }
                });
                frameIndex = 0;
                while (true) {
                    //绘图
                    renderFromSource(frameIndex);
                    //解码
                    // 因为我就想录制5秒  5*25 =125
                    if (computePresentationTimeMs(++frameIndex) > 5 * 1000 * 1000)
                        break;
                }
            }

        }).start();
    }

    private long computePresentationTimeMs(int frameIndex) {
        return frameIndex * 1000 * 1000 / 25;
    }


    private void renderFromSource(int frameIndex) {
        Canvas canvas = surface.lockCanvas(null);
        renderFrame(canvas, frameIndex);
        //绘制画布
        surface.unlockCanvasAndPost(canvas);
    }

    Paint paint;
    int frameIndex = 0;

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(150);
    }

    private void renderFrame(Canvas canvas, int frameIndex) {
        // 绘制背景
        paint.setColor(Color.GREEN);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        //绘制数字
        paint.setColor(Color.WHITE);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float showHeight = canvas.getHeight() / 2 - (fontMetrics.top + fontMetrics.bottom) / 2;
        float showWidth = canvas.getWidth() / 2 - paint.measureText(frameIndex + "") / 2;
        canvas.drawText(frameIndex + "", showWidth, showHeight, paint);
    }
}
