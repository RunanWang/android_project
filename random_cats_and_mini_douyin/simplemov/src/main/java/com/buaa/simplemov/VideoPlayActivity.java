package com.buaa.simplemov;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class VideoPlayActivity extends AppCompatActivity {

    String videoUrl;
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        videoUrl = getIntent().getStringExtra("videoUrl");
        user = getIntent().getStringExtra("user");
    }
}
