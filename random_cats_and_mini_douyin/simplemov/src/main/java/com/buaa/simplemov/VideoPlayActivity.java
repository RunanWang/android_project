package com.buaa.simplemov;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.airbnb.lottie.LottieAnimationView;
import com.buaa.simplemov.player.VideoPlayerIJK;
import com.buaa.simplemov.player.VideoPlayerListener;
import com.buaa.simplemov.utils.DoubleClickUtils;
import com.buaa.simplemov.utils.NetworkUtils;
import com.buaa.simplemov.utils.ResourceUtils;
import com.buaa.simplemov.utils.SeekBarThread;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import retrofit2.http.Url;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoPlayActivity extends AppCompatActivity {
    private String videoUrl;
    private String user;
    private static final String TAG = "debugR";
    private VideoPlayerIJK ijkPlayer;
    private SeekBar seekBar;
    private Thread seekBarThr;
    private ImageButton imageButton;
    private LottieAnimationView animation;
    private LinearLayout layout;
    private ImageView favorite;
    private int videoHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        animation = findViewById(R.id.animation_view3);
        animation.setVisibility(View.VISIBLE);
        videoUrl = getIntent().getStringExtra("videoUrl");
        user = getIntent().getStringExtra("user");
        layout = findViewById(R.id.button_seekBar);
        favorite = findViewById(R.id.favorite);
        favorite.setVisibility(View.GONE);
        int mCurrentOrientation = getResources().getConfiguration().orientation;

        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // If current screen is portrait
            Log.i("info", "portrait"); // 竖屏
            setContentView(R.layout.activity_video_play);
        } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            //If current screen is landscape
            Log.i("info", "landscape"); // 横屏
            setContentView(R.layout.activity_video_play);
        }
        setTitle("ijkPlayer");
        //mSelectedVideo = getIntent().getData();
        ijkPlayer = findViewById(R.id.ijkPlayer);
        imageButton = findViewById(R.id.imageButton);

        //加载native库
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            this.finish();
        }
        ijkPlayer.setListener(new VideoPlayerListener());
        //ijkPlayer.setVideoResource(R.raw.yuminhong);
        Log.d(TAG, "call download");
        downloadFile3(videoUrl);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ijkPlayer.isPlaying()) {
                    ijkPlayer.pause();
                    imageButton.setImageResource(R.drawable.ic_play);
                } else {
                    ijkPlayer.start();
                    imageButton.setImageResource(R.drawable.ic_pause);
                }

            }
        });

        ijkPlayer.setListener(new VideoPlayerListener(){
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                videoHeight = ijkPlayer.getVideoHeight();
                int videoWidth = ijkPlayer.getVideoWidth();
                if(videoHeight<videoWidth){
                    ijkPlayer.getSurfaceView().setRotation(90);
                }
            }
        });

        ijkPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DoubleClickUtils.isFastDoubleClick()) {
                    favorite.setVisibility(View.VISIBLE);
                    Snackbar.make(seekBar, "已点赞", Snackbar.LENGTH_SHORT).show();
                }


                if (ijkPlayer.isPlaying()) {
                    ijkPlayer.pause();

                    imageButton.setImageResource(R.drawable.ic_play);
                } else {
                    ijkPlayer.start();
                    imageButton.setImageResource(R.drawable.ic_pause);
                }

            }
        });

        seekBar = findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setMax((int) ijkPlayer.getDuration());
                    Log.d(TAG, "onCreate() duration = [" + ijkPlayer.getDuration() + "]");
                    ijkPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarThr = new SeekBarThread(handler, ijkPlayer);

        seekBarThr.start();
    }


//    private String getVideoPath() {
//        return "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8";
////        return "android.resource://" + this.getPackageName() + "/" + resId;
//    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ijkPlayer.isPlaying()) {
            ijkPlayer.stop();
        }
        Log.d(TAG, "onStop() called");
        if (seekBarThr != null) {
            Log.d(TAG, "interrupt!");
            seekBarThr.interrupt();
        }
        IjkMediaPlayer.native_profileEnd();
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //float progress = msg.what/ijkPlayer.get
            seekBar.setMax((int) ijkPlayer.getDuration());
            //Log.d(TAG, "onCreate() duration = [" +  ijkPlayer.getDuration() + "]");
            seekBar.setProgress(msg.what);
            //currentTv.setText(formatTime(msg.what));
        }
    };
    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //float progress = msg.what/ijkPlayer.get

            String mSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String dirName = mSDCardPath + "/SimpleMov/";
            String dest = dirName + videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
            ijkPlayer.setVideoPath(dest);
//            videoHeight = ijkPlayer.getVideoHeight();
//            Log.d(TAG, "video height is " + videoHeight);
//            ijkPlayer.setMinimumHeight(videoHeight);
//            ijkPlayer.setMinimumWidth(ijkPlayer.getVideoWidth());
//            Log.d(TAG, "video width is " + ijkPlayer.getVideoWidth());
//            Log.d(TAG, "player height is " + ijkPlayer.getHeight());
            animation.setVisibility(View.GONE);
        }
    };


    private void downloadFile3(String inputUrl) {
        //下载路径，如果路径无效了，可换成你的下载路径
        final String url = inputUrl;
        final long startTime = System.currentTimeMillis();
        Log.i("DOWNLOAD", "startTime=" + startTime);

        Request request = new Request.Builder().url(url).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                e.printStackTrace();
                Log.i("DOWNLOAD", "download failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Sink sink = null;
                BufferedSink bufferedSink = null;
                try {
                    String mSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String dirName = mSDCardPath + "/SimpleMov/";
                    File file = new File(dirName);
                    //不存在创建
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File dest = new File(dirName, url.substring(url.lastIndexOf("/") + 1));
                    if (!dest.exists()) {
                        Log.d(TAG, "download place = " + dest);
                        Log.d(TAG, "file name = " + url.substring(url.lastIndexOf("/") + 1));

                        sink = Okio.sink(dest);
                        bufferedSink = Okio.buffer(sink);
                        bufferedSink.writeAll(response.body().source());

                        bufferedSink.close();
                        Log.i(TAG, "download success");
                        Log.i("DOWNLOAD", "totalTime=" + (System.currentTimeMillis() - startTime));
                    }
                    handler2.sendEmptyMessage(2);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("DOWNLOAD", "download failed");
                } finally {
                    if (bufferedSink != null) {
                        bufferedSink.close();
                    }

                }
            }
        });
    }
}