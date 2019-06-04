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

import de.k3b.util.TempFileUtil;

/** For all different workflows CropAreaXxxxActivity:
 * * Displays the cropping gui
 * * Contains Protected helpers for common functionalities */
abstract class CropAreasChooseBaseActivity extends BaseActivity  {
    protected static final String TAG = "LLCrop";

    private static int lastInstanceNo4Debug = 0;
    private int instanceNo4Debug = 0;

    protected static final int REQUEST_GET_PICTURE = 1;
    protected static final int REQUEST_GET_PICTURE_PERMISSION = 101;

    private static final String CURRENT_CROP_AREA = "CURRENT_CROP_AREA";
    protected static final String IMAGE_JPEG_MIME = "image/jpeg";

    protected CropImageView uCropView = null;
    private ImageProcessor mSpectrum;

    // #7: workaround rotation change while picker is open causes Activity re-create without
    // uCropView recreation completed.
    private Rect mLastCropRect = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instanceNo4Debug = ++lastInstanceNo4Debug;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        uCropView = findViewById(R.id.ucrop);

        mSpectrum = new ImageProcessor();

    }

    protected void SetImageUriAndLastCropArea(Uri uri, Bundle savedInstanceState) {
        final Rect crop = (Rect) ((savedInstanceState == null)
                ? null
                : savedInstanceState.getParcelable(CURRENT_CROP_AREA));

        SetImageUriAndLastCropArea(uri, crop);
    }

    protected void SetImageUriAndLastCropArea(Uri uri, Rect crop) {
        try {

            /*
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            uCropView.setImageBitmap(bitmap);
            */
            uCropView.setImageUriAsync(uri);

            setCropRect(crop);

        } catch (Exception e) {
            final String msg = getInstanceNo4Debug() + "SetImageUriAndLastCropArea '" + uri + "' ";
            Log.e(TAG, msg, e);
            Toast.makeText(this, msg + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected Uri getSourceImageUri(Intent intent) {
        if (intent == null) return null;

        Uri uri = intent.getData();
        return uri;
    }

    private void setCropRect(final Rect crop) {
        if (crop != null) {
            // #7: workaround rotation change while picker is open causes Activity re-create without
            // uCropView recreation completed.
            mLastCropRect = crop;

            uCropView.setCropRect(crop);

            uCropView.setOnSetImageUriCompleteListener(new CropImageView.OnSetImageUriCompleteListener() {
                @Override
                public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
                    // called when uCropView recreation is completed.
                    uCropView.setCropRect(crop);
                    Rect newCrop = getCropRect();
                    Log.d(TAG, getInstanceNo4Debug() + "delayed onCreate(): crop=" + crop + "/" + newCrop);
                    uCropView.setOnSetImageUriCompleteListener(null);
                }
            });
        }
    }

    protected Rect getCropRect() {
        if (uCropView == null) {
            Log.e(TAG, getInstanceNo4Debug() + "ups: no cropView");
            return null;
        }
        final Rect cropRect = uCropView.getCropRect();

        // #7: workaround rotation change while picker is open causes Activity re-create without
        // uCropView recreation completed.
        return (cropRect != null) ? cropRect : mLastCropRect;
    }

    protected String getInstanceNo4Debug() {
        return "#" + instanceNo4Debug + ":";
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Rect crop = getCropRect();
        Log.d(TAG, getInstanceNo4Debug() + "onSaveInstanceState : crop=" + crop);
        outState.putParcelable(CURRENT_CROP_AREA, crop);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send:
                return createSendIntend(true);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
*/

    protected void pickFromGallery(int REQUEST_GET_PICTURE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_GET_PICTURE_PERMISSION);
        } else {
            Log.d(TAG, getInstanceNo4Debug() + "Opening Image Picker");
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
            Log.e(TAG, getInstanceNo4Debug() + "err cannot convert uri to string('" + outUri.toString() + "').", e);
            return outUri.toString();
        }
    }

    protected void close(Closeable stream, Object source) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.w(TAG, getInstanceNo4Debug() + "Error closing " + source, e);
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

    protected void crop(InputStream inStream, OutputStream outStream, Rect rect) throws IOException {
        this.mSpectrum.crop(inStream, outStream, rect, 0);
    }

    protected File getSharedDir() {
        File sharedDir = new File(this.getFilesDir(), "shared");
        sharedDir.mkdirs();

        // #11: remove unused temporary crops from send/get_content after some time.
        TempFileUtil.removeOldTempFiles(sharedDir, System.currentTimeMillis());
        return sharedDir;
    }

    protected String createCropFileName() {
        Uri inUri = getSourceImageUri(getIntent());
        String originalFileName = (inUri == null) ? "" : inUri.getLastPathSegment();
        originalFileName = TempFileUtil.getLastPath(originalFileName);
        return replaceExtension(originalFileName, TempFileUtil.TEMP_FILE_SUFFIX);
    }

    /** replaceExtension("/path/to/image.jpg", ".xmp") becomes "/path/to/image.xmp" */
    private static String replaceExtension(String path, String extension) {
        if (path == null) return null;
        int ext = path.lastIndexOf(".");
        return ((ext >= 0) ? path.substring(0, ext) : path) + extension;
    }

    /** crops current jpg to new temp file and return a FileProvider-shareUri for it or null if error. */
    protected Uri cropToSharedUri() {
        Uri outUri = null;

        final Uri inUri = getSourceImageUri(getIntent());

        File outFile = new File(getSharedDir(), createCropFileName());

        if (inUri != null) {
            Rect rect = getCropRect();
            InputStream inStream = null;
            OutputStream outStream = null;

            final String context_message = getInstanceNo4Debug() + "Cropping '" + inUri + "'(" + rect + ") => '"
                    + outFile.getName() + " ";
            Log.i(TAG, context_message);

            try {
                inStream = getContentResolver().openInputStream(inUri);
                outStream = new FileOutputStream(outFile, false);
                crop(inStream, outStream, rect);

                outUri = FileProvider.getUriForFile(this, "de.k3b.LLCrop", outFile);

            } catch (Exception e) {
                // #14: delete affected file as it is useless
                close(outStream, outStream);
                outFile.delete();
                Log.e(TAG, "Error " + context_message + "(" + outUri +") => " + e.getMessage(), e);
                Toast.makeText(this,
                        getString(R.string.toast_saved_error, outFile.getAbsolutePath(), e.getMessage()),
                        Toast.LENGTH_LONG).show();
            } finally {
                close(outStream, outStream);
                close(inStream, inStream);
            }
        } else {
            Log.e(TAG, getInstanceNo4Debug() + "Error cropToSharedUri(): Missing input uri.");
        }
        return outUri;
    }
}
