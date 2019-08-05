package com.drumpads.drumpad.musicmaker;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomDrawerLayout extends DrawerLayout {

    private Activity activity;

    public CustomDrawerLayout(@NonNull Context context) {
        super(context);
    }

    public CustomDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return true;
//    }
//
//    public void setActivity (Activity activity) {
//        this.activity = activity;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        activity.onTouchEvent(ev);
//
//        return false;
//    }
}
