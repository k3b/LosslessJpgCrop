package de.k3b.android.lossless_jpg_crop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.provider.DocumentsContract;
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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** For all different workflows CropAreaXxxxActivity:
 * * Displays the cropping gui
 * * Contains Protected helpers for common functionalities */
abstract class CropAreasChooseBaseActivity extends BaseActivity  {
    private static int lastInstanceNo = 0;
    private int instanceNo = 0;
    protected static final String TAG = "llCrop";

    protected static final int REQUEST_GET_PICTURE = 1;
    protected static final int REQUEST_GET_PICTURE_PERMISSION = 101;

    private static final String CURRENT_CROP_AREA = "CURRENT_CROP_AREA";
    protected static final String IMAGE_JPEG_MIME = "image/jpeg";

    protected CropImageView uCropView = null;
    private ImageProcessor mSpectrum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instanceNo = ++lastInstanceNo;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        uCropView = findViewById(R.id.ucrop);

        mSpectrum = new ImageProcessor();

    }

    protected void SetImageUriAndLastCropArea(Uri uri, Bundle savedInstanceState) {
        try {

            /*
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            uCropView.setImageBitmap(bitmap);
            */
            uCropView.setImageUriAsync(uri);

            final Rect crop = (Rect) ((savedInstanceState == null)
                    ? null
                    : savedInstanceState.getParcelable(CURRENT_CROP_AREA));

            setCropRect(crop);

        } catch (Exception e) {
            final String msg = getInstanceNo() + "setImageUri '" + uri + "' ";
            Log.e(TAG, msg, e);
            Toast.makeText(this, msg + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected Uri getSourceImageUri(Intent intent) {
        if (intent == null) return null;

        Uri uri = intent.getData();
        return uri;
    }

    // #7: workaround rotation change while picker is open causes Activity re-create without
    // uCropView recreation completed.
    private Rect mLastCropRect = null;
    private void setCropRect(final Rect crop) {
        if (crop != null) {
            mLastCropRect = crop;
            uCropView.setCropRect(crop);

            uCropView.setOnSetImageUriCompleteListener(new CropImageView.OnSetImageUriCompleteListener() {
                @Override
                public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
                    // called when uCropView recreation is completed.
                    uCropView.setCropRect(crop);
                    Rect newCrop = getCropRect();
                    Log.d(TAG, getInstanceNo() + "delayed onCreate(): crop=" + crop + "/" + newCrop);
                    uCropView.setOnSetImageUriCompleteListener(null);
                }
            });
        }
    }

    protected Rect getCropRect() {
        if (uCropView == null) {
            Log.e(TAG, getInstanceNo() + "ups: no cropView");
            return null;
        }
        final Rect cropRect = uCropView.getCropRect();
        return (cropRect != null) ? cropRect : mLastCropRect;
    }

    protected String getInstanceNo() {
        return "#" + instanceNo + ":";
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Rect crop = getCropRect();
        Log.d(TAG, getInstanceNo() + "onSaveInstanceState : crop=" + crop);
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

    protected static void copyExtra(Intent outIntent, Bundle extras, String... extraIds) {
        if (extras != null) {
            for (String id : extraIds) {
                String value = extras.getString(id, null);
                if (value != null) outIntent.putExtra(id, value);
            }
        }
    }

    protected void pickFromGallery(int REQUEST_GET_PICTURE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_GET_PICTURE_PERMISSION);
        } else {
            Log.d(TAG, getInstanceNo() + "Opening Image Picker");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    .setType(IMAGE_JPEG_MIME)
                    .putExtra(Intent.EXTRA_TITLE, getString(R.string.label_select_picture))
                    .putExtra(DocumentsContract.EXTRA_PROMPT, getString(R.string.label_select_picture))
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    ;

            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_GET_PICTURE);
        }
    }

    protected String toString(Uri outUri) {
        if (outUri == null) return "";
        // may crash with "IllegalCharsetNameException" in https://github.com/k3b/LosslessJpgCrop/issues/7
        try {
            return URLDecoder.decode(outUri.toString(), StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            //!!! UnsupportedEncodingException, IllegalCharsetNameException
            Log.e(TAG, getInstanceNo() + "err cannot convert uri to string('" + outUri.toString() + "').", e);
            return outUri.toString();
        }
    }

    protected void close(Closeable stream, Object source) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.w(TAG, getInstanceNo() + "Error closing " + source, e);
            }
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GET_PICTURE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery(REQUEST_GET_PICTURE);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Uri __delete_saveAsPrivate() {

        final Uri inUri = getSourceImageUri(getIntent());
        Rect rect = getCropRect();
        InputStream inStream = null;
        OutputStream outStream = null;

        final String context_message = getInstanceNo() + "Cropping '" + inUri + "'(" + rect + ")";
        Log.d(TAG, context_message);

        try {
            final File sharedFolder = getSharedDir();
            final File sharedFile = File.createTempFile("llCrop_",".jpg", sharedFolder);
            sharedFolder.mkdirs();
            outStream = new FileOutputStream(sharedFile);
            inStream = getContentResolver().openInputStream(inUri);
            crop(inStream, outStream, rect);

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

    protected void crop(InputStream inStream, OutputStream outStream, Rect rect) {
        this.mSpectrum.crop(inStream, outStream, rect, 0);
    }

    private Uri createPrivateOutUriOrNull() {
        try {
            final File sharedFolder = getSharedDir();
            final File sharedFile = File.createTempFile("llCrop_",".jpg", sharedFolder);
            sharedFolder.mkdirs();

            Uri sharedFileUri = FileProvider.getUriForFile(this, "de.k3b.llCrop", sharedFile);

            return sharedFileUri;

        } catch (Exception e) {
            Log.e(TAG, getInstanceNo() + e.getMessage(), e);
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

    protected File getSharedDir() {
        File sharedDir = new File(this.getFilesDir(), "shared");
        sharedDir.mkdirs();
        return sharedDir;
    }

    protected String createCropFileName() {
        Uri inUri = getSourceImageUri(getIntent());
        String originalFileName = (inUri == null) ? "" : inUri.getLastPathSegment();
        return replaceExtension(originalFileName, "_llcrop.jpg");
    }

    /** replaceExtension("/path/to/image.jpg", ".xmp") becomes "/path/to/image.xmp" */
    private static String replaceExtension(String path, String extension) {
        if (path == null) return null;
        int ext = path.lastIndexOf(".");
        return ((ext >= 0) ? path.substring(0, ext) : path) + extension;
    }



}
