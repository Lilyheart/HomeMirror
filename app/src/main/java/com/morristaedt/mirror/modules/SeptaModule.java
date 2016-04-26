package com.morristaedt.mirror.modules;

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

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by Lee on 4/24/2016.
 *
 */
public class SeptaModule {

    private final String TAG = "SeptaModule";
    private String routeID;                     //TODO Assigned in settings
    private String textAlert;                   // Single alert message
    private String nextFiveArrivals;            // Next five arrivals

    public interface SeptaListener {
        void onShouldGivePositiveAffirmation(String affirmation);
    }

    public SeptaModule() {
//TODO        this.routeID = routeID;
//        this.textAlert = textAlert;
//        this.nextFiveArrivals = nextFiveArrivals;
    }

    public String getSingleAlert() {
        try {
            Document doc = Jsoup.connect("http://www.septa.org/realtime/alert.html").get();
            this.textAlert = doc.text();
        } catch (Exception err) {
            Log.d(TAG, "Single Alert");
        }
        return textAlert;
    }
}