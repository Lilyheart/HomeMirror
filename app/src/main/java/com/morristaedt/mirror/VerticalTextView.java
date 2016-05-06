package com.morristaedt.mirror;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * The VerticalTextView class rotates the TextView by 90 degrees by extended the TextView class
 * Created by Lilyheart on 4/15/2016.
 * Original code sourced from http://stackoverflow.com/questions/1258275/vertical-rotated-label-in-android
 */

public class VerticalTextView extends TextView {

    /**
     * The VerticalTextView construction class calls it's super.
     * @param context Context class gives access to global information
     * @param attrs A collection of attributes
     */

    public VerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * The onMeasure class extends it's parent class and reverses width and height
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        //Reverse width and height
        //noinspection SuspiciousNameCombination
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    /**
     * The onDraw method draws the TextView on the screen
     * @param canvas holds the draw calls
     */

    @Override
    protected void onDraw(Canvas canvas){
        //Create TextPaint object
        TextPaint textPaint = getPaint();
        //Determine TextPaint object color
        textPaint.setColor(getCurrentTextColor());
        //Determine TextPain object's drawable state
        textPaint.drawableState = getDrawableState();

        //save canvas
        canvas.save();

        //translate the canvas
        canvas.translate(getWidth(), 0);
        //rotate the canvas 90 degrees clockwise
        canvas.rotate(90);

        //reverse padding
        canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());

        //draw canvas with new settings
        getLayout().draw(canvas);
        canvas.restore();
    }
}