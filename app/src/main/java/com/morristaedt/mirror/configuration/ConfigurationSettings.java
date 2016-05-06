package com.morristaedt.mirror.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.morristaedt.mirror.BuildConfig;
import com.morristaedt.mirror.requests.ForecastRequest;

/**
 * ConfigurationSettings.java saves configuration items input into initial screen and is
 * used by other parts of the program.
 * Created by HannahMitt on 9/26/15.
 * Updated by Lilyheart.
 */
public class ConfigurationSettings {

    /*
     * Hardcode on to enable features outside of their regularly scheduled hours
     */
    private static final boolean DEMO_MODE = false;

    private static final String PREFS_MIRROR = "MirrorPrefs";

    private static final String FORECAST_UNITS = "forecast_units";
    private static final String SHOW_CALENDAR = "show_calendar";
    private static final String SHOW_XKCD = "xkcd";
    private static final String INVERT_XKCD = "invert_xkcd";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String STATION = "station";    // Septa station ID

    @NonNull
    private SharedPreferences mSharedPrefs;

    private String mForecastUnits;

    private boolean mShowNextCalendarEvent;
    private boolean mShowXKCD;
    private boolean mInvertXKCD;

    private String mLatitude;
    private String mLongitude;

    private String mStation;

    /**
     * The construction method
     * @param context
     */

    public ConfigurationSettings(Context context) {
        mSharedPrefs = context.getSharedPreferences(PREFS_MIRROR, Context.MODE_PRIVATE);
        readPrefs();
    }

    /**
     *  The readPrefs method reads the user input and stores them for future access.
     */
    private void readPrefs() {
        mForecastUnits = mSharedPrefs.getString(FORECAST_UNITS, ForecastRequest.UNITS_US);
        mShowNextCalendarEvent = mSharedPrefs.getBoolean(SHOW_CALENDAR, false);
        mShowXKCD = mSharedPrefs.getBoolean(SHOW_XKCD, false);
        mInvertXKCD = mSharedPrefs.getBoolean(INVERT_XKCD, false);

        mLatitude = mSharedPrefs.getString(LAT, "");
        mLongitude = mSharedPrefs.getString(LON, "");

        mStation = mSharedPrefs.getString(STATION, "");

    }

    /**
     * The setIsCelsius method sets a SharedPreference based on the isCelsius boolean
     * @param isCelsius
     */

    public void setIsCelsius(boolean isCelsius) {
        //Saves variable as SharedPreference
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        //Set the String value in the preferences editor
        editor.putString(FORECAST_UNITS, isCelsius ? ForecastRequest.UNITS_SI : ForecastRequest.UNITS_US);
        //Commit the changes
        editor.apply();
    }

    /**
     * The setShowNextCalendarEvent indicates if the user has selected to show the next calendar event.
     * @param show boolean that indicates true if the calendar should be shown
     */

    public void setShowNextCalendarEvent(boolean show) {
        //Store entered text in variable
        mShowNextCalendarEvent = show;
        //Saves variable as SharedPreference
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        //Set the boolean value in the preferences editor
        editor.putBoolean(SHOW_CALENDAR, show);
        //Commit the changes
        editor.apply();
    }

    /**
     * The setXKCDPreference indicates if the user has selected to show the next XKCD and if the colors are to be inverted.
     * @param showXKCD
     * @param invertXKCDColors
     */

    public void setXKCDPreference(boolean showXKCD, boolean invertXKCDColors) {
        //Store entered text in variable
        mShowXKCD = showXKCD;
        mInvertXKCD = invertXKCDColors;
        //Saves variable as SharedPreference
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        //Set the String value in the preferences editor
        editor.putBoolean(SHOW_XKCD, showXKCD);
        editor.putBoolean(INVERT_XKCD, invertXKCDColors);
        //Commit the changes
        editor.apply();
    }

    /**
     * The setLatLon stores the latitude and longitude.
     * @param latitude
     * @param longitude
     */

    public void setLatLon(String latitude, String longitude) {
        //Store entered text in variable
        mLatitude = latitude.trim();
        mLongitude = longitude.trim();

        //Saves variable as SharedPreference
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        //Set the String value in the preferences editor
        editor.putString(LAT, mLatitude);
        editor.putString(LON, mLongitude);
        //Commit the changes
        editor.apply();
    }

    /**
     * The setStation method saves the station id entered by the user.
     * @param station Station ID Number.
     */

    public void setStation(String station) {
        //Store entered text in variable
        mStation = station.trim();
        //Saves variable as SharedPreference
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        //Set the String value in the preferences editor
        editor.putString(STATION, mStation);
        //Commit the changes
        editor.apply();
    }

    /**
     * The getIsCelsius method returns a boolean indicating if celsius is selected
     * @return true if the units should be celsius
     */

    public boolean getIsCelsius() {
        return ForecastRequest.UNITS_SI.equals(mForecastUnits);
    }

    /**
     * The getForecastUnits method returns the forecast units.
     * @return the forecast unit
     */

    public String getForecastUnits() {
        return mForecastUnits;
    }

    /**
     * The showNextCalendarEvent returns the next calendar event.
     * @return true if the next calendar event should be shown.
     */

    public boolean showNextCalendarEvent() {
        return mShowNextCalendarEvent;
    }

    /**
     * The showXKCD method returns true if XKCD should be shown.
     * @return true if XKCD should be shown.
     */

    public boolean showXKCD() {
        return mShowXKCD;
    }

    /**
     * the invertXKCD method returns true if XKCD colors should be inverted.
     * @return true if the comic's colors should be inverted.
     */

    public boolean invertXKCD() {
        return mInvertXKCD;
    }

    /**
     * The getLatitude method returns the latitude value.
     * @return the latitude value.
     */

    public String getLatitude() {
        return mLatitude;
    }

    /**
     * The getLongitude method returns the longitude value.
     * @return the longitude value.
     */

    public String getLongitude() {
        return mLongitude;
    }

    /**
     * the getStation method returns the Septa Station ID.
     * @return the septa station ID.
     */

    public String getStation() {
        return mStation;
    }

    /**
     * the isDebugBuild returns true if the build is a debug build.
     * @return true if the build is a debug build.
     */

    public static boolean isDebugBuild() {
        return BuildConfig.DEBUG;
    }

    /**
     * The idDemoMode returns true if the app is in demo mode.
     * @return true if the app is in demo mode.
     */

    public static boolean isDemoMode() {
        return DEMO_MODE;
    }
}
