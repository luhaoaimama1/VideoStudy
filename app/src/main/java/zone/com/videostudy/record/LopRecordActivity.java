package zone.com.videostudy.record;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
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
    private boolean isVideoSd = false;
    private boolean openIsMediaRecord = true;
    /**
     * 是否开启音频录制
     */
    private boolean isAudio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        RadioGroup radioGroup2 = (RadioGroup) findViewById(R.id.radio_group2);
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_open_codec:
                        openIsMediaRecord = false;
                        break;
                    case R.id.rb_open_mediaRecord:
                        openIsMediaRecord = true;
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
                isAudio = isChecked;
            }
        });
        audioBox.performClick();
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
                Intent service = new Intent(LopRecordActivity.this,
                        getOpenServiceCls());
                params.writeExtra(service);
                service.putExtra("quality",isVideoSd);
                service.putExtra("audio",isAudio);
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

    @NonNull
    private Class<? extends Service> getOpenServiceCls() {
        return openIsMediaRecord ? ScreenRecordService.class : ScreenRecordMuxerService.class;
    }

    /**
     * 关闭屏幕录制，即停止录制Service
     */
    private void stopScreenRecording() {
        Intent service = new Intent(this, getOpenServiceCls());
        if (openIsMediaRecord)
            stopService(service);
        else{
            service.putExtra("stop",true);
            startService(service);
        }
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

}  