package io.agora.agorascreenshare.utils;

import android.content.Context;

import io.agora.agorascreenshare.BuildConfig;
import io.agora.agorascreenshare.ScreenShareApp;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IAgoraEventHandler;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;

public class RtcEngineManager {

    private Context mContext;
    private static IAgoraEventHandler mAgoraEventHandler;
    private RtcEngine engine;

    public RtcEngineManager(Context context) {
        mContext = context;
    }

    private RtcEngine createRtcEngine() {
        RtcEngine engine = null;
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = mContext.getApplicationContext();
            config.mAppId = BuildConfig.AGORA_APP_ID;
            config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
            config.mEventHandler = mAgoraEventHandler;
            config.mAudioScenario = Constants.AudioScenario.getValue(Constants.AudioScenario.DEFAULT);
            config.mAreaCode = ((ScreenShareApp)mContext).getGlobalSettings().getAreaCode();
            engine = (RtcEngineEx) RtcEngine.create(config);
            engine.setLocalAccessPoint(((ScreenShareApp)mContext).getGlobalSettings().getPrivateCloudConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return engine;
    }

    public void addInitInfo(IRtcEngineEventHandler eventHandler) {
        mAgoraEventHandler = eventHandler;
        engine.addHandler(eventHandler);
    }
}
