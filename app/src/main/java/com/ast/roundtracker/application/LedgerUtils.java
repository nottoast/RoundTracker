package com.ast.roundtracker.application;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

public class LedgerUtils {

    public static void main(String[] args) {
        System.out.println(getInviteCode());
    }

    public static String getDateTime(Long timeInMilliseconds) {
        DateFormat dateTimeFormat = new SimpleDateFormat("dd MMM yy HH:mm");
        dateTimeFormat.setTimeZone(TimeZone.getDefault());
        String dateFormatted = dateTimeFormat.format(new Date(timeInMilliseconds));
        return dateFormatted;
    }

    public static String getInviteCode() {
        String inviteCode = "a0b1c2d3";

        try {
            String randomNumber = Math.random()+"";
            MessageDigest hashedNumber = MessageDigest.getInstance("MD5");
            hashedNumber.update(randomNumber.getBytes(), 0, randomNumber.length());
            inviteCode = (new BigInteger(1, hashedNumber.digest()).toString(16)).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return inviteCode;
    }

}
