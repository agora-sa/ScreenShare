package io.agora.agorascreenshare;

import android.app.Application;

import com.tencent.mmkv.MMKV;

import io.agora.agorascreenshare.utils.GlobalSettings;
import io.agora.agorascreenshare.utils.RtcEngineManager;
import io.agora.rtc2.RtcEngine;

public class ScreenShareApp extends Application {

    private GlobalSettings globalSettings;

    @Override
    public void onCreate() {
        super.onCreate();

        MMKV.initialize(this); // 初始化 MMKV

        // 初始化RTCEngine，全局只一份
        RtcEngineManager rtcManager = RtcEngineManager.getInstance();
        rtcManager.initializeRtcEngine(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RtcEngine.destroy();
    }

    public GlobalSettings getGlobalSettings() {
        if(globalSettings == null){
            globalSettings = new GlobalSettings();
        }
        return globalSettings;
    }
}
