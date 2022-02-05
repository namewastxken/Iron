package com.hylandermc.iron.util;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class NumberFormat {

    private static final String[] NAMES = new String[]{
            "k",
            "M",
            "B",
            "T",
            "Quad",
            "Quint",
            "Sext",
            "Sept",
            "Octillion",
            "Nonillion",
            "Decillion",
            "Undecillion",
            "Duodecillion",
            "Tredecillion",
            "Quattuordecillion",
            "Quindecillion",
            "Sexdecillion",
            "Septendecillion",
            "Octodecillion",
            "Novemdecillion",
            "Vigintillion",
    };
    private static final BigInteger THOUSAND = BigInteger.valueOf(1000);
    private static final NavigableMap<BigInteger, String> MAP;
    private static  DecimalFormat format = new DecimalFormat("#.##");

    static {
        MAP = new TreeMap<BigInteger, String>();
        for (int i = 0; i < NAMES.length; i++) {
            MAP.put(THOUSAND.pow(i + 1), NAMES[i]);
        }
    }

    public static String acreateString(BigInteger number) {
        Map.Entry<BigInteger, String> entry = MAP.floorEntry(number);
        if (entry == null) {
            return number.toString();
        }
        BigInteger key = entry.getKey();
        BigInteger d = key.divide(THOUSAND);
        BigInteger m = number.divide(d);
        float f = m.floatValue() / 1000.0f;
        float rounded = ((int) (f * 100.0)) / 100.0f;
        if (rounded % 1 == 0) {
            return ((int) rounded) + "" + entry.getValue();
        }
        return rounded + "" + entry.getValue();
    }

    public static String createString(double d) {

        if (d < 1000L) {
            return smallFormat(d);
        }
        if (d < 1000000L) {
            return smallFormat(d / 1000L) + "k";
        }
        if (d < 1000000000L) {
            return format(d / 1000000L) + "M";
        }
        if (d < 1000000000000L) {
            return format(d / 1000000000L) + "B";
        }
        if (d < 1000000000000000L) {
            return format(d / 1000000000000L) + "T";
        }
        if (d < 1000000000000000000L) {
            return format(d / 1000000000000000L) + "Q";
        }

        return toLong(d);
    }

    private static String format(double d) {
        java.text.NumberFormat format = java.text.NumberFormat.getInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(3);
        format.setMinimumFractionDigits(0);
        return format.format(d);
    }

    private static String smallFormat(double d) {
        java.text.NumberFormat format = java.text.NumberFormat.getInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        return format.format(d);
    }
    private static String toLong(double amt) {
        return String.valueOf((long) amt);
    }
}
