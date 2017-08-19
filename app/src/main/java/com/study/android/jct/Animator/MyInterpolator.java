package com.study.android.jct.Animator;

import android.view.animation.Interpolator;

/**
 * Created by 10764 on 2017/8/17.
 */

public class MyInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
        float result;

        result = (float) Math.sin(Math.PI*input);

        return result;
    }
}
