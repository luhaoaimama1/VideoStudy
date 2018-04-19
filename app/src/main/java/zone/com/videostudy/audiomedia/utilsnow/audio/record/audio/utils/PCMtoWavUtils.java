package zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.utils;

import android.media.AudioFormat;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecordConfig;

/**
 * https://github.com/lixiaoshaxing/MultiMediaLearn/blob/master/app/src/main/java/com/lx/multimedialearn/utils/MediaUtils.java
 * 多媒体工具类
 * 1. PCM转WAV格式（注意大小端）
 *
 * @author lixiao
 * @since 2017-11-27 15:44
 */
public class PCMtoWavUtils {

    public static void pcm2wav(AudioRecordConfig audioRecordConfig, String inputPath, String outPath) {
//        int sampleRateInHz, int bitsPerSample, int channels
        int sampleRateInHz = audioRecordConfig.getSampleRate();
        int bitsPerSample = 16;
        switch (audioRecordConfig.getAudioFormat()) {
            case AudioFormat.ENCODING_PCM_16BIT:
                bitsPerSample = 16;
                break;
            case AudioFormat.ENCODING_PCM_8BIT:
                bitsPerSample = 8;
                break;
            default:
                throw new IllegalStateException("内置无法处理此格式");
        }
        int channels = 1;
        switch (audioRecordConfig.getChannelConfig()) {
            case AudioFormat.CHANNEL_IN_MONO:
                channels = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                channels = 2;
                break;
            default:
                throw new IllegalStateException("内置无法处理此格式");
        }

        pcm2wav(sampleRateInHz, bitsPerSample, channels, audioRecordConfig.getMinBufferSizeAR(), inputPath, outPath);
    }

    /**
     * pcm转wav
     *
     * @param sampleRateInHz 采样率：44100，16000等
     * @param bitsPerSample  每个采样的位数：16 （不要传入AudioFormat.ENCODING_PCM_16BIT）
     * @param channels       通道数：2
     * @param bufferSize     录入是buffersize大小，没有用录制，传入0
     * @param inputPath      输入文件
     * @param outPath        输出文件
     */
    public static void pcm2wav(int sampleRateInHz, int bitsPerSample, int channels, int bufferSize, String inputPath, String outPath) {
        FileInputStream in = null;
        DataOutputStream out = null;
        long totalSize = 0;
        try {
            in = new FileInputStream(inputPath);
            out = new DataOutputStream(new FileOutputStream(outPath));
            writeHeader(out, sampleRateInHz, bitsPerSample, channels);
            if (bufferSize == 0) {
                bufferSize = 1024;
            }
            byte[] data = new byte[bufferSize];
            while (in.read(data) != -1) {
                out.write(data, 0, data.length);
                totalSize += data.length;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭流，写入总大小
            if (out != null) {
                writeDataSize(out, outPath, totalSize);
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void writeHeader(DataOutputStream dataOutputStream, int sampleRateInHz, int bitsPerSample, int channels) {
        if (dataOutputStream == null) {
            return;
        }
        WavFileHeader header = new WavFileHeader(sampleRateInHz, bitsPerSample, channels);
        try {
            dataOutputStream.writeBytes(header.mChunkID);
            //关闭文件时需要追加设置这里的大小
            dataOutputStream.write(intToByteArray((int) header.mChunkSize), 0, 4);
            dataOutputStream.writeBytes(header.mFormat);
            dataOutputStream.writeBytes(header.mSubChunk1ID);
            dataOutputStream.write(intToByteArray((int) header.mSubChunk1Size), 0, 4);
            dataOutputStream.write(shortToByteArray((short) header.mAudioFormat), 0, 2);
            dataOutputStream.write(shortToByteArray((short) header.mNumChannel), 0, 2);
            dataOutputStream.write(intToByteArray((int) header.mSampleRate), 0, 4);
            dataOutputStream.write(intToByteArray((int) header.mByteRate), 0, 4);
            dataOutputStream.write(shortToByteArray((short) header.mBlockAlign), 0, 2);
            dataOutputStream.write(shortToByteArray((short) header.mBitsPerSample), 0, 2);
            dataOutputStream.writeBytes(header.mSubChunk2ID);
            dataOutputStream.write(intToByteArray((int) header.mSubChunk2Size), 0, 4);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeDataSize(DataOutputStream outputStream, String filePath, long totalSize) {

        if (outputStream == null) {
            return;
        }
        try {
            RandomAccessFile wavFile = new RandomAccessFile(filePath, "rw");
            wavFile.seek(4);
            wavFile.write(intToByteArray((int) (totalSize + WavFileHeader.WAV_CHUNKSIZE_EXCLUDE_DATA)), 0, 4);
            wavFile.seek(40);
            wavFile.write(intToByteArray((int) (totalSize)), 0, 4);
            wavFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        return;
    }

    //基本数据类型转为byte数组
    private static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    private static byte[] shortToByteArray(short data) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array();
    }

    /**
     * wav文件的头信息
     */
    private static class WavFileHeader {
        public static final int WAV_FILE_HEADER_SIZE = 44;
        public static final int WAV_CHUNKSIZE_EXCLUDE_DATA = 36;
        public static final int WAV_CHUNKSIZE_OFFSET = 4;
        public static final int WAV_SUB_CHUNKSIZE1_OFFSET = 16;
        public static final int WAV_SUB_CHUNKSIZE2_OFFSET = 40;
        public String mChunkID = "RIFF";
        public int mChunkSize = 0;
        public String mFormat = "WAVE";
        public String mSubChunk1ID = "fmt ";
        public int mSubChunk1Size = 16;
        public short mAudioFormat = 1;
        public short mNumChannel = 1;
        public int mSampleRate = 8000;
        public int mByteRate = 0;
        public short mBlockAlign = 0;
        public short mBitsPerSample = 8;

        public String mSubChunk2ID = "data";
        public int mSubChunk2Size = 0;

        public WavFileHeader(int sampleRateInHz, int bitsPerSample, int channels) {
            mSampleRate = sampleRateInHz;
            mBitsPerSample = (short) bitsPerSample;
            mNumChannel = (short) channels;
            mByteRate = mSampleRate * mNumChannel * mBitsPerSample / 8;
            mBlockAlign = (short) (mNumChannel * mBitsPerSample / 8);
        }
    }
}