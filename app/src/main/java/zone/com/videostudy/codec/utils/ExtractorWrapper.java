package zone.com.videostudy.codec.utils;

import android.annotation.TargetApi;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;

import java.io.IOException;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public  class ExtractorWrapper {
    public static final String VIDEO = "video/";
    public static final String AUDIO = "audio/";

    public boolean isAudio;
    public MediaExtractor extractor;
    public int trackIndex = -1;
    public int muxTrackIndex = -1;
    public MediaFormat format;

    private ExtractorWrapper() {
    }

    public void addTrackMuxer(MediaMuxer muxer) {
        muxTrackIndex = muxer.addTrack(format);
    }

    public static ExtractorWrapper getExtractor(String filePath, String contains) throws IOException {
        ExtractorWrapper extractorWrapper = new ExtractorWrapper();
        // 视频的MediaExtractor
        MediaExtractor mVideoExtractor = new MediaExtractor();
        extractorWrapper.extractor = mVideoExtractor;
        mVideoExtractor.setDataSource(filePath);
        for (int i = 0; i < mVideoExtractor.getTrackCount(); i++) {
            MediaFormat format = mVideoExtractor.getTrackFormat(i);
//查找是否支持编解码功能                new MediaCodecList(1).findDecoderForFormat(warper.format)
            if (format.getString(MediaFormat.KEY_MIME).contains(contains)) {
                extractorWrapper.format = format;
                extractorWrapper.trackIndex = i;
                extractorWrapper.isAudio = format.getString(MediaFormat.KEY_MIME).contains(AUDIO);
                break;
            }
        }
        return extractorWrapper;
    }

    public void release() {
        if (extractor != null) {
            extractor.release();
            extractor = null;
        }

    }

}