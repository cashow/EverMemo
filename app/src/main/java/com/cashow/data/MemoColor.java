package com.cashow.data;

import com.cashow.cashowevermemo.R;

public class MemoColor {
    public final static String NORMAL = "normal";
    public final static String BLUE = "blue";
    public final static String RED = "red";
    public final static String GREEN = "green";
    public final static String GREY = "grey";
    public final static String YELLOW = "yellow";

    public final static int getColorId(String color) {
        if (color.equals(NORMAL)) {
            return R.color.bg_normal;
        } else if (color.equals(BLUE)) {
            return R.color.bg_blue;
        } else if (color.equals(RED)) {
            return R.color.bg_red;
        } else if (color.equals(GREEN)) {
            return R.color.bg_green;
        } else if (color.equals(GREY)) {
            return R.color.bg_grey;
        } else if (color.equals(YELLOW)) {
            return R.color.bg_yellow;
        }
        return R.color.bg_normal;
    }
}
