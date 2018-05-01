package us.xingkong.Dashboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

/**
 * @作者: Xuer
 * @包名: us.xingkong.view
 * @类名: Dashboard
 * @创建时间: 2018/4/29 17:08
 * @最后修改于:
 * @版本: 1.0
 * @描述: 一个自定义的仪表盘
 * @更新日志:
 * @参考的文章： Android 自定义控件 仿支付宝芝麻信用的刻度盘       https://blog.csdn.net/cekiasoo/article/details/55823287
 * Android自定义view之仿支付宝芝麻信用仪表盘 ---by ccy     https://blog.csdn.net/ccy0122/article/details/53241648
 * android 自定义View 仪表盘 DashboardView 的实现        https://blog.csdn.net/qq_17422503/article/details/51769672
 * Android 仪表盘View      https://blog.csdn.net/qq_26411333/article/details/52399831
 * android canvas drawText()文字居中        https://blog.csdn.net/zly921112/article/details/50401976
 * Android自定义View仿支付宝芝麻信用分仪表盘       https://m.jb51.net/article/92077.htm
 * Android Bitmap 常见的几个操作：缩放，裁剪，旋转，偏移       https://www.cnblogs.com/rustfisher/p/5071494.html
 * 自定义View实战（一） 汽车速度仪表盘     https://blog.csdn.net/lxk_1993/article/details/51373269
 * android自定义view之汽车仪表盘增强版      https://blog.csdn.net/u010129985/article/details/52837779
 * 手把手带你画一个 时尚仪表盘 Android 自定义View       https://www.2cto.com/kf/201601/465192.html
 * Android 自定义圆形带刻度渐变色的进度条样式实例代码        http://www.jb51.net/article/98400.htm
 * Android环形颜色渐变进度条     https://blog.csdn.net/ywl5320/article/details/50507196
 * Android画个颜色渐变的圆环玩玩       https://blog.csdn.net/qp23401/article/details/50373660
 * @目前的问题： (1) 还没加动画效果，后期会加的
 * (2) 第二段未达到的圆弧长度会改变(似乎是用float计算的问题)
 * (3) 分数为0时指针会飞到右上角去
 */

public class Dashboard extends View {

    // 画笔
    // mPaint 绘制刻度
    // textPaint 表盘文字画笔
    // cursorPaint 指针画笔
    private Paint mPaint, textPaint, cursorPaint;
    private Context mContext;

    // 屏幕宽高
    private int screenWidth, screenHeight;

    // 屏幕密度
    private float mDensityDpi;

    // 仪表盘圆弧的半径
    private float arcRadius;
    private float defaultArcRadius;

    // 刻度半径
    private float scaleRadius;
    private float defaultScaleRadius;

    // 圆弧线宽
    private float arcLineWidth;
    private float defaultArcLineWidth;

    // 刻度线宽
    private float scaleLineWidth;
    private float defaultScaleLineWidth;

    // 圆心
    private int pointX, pointY;

    // 分数
    private int score;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // 当前分数
    private int currentScore;

    public int getCurrentScore() {
        return currentScore;
    }

    private void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    // 总分
    private int targetScore;

    public int getTargetScore() {
        return targetScore;
    }

    public void setTargetScore(int targetScore) {
        this.targetScore = targetScore;
    }

    // 指针bitmap
    private Bitmap bitmap;

    private float[] pos;
    private float[] tan;
    private Matrix matrix;

    // 百分比
    private float percent;

    private int arcGradientStart, arcGradientMiddle, arcGradientEnd;
    private int defaultArcGradientStart = getResources().getColor(R.color.arcGradientStart);
    // 圆环和刻度默认无中间渐变色，需要的话在xml加上 app:scaleGradientMiddle="颜色"
    private int defaultArcGradientMiddle = 0x00000000;
    private int defaultArcGradientEnd = getResources().getColor(R.color.arcGradientStart);
    private int defaultUnreachedColor = Color.WHITE;

    private int scaleGradientStart, scaleGradientMiddle, scaleGradientEnd;
    private int unreachedColor;

    // 外切矩形
    private RectF scoreRectF, scaleRectF;

    private int scoreTextSize;
    private int defaultScoreTextSize;

    // 开始重绘
    private boolean start = true;

    public void setStart(boolean start) {
        this.start = start;
    }

    public Dashboard(Context context) {
        this(context, null);
        init(context);
    }

    public Dashboard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public Dashboard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Dashboard);
        arcRadius = typedArray.getFloat(R.styleable.Dashboard_arcRadius, defaultArcRadius);
        scaleRadius = typedArray.getFloat(R.styleable.Dashboard_scaleRadius, defaultScaleRadius);
        arcLineWidth = typedArray.getFloat(R.styleable.Dashboard_arcLineWidth, defaultArcLineWidth);
        scaleLineWidth = typedArray.getFloat(R.styleable.Dashboard_scaleLineWidth, defaultScaleLineWidth);
        targetScore = typedArray.getInteger(R.styleable.Dashboard_targetScore, 100);
        arcGradientStart = typedArray.getColor(R.styleable.Dashboard_arcGradientStart, defaultArcGradientStart);
        arcGradientMiddle = typedArray.getColor(R.styleable.Dashboard_arcGradientMiddle, defaultArcGradientMiddle);
        arcGradientEnd = typedArray.getColor(R.styleable.Dashboard_arcGradientEnd, defaultArcGradientEnd);
        unreachedColor = typedArray.getColor(R.styleable.Dashboard_unreachedColor, defaultUnreachedColor);
        scaleGradientStart = typedArray.getColor(R.styleable.Dashboard_scaleGradientStart, defaultArcGradientStart);
        scaleGradientMiddle = typedArray.getColor(R.styleable.Dashboard_scaleGradientMiddle, defaultArcGradientMiddle);
        scaleGradientEnd = typedArray.getColor(R.styleable.Dashboard_scaleGradientEnd, defaultArcGradientEnd);
        scoreTextSize = typedArray.getDimensionPixelSize(R.styleable.Dashboard_scoreTextSize, defaultScoreTextSize);
        typedArray.recycle();

        // 外圈圆环的外切矩形
        scoreRectF = new RectF(
                pointX - arcRadius,
                pointY - arcRadius,
                pointX + arcRadius,
                pointY + arcRadius);

        // 刻度的外切矩形
        scaleRectF = new RectF(
                pointX - scaleRadius,
                pointY - scaleRadius,
                pointX + scaleRadius,
                pointY + scaleRadius);

    }

    private void init(Context context) {
        mContext = context;
        // 获取屏幕宽高 和 屏幕密度dpi
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        // 像素密度相对于标准320的比例
        mDensityDpi = displayMetrics.densityDpi / 320;
        Log.i("screenWidth", String.valueOf(screenWidth));
        Log.i("screenHeight", String.valueOf(screenHeight));
        Log.i("mDensityDpi", String.valueOf(mDensityDpi));

        // 初始化默认圆弧半径
        defaultArcRadius = screenWidth / 3;
        // 初始化默认刻度半径
        defaultScaleRadius = defaultArcRadius - 45 * mDensityDpi;

        // 圆心位于中心点
        pointX = pointY = screenWidth / 2;

        // 开启硬件加速
        //setLayerType(LAYER_TYPE_SOFTWARE, null);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mPaint.setAntiAlias(true);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //textPaint.setAntiAlias(true);

        cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //cursorPaint.setAntiAlias(true);

        defaultArcLineWidth = 4 * mDensityDpi;
        defaultScaleLineWidth = 30 * mDensityDpi;

        defaultScoreTextSize = 120;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制外层圆
        drawCicle(canvas);
        // 绘制文字
        drawText(canvas);
        // 绘制指针
        drawCursor(canvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(widthMeasureSpec));
    }

    // 当wrap_content的时候，view的大小根据半径大小改变，但最大不会超过屏幕
    private int measure(int measureSpec) {
        int result;
        //1、先获取测量模式 和 测量大小
        //2、如果测量模式是MatchParent 或者精确值，则宽为测量的宽
        //3、如果测量模式是WrapContent ，则宽为 直径值 与 测量宽中的较小值；否则当直径大于测量宽时，会绘制到屏幕之外；
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (arcRadius * 2 + mPaint.getStrokeWidth() * 2);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }


    /**
     * 绘制外层圆
     */
    private void drawCicle(Canvas canvas) {
        canvas.save();
        // 外圈2个圆
        mPaint.setStyle(Paint.Style.STROKE);
        // 设置空心线宽
        mPaint.setStrokeWidth(arcLineWidth);

        int[] arccColors;
        if (scaleGradientMiddle == 0x00000000) {
            arccColors = new int[]{arcGradientStart, arcGradientEnd};
        } else {
            arccColors = new int[]{arcGradientStart, arcGradientMiddle, arcGradientEnd};
        }
        SweepGradient arcGradient = new SweepGradient(pointX, pointY, arccColors, null);
        mPaint.setShader(arcGradient);

        // 将画布逆时针旋转90°
        canvas.rotate(-90, pointX, pointY);
        // 外圈渐变圆环
        canvas.drawArc(scoreRectF, 0, 360, false, mPaint);

        // 内圈渐变刻度
        canvas.rotate(180, pointX, pointY);
        mPaint.setStrokeWidth(scaleLineWidth);

        int[] scaleColors;
        if (scaleGradientMiddle == 0x00000000) {
            scaleColors = new int[]{scaleGradientStart, scaleGradientEnd};
        } else {
            scaleColors = new int[]{scaleGradientStart, scaleGradientMiddle, scaleGradientEnd};
        }
        SweepGradient scaleGradient = new SweepGradient(pointX, pointY, scaleColors, null);
        mPaint.setShader(scaleGradient);

        percent = getScore() / 100f;
        //Log.i("percent", percent + "");
        for (int i = 0; i < percent * 300; i++) {
            if (i % 2 == 0) {
                canvas.drawArc(scaleRectF, i + 30, 1.0f, false, mPaint);
                /*if (currentScore < targetScore) {
                    //当前百分比+1
                    currentScore++;
                }*/
            }
        }
        for (int i = 0; i < 300 - percent * 300; i++) {
            if (i % 2 == 0) {
                mPaint.setShader(null);
                mPaint.setColor(unreachedColor);
                canvas.drawArc(scaleRectF, i + 30 + percent * 300, 1.0f, false, mPaint);
            }
        }
        canvas.restore();
    }

    // 设置分数 并重绘视图
    public void setScoreText(int score) {
        this.score = score;
        if (score >= 90 && score <= 100) {
            textPaint.setColor(Color.GREEN);
        } else if (score >= 60 && score < 90) {
            textPaint.setColor(Color.WHITE);
        } else
            textPaint.setColor(Color.RED);
        postInvalidate();
    }

    /**
     * 绘制文字
     */
    private void drawText(Canvas canvas) {
        canvas.save();
        setScoreText(getScore());
        textPaint.setTextSize(scoreTextSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        // 基线到字体上边框的距离
        float top = fontMetrics.top;
        // 基线到字体下边框的距离
        float bottom = fontMetrics.bottom;
        int baseLineY = (int) (scoreRectF.centerY() - top / 2 - bottom / 2);
        canvas.drawText(String.valueOf(getScore()), scoreRectF.centerX(), baseLineY, textPaint);
        float scoreLength = textPaint.measureText(String.valueOf(getScore()));
        //Log.i("scoreLength", scoreLength + "");
        textPaint.setColor(getResources().getColor(R.color.arcGradientEnd));
        textPaint.setTextSize(40 * mDensityDpi);
        canvas.drawText("分", scoreRectF.centerX() + scoreLength / 2 + 25 * mDensityDpi, baseLineY, textPaint);
        textPaint.setColor(getResources().getColor(R.color.arcGradientStart));
        textPaint.setTextSize(45 * mDensityDpi);
        canvas.drawText("今日分数", scoreRectF.centerX(), scoreRectF.centerY() - (Math.abs(top) + Math.abs(bottom)) / 2 - 25 * mDensityDpi, textPaint);
        canvas.restore();
    }

    /**
     * 绘制指针
     */
    private void drawCursor(Canvas canvas) {
        canvas.save();
        canvas.rotate(90, pointX, pointY);
        cursorPaint.setStyle(Paint.Style.FILL);
        cursorPaint.setStrokeWidth(mDensityDpi);
        cursorPaint.setColor(getResources().getColor(R.color.arcGradientEnd));

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);
        bitmap = setBitmapSize(bitmap);
        // 此时画布顺时针旋转了90度，指针对着正右方，离开始旋转的线偏60度
        bitmap = rotateBitmap(bitmap, percent * 300 - 60);

        pos = new float[2];
        tan = new float[2];
        matrix = new Matrix();

        Path path = new Path();
        path.addArc(scoreRectF, 30, percent * 300);
        PathMeasure pathMeasure = new PathMeasure(path, false);
        pathMeasure.getPosTan(pathMeasure.getLength(), pos, tan);
        matrix.reset();
        matrix.postTranslate(pos[0] - bitmap.getWidth() / 2, pos[1] - bitmap.getHeight() / 2);
        //canvas.drawPath(path, cursorPaint);
        //canvas.rotate(30, pointX, pointY);
        canvas.drawBitmap(bitmap, matrix, cursorPaint);
        //cursorPaint.setColor(getResources().getColor(R.color.arcGradientEnd));
        //canvas.drawCircle(pos[0], pos[1], arcRadius, cursorPaint);
        canvas.restore();
    }

    private Bitmap setBitmapSize(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = (float) ((2 * 30 * mDensityDpi * Math.tan(Math.PI / 6)) / width);
        float scaleHeight = ((float) 60 * mDensityDpi) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, float rotate) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }
}
