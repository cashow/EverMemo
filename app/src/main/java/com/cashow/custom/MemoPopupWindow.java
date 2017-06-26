package com.cashow.custom;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.cashow.cashowevermemo.R;

public class MemoPopupWindow extends PopupWindow {
    private int memoId;

	public MemoPopupWindow(Context context, View.OnClickListener itemsOnClick) {
		super(context);

        View mMenuView = LayoutInflater.from(context).inflate(R.layout.popup_memo, null);
        this.setContentView(mMenuView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);

        mMenuView.findViewById(R.id.image_normal).setOnClickListener(itemsOnClick);
        mMenuView.findViewById(R.id.image_green).setOnClickListener(itemsOnClick);
        mMenuView.findViewById(R.id.image_blue).setOnClickListener(itemsOnClick);
        mMenuView.findViewById(R.id.image_grey).setOnClickListener(itemsOnClick);
        mMenuView.findViewById(R.id.image_yellow).setOnClickListener(itemsOnClick);
        mMenuView.findViewById(R.id.image_red).setOnClickListener(itemsOnClick);
        mMenuView.findViewById(R.id.image_delete).setOnClickListener(itemsOnClick);

        ColorDrawable dw = new ColorDrawable(0x8d000000);
        this.setBackgroundDrawable(dw);

        mMenuView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent event) {
                dismiss();
                return true;
            }
        });
	}

    public int getMemoId() {
        return memoId;
    }

    public void setMemoId(int memoId) {
        this.memoId = memoId;
    }
}
