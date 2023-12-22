package io.agora.agorascreenshare.activity;

import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE;
import static io.agora.rtc2.video.VideoEncoderConfiguration.STANDARD_BITRATE;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_640x360;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import io.agora.agorascreenshare.BuildConfig;
import io.agora.agorascreenshare.common.CommonEventHandler;
import io.agora.agorascreenshare.utils.CacheLogic;
import io.agora.agorascreenshare.utils.MediaProjectFgService;
import io.agora.agorascreenshare.R;
import io.agora.agorascreenshare.ScreenShareApp;
import io.agora.agorascreenshare.utils.RtcEngineManager;
import io.agora.agorascreenshare.utils.TokenUtils;
import io.agora.agorascreenshare.utils.VideoReportLayout;
import io.agora.agorascreenshare.utils.ViewUtils;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.ScreenCaptureParameters;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class ScreenShareForSingleUid extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener, CommonEventHandler {

    private static final String TAG = "ScreenShareForSingleUid";

    private VideoReportLayout fl_camera;
    private VideoReportLayout fl_screen;
    private Button join;
    private EditText et_channel;
    private SwitchCompat screenPreview;
    private SwitchCompat screenAudio;
    private SwitchCompat micAudio;
    private SwitchCompat publicScreen;
    private SeekBar screenAudioVolume;
    private SeekBar captureAudioVolume;
    private Spinner screenScenarioType;

    private Intent fgServiceIntent;
    private boolean joined;
    private int remoteUid;

    private boolean isFullOfCamera;
    private boolean isFullOfScreen;

    private RtcEngineEx engine;
    private CacheLogic mCacheLogic;

    private final ScreenCaptureParameters screenCaptureParameters = new ScreenCaptureParameters();
    private final ChannelMediaOptions options = new ChannelMediaOptions();
    private Runnable dumpRunnable;

    // 是否已经开始了视频dump的功能，默认没有开始
    private boolean isDumped;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_share_single_uid);

        initMMKVValue();
        initView();
        initEngine();
    }

    @Override
    protected void next() {
        if (!isDumped) {
            startDump();
            isDumped = true;
        }

        engine.setParameters("{\"che.video.mobile_1080p\":true}");
        engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);

        engine.enableVideo();
        engine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VD_640x360,
                FRAME_RATE_FPS_15,
                STANDARD_BITRATE,
                ORIENTATION_MODE_ADAPTIVE
        ));
        engine.setDefaultAudioRoutetoSpeakerphone(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForegroundService(fgServiceIntent);
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        screenCaptureParameters.captureVideo = true;
        screenCaptureParameters.videoCaptureParameters.width = mCacheLogic.getDimens()[0];
        screenCaptureParameters.videoCaptureParameters.height = (int) (mCacheLogic.getDimens()[0] * 1.0f / metrics.widthPixels * metrics.heightPixels);
        screenCaptureParameters.videoCaptureParameters.framerate = mCacheLogic.getFps();
        screenCaptureParameters.captureAudio = true;
        screenCaptureParameters.audioCaptureParameters.captureSignalVolume = screenAudioVolume.getProgress();
        engine.startScreenCapture(screenCaptureParameters);

        if (screenPreview.isChecked()) {
            startScreenSharePreview();
        }

        String channelName = et_channel.getText().toString();
        TokenUtils.gen(ScreenShareForSingleUid.this, channelName, 0, accessToken -> {
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            options.autoSubscribeVideo = true;
            options.autoSubscribeAudio = true;
            options.publishCameraTrack = false;
            options.publishMicrophoneTrack = true;
            options.publishScreenCaptureVideo = true;
            options.publishScreenCaptureAudio = true;
            int res = engine.joinChannel(accessToken, channelName, 0, options);
            if (res != 0) {
                return;
            }
            join.setEnabled(false);
            micAudio.setEnabled(true);
            screenAudio.setEnabled(true);
            screenPreview.setEnabled(true);
            publicScreen.setEnabled(true);
        });
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stopService(fgServiceIntent);
        }
        if (engine != null) {
            engine.leaveChannel();
            engine.stopScreenCapture();
            engine.stopPreview();
        }
        engine = null;
        super.onDestroy();
        // handler.post(RtcEngine::destroy);

        // 在活动销毁时，移除Runnable对象，避免内存泄漏
        handler.removeCallbacks(dumpRunnable);
    }

    private void initView() {
        join = findViewById(R.id.btn_join);
        et_channel = findViewById(R.id.et_channel);
        fl_camera = findViewById(R.id.fl_camera);
        fl_screen = findViewById(R.id.fl_screen_share);
        join.setOnClickListener(this);

        screenPreview = findViewById(R.id.switch_preview_screen_stream);
        screenAudio = findViewById(R.id.switch_public_screen_audio);
        micAudio = findViewById(R.id.switch_public_mic_audio);
        publicScreen = findViewById(R.id.switch_public_screen_stream);

        screenAudioVolume = findViewById(R.id.screen_audio_volume);
        captureAudioVolume = findViewById(R.id.mic_audio_volume);
        screenScenarioType = findViewById(R.id.spinner_screen_scenario_type);

        screenScenarioType.setOnItemSelectedListener(this);
        screenPreview.setOnCheckedChangeListener(this);
        screenAudio.setOnCheckedChangeListener(this);
        micAudio.setOnCheckedChangeListener(this);
        publicScreen.setOnCheckedChangeListener(this);
        screenAudioVolume.setOnSeekBarChangeListener(this);
        captureAudioVolume.setOnSeekBarChangeListener(this);

        fl_camera.setOnClickListener(v -> {
            if (!isFullOfCamera) {
                ViewUtils.setLargeWindow(fl_camera);
                fl_camera.setVisibility(View.VISIBLE);
                fl_screen.setVisibility(View.GONE);
                isFullOfCamera = true;
            } else {
                ViewUtils.setSmallWindow(fl_camera);
                visibleAll();
                isFullOfCamera = false;
            }
        });
        fl_screen.setOnClickListener(v -> {
            if (!isFullOfScreen) {
                ViewUtils.setLargeWindow(fl_screen);
                fl_camera.setVisibility(View.GONE);
                fl_screen.setVisibility(View.VISIBLE);
                isFullOfScreen = true;
            } else {
                ViewUtils.setSmallWindow(fl_screen);
                visibleAll();
                isFullOfScreen = false;
            }
        });
    }

    private void initEngine() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            fgServiceIntent = new Intent(ScreenShareForSingleUid.this, MediaProjectFgService.class);
        }
        engine = (RtcEngineEx) RtcEngineManager.getInstance().getRtcEngine();
        if (null == engine) {
            Log.d(TAG, "engine is null!");
            finish();
        }
        RtcEngineManager.getInstance().setEventHandler(this);
    }

    /**
     * 初始化之前在mmkv里面设置的值
     */
    private void initMMKVValue() {
        mCacheLogic = new CacheLogic();
        mCacheLogic.loadCache(this);
    }

    private void leaveChannel() {
        joined = false;
        join.setText("加入频道");
        fl_camera.removeAllViews();
        fl_screen.removeAllViews();
        remoteUid = -1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stopService(fgServiceIntent);
        }
        engine.leaveChannel();
        engine.stopScreenCapture();
        engine.stopPreview();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_join) {
            if (!joined) {
                ViewUtils.hideInputBoard(ScreenShareForSingleUid.this, et_channel);
                requestPermissions();
            } else {
                micAudio.setEnabled(false);
                screenAudio.setEnabled(false);
                screenPreview.setEnabled(false);
                publicScreen.setEnabled(false);
                leaveChannel();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView == screenScenarioType) {
            engine.setScreenCaptureScenario(Constants.ScreenScenarioType.valueOf(screenScenarioType.getSelectedItem().toString()));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (compoundButton == screenPreview) {
            if (!joined) {
                return;
            }
            if (checked) {
                startScreenSharePreview();
            } else {
                stopScreenSharePreview();
            }
            return;
        } else if (compoundButton == screenAudio) {
            screenCaptureParameters.captureAudio = checked;
            engine.updateScreenCaptureParameters(screenCaptureParameters);
            options.publishScreenCaptureAudio = checked;
        } else if (compoundButton == micAudio) {
            options.publishMicrophoneTrack = checked;
        } else if (compoundButton == publicScreen) {
            options.publishScreenCaptureVideo = checked;
        }
        engine.updateChannelMediaOptions(options);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        Log.d(TAG, "progress value is : " + progress + " , " + mCacheLogic.getVolumeMultiple());
        progress *= mCacheLogic.getVolumeMultiple();
        if (seekBar == screenAudioVolume) {
            if (!joined) {
                return;
            }
            screenCaptureParameters.audioCaptureParameters.captureSignalVolume = progress;
            engine.updateScreenCaptureParameters(screenCaptureParameters);
        } else if (seekBar == captureAudioVolume) {
            if (!joined) {
                return;
            }
            engine.adjustRecordingSignalVolume(progress * 4);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void startScreenSharePreview() {
        SurfaceView surfaceView = new SurfaceView(ScreenShareForSingleUid.this);
        if (fl_camera.getChildCount() > 0) {
            fl_camera.removeAllViews();
        }
        // Add to the local container
        fl_camera.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Setup local video to render your local camera preview
        VideoCanvas local = new VideoCanvas(surfaceView, Constants.RENDER_MODE_FIT, 0);
        local.mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED;
        local.sourceType = Constants.VIDEO_SOURCE_SCREEN_PRIMARY;
        engine.setupLocalVideo(local);

        engine.startPreview(Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY);
    }

    private void stopScreenSharePreview() {
        fl_camera.removeAllViews();
        engine.setupLocalVideo(new VideoCanvas(null));
        engine.stopPreview(Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY);
    }

    private void visibleAll() {
        fl_camera.setVisibility(View.VISIBLE);
        fl_screen.setVisibility(View.VISIBLE);
    }

    /**
     * 开始循环，一分钟dump一次
     * 查看dump的视频是否有I帧的命令
     * ffprobe -select_streams v:0 -show_frames -show_entries frame=pict_type xxx.h264
     */
    private void startDump() {
        Log.d(TAG, "SSSSS JD-DEMO dumpVideo : " + mCacheLogic.isDumpVideo());
        // 是否开启视频dump功能
        if (mCacheLogic.isDumpVideo()) {
            // 创建一个Runnable对象，定义每分钟执行的任务
            dumpRunnable = new Runnable() {
                @Override
                public void run() {
                    // 以下的调用，如果没有远端加入频道会返回-2
                    int openRet = engine.setParameters("{\"engine.video.enable_video_dump\":{\"mode\":8,\"enable\":true}}");
                    Log.d(TAG, "dump result is : " + openRet);
                    // 再次使用Handler的postDelayed方法将这个Runnable对象添加到消息队列中，等待下一分钟执行
                    handler.postDelayed(this, 58000); // 58S
                }
            };
            // 使用Handler的postDelayed方法将Runnable对象添加到消息队列中，等待1分钟后执行
            handler.postDelayed(dumpRunnable, 0); // 立即开始

            // 设置sdk 日志模式为debug模式并且size设置到很大，这个也是dump开关打开后才设置
            engine.setParameters("{\"rtc.log_filter\":65535}");
            engine.setParameters("{\"rtc.log_size \":1000000000}");
        }
    }

    @Override
    public void onError(int err) {
        Log.e(TAG, String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err)));
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid));
        joined = true;
        handler.post(() -> {
            join.setEnabled(true);
            join.setText("离开");
        });
    }

    @Override
    public void onLocalVideoStateChanged(Constants.VideoSourceType source, int state, int error) {
        Log.i(TAG, "onLocalVideoStateChanged source=" + source + ", state=" + state + ", error=" + error);
        if (source == Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY) {
            if (state == Constants.LOCAL_VIDEO_STREAM_STATE_ENCODING) {
                if (error == Constants.ERR_OK) {
                    showLongToast("Screen sharing start successfully.");
                }
            } else if (state == Constants.LOCAL_AUDIO_STREAM_STATE_FAILED) {
                if (error == Constants.ERR_SCREEN_CAPTURE_SYSTEM_NOT_SUPPORTED) {
                    showLongToast("Screen sharing has been cancelled");
                } else {
                    showLongToast("Screen sharing start failed for error " + error);
                }
                runOnUIThread(() -> leaveChannel());
            }
        }
    }

    @Override
    public void onLocalAudioStats(IRtcEngineEventHandler.LocalAudioStats stats) {
        if (mCacheLogic.isAddState())
            fl_camera.setLocalAudioStats(stats);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        if (mCacheLogic.isAddState()) {
            fl_screen.setRemoteAudioStats(stats);
        }

    }

    @Override
    public void onLocalVideoStats(Constants.VideoSourceType source, IRtcEngineEventHandler.LocalVideoStats stats) {
        if (mCacheLogic.isAddState())
            fl_camera.setLocalVideoStats(stats);
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        if (mCacheLogic.isAddState()) {
            fl_screen.setRemoteVideoStats(stats);
        }
    }

    @Override
    public void onVideoSizeChanged(Constants.VideoSourceType source, int uid, int width, int height, int rotation) {
        Log.i(TAG, "onVideoSizeChanged。。。");
    }

    @Override
    public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
        Log.i(TAG, "onRemoteVideoStateChanged:uid->" + uid + ", state->" + state);
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        Log.i(TAG, "onUserJoined->" + uid);
        showLongToast("user joined success : " + uid);
        if (remoteUid > 0) {
            return;
        }
        remoteUid = uid;
        runOnUIThread(() -> {
            SurfaceView renderView = new SurfaceView(ScreenShareForSingleUid.this);
            engine.setupRemoteVideo(new VideoCanvas(renderView, Constants.RENDER_MODE_FIT, uid));
            fl_screen.removeAllViews();
            fl_screen.addView(renderView);
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason));
        showLongToast("user offline : " + uid + " , and reason : " + reason);
        if (remoteUid == uid) {
            remoteUid = -1;
            runOnUIThread(() -> {
                fl_screen.removeAllViews();
                engine.setupRemoteVideo(new VideoCanvas(null, Constants.RENDER_MODE_FIT, uid));
            });
        }
    }
}
