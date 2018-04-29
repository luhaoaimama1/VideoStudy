package zone.com.videostudy.record;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Surface;

import java.io.File;

import and.utils.data.file2io2data.FileUtils;
import and.utils.data.file2io2data.SDCardUtils;
import zone.com.videostudy.record.uitlssss.MediaProjectionHelper;
import zone.com.videostudy.record.uitlssss.MediaRecorderHelper;
import zone.com.videostudy.record.uitlssss.VirtualDisplayParams;
import zone.com.videostudy.video.utils.VideoUtils;

public class ScreenRecordService extends Service {


    private boolean isVideoSd, isAudio;
    private MediaRecorder mMediaRecorder;
    MediaProjectionHelper.Recorder mediaProjectionHelper = new MediaProjectionHelper.Recorder();
    private VirtualDisplayParams vdp;

    @Override
    public void onCreate() {
        super.onCreate();
    }
    MediaRecorderHelper mediaRecorderHelper;
    File mp4 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "record_lopip.mp4");

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        isVideoSd = intent.getBooleanExtra("quality", true);
        isAudio = intent.getBooleanExtra("audio", false);

        mediaProjectionHelper.startRecord(this,vdp= VirtualDisplayParams.readExtra(intent), new MediaProjectionHelper.Recorder.RecordNeed() {
            @Override
            public Surface getSurface() {
                mediaRecorderHelper=new MediaRecorderHelper(isAudio?new MediaRecorderHelper.Audio():null,
                        new MediaRecorderHelper.Video(
                                vdp.width, vdp.height,
                                isVideoSd?MediaRecorderHelper.VideoStyle.SD:MediaRecorderHelper.VideoStyle.HD)
                        , new MediaRecorderHelper.EndSymbol() {
                    @Override
                    public String getFileSavePath(String endSymbol) {
                        return FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "record_lopip.mp4")
                                .getAbsolutePath().replace(".mp4",endSymbol);
                    }
                });
                mMediaRecorder = mediaRecorderHelper.createMediaRecorder();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return mMediaRecorder.getSurface();
                } else {
                    throw new IllegalStateException("版本太低");
                }
            }

        });

        mMediaRecorder.start();
        return Service.START_NOT_STICKY;
    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaRecorderHelper.release();
        mediaProjectionHelper.onDestory();
        VideoUtils.startSystemVideo(ScreenRecordService.this, Uri.fromFile(mp4));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

} 