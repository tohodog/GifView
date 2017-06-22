package xxx;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by song on 2017-6-21
 * GIF动图
 */
public class GifView extends View {

    public static final int MATRIX = 0;
    public static final int FIT_XY = 1;
    public static final int FIT_START = 2;
    public static final int FIT_CENTER = 3;
    public static final int FIT_END = 4;
    public static final int CENTER = 5;
    public static final int CENTER_CROP = 6;
    public static final int CENTER_INSIDE = 7;


    private int resourceId;
    private String resourcePath;
    private Movie movie;

    private int currentPosition;

    private int scaleType;
    private float mLeft;
    private float mTop;
    private float mScaleX, mScaleY;

    private int time = 50;//绘制间隔毫秒 影响cpu使用率 根据情况调节

    private float speed = 1.0f;
    private volatile boolean isPlaying;
    private boolean mVisible = true;

    private Handler handler = new Handler();

    public GifView(Context context) {
        this(context, null);
    }

    public GifView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setViewAttributes(context, attrs, defStyle);
    }

    private void setViewAttributes(Context context, AttributeSet attrs, int defStyle) {

        //关闭硬件加速,可能有兼容性问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GifView);
        resourceId = array.getResourceId(R.styleable.GifView_gif, -1);
        isPlaying = array.getBoolean(R.styleable.GifView_paused, true);
        scaleType = array.getInt(R.styleable.GifView_scaleType, FIT_CENTER);
        array.recycle();

        if (resourceId != -1) {
            movie = Movie.decodeStream(getResources().openRawResource(resourceId));
        }
        handler.post(refreshRun);
    }

    public void setGifResource(int movieResourceId) {
        resourcePath = null;
        movie = Movie.decodeStream(getResources().openRawResource(this.resourceId = movieResourceId));
        tempNowTime = currentPosition = 0;
        requestLayout();
    }

    public void setGifPath(String path) {
        resourceId = -1;
        movie = Movie.decodeFile(this.resourcePath = path);
        tempNowTime = currentPosition = 0;
        requestLayout();
    }

    public void setScaleType(int scaleType) {
        this.scaleType = scaleType;
        requestLayout();
    }

    /**
     * 设置速度 支持 十倍加减速
     */
    public void setSpeed(float speed) {
        if (speed > 0) {
            if (speed < 0.1)
                speed = 0.1f;
            if (speed > 10)
                speed = 10;
        } else {
            if (speed > -0.1)
                speed = -0.1f;
            if (speed < -10)
                speed = -10;
        }
        this.speed = speed;
    }

    /**
     * 测试发现非常耗费cpu 不要调太高 默认20fps
     */
    public void setFPS(int fps) {
        time = 1000 / fps;
    }

    public int getGifResource() {
        return this.resourceId;
    }

    public String getGifPath() {
        return this.resourcePath;
    }

    public float getSpeed() {
        return speed;
    }

    /**
     * 播放,恢复进度
     */
    public void play() {
        if (!isPlaying) {
            isPlaying = true;
            tempNowTime = 0;
        }
    }

    /**
     * 重播
     */
    public void rePlay() {
        isPlaying = true;
        currentPosition = 0;
        tempNowTime = 0;
    }

    /**
     * 暂停
     */
    public void pause() {
        isPlaying = false;
    }

    /**
     * 停止
     */
    public void stop() {
        if (resourceId > 0)
            movie = Movie.decodeStream(getResources().openRawResource(resourceId));
        else if (resourcePath != null)
            movie = Movie.decodeFile(resourcePath);
        isPlaying = false;
        tempNowTime = 0;
        currentPosition = 0;
        invalidate();
    }

    public boolean isPlaying() {
        return isPlaying;
    }


    /**
     * 根据模式计算缩放倍数和坐标点
     */
    private void setScaleWithMode(int viewW, int viewH, int gifW, int gifH, int mode) {

        float scaleW = 1.0f * viewW / gifW;
        float scaleH = 1.0f * viewH / gifH;
        boolean isH = scaleW > scaleH;//是否gif比view高瘦
        switch (scaleType = mode) {
            case MATRIX:
                mLeft = mTop = 0;
                mScaleX = mScaleY = 1;
                break;
            case FIT_XY:
                mLeft = mTop = 0;
                mScaleX = scaleW;
                mScaleY = scaleH;
                break;
            case FIT_START:
                mLeft = mTop = 0;
                mScaleX = mScaleY = isH ? scaleH : scaleW;
                break;
            default:
            case FIT_CENTER:
                mScaleX = mScaleY = isH ? scaleH : scaleW;
                if (isH) {
                    mTop = 0;
                    mLeft = (viewW - gifW * mScaleX) / 2;
                } else {
                    mLeft = 0;
                    mTop = (viewH - gifH * mScaleY) / 2;
                }
                break;
            case FIT_END:
                mScaleX = mScaleY = isH ? scaleH : scaleW;
                if (isH) {
                    mLeft = viewW - gifW * mScaleX;
                    mTop = 0;
                } else {
                    mLeft = 0;
                    mTop = viewH - gifH * mScaleY;
                }
                break;
            case CENTER:
                mScaleX = mScaleY = 1;
                mLeft = (viewW - gifW) / 2;
                mTop = (viewH - gifH) / 2;
                break;
            case CENTER_CROP:
                mScaleX = mScaleY = !isH ? scaleH : scaleW;
                if (!isH) {
                    mTop = 0;
                    mLeft = (viewW - gifW * mScaleX) / 2;
                } else {
                    mLeft = 0;
                    mTop = (viewH - gifH * mScaleY) / 2;
                }
                break;
            case CENTER_INSIDE:
                if (scaleH < 1 | scaleW < 1) {
                    mScaleX = mScaleY = isH ? scaleH : scaleW;
                    if (isH) {
                        mTop = 0;
                        mLeft = (viewW - gifW * mScaleX) / 2;
                    } else {
                        mLeft = 0;
                        mTop = (viewH - gifH * mScaleY) / 2;
                    }
                } else {
                    mScaleX = mScaleY = 1;
                    mLeft = (viewW - gifW) / 2;
                    mTop = (viewH - gifH) / 2;
                }
                break;
        }
    }

    //MATCH_PARENT（EXACTLY）  WRAP_CONTENT（AT_MOST)
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        if (movie != null) {
//            int movieWidth = movie.width();
//            int movieHeight = movie.height();
//            int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);
//            int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
//        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (movie != null)
            setScaleWithMode(getWidth(), getHeight(), movie.width(), movie.height(), scaleType);
        mVisible = getVisibility() == View.VISIBLE;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (movie == null)
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        else
            drawMovieFrame(canvas);
    }

    private void drawMovieFrame(Canvas canvas) {
        if (!mVisible)
            return;
        if (isPlaying())
            updateAnimationTime();
        movie.setTime(currentPosition);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScaleX, mScaleY);
        movie.draw(canvas, mLeft / mScaleX, mTop / mScaleY);
//        if (currentPosition == 0) {//谜一样的bug 播放过后调用stop settime并不能马上绘制出该帧,调用几次才会,而且time必须递增,目前新建一个movie解决,不用这种方式
//            movie.setTime(10);
//            movie.draw(canvas, mLeft / mScaleX, mTop / mScaleY);
//            movie.setTime(20);
//            movie.draw(canvas, mLeft / mScaleX, mTop / mScaleY);
//        }
        canvas.restore();
//        Log.e("====", "drawMovieFrame" + currentPosition);
    }


    private Runnable refreshRun = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(refreshRun, time);
            if (isPlaying())
                invalidate();
        }
    };
    private long tempNowTime;

    //计算播放进度
    private void updateAnimationTime() {
        long nowTime = android.os.SystemClock.uptimeMillis();
        if (tempNowTime == 0)
            tempNowTime = nowTime;
        int dur = movie.duration();
        if (dur == 0)
            dur = 1000;
        currentPosition = (int) ((dur * 100 + currentPosition + ((nowTime - tempNowTime) % dur) * speed) % dur);
        tempNowTime = nowTime;
    }


    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        mVisible = screenState == SCREEN_STATE_ON;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mVisible = visibility == View.VISIBLE;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == View.VISIBLE;
    }
}