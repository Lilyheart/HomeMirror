package com.morristaedt.mirror.modules;

import android.os.AsyncTask;
import android.util.Log;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
    public static void getStationStatus(final String StationID, final SeptaListener septaListener) {
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

                    String sURL = "http://www3.septa.org/hackathon/Arrivals/" + StationID + "/1/";    // Connect to the URL
                    URL url = new URL(sURL);

                    //starts and opens a connection request
                    HttpURLConnection request = (HttpURLConnection)url.openConnection();
                    request.connect();

                    JsonParser jp = new JsonParser(); //from gson
                    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
                    JsonObject rootObj = root.getAsJsonObject();

                    //Convert Array to JsonReader Object to determine exact name of first key
                    JsonReader jreader = new JsonReader(new StringReader(rootObj.toString()));
                    jreader.beginObject(); //Consumes the next token from the JSON stream and asserts that it is the beginning of a new object.
                    String septaTrainHeader = jreader.nextName();


                    //JsonObject trainNext = rootObj.get(septaTrainHeader).getAsJsonObject();
                    JsonArray trainDetail = rootObj.getAsJsonArray(septaTrainHeader);
                    JsonObject northbound = trainDetail.get(0).getAsJsonObject().getAsJsonArray("Northbound").get(0).getAsJsonObject();
                    JsonObject southbound = trainDetail.get(1).getAsJsonObject().getAsJsonArray("Southbound").get(0).getAsJsonObject();

                    septaTrainHeader = septaTrainHeader.replace(": ", "\n as of ");

                    String northDepart = northbound.get("depart_time").getAsString();
                    northDepart = northDepart.replace("  "," ");
                    northDepart = northDepart.replaceAll(":[0-9]{2}:[0-9]{3}"," ");
                    String southDepart = southbound.get("depart_time").getAsString();
                    southDepart = southDepart.replace("  "," ");
                    southDepart = southDepart.replaceAll(":[0-9]{2}:[0-9]{3}"," ");

                    return septaTrainHeader + "\n\n" +
                            "Next Northbound\n" +
                            "Leaving: " + northDepart + "\n" +
                            "Delay: " + northbound.get("status").getAsString() + "\n" +
                            "Destination: " + northbound.get("destination").getAsString() + "\n\n" +
                            "Next Southbound\n" +
                            "Leaving: " + southDepart + "\n" +
                            "Delay: " + southbound.get("status").getAsString() + "\n" +
                            "Destination: " + southbound.get("destination").getAsString();

                } catch (Exception err) {
                    Log.d(TAG, "Station status exception thrown: " + err);
                    return null;
                }
            }
        }.execute();
    }
}