package zone.com.videostudy.video.utils;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
    <uses-permission android:name="android.permission.CAMERA" />
    <!-- 调用摄像头 -->
     <uses-permission android:name="android.permission.RECORD_AUDIO" />
     <!-- 录制视频/音频 -->
 */
public class MovieRecorder {
    private  String savePath;
    private  SurfaceView surfaceView;
    private  Camera mCamera;
    public MediaRecorder mediarecorder;
    boolean isRecording;

    /**
     *  @param surfaceView
     * @param mCamera
     * @param savePath .mp4
     */
    public MovieRecorder(SurfaceView surfaceView, Camera mCamera, String savePath) {
        if (mCamera==null||surfaceView==null||savePath==null||!new File(savePath).isFile())
            throw new IllegalArgumentException(" surfaceView,  mCamera,  savePath  may be null");
        this.surfaceView=surfaceView;
        this.mCamera=mCamera;
        this.savePath=savePath;
    }


    public void startRecording( Camera.Size size ) {

        mediarecorder = new MediaRecorder();// 创建mediarecorder对象
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        //给Recorder设置Camera对象，保证录像跟预览的方向保持一致
        mediarecorder.setCamera(mCamera);
        //setCamera 相当于下面那个 官方给的setCamera
//        mediarecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        mediarecorder.setOrientationHint(90);

        // Step 2: Set sources
        // 设置录制视频源为Camera(相机)
        mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // 设置音频源为麦克风
        mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);


        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = size.width;
        profile.videoFrameHeight = size.height;
        mediarecorder.setProfile(profile);
//        setProfile  做了下面这些
//        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
//        mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        // 设置录制的视频编码h263 h264
//        mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        // 设置音频编码amr_nb
//        mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

//       // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
//        mediarecorder.setVideoSize(320, 240);
//        // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
//        mediarecorder.setVideoFrameRate(15);
//        //        在这里我提高了帧频率
//        mediarecorder.setVideoEncodingBitRate(5*1024*1024);

        // Step 4: Set output file
        // 设置视频文件输出的路径
        mediarecorder.setOutputFile(savePath);
        try {
            // 准备录制
            mediarecorder.prepare();
            // 开始录制
            mediarecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRecording = true;
        timeSize = 0;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeSize++;
            }
        }, 0, 1000);
    }

    Timer timer;
    int timeSize = 0;
    private String lastFileName;

    public void stopRecording() {
        if (mCamera!=null)
            mCamera.lock();
        if (mediarecorder != null) {
            // 停止
            mediarecorder.stop();
            mediarecorder.release();
            mediarecorder = null;
            timer.cancel();
            if (null != lastFileName && !"".equals(lastFileName)) {
                File f = new File(lastFileName);
                String name = f.getName().substring(0,
                        f.getName().lastIndexOf(".3gp"));
                name += "_" + timeSize + "s.3gp";
                String newPath = f.getParentFile().getAbsolutePath() + "/"
                        + name;
                if (f.renameTo(new File(newPath))) {
                    int i = 0;
                    i++;
                }
            }
        }
    }

    public void release() {
        if (mediarecorder != null) {
            // 停止
            mediarecorder.stop();
            mediarecorder.release();
            mediarecorder = null;
        }
    }

}