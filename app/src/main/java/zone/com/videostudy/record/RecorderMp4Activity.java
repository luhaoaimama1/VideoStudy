package zone.com.videostudy.record;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;

import java.io.File;

import and.utils.activity_fragment_ui.ToastUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zone.com.videostudy.MainActivity;
import zone.com.videostudy.R;
import zone.com.videostudy.record.utils.AEncoder;
import zone.com.videostudy.record.utils.MediaMuxerExample;
import zone.com.videostudy.record.utils.AMediaProjection;

/**
 * Created by fuzhipeng on 16/7/25.
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public class RecorderMp4Activity extends Activity {
    @Bind(R.id.bt_capture)
    Button btCapture;
    private AMediaProjection mph;
    private AEncoder aEncoder;
    private MediaMuxerExample muxerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_screen_capture_mp4);
        ButterKnife.bind(this);

        mph = new AMediaProjection(this, new AMediaProjection.Callback() {

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public AMediaProjection.VirtualDisplayParams getParams() {
                AMediaProjection.VirtualDisplayParams params =
                        new AMediaProjection.VirtualDisplayParams();
                params.surfaceWidth = MainActivity.windowWidth;
                params.surfaceHeight =MainActivity.windowHeight;
                //建立解码器
                aEncoder = new AEncoder(MainActivity.windowWidth,
                        MainActivity.windowHeight);
                params.surface = aEncoder.prepareEncoder();
                return params;
            }

            @Override
            public void complete() {
                //成功  解码转成mp4
                String mImagePath = Environment.getExternalStorageDirectory().getPath()
                        + "/screenshort/";
                File file = new File(mImagePath, "record-" + System.currentTimeMillis() + ".mp4");
                muxerHelper = new MediaMuxerExample(file.getAbsolutePath(), aEncoder
                        , new MediaMuxerExample.Callback() {
                    @Override
                    public void quit() {
                        aEncoder.release();
                        muxerHelper.release();
                        mph.requestOpen(false);
                        btCapture.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort(RecorderMp4Activity.this, "视频录制成功");
                            }
                        });
                    }
                });
                muxerHelper.startRecord();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (muxerHelper != null)
            muxerHelper.quit();//停止
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mph.ondestory();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mph.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.bt_capture)
    public void onClick() {
        if (btCapture.getText().equals("start!")) {
            mph.requestOpen(true);
            btCapture.setText("stop2release!");
        } else {
            if (muxerHelper != null) {
                muxerHelper.quit();//停止
                btCapture.setText("start!");
            }
        }
    }
}
