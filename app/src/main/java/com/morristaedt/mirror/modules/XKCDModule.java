package com.morristaedt.mirror.modules;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.morristaedt.mirror.configuration.ConfigurationSettings;
import com.morristaedt.mirror.requests.XKCDRequest;
import com.morristaedt.mirror.requests.XKCDResponse;

import java.util.Calendar;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by HannahMitt on 8/22/15.
 *
 */
public class XKCDModule {
    /** XKCDListener is called by android framework to bring up the XKCD comic
     *
     */
    public interface XKCDListener {
        void onNewXKCDToday(String url);
    }

    /**
     * The getXKCDForToday method fetches the the latest XKCD comic
     * @param listener XKCD listener object
     */
    public static void getXKCDForToday(final XKCDListener listener) {
        new AsyncTask<Void, Void, XKCDResponse>() {

            @Override
            protected XKCDResponse doInBackground(Void... params) {
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint("http://xkcd.com")
                        .build();

                XKCDRequest service = restAdapter.create(XKCDRequest.class);
                try {
                    return service.getLatestXKCD();
                } catch (RetrofitError e) {
                    Log.w("XKCDModule", "Error loading xkcd", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(@Nullable XKCDResponse xkcdResponse) {
                if (xkcdResponse != null && !TextUtils.isEmpty(xkcdResponse.img)) {
                    if (ConfigurationSettings.isDemoMode()){
                        listener.onNewXKCDToday(xkcdResponse.img);
                        return;
                    }
                }
                listener.onNewXKCDToday(null);
            }

        }.execute();

    }
}
