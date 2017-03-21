package com.fsw.musicbutton.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.fsw.musicbutton.R;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fsw on 2017/3/20.
 * 带进度条的播放按钮
 */

public class MusicProgressButton extends View {
    /**
     * 中心圆的背景色
     */
    private int centerCircleColor = 0xffe12a58;
    /**
     * 进度条的颜色
     * 和进度条底色
     */
    private int progressColor = 0xff50d2c2;
    private int progressBackgroundColor = 0x00ffffff;
    /**
     * 进度条的宽度
     */
    private float progressLineWidth = 5;
    /**
     * 设置最大的进度
     */
    private float maxProgress = 100;
    /**
     * 当前的进度
     */
    private float currentProgress = 0;
    /**
     * 竖条的宽度
     */
    private float barLineWidth = progressLineWidth;
    /**
     * 竖条的颜色
     */
    private int barLineColor = 0xffffffff;
    /**
     * 播放按钮的背景色
     */
    private int playButtonColor = 0xffffffff;
    /**
     * 动画的速度
     */
    private int speed = 100;
    /**
     * 开始按钮的点击事件
     */
    private OnStartClickListener onStartClickListener;
    /**
     * 是否开始播放了
     */
    private boolean isPlay = false;
    /**
     * 画笔
     */
    private Paint paint;
    /**
     * 控件的圆心坐标
     * 半径
     */
    private int centerX, centerY, radius;
    /**
     * 开始的角度
     */
    private int startAngle = -90;
    /**
     * 根据这个三个数字随机一个比例设置三条竖线的高度
     */
    private Random random = new Random();
    private float randomLineOne = 100;
    private float randomLineTwo = 100;
    private float randomLineThr = 100;
    /**
     * 使用Timer ,TimerTask 和 Handler三者相结合的方法使三天线跳跃起来
     */
    private Timer timer;
    private TimerTask timerTask;
    private MyHandler handler = new MyHandler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (currentProgress > maxProgress) {
                        currentProgress = 0;
                    }
                    currentProgress++;
                    randomLineOne = random.nextInt(100);
                    randomLineTwo = random.nextInt(100);
                    randomLineThr = random.nextInt(100);
                    invalidate();
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 中心触摸区域
     */
    private Path centerPath;
    private Region centerRegion;
    /**
     * 最外层RectF
     */
    private RectF rectF;
    /**
     * 播放按钮三角形
     */
    private Path path;
    /**
     * 改参数使用是用来判断为点击事件还是滑动事件
     * 手机不同，可能这个参数也不相同
     */
    private int minSlide;

    public MusicProgressButton(Context context) {
        this(context, null);
    }

    public MusicProgressButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicProgressButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicProgressButton);
        centerCircleColor = typedArray.getColor(R.styleable.MusicProgressButton_centerCircleColor, centerCircleColor);
        progressColor = typedArray.getColor(R.styleable.MusicProgressButton_progressColor, progressColor);
        progressBackgroundColor = typedArray.getColor(R.styleable.MusicProgressButton_progressBackgroundColor, progressBackgroundColor);
        progressLineWidth = typedArray.getDimension(R.styleable.MusicProgressButton_progressBackgroundColor, 0) == 0 ? progressLineWidth : dp2px(typedArray.getDimension(R.styleable.MusicProgressButton_progressBackgroundColor, 0));
        maxProgress = typedArray.getFloat(R.styleable.MusicProgressButton_maxProgress, maxProgress);
        currentProgress = typedArray.getFloat(R.styleable.MusicProgressButton_currentProgress, currentProgress);
        barLineWidth = typedArray.getDimension(R.styleable.MusicProgressButton_barLineWidth, 0) == 0 ? barLineWidth : dp2px(typedArray.getDimension(R.styleable.MusicProgressButton_barLineWidth, 0));
        barLineColor = typedArray.getColor(R.styleable.MusicProgressButton_barLineColor, barLineColor);
        playButtonColor = typedArray.getColor(R.styleable.MusicProgressButton_playButtonColor, playButtonColor);
        speed = typedArray.getInt(R.styleable.MusicProgressButton_speed, speed);
        typedArray.recycle();
        initView(context);
    }

    /**
     * 初始化控件
     */
    private void initView(Context context) {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isPlay) {//如果是在播放才执行任务
                    handler.sendEmptyMessage(0);
                }
            }
        };
        paint = new Paint();
        paint.setAntiAlias(true);
        centerPath = new Path();
        path = new Path();
        minSlide = ViewConfiguration.get(context).getScaledTouchSlop();
        timer.schedule(timerTask, 1000, speed);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measureLength(widthMeasureSpec);
        int measuredHeight = measureLength(heightMeasureSpec);
        int flag = Math.min(measuredHeight / 2, measuredWidth / 2);
        centerX = centerY = flag;
        radius = flag - 10;
        centerRegion = new Region(centerX - radius + 10, centerY - radius + 10, centerX + radius - 10, centerY + radius - 10);
        rectF = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        //设置该控件的高宽为半径的二位
        setMeasuredDimension(flag * 2, flag * 2);
    }

    private int measureLength(int measureSpec) {
        int specModel = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 0;
        if (specModel == MeasureSpec.EXACTLY || specModel == MeasureSpec.AT_MOST) {
            result = specSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centerPath.addCircle(centerX, centerY, radius - 10, Path.Direction.CW);
        centerRegion.setPath(centerPath, centerRegion);
        //外圆的底色
        paint.setColor(progressBackgroundColor);
        paint.setStrokeWidth(progressLineWidth);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(centerX, centerY, radius, paint);
        //绘制进度条
        paint.setColor(progressColor);

        canvas.drawArc(rectF, startAngle, (currentProgress / maxProgress) * 360, false, paint);
        //绘制中心圆
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(centerCircleColor);
        canvas.drawCircle(centerX, centerY, radius - progressLineWidth / 2, paint);
        if (isPlay) {
            //绘制三个竖条, 首先确定三条数显的位置，高宽, 在这里假设三条竖线为半径的三根之一
            paint.setColor(barLineColor);
            paint.setStrokeWidth(barLineWidth);
            //画第一条竖线
            canvas.drawLine(centerX - radius / 3, centerY + radius / 3, centerX - radius / 3, (centerY + radius / 3) - ((radius * 2 / 3) * randomLineOne / 100), paint);
            //画第二条线
            canvas.drawLine(centerX, centerY + radius / 3, centerX, (centerY + radius / 3) - ((radius * 2 / 3) * randomLineTwo / 100), paint);
            //画第三条线
            canvas.drawLine(centerX + radius / 3, centerY + radius / 3, centerX + radius / 3, (centerY + radius / 3) - ((radius * 2 / 3) * randomLineThr / 100), paint);
        } else {
            //画一个未播放时的指示，这里用一个三角形
            paint.setColor(playButtonColor);
            paint.setStyle(Paint.Style.FILL);
            path.moveTo(centerX - radius / 3 + 10, centerY - radius / 3);
            path.lineTo(centerX - radius / 3 + 10, centerY + radius / 3);
            path.lineTo(centerX + radius / 3, centerY);
            path.close(); // 使这些点构成封闭的多边形
            canvas.drawPath(path, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = 0, y = 0;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (centerRegion.contains((int) event.getX(), (int) event.getY())) {//该判断是为了判断点击的区域是否为指定的播放按钮触发区域
                    x = event.getX();
                    y = event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (minSlide <= (Math.abs(x - event.getX())) && minSlide <= (Math.abs(y - event.getY()))) {//此判断表示为点击事件而不是滑动事件
                    if (centerRegion.contains((int) event.getX(), (int) event.getY())) {//该判断是为了判断点击的区域是否为指定的播放按钮触发区域
                        if (onStartClickListener != null) {
                            onStartClickListener.onStartClickListener(isPlay);
                        }
                        if (isPlay) {
                            stopPlay();
                        } else {
                            startPlay();
                        }
                    }
                }
                invalidate();
                break;
        }
        return true;
    }

    /**
     * 控制开始播放
     */
    public void startPlay() {
        isPlay = true;
        invalidate();
    }

    /**
     * 控制停止播放
     */
    public void stopPlay() {
        isPlay = false;
        invalidate();
    }

    /**
     * 将dp转为px
     *
     * @param dpValue
     * @return
     */
    public int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 控制播放按钮点击的时间监听
     */
    private interface OnStartClickListener {
        void onStartClickListener(boolean isStart);
    }

    /**
     * 此写法为了解决This Handler class should be static or leaks might occur警告
     * 大致意思就是说：Handler类应该定义成静态类，否则可能导致内存泄露。
     */
    private static class MyHandler extends Handler {

    }

    public OnStartClickListener getOnStartClickListener() {
        return onStartClickListener;
    }

    public void setOnStartClickListener(OnStartClickListener onStartClickListener) {
        this.onStartClickListener = onStartClickListener;
        invalidate();
    }

    public int getCenterCircleColor() {
        return centerCircleColor;
    }

    public void setCenterCircleColor(int centerCircleColor) {
        this.centerCircleColor = centerCircleColor;
        invalidate();
    }

    public int getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        invalidate();
    }

    public int getProgressBackgroundColor() {
        return progressBackgroundColor;
    }

    public void setProgressBackgroundColor(int progressBackgroundColor) {
        this.progressBackgroundColor = progressBackgroundColor;
        invalidate();
    }

    public float getProgressLineWidth() {
        return progressLineWidth;
    }

    public void setProgressLineWidth(int progressLineWidth) {
        this.progressLineWidth = progressLineWidth;
        invalidate();
    }

    public float getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }

    public float getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(float currentProgress) {
        this.currentProgress = currentProgress;
        invalidate();
    }

    public float getBarLineWidth() {
        return barLineWidth;
    }

    public void setBarLineWidth(int barLineWidth) {
        this.barLineWidth = barLineWidth;
        invalidate();
    }

    public int getBarLineColor() {
        return barLineColor;
    }

    public void setBarLineColor(int barLineColor) {
        this.barLineColor = barLineColor;
        invalidate();
    }

    public int getPlayButtonColor() {
        return playButtonColor;
    }

    public void setPlayButtonColor(int playButtonColor) {
        this.playButtonColor = playButtonColor;
        invalidate();
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        invalidate();
    }

}
