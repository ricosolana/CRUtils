package com.crazicrafter1.crutils;

public enum Version {
    // Version is at least
    AT_LEAST_v1_8,
    AT_LEAST_v1_9,
    AT_LEAST_v1_10,
    AT_LEAST_v1_11,
    AT_LEAST_v1_12,
    AT_LEAST_v1_13,
    AT_LEAST_v1_14,
    AT_LEAST_v1_15,
    AT_LEAST_v1_16,
    AT_LEAST_v1_17,
    AT_LEAST_v1_18,

    // Version is at most
    AT_MOST_v1_8,
    AT_MOST_v1_9,
    AT_MOST_v1_10,
    AT_MOST_v1_11,
    AT_MOST_v1_12,
    AT_MOST_v1_13,
    AT_MOST_v1_14,
    AT_MOST_v1_15,
    AT_MOST_v1_16,
    AT_MOST_v1_17,
    AT_MOST_v1_18,

    // Version equals
    v1_8,
    v1_9,
    v1_10,
    v1_11,
    v1_12,
    v1_13,
    v1_14,
    v1_15,
    v1_16,
    v1_17,
    v1_18,
    //v1_18_R1,
    //v1_18_R2,
    ;

    private final boolean active;

    Version() {
        int cmp = cmp();
        if (name().startsWith("AT_LEAST")) {
            active = cmp >= 0;
        } else if (name().startsWith("AT_MOST")) {
            active = cmp <= 0;
        } else {
            String[] split = name().substring(name().indexOf('v')+1).split("_");
            if (split.length != 3) {
                active = cmp == 0;
            } else {
                // exact 3 version parsing
                active = ReflectionUtil.VERSION.equals(name());
            }
        }
    }

    /**
     * Compares a bukkit version against this version
     * Returns how far ahead bukkit version is against this
     *  - Positive for greater bukkit version
     *  - Negative for less bukkit version
     *  - 0 for same version
     * @return signed difference
     */
    private int cmp() {
        String[] split = name().substring(name().indexOf('v')+1).split("_");
        int major = Integer.parseInt(split[1]);
        //int minor = Integer.parseInt(split[2]);
        return ReflectionUtil.VERSION_MAJOR - major;
    }

    public boolean a() {
        return active;
    }
}
