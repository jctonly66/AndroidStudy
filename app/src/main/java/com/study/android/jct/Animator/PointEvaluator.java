package com.study.android.jct.Animator;

import android.animation.TypeEvaluator;

import com.study.android.jct.Animator.Point;

/**
 * Created by 10764 on 2017/8/17.
 */

public class PointEvaluator implements TypeEvaluator {
    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        Point startPoint = (Point) startValue;
        Point endPoint = (Point) endValue;
        float x = startPoint.getX() + fraction * (endPoint.getX() - startPoint.getX());
        float y = startPoint.getY() + fraction * (endPoint.getY() - startPoint.getY());
        Point point = new Point(x,y);
        return point;
    }
}
