package com.study.android.jct.MemoryTest;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.study.android.jct.R;

import java.util.ArrayList;
import java.util.List;

public class MemoryActivity extends AppCompatActivity {
    private static final String TAG = "MemoryActivity";
    private static Context sContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = activityManager.getMemoryClass();
        Log.w(TAG, "APP内存限制为：" + String.valueOf(memoryClass));

        sContext = this;

    }
}
