package com.crazicrafter1.crutils;

import java.util.Random;

public enum MathUtil {
    ;

    public static boolean inRange(int i, int min, int max) {
        return i >= min && i <= max;
    }

    public static int manhattanDist(int x1, int y1, int x2, int y2) {
        // return the summed block distance between 2 points
        return Math.abs(x1-x2) + Math.abs(y1-y2);
    }

    public static int sqDist(int x1, int y1, int x2, int y2) {
        return (x1-x2) * (x1-x2) + (y1-y2) * (y1-y2);
    }

    public static int clamp(int i, int a, int b) {
        return i < a ? a : Math.min(i, b);
    }
}
