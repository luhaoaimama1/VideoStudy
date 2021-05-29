package zone.com.videostudy.audiomedia;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.File;

import zone.com.videostudy.R;
import zone.com.videostudy.audiomedia.utils.MediaRecorderHandler;
import zone.com.videostudy.audiomedia.utils.Player;

public class RecordingActivity extends Activity {

    private Button Start;
    private Button Stop;
    private MediaRecorderHandler recoder;

//    private static final String MusicUrl="http://abv.cn/music/光辉岁月.mp3";
    private static final String MusicUrl="https://github.com/luhaoaimama1/HexoPics/blob/master/abc.mp3";
    private Player player;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_audio_record);
        record();
        onlinePlay();
    }

    private void record() {
        String path = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        path += "/mmmm.amr";
        Start = (Button) this.findViewById(R.id.start);
        Stop = (Button) this.findViewById(R.id.stop);
        recoder=new MediaRecorderHandler();
        final String finalPath = path;
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoder.start(new File(finalPath));
            }
        });
        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoder.stop2release();
            }
        });
    }

    private void onlinePlay() {
        player=new Player((SeekBar) findViewById(R.id.music_progress));
        findViewById(R.id.btn_online_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.playUrl(MusicUrl);
            }
        });
    }
}
