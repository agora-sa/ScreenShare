package io.agora.agorascreenshare.common;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;

public interface CommonEventHandler {

    void onError(int err);

    void onJoinChannelSuccess(String channel, int uid, int elapsed);

    void onLocalVideoStateChanged(Constants.VideoSourceType source, int state, int error);

    void onLocalAudioStats(IRtcEngineEventHandler.LocalAudioStats stats);

    void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats);

    void onLocalVideoStats(Constants.VideoSourceType source, IRtcEngineEventHandler.LocalVideoStats stats);

    void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats);

    void onVideoSizeChanged(Constants.VideoSourceType source, int uid, int width, int height, int rotation);

    void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed);

    void onUserJoined(int uid, int elapsed);

    void onUserOffline(int uid, int reason);

    void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed);

    void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);
}
