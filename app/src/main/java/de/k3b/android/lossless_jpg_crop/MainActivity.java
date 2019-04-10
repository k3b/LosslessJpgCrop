package de.k3b.android.lossless_jpg_crop;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;
import com.yalantis.ucrop.view.UCropView;

import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "ResultActivity";

    private ImageProcessor mSpectrum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpectrum = new ImageProcessor();

        Uri uri = getIntent().getData();
        if (uri != null) {
            try {
                UCropView uCropView = findViewById(R.id.ucrop);
                uCropView.getCropImageView().setImageUri(uri, null);
                uCropView.getOverlayView().setShowCropFrame(false);
                uCropView.getOverlayView().setShowCropGrid(false);
                uCropView.getOverlayView().setDimmedColor(Color.TRANSPARENT);

                /* parameters for UCropActivity/UCropFragment not used here
                UCrop.Options options = new UCrop.Options();

                options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                options.setCompressionQuality(80);
                options.setHideBottomControls(true);
                options.setFreeStyleCropEnabled(true);
                options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.SCALE, UCropActivity.SCALE);

                uCropView.withOptions(options);
                */
            } catch (Exception e) {
                Log.e(TAG, "setImageUri", e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        /*
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(getIntent().getData().getPath()).getAbsolutePath(), options);
        */

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.app_name)); // , options.outWidth, options.outHeight));
        }

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
