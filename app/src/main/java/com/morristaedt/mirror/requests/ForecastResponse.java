package com.morristaedt.mirror.requests;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by HannahMitt on 8/23/15.
 *
 */
public class ForecastResponse {

    public ForecastCurrently currently;
    public ForecastHourly hourly;

    public class ForecastCurrently {
        public String summary;
        public float temperature;

        public String getDisplayTemperature() {
            return String.valueOf(Math.round(temperature)) + (char) 0x00B0;
        }
    }

    public class ForecastHourly {
        public ArrayList<Hour> data;
    }

    public class Hour {
        public long time; // in seconds
        public float precipProbability;

        public Calendar getCalendar() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time * 1000);
            return calendar;
        }
    }
}
