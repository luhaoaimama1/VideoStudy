package zone.com.videostudy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import zone.com.videostudy.audio.Record2PlayActivity;
import zone.com.videostudy.audio.RecordingActivity;
import zone.com.videostudy.video.ScreenRecordActivity;
import zone.com.videostudy.video.VideoActivity;
import zone.com.videostudy.video.VideoRecordActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                startActivity(new Intent(this,ScreenRecordActivity.class));
                break;

            case R.id.audio:
                startActivity(new Intent(this,Record2PlayActivity.class)  );
                break;
            case R.id.recordWeixin:
                startActivity(new Intent(this,RecordingActivity.class)  );
                break;
        }

    }
}
