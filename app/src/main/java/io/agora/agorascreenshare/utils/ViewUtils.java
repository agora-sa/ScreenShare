package io.agora.agorascreenshare.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewUtils {

    /**
     * 业务逻辑-根据输入计算宽高
     * @param dimensions 输入的特定值
     * @return 返回宽、高的数组，元素0为宽，元素1为高
     */
    public static int[] computeWidthAndHeight(String dimensions) {
        int[] dimens = new int[2];
        if (TextUtils.isEmpty(dimensions)) {
            dimens[0] = 1280;
            dimens[1] = 720;
            return dimens;
        }
        int indexOfX = dimensions.indexOf("x");
        dimens[0] = Integer.parseInt(dimensions.substring(3, indexOfX));
        dimens[1] = Integer.parseInt(dimensions.substring(indexOfX + 1));
        return dimens;
    }

    /**
     * 使传入的view全屏
     *
     * @param viewGroup 要全屏的view
     */
    public static void setLargeWindow(ViewGroup viewGroup) {
        setLargeWindow(viewGroup, 1.0f, 1.0f);
    }

    private static void setLargeWindow(ViewGroup viewGroup, float widthF, float heightF) {
        // 设置LinearLayout的布局参数为大窗口样式
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) viewGroup.getLayoutParams();
        layoutParams.matchConstraintPercentWidth = widthF;
        layoutParams.matchConstraintPercentHeight = heightF;
        viewGroup.setLayoutParams(layoutParams);
    }

    /**
     * 使传入的view宽高缩小0.5倍
     *
     * @param viewGroup 要缩小的view
     */
    public static void setSmallWindow(ViewGroup viewGroup) {
        setSmallWindow(viewGroup, 0.5f, 0.5f);
    }

    private static void setSmallWindow(ViewGroup viewGroup, float widthF, float heightF) {
        // 设置LinearLayout的布局参数为小窗口样式
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) viewGroup.getLayoutParams();
        layoutParams.matchConstraintPercentWidth = widthF;
        layoutParams.matchConstraintPercentHeight = heightF;
        viewGroup.setLayoutParams(layoutParams);
    }

    public static void hideInputBoard(Activity activity, EditText editText) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
