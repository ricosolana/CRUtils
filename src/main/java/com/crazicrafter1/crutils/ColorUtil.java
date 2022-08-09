package com.crazicrafter1.crutils;

import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.crazicrafter1.crutils.MathUtil.clamp;

public enum ColorUtil {
    AS_IS(s -> s),
    RENDER_MARKERS(ColorUtil::renderMarkers),
    STRIP_RENDERED(ColorUtil::stripRendered),
    STRIP_MARKERS(ColorUtil::stripMarkers),
    INVERT_RENDERED(ColorUtil::invertRendered),
    APPLY_GRADIENTS(ColorUtil::applyGradients),
    RENDER_ALL(ColorUtil::renderAll)
    ;

    private final Function<String, String> formatFunction;

    ColorUtil(Function<String, String> formatFunction) {
        this.formatFunction = formatFunction;
    }

    public String a(String s) {
        return formatFunction.apply(s);
    }

    private static final char MARK_CHAR = '&';
    private static final char RENDER_CHAR = ChatColor.COLOR_CHAR;

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#[0-9a-fA-F]{6}");

    //private static final Pattern LEGACY_MARK_PATTERN = Pattern.compile(MARK_CHAR + "[0-9a-fA-Fk-oK-OrR]");
    //private static final Pattern HEX_MARK_PATTERN = Pattern.compile("(?im)" + MARK_CHAR + HEX_COLOR_PATTERN);

    private static final Pattern GRADIENT_HEX_PATTERN = Pattern.compile("<" + HEX_COLOR_PATTERN + "[^>]*>" +
            "(.*?)" +
            "</" + HEX_COLOR_PATTERN + ">");
    private static final Pattern GRADIENT_NAME_PATTERN = Pattern.compile("<[0-9a-zA-Z_']+[^>]*>(.*?)</[0-9a-zA-Z_]+>");

    private static final Pattern GRADIENT_PATTERN
            = Pattern.compile("<(" + HEX_COLOR_PATTERN + "|[0-9a-zA-Z_']+)[^>]*>" +
            "(.*?)" +
            "</(" + HEX_COLOR_PATTERN + "|[0-9a-zA-Z_]+)>");

    private static final char[] BUFFER = new char[2048 * 10];

    // todo currently experimental and unused
    //  not really necessary if attention is given to
    //  initial colors to prevent redundant duplicates
    private static class ColoredChar {
        private String color;

        private boolean reset;
        public boolean italic;
        public boolean bold;
        public boolean underline;
        public boolean obfuscated;
        public boolean strikethrough;

        public ColoredChar() {
            reset();
        }

        public void reset() {
            color = null;

            reset = true;
            italic = false;
            bold = false;
            underline = false;
            obfuscated = false;
            strikethrough = false;
        }

        public void format(char f) {
            switch (f) {
                case 'k': obfuscated = true; break;
                case 'l': bold = true; break;
                case 'm': strikethrough = true; break;
                case 'n': underline = true; break;
                case 'o': italic = true; break;
            }
        }

        public boolean isReset() {
            return reset;
        }

        public String getColor() {
            return color;
        }

        public void color(String color) {
            this.color = color;
            reset = false;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ColoredChar))
                return false;

            ColoredChar o = (ColoredChar) obj;

            if (reset && o.reset)
                return true;

            return String.valueOf(color).equals(o.color)
                && italic == o.italic
                && bold == o.bold
                && underline == o.underline
                && obfuscated == o.obfuscated
                && strikethrough == o.strikethrough;
        }
    }

    /**
     * Eliminates any Redundant or useless color codes
     *  - Duplicated or consecutive colors
     *  - Overridden color codes that change nothing
     *  - Color codes that change nothing
     * @param s
     * @return
     */
    public static String eraseRepeatMarkers(String s) {
        //&7&7&7&7
        // Options:
        //  - could use regex
        //  - could use a loop and manual replace
        //  - could brute force, using a per-character
        //      color applicator to detect differences in string
        //      if color changes


        char[] in = s.toCharArray();

        StringBuilder result = new StringBuilder();

        final int length = in.length;
        //ArrayList<ColoredChar> allFormatted = new ArrayList<>(length);

        ColoredChar streamingColor = new ColoredChar();

        char lastValidChar = '\0'; // null
        int lastValidIndex = -1; // null

        // Fox
        // The fox
        // &6The fox
        // &6The &6fox
        // &6&6The fox          If no change, remove

        for (int i=0; i < length; i++) {
            // determine whether HEX char

            // & # 0 8 4 c f b H
            // 0 1 2 3 4 5 6 7 8
            char c0 = in[i];
            if (c0 == MARK_CHAR && i+1 < length) {
                char c1 = in[i + 1];
                if (c1 == '#' && i+7 < length) {
                    ColoredChar currentColor = new ColoredChar();
                    currentColor.color(new String(in, i, 8));
                    i+=7;

                    // If this new color denotes a change, then apply the change
                    if (!currentColor.equals(streamingColor)) {
                        streamingColor.color(currentColor.getColor());
                        result.append(currentColor.getColor());
                    }
                    continue;
                }
                else if ((c1 >= '0' && c1 <= '9') || (c1 >= 'a' && c1 <= 'f')) {
                    ColoredChar currentColor = new ColoredChar();
                    currentColor.color("&" + c1);
                    i++;

                    if (!currentColor.equals(streamingColor)) {
                        streamingColor.color(currentColor.getColor());
                        result.append(currentColor.getColor());
                    }

                    continue;
                } else if ((c1 >= 'k' && c1 <= 'o')) {
                    ColoredChar currentColor = new ColoredChar();
                    currentColor.format(c1);
                    i++;

                    if (!currentColor.equals(streamingColor)) {
                        streamingColor.format(c1);
                        result.append("&").append(c1);
                    }

                    continue;
                } else if (c1 == 'r') {
                    // apply a reset
                    // add a reset token?
                    streamingColor.reset();
                    result.append("&").append("r");
                    i++;
                    continue;
                }
            }

            lastValidChar = c0;
            result.append(c0);
        }

        return result.toString();
    }



    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *                  renderers                  *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Format an input string completely, including legacy markers, hex markers, hex gradients and name gradients
     * @param s input string
     * @return the formatted string
     */
    @CheckReturnValue
    public static String renderAll(@Nullable String s) {
        if (s == null) return null;
        s = applyGradients(s); //applyHexAndNameGradients(s);
        return renderMarkers(s);
    }

    @CheckReturnValue
    public static String renderMarkers(@Nullable final String s) {
        if (s == null) return null;

        int size = renderMarkers(s.toCharArray(), BUFFER, 0);
        return new String(BUFFER, 0, size);
    }

    @CheckReturnValue
    public static String renderMarkers_ThreadSafe(@Nullable String s) {
        if (s == null) return null;
        char[] res = new char[(int)(s.length()*1.7f)]; // &7h -> §7h          &#RRGGBBh -> §x§R§R§G§G§B§Bh
        int offset = renderMarkers(s.toCharArray(), res, 0);
        return new String(res, 0, offset);
    }

    @CheckReturnValue
    public static int renderMarkers(@Nonnull char[] in, @Nonnull char[] out, int outOffset) {
        int end = outOffset;

        final int length = in.length;

        for (int i=0; i < length; i++) {
            // determine whether HEX char

            // & # 0 8 4 c f b H
            // 0 1 2 3 4 5 6 7 8
            char c0 = in[i];
            if (c0 == MARK_CHAR && i+1 < length) {
                char c1 = in[i + 1];
                if (c1 == '#' && i+7 < length) {
                    // analyze the characters ahead of time
                    // ... later
                    // Assuming that the characters do not need to
                    // be checked against during a '&#......' match,
                    // because 99.99% of the time the characters are intended
                    // to be converted into hex characters
                    // just asking who would pass &#zztyui into this
                    // function? such an extraneous value
                    // this allows for performance in the long run
                    // with the use of failsafe assumption


                    // Then scan assuming this branch is successful
                    out[end++] = RENDER_CHAR;
                    out[end++] = 'x';
                    i += 2;
                    for (int w = 0; w < 6; w++) {
                        out[end++] = RENDER_CHAR;
                        out[end++] = in[i];
                        i++;
                    }
                    i--;
                    //out[end++] = in[i];
                    continue;
                }
                else if ((c1 >= '0' && c1 <= '9') || (c1 >= 'a' && c1 <= 'f') || (c1 >= 'k' && c1 <= 'o') || c1 == 'r') {
                    out[end++] = RENDER_CHAR;
                    out[end++] = c1;
                    i++;
                    continue;
                }
            }

            out[end++] = c0;
        }

        return end;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *                invert string                *
     *                                             *
     * Non threadsafe methods are intended to have
     * moderate performance gains over the
     * threadsafe implementation
     * This is because an internal array is reused
     * for the non-threadsafe ones, unlike the
     * threadsafe implementation where a new array
     * is allocated each time
     * Also performance is ok because no regex
     * parsing or replacement is used
     * * * * * * * * * * * * * * * * * * * * * * * */

    @CheckReturnValue
    public static String invertRendered(@Nullable String s) {
        if (s == null) return null;
        int offset = invertRendered(s.toCharArray(), BUFFER, 0);
        return new String(BUFFER, 0, offset);
    }

    @CheckReturnValue
    public static List<String> invertRendered(@Nullable List<String> list) {
        if (list == null) return null;

        List<String> ret = new ArrayList<>();
        for (String lore : list) {
            ret.add(invertRendered(lore));
        }

        return ret;
    }

    @CheckReturnValue
    public static String invertRendered_ThreadSafe(@Nullable String s) {
        if (s == null) return null;
        char[] res = new char[(int)(s.length()*1.667f) + 1]; // expands array by an aprx
        int offset = invertRendered(s.toCharArray(), res, 0);
        return new String(res, 0, offset);
    }

    @CheckReturnValue
    public static int invertRendered(@Nonnull char[] in, @Nonnull char[] out, int outOffset) {
        final int length = in.length;

        for (int i=0; i < length; i++) {
            char c0 = in[i];
            if (c0 == RENDER_CHAR && i+1 < length) {
                char c1 = in[i+1];

                if (c1 == 'x' && i+13 < length) {
                    // HEX
                    out[outOffset++] = MARK_CHAR;
                    out[outOffset++] = '#';
                    i++;
                    for (int w = 0; w < 6; w++) {
                        out[outOffset++] = in[i+=2];
                    }

                    continue;
                } else if ((c1 >= '0' && c1 <= '9')
                        || (c1 >= 'a' && c1 <= 'f')
                        || (c1 >= 'k' && c1 <= 'o')
                        || c1 == 'r') {
                    // LEGACY
                    out[outOffset++] = MARK_CHAR;

                    continue;
                }
            }

            out[outOffset++] = c0;
        }

        return outOffset;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *                 strip string                *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    @CheckReturnValue
    public static String stripRendered(@Nullable String s) {
        return strip(s, false);
    }

    public static String stripMarkers(@Nullable String s) {
        return strip(s, true);
    }

    @CheckReturnValue
    public static String strip(@Nullable String s, boolean removeMarkersOnly) {
        if (s == null) return null;
        int offset = strip(s.toCharArray(), BUFFER, 0, removeMarkersOnly);
        return new String(BUFFER, 0, offset);
    }

    @CheckReturnValue
    public static String strip_ThreadSafe(@Nullable String s, boolean removeMarkersOnly) {
        if (s == null) return null;
        char[] res = new char[(int)(s.length()*1.667f) + 1];
        int offset = strip(s.toCharArray(), res, 0, removeMarkersOnly);
        return new String(res, 0, offset);
    }

    @CheckReturnValue
    public static int strip(@Nonnull char[] in, @Nonnull char[] out, int outOffset, boolean removeMarkersOnly) {
        final int length = in.length;

        for (int i=0; i < length; i++) {
            char c0 = in[i];
            if (((c0 == RENDER_CHAR && !removeMarkersOnly)
                    || (c0 == MARK_CHAR && removeMarkersOnly))
                    && i+1 < length) {
                char c1 = in[i+1];

                if (c1 == 'x' && i+13 < length) {
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

            out[outOffset++] = c0;
        }
        return outOffset;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *                   gradient                  *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    @CheckReturnValue
    public static String applyGradients(@Nullable final String in) {
        if (in == null)
            return null;

        StringBuilder builder = new StringBuilder(in.length()*15).append(in);

        Matcher matcher = GRADIENT_PATTERN.matcher(builder);
        while (matcher.find()) {
            String group = builder.substring(matcher.start(), matcher.end());

            int ar1 = group.indexOf(">");
            int ar2 = group.lastIndexOf("<");

            String text = strip(group.substring(ar1+1, ar2), true);

            // If version less than 1.16
            // then instead keep optional embedded legacy codes
            if (Version.AT_LEAST_v1_16.a()) {
                Color start = null;
                if (group.charAt(1) == '#')
                    start = toColor(group.substring(2, 8));

                Color end = null;
                int lastHash = group.indexOf("#", ar2);
                if (lastHash != -1)
                    end = toColor(group.substring(lastHash + 1, lastHash + 7));

                if (start != null && end != null) {
                    builder.replace(matcher.start(), matcher.end(),
                            applyEdgeColors(text, start, end));
                } else builder.replace(matcher.start(), matcher.end(), text);
            } else
                builder.replace(matcher.start(), matcher.end(), text);

            matcher = GRADIENT_PATTERN.matcher(builder);
        }

        return builder.toString();
    }

    /**
     * Linearly apply an array of colors comprised of gradients across an input string, resulting in a hex marked string
     * @param in the input string
     * @param colors the gradients
     * @return the hex marked string
     */
    @Nonnull
    @CheckReturnValue
    public static String applyEdgeColors(@Nonnull final String in, @Nonnull final Color... colors) {
        return applyAllColors(in, generateColors(in.length(), colors));
    }

    /**
     * Linearly apply a {@link List<Color>} across each character of an input string, returning a hex marked string
     * @param in input string
     * @param colors colors to linearly
     * @return the hex marked string
     */
    @Nonnull
    @CheckReturnValue
    private static String applyAllColors(@Nonnull final String in, @Nonnull final List<Color> colors) {
        // apply a foreach merge of colors onto 'in'
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < in.length(); i++) {
            builder.append(toHexMarker(colors.get(i))).append(in.charAt(i));
        }

        return builder.toString();
    }

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *               color generator               *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Construct a gradient comprised of {@link Color colors} many color edges
     * @param totalColors blends to generate
     * @param edgeColors edge colors
     * @return {@link ArrayList<Color>} colors
     * @throws IllegalArgumentException if edgeColors is less than 2
     */
    @Nonnull
    @CheckReturnValue
    private static ArrayList<Color> generateColors(@Nonnull int totalColors, @Nonnull Color... edgeColors) {
        int gradientCount = edgeColors.length - 1;

        if (gradientCount < 1)
            throw new IllegalArgumentException("Requires at least two colors to form a gradient");

        float avgChange = (float) totalColors / (float) gradientCount;
        float currentShift = 0;
        float lastShift;

        ArrayList<Color> colors = new ArrayList<>();

        for (int gradientIndex = 0; gradientIndex < gradientCount; gradientIndex++) {
            lastShift = currentShift;
            currentShift += avgChange;

            int samples = Math.round(currentShift) - Math.round(lastShift);

            colors.addAll(generateColors(edgeColors[gradientIndex], edgeColors[gradientIndex+1],
                    samples));
        }

        return colors;
    }

    /**
     * Construct a single linear gradient from start to end
     * @param startEdge {@link Color} start color
     * @param endEdge {@link Color} end color
     * @param totalColors the total colors to generate
     * @return {@link ArrayList<Color>} colors
     */
    @Nonnull
    @CheckReturnValue
    private static ArrayList<Color> generateColors(Color startEdge, Color endEdge, int totalColors) {
        int r1 = startEdge.getRed();
        int g1 = startEdge.getGreen();
        int b1 = startEdge.getBlue();

        int div = totalColors > 1 ? totalColors - 1 : 1;

        final int rc = (endEdge.getRed() - r1 + 1) / div;
        final int gc = (endEdge.getGreen() - g1 + 1) / div;
        final int bc = (endEdge.getBlue() - b1 + 1) / div;

        ArrayList<Color> colors = new ArrayList<>();

        for (int i = 0; i < totalColors; i++) {
            colors.add(toColor(r1, g1, b1));

            r1 += rc;
            g1 += gc;
            b1 += bc;
        }

        return colors;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * *
     *                                             *
     *                   utility                   *
     *                                             *
     * * * * * * * * * * * * * * * * * * * * * * * */

    public static String toHexMarker(Color color) {
        return "&#" + String.format("%06x", color.asRGB());
    }

    public static Color toColor(int r, int g, int b) {
        return Color.fromRGB(clamp(r, 0, 255), clamp(g, 0, 255), clamp(b, 0, 255));
    }

    public static Color toColor(int rgb) {
        return Color.fromRGB(rgb);
    }

    public static Color toColor(String hex) {
        return Color.fromRGB(Integer.parseInt(hex, 16));
    }

    public static String toHex(Color color) {
        return Integer.toHexString(color.asRGB());
    }

    public static String toHex(java.awt.Color jColor) {
        return toHex(toColor(jColor.getRed(), jColor.getGreen(), jColor.getBlue()));
    }
}
