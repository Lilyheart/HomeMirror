package com.morristaedt.mirror.modules;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.lang.StringBuilder;
import java.net.URLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Calendar;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.morristaedt.mirror.configuration.ConfigurationSettings;
import com.morristaedt.mirror.requests.XKCDRequest;
import com.morristaedt.mirror.requests.XKCDResponse;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by Lee on 4/24/2016.
 */
public class SeptaTravelAlertModule {

    private static String textAlert;            //Alert information from Septa
    private String routeID;                     //TODO Assigned in settings
    private String nextFiveArrivals;            // Next five arrivals

    //Updates when new alert received
    public interface SeptaListener {
        void onNewAlert(String SeptaAlert);
    }

    //Returns travel alert information from septa
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