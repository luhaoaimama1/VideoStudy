package zone.com.videostudy.audiomedia.utilsnow.audio.record.audio.utils;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class VolumeUtils {

    private static final int MAX_VOLUME = 2000;
    private int mVolume;

    /**
     * 此计算方法来自samsung开发范例
     *  @param buffer   buffer
     * @param readSize readSize
     */
    public int calculateRealVolume(byte[] buffer, int readSize) {
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
            // 这里没有做运算的优化，为了更加清晰的展示代码
            sum += buffer[i] * buffer[i];
        }
        if (readSize > 0) {
            double amplitude = sum / readSize;
            mVolume = (int) Math.sqrt(amplitude);
        }
        return mVolume;

    }

    /**
     * 获取真实的音量。 [算法来自三星]
     *
     * @return 真实音量
     */
    public int getRealVolume() {
        return mVolume;
    }

    /**
     * 获取相对音量。 超过最大值时取最大值。
     *
     * @return 音量
     */
    public int getVolume() {
        if (mVolume >= MAX_VOLUME) {
            return MAX_VOLUME;
        }
        return mVolume;
    }


}
