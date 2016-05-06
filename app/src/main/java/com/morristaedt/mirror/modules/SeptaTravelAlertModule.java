package com.morristaedt.mirror.modules;

import android.os.AsyncTask;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by Lee on 4/24/2016.
 */
public class SeptaTravelAlertModule {

    private static String textAlert;            //Alert information from Septa

    /** SeptaListener is called by android framework to bring up the Septa information
     *
     */
    public interface SeptaListener {
        void onNewAlert(String SeptaAlert);
    }

    /**
     * The getTravelAlert method return the current travel alert
     * @param septaListener updates the SeptaAlert view
     */
    public static void getTravelAlert(final SeptaListener septaListener) {
        final String TAG = "SeptaAsyncModule"; //Use to log error message

        //Start UI thread for background operations
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                septaListener.onNewAlert(s);
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    //Uses Jsoup to connect to septa and pulls text off webpage
                    Document doc = Jsoup.connect("http://www.septa.org/realtime/alert.html").get();
                    //Coverts the document to string
                    textAlert = doc.text();
                    //Removes generic text from text
                    textAlert = textAlert.replace("For current updates on all routes go to System Status.", "");
                    textAlert = textAlert.replace(
                            "This section will contain information on unanticipated service interruptions.",
                            "No current travel alerts at this time!");
                    return textAlert;
                } catch (Exception err) {
                    Log.d(TAG, "Single Alert exception thrown: " + err);
                    return null;
                }
            }
        }.execute();
    }
}