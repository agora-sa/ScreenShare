package io.agora.agorascreenshare.utils;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.constraintlayout.widget.ConstraintLayout;

public class CommonUtil {

    public static void hideInputBoard(Activity activity, EditText editText)
    {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * 全屏显示指定的布局
     * @param viewGroup 需要缩放的布局View
     */
    public static void setLargeWindow(ViewGroup viewGroup) {
        // 设置LinearLayout的布局参数为大窗口样式
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) viewGroup.getLayoutParams();
        layoutParams.matchConstraintPercentWidth = 1.0f;
        layoutParams.matchConstraintPercentHeight = 1.0f;
        viewGroup.setLayoutParams(layoutParams);
    }

    /**
     * 缩小指定的布局
     * @param viewGroup 需要缩放的嗯布局View
     */
    public static void setSmallWindow(ViewGroup viewGroup) {
        // 设置LinearLayout的布局参数为小窗口样式
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) viewGroup.getLayoutParams();
        layoutParams.matchConstraintPercentWidth = 0.5f;
        layoutParams.matchConstraintPercentHeight = 0.5f;
        viewGroup.setLayoutParams(layoutParams);
    }
}
