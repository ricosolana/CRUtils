package com.crazicrafter1.crutils;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public enum ColorUtil {
    ;

    //                                                      #123456
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?im)&#[0-9a-f]{6}");
    private static final Pattern STRIP_HEX_COLOR_PATTERN = Pattern.compile("(?im)\u00A7x(\u00A7([0-9a-f])){6}");
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?im)\u00A7[0-9a-fk-orx]");

    private static final char COLOR_CHAR = '§';
    private static final char RAW_CHAR = '&';

    private static final char[] BUFFER = new char[256];



    @Nullable
    public static String color(@Nullable String s) {
        if (s == null) return null;
        int size = color(s.toCharArray(), BUFFER, 0);
        return new String(BUFFER, 0, size);
    }

    @Nullable
    public static String color_ThreadSafe(@Nullable String s) {
        if (s == null) return null;
        char[] res = new char[(int)(s.length()*1.667f) + 1];
        int offset = color(s.toCharArray(), res, 0);
        return new String(res, 0, offset);
    }

    static int color(@Nonnull char[] in, @Nonnull char[] out, int outOffset) {
        int end = outOffset;

        final int length = in.length;

        for (int i=0; i < length; i++) {
            // determine whether HEX char

            // & # 0 8 4 c f b H
            // 0 1 2 3 4 5 6 7 8
            char c0 = in[i];
            if (c0 == RAW_CHAR && i+1 < length) {
                char c1 = in[i + 1];
                if (c1 == '#' && i+8 < length) {
                    // Then scan assuming this branch will be taken
                    out[end++] = COLOR_CHAR;
                    out[end++] = 'x';
                    i += 2;
                    for (int w = 0; w < 6; w++) {
                        out[end++] = COLOR_CHAR;
                        out[end++] = in[i];
                        i++;
                    }
                    out[end++] = in[i];
                    continue;
                }
                else if ((c1 >= '0' && c1 <= '9') || (c1 >= 'a' && c1 <= 'f') || (c1 >= 'k' && c1 <= 'o') || c1 == 'r') {
                    out[end++] = COLOR_CHAR;
                    out[end++] = c1;    // TODO similar assign
                    i++;
                    continue;
                }
            }

            out[end++] = c0;            // TODO similar assign
        }

        return end;
    }



    @Nullable
    public static String revert(@Nullable String s) {
        if (s == null) return null;
        int offset = revert(s.toCharArray(), BUFFER, 0);
        return new String(BUFFER, 0, offset);
    }

    @Nullable
    public static List<String> revert(@Nullable List<String> list) {
        if (list == null) return null;

        List<String> ret = new ArrayList<>();
        for (String lore : list) {
            ret.add(revert(lore));
        }

        return ret;
    }

    @Nullable
    public static String revert_ThreadSafe(@Nullable String s) {
        if (s == null) return null;
        char[] res = new char[(int)(s.length()*1.667f) + 1];
        int offset = revert(s.toCharArray(), res, 0);
        return new String(res, 0, offset);
    }

    static int revert(@Nonnull char[] buf, @Nonnull char[] out, int outOffset) {
        final int length = buf.length;

        for (int i=0; i < length; i++) {
            char c0 = buf[i];
            if (c0 == COLOR_CHAR && i+1 < length) {
                char c1 = buf[i+1];

                if (c1 == 'x' && i+14 < length) {
                    // HEX
                    out[outOffset++] = RAW_CHAR;
                    out[outOffset++] = '#';
                    i++;
                    for (int w = 0; w < 6; w++) {
                        BUFFER[outOffset++] = buf[i+=2];
                    }

                    continue;
                } else if ((c1 >= '0' && c1 <= '9')
                        || (c1 >= 'a' && c1 <= 'f')
                        || (c1 >= 'k' && c1 <= 'o')
                        || c1 == 'r') {
                    // LEGACY
                    out[outOffset++] = RAW_CHAR;

                    continue;
                }
            }

            out[outOffset++] = c0;
        }

        return outOffset;
    }



    @Nullable
    public static String strip(@Nullable String s) {
        if (s == null) return null;
        int offset = strip(s.toCharArray(), BUFFER, 0);
        return new String(BUFFER, 0, offset);
    }

    @Nullable
    public static String strip_ThreadSafe(@Nullable String s) {
        if (s == null) return null;
        char[] res = new char[(int)(s.length()*1.667f) + 1];
        int offset = strip(s.toCharArray(), res, 0);
        return new String(res, 0, offset);
    }

    static int strip(@Nonnull char[] buf, @Nonnull char[] out, int outOffset) {
        final int length = buf.length;

        for (int i=0; i < length; i++) {
            char c0 = buf[i];
            if (c0 == COLOR_CHAR && i+1 < length) {
                char c1 = buf[i+1];

                if (c1 == 'x' && i+14 < length) {
                    // HEX
                    i+=13;

                    continue;
                } else if ((c1 >= '0' && c1 <= '9')
                        || (c1 >= 'a' && c1 <= 'f')
                        || (c1 >= 'k' && c1 <= 'o')
                        || c1 == 'r') {
                    // LEGACY
                    i++;
                    continue;
                }
            }

            BUFFER[outOffset++] = c0;
        }
        return outOffset;
    }



    // Input a string, and gradient it from a..b..c..n(x)
    public static int gradientColor(@Nonnull char[] in, @Nonnull char[] out, @Nonnull int outOffset,
                                    int... colors) {
        // Take the unformatted string, and add procedural formatting to it

        //Color START = Color.fromRGB()

        // Test scenarios:
        // Hello       2 colors
        // LERP colors across 5 characters
        // percent change: 1/5, 5 iterations

        int r1 = (colors[0] >> 16) & 0xFF;
        int g1 = (colors[0] >> 8) & 0xFF;
        int b1 = (colors[0]) & 0xFF;
        final int r2 = (colors[0 + 1] >> 16) & 0xFF;
        final int g2 = (colors[0 + 1] >> 8) & 0xFF;
        final int b2 = (colors[0 + 1]) & 0xFF;

        float alpha = 0;
        float change = 1.f/(float)in.length;

        int rc = (r2-r1) / in.length;
        int gc = (g2-g1) / in.length;
        int bc = (b2-b1) / in.length;

        // Character iterate
        for (int ci=0; ci<in.length-1; ci++) {
            // 0b AAAAAAAA RRRRRRRR GGGGGGGG BBBBBBBB

            // parsing hex string
            // 0b1111: 0xF
            // 15: 0xF


            // write RR, GG, BB to out
            out[0] = COLOR_CHAR;
            out[1] = 'x';
            //out[2] = Integer.toHexString(rc += r1);

            //out[i] = in[(int)alpha];
            alpha += change;

        }

        return 0;
    }

    // https://rgb.birdflop.com/script.js
    public static ArrayList<String> gradient(String colorStart, String colorEnd, int colorCount) {

        final Color start = hexToColor(colorStart);
        final Color end = hexToColor(colorEnd);

        final ArrayList<String> gradient = new ArrayList<>();

        float alpha = 0;
        for (int i =0; i < colorCount; i++) {

            alpha += (1.f / (float)colorCount);

            gradient.add(colorToHex(Color.fromRGB(
                    (int)((float)start.getRed() * alpha + (1.f - alpha) * (float)end.getRed()),
                    (int)((float)start.getGreen() * alpha + (1.f - alpha) * (float)end.getGreen()),
                    (int)((float)start.getBlue() * alpha + (1.f - alpha) * (float)end.getBlue())
            )));
        }
        return gradient;
    }

    /**
     * Must be in the format #000000
     * @param hex
     * @return
     */
    @Deprecated
    public static Color hexToColor(String hex) {
        Validate.isTrue(hex.length() == 7, "Not a hex string (length)");
        Validate.isTrue(hex.charAt(0) == '#', "Not a hex string (#)");
        Validate.isTrue(NumberUtils.isNumber(hex.substring(1)), "Not a hex string (number)");

        return Color.fromRGB(Integer.parseInt(hex.substring(1,3), 16),
                Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5), 16));
    }

    @Deprecated
    public static String colorToHex(Color color) {
        return "#" + Integer.toHexString(color.getRed()) +
                Integer.toHexString(color.getGreen()) +
                Integer.toHexString(color.getBlue());
    }
}
