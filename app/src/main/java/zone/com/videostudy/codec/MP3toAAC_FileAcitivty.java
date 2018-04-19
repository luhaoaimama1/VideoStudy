package zone.com.videostudy.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import and.utils.activity_fragment_ui.ToastUtils;
import and.utils.data.file2io2data.FileUtils;
import and.utils.data.file2io2data.SDCardUtils;
import and.utils.data.file2io2data.SharedUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zone.com.videostudy.R;
import zone.com.videostudy.codec.utils.Callback;
import zone.com.videostudy.codec.utils.ExtractorWrapper;
import zone.com.videostudy.codec.utils.MediaCodecHelper;
import zone.com.videostudy.codec.utils.MediaFormatEs;
import zone.com.videostudy.utils.RawUtils;

/**
 * MIT License
 * <p>
 * AAC文件写入( MP3,wav支持)：解码+编码器（AAC）。在编码AAC的输出端 添加ACT头部 后  文件流的方式写入
 * Copyright (c) [2018] [Zone]
 * <p>
 * 参考：https://blog.csdn.net/TinsanMr/article/details/51049179
 */
public class MP3toAAC_FileAcitivty extends Activity {
    final String MP3NAMe = "record_asset.wav";
    File mp3 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP3NAMe);

    final String MP4NAME = "test_asset.mp3";
    File mp4 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", MP4NAME);

    @Bind(R.id.video)
    VideoView videoView;
    private MediaCodecHelper helper, helper2;
    private byte[] mAudioOutTempBuf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_muxer);
        ButterKnife.bind(this);


        new Thread(new Runnable() {
            @Override
            public void run() {
                RawUtils.copyFilesFromAssset(MP3toAAC_FileAcitivty.this,
                        MP3NAMe, mp3.getAbsolutePath());
                RawUtils.copyFilesFromAssset(MP3toAAC_FileAcitivty.this,
                        MP4NAME, mp4.getAbsolutePath());
                SharedUtils.put("exist", true);
            }
        }).start();
    }

    @OnClick(R.id.bt_muxer)
    public void onViewClicked() {
        if (!SharedUtils.get("exist", false))
            ToastUtils.showShort(this, "文件未保存入sd卡");
        muxerMedia();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void muxerMedia() {
        try {

            ExtractorWrapper audioWarper = ExtractorWrapper.getExtractor(mp3.getAbsolutePath(), ExtractorWrapper.AUDIO);
            if (audioWarper.trackIndex == -1) {
                ToastUtils.showShort(this, "音频轨道未找到！");
                return;
            }
            audioDecodeEncode2Mp3toAAc(audioWarper);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    File aac = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "heihei.aac");
    private FileOutputStream fos;
    private BufferedOutputStream bos;

    public void audioDecodeEncode2Mp3toAAc(final ExtractorWrapper wrapper) throws IOException {
        fos = new FileOutputStream(aac);
        bos = new BufferedOutputStream(fos, 200 * 1024);


        int sampleRate = wrapper.format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channelCount = wrapper.format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);


        MediaFormat mediaFormat = new MediaFormat();
        mediaFormat.setString("mime", "audio/mp4a-latm");
//        mediaFormat.setString("mime", MediaFormat.MIMETYPE_AUDIO_MPEG);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);// AAC-HE // 64kbps
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate,
                (channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                AudioFormat.ENCODING_PCM_16BIT);
        int maxInputSize = 0;
        try {
            maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,
                maxInputSize == 0 ? minBufferSize * 4 : maxInputSize);

        mAudioOutTempBuf = new byte[mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)];

        helper2 = MediaCodecHelper.encode(mediaFormat)
                .createByCodecName()
                .tag("helper___222")
                .callback(false, true,
                        new Callback() {
                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onInputBufferAvailable(@NonNull MediaCodec codec, int inputBufferIndex) {
                            }

                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
                                                                @NonNull MediaCodec.BufferInfo bufferInfo,
                                                                boolean isEndOfStream) {
//                                toAAC(codec, index, bufferInfo, isEndOfStream, wrapper);

                                toAACFile(codec, index, bufferInfo, isEndOfStream);
//                                codec.releaseOutputBuffer(index, false);
                            }

                            @Override
                            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                            }
                        }
                ).prepare();

//        audioTrack = AudioTrackUtils.getAudioTrackByFormat(wrapper.format);

        wrapper.extractor.unselectTrack(wrapper.trackIndex);
        wrapper.extractor.selectTrack(wrapper.trackIndex);

        helper = MediaCodecHelper
                .decode(wrapper.format)
                .createByCodecName()
                .tag("helper___111")
                .callback(new Callback() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onInputBufferAvailable(@NonNull MediaCodec codec, int inputBufferIndex) {
                        ByteBuffer byteBuffer = codec.getInputBuffer(inputBufferIndex);
                        int readSize = wrapper.extractor.readSampleData(byteBuffer, 0);
                        if (readSize >= 0) {
                            Log.d("helper___111", " input write readSize:" + readSize + "\ttime:" + wrapper.extractor.getSampleTime());
                            codec.queueInputBuffer(inputBufferIndex, 0, readSize
                                    , wrapper.extractor.getSampleTime(), 0);
                            wrapper.extractor.advance();
                        } else {
                            Log.d("helper___111", " input end");
                            helper.signalEndOfQueueInputBuffer(inputBufferIndex);
                        }
                    }

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int index,
                                                        @NonNull MediaCodec.BufferInfo info, boolean isEndOfStream) {
                        if (info.size > 0) {
                            ByteBuffer outBuffer = mediaCodec.getOutputBuffer(index);
                            writeData(outBuffer, info, false);
                        }
                        mediaCodec.releaseOutputBuffer(index, false);
//                        //有点数据 但是有一半
                        if (isEndOfStream) {
                            writeData(null, info, isEndOfStream);
                            helper.release();
                        }
                    }

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    private void writeData(ByteBuffer outBuffer, MediaCodec.BufferInfo info, boolean isQuit) {
                        MediaCodec codec2 = helper2.getMediaCodec();
                        if (isQuit) {
                            Log.d("helper___444", " helper dequeueInputBuffer.....");
                            int inputBufferIndex = codec2.dequeueInputBuffer(-1);
                            Log.d("helper___444", " helper dequeueInputBuffer inputBufferIndex :" + inputBufferIndex);
                            if (inputBufferIndex >= 0) {
                                helper2.signalEndOfQueueInputBuffer(inputBufferIndex);
                            }
                        } else {
                            Log.d("helper___3333", " helper dequeueInputBuffer.....");
                            int inputBufferIndex = codec2.dequeueInputBuffer(-1);
                            Log.d("helper___3333", " helper dequeueInputBuffer inputBufferIndex :" + inputBufferIndex);

                            if (inputBufferIndex >= 0) {
                                outBuffer.get(mAudioOutTempBuf, info.offset, info.size);

                                //把取出空InputBuffer的写入
                                ByteBuffer inputBuffer = codec2.getInputBuffer(inputBufferIndex);
                                // 向输入缓存区写入有效原始数据，并提交到编码器中进行编码处理
                                inputBuffer.position(info.offset);
                                inputBuffer.limit(info.offset + info.size);
                                inputBuffer.put(mAudioOutTempBuf, info.offset, info.size);

                                codec2.queueInputBuffer(inputBufferIndex, info.offset, info.size,
                                        info.presentationTimeUs, 0);
                            }
                        }
                    }

                    @Override
                    public void onOutputFormatChanged(@NonNull MediaCodec codec
                            , @NonNull MediaFormat format) {

                    }
                }).prepare();


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void toAACFile(@NonNull MediaCodec codec, int index,
                           @NonNull MediaCodec.BufferInfo info,
                           boolean isEndOfStream) {
        if (info.size > 0) {
            Log.d("helper___", " releaseOutputBuffer index:"
                    + index + "\t info.presentationTimeUs:" + info.presentationTimeUs);

            //设置缓冲区
            ByteBuffer byteBuffer = codec.getOutputBuffer(index);
            byteBuffer.position(info.offset);
            byteBuffer.limit(info.offset + info.size);


            byte[] outData = new byte[info.size + 7];
            //前七位添加头部
            addADTStoPacket(outData, outData.length);

            byteBuffer.get(outData, 7, info.size);

            try {
                bos.write(outData, 0, outData.length);//BufferOutputStream 将文件保存到内存卡中 *.aac
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        codec.releaseOutputBuffer(index, false);
        if (isEndOfStream) {
            fosRelase();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playMp4();

                }
            });
        }
    }

    private void fosRelase() {
        try {
            if (bos != null) {
                bos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    bos = null;
                }
            }
        }
        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fos = null;
        }
    }

    /**
     * Add ADTS header at the beginning of each and every AAC packet.
     * This is needed as MediaCodec encoder generates a packet of raw
     * AAC data.
     * <p>
     * Note the packetLen must count in the ADTS header itself.
     **/
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        //39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }


    private void playMp4() {
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(Uri.fromFile(aac));
        videoView.start();
        videoView.requestFocus();
    }
}
