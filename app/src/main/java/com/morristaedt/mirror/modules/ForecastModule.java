package com.morristaedt.mirror.modules;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.morristaedt.mirror.requests.ForecastRequest;
import com.morristaedt.mirror.requests.ForecastResponse;
import com.morristaedt.mirror.requests.OpenWeatherRequest;
import com.morristaedt.mirror.requests.OpenWeatherResponse;

import java.util.Locale;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by HannahMitt on 8/22/15.
 *
 */
public class ForecastModule {

    public interface ForecastListener {
        void onWeatherToday(String weatherToday);
    }

    /**
     * The getForecastIOHourlyForecast method returns the current hourly forecast if the Forecast.io key was provided.
     * @param apiKey allows android to access the forecast IO API
     * @param units the unit of measurement for the weather
     * @param lat the latitude of your location
     * @param lon the longitude of your location
     * @param listener Listener object to bring up the Forecast View
     */
    public static void getForecastIOHourlyForecast(final String apiKey, final String units, final String lat, final String lon, final ForecastListener listener) {
        new AsyncTask<Void, Void, ForecastResponse>() {

            @Override
            protected ForecastResponse doInBackground(Void... params) {
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint("https://api.forecast.io")
                        .build();

                ForecastRequest service = restAdapter.create(ForecastRequest.class);
                String excludes = "minutely,daily,flags";

                try {
                    return service.getHourlyForecast(apiKey, lat, lon, excludes, units, Locale.getDefault().getLanguage());
                } catch (RetrofitError error) {
                    Log.w("ForecastModule", "Forecast error: " + error.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ForecastResponse forecastResponse) {
                if (forecastResponse != null) {
                    if (forecastResponse.currently != null) {
                        listener.onWeatherToday(forecastResponse.currently.getDisplayTemperature() + " " + forecastResponse.currently.summary);
                    }

                }
            }

        }.execute();

    }

    /**
     * The getOpenWeatherForecast method returns the current hourly forecast if the Forecast.io key was provided.
     * @param apiKey allows android to access the forecast IO API
     * @param units the unit of measurement for the weather
     * @param lat the latitude of your location
     * @param lon the longitude of your location
     * @param listener Listener object to bring up the Forecast View
     */
    public static void getOpenWeatherForecast(final String apiKey, final String units, final String lat, final String lon, final ForecastListener listener) {
        new AsyncTask<Void, Void, OpenWeatherResponse>() {

            @Override
            protected OpenWeatherResponse doInBackground(Void... params) {
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint("http://api.openweathermap.org")
                        .build();

                OpenWeatherRequest service = restAdapter.create(OpenWeatherRequest.class);

                try {
                    return service.getCurrentForecast(apiKey, lat, lon, getOpenWeatherUnits(units), Locale.getDefault().getLanguage());
                } catch (RetrofitError error) {
                    Log.w("ForecastModule", "Forecast error: " + error.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(OpenWeatherResponse response) {
                if (response != null) {
                    if (response.main != null) {
                        listener.onWeatherToday(response.main.getDisplayTemperature() + " " + response.getWeatherDescription());
                    }
                }
            }

        }.execute();

    }


    /**
     * The getOpenWeatherUnits method returns what type of units the weather is in.
     * @param units celsius or fahrenheit
     * @return the unit of measurement of measurement for the weather
     */
    @NonNull
    private static String getOpenWeatherUnits(String units) {
        if (units.equalsIgnoreCase(ForecastRequest.UNITS_SI)) {
            return OpenWeatherRequest.UNITS_METRIC;
        } else if (units.equalsIgnoreCase(ForecastRequest.UNITS_US)) {
            return OpenWeatherRequest.UNITS_IMPERIAL;
        }
        return units;
    }
}
