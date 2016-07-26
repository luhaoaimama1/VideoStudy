package zone.com.videostudy.audio.utils;

import android.media.MediaRecorder;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
//Tip:  <uses-permission android:name="android.permission.RECORD_AUDIO"></uses-permission>
public  class MediaRecorderHandler {
	private MediaRecorder mRecorder = null;

	public void start(File saveFile) {
		if (!Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return;
		}
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//MIC 麦克
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);//
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//
			mRecorder.setOutputFile(saveFile.getPath());
			try {
				mRecorder.prepare();
				mRecorder.start();
			} catch (IllegalStateException e) {
				System.out.print(e.getMessage());
			} catch (IOException e) {
				System.out.print(e.getMessage());
			}

		}
	}

	public void stop() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}

	public void pause() {
		if (mRecorder != null) {
			mRecorder.stop();
		}
	}

	public void reStart() {
		if (mRecorder != null) {
			mRecorder.start();
		}
	}

	public double getAmplitude() {

//	int x = recorder.getMaxAmplitude();
//	if (x != 0) {
//		int f = (int) (10 * Math.log(x) / Math.log(10));
//		if (f < 26)
//			volumeHandler.sendEmptyMessage(0);
//		else if (f < 32)
//			volumeHandler.sendEmptyMessage(1);
//		else if (f < 38)
//			volumeHandler.sendEmptyMessage(2);
//		else
//			volumeHandler.sendEmptyMessage(3);
//	}
		if (mRecorder != null)
			return (mRecorder.getMaxAmplitude() / 2700.0);
		else
			return 0;
	}
}
