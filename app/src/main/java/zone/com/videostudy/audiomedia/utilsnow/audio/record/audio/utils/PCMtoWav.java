package zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.utils;

import android.media.AudioFormat;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.RecordConfig;

/**
 * 原理 ：PCMtoWavUtils
 * 不过这个不是 结尾转换 ，而是在读取pcm的时候就转换了
 * @author lixiao
 * @since 2017-11-27 15:44
 */
public class PCMtoWav {

    private String outPath;
    private int sampleRateInHz;
    private int bitsPerSample = 16;
    private int channels = 1;


    public void pcm2wav(RecordConfig recordConfig, String outPath) {
//        int sampleRateInHz, int bitsPerSample, int channels
        this.outPath = outPath;
        sampleRateInHz = recordConfig.getSampleRate();
        switch (recordConfig.getAudioFormat()) {
            case AudioFormat.ENCODING_PCM_16BIT:
                bitsPerSample = 16;
                break;
            case AudioFormat.ENCODING_PCM_8BIT:
                bitsPerSample = 8;
                break;
            default:
                throw new IllegalStateException("内置无法处理此格式");
        }

        switch (recordConfig.getChannelConfig()) {
            case AudioFormat.CHANNEL_IN_MONO:
                channels = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                channels = 2;
                break;
            default:
                throw new IllegalStateException("内置无法处理此格式");
        }

    }

    DataOutputStream out = null;
    private int totalSize;

    public void initFile() throws FileNotFoundException {
        totalSize = 0;
        out = new DataOutputStream(new FileOutputStream(outPath));
        writeHeader(out, sampleRateInHz, bitsPerSample, channels);
    }

    public void write(byte[] pcm) throws IOException {
        out.write(pcm, 0, pcm.length);
        totalSize += pcm.length;
    }

    public void closeFile() {
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

    private void writeHeader(DataOutputStream dataOutputStream, int sampleRateInHz, int bitsPerSample, int channels) {
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

    private void writeDataSize(DataOutputStream outputStream, String filePath, long totalSize) {

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
    private byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    private byte[] shortToByteArray(short data) {
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