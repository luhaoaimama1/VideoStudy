package zone.com.videostudy.record.uitlssss;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;

public class VirtualDisplayParams {
    public String VirtualName = "ScreenCapture";
    public int width;
    public int height;
    public int densityDpi = -1;
    public int resultCode;
    public Intent data;

    private VirtualDisplayParams() {
    }

    public VirtualDisplayParams(DisplayMetrics metrics, int resultCode, Intent data) {
        densityDpi = metrics.densityDpi;
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        this.resultCode =resultCode;
        this.data =data;
    }

    public static VirtualDisplayParams getDefault(Activity activity, int resultCode, Intent data) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new VirtualDisplayParams(metrics,resultCode,data);
    }

    public void writeExtra(Intent service) {
        service.putExtra("code", resultCode);
        service.putExtra("data", data);
        service.putExtra("width", width);
        service.putExtra("height", height);
        service.putExtra("density", densityDpi);
        service.putExtra("name", VirtualName);
    }

    public static VirtualDisplayParams readExtra(Intent serviceIntent) {
        VirtualDisplayParams obj = new VirtualDisplayParams();
        obj.resultCode = serviceIntent.getIntExtra("code", -1);
        obj.data = serviceIntent.getParcelableExtra("data");
        obj.width = serviceIntent.getIntExtra("width", 720);
        obj.height = serviceIntent.getIntExtra("height", 1280);
        obj.densityDpi = serviceIntent.getIntExtra("density", 1);
        obj.VirtualName = serviceIntent.getStringExtra("name");
        return obj;
    }

}