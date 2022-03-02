package com.crazicrafter1.crutils;

import java.util.function.Function;

public enum ColorMode {
    AS_IS(s -> s),
    RENDER(ColorUtil::render),
    STRIP(ColorUtil::strip),
    INVERT(ColorUtil::invert),
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
