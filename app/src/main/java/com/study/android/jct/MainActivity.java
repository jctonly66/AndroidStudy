package com.study.android.jct;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.study.android.jct.Animator.AnimatorActivty;
import com.study.android.jct.ImageLoader.ImageLoaderActivity;
import com.study.android.jct.MemoryTest.MemoryActivity;
import com.study.android.jct.view.LoadButtonActivity;

/**
 * Created by 10764 on 2017/8/19.
 */

public class MainActivity extends Activity implements View.OnClickListener{
    private Button animatorButton;
    private Button imageLoaderButton;
    private Button memoryTestButton;
    private Button myViewButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        animatorButton = (Button) findViewById(R.id.bt_animator);
        imageLoaderButton = (Button) findViewById(R.id.bt_imageloader);
        memoryTestButton = (Button) findViewById(R.id.bt_memory_test);
        myViewButton = (Button) findViewById(R.id.bt_view);

        animatorButton.setOnClickListener(this);
        imageLoaderButton.setOnClickListener(this);
        memoryTestButton.setOnClickListener(this);
        myViewButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_animator:
                Intent intent = new Intent(MainActivity.this, AnimatorActivty.class);
                startActivity(intent);
                break;
            case R.id.bt_imageloader:
                Intent intent1 = new Intent(MainActivity.this,ImageLoaderActivity.class);
                startActivity(intent1);
                break;
            case R.id.bt_memory_test:
                Intent intent2 = new Intent(MainActivity.this,MemoryActivity.class);
                startActivity(intent2);
                break;
            case R.id.bt_view:
                Intent intent3 = new Intent(MainActivity.this,LoadButtonActivity.class);
                startActivity(intent3);
                break;
        }
    }
}
