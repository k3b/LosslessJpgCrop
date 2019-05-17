package de.k3b.android.lossless_jpg_crop;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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

            final Intent execIntent = Intent.createChooser(childSend, this.getText(R.string.label_send));

            startActivity(execIntent);

            finish();
            return true;
        }
        return false;
    }

    private boolean isSendAction() {
        Intent i = getIntent();
        String action = (i != null) ? i.getAction() : null;
        return (action != null) && Intent.ACTION_SEND.equalsIgnoreCase(action);
    }

    private static void copyExtra(Intent outIntent, Bundle extras, String... extraIds) {
        if (extras != null) {
            for (String id : extraIds) {
                String value = extras.getString(id, null);
                if (value != null) outIntent.putExtra(id, value);
            }
        }
    }
}
