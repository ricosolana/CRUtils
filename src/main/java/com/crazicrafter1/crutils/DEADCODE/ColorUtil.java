package com.crazicrafter1.crutils.DEADCODE;

import com.crazicrafter1.crutils.Util;
import org.bukkit.Color;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public enum ColorUtil {
    ;

    //                                                      #123456
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?im)&#[0-9a-f]{6}");
    private static final Pattern STRIP_HEX_COLOR_PATTERN = Pattern.compile("(?im)\u00A7x(\u00A7([0-9a-f])){6}");
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?im)\u00A7[0-9a-fk-orx]");

    private static final char COLOR_CHAR = '\u00A7';
    private static final char RAW_CHAR = '&';

    // Hex color codes can get really fricking large
    // uses about .0819 MB, or ~82 KB
    private static final char[] BUFFER = new char[2048 * 10];




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

    public static int color(@Nonnull char[] in, @Nonnull char[] out, int outOffset) {
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

    public static int revert(@Nonnull char[] buf, @Nonnull char[] out, int outOffset) {
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

    public static int strip(@Nonnull char[] buf, @Nonnull char[] out, int outOffset) {
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




    //https://regexr.com/6ge2a
    private static final Pattern TAG_PATTERN = Pattern.compile("<#[0-9a-f]{6}[^>]*>(.*?)</#[0-9a-f]{6}>");
    public static String withTagGradient(final String in) {

        // search for the pattern in string, replacing as necessary
        StringBuilder builder = new StringBuilder(in.length()*15).append("&7").append(in);

        // begin and end gradient can be extracted sing string indexOf and lastIndexOf < and > chars
        Matcher matcher = TAG_PATTERN.matcher(in);
        while (matcher.find()) {
            // REPLACE
            String group = in.substring(matcher.start(), matcher.end());
            String text = group.substring(9, group.length() - 10);
            Color start = hexToColor(group.substring(2, 8));
            Color end = hexToColor(group.substring(group.length() - 7, group.length() - 1));

            //                                  +2 to offset the '&7' above
            builder.replace(matcher.start()+2, matcher.end()+2,
                    applyGradient(text, start, end) + "&7");
            //matcher = TAG_PATTERN.matcher(builder);
        }

        return builder.toString();
    }

    public static String applyGradient(String in, Color... colors) {
        return applyGradient(in, generateGradient(in.length(), colors));
    }

    @CheckReturnValue
    public static String applyGradient(String in, ArrayList<Color> colors) {
        // apply a foreach merge of colors onto 'in'
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < in.length(); i++) {
            builder.append(ColorUtil.colorToMarkdown(colors.get(i))).append(in.charAt(i));
        }

        return builder.toString();
    }

    /**
     * Construct a gradient comprised of {@link Color colors} many color boundaries
     * @param totalSamples how many blends to generate
     * @param gradients
     * @return
     */
    @CheckReturnValue
    public static ArrayList<Color> generateGradient(int totalSamples, Color... gradients) {
        int gradientCount = gradients.length - 1;

        float avgChange = (float) totalSamples / (float) gradientCount;
        float currentShift = 0;
        float lastShift;

        ArrayList<Color> allBlends = new ArrayList<>();

        for (int gradientIndex = 0; gradientIndex < gradientCount; gradientIndex++) {
            lastShift = currentShift;
            currentShift += avgChange;

            int samples = Math.round(currentShift) - Math.round(lastShift);

            allBlends.addAll(generateGradient(gradients[gradientIndex], gradients[gradientIndex+1],
                    samples));
        }

        return allBlends;
    }

    /**
     * Construct a single linear gradient from start to end
     * @param start {@link Color} starting color
     * @param end{@link Color} ending color
     * @param totalSamples the total colors to generate fixme make concise and descriptive names to avoid confusion
     * @return
     */
    @CheckReturnValue
    public static ArrayList<Color> generateGradient(Color start, Color end, int totalSamples) {
        int r1 = start.getRed();
        int g1 = start.getGreen();
        int b1 = start.getBlue();

        int div = totalSamples > 1 ? totalSamples - 1 : 1;

        final int rc = (end.getRed() - r1 + 1) / div;
        final int gc = (end.getGreen() - g1 + 1) / div;
        final int bc = (end.getBlue() - b1 + 1) / div;

        ArrayList<Color> gradient = new ArrayList<>();

        for (int i = 0; i < totalSamples; i++) {
            gradient.add(toColor(r1, g1, b1));

            r1 += rc;
            g1 += gc;
            b1 += bc;
        }

        return gradient;
    }

    /**
     * fixme to avoid confusion, use better naming schemas
     * @param color {@link Color} the bukkit color
     * @return the formatted string
     */
    private static String colorToMarkdown(Color color) {
        return "&#" + String.format("%06x", color.asRGB());
    }

    private static Color hexToColor(String hexString) {
        return Color.fromRGB(Integer.parseInt(hexString, 16));
    }

    private static Color toColor(int r, int g, int b) {
        return Color.fromRGB(Util.clamp(r, 0, 255), Util.clamp(g, 0, 255), Util.clamp(b, 0, 255));
    }

    private static String colorToHex(Color color) {
        return Integer.toHexString(color.asRGB());
    }
}
