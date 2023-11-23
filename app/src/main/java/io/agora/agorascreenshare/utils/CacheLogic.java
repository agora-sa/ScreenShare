package io.agora.agorascreenshare.utils;

import android.content.Context;
import android.util.Log;

import com.tencent.mmkv.MMKV;

import java.util.Objects;

public class CacheLogic {
    private MMKV mmkv;
    // 打开状态信息开关
    private boolean addState;
    // 打开dump video开关
    private boolean dumpVideo;
    // 分辨率
    private String dimensions;
    // 帧率
    private int fps;
    // 音量调节的增益倍数，默认不增益
    private int volumeMultiple = 1;
    // sdk log的大小，单位KB
    private int logSize = 1024;
    // 默认是720*1080
    private int[] dimens = {720, 1080};

    public void loadCache(Context mContext) {
        MMKV.initialize(mContext);
        mmkv = MMKV.defaultMMKV();

        addState = mmkv.decodeBool(CustomWindows.PARAMS_KEY.KEY_ADD_STATE, false);
        dumpVideo = mmkv.decodeBool(CustomWindows.PARAMS_KEY.KEY_DUMP_VIDEO, false);
        fps = Integer.parseInt(Objects.requireNonNull(mmkv.decodeString(CustomWindows.PARAMS_KEY.KEY_FPS, "15")));
        volumeMultiple = Integer.parseInt(Objects.requireNonNull(mmkv.decodeString(CustomWindows.PARAMS_KEY.KEY_VOLUME_MUL, "1")));
        dimensions = mmkv.decodeString(CustomWindows.PARAMS_KEY.KEY_DIMENSIONS, "VD_1280x720");
        logSize = Integer.parseInt(Objects.requireNonNull(mmkv.decodeString(CustomWindows.PARAMS_KEY.KEY_LOG_SIZE, "1024")));
        dimens = ViewUtils.computeWidthAndHeight(dimensions);

        Log.d("SSSSS DEMO",
                "SSSSS JD-DEMO JoinChannelVideo:::isAccessPoint=" + "dimensions=" + dimensions
                        + " , fps=" + fps + " , addState=" + addState + " , dumpVideo=" + dumpVideo);
    }

    public boolean isAddState() {
        return addState;
    }

    public boolean isDumpVideo() {
        return dumpVideo;
    }

    public String getDimensions() {
        return dimensions;
    }

    public int getFps() {
        return fps;
    }

    public int getVolumeMultiple() {
        return volumeMultiple;
    }

    public int getLogSize() {
        return logSize;
    }

    public int[] getDimens() {
        return dimens;
    }
}
