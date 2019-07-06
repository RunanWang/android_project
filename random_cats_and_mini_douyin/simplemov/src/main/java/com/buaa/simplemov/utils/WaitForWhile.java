package com.buaa.simplemov.utils;

import android.os.Handler;

public class WaitForWhile extends Thread {
    private Handler handler;

    public WaitForWhile(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessage(1);
    }

}
