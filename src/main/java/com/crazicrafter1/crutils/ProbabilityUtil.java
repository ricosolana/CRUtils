package com.crazicrafter1.crutils;

import java.util.Random;

public enum ProbabilityUtil {
    ;

    public static int randomRange(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    // fixme usage is too extraneous
    @Deprecated
    public static int randomRange(int min, int max, int min1, int max1) {
        if ((int)(Math.random()*2) == 0)
            return min + (int)(Math.random() * ((max - min) + 1));
        return min1 + (int)(Math.random() * ((max1 - min1) + 1));
    }

    public static int randomRange(int min, int max, Random random) {
        return random.nextInt((max - min) + 1) + min;
    }

    /**
     * Return a rolled chance result
     * @param i [0, 1]
     * @return Whether the pseudo-event occurred
     */
    public static boolean chance(float i) {
        return i <= Math.random();
    }

    public static boolean chance(float i, Random random) {
        return i <= (float) (random.nextInt(100) + 1) / 100f;
    }
}
