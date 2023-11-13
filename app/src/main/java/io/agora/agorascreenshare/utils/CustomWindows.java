package io.agora.agorascreenshare.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;

import com.tencent.mmkv.MMKV;

import io.agora.agorascreenshare.R;

public class CustomWindows {
    private static final String TAG = "CustomWindows";

    public static class PARAMS_KEY {
        public static final String KEY_ADD_STATE = "addState";
        public static final String KEY_DUMP_VIDEO = "dumpVideo";
        public static final String KEY_DIMENSIONS = "dimensions";
        public static final String KEY_FPS = "fps";
        public static final String KEY_VOLUME_MUL = "volumeMul";
        public static final String KEY_LOG_SIZE = "logSize";
    }

    public void popWindows(MMKV mmkv, Context context) {
        if (null == mmkv) {
            Log.d(TAG, "MMKV obj can not null");
            return;
        }

        boolean addState = mmkv.decodeBool(PARAMS_KEY.KEY_ADD_STATE, false); // 当前网络状态
        boolean dumpVideo = mmkv.decodeBool(PARAMS_KEY.KEY_DUMP_VIDEO, false); // 当前网络状态
        String dimensions = mmkv.decodeString(PARAMS_KEY.KEY_DIMENSIONS, "VD_640x480"); // 分辨率
        String fps = mmkv.decodeString(PARAMS_KEY.KEY_FPS, "15"); // 帧率
        String volumeMul = mmkv.decodeString(PARAMS_KEY.KEY_VOLUME_MUL, "1"); // 帧率
        String logSize = mmkv.decodeString(PARAMS_KEY.KEY_LOG_SIZE, "1024"); // sdk log size

        Log.d("JD-DEMO", "dimensions=" + dimensions + " , fps=" + fps);

        // 创建一个自定义View，包含需要输入的参数控件
        View customView = LayoutInflater.from(context).inflate(R.layout.dialog_params_setting, null);
        SwitchCompat addStateSwitch = customView.findViewById(R.id.add_state);
        SwitchCompat dumpVideoSwitch = customView.findViewById(R.id.dump_video);
        AppCompatSpinner fpsSpinner = customView.findViewById(R.id.rate_frame_spinner);
        AppCompatSpinner dimensionsSpinner = customView.findViewById(R.id.dimensions_spinner);
        AppCompatSpinner volumeSpinner = customView.findViewById(R.id.volume_mul__spinner);
        AppCompatSpinner logSizeSpinner = customView.findViewById(R.id.log_size_spinner);

        // ---------------------- 设置默认值 ----------------------
        addStateSwitch.setChecked(addState);
        dumpVideoSwitch.setChecked(dumpVideo);
        String[] mItems = context.getResources().getStringArray(R.array.fps);
        int i = 0;
        for (String item : mItems) {
            if (item.equals(fps)) {
                break;
            }
            i++;
        }
        fpsSpinner.setSelection(i);
        String[] mDimenItems = context.getResources().getStringArray(R.array.dimensions);
        i = 0;
        if (dimensions != null) {
            for (String item : mDimenItems) {
                if (dimensions.equals(item)) {
                    break;
                }
                i++;
            }
        }
        dimensionsSpinner.setSelection(i);
        String[] mVolumeItems = context.getResources().getStringArray(R.array.volumeMultiple);
        i = 0;
        if (mVolumeItems != null) {
            for (String item : mVolumeItems) {
                if (item.equals(volumeMul)) {
                    break;
                }
                i++;
            }
        }
        logSizeSpinner.setSelection(i);
        String[] mLogSizeItems = context.getResources().getStringArray(R.array.logSize);
        i = 0;
        if (mLogSizeItems != null) {
            for (String item : mLogSizeItems) {
                if (item.equals(logSize)) {
                    break;
                }
                i++;
            }
        }
        logSizeSpinner.setSelection(i);

        Log.d("JD-DEMO", "create dialog...1");

        // 创建一个AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // 设置弹框标题和内容
        builder.setTitle("screen capture params");
        // 将自定义View添加到弹框中
        builder.setView(customView);
        // 设置确定按钮和取消按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            // 点击确定保存设置的参数
            Log.d(TAG, " click positive button");

            mmkv.encode(PARAMS_KEY.KEY_ADD_STATE, addStateSwitch.isChecked());
            mmkv.encode(PARAMS_KEY.KEY_DUMP_VIDEO, dumpVideoSwitch.isChecked());
            mmkv.encode(PARAMS_KEY.KEY_DIMENSIONS, context.getResources().getStringArray(R.array.dimensions)[dimensionsSpinner.getSelectedItemPosition()]);
            mmkv.encode(PARAMS_KEY.KEY_FPS, context.getResources().getStringArray(R.array.fps)[fpsSpinner.getSelectedItemPosition()]);
            mmkv.encode(PARAMS_KEY.KEY_VOLUME_MUL, context.getResources().getStringArray(R.array.volumeMultiple)[volumeSpinner.getSelectedItemPosition()]);
            mmkv.encode(PARAMS_KEY.KEY_LOG_SIZE, context.getResources().getStringArray(R.array.logSize)[logSizeSpinner.getSelectedItemPosition()]);

            Log.d("JD-DEMO", "after click::: add state " + mmkv.decodeBool(PARAMS_KEY.KEY_ADD_STATE) + " " +
                    ", dimensions=" + mmkv.decodeString(PARAMS_KEY.KEY_DIMENSIONS)
                    + " , fps=" + mmkv.decodeString(PARAMS_KEY.KEY_FPS) + " , volume=" + mmkv.decodeString(PARAMS_KEY.KEY_VOLUME_MUL)
                    + " , logSize=" + mmkv.decodeString(PARAMS_KEY.KEY_LOG_SIZE));
        });
        builder.setNegativeButton("取消", null);
        builder.setCancelable(false);

        // 创建弹框对象并显示
        AlertDialog dialog = builder.create();
        dialog.show();
        Log.d("JD-DEMO", "create dialog...");
    }
}
