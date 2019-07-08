package com.buaa.simplemov.utils;

import android.os.Handler;
import android.util.Log;

import com.buaa.simplemov.player.VideoPlayerIJK;


public class SeekBarThread extends Thread {
    private Handler handler = null;
    private VideoPlayerIJK ijkPlayer = null;

    private static final String TAG = "debugR";

    public SeekBarThread(Handler handler, VideoPlayerIJK ijkPlayer) {
        this.handler = handler;
        this.ijkPlayer = ijkPlayer;
    }

    @Override
    public void run() {
        //while (ijkPlayer != null && ijkPlayer.isPlaying()) {
        boolean flag = false;
        while (!flag) {
            // 将SeekBar位置设置到当前播放位置

            //Message msg = new Message()
            //Log.d(TAG, "run() called " +isInterrupted());
            handler.sendEmptyMessage((int) ijkPlayer.getCurrentPosition());
            //Log.d(TAG, "run: current position = " + ijkPlayer.getCurrentPosition());
            try {
                // 每100毫秒更新一次位置
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.d(TAG, "run() called" + isInterrupted());
                flag = true;
            }

        }

    }
}
