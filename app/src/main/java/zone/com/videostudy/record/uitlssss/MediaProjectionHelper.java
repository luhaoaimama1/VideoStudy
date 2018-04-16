package zone.com.videostudy.record.uitlssss;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import static and.utils.activity_fragment_ui.FragmentSwitcher.TAG;

/**
 * MIT License
 * Copyright (c) [2018] [Zone]
 * 参考：https://blog.csdn.net/jiang_zhuo_yan/article/details/51126234
 */
public class MediaProjectionHelper {
    public static int REQUEST_Code = 1000;

    public static class Permission {
        private MediaProjectionManager mMediaProjectionManager;

        private Activity activity;

        private MediaProjectionManager getMediaProjectionManager(Context context) {
            return mMediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }

        //        获取屏幕录制的权限
        public void applyRecordPermission(Activity activity) {
            this.activity = activity;
            getMediaProjectionManager(activity);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(),
                        REQUEST_Code);
            } else {
                Toast.makeText(activity, "版本低于LOLLIPOP", Toast.LENGTH_SHORT).show();
            }
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data, ResultOK resultOK) {
            if (requestCode == REQUEST_Code) {
                if (resultCode != Activity.RESULT_OK) {
                    Log.i(TAG, "User cancelled");
                    Toast.makeText(activity, "权限拒绝", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "Starting screen capture");
                resultOK.onResultOK(resultOK.getParams());
            }
        }

    }


    public static class Recorder {

        private VirtualDisplay mVirtualDisplay;
        private MediaProjection mMediaProjection;
        private boolean isRun;

        public MediaProjectionManager getMediaProjectionManager(Context context) {
            return (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }

        public void startRecord(Context context, VirtualDisplayParams virtualDisplayParams, RecordNeed recordNeed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaProjection = getMediaProjectionManager(context)
                        .getMediaProjection(virtualDisplayParams.resultCode, virtualDisplayParams.data);
                mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                        virtualDisplayParams.VirtualName, virtualDisplayParams.width, virtualDisplayParams.height,
                        virtualDisplayParams.densityDpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        recordNeed.getSurface(), recordNeed.getCallback(), recordNeed.getHandler());
                isRun=true;
            }
        }

        public boolean isRun() {
            return isRun;
        }

        /**
         * 三个都可以null
         */
        public static abstract class RecordNeed {

            public abstract Surface getSurface();

            public VirtualDisplay.Callback getCallback() {
                return null;
            }

            public Handler getHandler() {
                return null;
            }
        }

        public void onDestory() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mVirtualDisplay != null) {
                    mVirtualDisplay.release();
                    mVirtualDisplay = null;
                }
                if (mMediaProjection != null) {
                    mMediaProjection.stop();
                    mMediaProjection = null;
                }
                isRun=false;
            }
        }

        public VirtualDisplay getmVirtualDisplay() {
            return mVirtualDisplay;
        }

        public MediaProjection getmMediaProjection() {
            return mMediaProjection;
        }

    }

}
