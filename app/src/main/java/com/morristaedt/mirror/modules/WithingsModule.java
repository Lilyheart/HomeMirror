package com.morristaedt.mirror.modules;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by Lilyheart on 4/30/2016.
 */
public class WithingsModule {

    private final String TAG = "WithingsModule";
    private static String textAlert;
    private String routeID;                     //TODO Assigned in settings
    private String nextFiveArrivals;            // Next five arrivals

    public interface WithingsListener {
        void onNewAlert(String WithingsAlert);
    }

    public static void getSingleAlert(final WithingsListener withingsListener) {
        final String TAG = "WithingsAsyncModule";
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                withingsListener.onNewAlert(s);
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    Document doc = Jsoup.connect("http://www.septa.org/realtime/alert.html").get();
                    textAlert = doc.text();
                    String removeText = "For current updates on all routes go to System Status.";

                    textAlert = textAlert.replace(removeText, "");
                    return textAlert;
                } catch (Exception err) {
                    Log.d(TAG, "Single Alert exception thrown: " + err);
                    return null;
                }
            }
        }.execute();
    }
}