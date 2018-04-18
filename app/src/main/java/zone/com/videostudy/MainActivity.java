package zone.com.videostudy;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import and.Configuration;
import zone.com.videostudy.audiomedia.RecordingActivity;
import zone.com.videostudy.audiomedia.RecordingAudioActivity;
import zone.com.videostudy.codec.DecodeMP4Activity;
import zone.com.videostudy.codec.MP3toAACAcitivty;
import zone.com.videostudy.codec.WAVtoMp4Activity;
import zone.com.videostudy.codec.EncodeSurfaceActivity;
import zone.com.videostudy.codec.Extract2MuxerActivity;
import zone.com.videostudy.codec.MP3toMP4_MuxerAcitivty;
import zone.com.videostudy.codec.RecordAudioToAAcActivity;
import zone.com.videostudy.record.CaptureActivity;
import zone.com.videostudy.record.RecorderMp4Activity;
import zone.com.videostudy.record.LopRecordActivity;
import zone.com.videostudy.video.VideoActivity;
import zone.com.videostudy.video.VideoRecordActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public static int windowWidth, windowHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Configuration.Build.init(this).perform();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowWidth = wm.getDefaultDisplay().getWidth();
        windowHeight = wm.getDefaultDisplay().getHeight();
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
//                startActivity(new Intent(this,RecordActivity.class));
//                startActivity(new Intent(this,LopRecordActivity.class));
                startActivity(new Intent(this, LopRecordActivity.class));
                break;
            case R.id.screenCapture:
                startActivity(new Intent(this, CaptureActivity.class));
//                startActivity(new Intent(this,CaptureActivity2.class));
                break;
            case R.id.screenRecordToMp4:
                startActivity(new Intent(this, RecorderMp4Activity.class));
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
                startActivity(new Intent(this, MP3toMP4_MuxerAcitivty.class));
                break;
            case R.id.bt_mp3_to_mp4:
                startActivity(new Intent(this, MP3toAACAcitivty.class));
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
            case R.id.bt_mp4_decode:
                startActivity(new Intent(this, DecodeMP4Activity.class));
                break;
        }

    }
}
