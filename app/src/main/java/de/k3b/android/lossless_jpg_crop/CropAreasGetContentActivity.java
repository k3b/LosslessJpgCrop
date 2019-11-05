package de.k3b.android.lossless_jpg_crop;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;

/**
 * Handles ACTION_GET_CONTENT and ACTION_PICK to pick a cropped image
 *
 * #3: GET_CONTENT => LLCrop => sourcePhoto.jpg=GET_CONTENT(mime=image/jpeg) => return crop(sourcePhoto.jpg)
 */
public class CropAreasGetContentActivity extends CropAreasChooseBaseActivity {
    public CropAreasGetContentActivity() {
        super(R.id.menu_get_content);
    }

    private static final String KEY_SOURCE_IMAGE_URI = "mSourceImageUri";
    private Uri mSourceImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSourceImageUri = savedInstanceState.getParcelable(KEY_SOURCE_IMAGE_URI);
        }

        if (mSourceImageUri == null) {
            content.pickFromGalleryForContent();
        } else {
            SetImageUriAndLastCropArea(mSourceImageUri, savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SOURCE_IMAGE_URI, mSourceImageUri);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_get_content, menu);
        return true;
    }

    /** get uri of image that will be cropped */
    @Override
    protected Uri getSourceImageUri(Intent intent) {
        return mSourceImageUri;
    }

    @Override
    protected void SetImageUriAndLastCropArea(Uri uri, Rect crop) {
        this.mSourceImageUri = uri;
        super.SetImageUriAndLastCropArea(uri, crop);
    }
}
