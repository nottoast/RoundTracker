package com.ast.roundtracker.application;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LedgerUtils {

    public static String getDateTime(Long timeInMilliseconds) {
        DateFormat dateTimeFormat = new SimpleDateFormat("dd MMM yy HH:mm");
        dateTimeFormat.setTimeZone(TimeZone.getDefault());
        String dateFormatted = dateTimeFormat.format(new Date(timeInMilliseconds));
        return dateFormatted;
    }

}
