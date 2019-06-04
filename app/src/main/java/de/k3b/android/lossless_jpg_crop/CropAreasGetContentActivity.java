package de.k3b.android.lossless_jpg_crop;

import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Handles ACTION_GET_CONTENT and ACTION_PICK to pick a cropped image
 *
 * #3: GET_CONTENT => LLCrop => sourcePhoto.jpg=GET_CONTENT(mime=image/jpeg) => return crop(sourcePhoto.jpg)
 */
public class CropAreasGetContentActivity extends CropAreasChooseBaseActivity {
    private static final String KEY_SOURCE_IMAGE_URI = "mSourceImageUri";
    private Uri mSourceImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSourceImageUri = savedInstanceState.getParcelable(KEY_SOURCE_IMAGE_URI);
        }

        if (mSourceImageUri == null) {
            pickFromGallery(REQUEST_GET_PICTURE);
        } else {
            SetImageUriAndLastCropArea(mSourceImageUri, savedInstanceState);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GET_PICTURE) {
            final Uri inUri = (data == null) ? null : data.getData();
            if ((resultCode == RESULT_OK) && (inUri != null)) {
                SetImageUriAndLastCropArea(inUri, getCropRect());
            } else {
                finish();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SOURCE_IMAGE_URI, mSourceImageUri);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_get_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_get_content:
                return returnPrivateCroppedImage();
            default:
                return super.onOptionsItemSelected(item);
        }
        // return true;
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

    private boolean returnPrivateCroppedImage() {
        Uri outUri = cropToSharedUri();

        if (outUri != null) {
            Intent result = new Intent();
            result.setDataAndType(outUri, IMAGE_JPEG_MIME);
            result.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setResult(Activity.RESULT_OK, result);
            finish();
            return true;
        }
        return false;
    }
}
