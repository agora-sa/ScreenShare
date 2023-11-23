package io.agora.agorascreenshare.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.mmkv.MMKV;

import io.agora.agorascreenshare.BuildConfig;
import io.agora.agorascreenshare.R;
import io.agora.agorascreenshare.utils.CustomWindows;
import io.agora.rtc2.RtcEngine;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // 连续点击8次，弹出弹框，设置私有参数
    private static final int CLICK_COUNT = 8;

    private AppCompatButton mSingleButton;
    private AppCompatButton mMoreButton;
    private AppCompatButton mSetting;
    private AppCompatTextView mVersion;

    // 连续点击版本号的次数
    private int clickCount = 0;

    private String appId;
    private MMKV mmkv;
    private CustomWindows mCustomWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MMKV.initialize(this);
        mmkv = MMKV.defaultMMKV();

        mSingleButton = this.findViewById(R.id.screen_share_single_uid);
        mMoreButton = this.findViewById(R.id.screen_share_more_uid);
        mSetting = this.findViewById(R.id.setting);

        mVersion = findViewById(R.id.version);
        mVersion.setText("sdk version : " + RtcEngine.getSdkVersion());
        mVersion.setOnClickListener(v -> {
            clickCount++;
            if (clickCount == CLICK_COUNT) {
                Log.d(TAG, "click pop window!");
                // 隐藏逻辑
                clickCount = 0;
            }
        });

        mSingleButton.setText("单uid发多路音频流");
        mMoreButton.setText("多uid发多路音视频流");

        mSetting.setOnClickListener(v -> {
            // 跳转到通用设置页面
            mCustomWindow = new CustomWindows();
            mCustomWindow.popWindows(mmkv, MainActivity.this);
        });

        appId = BuildConfig.AGORA_APP_ID;
        mSingleButton.setOnClickListener(v -> {
            if (!checkNecessaryInfo()) {
                startActivity(new Intent(MainActivity.this, ScreenShareForSingleUid.class));
            }
        });

        mMoreButton.setOnClickListener(v -> {
            if (!checkNecessaryInfo()) {
                startActivity(new Intent(MainActivity.this, ScreenShareForMoreUid.class));
            }
        });
    }

    /**
     * 校验appId等必要信息
     *
     * @return 是否存在必要信息
     */
    private boolean checkNecessaryInfo() {
        if ("".equals(appId)) {
            Toast.makeText(MainActivity.this, "请到gradle.properties中填写APPID等信息", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }


}