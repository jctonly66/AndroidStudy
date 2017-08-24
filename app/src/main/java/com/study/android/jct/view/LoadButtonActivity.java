package com.study.android.jct.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.study.android.jct.R;

public class LoadButtonActivity extends AppCompatActivity implements View.OnClickListener{

    LoadButton mLoadButton;
    Button mBtnSuccessed,mBtnError,mBtnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_button);

        mLoadButton = (LoadButton) findViewById(R.id.btn_status);
        mBtnSuccessed = (Button) findViewById(R.id.btn_test_successed);
        mBtnError = (Button) findViewById(R.id.btn_test_error);
        mBtnReset = (Button) findViewById(R.id.btn_reset);
        mBtnError.setOnClickListener(this);
        mBtnSuccessed.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);

        mLoadButton.setListenner(new LoadButton.LoadListenner() {
            @Override
            public void onClick(boolean isSuccessed) {
                if ( isSuccessed ) {
                    Toast.makeText(LoadButtonActivity.this,"加载成功",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoadButtonActivity.this,"加载失败",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void needLoading() {
                Toast.makeText(LoadButtonActivity.this,"重新下载",Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_test_successed:
                mLoadButton.loadSuccessed();
                break;

            case R.id.btn_test_error:
                mLoadButton.loadFailed();
                break;
            case R.id.btn_reset:
                mLoadButton.reset();
                break;

            default:
                break;
        }
    }
}
