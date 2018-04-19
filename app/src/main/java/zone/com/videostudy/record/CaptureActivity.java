package zone.com.videostudy.record;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.widget.Button;

import java.io.File;

import and.utils.activity_fragment_ui.ToastUtils;
import and.utils.data.file2io2data.FileUtils;
import and.utils.data.file2io2data.SDCardUtils;
import and.utils.image.compress2sample.CompressUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zone.com.videostudy.ImageActivity;
import zone.com.videostudy.R;
import zone.com.videostudy.record.uitlssss.MediaProjectionHelper;
import zone.com.videostudy.record.uitlssss.ResultOK;
import zone.com.videostudy.record.uitlssss.VirtualDisplayParams;
import zone.com.videostudy.record.utils.ImageReaderUtils;

/**
 * Created by fuzhipeng on 16/7/25.
 * 截一张图
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class CaptureActivity extends Activity {
    @Bind(R.id.bt_capture)
    Button btCapture;
    private ImageReader mImageReader;
    MediaProjectionHelper.Permission permission = new MediaProjectionHelper.Permission();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_screen_capture);
        ButterKnife.bind(this);
    }

    final MediaProjectionHelper.Recorder recorder = new MediaProjectionHelper.Recorder();

    @Override
    public void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        permission.onActivityResult(requestCode, resultCode, data, new ResultOK() {
            @Override
            public VirtualDisplayParams getParams() {
                VirtualDisplayParams params = VirtualDisplayParams.getDefault(CaptureActivity.this, resultCode, data);
                mImageReader = ImageReader.newInstance(
                        params.width, params.height, 0x1, 1);
                return params;
            }

            @Override
            public void onResultOK(VirtualDisplayParams params) {
//                if (!recorder.isRun())
                    recorder.startRecord(CaptureActivity.this, params, new MediaProjectionHelper.Recorder.RecordNeed() {
                        @Override
                        public Surface getSurface() {
                            return mImageReader.getSurface();
                        }
                    });

                //截图必须延迟500毫秒不然不好使
                btCapture.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        File imageFile = FileUtils.getFile(SDCardUtils.getSDCardDir(),
                                "VideoStudyHei", "capture"+System.currentTimeMillis() +".png");
                        Image image = mImageReader.acquireLatestImage();
                        if (image != null) {
                            CompressUtils.saveBitmap(imageFile.getAbsolutePath(), ImageReaderUtils.convertBitmap(image));
                            ToastUtils.showShort(CaptureActivity.this, "截图成功");
                            Intent intent = new Intent(CaptureActivity.this, ImageActivity.class);
                            intent.putExtra(ImageActivity.Image,imageFile.getAbsolutePath());
                            startActivity(intent);
                        }
                    }
                }, 500);
            }

        });
    }

    @OnClick(R.id.bt_capture)
    public void onClick() {
        permission.applyRecordPermission(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        recorder.onDestory();
    }

}

