package zone.com.videostudy.codec.utils;


import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import java.util.HashMap;
import java.util.Map;
import and.utils.reflect.Reflect;
import zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.AudioRecordConfig;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */
public class MediaFormatEs {

    public static MediaFormat deepClone(MediaFormat mediaFormat) {
        Map<String,Object> oldMap=Reflect.on(mediaFormat).get("mMap");
        Map<String,Object> newMap= new HashMap();
        oldMap.putAll(newMap);
        for (String s : oldMap.keySet())
            newMap.put(s,oldMap.get(s));

        MediaFormat newFormat = new MediaFormat();
        Reflect.on(newFormat)
                .set("mMap",newMap);
        return newFormat;
    }

    // 帧率  这两种支持的好
    public enum FrameRate {
        _25fps(25), _30fps(30);
        int frameRate;

        FrameRate(int frameRate) {
            this.frameRate = frameRate;
        }

        public int getFrameRate() {
            return frameRate;
        }
    }

    // 码率等级
    public enum Quality {
        LOW, MIDDLE, HIGH;
    }

    /**
     * 写法：video.h264
     * .screen(width,height)
     * .mime(mime)
     * .colorFormat(mime)
     * .bitRate(Quality.High/数字)
     * .iFrameInterval()
     * .frameRate(High)
     * //使用这个自由调用吧
     * .getMediaFormat();
     */
    public static class Video {
        static final int DEFAULT_BITRATE = 2000000;
        static final FrameRate DEFAULT_FRAMERATE = FrameRate._30fps;
        static final int DEFAULT_IFRAMEINTERVAL = 10;

        // =======================================
        // ============快速方法 ==============
        // =======================================
        public static Video H264() {
            return new Video(MediaFormat.MIMETYPE_VIDEO_AVC,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
                    DEFAULT_BITRATE,
                    DEFAULT_FRAMERATE,
                    DEFAULT_IFRAMEINTERVAL);
        }

        MediaFormat format;

        public Video() {
            format = new MediaFormat();
        }

        private Video(String mime, int colorFormat, int bitRate
                , FrameRate frameRate, int iFrameInterval) {
            format = new MediaFormat();
            this.colorFormat(colorFormat)
                    .mime(mime)
                    .bitRate(bitRate)
                    .frameRate(frameRate)
                    .iFrameInterval(iFrameInterval);

        }

        /**
         * 视频的尺寸，这个尺寸不能超过视频采集时采集到的尺寸，否则会直接crash
         *
         * @param width
         * @param height
         * @return
         */
        public Video screen(int width, int height) {
            format.setInteger(MediaFormat.KEY_WIDTH, width);
            format.setInteger(MediaFormat.KEY_HEIGHT, height);
            return this;
        }

        public Video mime(String mime) {
            format.setString(MediaFormat.KEY_MIME, mime);
            return this;
        }

        /**
         * 这里表明数据将是一个graphicbuffer元数据
         *
         * @param colorFormat
         * @return
         */
        public Video colorFormat(int colorFormat) {
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            return this;
        }

        /**
         * 设置码率，通常码率越高，视频越清晰，但是对应的视频也越大，这个值我默认设置成了2000000，
         * 也就是通常所说的2M，这已经不低了，如果你不想录制这么清晰的，你可以设置成500000，也就是500k
         *
         * @param bitRate
         * @return
         */
        public Video bitRate(int bitRate) {
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
            return this;
        }

        public Video bitRate(Quality quality) {
            //这里如果没有会崩溃
            int width = format.getInteger(MediaFormat.KEY_WIDTH);
            int height = format.getInteger(MediaFormat.KEY_HEIGHT);
            format.setInteger(MediaFormat.KEY_BIT_RATE,
                    BitRatesUtils.getVideoBitRate(quality, width, height));
            return this;
        }

        /**
         * 设置两个关键帧的间隔，这个值你设置成多少对我们这个例子都没啥影响
         * 这个值做视频的朋友可能会懂，反正我不是很懂，大概就是你预览的时候
         * ，比如你设置为10，那么你10秒内的预览图都是同一张
         *
         * @param iFramInterval
         * @return
         */
        public Video iFrameInterval(int iFramInterval) {
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFramInterval);
            return this;
        }

        /**
         * 设置帧率，通常这个值越高，视频会显得越流畅，一般默认我设置成30，你最低可以设置成24，
         * 不要低于这个值，低于24会明显卡顿
         *
         * @param frameRate
         * @return
         */
        public Video frameRate(int frameRate) {
            format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            return this;
        }

        /**
         * 设置帧率，通常这个值越高，视频会显得越流畅，一般默认我设置成30，你最低可以设置成24，
         * 不要低于这个值，低于24会明显卡顿
         *
         * @param frameRate
         * @return
         */
        public Video frameRate(FrameRate frameRate) {
            format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate.getFrameRate());
            return this;
        }

        /**
         * .screen(width,height)
         * .mime(mime)
         * .colorFormat(mime)
         * .bitRate(Quality.High/数字)
         * .iFrameInterval()
         * .frameRate(High)
         * 检查最少使用的
         *
         * @return
         */
        public Video safeCheck() {
            format.getString(MediaFormat.KEY_MIME);
            format.getInteger(MediaFormat.KEY_WIDTH);
            format.getInteger(MediaFormat.KEY_HEIGHT);

            format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
            format.getInteger(MediaFormat.KEY_BIT_RATE);
            format.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL);
            format.getInteger(MediaFormat.KEY_FRAME_RATE);
            return this;
        }

        public MediaFormat getFormat() {
            return format;
        }

    }

    /**
     * mimi,channel_count,key_sample_rate,key_bit_rate,key_aac_profile,key_max_input_size)
     * 写法：audio.h264
     * .mimi(mimi)
     * .channelCount(channelCount)
     * .sampleRate(mime)
     * .bitRate(Quality.High/数字)
     * .aacProfile()
     * .maxInputSize(High)
     * .getMediaFormat();
     * <p>
     * mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
     * //        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
     * //        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);// AAC-HE // 64kbps
     * //        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
     */
    public static class Audio {

        static final int BIT_RATE = 64000;

        public static Audio MP4A_LATM(AudioRecordConfig audioRecordConfig) {
            return  new Audio(MediaFormat.MIMETYPE_AUDIO_AAC,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC,
                    audioRecordConfig);
        }

        MediaFormat format;

        public Audio() {
            format = new MediaFormat();
        }

        private Audio(String mime, int aacProfile, AudioRecordConfig audioRecordConfig) {
            format = new MediaFormat();
            this.mime(mime)
                    .aacProfile(aacProfile)
                    .bitRate(BIT_RATE)
                    .channelCount(audioRecordConfig.getChannelCount())
                    .sampleRate(audioRecordConfig.getSampleRate())
                    .maxInputSize(audioRecordConfig.getMinBufferSizeAT());
        }

        public Audio mime(String mime) {
            format.setString(MediaFormat.KEY_MIME, mime);
            return this;
        }

        public Audio channelCount(int channelCount) {
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
            return this;
        }

        public Audio sampleRate(int sampleRate) {
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
            return this;
        }

        public Audio aacProfile(int aacProfile) {
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, aacProfile);
            return this;
        }

        public Audio maxInputSize(int maxInputSize) {
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);
            return this;
        }

        /**
         * 设置码率，通常码率越高，视频越清晰，但是对应的视频也越大，这个值我默认设置成了2000000，
         * 也就是通常所说的2M，这已经不低了，如果你不想录制这么清晰的，你可以设置成500000，也就是500k
         *
         * @param bitRate
         * @return
         */
        public Audio bitRate(int bitRate) {
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
            return this;
        }

        public Audio bitRate(Quality quality) {
            //这里如果没有会崩溃
            int width = format.getInteger(MediaFormat.KEY_WIDTH);
            int height = format.getInteger(MediaFormat.KEY_HEIGHT);
            format.setInteger(MediaFormat.KEY_BIT_RATE,
                    BitRatesUtils.getVideoBitRate(quality, width, height));
            return this;
        }

        /**
         * 设置帧率，通常这个值越高，视频会显得越流畅，一般默认我设置成30，你最低可以设置成24，
         * 不要低于这个值，低于24会明显卡顿
         *
         * @param frameRate
         * @return
         */
        public Audio frameRate(int frameRate) {
            format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            return this;
        }

        /**
         * 设置帧率，通常这个值越高，视频会显得越流畅，一般默认我设置成30，你最低可以设置成24，
         * 不要低于这个值，低于24会明显卡顿
         *
         * @param frameRate
         * @return
         */
        public Audio frameRate(FrameRate frameRate) {
            format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate.getFrameRate());
            return this;
        }

        /**
         * mimi,channel_count,key_sample_rate,key_bit_rate)
         *
         * @param frameRate
         * @return
         */
        public Audio safeCheck(FrameRate frameRate) {
            format.getString(MediaFormat.KEY_MIME);
            int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            if (!(channelCount == 1 || channelCount == 2))
                throw new IllegalStateException("内置无法处理此格式");

            format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            format.getInteger(MediaFormat.KEY_BIT_RATE);
            return this;
        }


        public MediaFormat getFormat() {
            return format;
        }


    }

}


