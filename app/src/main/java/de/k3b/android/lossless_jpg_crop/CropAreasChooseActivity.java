package de.k3b.android.lossless_jpg_crop;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.realify.lib.androidimagecropper.CropImageView;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Intent.EXTRA_STREAM;

public class CropAreasChooseActivity extends BaseActivity  {
    private static final String TAG = "llCrop";
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
            Log.d(TAG, "Intent.data has not initial image uri. Opening Image Picker");
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
                final String msg = "setImageUri '" + uri + "' ";
                Log.e(TAG, msg, e);
                Toast.makeText(this, msg + e.getMessage(), Toast.LENGTH_LONG).show();
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
        String action = getIntent().getAction();

		/*
        if (Intent.ACTION_GET_CONTENT.compareToIgnoreCase(action) == 0) {
            getMenuInflater().inflate(R.menu.menu_get_content, menu);
        } else if (Intent.ACTION_SEND.compareToIgnoreCase(action) == 0) {
            getMenuInflater().inflate(R.menu.menu_send, menu);
        } else if (Intent.ACTION_SENDTO.compareToIgnoreCase(action) == 0) {
            getMenuInflater().inflate(R.menu.menu_send_to, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_edit, menu);
        }
		*/
		
		getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                return saveAsPublicCroppedImage();
/*				
            case R.id.menu_send: {
                createSendIntend(true);

            }
                reloadContext = false;
                IntentUtil.cmdStartIntent("share", this, null, null, getCurrentFilePath(), Intent.ACTION_SEND, R.string.share_menu_title, R.string.share_err_not_found, 0);
                break;
*/				
            default:
                return super.onOptionsItemSelected(item);
        }
        // return true;
    }

    private void createSendIntend(boolean asExtra) {
        Intent sourceIntent = getIntent();
        Uri uri = createPrivateOutUriOrNull();

        if (uri != null) {
            final Intent outIntent = new Intent(sourceIntent.getAction())
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            copyExtra(outIntent, sourceIntent.getExtras(),
                    Intent.EXTRA_EMAIL, Intent.EXTRA_CC, Intent.EXTRA_BCC, Intent.EXTRA_SUBJECT);

            if (asExtra)
                outIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    .setType(IMAGE_JPEG_MIME);
            else
                outIntent.setDataAndType(uri, IMAGE_JPEG_MIME);
        }
    }

    private static void copyExtra(Intent outIntent, Bundle extras, String... extraIds) {
        for(String id: extraIds) {
            String value = extras.getString(id, null);
            if (value != null) outIntent.putExtra(id, value);
        }
    }

    private void pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_GET_PICTURE_PERMISSION);
        } else {
            Log.d(TAG, "Opening Image Picker");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    .setType(IMAGE_JPEG_MIME)
                    .addCategory(Intent.CATEGORY_OPENABLE);

            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_GET_PICTURE);
        }
    }
    private void onGetPictureResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            final Uri selectedUri = (data == null) ? null : data.getData();
            if (selectedUri != null) {
                Log.d(TAG, "Restarting with uri '" + selectedUri + "'");

                Intent intent = new Intent(Intent.ACTION_VIEW, selectedUri, this, CropAreasChooseActivity.class);
                this.startActivity(intent);
                finish();
                return;
            }
        }
        Log.d(TAG, this.getString(R.string.toast_cannot_retrieve_selected_image));
        Toast.makeText(this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
        finish();
        return;
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

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .setType(IMAGE_JPEG_MIME)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .putExtra(Intent.EXTRA_TITLE, proposedFileName)
                            .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                            ;

        Log.d(TAG, "openPublicOutputUriPicker '" + proposedFileName + "'");

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

            final String context_message = "Cropping '" + inUri + "'(" + rect + ") => '" + outUri + "'";
            Log.i(TAG, context_message);

            try {
                outStream = getContentResolver().openOutputStream(outUri, "w");
                inStream = getContentResolver().openInputStream(inUri);
                this.mSpectrum.crop(inStream, outStream, rect, 0);
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
            Log.i(TAG, "onOpenPublicOutputUriPickerResult(null): No output url, not saved.");
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
                    saveAsPublicCroppedImage();
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
            final Uri outUri = (data == null) ? null : data.getData();
            onOpenPublicOutputUriPickerResult(resultCode, outUri);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private Uri __delete_saveAsPrivate() {

        final Uri inUri = getIntent().getData();
        Rect rect = getCropRect();
        InputStream inStream = null;
        OutputStream outStream = null;

        final String context_message = "Cropping '" + inUri + "'(" + rect + ")";
        Log.d(TAG, context_message);

        try {
            final File sharedFolder = new File(getFilesDir(), "shared");
            final File sharedFile = File.createTempFile("llCrop_",".jpg", sharedFolder);
            sharedFolder.mkdirs();
            outStream = new FileOutputStream(sharedFile);
            inStream = getContentResolver().openInputStream(inUri);
            this.mSpectrum.crop(inStream, outStream, rect, 0);

            Uri sharedFileUri = FileProvider.getUriForFile(this, "de.k3b.llCrop", sharedFile);

            return sharedFileUri;
        } catch (Exception e) {
            Log.e(TAG, "Error " + context_message + e.getMessage(), e);
            return null;
        } finally {
            close(outStream, outStream);
            close(inStream, inStream);
        }

    }

    private Uri createPrivateOutUriOrNull() {
        try {
            final File sharedFolder = new File(getFilesDir(), "shared");
            final File sharedFile = File.createTempFile("llCrop_",".jpg", sharedFolder);
            sharedFolder.mkdirs();

            Uri sharedFileUri = FileProvider.getUriForFile(this, "de.k3b.llCrop", sharedFile);

            return sharedFileUri;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
		
		/* !!!TODO
        try {
            android.support.v4.provider.
            // #64: edit image (not) via chooser
            final Intent execIntent = (idChooserCaption == 0)
                    ? outIntent
                    : Intent.createChooser(outIntent, parentActivity.getText(idChooserCaption));

            ActivityWithCallContext.additionalCallContext = debugContext;
            if (idActivityResultRequestCode == 0) {
                parentActivity.startActivity(execIntent);
            } else {
                parentActivity.startActivityForResult(execIntent, idActivityResultRequestCode);
            }
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(parentActivity, idEditError,Toast.LENGTH_LONG).show();
        }
		*/
    }



}
