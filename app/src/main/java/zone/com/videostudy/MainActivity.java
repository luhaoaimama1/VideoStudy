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
import zone.com.videostudy.codec.DecodeMP4SelfActivity;
import zone.com.videostudy.codec.EncodeSurfaceAsyc2Activity;
import zone.com.videostudy.codec.EncodeSurfaceAsyc_SelfActivity;
import zone.com.videostudy.codec.Extract2MuxerActivity;
import zone.com.videostudy.record.CaptureActivity;
import zone.com.videostudy.record.RecorderMp4Activity;
import zone.com.videostudy.record.LopRecordActivity;
import zone.com.videostudy.video.VideoActivity;
import zone.com.videostudy.video.VideoRecordActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    public static int windowWidth,windowHeight;

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
        switch (view.getId()){
            case R.id.video:
                startActivity(new Intent(this,VideoActivity.class).putExtra("type","video"));
                break;
            case R.id.system:
                startActivity(new Intent(this,VideoActivity.class).putExtra("type","system"));
                break;
            case R.id.surfaceView:
                startActivity(new Intent(this,VideoActivity.class).putExtra("type","surfaceView"));
                break;
            case R.id.videoRecord:
                startActivity(new Intent(this,VideoRecordActivity.class));
                break;
            case R.id.screenRecord:
//                startActivity(new Intent(this,RecordActivity.class));
//                startActivity(new Intent(this,LopRecordActivity.class));
                startActivity(new Intent(this,LopRecordActivity.class));
                break;
            case R.id.screenCapture:
                startActivity(new Intent(this,CaptureActivity.class));
//                startActivity(new Intent(this,CaptureActivity2.class));
                break;
            case R.id.screenRecordToMp4:
                startActivity(new Intent(this,RecorderMp4Activity.class));
                break;

            case R.id.audio:
                startActivity(new Intent(this,RecordingAudioActivity.class)  );
                break;
            case R.id.recordWeixin:
                startActivity(new Intent(this,RecordingActivity.class)  );
                break;
            case R.id.bt_extract_muxer:
                startActivity(new Intent(this,Extract2MuxerActivity.class)  );
                break;
            case R.id.bt_encode_surface:
//                startActivity(new Intent(this,EncodeSurfaceActivity.class)  );
                startActivity(new Intent(this, EncodeSurfaceAsyc2Activity.class)  );
                break;
            case R.id.bt_encode_surface_asyc:
                // startActivity(new Intent(this,EncodeSurfaceActivity.class)  );
                startActivity(new Intent(this,EncodeSurfaceAsyc_SelfActivity.class)  );
                break;
            case R.id.bt_mp4_decode:
//                startActivity(new Intent(this,DecodeMP4Activity.class)  );
                startActivity(new Intent(this,DecodeMP4SelfActivity.class)  );
                break;
        }

    }
}
