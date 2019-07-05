package com.buaa.simplemov;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
    }

    public void Solution2C1(View view) {
        startActivity(new Intent(this, Solution2C1Activity.class));
    }

    public void Solution2C2(View view) {
        startActivity(new Intent(this, Solution2C2Activity.class));

    }
}
