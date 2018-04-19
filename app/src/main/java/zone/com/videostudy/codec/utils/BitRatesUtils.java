package zone.com.videostudy.codec.utils;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class BitRatesUtils {
    public static int getVideoBitRate(MediaFormatEs.Quality quality, int width, int height) {
        int bitRate = (int) (width * height * 20 * 2 * 0.07f);
        if (width >= 1920 || height >= 1920) {
            switch (quality) {
                case LOW:
                    bitRate *= 0.75;// 4354Kbps
                    break;
                case MIDDLE:
                    bitRate *= 1.1;// 6386Kbps
                    break;
                case HIGH:
                    bitRate *= 1.5;// 8709Kbps
                    break;
            }
        } else if (width >= 1280 || height >= 1280) {
            switch (quality) {
                case LOW:
                    bitRate *= 1.0;// 2580Kbps
                    break;
                case MIDDLE:
                    bitRate *= 1.4;// 3612Kbps
                    break;
                case HIGH:
                    bitRate *= 1.9;// 4902Kbps
                    break;
            }
        } else if (width >= 640 || height >= 640) {
            switch (quality) {
                case LOW:
                    bitRate *= 1.4;// 1204Kbps
                    break;
                case MIDDLE:
                    bitRate *= 2.1;// 1806Kbps
                    break;
                case HIGH:
                    bitRate *= 3;// 2580Kbps
                    break;
            }
        }
        return bitRate;
    }
}
