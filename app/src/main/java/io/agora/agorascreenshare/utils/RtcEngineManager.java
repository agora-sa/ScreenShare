package io.agora.agorascreenshare.utils;

import android.content.Context;

import io.agora.agorascreenshare.BuildConfig;
import io.agora.agorascreenshare.ScreenShareApp;
import io.agora.agorascreenshare.common.CommonEventHandler;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;

public class RtcEngineManager {
    private static RtcEngineManager instance;
    private RtcEngineEx engine;
    private CommonEventHandler mEventHandler;

    private RtcEngineManager() {
    }

    public static synchronized RtcEngineManager getInstance() {
        if (instance == null) {
            instance = new RtcEngineManager();
        }
        return instance;
    }

    public RtcEngine getRtcEngine() {
        return engine;
    }

    public void setEventHandler(CommonEventHandler eventHandler) {
        this.mEventHandler = eventHandler;
    }

    public void initializeRtcEngine(Context context) {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = context.getApplicationContext();
            config.mAppId = BuildConfig.AGORA_APP_ID;
            config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

//            RtcEngineConfig.LogConfig logConfig = new RtcEngineConfig.LogConfig();
//            logConfig.fileSizeInKB = logSize;
//            config.mLogConfig = logConfig;
//            Log.d(TAG, "logSize is : " + logSize);

            config.mEventHandler = iRtcEngineEventHandler;
            config.mAudioScenario = Constants.AudioScenario.getValue(Constants.AudioScenario.DEFAULT);
            config.mAreaCode = ((ScreenShareApp) context.getApplicationContext()).getGlobalSettings().getAreaCode();
            engine = (RtcEngineEx) RtcEngine.create(config);
            engine.setLocalAccessPoint(((ScreenShareApp) context.getApplicationContext()).getGlobalSettings().getPrivateCloudConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final IRtcEngineEventHandler iRtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onError(int err) {
            super.onError(err);
            if (null != mEventHandler) {
                mEventHandler.onError(err);
            }
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            if (null != mEventHandler) {
                mEventHandler.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

        @Override
        public void onLocalVideoStateChanged(Constants.VideoSourceType source, int state, int error) {
            super.onLocalVideoStateChanged(source, state, error);
            if (null != mEventHandler) {
                mEventHandler.onLocalVideoStateChanged(source, state, error);
            }
        }

        @Override
        public void onLocalAudioStats(LocalAudioStats stats) {
            super.onLocalAudioStats(stats);
            if (null != mEventHandler) {
                mEventHandler.onLocalAudioStats(stats);
            }
        }

        @Override
        public void onRemoteAudioStats(RemoteAudioStats stats) {
            super.onRemoteAudioStats(stats);
            if (null != mEventHandler) {
                mEventHandler.onRemoteAudioStats(stats);
            }
        }

        @Override
        public void onLocalVideoStats(Constants.VideoSourceType source, LocalVideoStats stats) {
            super.onLocalVideoStats(source, stats);
            if (null != mEventHandler) {
                mEventHandler.onLocalVideoStats(source, stats);
            }
        }

        @Override
        public void onRemoteVideoStats(RemoteVideoStats stats) {
            super.onRemoteVideoStats(stats);
            if (null != mEventHandler) {
                mEventHandler.onRemoteVideoStats(stats);
            }
        }

        @Override
        public void onVideoSizeChanged(Constants.VideoSourceType source, int uid, int width, int height, int rotation) {
            super.onVideoSizeChanged(source, uid, width, height, rotation);
            if (null != mEventHandler) {
                mEventHandler.onVideoSizeChanged(source, uid, width, height, rotation);
            }
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            if (null != mEventHandler) {
                mEventHandler.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            }
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            if (null != mEventHandler) {
                mEventHandler.onUserJoined(uid, elapsed);
            }
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            if (null != mEventHandler) {
                mEventHandler.onUserOffline(uid, reason);
            }
        }

        @Override
        public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
            if (null != mEventHandler) {
                mEventHandler.onFirstRemoteVideoFrame(uid, width, height, elapsed);
            }
        }

        @Override
        public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
            if (null != mEventHandler) {
                mEventHandler.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            }
        }
    };
}
