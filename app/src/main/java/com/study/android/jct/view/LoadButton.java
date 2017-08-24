package com.study.android.jct.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.study.android.jct.R;

/**实现了一个具有加载动画的Button
 * Created by jct on 2017/8/24.
 */

public class LoadButton extends View{
    private static final String TAG = "LoadButton";

    private int mTextColor;  //文字颜色
    private float mProgressWidth;  //进度圈宽度
    private int mRectWidth; //中间矩形的宽度
    private int mTopBottomPadding;  //中间矩形上下Padding
    private int mLeftRightPadding;  //中间矩形左右Padding
    private String mText;   //矩形中文字
    private int mTextSize;  //文字大小
    private int mRadiu;     //左右圆的半径
    private Drawable mSuccessedDrawable;    //成功图片
    private Drawable mErrorDrawable;    //失败图片
    private Drawable mPauseDrawable;    //暂停图片
    private int mBackgroundColor;
    private int mProgressSecondColor;   //加载动画底色
    private int mProgressColor;     //加载动画颜色


    private Paint mPaint;
    private TextPaint mTextPaint;
    private Path mPath;

    private RectF leftRect;
    private RectF rightRect;
    private RectF contentRect;
    private RectF progressRect;

    private boolean isUnfold;
    private boolean hasDrawSecondCircle = false;

    private State mCurrentState;
    private float circleSweep;
    private ObjectAnimator loadAnimator;
    private ObjectAnimator shrinkAnim;

    private boolean progressReverse;

    private int mProgressStartAngel;
    private LoadListenner mLoadListenner;


    private enum State {
        INITIAL,    //初始化状态
        FODDING,    //等待加载状态
        LOADDING,   //加载状态
        COMPLETED_ERROR,    //失败状态
        COMPLETED_SUCCESSED,    //成功状态
        LOADDING_PAUSE  //暂停状态
    }


    public LoadButton(Context context) {
        this(context,null);
    }

    public LoadButton(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LoadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.LoadButton);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.LoadButton_android_textSize,50);
        mTextColor = typedArray.getColor(R.styleable.LoadButton_android_textColor, Color.WHITE);
        mText = typedArray.getString(R.styleable.LoadButton_android_text);
        mRadiu = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_radiu,40);
        mTopBottomPadding = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_contentPaddingTB,20);
        mLeftRightPadding = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_contentPaddingLR,20);
        mBackgroundColor = typedArray.getColor(R.styleable.LoadButton_backColor,Color.parseColor("#009966"));
        mProgressColor = typedArray.getColor(R.styleable.LoadButton_progressColor,Color.WHITE);
        mProgressSecondColor = typedArray.getColor(R.styleable.LoadButton_progressSecondColor,Color.parseColor("#c3c3c3"));
        mProgressWidth = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_progressedWidth,2);
        mRectWidth = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_rectWidth,0);
        mSuccessedDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadSuccessDrawable);
        mErrorDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadErrorDrawable);
        mPauseDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadPauseDrawable);
        typedArray.recycle();

        if (mSuccessedDrawable == null) {
            mSuccessedDrawable = context.getResources().getDrawable(R.drawable.yes);
        }
        if (mErrorDrawable == null) {
            mErrorDrawable = context.getResources().getDrawable(R.drawable.no);
        }
        if (mPauseDrawable == null) {
            mPauseDrawable = context.getResources().getDrawable(R.drawable.pause);
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);

        leftRect = new RectF();
        rightRect = new RectF();
        contentRect = new RectF();

        isUnfold = true;

        setOnClickListener(mListenner);

        mCurrentState = State.INITIAL;
    }


    public void reset(){
        mCurrentState = State.INITIAL;
        mRectWidth = getWidth() - mRadiu * 2;
        isUnfold = true;
        hasDrawSecondCircle = false;
        cancelAnimation();
        invaidateSelft();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int resultW = widthSize;
        int resultH = heightSize;

        int contentW = 0;
        int contentH = 0;

        if ( widthMode == MeasureSpec.AT_MOST ) {
            int mTextWidth = (int) mTextPaint.measureText(mText);
            contentW += mTextWidth + mLeftRightPadding * 2 + mRadiu * 2;
            resultW = contentW < widthSize ? contentW : widthSize;
        }

        if ( heightMode == MeasureSpec.AT_MOST ) {
            contentH += mTopBottomPadding * 2 + mTextSize;
            resultH = contentH < heightSize ? contentH : heightSize;
        }

        resultW = resultW < 2 * mRadiu ? 2 * mRadiu : resultW;
        resultH = resultH < 2 * mRadiu ? 2 * mRadiu : resultH;

        if ((resultW - 2*mRadiu) < mRectWidth){
            resultW = mRectWidth + 2 * mRadiu;
        }

        mRadiu = resultH / 2;

        mRectWidth = resultW - 2 * mRadiu;

        setMeasuredDimension(resultW,resultH);

        Log.d(TAG,"onMeasure: w:"+resultW+" h:"+resultH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        drawPath(canvas,cx,cy);

        int textDescent = (int) mTextPaint.getFontMetrics().descent;
        int textAscent = (int) mTextPaint.getFontMetrics().ascent;
        int delta = Math.abs(textAscent) - textDescent;

        int circleR = mRadiu / 2;

        if ( mCurrentState == State.INITIAL) {

            canvas.drawText(mText,cx,cy + delta / 2,mTextPaint);

        } else if ( mCurrentState == State.LOADDING ) {

            if ( progressRect == null ) {
                progressRect = new RectF();
            }
            progressRect.set(cx - circleR,cy - circleR,cx + circleR,cy + circleR);

            if (!hasDrawSecondCircle){
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mProgressWidth);
                mPaint.setColor(mProgressSecondColor);
                canvas.drawCircle(cx,cy,circleR,mPaint);
            }
            mPaint.setColor(mProgressColor);

            Log.d(TAG,"onDraw() pro:"+progressReverse+" swpeep:"+circleSweep);
            if ( circleSweep != 360 ) {
                mProgressStartAngel = progressReverse ? 270 : (int) (270 + circleSweep);
                canvas.drawArc(progressRect
                        ,mProgressStartAngel,progressReverse ? circleSweep : (int) (360 - circleSweep),
                        false,mPaint);
            }
            mPaint.setColor(mBackgroundColor);
        } else if ( mCurrentState == State.COMPLETED_ERROR ) {
            mErrorDrawable.setBounds(cx - circleR,cy - circleR,cx + circleR,cy + circleR);
            mErrorDrawable.draw(canvas);
        } else if (mCurrentState == State.COMPLETED_SUCCESSED) {
            mSuccessedDrawable.setBounds(cx - circleR,cy - circleR,cx + circleR,cy + circleR);
            mSuccessedDrawable.draw(canvas);
        } else if (mCurrentState == State.LOADDING_PAUSE) {
            mPauseDrawable.setBounds(cx - circleR,cy - circleR,cx + circleR,cy + circleR);
            mPauseDrawable.draw(canvas);
        }


    }

    private void drawPath(Canvas canvas,int cx,int cy) {
        if (mPath == null) {
            mPath = new Path();
        }
        mPath.reset();

        int left = cx - mRectWidth / 2 - mRadiu;
        int top = 0;
        int right = cx + mRectWidth / 2 + mRadiu;
        int bottom = getHeight();

        leftRect.set(left, top, left + mRadiu * 2, bottom);
        rightRect.set(right - mRadiu * 2, top, right, bottom);
        contentRect.set(cx- mRectWidth /2, top,cx + mRectWidth /2, bottom);
        mPath.moveTo(cx - mRectWidth /2, bottom);
        mPath.arcTo(leftRect,
                90.0f,180f);
        mPath.lineTo(cx + mRectWidth /2, top);
        mPath.arcTo(rightRect,
                270.0f,180f);

        mPath.close();

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBackgroundColor);
        canvas.drawPath(mPath,mPaint);
    }

    public void setRectWidth (int width) {
        mRectWidth = width;
        invaidateSelft();
    }

    private void invaidateSelft() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public void shringk() {
        if (shrinkAnim == null) {
            shrinkAnim = ObjectAnimator.ofInt(this,"rectWidth", mRectWidth,0);
        }
        shrinkAnim.setDuration(500);
        shrinkAnim.start();
        shrinkAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isUnfold = false;
                load();
            }
        });
        mCurrentState = State.FODDING;
    }

    public void load() {
        if (loadAnimator == null) {
            //这里为什么360的时候会绘制整个圆
            loadAnimator = ObjectAnimator.ofFloat(this,"circleSweep",0,359);
        }

        loadAnimator.setDuration(1000);
        loadAnimator.setRepeatCount(ValueAnimator.INFINITE);

        loadAnimator.removeAllListeners();

        loadAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                progressReverse = !progressReverse;
            }
        });
        loadAnimator.start();
        mCurrentState = State.LOADDING;
    }

    public void setCircleSweep(float circleSweep) {
        this.circleSweep = circleSweep;
        invaidateSelft();
    }

    public void loadSuccessed() {
        mCurrentState = State.COMPLETED_SUCCESSED;
        cancelAnimation();
        invaidateSelft();
    }

    public void loadFailed() {
        mCurrentState = State.COMPLETED_ERROR;
        cancelAnimation();
        invaidateSelft();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimation();
    }

    private void cancelAnimation() {
        if ( shrinkAnim != null && shrinkAnim.isRunning() ) {
            shrinkAnim.removeAllListeners();
            shrinkAnim.cancel();
            shrinkAnim = null;
        }
        if ( loadAnimator != null && loadAnimator.isRunning() ) {
            loadAnimator.removeAllListeners();
            loadAnimator.cancel();
            loadAnimator = null;
        }
    }

    private OnClickListener mListenner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentState == State.FODDING) {
                return;
            }
            if (mCurrentState == State.INITIAL) {
                if (isUnfold) {
                    shringk();
                }
            } else if (mCurrentState == State.COMPLETED_ERROR) {
                if (mLoadListenner != null) {
                    mLoadListenner.onClick(false);
                }
            } else if (mCurrentState == State.COMPLETED_SUCCESSED) {
                if (mLoadListenner != null) {
                    mLoadListenner.onClick(true);
                }
            } else if (mCurrentState == State.LOADDING_PAUSE) {
                if (mLoadListenner != null) {
                    mLoadListenner.needLoading();
                    load();
                }
            } else if (mCurrentState == State.LOADDING) {
                mCurrentState = State.LOADDING_PAUSE;
                cancelAnimation();
                invaidateSelft();
            }
        }
    };

    public interface LoadListenner {

        void onClick(boolean isSuccessed);

        void needLoading();
    }

    public LoadListenner getListenner() {
        return mLoadListenner;
    }

    public void setListenner(LoadListenner listenner) {
        this.mLoadListenner = listenner;
    }

}
