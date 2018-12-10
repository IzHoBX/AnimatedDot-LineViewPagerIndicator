package com.izho.progressbartest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.viewpagerindicator.CirclePageIndicator;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.widget.LinearLayout.HORIZONTAL;

/**
 * This indicator is written during CALLAPP-11044, where backward scrolling of viewpager is not in scope.
 * Please implement animation for backward scrolling if required.
 */
public class AnimatedDotAndLineViewPagerIndicator extends CirclePageIndicator {
    private ValueAnimator animator;
    private int currentAnimatedValue = 1;
    private int lastAnimatedPosition = 0;
    private ViewPager mViewPager;
    private float separationLineLength = -1;
    private float mPageOffset = 0;
    private float mPrevPageOffset = 0;
    private boolean ignoreScrollOvershot = false;

    private long DOT_ANIMATION_DURATION = 400;

    public AnimatedDotAndLineViewPagerIndicator(Context context) {
        super(context);
    }

    public AnimatedDotAndLineViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedDotAndLineViewPagerIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mViewPager == null) {
            return;
        }
        final int count = mViewPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }

        int mCurrentPage = mViewPager.getCurrentItem();
        if (mCurrentPage >= count) {
            setCurrentItem(count - 1);
            return;
        }

        int longSize;
        int longPaddingBefore;
        int longPaddingAfter;
        int shortPaddingBefore;
        int mOrientation = getOrientation();
        float mRadius = getRadius();
        if (mOrientation == HORIZONTAL) {
            longSize = getWidth();
            longPaddingBefore = getPaddingLeft();
            longPaddingAfter = getPaddingRight();
            shortPaddingBefore = getPaddingTop();
        } else {
            longSize = getHeight();
            longPaddingBefore = getPaddingTop();
            longPaddingAfter = getPaddingBottom();
            shortPaddingBefore = getPaddingLeft();
        }

        //singleUnit = 1 circle + 1 separation line
        final float singleUnitLength = getSeparationLineLength() + 2 * mRadius;
        final float shortOffset = shortPaddingBefore + mRadius;
        //need to add radius because drawing of circles requires center of circle, not beginning x-coordinate
        float longOffset = longPaddingBefore;
        if (isCentered()) {
            longOffset += (longSize - longPaddingBefore - longPaddingAfter - ((count-1) * singleUnitLength + 2 * mRadius))/2.0f;
        }
        float centerOfFirstCircleOffset = longOffset + mRadius;

        float dX;
        float dY = shortOffset;

        Paint mPaintFill = new Paint(ANTI_ALIAS_FLAG);
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(getFillColor());
        mPaintFill.setStrokeWidth(getStrokeWidth());

        Paint mPaintStroke = new Paint(ANTI_ALIAS_FLAG);
        mPaintStroke.setStyle(Paint.Style.STROKE);
        mPaintStroke.setColor(getStrokeColor());
        mPaintStroke.setStrokeWidth(getStrokeWidth());

        //Draw outline
        for (int iLoop = 0; iLoop < count; iLoop++) {
            float drawLong = centerOfFirstCircleOffset + (iLoop * singleUnitLength);
            if (mOrientation == HORIZONTAL) {
                dX = drawLong;
                dY = shortOffset;
            } else {
                dX = shortOffset;
                dY = drawLong;
            }

            if(iLoop < mCurrentPage) {
                canvas.drawCircle(dX, dY, mRadius, mPaintFill);
            } else {
                canvas.drawCircle(dX, dY, mRadius - getStrokeWidth(), mPaintStroke);
            }

            if (iLoop != count - 1) {
                if(iLoop < mCurrentPage) {
                    canvas.drawLine(dX + mRadius, dY, dX + singleUnitLength - mRadius, dY, mPaintFill);
                } else {
                    canvas.drawLine(dX + mRadius, dY, dX + singleUnitLength - mRadius, dY, mPaintStroke);
                }
            }
        }

        //draws the previously animated dot when viewpager is scrolled past it
        if(mCurrentPage == lastAnimatedPosition) {
            canvas.drawCircle(centerOfFirstCircleOffset + mCurrentPage * singleUnitLength, getPaddingTop() + mRadius, mRadius, mPaintFill);
        }

        //dot animation
        if (animator != null && animator.isRunning()) {
            canvas.drawCircle(centerOfFirstCircleOffset + mCurrentPage * singleUnitLength, getPaddingTop() + mRadius, currentAnimatedValue, mPaintFill);
        } else if (currentAnimatedValue == (int) mRadius) {
            //end animation tear down
            canvas.drawCircle(centerOfFirstCircleOffset + mCurrentPage * singleUnitLength, getPaddingTop() + mRadius, mRadius, mPaintFill);
            currentAnimatedValue = 1;
            lastAnimatedPosition = mCurrentPage;
            ignoreScrollOvershot = false;
        }

        if(mPageOffset == 0 && mPrevPageOffset != 0) {
            if(animator == null || !animator.isRunning()) {
                //reached a new page while swiping
                animator = new ValueAnimator().ofInt(1, (int) mRadius);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        currentAnimatedValue = (int) animation.getAnimatedValue();
                        invalidate();
                    }
                });
                animator.setDuration(DOT_ANIMATION_DURATION);
                animator.start();
            }
        } else if(mPageOffset != 0 && !ignoreScrollOvershot) {
            //while scroll
            dX = longOffset + mCurrentPage * singleUnitLength + 2 * mRadius;
            float cx = dX + mPageOffset * (singleUnitLength - mRadius * 2);
            canvas.drawLine(dX, dY, cx, dY, mPaintFill);
        }
        mPrevPageOffset = mPageOffset;
    }

    @Override
    public void setViewPager(ViewPager view) {
        super.setViewPager(view);
        this.mViewPager = view;
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        super.setViewPager(view, initialPosition);
        this.mViewPager = mViewPager;
    }

    public float getSeparationLineLength() {
        if(separationLineLength == -1) {
            return getRadius();
        } else {
            return separationLineLength;
        }
    }

    public void setSeparationLineLength(float separation) {
        if(separation <= 0)
            throw new IllegalArgumentException("separation must be > 0");
        this.separationLineLength = separation;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPageOffset = positionOffset;
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    public void setDotAnimationDuration(long x) {
        DOT_ANIMATION_DURATION = x;
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        ignoreScrollOvershot = true;
    }
}
