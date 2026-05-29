package com.example.videoapp;

import android.os.SystemClock;
import android.view.View;


// 新建DebouncedOnClickListener.java
public abstract class DebouncedOnClickListener implements View.OnClickListener {
    private static final long MIN_CLICK_INTERVAL = 600; // 毫秒
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            onDebouncedClick(v);
        }
    }

    public abstract void onDebouncedClick(View v);
}



