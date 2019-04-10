package de.k3b.android.lossless_jpg_crop;

import android.content.Context;
import android.util.Log;

import com.facebook.spectrum.Configuration;
import com.facebook.spectrum.EncodedImageSink;
import com.facebook.spectrum.EncodedImageSource;
import com.facebook.spectrum.Spectrum;
import com.facebook.spectrum.SpectrumException;
import com.facebook.spectrum.SpectrumSoLoader;
import com.facebook.spectrum.image.EncodedImageFormat;
import com.facebook.spectrum.logging.SpectrumLogcatLogger;
import com.facebook.spectrum.options.TranscodeOptions;
import com.facebook.spectrum.plugins.SpectrumPluginJpeg;
import com.facebook.spectrum.requirements.EncodeRequirement;

import java.io.InputStream;
import java.io.OutputStream;

public class ImageProcessor {
    private Spectrum mSpectrum;

    public static void init(Context context) {
        SpectrumSoLoader.init(context);
    }

    public void ImageProcessor() {
        mSpectrum = Spectrum.make(
                new SpectrumLogcatLogger(Log.INFO),
                Configuration.makeEmpty(),
                SpectrumPluginJpeg.get()); // JPEG only
        // DefaultPlugins.get()); // JPEG, PNG and WebP plugins
    }

    public void crop(InputStream inputStream, OutputStream outputStream, int left, int top, int right, int bottom, int degrees) {
        final EncodeRequirement encoding =
                new EncodeRequirement(EncodedImageFormat.JPEG, 80, EncodeRequirement.Mode.LOSSLESS);
        try {
            mSpectrum.transcode(
                    EncodedImageSource.from(inputStream),
                    EncodedImageSink.from(outputStream),
                    TranscodeOptions
                            .Builder(encoding)
                            .cropAbsoluteToOrigin(left, top, right, bottom, false)

                            // forceUpOrientation=true
                            .rotate(degrees, false, false, true)
                            .build(),
                    "my_callsite_identifier");
        } catch (Exception ex) {
            throw new RuntimeException("Cannot Transcode from " + inputStream + " to " + outputStream + " : " + ex.getMessage(), ex);
        }
    }
}
