package com.folioreader.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.Layout;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.folioreader.R;

/**
 * Created by mobisys on 7/4/2016.
 */
public class UnderlinedTextView extends AppCompatTextView {

    private Rect mRect;
    private Paint mPaint;
    private int mColor;
    private float mDensity;
    private float mStrokeWidth;

    private boolean dotted;
    private boolean underline;
    private Path mPath;
    public UnderlinedTextView(Context context) {
        this(context, null, 0);
    }

    public UnderlinedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UnderlinedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attributeSet, int defStyle) {

        mDensity = context.getResources().getDisplayMetrics().density;

        TypedArray typedArray =
                context.obtainStyledAttributes(attributeSet, R.styleable.UnderlinedTextView,
                        defStyle, 0);
        mStrokeWidth =
                typedArray.getDimension(
                        R.styleable.UnderlinedTextView_underlineWidth,
                        mDensity * 2);
        typedArray.recycle();

        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColor); //line mColor
        mPaint.setStrokeWidth(mStrokeWidth);
        mPath = new Path();
    }

    public int getUnderLineColor() {
        return mColor;
    }

    public void setUnderLineColor(int mColor) {
        this.mColor = mColor;
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColor); //line mColor
        mPaint.setStrokeWidth(mStrokeWidth);
        postInvalidate();
    }

    public float getUnderlineWidth() {
        return mStrokeWidth;
    }

    public void setUnderlineWidth(float mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getLineCount();

        final Layout layout = getLayout();
        float xStart, xStop, xDiff;
        int firstCharInLine, lastCharInLine;
        mPath.reset();
        if(underline && dotted){
            DashPathEffect dashPathEffect = new DashPathEffect(new float[]{5f, 5f}, 0);
            mPaint.setPathEffect(dashPathEffect);
            //换行之后画线
            for (int i = 0; i < getLineCount(); i++) {
                mPath.moveTo(getLayout().getLineLeft(i), getLayout().getLineBottom(i));
                mPath.lineTo(getLayout().getLineRight(i), getLayout().getLineBottom(i));
            }
            if(mStrokeWidth != 0){
                mPaint.setStrokeWidth(mStrokeWidth+2);
            }

            canvas.drawPath(mPath, mPaint);
        }else if(underline){
            for (int i = 0; i < count; i++) {
                int baseline = getLineBounds(i, mRect);
                firstCharInLine = layout.getLineStart(i);
                lastCharInLine = layout.getLineEnd(i);

                xStart = layout.getPrimaryHorizontal(firstCharInLine);
                xDiff = layout.getPrimaryHorizontal(firstCharInLine + 1) - xStart;
                xStop = layout.getPrimaryHorizontal(lastCharInLine - 1) + xDiff;

                canvas.drawLine(xStart,
                        baseline + mStrokeWidth,
                        xStop,
                        baseline + mStrokeWidth,
                        mPaint);
            }
        }





        super.onDraw(canvas);
    }

    public void setDotted(boolean dotted) {
        this.dotted = dotted;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }
}