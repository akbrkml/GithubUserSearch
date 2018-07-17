package com.akbrkml.githubusersearch.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    public static long convertFullDateToMillis(String fullDate) {
        // Mon, 04 Sep 2017 17:26:43 GMT
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        long millis = 0;
        try {
            if (fullDate != null) {
                Date date = inputFormat.parse(fullDate);
                millis = date.getTime();
            }
            else
                return 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millis;
    }

    public static String convertMillisToTimeFormat(long millisTime) {
        Date date = new Date(millisTime);
        DateFormat formatter = new SimpleDateFormat("mm:ss");
        String dateFormatted = formatter.format(date);
        return dateFormatted;
    }
}
