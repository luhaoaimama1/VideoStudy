package zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.process;

import android.media.AudioRecord;

import java.io.File;
import java.io.FileOutputStream;

import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecorder;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.Process;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class FileProcess implements Process {

    private final String filePath;
    private FileOutputStream fos;
    private AudioRecorder audioRecord;

    public FileProcess(String filePath) {
        this.filePath = filePath;
    }


    @Override
    public void init(AudioRecorder audioRecord) throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        fos = new FileOutputStream(file);// 建立一个可存取字节的文件
    }

    @Override
    public int processData(AudioRecorder audioRecord, byte[] pcmData, int readsize) throws Exception {
        if (AudioRecord.ERROR_INVALID_OPERATION != readsize)
            fos.write(pcmData);
        this.audioRecord = audioRecord;
        return readsize;
    }

    @Override
    public void end() throws Exception {
        fos.close();
    }

    @Override
    public void release() throws Exception {

    }
}
