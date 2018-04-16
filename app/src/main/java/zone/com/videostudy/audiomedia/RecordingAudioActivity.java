package zone.com.videostudy.audiomedia;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import and.utils.data.file2io2data.FileUtils;
import and.utils.data.file2io2data.SDCardUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zone.com.videostudy.R;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecorder;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process.FileProcess;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process.VolumeProcess;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process.WavFileProcess;
import zone.com.videostudy.audiomedia.utilsnow.audio.play.audiotrack.AudioTrackHelper;

public class RecordingAudioActivity extends Activity {


    @Bind(R.id.bt_volume)
    TextView btVolume;
    private AudioRecorder mAudioRecorder = new AudioRecorder();
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.a_record_audio);
        ButterKnife.bind(this);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.bt_start, R.id.bt_end, R.id.bt_start_record, R.id.bt_start_AudioTrack})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.bt_start:
                mAudioRecorder
                        .process(
                                new WavFileProcess(wav2.getAbsolutePath()),
//                                new AudioTrackProcess(),
                                new FileProcess(file.getAbsolutePath()),
                                new VolumeProcess(new VolumeProcess.VolumeListener() {
                                    @Override
                                    public void onVolume(final int volume, int maxVolume) {
                                        System.out.println("volume:" + volume + "\t maxVolume:" + maxVolume);
                                        btVolume.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                btVolume.setText("音量：" + volume);
                                            }
                                        });


                                    }
                                }))
                        .start();
                break;
            case R.id.bt_end:
                mAudioRecorder.stop();
                break;
            case R.id.bt_start_AudioTrack:
                mAudioRecorder.addOnStopRunnable(new Runnable() {
                    @Override
                    public void run() {
                        audioTrack_();
                    }
                });

                break;
            case R.id.bt_start_record:
                mAudioRecorder.addOnStopRunnable(new Runnable() {
                    @Override
                    public void run() {
                        mediaPlay_();
                    }
                });
                break;
        }
    }

    private void mediaPlay_() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();//重置为初始状态
        }
        try {
            mediaPlayer.setDataSource(wav.getAbsolutePath());
            mediaPlayer.prepare();//缓冲
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        mediaPlayer.start();//开始或恢复播放
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.reset();
            }
        });
    }

    private void audioTrack_() {
        try {
            audioTrack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    File file = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "record.pcm_");
    File wav = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "record.wav");
    File wav2 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "record.wav");

    private void audioTrack() throws Exception {
        new AudioTrackHelper(mAudioRecorder.getRecordConfig())
                .playPCM(file.getAbsolutePath())
                .release();
    }

}
