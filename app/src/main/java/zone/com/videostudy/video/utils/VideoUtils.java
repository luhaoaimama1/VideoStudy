package zone.com.videostudy.video.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 */

public class VideoUtils {

    //调用系统自带的播放器
    public  static void   startSystemVideo(Activity activity , Uri videoUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Log.v("URI:::::::::", videoUri.toString());
        intent.setDataAndType(videoUri, "video/mp4");
        activity.startActivity(intent);
    }
    //调用系统自带的播放器
    public  static void   startSystemVideo(Service service , Uri videoUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Log.v("URI:::::::::", videoUri.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(videoUri, "video/mp4");
        service.startActivity(intent);
    }
}
