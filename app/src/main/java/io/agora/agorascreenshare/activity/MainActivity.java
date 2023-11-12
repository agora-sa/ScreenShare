package io.agora.agorascreenshare.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import io.agora.agorascreenshare.BuildConfig;
import io.agora.agorascreenshare.R;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton mSingleButton;
    private AppCompatButton mMoreButton;

    private String appId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSingleButton = this.findViewById(R.id.screen_share_single_uid);
        mMoreButton = this.findViewById(R.id.screen_share_more_uid);

        mSingleButton.setText("单uid发多路音频流");
        mMoreButton.setText("多uid发多路音视频流");

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