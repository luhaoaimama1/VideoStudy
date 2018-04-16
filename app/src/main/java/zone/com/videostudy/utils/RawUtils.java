package zone.com.videostudy.utils;

import android.content.Context;
import android.support.annotation.RawRes;
import java.io.File;
import java.io.InputStream;

import and.utils.data.file2io2data.IOUtils;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 *
 * assets和res/raw目录的相同点：
 *     两者目录下的文件在打包后会原封不动的保存在apk包中，不会被编译成二进制。
 *
 * res/raw和assets的不同点：
 *     res/raw中的文件会被映射到R.java文件中，访问的时候直接使用资源id即R.id.filename；
 *     assets文件夹下的文件不会被映射到R.java中，访问的时候需要AssetManager类。
 *     res/raw不可以有目录结构，而assets则可以有目录结构，也就是assets目录下可以再建立文件夹。
 */

public class RawUtils {

    private static final String SEPARATOR = File.separator;//路径分隔符
    /**
     * res/raw中的文件会被映射到R.java文件中
     * @param context
     * @param id          资源ID
     * @param storagePath
     */
    public static void copyFilesFromRaw(Context context, @RawRes int id, String storagePath) {
        InputStream inputStream = context.getResources().openRawResource(id);
        try {
            IOUtils.write(new File(storagePath), inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * assets文件夹下的文件不会被映射到R.java中
     *
     * @param context
     * @param id          资源ID
     * @param storagePath
     */
    public static void copyFilesFromAssset(Context context, String fileName, String storagePath) {
        try {
            IOUtils.write(new File(storagePath), context.getResources().getAssets().open(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
