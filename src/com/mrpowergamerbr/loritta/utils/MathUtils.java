package com.mrpowergamerbr.loritta.utils;

public class MathUtils {
    public static double getPercentage(double val1, double val2) {
        return (val1 / val2) * 100;
    }
    
    public static boolean chance(final double e) {
        final double d = Math.random();
        return d < e / 100.0;
    }
}
