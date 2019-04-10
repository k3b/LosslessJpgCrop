package de.k3b.android.lossless_jpg_crop;

import android.app.Application;

public class MainApp extends Application {
    public void onCreate() {
        super.onCreate();
        ImageProcessor.init(this);
    }
}
