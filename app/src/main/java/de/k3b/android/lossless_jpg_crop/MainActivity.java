package de.k3b.android.lossless_jpg_crop;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.yalantis.ucrop.model.ImageState;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;
import com.yalantis.ucrop.view.UCropView;

import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends BaseActivity  {
    private static final String TAG = "ResultActivity";
    private static final int REQUEST_GET_PICTURE = 1;
    protected static final int REQUEST_GET_PICTURE_PERMISSION = 101;

    private static final int REQUEST_SAVE_PICTURE = 2;
    private static final int REQUEST_SAVE_PICTURE_PERMISSION = 102;

    private GestureCropImageView mImageView;
    private OverlayView mCropView;

    private ImageProcessor mSpectrum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpectrum = new ImageProcessor();

        Uri uri = getIntent().getData();

        if (uri == null) {
            // must be called with image uri
            pickFromGallery();
        } else {
            try {
                UCropView uCropView = findViewById(R.id.ucrop);

                mImageView = uCropView.getCropImageView();

                mImageView.setImageUri(uri, null);
                mImageView.setRotateEnabled(false);

                mCropView = uCropView.getOverlayView();
                mCropView.setShowCropFrame(true);
                mCropView.setShowCropGrid(true);
                mCropView.setDimmedColor(Color.TRANSPARENT);
                mCropView.setFreestyleCropMode(OverlayView.FREESTYLE_CROP_MODE_ENABLE);

            } catch (Exception e) {
                Log.e(TAG, "setImageUri", e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        /*
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(getIntent().getData().getPath()).getAbsolutePath(), options);
        */

        final ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.app_name)); // , options.outWidth, options.outHeight));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            saveCroppedImage();
/*
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
*/
        }
        return super.onOptionsItemSelected(item);
    }

    private void pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_GET_PICTURE_PERMISSION);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    .setType("image/jpeg")
                    .addCategory(Intent.CATEGORY_OPENABLE);

            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_GET_PICTURE);
        }
    }
    private void onGetPictureResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            final Uri selectedUri = data.getData();
            if (selectedUri != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, selectedUri, this, MainActivity.class);
                this.startActivity(intent);
                finish();
                return;
            }
        }
        Toast.makeText(this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
        finish();
        return;
    }

    private void saveCroppedImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    getString(R.string.permission_write_storage_rationale),
                    REQUEST_SAVE_PICTURE_PERMISSION);
        } else {
            Uri imageUri = getIntent().getData();

            /**
            !!!! mImageView.getCurrentCropImageState() scale must be fixed according to image with/hight/orientation
                    see BitmapLoadTask.resize() for new details
                    curent workaround: load visible image with full bitmap instead of uri
             **/
            debug(imageUri, mImageView.getRelativeCroppingRectangleF());
            /*
            if (imageUri != null && imageUri.getScheme().equals("file")) {
                try {
                    copyFileToDownloads(getIntent().getData());
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, imageUri.toString(), e);
                }
            } else {
                Toast.makeText(this, getString(R.string.toast_unexpected_error), Toast.LENGTH_SHORT).show();
            }
            */
        }
    }

    private void debug(Uri imageUri, RectF currentCropImageState) {
        Log.d(TAG, imageUri.getLastPathSegment() + "(" + currentCropImageState + ")");
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GET_PICTURE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                }
                break;
            case REQUEST_SAVE_PICTURE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveCroppedImage();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GET_PICTURE) {
            onGetPictureResult(resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    //!!! Todo call cropping lib
    private void crop() throws RuntimeException {
        InputStream inputStream = null;
        OutputStream outputStream = null;


        int left=0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        int degrees = 0;
        mSpectrum.crop(inputStream, outputStream, left, top, right, bottom, degrees);
    }


}
