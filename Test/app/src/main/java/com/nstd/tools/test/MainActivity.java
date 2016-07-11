package com.nstd.tools.test;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @OnClick(R.id.btn_test_mime)
    public void startMime() {
        Intent intent = new Intent(this, TestMimeActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_test_mouse)
    public void startMouse() {
        Intent intent = new Intent(this, MouseActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
//        Log.e(TAG, String.format("%.0f", 1.12f));
//        Log.e(TAG, String.format("%.1f", 1.12f));
    }

}
