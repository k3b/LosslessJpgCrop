package de.k3b.android.lossless_jpg_crop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.InputStream;
import java.io.OutputStream;

public class CropAreasEditActivity extends CropAreasChooseBaseActivity {
    private static final int REQUEST_SAVE_PICTURE = 2;
    private static final int REQUEST_SAVE_PICTURE_PERMISSION = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                return saveAsPublicCroppedImage();
            default:
                return super.onOptionsItemSelected(item);
        }
        // return true;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SAVE_PICTURE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveAsPublicCroppedImage();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SAVE_PICTURE) {
            final Uri outUri = (data == null) ? null : data.getData();
            onOpenPublicOutputUriPickerResult(resultCode, outUri);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean saveAsPublicCroppedImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    getString(R.string.permission_write_storage_rationale),
                    REQUEST_SAVE_PICTURE_PERMISSION);
        } else {
            openPublicOutputUriPicker(REQUEST_SAVE_PICTURE);
        }
        return true;
    }

    private boolean openPublicOutputUriPicker(int folderpickerCode) {
        Uri inUri = getIntent().getData();
        String originalFileName = (inUri == null) ? "" : inUri.getLastPathSegment();
        String proposedFileName = replaceExtension(originalFileName, "_llcrop.jpg");
        // String proposedOutPath = inUri.getP new Uri() replaceExtension(originalFileName, "_llcrop.jpg");


        // DocumentsContract#EXTRA_INITIAL_URI
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType(IMAGE_JPEG_MIME)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_TITLE, proposedFileName)
                .putExtra(DocumentsContract.EXTRA_PROMPT, getString(R.string.label_save_as))
                .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                ;

        Log.d(TAG, getInstanceNo() + "openPublicOutputUriPicker '" + proposedFileName + "'");

        startActivityForResult(intent, folderpickerCode);
        return true;
    }

    private void onOpenPublicOutputUriPickerResult(int resultCode, Uri outUri) {
        final Intent intent = getIntent();
        final Uri inUri = ((resultCode == RESULT_OK) && (intent != null)) ? intent.getData() : null;

        if (inUri != null) {
            Rect rect = getCropRect();
            InputStream inStream = null;
            OutputStream outStream = null;

            final String context_message = getInstanceNo() + "Cropping '" + inUri + "'(" + rect + ") => '"
                    + outUri + "' ('" + toString(outUri) + "')";
            Log.i(TAG, context_message);

            try {
                inStream = getContentResolver().openInputStream(inUri);
                outStream = getContentResolver().openOutputStream(outUri, "w");
                crop(inStream, outStream, rect);

                String message = getString(R.string.toast_saved_as,
                        toString(outUri));
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                finish();
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error " + context_message + e.getMessage(), e);
            } finally {
                close(outStream, outStream);
                close(inStream, inStream);
            }
        } else {
            // uri==null or error
            Log.i(TAG, getInstanceNo() + "onOpenPublicOutputUriPickerResult(null): No output url, not saved.");
        }
    }

    /** replaceExtension("/path/to/image.jpg", ".xmp") becomes "/path/to/image.xmp" */
    private static String replaceExtension(String path, String extension) {
        if (path == null) return null;
        int ext = path.lastIndexOf(".");
        return ((ext >= 0) ? path.substring(0, ext) : path) + extension;
    }
}
