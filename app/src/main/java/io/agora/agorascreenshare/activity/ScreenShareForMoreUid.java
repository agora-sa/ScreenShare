package io.agora.agorascreenshare.activity;

import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_640x360;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import io.agora.agorascreenshare.BuildConfig;
import io.agora.agorascreenshare.R;
import io.agora.agorascreenshare.ScreenShareApp;
import io.agora.agorascreenshare.utils.CacheLogic;
import io.agora.agorascreenshare.utils.CommonUtil;
import io.agora.agorascreenshare.utils.TokenUtils;
import io.agora.agorascreenshare.utils.VideoReportLayout;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcConnection;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.ScreenCaptureParameters;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class ScreenShareForMoreUid extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "ScreenShareForMoreUid";
    private static final int DEFAULT_SHARE_FRAME_RATE = 15;
    private VideoReportLayout fl_camera, fl_screen;
    private VideoReportLayout fl_remote_1, fl_remote_2;
    private Button join;
    private SwitchCompat camera, screenShare, screenSharePreview, closeMicAudio, closeScreenAudio;
    private EditText et_channel;
    private boolean joined = false;
    private final ChannelMediaOptions screenShareOptions = new ChannelMediaOptions();
    private Intent fgServiceIntent;
    private RtcEngineEx engine;
    private final RtcConnection rtcConnection2 = new RtcConnection();
    private final ChannelMediaOptions cameraMediaOptions = new ChannelMediaOptions();
    private final ScreenCaptureParameters parameters = new ScreenCaptureParameters();

    private final Map<Integer, ViewGroup> remoteViews = new ConcurrentHashMap<Integer, ViewGroup>();
    private CacheLogic mCacheLogic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_share_more_uid);

        mContext = this;
        initMMKVValue();
        initView();
        initEngine();
    }

    private void initView() {
        join = findViewById(R.id.btn_join);
        camera = findViewById(R.id.camera);
        screenShare = findViewById(R.id.screenShare);
        screenSharePreview = findViewById(R.id.screenSharePreview);
        closeMicAudio = findViewById(R.id.closeMicAudio);
        closeScreenAudio = findViewById(R.id.closeScreenAudio);
        et_channel = findViewById(R.id.et_channel);
        fl_camera = findViewById(R.id.fl_camera);
        fl_screen = findViewById(R.id.fl_screen_share);
        fl_remote_1 = findViewById(R.id.fl_remote1);
        fl_remote_2 = findViewById(R.id.fl_remote2);
        join.setOnClickListener(this);
        camera.setOnCheckedChangeListener(this);
        screenShare.setOnCheckedChangeListener(this);
        screenSharePreview.setOnCheckedChangeListener(this);
        closeMicAudio.setOnCheckedChangeListener(this);
        closeScreenAudio.setOnCheckedChangeListener(this);
    }

    private void initEngine() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            fgServiceIntent = new Intent(mContext, MediaProjectFgService.class);
        }
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = mContext.getApplicationContext();
            config.mAppId = BuildConfig.AGORA_APP_ID;
            config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
            config.mEventHandler = iRtcEngineEventHandler;
            config.mAudioScenario = Constants.AudioScenario.getValue(Constants.AudioScenario.GAME_STREAMING);
            config.mAreaCode = RtcEngineConfig.AreaCode.AREA_CODE_CN;
            engine = (RtcEngineEx) RtcEngine.create(config);
            initParameters();
            engine.setLocalAccessPoint(((ScreenShareApp) getApplication()).getGlobalSettings().getPrivateCloudConfig());
        } catch (Exception e) {
            e.printStackTrace();
            onBackPressed();
        }
    }

    private void initParameters() {
        engine.setParameters("{\"che.video.nasa_encode\":false}");
        engine.setParameters("{\"che.audio.codec.opus_celt\":true}");
        engine.setParameters("{\"che.video.end2end_bwe\":false}");
        engine.setParameters("{\"che.video.camera.face_detection\":false}");

        // 改变音频编码方式
        engine.setParameters("{\"che.audio.custom_payload_type\":9}");
        // 关闭3A
        engine.setParameters("{\"che.audio.aec.enable\":false}");
        engine.setParameters("{\"che.audio.agc.enable\":false}");
        engine.setParameters("{\"che.audio.ans.enable\":false}");
        // 设置音频码率、采样率
        engine.setParameters("{\"che.audio.input_sample_rate\":48000}");
        engine.setParameters("{\"che.audio.custom_bitrate\":48000}");
        // 开启硬编硬解
        engine.setParameters("{\"engine.video.enable_hw_decoder\":true}");
        engine.setParameters("{\"engine.video.enable_hw_encoder\":true}");
    }

    /**
     * 初始化之前在mmkv里面设置的值
     */
    private void initMMKVValue() {
        mCacheLogic = new CacheLogic();
        mCacheLogic.loadCache(mContext);
    }

    @Override
    public void onDestroy() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stopService(fgServiceIntent);
        }
        if (engine != null) {
            if(camera.isChecked()){
                engine.leaveChannelEx(rtcConnection2);
            }
            engine.leaveChannel();
            engine.stopPreview();
        }
        engine = null;
        super.onDestroy();
        handler.post(RtcEngine::destroy);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.screenShare) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                if(b){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        mContext.startForegroundService(fgServiceIntent);
                    }
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                    parameters.videoCaptureParameters.width = mCacheLogic.getDimens()[0];
                    parameters.videoCaptureParameters.height = (int) (mCacheLogic.getDimens()[0] * 1.0f / metrics.widthPixels * metrics.heightPixels);
                    parameters.videoCaptureParameters.framerate = mCacheLogic.getFps();
                    parameters.videoCaptureParameters.bitrate = 1000;
                    // start screen capture and update options
                    parameters.captureAudio = true;
                    engine.startScreenCapture(parameters);
                    screenShareOptions.publishScreenCaptureVideo = true;
                    screenShareOptions.publishCameraTrack = false;
                    screenShareOptions.publishScreenCaptureAudio = true;
                    engine.updateChannelMediaOptions(screenShareOptions);
                    addScreenSharePreview();
                }
                else{
                    // stop screen capture and update options
                    engine.stopScreenCapture();
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        stopService(fgServiceIntent);
                    }
                    screenShareOptions.publishScreenCaptureVideo = false;
                    engine.updateChannelMediaOptions(screenShareOptions);
                }
                screenSharePreview.setEnabled(b);
                screenSharePreview.setChecked(b);
            } else {
                // showAlert(getString(R.string.lowversiontip));
            }
        } else if (compoundButton.getId() == R.id.camera) {
            if(b){
                cameraMediaOptions.autoSubscribeAudio = false;
                cameraMediaOptions.autoSubscribeVideo = false;
                cameraMediaOptions.publishScreenCaptureVideo = false;
                cameraMediaOptions.publishCameraTrack = true;
                cameraMediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                cameraMediaOptions.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
                rtcConnection2.channelId = et_channel.getText().toString();
                rtcConnection2.localUid = new Random().nextInt(512)+512;

                engine.setVideoEncoderConfigurationEx(new VideoEncoderConfiguration(
                        VD_640x360,
                        FRAME_RATE_FPS_15,
                        400,
                        ORIENTATION_MODE_ADAPTIVE
                ), rtcConnection2);

                engine.joinChannelEx(null ,rtcConnection2, cameraMediaOptions, iRtcEngineEventHandler);
            }
            else{
                engine.leaveChannelEx(rtcConnection2);
                engine.startPreview(Constants.VideoSourceType.VIDEO_SOURCE_CAMERA_PRIMARY);
            }
        } else if (compoundButton.getId() == R.id.screenSharePreview) {
            if(b){
                addScreenSharePreview();
            }else{
                fl_screen.removeAllViews();
                engine.stopPreview(Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY);
            }
        }
        else if (compoundButton.getId() == R.id.closeMicAudio) {
            cameraMediaOptions.publishMicrophoneTrack = b;
            engine.updateChannelMediaOptionsEx(cameraMediaOptions, rtcConnection2);
        }
        else if (compoundButton.getId() == R.id.closeScreenAudio) {
            parameters.captureAudio = b;
            engine.updateScreenCaptureParameters(parameters);
            screenShareOptions.publishScreenCaptureAudio = b;
            engine.updateChannelMediaOptions(screenShareOptions);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_join) {
            if (!joined) {
                CommonUtil.hideInputBoard(ScreenShareForMoreUid.this, et_channel);
                requestPermissions();
            } else {
                joined = false;
                join.setText("join");
                camera.setEnabled(false);
                screenShare.setEnabled(false);
                screenSharePreview.setEnabled(false);
                fl_camera.removeAllViews();
                fl_screen.removeAllViews();

                if (camera.isChecked()) {
                    engine.leaveChannelEx(rtcConnection2);
                    camera.setChecked(false);
                }
                if(screenSharePreview.isChecked()){
                    engine.stopPreview(Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY);
                    screenSharePreview.setChecked(false);
                }
                if(screenShare.isChecked()){
                    engine.stopScreenCapture();
                    screenShare.setChecked(false);
                }
                if(closeMicAudio.isChecked()){
                    closeMicAudio.setChecked(false);
                }
                if(closeScreenAudio.isChecked()){
                    closeScreenAudio.setChecked(false);
                }
                engine.stopPreview(Constants.VideoSourceType.VIDEO_SOURCE_CAMERA_PRIMARY);
                engine.leaveChannel();

                for (ViewGroup value : remoteViews.values()) {
                    value.removeAllViews();
                }
                remoteViews.clear();
            }
        }
    }

    private void addScreenSharePreview() {
        // Create render view by RtcEngine
        SurfaceView surfaceView = new SurfaceView(mContext);
        if (fl_screen.getChildCount() > 0) {
            fl_screen.removeAllViews();
        }
        // Add to the local container
        fl_screen.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Setup local video to render your local camera preview
        VideoCanvas local = new VideoCanvas(surfaceView, Constants.RENDER_MODE_FIT, 0);
        local.mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED;
        local.sourceType = Constants.VIDEO_SOURCE_SCREEN_PRIMARY;
        engine.setupLocalVideo(local);
        engine.startPreview(Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY);
    }

    private void addCameraPreview() {
        // Create render view by RtcEngine
        SurfaceView surfaceView = new SurfaceView(mContext);
        if (fl_camera.getChildCount() > 0) {
            fl_camera.removeAllViews();
        }
        // Add to the local container
        fl_camera.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Setup local video to render your local camera preview
        VideoCanvas local = new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        local.sourceType = Constants.VIDEO_SOURCE_CAMERA_PRIMARY;
        engine.setupLocalVideo(local);
        engine.startPreview(Constants.VideoSourceType.VIDEO_SOURCE_CAMERA_PRIMARY);
    }

    private void joinChannel() {
        // engine.setParameters("{\"che.video.mobile_1080p\":true}");
        // set options
        screenShareOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        screenShareOptions.autoSubscribeVideo = true;
        screenShareOptions.autoSubscribeAudio = false;
        screenShareOptions.publishCameraTrack = false;
        screenShareOptions.publishScreenCaptureVideo = false;
        screenShareOptions.publishMicrophoneTrack = false;
        screenShareOptions.enableAudioRecordingOrPlayout = false;
        screenShareOptions.publishEncodedVideoTrack = false;

        engine.enableVideo();
        // Setup video encoding configs

        engine.setDefaultAudioRoutetoSpeakerphone(true);
        // engine.setScreenCaptureScenario(Constants.ScreenScenarioType.SCREEN_SCENARIO_VIDEO);

        String channelName = et_channel.getText().toString();
        TokenUtils.gen(mContext, channelName, 0, accessToken -> {
            int res = engine.joinChannel(accessToken, channelName, 0, screenShareOptions);
            if (res != 0) {
                return;
            }
            join.setEnabled(false);
            addCameraPreview();
        });
    }

    private final IRtcEngineEventHandler iRtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onError(int err) {
            Log.e(TAG, String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err)));
            // showAlert(String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err)));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid));
            showLongToast(String.format("onJoinChannelSuccess channel %s uid %d", channel, uid));
            joined = true;
            handler.post(() -> {
                join.setEnabled(true);
                join.setText("leave");
                camera.setEnabled(true);
                screenShare.setEnabled(true);
                closeMicAudio.setEnabled(true);
                closeScreenAudio.setEnabled(true);
            });
        }

        @Override
        public void onLocalVideoStateChanged(Constants.VideoSourceType source, int state, int error) {
            super.onLocalVideoStateChanged(source, state, error);
            if (state == 1) {
                Log.i(TAG, "local view published successfully!");
            }
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            Log.i(TAG, "onRemoteVideoStateChanged:uid->" + uid + ", state->" + state);
        }

        @Override
        public void onRemoteVideoStats(RemoteVideoStats stats) {
            super.onRemoteVideoStats(stats);
            Log.d(TAG, "onRemoteVideoStats: width:" + stats.width + " x height:" + stats.height);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Log.i(TAG, "onUserJoined->" + uid);
            showLongToast(String.format("user %d joined!", uid));
            if(remoteViews.containsKey(uid)){
                return;
            }
            else{
                handler.post(() ->
                {
                    /**Display remote video stream*/
                    SurfaceView surfaceView = null;
                    // Create render view by RtcEngine
                    surfaceView = new SurfaceView(mContext);
                    surfaceView.setZOrderMediaOverlay(true);
                    VideoReportLayout view = getAvailableView();
                    view.setReportUid(uid);
                    remoteViews.put(uid, view);
                    // Add to the remote container
                    view.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    // Setup remote video to render
                    engine.setupRemoteVideo(new VideoCanvas(surfaceView, Constants.RENDER_MODE_HIDDEN, uid));
                });
            }
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason));
            showLongToast(String.format("user %d offline! reason:%d", uid, reason));
            handler.post(() -> {
                engine.setupRemoteVideo(new VideoCanvas(null, Constants.RENDER_MODE_HIDDEN, uid));
                remoteViews.get(uid).removeAllViews();
                remoteViews.remove(uid);
            });
        }
    };

    public static class MediaProjectFgService extends Service {
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                createNotificationChannel();
            }
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return START_NOT_STICKY;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            stopForeground(true);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        private void createNotificationChannel() {
            CharSequence name = getString(R.string.app_name);
            String description = "Notice that we are trying to capture the screen!!";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String channelId = "agora_channel_mediaproject";
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            int notifyId = 1;
            // Create a notification and set the notification channel.
            Notification notification = new NotificationCompat.Builder(this, channelId)
                    .setContentText(name + "正在录制屏幕内容...")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setChannelId(channelId)
                    .setWhen(System.currentTimeMillis())
                    .build();
            startForeground(notifyId, notification);
        }
    }

    @Override
    protected void next() {
        joinChannel();
    }

    private VideoReportLayout getAvailableView() {
        if(fl_remote_1.getChildCount() == 0){
            return fl_remote_1;
        }
        else if(fl_remote_2.getChildCount() == 0){
            return fl_remote_2;
        }
        else{
            return fl_remote_1;
        }
    }
}
