package com.morristaedt.mirror.modules;

import android.os.AsyncTask;
import android.util.Log;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by Lee on 4/24/2016.
 */
public class SeptaStationStatusModule {

    private static String nextArrivals;            // Next five arrivals

    //Updates when new station update received
    public interface SeptaListener {
        void onNewUpdate(String SeptaUpdate);
    }

    //Returns station status information from septa
    public static void getStationStatus(final SeptaListener septaListener) {
        final String TAG = "SeptaAsyncModule"; //Use to log error message

        //Start UI thread for background operations
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                septaListener.onNewUpdate(s);
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    //Uses Jsoup to connect to septa and pulls text off webpage
                    Document doc = Jsoup.connect("http://www.septa.org/realtime/alert.html").get();
                    //Coverts the document to string
                    nextArrivals = doc.text();
                    //Removes generic text from text
                    nextArrivals = nextArrivals.replace("For current updates on all routes go to System Status.", "");
                    nextArrivals = nextArrivals.replace(
                            "This section will contain information on unanticipated service interruptions.",
                            "No current travel alerts at this time!");
                    return "Station \n" + nextArrivals;
                } catch (Exception err) {
                    Log.d(TAG, "Station status exception thrown: " + err);
                    return null;
                }
            }
        }.execute();
    }
}