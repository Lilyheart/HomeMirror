package com.morristaedt.mirror.modules;

import android.text.Html;
import android.text.Spanned;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by HannahMitt on 8/23/15.
 *
 */
public class DayModule {
    /**
     * The getDay method returns the day of the month in a formatted markup.
     * @return the day of the month in a formatted markup.
     */
    public static Spanned getDay() {
        SimpleDateFormat formatDayOfMonth = new SimpleDateFormat("EEEE", Locale.US);
        Calendar now = Calendar.getInstance();
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        return Html.fromHtml(formatDayOfMonth.format(now.getTime()) + " the " + dayOfMonth + "<sup><small>" + getDayOfMonthSuffix(dayOfMonth) + "</small></sup>");
    }

    /**
     * The getDayOfMonthSuffix returns the end of the string for the day.
     * @param n the current date of the month.
     * @return the suffix of the current day.
     */
    private static String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
}
