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

    /** SeptaListener is called by android framework to bring up the Septa information
     *
     */
    public interface SeptaListener {
        void onNewUpdate(String SeptaUpdate);
    }

    /**
     * The getStationStatus returns the next departure in each direction for the selected station ID
     * @param StationID The septa station ID number or name
     * @param septaListener Brings up the septa view
     */
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

                    JsonParser jp = new JsonParser();
                    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
                    JsonObject rootObj = root.getAsJsonObject();

                    //Convert Array to JsonReader Object to determine exact name of first key
                    JsonReader jreader = new JsonReader(new StringReader(rootObj.toString()));
                    jreader.beginObject(); //Consumes the next token from the JSON stream and asserts that it is the beginning of a new object.
                    String septaTrainHeader = jreader.nextName();


                    //the next train to arrive from both northbound and southbound
                    JsonArray trainDetail = rootObj.getAsJsonArray(septaTrainHeader);
                    JsonObject northbound = trainDetail.get(0).getAsJsonObject().getAsJsonArray("Northbound").get(0).getAsJsonObject();
                    JsonObject southbound = trainDetail.get(1).getAsJsonObject().getAsJsonArray("Southbound").get(0).getAsJsonObject();

                    //replaces certain text in the septaTrainHeader
                    septaTrainHeader = septaTrainHeader.replace(": ", "\n as of ");

                    //gets the depart time of the JSON object
                    String northDepart = northbound.get("depart_time").getAsString();
                    //Cleans up double spaces
                    northDepart = northDepart.replace("  "," ");
                    //using regular expression to remove milliseconds from the time
                    northDepart = northDepart.replaceAll(":[0-9]{2}:[0-9]{3}"," ");
                    //gets the depart time from the json object
                    String southDepart = southbound.get("depart_time").getAsString();
                    //cleans up double spaces
                    southDepart = southDepart.replace("  "," ");
                    //using regular expression to remove milliseconds from the time
                    southDepart = southDepart.replaceAll(":[0-9]{2}:[0-9]{3}"," ");

                    //return user friendly string of text for display
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