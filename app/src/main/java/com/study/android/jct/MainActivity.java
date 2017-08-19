package com.study.android.jct;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.study.android.jct.Animator.AnimatorActivty;
import com.study.android.jct.ImageLoader.ImageLoaderActivity;

/**
 * Created by 10764 on 2017/8/19.
 */

public class MainActivity extends Activity implements View.OnClickListener{
    private Button animatorButton;
    private Button imageLoaderButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        animatorButton = (Button) findViewById(R.id.bt_animator);
        imageLoaderButton = (Button) findViewById(R.id.bt_imageloader);

        animatorButton.setOnClickListener(this);
        imageLoaderButton.setOnClickListener(this);

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
        }
    }
}
