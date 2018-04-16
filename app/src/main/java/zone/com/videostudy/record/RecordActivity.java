package zone.com.videostudy.record;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import zone.com.videostudy.R;
import zone.com.videostudy.record.utils.AMediaProjection;

/**
 * Created by fuzhipeng on 16/7/25.
 */
public class RecordActivity extends Activity {
    private SurfaceView mSurfaceView;
    private Surface mSurface;
    private Button mButtonToggle;
    private AMediaProjection mph;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_capture);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mSurface = mSurfaceView.getHolder().getSurface();
        mButtonToggle = (Button)findViewById(R.id.toggle);
        mph=new AMediaProjection(this,new AMediaProjection.Callback(){


            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public AMediaProjection.VirtualDisplayParams getParams() {
                AMediaProjection.VirtualDisplayParams params =
                        new AMediaProjection.VirtualDisplayParams();
                params.surfaceHeight=mSurfaceView.getHeight();
                params.surfaceWidth=mSurfaceView.getWidth();
                params.surface=mSurface;
                return params;
            }

            @Override
            public void complete() {

            }
        });

        mButtonToggle.setText("Start!");
        mButtonToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public synchronized void onClick(View v) {
                if (mButtonToggle.getText().equals("Start!")) {
                    mph.requestOpen(true);
                    mButtonToggle.setText("Stop!");

                } else {
                    mph.requestOpen(false);
                    mButtonToggle.setText("Start!");
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mph.requestOpen(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mph.ondestory();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mph.onActivityResult(requestCode, resultCode, data);
    }
}
