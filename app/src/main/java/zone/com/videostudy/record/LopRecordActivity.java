package zone.com.videostudy.record;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import zone.com.videostudy.R;
import zone.com.videostudy.record.uitlssss.MediaProjectionHelper;
import zone.com.videostudy.record.uitlssss.ResultOK;
import zone.com.videostudy.record.uitlssss.VirtualDisplayParams;

/**
 * 屏幕录制 配合MediaRecord
 */
public class LopRecordActivity extends Activity {

    private static final String TAG = "MainActivity";

    private TextView mTextView;

    private static final String RECORD_STATUS = "record_status";

    /**
     * 是否已经开启视频录制
     */
    private boolean isStarted = false;
    /**
     * 是否为标清视频
     */
    private boolean isVideoSd = true;
    /**
     * 是否开启音频录制
     */
    private boolean isAudio = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub  
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_lop_media);

        Log.i(TAG, "onCreate");
        if (savedInstanceState != null) {
            isStarted = savedInstanceState.getBoolean(RECORD_STATUS);
        }
        getView();
    }

    private void getView() {
        mTextView = (TextView) findViewById(R.id.button_control);
        if (isStarted) {
            statusIsStarted();
        } else {
            statusIsStoped();
        }
        mTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub  
                if (isStarted) {
                    stopScreenRecording();
                    statusIsStoped();
                    Log.i(TAG, "Stoped screen recording");
                } else {
                    mediaProjectionHelper.applyRecordPermission(LopRecordActivity.this);
                }
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.sd_button:
                        isVideoSd = true;
                        break;
                    case R.id.hd_button:
                        isVideoSd = false;
                        break;

                    default:
                        break;
                }
            }
        });

        CheckBox audioBox = (CheckBox) findViewById(R.id.audio_check_box);
        audioBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub  
                isAudio = isChecked;
            }
        });
    }

    /**
     * 开启屏幕录制时的UI状态
     */
    private void statusIsStarted() {
        mTextView.setText("停止录制");
    }

    /**
     * 结束屏幕录制后的UI状态
     */
    private void statusIsStoped() {
        mTextView.setText("开始录制");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub  
        super.onSaveInstanceState(outState);
        outState.putBoolean(RECORD_STATUS, isStarted);
    }

    MediaProjectionHelper.Permission mediaProjectionHelper = new MediaProjectionHelper.Permission();

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mediaProjectionHelper.onActivityResult(requestCode, resultCode, data, new ResultOK() {

            @Override
            public void onResultOK(VirtualDisplayParams params) {
                Intent service = new Intent(LopRecordActivity.this, ScreenRecordService.class);
                params.writeExtra(service);
                startService(service);
                // 已经开始屏幕录制，修改UI状态
                isStarted = !isStarted;
                statusIsStarted();
                simulateHome(); // this.finish();  // 可以直接关闭Activity
            }

            @Override
            public VirtualDisplayParams getParams() {
                return VirtualDisplayParams.getDefault(LopRecordActivity.this, resultCode, data);
            }
        });
    }

    /**
     * 关闭屏幕录制，即停止录制Service
     */
    private void stopScreenRecording() {
        Intent service = new Intent(this, ScreenRecordService.class);
        stopService(service);
        isStarted = !isStarted;
    }

    /**
     * 模拟HOME键返回桌面的功能
     */
    private void simulateHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 在这里将BACK键模拟了HOME键的返回桌面功能（并无必要）
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            simulateHome();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}  