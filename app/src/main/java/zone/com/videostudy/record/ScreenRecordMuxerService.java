package zone.com.videostudy.record;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import and.utils.data.file2io2data.FileUtils;
import and.utils.data.file2io2data.SDCardUtils;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecorder;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.Process;
import zone.com.videostudy.codec.utils.Callback;
import zone.com.videostudy.codec.utils.MediaCodecHelper;
import zone.com.videostudy.codec.utils.MediaFormatEs;
import zone.com.videostudy.record.uitlssss.MediaProjectionHelper;
import zone.com.videostudy.record.uitlssss.MediaRecorderHelper;
import zone.com.videostudy.record.uitlssss.VirtualDisplayParams;
import zone.com.videostudy.video.utils.VideoUtils;

/**
 * 因为 视频与声音 同时添加数据到Muxer上  并且 start之后不能addTrack，
 * 所以声音和视频 最后触发的那个  开始start。
 * <p>
 * release 同理  取最后那个触发  release()
 */
public class ScreenRecordMuxerService extends Service {

    File muxer = FileUtils.getFile(SDCardUtils.getSDCardDir(),
            "VideoStudyHei", "muxer_ScreenRecord.mp4");
    private boolean isVideoSd, isAudio;
    MediaProjectionHelper.Recorder mediaProjectionHelper = new MediaProjectionHelper.Recorder();
    private VirtualDisplayParams vdp;
    private MediaCodecHelper videoHelper, audioHelper;
    public MediaFormat forMat, audioFormat;
    private Surface surface;
    private MediaMuxer mMediaMuxer;
    MediaRecorderHelper mediaRecorderHelper;
    File mp4 = FileUtils.getFile(SDCardUtils.getSDCardDir(), "VideoStudyHei", "record_lopipMuxer.mp4");
    public int videoMuxTrack = -1, audioMuxTrack = -1;
    public boolean audioEncodeOver = false, videoEncodeOver = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean stop = intent.getBooleanExtra("stop", false);
        if (stop) {
            toStop();
            return Service.START_NOT_STICKY;
        }


        isVideoSd = intent.getBooleanExtra("quality", true);
        isAudio = intent.getBooleanExtra("audio", true);

        mediaProjectionHelper.startRecord(this, vdp = VirtualDisplayParams.readExtra(intent), new MediaProjectionHelper.Recorder.RecordNeed() {


            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public Surface getSurface() {
                try {
                    mMediaMuxer = new MediaMuxer(muxer.getAbsolutePath(),
                            MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    videoCodec();
                    audioCodec();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return surface;
            }


        });
        return Service.START_NOT_STICKY;
    }

    private void toStop() {
        videoHelper.signalEndOfInputStream();
        mAudioRecorder.stop();
    }

    private AudioRecorder mAudioRecorder = new AudioRecorder();

    private void audioCodec() throws IOException {
        audioFormat = MediaFormatEs.Audio.MP4A_LATM(mAudioRecorder.getAudioRecordConfig())
                .getFormat();
        audioHelper = MediaCodecHelper.encode(audioFormat)
                .createByCodecName()
                .callback(false, true, new Callback() {
                    @Override
                    public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

                    }

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info, boolean isEndOfStream) {
                        //设置缓冲区
                        ByteBuffer byteBuffer = codec.getOutputBuffer(index);
                        byteBuffer.position(info.offset);
                        byteBuffer.limit(info.offset + info.size);

                        pushMuxer(info, byteBuffer, audioMuxTrack);

                        codec.releaseOutputBuffer(index, false);
                        if (isEndOfStream) {
                            audioEncodeOver = true;
                            release();
                        }
                    }

                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                    @Override
                    public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat newFormat) {
                        audioMuxTrack = mMediaMuxer.addTrack(newFormat);
                        muxerStart();
                    }
                })
                .prepare();
        mAudioRecorder.process(
                new Process() {
                    @Override
                    public void init(AudioRecorder audioRecorder) throws Exception {
                    }

                    @Override
                    public int processData(AudioRecorder audioRecord, byte[] pcmData, int readsize) throws Exception {
                        if (readsize > 0) {
                            feedData(pcmData, readsize);
                        }
                        return readsize;
                    }


                    @Override
                    public void end() throws Exception {
                        feedData(null, -1);
                    }

                    @Override
                    public void release() throws Exception {

                    }
                })
                .start();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void pushMuxer(@NonNull MediaCodec.BufferInfo info, ByteBuffer byteBuffer, int muxTrack) {
        if (videoMuxTrack != -1 && audioMuxTrack != -1)
            mMediaMuxer.writeSampleData(muxTrack, byteBuffer, info);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void feedData(byte[] pcmData, int readsize) {
        MediaCodec codec2 = audioHelper.getMediaCodec();
        Log.d("helper___3333", " helper dequeueInputBuffer.....");
        int inputBufferIndex = codec2.dequeueInputBuffer(-1);
        Log.d("helper___3333", " helper dequeueInputBuffer inputBufferIndex :" + inputBufferIndex);
        if (inputBufferIndex >= 0) {
            //把取出空InputBuffer的写入
            ByteBuffer inputBuffer = codec2.getInputBuffer(inputBufferIndex);
            // 向输入缓存区写入有效原始数据，并提交到编码器中进行编码处理
            if (readsize <= 0) {
                audioHelper.signalEndOfQueueInputBuffer(inputBufferIndex);
            } else {
                inputBuffer.clear();
                inputBuffer.put(pcmData);
                codec2.queueInputBuffer(inputBufferIndex, 0, readsize, getPTSUs(), 0);
            }
        }
    }

    private long prevPresentationTimes = 0;

    private long getPTSUs() {
        long result = System.nanoTime() / 1000;
        if (result < prevPresentationTimes) {
            result = (prevPresentationTimes - result) + result;
        }
        return result;
    }


    private void videoCodec() throws IOException {
        forMat = MediaFormatEs.Video.H264().screen(vdp.width, vdp.height).safeCheck().getFormat();
        videoHelper = MediaCodecHelper.encode(forMat)
                .intputSurface(new MediaCodecHelper.IntputSurface() {
                    @Override
                    public void onCreate(Surface surface2) {
                        surface = surface2;
                    }
                })
                .createByCodecName()
                .callback(new Callback() {


                              @Override
                              public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                              }

                              @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                              @Override
                              public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
                                                                  @NonNull MediaCodec.BufferInfo info,
                                                                  boolean isEndOfStream) {
                                  //设置缓冲区
                                  ByteBuffer byteBuffer = codec.getOutputBuffer(index);
                                  byteBuffer.position(info.offset);
                                  byteBuffer.limit(info.offset + info.size);

                                  pushMuxer(info, byteBuffer, videoMuxTrack);
                                  codec.releaseOutputBuffer(index, false);
                                  if (isEndOfStream) {
                                      videoEncodeOver = true;
                                      release();
                                  }
                              }

                              @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                              @Override
                              public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                                  videoMuxTrack = mMediaMuxer.addTrack(format);
                                  muxerStart();
                              }
                          }
                ).prepare();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void muxerStart() {
        if (videoMuxTrack != -1 && audioMuxTrack != -1)
            mMediaMuxer.start();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void release() {
        if (!(audioEncodeOver && videoEncodeOver))
            return;

        videoHelper.release();
        audioHelper.release();

        try {
            mMediaMuxer.stop();
            mMediaMuxer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        VideoUtils.startSystemVideo(ScreenRecordMuxerService.this, Uri.fromFile(muxer));
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}