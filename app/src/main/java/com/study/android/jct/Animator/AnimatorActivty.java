package com.study.android.jct.Animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ViewStubCompat;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.study.android.jct.R;

public class AnimatorActivty extends Activity implements View.OnClickListener{
    private Button alphaButton;
    private Button rotationButton;
    private Button translationButton;
    private Button scaleXButton;
    private Button animatorSetButton;
    private Button objectAnimatorButton;
    private Button interpolatorButton;
    private ViewStubCompat vsc_object_animator;
    private TextView tv_property_animator;
    private MyAnimView myAnimView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animator);

        alphaButton = (Button) findViewById(R.id.bt_alpha);
        rotationButton = (Button) findViewById(R.id.bt_Rotation);
        translationButton = (Button) findViewById(R.id.bt_Translation);
        scaleXButton = (Button) findViewById(R.id.bt_ScaleY);
        animatorSetButton = (Button) findViewById(R.id.bt_AnimatorSet);
        objectAnimatorButton = (Button) findViewById(R.id.bt_getViewStub);
        interpolatorButton = (Button) findViewById(R.id.bt_Interpolator);
        tv_property_animator = (TextView) findViewById(R.id.tv_property_animator);
        vsc_object_animator = (ViewStubCompat) findViewById(R.id.viewstup);

        alphaButton.setOnClickListener(this);
        rotationButton.setOnClickListener(this);
        translationButton.setOnClickListener(this);
        scaleXButton.setOnClickListener(this);
        animatorSetButton.setOnClickListener(this);
        objectAnimatorButton.setOnClickListener(this);
        interpolatorButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        ObjectAnimator anim;
        switch (view.getId()){
            case R.id.bt_alpha:
                anim  = ObjectAnimator.ofFloat(tv_property_animator,"alpha",1f,0f,1f);
                anim.setDuration(5000);
                anim.start();
                break;
            case R.id.bt_Rotation:
                anim = ObjectAnimator.ofFloat(tv_property_animator,"rotation",0f,360f);
                anim.setDuration(5000);
                anim.start();
                break;
            case R.id.bt_Translation:
                float curTranslationX = tv_property_animator.getTranslationX();
                anim = ObjectAnimator.ofFloat(tv_property_animator,"translationX",curTranslationX,-500f,curTranslationX);
                anim.setDuration(5000);
                anim.start();
                break;
            case R.id.bt_ScaleY:
                anim = ObjectAnimator.ofFloat(tv_property_animator,"scaleY",1f,3f,1f);
                anim.setDuration(5000);
                anim.start();
                break;
            case R.id.bt_AnimatorSet:
                float curTranslationX1 = tv_property_animator.getTranslationX();
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(tv_property_animator,"translationX",curTranslationX1,-500f,curTranslationX1);
                ObjectAnimator anim2 = ObjectAnimator.ofFloat(tv_property_animator,"rotation",0f,360f);
                ObjectAnimator anim3 = ObjectAnimator.ofFloat(tv_property_animator,"alpha",1f,0f,1f);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(anim1).with(anim2).with(anim3);
                animatorSet.setDuration(5000);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        Toast.makeText(AnimatorActivty.this,"AnimatorSet动画开始",Toast.LENGTH_SHORT).show();
                    }
                });
                animatorSet.start();
                break;
            case R.id.bt_getViewStub:
                if ( myAnimView == null){
                    myAnimView = (MyAnimView) vsc_object_animator.inflate();
                    Toast.makeText(AnimatorActivty.this,"加载ofObject动画，使用viewstub标签延迟加载",Toast.LENGTH_SHORT).show();
                }else {
                    myAnimView.startViewAnimator(new BounceInterpolator());
                    Toast.makeText(AnimatorActivty.this,"ViewStub不可重复加载",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_Interpolator:
                if ( myAnimView == null){
                    Toast.makeText(AnimatorActivty.this,"请先使用Object动画",Toast.LENGTH_SHORT).show();
                }else {
                    myAnimView.startViewAnimator(new MyInterpolator());
                }
                break;
        }

    }
}
