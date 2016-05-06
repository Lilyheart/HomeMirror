package com.morristaedt.mirror.requests;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by HannahMitt on 8/23/15.
 *
 */
public interface ForecastRequest {

    //units of measurement
    String UNITS_SI = "si";
    String UNITS_US = "us";


    //sends out a request to the forecast api
    @GET("/forecast/{apikey}/{lat},{lon}")
    ForecastResponse getHourlyForecast(@Path("apikey") String apiKey, @Path("lat") String lat, @Path("lon") String lon, @Query("exclude") String exclude, @Query("units") String units, @Query("lang") String language);
}
