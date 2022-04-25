package com.crazicrafter1.crutils;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

public class SimilarString implements Comparable<SimilarString> {

    public final int distance;
    public final String s;

    public SimilarString(String base, String other) {
        distance = StringUtils.getLevenshteinDistance(base, other);
        s = base;
    }

    @Override
    public int compareTo(@Nonnull SimilarString o) {
        return distance - o.distance;
    }

    @Override
    public String toString() {
        return s;
    }
}
