package io.agora.agorascreenshare;

import android.app.Application;

import com.tencent.mmkv.MMKV;

import io.agora.agorascreenshare.utils.GlobalSettings;

public class ScreenShareApp extends Application {

    private GlobalSettings globalSettings;

    @Override
    public void onCreate() {
        super.onCreate();

        MMKV.initialize(this); // 初始化 MMKV
    }

    public GlobalSettings getGlobalSettings() {
        if(globalSettings == null){
            globalSettings = new GlobalSettings();
        }
        return globalSettings;
    }
}
