package de.k3b.android.lossless_jpg_crop;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * #2: SEND/SENDTO(uri=sourcePhoto.jpg) => crop => tempfile.jpg => SEND/SENDTO(uri=tempfile.jpg)
 *
 * Handles ACTION_SENDTO(uri=DATA) and ACTION_SEND(uri=EXTRA_STREAM):
 */
public class CropAreasSendActivity extends CropAreasChooseBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getSourceImageUri(getIntent());

        //
        /*As of {@link android.os.Build.VERSION_CODES#JELLY_BEAN}, the data
         * being sent can be supplied through {@link #setClipData(ClipData)}.  This
         * allows you to use {@link #FLAG_GRANT_READ_URI_PERMISSION} when sharing
         * content: URIs and other advanced features of {@link ClipData}.  If
         * using this approach, you still must supply the same data through the
         * {@link #EXTRA_TEXT} or {@link #EXTRA_STREAM} fields described below
         * for compatibility with old applications.  */

        SetImageUriAndLastCropArea(uri, savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send:
                return sendPrivateCroppedImage();
            default:
                return super.onOptionsItemSelected(item);
        }
        // return true;
    }

    /** get uri from intent: ACTION_SENDTO(uri=DATA) and ACTION_SEND(uri=EXTRA_STREAM)  */
    @Override
    protected Uri getSourceImageUri(Intent intent) {
        Uri uri = super.getSourceImageUri(intent);

        if ((uri == null) && (intent != null)) {
            Bundle extras = (uri != null) ? null : intent.getExtras();
            Object stream = (extras == null) ? null : extras.get(Intent.EXTRA_STREAM);
            if (stream != null) {
                uri = Uri.parse(stream.toString());
            }

        }
        return uri;
    }

    private boolean sendPrivateCroppedImage() {

        final Intent parentIntent = getIntent();
        final Uri inUri = getSourceImageUri(parentIntent);
        Uri outUri = null;

        File outFile = new File(getSharedDir(), createCropFileName());

        if (inUri != null) {
            Rect rect = getCropRect();
            InputStream inStream = null;
            OutputStream outStream = null;

            final String context_message = getInstanceNo() + "Cropping '" + inUri + "'(" + rect + ") => '"
                    + outFile.getName();
            Log.i(TAG, context_message);

            try {
                inStream = getContentResolver().openInputStream(inUri);
                outStream = new FileOutputStream(outFile, false);
                crop(inStream, outStream, rect);

                outUri = FileProvider.getUriForFile(this, "de.k3b.llCrop", outFile);

            } catch (Exception e) {
                Log.e(TAG, "Error " + context_message + e.getMessage(), e);
            } finally {
                close(outStream, outStream);
                close(inStream, inStream);
            }
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
            copyExtra(childSend, parentIntent.getExtras(),
                    Intent.EXTRA_EMAIL, Intent.EXTRA_CC, Intent.EXTRA_BCC, Intent.EXTRA_SUBJECT,
                    Intent.EXTRA_TEXT);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip = ClipData.newUri(getContentResolver(), outUri.toString(), outUri);
                childSend.setClipData(clip);
            }

            final Intent execIntent = Intent.createChooser(childSend, this.getText(R.string.label_send));

            startActivity(execIntent);

            finish();
            return true;
        } else {
            // uri==null or error
            Log.i(TAG, getInstanceNo() + "onOpenPublicOutputUriPickerResult(null): No output url, not saved.");
        }
        return false;
    }

    private boolean isSendAction() {
        Intent i = getIntent();
        String action = (i != null) ? i.getAction() : null;
        return (action != null) && Intent.ACTION_SEND.equalsIgnoreCase(action);
    }
}
