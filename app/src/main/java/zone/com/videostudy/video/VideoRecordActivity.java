package zone.com.videostudy.video;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import zone.com.videostudy.R;
import zone.com.videostudy.file2io2data.FileUtils;
import zone.com.videostudy.file2io2data.SDCardUtils;
import zone.com.videostudy.video.utils.CameraHelper;
import zone.com.videostudy.video.utils.MovieRecorder;

/**
 * Created by fuzhipeng on 16/7/25.
 */
public class VideoRecordActivity extends Activity {

    private static final String TAG = "VideoRecordActivity";

    private boolean mIsRecording = false;
    private boolean mIsSufaceCreated = false;

    private SurfaceView mCameraPreview;
    private Button record_shutter;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private MovieRecorder mMovieRecorder;
    private Camera.Size size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        mCameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
        record_shutter=(Button)findViewById(R.id.record_shutter);


        mSurfaceHolder = mCameraPreview.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        record_shutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mIsRecording){
                    startRecord();
                    record_shutter.setText("停止录制");
                } else{
                    stopRecord();
                    record_shutter.setText("开始录制");
                }

                mIsRecording=!mIsRecording;
            }
        });
    }
    protected void onPause() {
        super.onPause();
        if (mIsRecording) {
            stopRecord();
        }
        CameraHelper.stopPreview(mCamera);
        mCamera=null;
    }



    @Override
    protected void onResume() {
        super.onResume();
        startPreview();
    }



    private void stopRecord() {
        if (mMovieRecorder!=null)
            mMovieRecorder.stopRecording();
    }

    private void startRecord() {
        mMovieRecorder=new MovieRecorder(mCameraPreview,mCamera,
                FileUtils.getFile(SDCardUtils.getSDCardDir(), "录制.mp4").getAbsolutePath());
        mMovieRecorder.startRecording(size);
    }



    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsSufaceCreated = false;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mIsSufaceCreated = true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            startPreview();
        }
    };

    //启动预览
    private void startPreview() {
        //保证只有一个Camera对象
        if (mCamera != null || !mIsSufaceCreated) {
            Log.d(TAG, "startPreview will return");
            return;
        }

        mCamera = CameraHelper.getDefaultBackFacingCameraInstance();

        Camera.Parameters parameters = mCamera.getParameters();
        size = CameraHelper.getOptimalVideoSize(parameters,1080, 1920);
        if (size != null)
            parameters.setPreviewSize(size.width, size.height);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        //设置相机预览方向
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
//          mCamera.setPreviewCallback(mPreviewCallback);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        mCamera.startPreview();
    }

}
