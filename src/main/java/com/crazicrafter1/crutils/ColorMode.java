package com.crazicrafter1.crutils;

import java.util.function.Function;

public enum ColorMode {
    AS_IS(s -> s),
    RENDER_MARKERS(ColorUtil::render),
    STRIP_RENDERED(ColorUtil::strip),
    STRIP_MARKERS(s -> ColorUtil.strip(s, true)),
    INVERT_RENDERED(ColorUtil::invert),
    APPLY_GRADIENTS(ColorUtil::applyGradients),
    RENDER_ALL(ColorUtil::renderAll)
    ;

    private final Function<String, String> formatFunction;

    ColorMode(Function<String, String> formatFunction) {
        this.formatFunction = formatFunction;
    }

    public String a(String s) {
        return formatFunction.apply(s);
    }
}
