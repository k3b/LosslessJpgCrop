package de.k3b.android.lossless_jpg_crop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;

/**
 * Handles ACTION_SENDTO(uri=DATA) and ACTION_SEND(uri=EXTRA_STREAM) to re-send a cropped image
 *
 * #2: SEND/SENDTO(uri=sourcePhoto.jpg) => crop => tempfile.jpg => SEND/SENDTO(uri=tempfile.jpg)
 */
public class CropAreasSendActivity extends CropAreasChooseBaseActivity {
    public CropAreasSendActivity() {
        super(R.id.menu_send);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getSourceImageUri(getIntent());

        send.onGetSendImage(uri, savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /** get uri from intent: ACTION_SENDTO(uri=DATA) and ACTION_SEND(uri=EXTRA_STREAM)  */
    @Override
    protected Uri getSourceImageUri(Intent intent) {
        Uri uri = super.getSourceImageUri(intent);

        if ((uri == null) && (intent != null)) {
            Bundle extras = intent.getExtras();
            Object stream = (extras == null) ? null : extras.get(Intent.EXTRA_STREAM);
            if (stream != null) {
                uri = Uri.parse(stream.toString());
            }

        }
        return uri;
    }
}
