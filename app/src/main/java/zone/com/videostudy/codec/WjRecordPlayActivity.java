package zone.com.videostudy.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zone.com.videostudy.R;
import zone.com.videostudy.audiomedia.utilsnow.audio.play.audiotrack.AudioTrackHelper;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecorder;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.Process;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process.VolumeProcess;

public class WjRecordPlayActivity extends Activity {
    @Bind(R.id.bt_volume)
    TextView btVolume;
    private AudioRecorder mAudioRecorder = new AudioRecorder();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.a_record_audio_wj);
        ButterKnife.bind(this);
    }

    //zone todo: 2021/5/29  研究
    Process process = new Process() {
        private AudioTrackHelper audioTrackHelper = new AudioTrackHelper(mAudioRecorder.getAudioRecordConfig());

        @Override
        public void init(AudioRecorder audioRecorder) throws Exception {
            AudioTrack audioTrack = audioTrackHelper.getAudioTrack();
            if (audioTrack != null) {
                audioTrack.play();
            }
        }

        @Override
        public int processData(AudioRecorder audioRecord, byte[] pcmData, int readsize) throws Exception {
            if (readsize > 0) {
                AudioTrack audioTrack = audioTrackHelper.getAudioTrack();
                if (audioTrack != null) {
                    audioTrack.write(pcmData, 0, readsize);
                }
            }
            return readsize;
        }


        @Override
        public void end() throws Exception {
            AudioTrack audioTrack = audioTrackHelper.getAudioTrack();
            if (audioTrack != null) {
                audioTrack.stop();
            }
            audioTrackHelper.release();
        }

        @Override
        public void release() throws Exception {

        }
    };

    VolumeProcess volumeProcess = new VolumeProcess(new VolumeProcess.VolumeListener() {
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
    });

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.bt_start, R.id.bt_end})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_start:
                mAudioRecorder.process(
                        process,
                        volumeProcess
                ).start();

                break;
            case R.id.bt_end:
                mAudioRecorder.stop();
                break;
        }

    }
}
