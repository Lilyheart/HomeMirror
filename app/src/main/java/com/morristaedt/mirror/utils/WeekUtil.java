package com.morristaedt.mirror.utils;

import java.util.Calendar;

/**
 * Created by HannahMitt on 8/23/15.
 */
public class WeekUtil {

    /**
     * The method isWeekday determines if it is a weekday.
     * @return true if it a weekday.
     */
    public static boolean isWeekday() {
        //Determine current day
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }

    /**
     * The method afterFive determines if it is evening.
     * @return true if it 5 p.m. or later and before midnight.
     */

    public static boolean afterFive() {
        //Determine current hour
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hourOfDay >= 17;
    }

    /**
     * The method isWeekdayBeforeFive determines if it is a weekday evening.
     * @return true if it is before 5 p.m. and a weekday.
     */

    public static boolean isWeekdayBeforeFive() {
        return isWeekday() && !afterFive();
    }

    /**
     * The method isWeekdayAfterFive
     * @return true if it is 5 p.m. or later and before midnight as well as a weekday
     */

    public static boolean isWeekdayAfterFive() {
        return isWeekday() && afterFive();
    }
}
