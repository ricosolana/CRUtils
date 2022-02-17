package com.crazicrafter1.crutils;

import com.crazicrafter1.crutils.refl.GameProfileMirror;
import com.crazicrafter1.crutils.refl.PropertyMirror;
//import com.mojang.authlib.GameProfile;
//import com.mojang.authlib.properties.Property;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
//import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
//import net.minecraft.server.level.EntityPlayer;
//import net.minecraft.server.network.PlayerConnection;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Color;
//import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Util {
    ;

    public static boolean inRange(int i, int min, int max) {
        return i >= min && i <= max;
    }

    public static void giveItemToPlayer(Player p, ItemStack item) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (p.isDead()) {
                    p.getWorld().dropItem(p.getLocation(), item);
                } else {
                    HashMap<Integer, ItemStack> remaining;
                    if (!(remaining = p.getInventory().addItem(item)).isEmpty()) {
                        for (ItemStack itemStack : remaining.values()) {
                            p.getWorld().dropItem(p.getLocation(), itemStack);
                        }
                    }
                }
            }
        }.runTaskLater(Main.getInstance(), 1);
    }

    /**
     * Simply converts § to &
     * @param itemStack
     * @return
     */
    public static String flattenedName(ItemStack itemStack, String def) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                return Util.toAlternateColorCodes('&', meta.getDisplayName());
            }
            return meta.getLocalizedName();
        }
        return def;
    }

    //public static ItemStack getMaterial(String name) {
    //    try {
    //        return new ItemStack(Material.matchMaterial(name));
    //    } catch (Exception e) {
    //        switch (name.toUpperCase()) {
    //            case "FIREWORK_ROCKET":
    //
    //        }
    //    }
    //}

    /**
     * Same as {@link @flattenedName} but lores are combined, and separated by a newline '\n' character
     * @param itemStack
     * @return
     */
    public static String flattenedLore(ItemStack itemStack, String def) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> loreList = meta.getLore();
            if (loreList != null) {
                StringBuilder builder = new StringBuilder();
                for (String lore : loreList) {
                    builder.append(toAlternateColorCodes('&', lore)).append("\n");
                }
                return builder.toString();
            }
        }
        return toAlternateColorCodes('&', def);
    }

    /*
    public static Color matchColor(String color) {
        // Java 16 magic!


        switch (color.toUpperCase()) {
            case "BLUE": return Color.BLUE;
            case "RED" -> Color.RED;
            case "WHITE" -> Color.WHITE;
            case "GRAY" -> Color.GRAY;
            case "GREEN" -> Color.GREEN;
            case "YELLOW" -> Color.YELLOW;
            case "AQUA" -> Color.AQUA;
            case "BLACK" -> Color.BLACK;
            case "FUCHSIA" -> Color.FUCHSIA;
            case "LIME" -> Color.LIME;
            case "MAROON" -> Color.MAROON;
            case "NAVY" -> Color.NAVY;
            case "OLIVE" -> Color.OLIVE;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            case "SILVER" -> Color.SILVER;
            case "TEAL" -> Color.TEAL;
            default -> null;
        };
    }
     */

    public static int randomRange(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    public static int randomRange(int min, int max, int min1, int max1) {
        if ((int)(Math.random()*2) == 0)
            return min + (int)(Math.random() * ((max - min) + 1));
        return min1 + (int)(Math.random() * ((max1 - min1) + 1));
    }

    public static int randomRange(int min, int max, Random random)
    {
        return random.nextInt((max - min) + 1) + min;
    }

    /**
     * Returns a chance
     * @param i [0, 1]
     * @return whether 'i' exceeded a chance
     */
    public static boolean randomChance(float i) {
        return i >= Math.random();
    }

    @Deprecated
    public static boolean randomChance(float i, Random random) {
        return i <= (float)randomRange(0, 100, random) / 100f;
    }

    public static int sqDist(int x1, int y1, int x2, int y2) {
        return (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2);
    }

    //                                                      #123456
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");

    public static String format(String s) {
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

    private static final Pattern STRIP_HEX_COLOR_PATTERN = Pattern.compile("(?i)§x(§([0-9]|[a-f])){6}");
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '§' + "[0-9A-FK-ORX]");

    public static String stripColor(String input, Map<Integer, String> indexes) {
        if (input == null)
            return null;

        Matcher matcher = STRIP_COLOR_PATTERN.matcher(input);
        while (matcher.find()) {
            indexes.put(matcher.start(), input.substring(matcher.start(), matcher.end()));
        }
        input = matcher.replaceAll("");

        matcher = STRIP_HEX_COLOR_PATTERN.matcher(input);
        while (matcher.find()) {
            indexes.put(matcher.start(), input.substring(matcher.start(), matcher.end()));
        }
        return matcher.replaceAll("");
    }

    public static String stripColor(String input) {
        String output = STRIP_HEX_COLOR_PATTERN.matcher(input).replaceAll("");

        return STRIP_COLOR_PATTERN.matcher(output).replaceAll("");
    }

    public static String macro(String str, String delim, String match, String value) {
        if (str == null)
            return null;
        return str.replace(delim + match + delim, value);
    }

    /**
     * Set placeholders of a string using PAPI
     * PAPI-safe (plugin doesnt have to exist)
     * @param p player
     * @param text text
     * @return placeholder text
     */
    @NotNull
    public static String placeholders(@Nullable Player p, @NotNull String text) {
        if (p != null && Main.getInstance().supportPlaceholders) {
            return PlaceholderAPI.setPlaceholders(p, text);
        }
        return text;
    }

    /*
    public static Enchantment matchEnchant(String enchant) {
        String e = enchant.toUpperCase().replace(" ", "_");

        return switch (e) {
            case "ARROW_DAMAGE", "POWER" -> Enchantment.ARROW_DAMAGE;
            case "ARROW_FIRE", "FLAME" -> Enchantment.ARROW_FIRE;
            case "ARROW_INFINITE", "INFINITY" -> Enchantment.ARROW_INFINITE;
            case "ARROW_KNOCKBACK", "PUNCH" -> Enchantment.ARROW_KNOCKBACK;
            case "BINDING_CURSE", "CURSE_OF_BINDING" -> Enchantment.BINDING_CURSE;
            case "CHANNELING" -> Enchantment.CHANNELING;
            case "DAMAGE_ALL", "SHARPNESS" -> Enchantment.DAMAGE_ALL;
            case "DAMAGE_ANTHROPODS", "BANE_OF_ANTHROPODS" -> Enchantment.DAMAGE_ARTHROPODS;
            case "DAMAGE_UNDEAD", "SMITE" -> Enchantment.DAMAGE_UNDEAD;
            case "DEPTH_STRIDER" -> Enchantment.DEPTH_STRIDER;
            case "DIG_SPEED", "EFFICIENCY" -> Enchantment.DIG_SPEED;
            case "DURABILITY", "UNBREAKING" -> Enchantment.DURABILITY;
            case "FIRE_ASPECT" -> Enchantment.FIRE_ASPECT;
            case "FROST_WALKER" -> Enchantment.FROST_WALKER;
            case "IMPALING" -> Enchantment.IMPALING;
            case "KNOCKBACK" -> Enchantment.KNOCKBACK;
            case "LOOT_BONUS_BLOCKS", "FORTUNE" -> Enchantment.LOOT_BONUS_BLOCKS;
            case "LOOT_BONUS_MOBS", "LOOTING" -> Enchantment.LOOT_BONUS_MOBS;
            case "LOYALTY" -> Enchantment.LOYALTY;
            case "LUCK", "LUCK_OF_THE_SEA" -> Enchantment.LUCK;
            case "LURE" -> Enchantment.LURE;
            case "MENDING" -> Enchantment.MENDING;
            case "MULTISHOT" -> Enchantment.MULTISHOT;
            case "OXYGEN", "RESPIRATION" -> Enchantment.OXYGEN;
            case "PIERCING" -> Enchantment.PIERCING;
            case "PROTECTION_ENVIRONMENTAL", "PROTECTION" -> Enchantment.PROTECTION_ENVIRONMENTAL;
            case "PROTECTION_FIRE", "FIRE_PROTECTION" -> Enchantment.PROTECTION_FIRE;
            case "PROTECTION_FALL", "FEATHER_FALLING" -> Enchantment.PROTECTION_FALL;
            case "PROTECTION_EXPLOSIONS", "BLAST_PROTECTION" -> Enchantment.PROTECTION_EXPLOSIONS;
            case "PROTECTION_PROJECTILE", "PROJECTILE_PROTECTION" -> Enchantment.PROTECTION_PROJECTILE;
            case "QUICK_CHARGE" -> Enchantment.QUICK_CHARGE;
            case "RIPTIDE" -> Enchantment.RIPTIDE;
            case "SILK_TOUCH" -> Enchantment.SILK_TOUCH;
            case "SOUL_SPEED" -> Enchantment.SOUL_SPEED;
            case "SWEEPING_EDGE" -> Enchantment.SWEEPING_EDGE;
            case "THORNS" -> Enchantment.THORNS;
            case "VANISHING_CURSE", "CURSE_OF_VANISHING" -> Enchantment.VANISHING_CURSE;
            case "WATER_WORKER", "AQUA_AFFINITY" -> Enchantment.WATER_WORKER;
            default -> null;
        };
    }

    public static PotionEffectType matchPotionEffectType(String effect) {
        String e = effect.toUpperCase().replace(" ", "_");

        return switch (e) {
            case "ABSORPTION" -> PotionEffectType.ABSORPTION;
            case "BAD_OMEN" -> PotionEffectType.BAD_OMEN;
            case "BLINDNESS" -> PotionEffectType.BLINDNESS;
            case "CONDUIT_POWER" -> PotionEffectType.CONDUIT_POWER;
            case "CONFUSION", "NAUSEA" -> PotionEffectType.CONFUSION;
            case "DAMAGE_RESISTANCE", "RESISTANCE" -> PotionEffectType.DAMAGE_RESISTANCE;
            case "DOLPHINS_GRACE" -> PotionEffectType.DOLPHINS_GRACE;
            case "FAST_DIGGING", "HASTE" -> PotionEffectType.FAST_DIGGING;
            case "FIRE_RESISTANCE" -> PotionEffectType.FIRE_RESISTANCE;
            case "GLOWING" -> PotionEffectType.GLOWING;
            case "HARM", "INSTANT_DAMAGE" -> PotionEffectType.HARM;
            case "HEAL", "INSTANT_HEALTH" -> PotionEffectType.HEAL;
            case "HEALTH_BOOST" -> PotionEffectType.HEALTH_BOOST;
            case "HERO_OF_THE_VILLAGE" -> PotionEffectType.HERO_OF_THE_VILLAGE;
            case "HUNGER" -> PotionEffectType.HUNGER;
            case "INCREASE_DAMAGE", "STRENGTH" -> PotionEffectType.INCREASE_DAMAGE;
            case "INVISIBILITY" -> PotionEffectType.INVISIBILITY;
            case "JUMP", "JUMP_BOOST" -> PotionEffectType.JUMP;
            case "LEVITATION" -> PotionEffectType.LEVITATION;
            case "LUCK" -> PotionEffectType.LUCK;
            case "NIGHT_VISION" -> PotionEffectType.NIGHT_VISION;
            case "POISON" -> PotionEffectType.POISON;
            case "REGENERATION" -> PotionEffectType.REGENERATION;
            case "SATURATION" -> PotionEffectType.SATURATION;
            case "SLOW", "SLOWNESS" -> PotionEffectType.SLOW;
            case "SLOW_DIGGING", "MINING_FATIGUE" -> PotionEffectType.SLOW_DIGGING;
            case "SLOW_FALLING" -> PotionEffectType.SLOW_FALLING;
            case "SPEED", "SWIFTNESS" -> PotionEffectType.SPEED;
            case "UNLUCK", "BAD_LUCK" -> PotionEffectType.UNLUCK;
            case "WATER_BREATHING" -> PotionEffectType.WATER_BREATHING;
            case "WEAKNESS" -> PotionEffectType.WEAKNESS;
            case "WITHER" -> PotionEffectType.WITHER;
            default -> null;
        };
    }
     */

    public static int clamp(int i, int a, int b) {
        return i < a ? a : Math.min(i, b);
    }

    /**
     * Does the opposite of ChatColor.translateAlternateColorCodes(...)
     * (Returns the string with &'s)
     */
    public static String toAlternateColorCodes(char altColorChar, @Nullable String textToTranslate) {
        if (textToTranslate == null)
            return null;
        //Validate.notNull(textToTranslate, "Cannot translate null text");
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == 167 && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = altColorChar;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static List<String> toAlternateColorCodes(char altColorChar, @Nullable List<String> list) {
        if (list == null)
            return null;

        List<String> ret = new ArrayList<>();
        for (String lore : list) {
            ret.add(Util.toAlternateColorCodes(altColorChar, lore));
        }

        return ret;
    }

    public static long copy(InputStream in, OutputStream out) throws IOException {
        long bytes = 0;
        byte[] buf = new byte[0x1000];
        while (true) {
            int r = in.read(buf);
            if (r == -1)
                break;
            out.write(buf, 0, r);
            bytes += r;
        }
        out.flush();
        out.close();
        in.close();
        return bytes;
    }

    /**
     * Untested, needs testing
     */
    @Deprecated
    static void downloadURLAsFile(String link, String out) {
        try {
            URL website = new URL(link);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(out);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            //fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object makeGameProfile(String b64) {
        // random uuid based on the b64 string
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );

        // https://github.com/deanveloper/SkullCreator/blob/master/src/main/java/dev/dbassett/skullcreator/SkullCreator.java#L260
        GameProfileMirror profile = new GameProfileMirror(id, "aaaaa");
        profile.putProperty("textures", new PropertyMirror("textures", b64, null));
        return profile.getInstance();
    }

    //final static Class<?> CLASS_EntityPlayer = ReflectionUtil.getNMClass("level.EntityPlayer");
    static Class<?> CLASS_CraftPlayer;
    static Method METHOD_getHandle;// = ReflectionUtil.getMethod(CLASS_EntityPlayer, "getHandle");
    static Class<?> CLASS_EntityPlayer;
    static Field FIELD_playerConnection;
    static Class<?> CLASS_PlayerConnection;
    //static Class<?> CLASS_Packet;
    static Method METHOD_sendPacket;
    static Class<?> CLASS_PacketPlayOutPlayerInfo;

    static Class<?> CLASS_EnumPlayerInfoAction;
    static Object ENUM_REMOVE_PLAYER;
    static Object ENUM_ADD_PLAYER;

    static {
        CLASS_CraftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
        METHOD_getHandle = ReflectionUtil.getMethod(CLASS_CraftPlayer, "getHandle");
        CLASS_EntityPlayer = METHOD_getHandle.getReturnType();
        FIELD_playerConnection = ReflectionUtil.findFieldByType(CLASS_EntityPlayer, "PlayerConnection");
        // get the PlayerConnection
        CLASS_PlayerConnection = FIELD_playerConnection.getType();
        //CLASS_Packet = ReflectionUtil.getNMClass("");
        //METHOD_sendPacket = ReflectionUtil.getMethod(CLASS_PlayerConnection, "sendPacket", CLASS_Packet);
    }

    // cast player to

    private static String getStringFromURL(String url) {
        StringBuilder text = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new URL(url).openStream());
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                while (line.startsWith(" ")) {
                    line = line.substring(1);
                }
                text.append(line);
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    /*
    // net.minecraft.server.level.EntityPlayer
    public static void setSkin(Player p, String name) {

        //CLASS_PlayerConnection.getPackage().get

        //GameProfileMirror profile = new GameProfileMirror(p.getUniqueId(), null);

        // Run async to not crash or delay server
        new BukkitRunnable() {
            @Override
            public void run() {
                Gson gson = new Gson();

                // Get the player by name UUID
                String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
                String json = getStringFromURL(url);
                String uuid = gson.fromJson(json, JsonObject.class).get("id").getAsString();

                // Get the player skin data by UUID
                url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";
                json = getStringFromURL(url);
                JsonObject mainObject = gson.fromJson(json, JsonObject.class);
                JsonObject jObject = mainObject.get("properties").getAsJsonArray().get(0).getAsJsonObject();
                String value = jObject.get("value").getAsString();
                String sig = jObject.get("signature").getAsString();

                //profile.putProperty("textures", new PropertyMirror("textures", value, sig));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        EntityPlayer nmPlayer = ((CraftPlayer)p).getHandle();
                        PlayerConnection con = nmPlayer.b;

                        // In 1.18.1
                        // void a(Packet<?> packet)

                        // Remove old skin packet
                        con.a(new PacketPlayOutPlayerInfo(
                                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e,
                                nmPlayer));

                        // In 1.18.1
                        // GameProfile fp()

                        // Set new skin
                        GameProfile profile = nmPlayer.fp();
                        profile.getProperties().removeAll("textures");
                        profile.getProperties().put("textures", new Property("textures", value, sig));

                        // Add new skin packet
                        con.a(new PacketPlayOutPlayerInfo(
                                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a,
                                nmPlayer));

                        //p.hidePlayer(p);
                        p.hidePlayer(Main.getInstance(), p);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                p.showPlayer(Main.getInstance(), p);
                            }
                        }.runTaskLater(Main.getInstance(), 1);
                    }
                }.runTask(Main.getInstance());
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
     */
}
