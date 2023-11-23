package io.agora.agorascreenshare.activity;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected Handler handler;
    protected Context mContext;

    // 是否同意了权限
    protected boolean isRequested = false;
    private ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;
    private final String[] permissionsToRequest = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());

        intiPermission();
    }

    protected void requestPermissions() {
        // 检查未被授予的权限
        if (!isRequested) {
            // 请求未被授予的权限
            requestMultiplePermissionsLauncher.launch(permissionsToRequest);
        } else {
            next();
        }
    }

    private void intiPermission() {
        // 初始化 ActivityResultLauncher
        requestMultiplePermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (Boolean.TRUE.equals(result.get("android.permission.CAMERA"))
                            && Boolean.TRUE.equals(result.get("android.permission.RECORD_AUDIO"))
                            && Boolean.TRUE.equals(result.get("android.permission.WRITE_EXTERNAL_STORAGE"))
                            && Boolean.TRUE.equals(result.get("android.permission.READ_EXTERNAL_STORAGE"))) {
                        // 获取到权限
                        next();
                        isRequested = true;
                    } else {
                        // 未获取到权限
                        isRequested = false;
                        Log.d("MainActivity", "使用demo需要 摄像头、录音、存储权限！");
                    }
                });
    }

    protected abstract void next();

    protected final void showLongToast(final String msg) {
        runOnUIThread(() -> {
            Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_LONG).show();
        });
    }

    protected final void runOnUIThread(Runnable runnable) {
        this.runOnUIThread(runnable, 0);
    }

    protected final void runOnUIThread(Runnable runnable, long delay) {
        if (handler != null && runnable != null) {
            if (delay <= 0 && handler.getLooper().getThread() == Thread.currentThread()) {
                runnable.run();
            } else {
                handler.postDelayed(() -> {
                    runnable.run();
                }, delay);
            }
        }
    }
}
