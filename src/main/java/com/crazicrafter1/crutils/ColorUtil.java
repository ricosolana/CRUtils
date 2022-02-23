package com.crazicrafter1.crutils;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Color;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ColorUtil {
    ;

    //                                                      #123456
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");
    private static final Pattern STRIP_HEX_COLOR_PATTERN = Pattern.compile("(?i)§x(§([0-9]|[a-f])){6}");
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + "§" + "[0-9A-FK-ORX]");

    public static String color(String s) {
        if (Version.AT_LEAST_v1_16.a()) {
            Matcher match = HEX_COLOR_PATTERN.matcher(s);
            while (match.find()) {
                String color = s.substring(match.start(), match.end());
                s = s.replace(color, "" + net.md_5.bungee.api.ChatColor.of(color.substring(1)));
                match = HEX_COLOR_PATTERN.matcher(s);
            }
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * More testing is needed for this
     * @param input
     * @param indexes
     * @return
     */
    @Deprecated
    public static String strip(String input, Map<Integer, String> indexes) {
        if (input == null)
            return null;

        Matcher matcher = STRIP_COLOR_PATTERN.matcher(input);
        while (matcher.find()) {
            indexes.put(matcher.start(), input.substring(matcher.start(), matcher.end()));
        }
        //input = matcher.replaceAll("");

        matcher = STRIP_HEX_COLOR_PATTERN.matcher(input);
        while (matcher.find()) {
            indexes.put(matcher.start(), input.substring(matcher.start(), matcher.end()));
        }
        return matcher.replaceAll("");
    }

    public static String strip(String input) {
        String output = STRIP_HEX_COLOR_PATTERN.matcher(input).replaceAll("");

        return STRIP_COLOR_PATTERN.matcher(output).replaceAll("");
    }

    /**
     * Does the opposite of ChatColor.translateAlternateColorCodes(...)
     * (Returns the string with &'s)
     */
    public static String revert(@Nullable String textToTranslate) {
        if (textToTranslate == null)
            return null;

        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == 167 && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = '&';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static List<String> revert(@Nullable List<String> list) {
        if (list == null)
            return null;

        List<String> ret = new ArrayList<>();
        for (String lore : list) {
            ret.add(revert(lore));
        }

        return ret;
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
    public static Color hexToColor(String hex) {
        Validate.isTrue(hex.length() == 7, "Not a hex string (length)");
        Validate.isTrue(hex.charAt(0) == '#', "Not a hex string (#)");
        Validate.isTrue(NumberUtils.isNumber(hex.substring(1)), "Not a hex string (number)");

        return Color.fromRGB(Integer.parseInt(hex.substring(1,3), 16),
                Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5), 16));
    }

    public static String colorToHex(Color color) {
        return "#" + Integer.toHexString(color.getRed()) +
                Integer.toHexString(color.getGreen()) +
                Integer.toHexString(color.getBlue());
    }
}
