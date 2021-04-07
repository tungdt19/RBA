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
        System.out.println(distance(21.06757812, 105.81127031, 21.066706, 105.811164));
    }
}
