package com.crazicrafter1.crutils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Stream;

public enum RandomUtil {
    ;

    public static double randomRange(double min, double max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

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

    //public static @Nullable
    //<T> T getRandom(@Nonnull Stream<T> stream) {
    //    // return
    //    return stream
    //            .skip((int) (stream.count() * Math.random()))
    //            .findFirst().orElse(null);
    //}

    public static @Nullable
    <T> T getRandom(@Nonnull T[] values) {
        //return getRandom(Arrays.stream(values));
        return values[(int) (values.length * Math.random())];
    }

    public static @Nullable
    <T> T getRandom(@Nonnull Collection<T> collection) {
        return (T) collection.toArray();
        //return getRandom(collection.stream());
    }

    public static @Nullable
    <T> T getRandomOf(@Nonnull T... values) {
        return getRandom(values);
    }
}
