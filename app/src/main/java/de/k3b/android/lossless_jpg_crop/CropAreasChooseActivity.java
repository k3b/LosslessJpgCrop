package de.k3b.android.lossless_jpg_crop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.realify.lib.androidimagecropper.CropImageView;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CropAreasChooseActivity extends BaseActivity  {
    private static final String TAG = "ResultActivity";
    private static final int REQUEST_GET_PICTURE = 1;
    protected static final int REQUEST_GET_PICTURE_PERMISSION = 101;

    private static final int REQUEST_SAVE_PICTURE = 2;
    private static final int REQUEST_SAVE_PICTURE_PERMISSION = 102;
    private static final String CURRENT_CROP_AREA = "CURRENT_CROP_AREA";
    private static final String IMAGE_JPEG_MIME = "image/jpeg";

    private CropImageView uCropView = null;
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
                uCropView = findViewById(R.id.ucrop);

                /*
                InputStream stream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);

                uCropView.setImageBitmap(bitmap);
                */
                uCropView.setImageUriAsync(uri);

                Rect crop = (Rect) ((savedInstanceState == null)
                        ? null
                        : savedInstanceState.getParcelable(CURRENT_CROP_AREA));

                uCropView.setCropRect(crop);
            } catch (Exception e) {
                Log.e(TAG, "setImageUri", e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Rect crop = getCropRect();
        outState.putParcelable(CURRENT_CROP_AREA, crop);
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
                    .setType(IMAGE_JPEG_MIME)
                    .addCategory(Intent.CATEGORY_OPENABLE);

            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_GET_PICTURE);
        }
    }
    private void onGetPictureResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            final Uri selectedUri = data.getData();
            if (selectedUri != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, selectedUri, this, CropAreasChooseActivity.class);
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

            Rect crop = getCropRect();

            openOutputUriPicker(REQUEST_SAVE_PICTURE);
            // debug(imageUri, crop);

            /**
            !!!! mImageView.getCurrentCropImageState() scale must be fixed according to image with/hight/orientation
                    see BitmapLoadTask.resize() for new details
                    curent workaround: load visible image with full bitmap instead of uri
             **/
            // debug(imageUri, mImageView.getRelativeCroppingRectangleF());
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

    private boolean openOutputUriPicker(int folderpickerCode) {
        Uri inUri = getIntent().getData();
        String originalFileName = (inUri == null) ? "" : inUri.getLastPathSegment();
        String proposedFileName = replaceExtension(originalFileName, "_llcrop.jpg");

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .setType(IMAGE_JPEG_MIME)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .putExtra(Intent.EXTRA_TITLE, proposedFileName)
                            .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                            ;
        startActivityForResult(intent, folderpickerCode);
        return true;
    }

    private void onOpenOutputUriPickerResult(int resultCode, Uri outUri) {

        if (resultCode == RESULT_OK) {
            final Uri inUri = getIntent().getData();
            Rect rect = getCropRect();
            InputStream inStream = null;
            OutputStream outStream = null;

            final String context_message = "Cropping '" + inUri + "'(" + rect + ") => '" + outUri + "'";
            Log.d(TAG, context_message);

            try {
                inStream = getContentResolver().openInputStream(inUri);
                outStream = getContentResolver().openOutputStream(outUri, "w");
                this.mSpectrum.crop(inStream, outStream, rect, 0);
                finish();
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error " + context_message + e.getMessage(), e);
            } finally {
                close(outStream, outStream);
                close(inStream, inStream);
            }
        }
    }

    private static void close(Closeable stream, Object source) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.w(TAG, "Error closing " + source, e);
            }
        }
    }

    /** replaceExtension("/path/to/image.jpg", ".xmp") becomes "/path/to/image.xmp" */
    private static String replaceExtension(String path, String extension) {
        if (path == null) return null;
        int ext = path.lastIndexOf(".");
        return ((ext >= 0) ? path.substring(0, ext) : path) + extension;
    }

    private Rect getCropRect() {
        if (uCropView == null) return null;
        return uCropView.getCropRect();
    }

    private void debug(Uri imageUri, Object currentCropImageState) {
        Log.d(TAG, imageUri.getLastPathSegment() + "(" + currentCropImageState + ")");
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
        if (requestCode == REQUEST_SAVE_PICTURE) {
            onOpenOutputUriPickerResult(resultCode, data.getData());
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
