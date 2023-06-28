package com.fishsemi.gcsexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fishsemi.sdk.aircontrol.AirControlListener;
import com.fishsemi.sdk.aircontrol.AirController;
import com.fishsemi.sdk.d2dcontrol.D2dControlListener;
import com.fishsemi.sdk.d2dcontrol.D2dController;

public class MainActivity extends Activity {

    private final String TAG = "MainActivity";

    private TextView mVideoStremIdInfo;
    private TextView mCaptureStateInfo;
    private TextView mCaptureModeInfo;
    private TextView mVideoRecordingInfo;
    private TextView mFreeSpaceInfo;
    private TextView mUart1Info;
    private TextView mUart2Info;
    private TextView mD2dConnectionInfo;
    private TextView mD2dSignalAirInfo;
    private TextView mD2dSignalControllerInfo;

    private Button mPairButton;
    private Button mToggleModeButton;
    private Button mPhotoButton;
    private Button mStartVideoButton;
    private Button mStopVideoButton;
    private Button mSetVideoStreamButton;
    private Button mSetUartButton;

    private TextView mPairPrompt;
    private TextView mToggleModePrompt;
    private TextView mPhotoPrompt;
    private TextView mStartVideoPrompt;
    private TextView mStopVideoPrompt;
    private TextView mSetVideoStreamPrompt;
    private TextView mSetUartPrompt;

    private EditText mUartIdEdit;
    private EditText mUartBaudEdit;

    private AirController mAirController;
    private D2dController mD2dController;

    private AirController.CaptureMode mCaptureMode = AirController.CaptureMode.UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoStremIdInfo = (TextView)findViewById(R.id.info_video_stream_id);
        mCaptureStateInfo = (TextView)findViewById(R.id.info_capture_state);
        mCaptureModeInfo = (TextView)findViewById(R.id.info_capture_mode);
        mVideoRecordingInfo = (TextView)findViewById(R.id.info_video_recording_status);
        mFreeSpaceInfo = (TextView)findViewById(R.id.info_free_space);
        mUart1Info = (TextView)findViewById(R.id.info_uart1_baud);
        mUart2Info = (TextView)findViewById(R.id.info_uart2_baud);
        mUart2Info.setVisibility(View.INVISIBLE);

        mPairPrompt = (TextView)findViewById(R.id.pair_prompt);
        mPairButton = (Button)findViewById(R.id.pair);
        mPairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mD2dController.requestPairAir();
                mPairPrompt.setText(getString(R.string.prompt_result_d2d_pair_waiting));
            }
        });

        mToggleModePrompt = (TextView)findViewById(R.id.toggle_mode_prompt);
        mToggleModeButton = (Button)findViewById(R.id.toggle_mode);
        mToggleModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AirController.CaptureMode mode = AirController.CaptureMode.PHOTO;
                if (mCaptureMode == AirController.CaptureMode.PHOTO) {
                    mode = AirController.CaptureMode.VIDEO;
                }
                AirController.RetValue ret = mAirController.toggleCaptureMode(mode);
                showRetValue(ret, mToggleModePrompt, getString(R.string.prompt_toggle_mode));
            }
        });

        mPhotoPrompt = (TextView)findViewById(R.id.photo_prompt);
        mPhotoButton = (Button)findViewById(R.id.photo);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AirController.RetValue ret = mAirController.takePhoto();
                showRetValue(ret, mPhotoPrompt, getString(R.string.prompt_taking_photo));
            }
        });

        mStartVideoPrompt = (TextView)findViewById(R.id.start_video_prompt);
        mStartVideoButton = (Button)findViewById(R.id.start_video);
        mStartVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AirController.RetValue ret = mAirController.startVideoRecording();
                showRetValue(ret, mStartVideoPrompt, getString(R.string.prompt_starting_video_record));
            }
        });

        mStopVideoPrompt = (TextView)findViewById(R.id.stop_video_prompt);
        mStopVideoButton = (Button)findViewById(R.id.stop_video);
        mStopVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AirController.RetValue ret = mAirController.stopVideoRecording();
                showRetValue(ret, mStopVideoPrompt, getString(R.string.prompt_stopping_video_record));
            }
        });

        mSetVideoStreamPrompt = (TextView)findViewById(R.id.stream_prompt);
        mSetVideoStreamButton = (Button)findViewById(R.id.switch_stream);
        mSetVideoStreamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = mAirController.getVideoStreamId();
                AirController.RetValue ret = mAirController.setVideoStreamId(id==0 ? 1: 0);
                showRetValue(ret, mSetVideoStreamPrompt, getString(R.string.prompt_setting_video_stream_id));
            }
        });

        mSetUartPrompt = (TextView)findViewById(R.id.set_uart_prompt);
        mUartIdEdit = (EditText)findViewById(R.id.input_uart_id);
        mUartBaudEdit = (EditText)findViewById(R.id.input_uart_baud);
        mSetUartButton = (Button)findViewById(R.id.set_uart);
        mSetUartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUartIdEdit.getText().toString().isEmpty()
                        || mUartBaudEdit.getText().toString().isEmpty()) {
                    mSetUartPrompt.setText(getString(R.string.prompt_input_valid));
                    return;
                };
                try {
                    int id = Integer.valueOf(mUartIdEdit.getText().toString());
                    int baud = Integer.valueOf(mUartBaudEdit.getText().toString());
                    AirController.RetValue ret = mAirController.setUartBaudrate(id, baud);
                    showRetValue(ret, mSetUartPrompt, getString(R.string.prompt_setting_uart));
                } catch (Exception e) {
                    mSetUartPrompt.setText(getString(R.string.prompt_input_valid));
                    return;
                }

            }
        });

        mAirController = new AirController(this, new AirControlListener() {
            @Override
            public void onCameraReady(boolean ready) {
                // camera is ready, set video stream id can work now
            }

            @Override
            public void onCaptureReady(boolean ready) {
                // camera capture is ready, photo and video capture can work now
                String str = ready ? getString(R.string.info_ready) : getString(R.string.info_not_ready);
                mCaptureStateInfo.setText(getString(R.string.info_capture_state)+str);
            }

            @Override
            public void onToggleCaptureModeDone(AirController.Result result) {
                Log.d(TAG, "toggle capture mode done :"+result);
                showResult(result, mToggleModePrompt, getString(R.string.prompt_toggle_mode));
            }

            @Override
            public void onTakePhotoDone(AirController.Result result) {
                Log.d(TAG, "take photo done :"+result);
                showResult(result, mPhotoPrompt, getString(R.string.prompt_taking_photo));
            }

            @Override
            public void onStartVideoRecordingDone(AirController.Result result) {
                Log.d(TAG, "start video recording done :"+result);
                showResult(result, mStartVideoPrompt, getString(R.string.prompt_starting_video_record));
            }

            @Override
            public void onStopVideoRecordingDone(AirController.Result result) {
                Log.d(TAG, "stop video recording done :"+result);
                showResult(result, mStopVideoPrompt, getString(R.string.prompt_stopping_video_record));
            }

            @Override
            public void onSetVideoStreamIdDone(AirController.Result result) {
                Log.d(TAG, "set video stream done :"+result);
                showResult(result, mSetVideoStreamPrompt, getString(R.string.prompt_setting_video_stream_id));
            }

            @Override
            public void onVideoStreamIdChanged(int id) {
                String info = (id == -1) ? "" : String.valueOf(id);
                 mVideoStremIdInfo.setText(getString(R.string.info_video_stream_id)+info);
            }

            @Override
            public void onSetUartBaudrateDone(AirController.Result result) {
                showResult(result, mSetUartPrompt, getString(R.string.prompt_setting_uart));
                int count = mAirController.getUartCount();
                if (count > 0) {
                    mUart1Info.setText(getString(R.string.info_uart1_baudrate)
                            +mAirController.getUartBaudrate(1));
                }
                if (count > 1) {
                    mUart2Info.setVisibility(View.VISIBLE);
                    mUart2Info.setText(getString(R.string.info_uart2_baudrate)
                            +mAirController.getUartBaudrate(2));
                }
            }

            @Override
            public void onVideoRecordingStatusChanged(boolean inVideoRecording) {
                String info = inVideoRecording ? getString(R.string.info_on)
                                : getString(R.string.info_off);
                mVideoRecordingInfo.setText(getString(R.string.info_video_recording)+info);
            }

            @Override
            public void onCaptureModeChanged(AirController.CaptureMode captureMode) {
                mCaptureMode = captureMode;
                String info;
                if (captureMode == AirController.CaptureMode.PHOTO) {
                    info = getString(R.string.info_cap_mode_photo);
                } else if (captureMode == AirController.CaptureMode.VIDEO) {
                    info = getString(R.string.info_cap_mode_video);
                } else {
                    info = "";
                }
                mCaptureModeInfo.setText(getString(R.string.info_capture_mode)+info);
            }

            @Override
            public void onFreeSpaceChanged(int i) {
                String info = (i == -1) ? "" : String.format(getString(R.string.info_free_mb), i);
                mFreeSpaceInfo.setText(getString(R.string.info_free_space)+info);
            }

            @Override
            public void onUartReady(boolean ready) {
                int count = mAirController.getUartCount();
                if (count > 0) {
                    mUart1Info.setText(getString(R.string.info_uart1_baudrate)
                            +mAirController.getUartBaudrate(1));
                }
                if (count > 1) {
                    mUart2Info.setVisibility(View.VISIBLE);
                    mUart2Info.setText(getString(R.string.info_uart2_baudrate)
                            +mAirController.getUartBaudrate(2));
                }

            }

            private void showResult(AirController.Result r, TextView v, String baseString) {
                switch (r) {
                    case SUCCESS:
                        v.setText(baseString+getString(R.string.prompt_result_success));
                        break;
                    case FAIL:
                        v.setText(baseString+getString(R.string.prompt_result_fail));
                        break;
                    case TIMEOUT:
                        v.setText(baseString+getString(R.string.prompt_result_timeout));
                        break;
                }
            }

        });

        mD2dConnectionInfo = (TextView)findViewById(R.id.d2d_connection);
        mD2dSignalAirInfo = (TextView)findViewById(R.id.d2d_signal_air);
        mD2dSignalControllerInfo = (TextView)findViewById(R.id.d2d_signal_controller);

        mD2dController = new D2dController(this, new D2dControlListener() {
            @Override
            public void onLinkConnectionChanged(boolean connected) {
                String info = connected ? getString(R.string.info_connected)
                        : getString(R.string.info_disconnected);
                mD2dConnectionInfo.setText(getString(R.string.info_d2d_connection)+info);
            }

            @Override
            public void onAirSignalChanged(int rsrp) {
                String info = String.format(getString(R.string.info_dbm), rsrp);
                mD2dSignalAirInfo.setText(getString(R.string.info_d2d_signal_air)+info);
            }

            @Override
            public void onControllerSignalChanged(int rsrp) {
                String info = String.format(getString(R.string.info_dbm), rsrp);
                mD2dSignalControllerInfo.setText(getString(R.string.info_d2d_signal_controller)+info);
            }

            @Override
            public void onPairAirDone(boolean success) {
                if (success) {
                    mPairPrompt.setText(getString(R.string.prompt_result_d2d_pair_succeed));
                } else {
                    mPairPrompt.setText(getString(R.string.prompt_result_d2d_pair_fail));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mVideoStremIdInfo.requestFocus();
        mAirController.start();
        mD2dController.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAirController.stop();
        mD2dController.stop();
    }

    private void showRetValue(AirController.RetValue ret, TextView v, String string) {
        switch (ret) {
            case OK:
                v.setText(string+getString(R.string.prompt_return_ok));
                break;
            case NOT_READY:
                v.setText(string+getString(R.string.prompt_return_not_ready));
                break;
            case WAIT_PENDING:
                v.setText(string+getString(R.string.prompt_return_wait_pending));
                break;
            case CAP_NOT_READY:
                v.setText(string+getString(R.string.prompt_return_capture_not_ready));
                break;
            case NO_FREE_MEM:
                v.setText(string+getString(R.string.prompt_return_capture_no_free_mem));
                break;
            case REMOTE_ERROR:
            case UNKNOWN:
                v.setText(string+getString(R.string.prompt_result_fail));
                break;
        }
    }
}
