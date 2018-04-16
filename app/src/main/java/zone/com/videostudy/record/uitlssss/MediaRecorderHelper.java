package zone.com.videostudy.record.uitlssss;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class MediaRecorderHelper {

    private static final String TAG = "MediaRecorderHelper";

    private MediaRecorder mediaRecorder;
    private Audio audio;
    private Video video;
    private OutputFormat outputFormat;
    private String filePath;
    public interface  EndSymbol{
        String getFileSavePath(String endSymbol);
    }

    public MediaRecorderHelper(Audio audio, Video video, EndSymbol endSymbol) {
        this.audio = audio;
        this.video = video;
        if (audio == null && video == null) {
            throw new IllegalStateException("不能同时为空");
        }

        if (video != null) {
            outputFormat = OutputFormat.Video;
        } else {
            outputFormat = OutputFormat.Audio;
        }
        filePath=endSymbol.getFileSavePath(outputFormat.endSymbol);

    }

    private enum OutputFormat {
        Audio(MediaRecorder.OutputFormat.AMR_NB, ".amr"),
        Video(MediaRecorder.OutputFormat.THREE_GPP, ".mp4");
        int output_format;
        String endSymbol;

        OutputFormat(int output_format, String fileEndStr) {
            this.output_format = output_format;
            this.endSymbol = fileEndStr;
        }
    }


    /**
     * video
     *      1.surface设置上
     *      2.mediaRecorder.start();
     *
     * audio: mediaRecorder.start();
     * @return
     */
    public MediaRecorder createMediaRecorder() {
        Log.i(TAG, "Create MediaRecorder");
        mediaRecorder = new MediaRecorder();
        if (audio!=null) mediaRecorder.setAudioSource(audio.audio_source);
        if (video!=null)mediaRecorder.setVideoSource(video.video_source);
        mediaRecorder.setOutputFormat(outputFormat.output_format);
        mediaRecorder.setOutputFile(filePath);
        if (video!=null){
            mediaRecorder.setVideoSize(video.mScreenWidth, video.mScreenHeight);  //after setVideoSource(), setOutFormat()
            mediaRecorder.setVideoEncoder(video.video_encoder);  //after setOutputFormat()
        }
        if (audio!=null) mediaRecorder.setAudioEncoder(audio.audio_encoder);  //after setOutputFormat()
        if (video!=null) {
            mediaRecorder.setVideoEncodingBitRate(video.VideoStyle.bitRateRadio * video.mScreenWidth * video.mScreenHeight);
            mediaRecorder.setVideoFrameRate(video.VideoStyle.rate);
            int bitRate = video.VideoStyle.bitRateRadio *  video.mScreenWidth * video.mScreenHeight / 1000;
            Log.i(TAG, "Audio: " + (audio!=null) + ", Video is HD or SD: " + video.VideoStyle.name() + ", BitRate: " + bitRate + "kbps");
        }
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        return mediaRecorder;
    }

    public void release() {
        if (mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }




    public  static enum VideoStyle {
        SD(30, 1), HD(60, 5);
        int rate, bitRateRadio;

        VideoStyle(int rate, int bitRateRadio) {
            this.rate = rate;
            this.bitRateRadio = bitRateRadio;
        }
    }


    public static class Audio {
        int audio_source = MediaRecorder.AudioSource.MIC;
        int audio_encoder = MediaRecorder.AudioEncoder.AAC;

        public int getAudio_source() {
            return audio_source;
        }

        public void setAudio_source(int audio_source) {
            this.audio_source = audio_source;
        }

        public int getAudio_encoder() {
            return audio_encoder;
        }

        public void setAudio_encoder(int audio_encoder) {
            this.audio_encoder = audio_encoder;
        }
    }

    public static class Video {
        int video_source = MediaRecorder.VideoSource.SURFACE;
        int video_encoder = MediaRecorder.VideoEncoder.H264;
        int mScreenWidth = -1, mScreenHeight = -1;
        VideoStyle VideoStyle;

        public Video(int mScreenWidth, int mScreenHeight, MediaRecorderHelper.VideoStyle videoStyle) {
            this.mScreenWidth = mScreenWidth;
            this.mScreenHeight = mScreenHeight;
            VideoStyle = videoStyle;
        }

        public int getVideo_source() {
            return video_source;
        }

        public void setVideo_source(int video_source) {
            this.video_source = video_source;
        }

        public int getVideo_encoder() {
            return video_encoder;
        }

        public void setVideo_encoder(int video_encoder) {
            this.video_encoder = video_encoder;
        }

        public int getmScreenWidth() {
            return mScreenWidth;
        }

        public void setmScreenWidth(int mScreenWidth) {
            this.mScreenWidth = mScreenWidth;
        }

        public int getmScreenHeight() {
            return mScreenHeight;
        }

        public void setmScreenHeight(int mScreenHeight) {
            this.mScreenHeight = mScreenHeight;
        }

        public MediaRecorderHelper.VideoStyle getVideoStyle() {
            return VideoStyle;
        }

        public void setVideoStyle(MediaRecorderHelper.VideoStyle videoStyle) {
            VideoStyle = videoStyle;
        }
    }


}
