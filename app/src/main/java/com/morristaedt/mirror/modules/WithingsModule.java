package com.morristaedt.mirror.modules;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Random;

/**
 * Created by Lilyheart on 4/30/2016.
 */
public class WithingsModule {

    private static String withingsDisplayInfo;

    public interface WithingsListener {
        void onNewAlert(String WithingsAlert);
    }

    public static void getWithingsDisplayInfo(final String withings_userid, final String withings_con_key, final String withings_sig, final String withings_sig_method, final String withings_oauthtoken, final String withings_oauthvers, final WithingsListener withingsListener) {
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

                    double weight = -99;
                    double height = -99;
                    double leanMass = -99;
                    double fatRatio = -99;
                    double fatMass = -99;
                    double diastolic = -99;
                    double systolic = -99;
                    double pulse = -99;
                    double sp02Percent = -99;
                    int mostRecentWeighInEpoch;
                    Date mostRecentWeighInDate = new Date(0);
                    Date currMeasureGroupDate;
                    int hoursAgo = 0;
                    int daysAgo = 0;

                    final int oauth_nonce;
                    Random randNum = new Random();
                    oauth_nonce = randNum.nextInt(100000);

                    long OAUTHTimeStamp = System.currentTimeMillis() / 1000L;

                    String wURL = "https://wbsapi.withings.net/measure?action=getmeas&userid="+withings_userid+
                            "&lastupdate=1459468800&oauth_consumer_key="+withings_con_key+
                            "&oauth_nonce="+oauth_nonce+"&oauth_signature="+withings_sig+
                            "&oauth_signature_method="+withings_sig_method+
                            "&oauth_timestamp="+OAUTHTimeStamp+
                            "&oauth_token="+withings_oauthtoken+
                            "&oauth_version="+withings_oauthvers;

                    // Connect to the URL
                    URL url = new URL(wURL);

                    // Starts and opens a connection request
                    HttpURLConnection request = (HttpURLConnection)url.openConnection();
                    request.connect();

                    // Parse the input stream into a json element using Gson library
                    JsonParser jp = new JsonParser();

                    // TODO Caitlyn comment
                    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));

                    // TODO Caitlyn comment
                    JsonObject rootObj = root.getAsJsonObject();

                    // TODO Caitlyn comment
                    JsonArray measureGroups = rootObj.get("body").getAsJsonObject().get("measuregrps").getAsJsonArray();

                    // TODO Caitlyn comment
                    for (int j = 0; j < measureGroups.size(); j++)
                    {

                        // TODO Caitlyn comment
                        JsonObject nextMeasureGroup =  measureGroups.get(j).getAsJsonObject();

                        // TODO Caitlyn comment
                        mostRecentWeighInEpoch = nextMeasureGroup.get("date").getAsInt();
                        currMeasureGroupDate = new Date((nextMeasureGroup.get("date").getAsLong() * 1000L));

                        // TODO Caitlyn comment
                        JsonArray measures =  nextMeasureGroup.get("measures").getAsJsonArray();

                        // TODO Caitlyn comment
                        for (int i = 0; i < measures.size(); i++)
                        {
                            // TODO Caitlyn comment
                            JsonObject next = measures.get(i).getAsJsonObject();
                            // TODO Caitlyn comment
                            double value = next.get("value").getAsLong() * Math.pow(10, next.get("unit").getAsLong());

                            // TODO Caitlyn comment
                            switch(next.get("type").getAsInt())
                            {
                                case  1: //Weight (kg)
                                    if (weight == -99) {
                                        weight = (value * 2.2046);
                                        mostRecentWeighInDate = currMeasureGroupDate;
                                        //Calculate Easy to read how long ago hoursAgo daysAgo
                                        if ((OAUTHTimeStamp - mostRecentWeighInEpoch) <= 1*24*60*60) // Less than 1 day in seconds
                                        {
                                            hoursAgo = (int)(OAUTHTimeStamp - mostRecentWeighInEpoch) / 60 / 60;
                                        }
                                        else
                                        {
                                            daysAgo = (int)(OAUTHTimeStamp - mostRecentWeighInEpoch) / 60 / 60 / 24;
                                        }
                                    }
                                    break;
                                case  4: //Height (meter)
                                    if (height == -99) height = value;
                                    break;
                                case  5: //Fat Free Mass (kg)
                                    if (leanMass == -99) leanMass = (value * 2.2046);
                                    break;
                                case  6: //Fat Ratio (%)
                                    if (fatRatio == -99) fatRatio = value;
                                    break;
                                case  8: //Fat Mass Weight (kg)
                                    if (fatMass == -99) fatMass = (value * 2.2046);
                                    break;
                                case  9: //Diastolic Blood Pressure (mmHg)
                                    if (diastolic == -99) diastolic = value;
                                    break;
                                case 10: //Systolic Blood Pressure (mmHg)
                                    if (systolic == -99) systolic = value;
                                    break;
                                case 11: //Heart Pulse (bpm)
                                    if (pulse == -99) pulse = value;
                                    break;
                                case 54: //SP02(%)
                                    if (sp02Percent == -99) sp02Percent = value;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    // TODO Caitlyn comment
                    withingsDisplayInfo = String.format("You weigh: %.1f lbs\n" + "Your body fat percentage: %.1f%%\n" +
                            "Your last weight in was: \n" +
                            ((hoursAgo > 0) ? hoursAgo + " hour"+((hoursAgo > 1) ? "s" : "" )+" ago." :  daysAgo + " day"+((daysAgo > 1) ? "s" : "" )+" ago."), weight, fatRatio);


                    return withingsDisplayInfo;
                } catch (Exception err) {
                    Log.d(TAG, "getWDI thrown: " + err);
                    return null;
                }
            }
        }.execute();
    }
}