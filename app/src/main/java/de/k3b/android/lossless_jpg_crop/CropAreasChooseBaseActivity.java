package de.k3b.android.lossless_jpg_crop;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import net.realify.lib.androidimagecropper.CropImageView;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import de.k3b.util.TempFileUtil;

/**
 * For all different workflows CropAreaXxxxActivity:
 * * Displays the cropping gui
 * * Contains Protected helpers for common functionalities
 */
abstract class CropAreasChooseBaseActivity extends BaseActivity {
    protected static final String TAG = "LLCrop";

    private static final String STATE_CURRENT_CROP_AREA = "CURRENT_CROP_AREA";
    private static final String STATE_CURRENT_ASPECT_RATIO = "KEY_ASPECT_RATIO";

    protected static final boolean LOAD_ASYNC = false;
    private static final boolean ENABLE_ASPECT_RATIO = false;

    private static int lastInstanceNo4Debug = 0;
    private int instanceNo4Debug = 0;

    private final int idMenuMainMethod;
    private final Map<Integer, Integer> menu2Rotation;

    protected static final String IMAGE_JPEG_MIME = "image/jpeg";

    protected CropImageView uCropView = null;
    protected TextView txtStatus = null;
    private ImageProcessor mSpectrum;
    private String aspectRatio = null;

    // #7: workaround rotation change while picker is open causes Activity re-create without
    // uCropView recreation completed.
    private Rect mLastCropRect = null;
    protected CropAreasChooseBaseActivity(int idMenuMainMethod) {
        this.idMenuMainMethod = idMenuMainMethod;
        menu2Rotation = new HashMap<>();
        menu2Rotation.put(R.id.menu_rotate_0, 0);
        menu2Rotation.put(R.id.menu_rotate_90, 90);
        menu2Rotation.put(R.id.menu_rotate_180, 180);
        menu2Rotation.put(R.id.menu_rotate_270, 270);
    }

    protected class Content {
        protected static final int REQUEST_GET_CONTENT_PICTURE = 1;
        protected static final int REQUEST_GET_CONTENT_PICTURE_PERMISSION = 101;

        protected void pickFromGalleryForContent() {
            pickFromGallery(Content.REQUEST_GET_CONTENT_PICTURE, Content.REQUEST_GET_CONTENT_PICTURE_PERMISSION);
        }
        protected boolean returnPrivateCroppedImage() {
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
    protected Content content = new Content();

    protected class Edit {
        protected static final int REQUEST_GET_EDIT_PICTURE = 3;
        protected static final int REQUEST_GET_EDIT_PICTURE_PERMISSION = 103;

        protected static final int REQUEST_SAVE_EDIT_PICTURE_AS = 2;
        protected static final int REQUEST_SAVE_EDIT_PICTURE_PERMISSION = 102;

        protected void pickFromGalleryForEdit() {
            pickFromGallery(Edit.REQUEST_GET_EDIT_PICTURE, Edit.REQUEST_GET_EDIT_PICTURE_PERMISSION);
        }

        protected boolean saveAsPublicCroppedImage() {
            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getString(R.string.permission_write_storage_rationale),
                        Edit.REQUEST_SAVE_EDIT_PICTURE_PERMISSION);
            } else {
                edit.openPublicOutputUriPicker(Edit.REQUEST_SAVE_EDIT_PICTURE_AS);
            }
            return true;
        }

        protected boolean openPublicOutputUriPicker(int folderpickerCode) {
            String proposedFileName = createCropFileName();
            // String proposedOutPath = inUri.getP new Uri() replaceExtension(originalFileName, "_llcrop.jpg");


            // DocumentsContract#EXTRA_INITIAL_URI
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .setType(IMAGE_JPEG_MIME)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .putExtra(Intent.EXTRA_TITLE, proposedFileName)
                    .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                intent.putExtra(DocumentsContract.EXTRA_PROMPT, getString(R.string.label_save_as));
            }

            Log.d(TAG, getInstanceNo4Debug() + "openPublicOutputUriPicker '" + proposedFileName + "'");

            startActivityForResult(intent, folderpickerCode);
            return true;
        }
        protected void onGetEditPictureResult(int resultCode, Intent data) {
            if (resultCode == RESULT_OK) {
                final Uri selectedUri = (data == null) ? null : getSourceImageUri(data);
                if (selectedUri != null) {
                    Log.d(TAG, getInstanceNo4Debug() + "Restarting with uri '" + selectedUri + "'");

                    Intent intent = new Intent(Intent.ACTION_EDIT, selectedUri, getBaseContext(), CropAreasEditActivity.class);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(intent);
                    finishIfMainMethod(R.id.menu_save);
                    return;
                }
            }
            Log.d(TAG, getInstanceNo4Debug() +  getString(R.string.toast_cannot_retrieve_selected_image));
            Toast.makeText(getBaseContext(), R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
            finishIfMainMethod(R.id.menu_save);
        }
        protected void onSaveEditPictureAsOutputUriPickerResult(Uri _outUri) {

            // use to provoke an error to test error handling
            // Uri outUri = Uri.parse(_outUri + "-err");

            Uri outUri = _outUri;

            final Uri inUri = getSourceImageUri(getIntent());

            if (inUri != null) {
                Rect rect = getCropRect();
                InputStream inStream = null;
                OutputStream outStream = null;

                final String context_message = getInstanceNo4Debug() + "Cropping '" + inUri + "'(" + rect + ") => '"
                        + outUri + "' ('" + asString(outUri) + "')";
                Log.i(TAG, context_message);

                try {
                    inStream = getContentResolver().openInputStream(inUri);
                    outStream = getContentResolver().openOutputStream(outUri, "w");
                    crop(inStream, outStream, rect);

                    String message = getString(R.string.toast_saved_as,
                            asString(outUri));
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();

                    finishIfMainMethod(R.id.menu_save);
                    return;
                } catch (Exception e) {
                    close(outStream, outStream);

                    Log.e(TAG, "Error " + context_message + e.getMessage(), e);

                    try {
                        // #14: delete affected file as it is useless
                        DocumentsContract.deleteDocument(getContentResolver(), _outUri);
                    } catch (Exception exDelete) {
                        // ignore if useless file cannot be deleted
                    }

                    Log.e(TAG, "Error " + context_message + "(" + outUri +") => " + e.getMessage(), e);
                    Toast.makeText(getBaseContext(),
                            getString(R.string.toast_saved_error, asString(outUri), e.getMessage()),
                            Toast.LENGTH_LONG).show();

                } finally {
                    close(outStream, outStream);
                    close(inStream, inStream);
                }
            } else {
                // outUri==null or error
                Log.i(TAG, getInstanceNo4Debug() + "onOpenPublicOutputUriPickerResult(null): No output url, not saved.");
            }
        }
    }
    protected Edit edit = new Edit();

    protected class Send {
        protected void onGetSendImage(Uri imageUri, Bundle savedInstanceState) {
            SetImageUriAndLastCropArea(imageUri, savedInstanceState);
        }

        protected boolean sendPrivateCroppedImage() {
            Uri outUri = cropToSharedUri();

            if (outUri != null) {
                boolean isSend = isSendAction();

                Intent childSend = new Intent();

                if (isSend) {
                    childSend
                            .setAction(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_STREAM, outUri)
                            .setType(IMAGE_JPEG_MIME);
                } else {
                    childSend
                            .setAction(Intent.ACTION_SENDTO)
                            .setDataAndType(outUri, IMAGE_JPEG_MIME);
                }

                childSend.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                copyExtra(childSend, getIntent().getExtras(),
                        Intent.EXTRA_EMAIL, Intent.EXTRA_CC, Intent.EXTRA_BCC, Intent.EXTRA_SUBJECT,
                        Intent.EXTRA_TEXT);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = ClipData.newUri(getContentResolver(), outUri.toString(), outUri);
                    childSend.setClipData(clip);
                }

                final Intent execIntent = Intent.createChooser(childSend, getText(R.string.label_send));

                startActivity(execIntent);

                finishIfMainMethod(R.id.menu_send);
                return true;
            }
            return false;
        }

        private boolean isSendAction() {
            Intent i = getIntent();
            String action = (i != null) ? i.getAction() : null;
            return (action != null) && Intent.ACTION_SEND.equalsIgnoreCase(action);
        }

        private void copyExtra(Intent outIntent, Bundle extras, String... extraIds) {
            if (extras != null) {
                for (String id : extraIds) {
                    String value = extras.getString(id, null);
                    if (value != null) outIntent.putExtra(id, value);
                }
            }
        }
    }
    protected Send send = new Send();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instanceNo4Debug = ++lastInstanceNo4Debug;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        uCropView = findViewById(R.id.ucrop);
        txtStatus = findViewById(R.id.status);

        if (savedInstanceState != null) {
            aspectRatio = savedInstanceState.getString(STATE_CURRENT_ASPECT_RATIO, aspectRatio);
        }
        mSpectrum = new ImageProcessor();

        uCropView.setOnSetCropOverlayMovedListener(new CropImageView.OnSetCropOverlayMovedListener() {
            @Override
            public void onCropOverlayMoved(Rect rect) {
                onUpdateCropping();
            }
        });

        setAspectRatio(aspectRatio);
    }

    private void setAspectRatio(String aspectRatio) {
        String[] xy = (aspectRatio == null) ? null : aspectRatio.split("x");

        if (xy == null) {
            uCropView.clearAspectRatio();
        } else {
            try {
                int x = Integer.parseInt(xy[0]);
                int y = Integer.parseInt(xy[1]);
                uCropView.setAspectRatio(x, y);
                if (x >= 100 && y >= 100) {
                    uCropView.setMinCropResultSize(x,y);
                    uCropView.setMaxCropResultSize(x,y);
                } else {
                    uCropView.setMinCropResultSize(40,99999);
                    uCropView.setMaxCropResultSize(40,99999);
                }
            } catch (Exception ex) {
                String message = "setAspectRatio('" + aspectRatio + "') . Valid example '7x13'";
                Log.e(TAG, getInstanceNo4Debug() + message);

                // throw new IllegalArgumentException(message, ex);
            }
        }
    }

    private void onUpdateCropping() {
        String cropInfo = toString(getCropRect());
        txtStatus.setText(cropInfo);
        Log.d(TAG, getInstanceNo4Debug() + "onUpdateCropping(crop=" + cropInfo + ")");
    }

    public String toString(Rect r) {
        StringBuilder sb = new StringBuilder();

                sb
                .append('(').append(r.left).append(',').append(r.top).append(") .. (")
                .append(r.right).append(',').append(r.bottom).append(") => [")
                        .append(r.width()).append('x').append(r.height()).append(']');
        return sb.toString();
    }

    protected void finishIfMainMethod(int idMenuMainMethod) {
        if (this.idMenuMainMethod == idMenuMainMethod) {
            finish();
        }
    }

    protected void SetImageUriAndLastCropArea(Uri imageUri, Bundle savedInstanceState) {
        final Rect crop = (Rect) ((savedInstanceState == null)
                ? null
                : savedInstanceState.getParcelable(STATE_CURRENT_CROP_AREA));

        SetImageUriAndLastCropArea(imageUri, crop);
    }

    protected void SetImageUriAndLastCropArea(Uri imageUri, Rect crop) {
        try {
            if (LOAD_ASYNC) {
                uCropView.setImageUriAsync(imageUri);
            } else {
                InputStream stream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                ExifInterface exif = getExif(this, imageUri);
                uCropView.setImageBitmap(bitmap, exif);
                if (exif != null) {
                    setBaseRotation(exif.getRotationDegrees());
                }
            }
            setCropRect(crop);
            

        } catch (Exception e) {
            final String msg = getInstanceNo4Debug() + "SetImageUriAndLastCropArea '" + imageUri + "' ";
            Log.e(TAG, msg, e);
            Toast.makeText(this, msg + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static ExifInterface getExif(Context context, Uri imageUri) {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(imageUri);
            if (is != null) {
                ExifInterface ei = new ExifInterface(is);
                is.close();
                return ei;
            }
        } catch (Exception ignored) {
        } finally {
            saveClose(is);
        }
        return null;
    }

    private static void saveClose(Closeable is) {
        if (is != null) {
            try {
                is.close();
            } catch (Exception ignore) {
            }
        }
    }

    protected Uri getSourceImageUri(Intent intent) {
        if (intent == null) return null;

        return intent.getData();
    }

    private void setCropRect(final Rect crop) {
        if (crop != null) {
            // #7: workaround rotation change while picker is open causes Activity re-create without
            // uCropView recreation completed.
            mLastCropRect = crop;

            uCropView.setCropRect(crop);

            if (LOAD_ASYNC) {
                uCropView.setOnSetImageUriCompleteListener(new CropImageView.OnSetImageUriCompleteListener() {
                    @Override
                    public void onSetImageUriComplete(CropImageView view, Uri imageUri, Exception error) {
                        // called when uCropView recreation is completed.
                        uCropView.setCropRect(crop);
                        Rect newCrop = getCropRect();
                        Log.d(TAG, getInstanceNo4Debug() + "delayed onCreate(): crop=" + crop + "/" + newCrop);
                        uCropView.setOnSetImageUriCompleteListener(null);

                        setBaseRotation(uCropView.getRotatedDegrees());
                    }
                });
            }
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
        outState.putParcelable(STATE_CURRENT_CROP_AREA, crop);
        outState.putString(STATE_CURRENT_ASPECT_RATIO, aspectRatio);

    }

    private void pickFromGallery(int requestId, int requestPermissionId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    requestPermissionId);
        } else {
            Log.d(TAG, getInstanceNo4Debug() + "Opening Image Picker");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    .setType(IMAGE_JPEG_MIME)
                    .putExtra(Intent.EXTRA_TITLE, getString(R.string.label_select_picture))
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    ;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                intent.putExtra(DocumentsContract.EXTRA_PROMPT, getString(R.string.label_select_picture));
            }

            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), requestId);
        }
    }

    protected String asString(Uri outUri) {
        if (outUri == null) return "";
        // may crash with "IllegalCharsetNameException" in https://github.com/k3b/LosslessJpgCrop/issues/7
        try {
            return URLDecoder.decode(outUri.toString(), StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            //!!! UnsupportedEncodingException, IllegalCharsetNameException
            Log.e(TAG, getInstanceNo4Debug() + "err cannot convert imageUri to string('" + outUri.toString() + "').", e);
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
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case Content.REQUEST_GET_CONTENT_PICTURE_PERMISSION:
                    content.pickFromGalleryForContent();
                    return;
                case Edit.REQUEST_GET_EDIT_PICTURE_PERMISSION:
                    edit.pickFromGalleryForEdit();
                    return;
                case Edit.REQUEST_SAVE_EDIT_PICTURE_PERMISSION:
                    edit.saveAsPublicCroppedImage();
                    return;
            }
        } else {
            Log.i(TAG, this.getClass().getSimpleName()
                    + ": " + getText(R.string.permission_error));
            Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
            setResult(Activity.RESULT_CANCELED, null);
            finish();

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Edit.REQUEST_GET_EDIT_PICTURE) {
            edit.onGetEditPictureResult(resultCode, data);
            return;
        }

        if (requestCode == Edit.REQUEST_SAVE_EDIT_PICTURE_AS) {
            if (resultCode == RESULT_OK) {
                final Uri outUri = (data == null) ? null : data.getData();
                edit.onSaveEditPictureAsOutputUriPickerResult(outUri);
            } else finishIfMainMethod(R.id.menu_save);
            return;
        }

        if (requestCode == Content.REQUEST_GET_CONTENT_PICTURE) {
            final Uri inUri = (data == null) ? null : data.getData();
            if ((resultCode == RESULT_OK) && (inUri != null)) {
                SetImageUriAndLastCropArea(inUri, getCropRect());
            } else {
                finishIfMainMethod(R.id.menu_get_content);
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void crop(InputStream inStream, OutputStream outStream, Rect rect) throws IOException {
        int relaqtiverRotation = (360 + getRotation() - getBaseRotation()) % 360;
        this.mSpectrum.crop(inStream, outStream, rect, relaqtiverRotation);
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
            Log.e(TAG, getInstanceNo4Debug() + "Error cropToSharedUri(): Missing input imageUri.");
        }
        return outUri;
    }

    private int baseRotation = 0;
    public void setBaseRotation(int baseRotation) {
        this.baseRotation = baseRotation % 360;
        setRotation(this.baseRotation);
    }

    public int getBaseRotation() {
        return baseRotation;
    }

    private int rotation = 0;

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        if (rotation != getRotation()) {
            this.rotation = rotation;
            invalidateOptionsMenu();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rotate, menu);
        if (ENABLE_ASPECT_RATIO) {
            getMenuInflater().inflate(R.menu.menu_aspect_ratio, menu);
        }

/*
    Format 3:4:
            9x11,10x13,11x15,13x17,20x27,30x40
    Format 2:3:
            9x13,10x15,11x17,13x18,20x30,30x45
    Format 9:16:
            9x15,10x18
*/

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for(Integer key : menu2Rotation.keySet()) {
            if (key != null) {
                menu.findItem(key).setChecked(getRotation() == menu2Rotation.get(key));
            }
        }
        if (ENABLE_ASPECT_RATIO) {
            SubMenu menuCrop = menu.findItem(R.menu.menu_aspect_ratio).getSubMenu();
            for (int i = menuCrop.size() - 1; i >= 0; i--) {
                MenuItem item = menuCrop.getItem(i);
                item.setCheckable(true);
                item.setChecked(isCheckedAspectRatio(item));
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private boolean isCheckedAspectRatio(MenuItem item) {
        // !!! ENABLE_ASPECT_RATIO
        return item.getItemId() == Menu.NONE && item.getTitle() != null;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();
        Integer rotation = menu2Rotation.get(menuItemId);
        if (rotation != null) {
            this.setRotation(rotation);
            uCropView.setRotatedDegrees(this.getRotation());
            return true;
        }
        if (menuItemId == R.id.menu_save) {
            return edit.saveAsPublicCroppedImage();
        } else if (menuItemId == R.id.menu_send) {
            return send.sendPrivateCroppedImage();
        } else if (menuItemId == R.id.menu_get_content) {
            return content.returnPrivateCroppedImage();
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
