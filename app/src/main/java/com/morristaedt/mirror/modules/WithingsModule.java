package com.morristaedt.mirror.modules;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    /** WithingsListener is called by android framework to bring up the Withings information
     *
     */
    public interface WithingsListener {
        void onNewAlert(String WithingsAlert);
    }

    /**
     * The getWithingsDisplayInfo method returns the the most recent measurements.
     * @param withings_userid withings userID
     * @param withings_con_key Consumer key, provided by withings when registering as a partner.
     * @param withings_sig  OAuth signature
     * @param withings_sig_method OAuth signature method
     * @param withings_oauthtoken OAuth token for user authorization
     * @param withings_oauthvers OAuth version
     * @param withingsListener withings view Listener
     */
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

                    //Declare new variables to hold possible measurements.
                    //Initialized to -99 which will indicate if they get new values
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
                    int hoursAgo = 0;
                    int daysAgo = 0;

                    final int oauth_nonce;
                    Random randNum = new Random();
                    oauth_nonce = randNum.nextInt(100000);

                    // Convert current time into UNIX epoch
                    long OAUTHTimeStamp = System.currentTimeMillis() / 1000L;

                    // Build URL to access the Withings health information
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

                    // Create new JsonParson object
                    JsonParser jp = new JsonParser();

                    // Converts the input stream to a json element
                    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));

                    // converts the json element to a json object
                    JsonObject rootObj = root.getAsJsonObject();

                    // Takes the JsonObject and get the json item with the body key,
                    // convert it an object and then get the json item with the measuregrps key as an Array
                    JsonArray measureGroups = rootObj.get("body").getAsJsonObject().get("measuregrps").getAsJsonArray();

                    // The For loop steps through the measureGroups JsonArray
                    for (int j = 0; j < measureGroups.size(); j++)
                    {

                        // Gets the index item of the array - adding the .getAsJsonObject() we save it directly into another JsonObject
                        JsonObject nextMeasureGroup =  measureGroups.get(j).getAsJsonObject();

                        // Gathers the time stamp from last entry as an Int
                        mostRecentWeighInEpoch = nextMeasureGroup.get("date").getAsInt();

                        // gets the json item with the measures key as an array
                        JsonArray measures =  nextMeasureGroup.get("measures").getAsJsonArray();

                        // The inner for loop goes through the measures array to obtain the measures values
                        for (int i = 0; i < measures.size(); i++)
                        {
                            // Again gets the index item of the Array and saves it directly as an object
                            JsonObject next = measures.get(i).getAsJsonObject();
                            // Grabs the "Value" and uses Math.pow to translates it to it's appropriate SI value.
                            double value = next.get("value").getAsLong() * Math.pow(10, next.get("unit").getAsLong());

                            // This gathers the type of measure and stores it as an Int
                            // Withings uses and integer key to indicate which measurement is being utilized.
                            switch(next.get("type").getAsInt())
                            {
                                case  1: //Weight (kg) - multiply by 2.2046 to get lbs.
                                    if (weight == -99) {
                                        weight = (value * 2.2046);
                                        //Calculate Easy to read how long ago hoursAgo daysAgo
                                        if ((OAUTHTimeStamp - mostRecentWeighInEpoch) <= 1*24*60*60) // Less than 1 day in seconds
                                        {
                                            hoursAgo = (int)(OAUTHTimeStamp - mostRecentWeighInEpoch) / 60 / 60;
                                        }
                                        else // More then a day
                                        {
                                            daysAgo = (int)(OAUTHTimeStamp - mostRecentWeighInEpoch) / 60 / 60 / 24;
                                        }
                                    }
                                    break;
                                case  4: //Height (meter)
                                    if (height == -99) height = value;
                                    break;
                                case  5: //Fat Free Mass (kg) - multiply by 2.2046 to get lbs.
                                    if (leanMass == -99) leanMass = (value * 2.2046);
                                    break;
                                case  6: //Fat Ratio (%)
                                    if (fatRatio == -99) fatRatio = value;
                                    break;
                                case  8: //Fat Mass Weight (kg) - multiply by 2.2046 to get lbs.
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

                    // Once the information is received and stored it is displayed for the user to see
                    withingsDisplayInfo = String.format("You weigh: %.1f lbs\n" + "Your body fat percentage: %.1f%%\n" +
                            "Your last weight in was: \n" +
                            ((hoursAgo > 0) ? hoursAgo + " hour"+((hoursAgo > 1) ? "s" : "" )+" ago." :  daysAgo + " day"+((daysAgo > 1) ? "s" : "" )+" ago."), weight, fatRatio);

                    // returns the string above
                    return withingsDisplayInfo;
                } catch (Exception err) {
                    Log.d(TAG, "getWithingsDI thrown: " + err);
                    return null;
                }
            }
        }.execute();
    }
}