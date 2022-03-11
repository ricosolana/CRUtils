package com.crazicrafter1.crutils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public enum Util {
    ;

    private static final Pattern SEMVER_PATTERN = Pattern.compile("[0-9]+(\\.[0-9]+)+");

    public static void give(Player p, ItemStack item) {
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
        }.runTask(Main.getInstance());
    }

    public static String replace(String find, String replaceWith, char d) {
        return find.replace(d + find + d, replaceWith);
    }

    public static String strDef(@Nullable String value, @Nonnull String defaultValue) {
        return value == null || value.isEmpty()
                ? defaultValue : value;
    }

    public static <T> T def(@Nullable T value, @Nonnull T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Set placeholders of a string using PAPI
     * PAPI-safe (plugin doesnt have to exist)
     * @param p player
     * @param text text
     * @return placeholder text
     */
    @Nonnull
    public static String placeholders(@Nullable Player p, @Nonnull String text) {
        if (p != null && Main.getInstance().supportPlaceholders) {
            return PlaceholderAPI.setPlaceholders(p, text);
        }
        return text;
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

    public static boolean zip(File in, File out) {
        /// Only if a backup failed for an existing File, then FAIL
        try {
            if (!(in.exists() && in.isFile()))
                return true;

            out.getParentFile().mkdirs();

            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(out));
            zipOut.putNextEntry(new ZipEntry(in.getName()));

            byte[] bytes = Files.readAllBytes(in.toPath());
            zipOut.write(bytes, 0, bytes.length);

            zipOut.close();

            return true;
        } catch (Exception e) {}

        return false;
    }

    public static boolean outdatedSemver(String baseVersion, String otherVersion) {
        if (baseVersion.equals(otherVersion))
            return false;

        Matcher baseMatcher = SEMVER_PATTERN.matcher(baseVersion);
        Matcher otherMatcher = SEMVER_PATTERN.matcher(otherVersion);
        //if (baseMatcher.matches() && otherMatcher.matches()) {
        if (baseMatcher.find() && otherMatcher.find()) {
            baseVersion = baseVersion.substring(baseMatcher.start(), baseMatcher.end());
            otherVersion = otherVersion.substring(otherMatcher.start(), otherMatcher.end());

            List<Integer> baseSemver = Arrays.stream(baseVersion.split("\\.")).map(Integer::parseInt).collect(Collectors.toList());
            List<Integer> otherSemver = Arrays.stream(otherVersion.split("\\.")).map(Integer::parseInt).collect(Collectors.toList());

            // expand one to the other
            int diff = baseSemver.size() - otherSemver.size();
            if (diff < 0) {
                baseSemver.addAll(Collections.nCopies(-diff, 0));
            } else if (diff > 0)
                otherSemver.addAll(Collections.nCopies(diff, 0));

            for (int i = 0; i < baseSemver.size(); i++) {
                if (baseSemver.get(i) < otherSemver.get(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    // TODO untested
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

    public static GameProfile makeGameProfile(String b64) {
        // random uuid based on the b64 string
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );

        GameProfile profile = new GameProfile(id, "aaaaa");
        profile.getProperties().put("textures", new Property("textures", b64, null));
        return profile;
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
        try {
            CLASS_CraftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
            METHOD_getHandle = ReflectionUtil.getMethod(CLASS_CraftPlayer, "getHandle");
            CLASS_EntityPlayer = METHOD_getHandle.getReturnType();
            FIELD_playerConnection = ReflectionUtil.findField(CLASS_EntityPlayer, "PlayerConnection");
            // get the PlayerConnection
            CLASS_PlayerConnection = FIELD_playerConnection.getType();
            //CLASS_Packet = ReflectionUtil.getNMClass("");
            //METHOD_sendPacket = ReflectionUtil.getMethod(CLASS_PlayerConnection, "sendPacket", CLASS_Packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
