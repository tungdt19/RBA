package com.viettel.vtag.utils;

import static java.lang.Math.*;

public class GpsUtils {

    private static final double R = 6_371_000;
    private static final double radian = PI / 180;

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        var phi1 = lat1 * radian;
        var phi2 = lat2 * radian;
        var deltaPhi = (lat2 - lat1) * radian / 2;
        var deltaLambda = (lon2 - lon1) * radian / 2;
        var sinPhi = sin(deltaPhi);
        var sinLambda = sin(deltaLambda);
        var a = sinPhi * sinPhi + cos(phi1) * cos(phi2) * sinLambda * sinLambda;
        return 2 * R * atan2(sqrt(a), sqrt(1 - a));
    }

    public static double toDouble(double degree, double minute, double second, boolean reverse) {
        return (reverse ? -1 : 1) * (degree + minute / 60 + second / 360);
    }

    public static void main(String[] args) {
        var lat1 = toDouble(50, 3, 59, false);
        var lon1 = toDouble(5, 42, 53, true);
        var lat2 = toDouble(58, 38, 38, false);
        var lon2 = toDouble(3, 4, 12, true);

        System.out.println(distance(lat1, lon1, lat2, lon2));
        System.out.println(distance(51.5, 0, 38.8, -77.1));
    }
}
