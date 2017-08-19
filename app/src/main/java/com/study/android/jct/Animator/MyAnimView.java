package com.study.android.jct.Animator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by 10764 on 2017/8/17.
 */

public class MyAnimView extends View {
    public static final float RADIUS = 50f;
    private Point currentPoint;
    private Paint mPaint;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        mPaint.setColor(Color.parseColor(color));
        invalidate();
    }

    public void startViewAnimator(Interpolator interpolator){
        startAnimator(interpolator);
    }
    private String color;

    public MyAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentPoint == null){
            currentPoint = new Point(getWidth()/2,RADIUS);
            drawCircle(canvas);
            startAnimator(new BounceInterpolator());
        }else {
            drawCircle(canvas);
        }
    }

    private void drawCircle(Canvas canvas){
        float x = currentPoint.getX();
        float y = currentPoint.getY();
        canvas.drawCircle(x,y,RADIUS,mPaint);
    }

    private void startAnimator(Interpolator interpolator){
        Point startPoint = new Point(getWidth()/2,RADIUS);
        Point endPoint = new Point(getWidth()/2 , getHeight() - RADIUS);
        final ValueAnimator anim = ValueAnimator.ofObject(new PointEvaluator(),startPoint,endPoint);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentPoint = (Point) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        ObjectAnimator colorAnimator = ObjectAnimator.ofObject(this,"color",new ColorEvaluator(),"#0000FF","#FF0000");
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(anim).with(colorAnimator);
        animatorSet.setInterpolator(interpolator);
        animatorSet.setDuration(5000);
        animatorSet.start();
    }
}
