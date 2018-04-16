package zone.com.videostudy.record.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

/**
 * [2017] by Zone
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AMediaProjection {

    private static final int NULL = 0;
    private static final int START = 1;
    private static final int STOP = 2;
    private int state = NULL;

    private static final String TAG = "MediaProjectionHelper";
    private final Activity activity;
    private final Callback mCallback;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private int mResultCode;
    private Intent mResultData;
    private int REQUEST_Code = 1001;
    private VirtualDisplayParams virtualDisplayParams;


    public AMediaProjection(Activity activity, Callback mCallback) {
        this.mCallback = mCallback;
        this.activity = activity;
    }

    public synchronized void requestOpen(boolean isOpen) {
        if (isOpen) {
            if (state != START)
                startScreenCapture();
        } else {
            if (state == START)
                stopScreenCapture();
        }
    }

    //延迟500ms 不然会卡死
    private void startScreenCapture() {
        if (mResultCode != 0 && mResultData != null) {
            //屏幕已经弹窗 可以录制了
            setUpMediaProjection();
            setUpVirtualDisplay();
        } else {
            Log.i(TAG, "Requesting confirmation");
            // This initiates a prompt dialog for the user to confirm screen projection.
            setUpMediaProjection();
            activity.startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_Code);
        }
    }

    private void setUpMediaProjection() {
        if (mMediaProjectionManager == null)
            mMediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_Code) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i(TAG, "User cancelled");
                Toast.makeText(activity, "权限拒绝", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i(TAG, "Starting screen capture");
            mResultCode = resultCode;
            mResultData = data;
            mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
            setUpVirtualDisplay();
        }
    }

    private void setUpVirtualDisplay() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mScreenDensity = metrics.densityDpi;
        virtualDisplayParams = mCallback.getParams();
        Log.i(TAG, "Setting up a VirtualDisplay: " +
                virtualDisplayParams.surfaceWidth + "x" + virtualDisplayParams.surfaceHeight +
                " (" + mScreenDensity + ")");
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(virtualDisplayParams.VirtualName,
                virtualDisplayParams.surfaceWidth, virtualDisplayParams.surfaceHeight,
                virtualDisplayParams.densityDpi == -1 ? mScreenDensity : virtualDisplayParams.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                virtualDisplayParams.surface, virtualDisplayParams.virtualCallback, null);
        mCallback.complete();
        state = START;
    }


    private void stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        state = STOP;
    }

    public void ondestory() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
            state = NULL;
        }
    }

    public VirtualDisplay getVirtualDisplay() {
        return mVirtualDisplay;
    }

    public int getREQUEST_Code() {
        return REQUEST_Code;
    }

    public void setREQUEST_Code(int REQUEST_Code) {
        this.REQUEST_Code = REQUEST_Code;
    }

    public void resize(int width, int height, int densityDpi) {
        mVirtualDisplay.resize(width, height, densityDpi);
    }

    public interface Callback {
        //构造之前
        VirtualDisplayParams getParams();

        //构造完成
        void complete();
    }

    public static class VirtualDisplayParams {
        public Surface surface;
        public int surfaceWidth;
        public int surfaceHeight;
        public int densityDpi = -1;
        public VirtualDisplay.Callback virtualCallback;
        public String VirtualName = "ScreenCapture";
    }

}
