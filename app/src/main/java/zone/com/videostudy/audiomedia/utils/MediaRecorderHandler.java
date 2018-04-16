package zone.com.videostudy.audiomedia.utils;

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
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void stop2release() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}

	public MediaRecorder getmRecorder() {
		return mRecorder;
	}

	public void setmRecorder(MediaRecorder mRecorder) {
		this.mRecorder = mRecorder;
	}

	public double getAmplitude() {
		if (mRecorder != null)
			return (mRecorder.getMaxAmplitude() / 2700.0);
		else
			return 0;
	}
}
