package zone.com.videostudy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import java.io.File;

import and.Configuration;
import and.utils.data.file2io2data.FileUtils;
import and.utils.data.file2io2data.SDCardUtils;
import zone.com.videostudy.audiomedia.RecordingActivity;
import zone.com.videostudy.audiomedia.RecordingAudioActivity;
import zone.com.videostudy.codec.DecodeMP4Activity;
import zone.com.videostudy.codec.EncodeTextureActivity;
import zone.com.videostudy.codec.MP3toAAC_FileAcitivty;
import zone.com.videostudy.codec.WAVtoMp4Activity;
import zone.com.videostudy.codec.EncodeSurfaceActivity;
import zone.com.videostudy.codec.Extract2MuxerActivity;
import zone.com.videostudy.codec.MP3toMP4_MuxerAcitivty;
import zone.com.videostudy.codec.RecordAudioToAAcActivity;
import zone.com.videostudy.codec.WjRecordPlayActivity;
import zone.com.videostudy.record.CaptureActivity;
import zone.com.videostudy.record.LopRecordActivity;
import zone.com.videostudy.utils.RawUtils;
import zone.com.videostudy.video.VideoActivity;
import zone.com.videostudy.video.VideoRecordActivity;

import static butterknife.ButterKnife.Finder.arrayOf;

//todo android:visibility="gone" 的后期有机会处理
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //申请录音权限
    private static final int GET_RECODE_AUDIO = 1;
    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static int windowWidth, windowHeight;
    public static final String MP5NAME = "record_asset.mp4";
    File mp5 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP5NAME);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Configuration.Build.init(this).perform();


        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowWidth = wm.getDefaultDisplay().getWidth();
        windowHeight = wm.getDefaultDisplay().getHeight();
        ActivityCompat.requestPermissions(MainActivity.this, PERMISSION_AUDIO, 1);

        if (!mp5.exists())
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RawUtils.copyFilesFromAssset(MainActivity.this,
                            MP5NAME, mp5.getAbsolutePath());
                }
            }).start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (!isGranted) {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video:
                startActivity(new Intent(this, VideoActivity.class).putExtra("type", "video"));
                break;
            case R.id.system:
                startActivity(new Intent(this, VideoActivity.class).putExtra("type", "system"));
                break;
            case R.id.surfaceView:
                startActivity(new Intent(this, VideoActivity.class).putExtra("type", "surfaceView"));
                break;
            case R.id.videoRecord:
                startActivity(new Intent(this, VideoRecordActivity.class));
                break;
            case R.id.screenRecord:
                startActivity(new Intent(this, LopRecordActivity.class));
                break;
            case R.id.screenCapture:
                startActivity(new Intent(this, CaptureActivity.class));
                break;

            case R.id.audio:
                startActivity(new Intent(this, RecordingAudioActivity.class));
                break;
            case R.id.recordWeixin:
                startActivity(new Intent(this, RecordingActivity.class));
                break;
            case R.id.bt_extract_muxer:
                startActivity(new Intent(this, Extract2MuxerActivity.class));
                break;
            case R.id.bt_wav_to_aac:
                startActivity(new Intent(this, MP3toAAC_FileAcitivty.class));
                break;
            case R.id.bt_mp3_to_mp4:
                startActivity(new Intent(this, MP3toMP4_MuxerAcitivty.class));
                break;
            case R.id.bt_pcmtoMp4:
                startActivity(new Intent(this, RecordAudioToAAcActivity.class));
                break;
            case R.id.bt_encodeMp4:
                startActivity(new Intent(this, WAVtoMp4Activity.class));
                break;
            case R.id.bt_encode_surface:
                startActivity(new Intent(this, EncodeSurfaceActivity.class));
                break;
            case R.id.bt_TextureView:
                startActivity(new Intent(this, EncodeTextureActivity.class));
                break;
            case R.id.bt_mp4_decode:
                startActivity(new Intent(this, DecodeMP4Activity.class));
                break;
            case R.id.bt_wj:
                startActivity(new Intent(this, WjRecordPlayActivity.class));
                break;
        }

    }
}
