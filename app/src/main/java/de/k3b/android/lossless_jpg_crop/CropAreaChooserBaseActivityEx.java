package de.k3b.android.lossless_jpg_crop;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class CropAreaChooserBaseActivityEx extends CropAreasChooseBaseActivity {
    private final Map<Integer,Integer> menu2Rotation;

    protected CropAreaChooserBaseActivityEx(int idMenuMainMethod) {
        super(idMenuMainMethod);
        menu2Rotation = new HashMap<Integer,Integer>();
        menu2Rotation.put(R.id.menu_rotate_0, 0);
        menu2Rotation.put(R.id.menu_rotate_90, 90);
        menu2Rotation.put(R.id.menu_rotate_180, 180);
        menu2Rotation.put(R.id.menu_rotate_270, 270);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rotate, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for(Integer key : menu2Rotation.keySet()) {
            menu.findItem(key.intValue()).setChecked(getRotation() == menu2Rotation.get(key).intValue());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setRotation(int rotation) {
        if (rotation != getRotation()) {
            super.setRotation(rotation);
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Integer rotation = menu2Rotation.get(item.getItemId());
        if (rotation != null) {
            this.setRotation(rotation.intValue());
            uCropView.setRotatedDegrees(this.getRotation());
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_save:
                return edit.saveAsPublicCroppedImage();
            case R.id.menu_send:
                return sendPrivateCroppedImage();
            case R.id.menu_get_content:
                return returnPrivateCroppedImage();
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
            case Edit.REQUEST_GET_EDIT_PICTURE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    edit.pickFromGalleryForEdit();
                }
                break;
            case Edit.REQUEST_SAVE_EDIT_PICTURE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    edit.saveAsPublicCroppedImage();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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

            final Intent execIntent = Intent.createChooser(childSend, this.getText(R.string.label_send));

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

    private static void copyExtra(Intent outIntent, Bundle extras, String... extraIds) {
        if (extras != null) {
            for (String id : extraIds) {
                String value = extras.getString(id, null);
                if (value != null) outIntent.putExtra(id, value);
            }
        }
    }
    private boolean returnPrivateCroppedImage() {
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
