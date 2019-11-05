package de.k3b.android.lossless_jpg_crop;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

/**
 * #1: ACTION_EDIT(uri=sourcePhoto.jpg) => crop => public-file.jpg
 * #1: ACTION_MAIN => ACTION_EDIT(uri=GetContent(mime=image/jpeg)) => crop => public-file.jpg
 *
 * Handles ACTION_EDIT(uri=DATA) and ACTION_MAIN:
 */

public class CropAreasEditActivity extends CropAreasChooseBaseActivity {
    public CropAreasEditActivity() {
        super(R.id.menu_save);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getSourceImageUri(getIntent());

        if (uri == null) {
            Log.d(TAG, getInstanceNo4Debug() + "Intent.data has not initial image uri. Opening Image Picker");
            // must be called with image uri
            edit.pickFromGalleryForEdit();
        } else {
            SetImageUriAndLastCropArea(uri, savedInstanceState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }
}
