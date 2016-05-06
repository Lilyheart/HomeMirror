package com.morristaedt.mirror;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.morristaedt.mirror.configuration.ConfigurationSettings;

import io.fabric.sdk.android.Fabric;

/**
 * Created by HannahMitt on 8/22/15.
 *
 */
public class MirrorApplication extends Application {

    /**
     * The onCreate method runs as soon as the activity is called
     */

    @Override
    public void onCreate() {
        super.onCreate();
        if (!ConfigurationSettings.isDebugBuild()) {
            Fabric.with(this, new Crashlytics());
        }
    }
}
