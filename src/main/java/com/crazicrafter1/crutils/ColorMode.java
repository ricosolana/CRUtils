package com.crazicrafter1.crutils;

import java.util.function.Function;

public enum ColorMode {
    AS_IS(s -> s),
    COLOR(ColorUtil::color),
    STRIP(ColorUtil::strip),
    REVERT(ColorUtil::revert);

    private final Function<String, String> formatFunction;

    ColorMode(Function<String, String> formatFunction) {
        this.formatFunction = formatFunction;
    }

    public String a(String s) {
        return formatFunction.apply(s);
    }
}
