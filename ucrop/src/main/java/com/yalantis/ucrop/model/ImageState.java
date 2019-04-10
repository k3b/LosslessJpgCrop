package com.yalantis.ucrop.model;

import android.graphics.RectF;

/**
 * Created by Oleksii Shliama [https://github.com/shliama] on 6/21/16.
 */
public class ImageState {

    /** current cropping rectangle relative to mCurrentImageRect */
    private RectF mCropRect;

    /** In case of zoom-in: which part of the original image is currently visible */
    private RectF mCurrentImageRect;

    private float mCurrentScale, mCurrentAngle;

    public ImageState(RectF cropRect, RectF currentImageRect, float currentScale, float currentAngle) {
        mCropRect = cropRect;
        mCurrentImageRect = currentImageRect;
        mCurrentScale = currentScale;
        mCurrentAngle = currentAngle;
    }

    public RectF getCropRect() {
        return mCropRect;
    }

    public RectF getCurrentImageRect() {
        return mCurrentImageRect;
    }

    public float getCurrentScale() {
        return mCurrentScale;
    }

    public float getCurrentAngle() {
        return mCurrentAngle;
    }

    public int getCroppedImageHeight() {
        return Math.round(mCropRect.height() / mCurrentScale);
    }

    public int getCroppedImageWidth() {
        return Math.round(mCropRect.width() / mCurrentScale);
    }

    public int getCropOffsetY() {
        return Math.round((mCropRect.top - mCurrentImageRect.top) / mCurrentScale);
    }

    public int getCropOffsetX() {
        return Math.round((mCropRect.left - mCurrentImageRect.left) / mCurrentScale);
    }
}
