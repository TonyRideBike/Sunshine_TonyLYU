package com.dev.tonylyu.sunshine;

/**
 * Created by Tony Lyu on 2017/3/8.
 */

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class Utility {
    public static final String DATE_FORMAT = "yyyyMMdd";

    static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_unit_key),
                context.getString(R.string.pref_unit_metric))
                .equals(context.getString(R.string.pref_unit_metric));
    }

    static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }

        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    static String getFriendlyDateString(Context context, long dateMills) {
        Time time = new Time();
        time.setToNow();

        int julianDay = Time.getJulianDay(dateMills, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), time.gmtoff);

        if (currentJulianDay == julianDay) {
            // return today
            String today = getDayName(context, dateMills);
            int formatID = R.string.format_full_friendly_date;
            return context.getString(formatID, today, getFormattedMonthDay(context, dateMills));
        } else if (currentJulianDay + 7 > julianDay) {
            // return week day
            return getDayName(context, dateMills);
        } else {
            // return date
            SimpleDateFormat shorthenDateFormat = new SimpleDateFormat("EEEE, MMMM d");
            return shorthenDateFormat.format(dateMills);
        }
    }

    static String getDayName(Context context, long dateMills) {
        String dayName;
        Time time = new Time();
        time.setToNow();

        int julianDay = Time.getJulianDay(dateMills, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), time.gmtoff);

        if (julianDay == currentJulianDay) {
            dayName = context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            dayName = context.getString(R.string.tomorrow);
        } else {
            SimpleDateFormat weekDayName = new SimpleDateFormat("EEEE", Locale.getDefault());
            dayName = weekDayName.format(dateMills);
        }
        return dayName;
    }

    private static String getFormattedMonthDay(Context context, long dateMills) {
        Time time = new Time();
        time.setToNow();

        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM d");
        return monthDayFormat.format(dateMills);
    }

    static String getDirection(float degree) {
        if (degree >= 337.5 || degree < 22.5) {
            return "N";
        } else if (degree >= 22.5 && degree < 67.5) {
            return "NE";
        } else if (degree >= 67.5 && degree < 122.5) {
            return "E";
        } else if (degree >= 122.5 && degree < 157.5) {
            return "SE";
        } else if (degree >= 157.5 && degree < 202.5) {
            return "S";
        } else if (degree >= 202.5 && degree < 247.5) {
            return "SW";
        } else if (degree >= 247.5 && degree < 292.5) {
            return "W";
        } else if (degree >= 292.5 && degree < 337.5) {
            return "NW";
        } else return "ERROR";
    }


}
