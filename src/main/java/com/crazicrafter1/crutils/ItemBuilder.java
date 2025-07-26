package com.crazicrafter1.crutils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"UnusedReturnValue", "unused", "unchecked"})
public class ItemBuilder implements ConfigurationSerializable {

    static final Class<?> CLASS_CraftMetaSkull = ReflectionUtil.getCraftBukkitClass("inventory.CraftMetaSkull");
    static final Field FIELD_profile = ReflectionUtil.getField(CLASS_CraftMetaSkull, "profile");

    private static final BiMap<String, Enchantment> ENCHANTMENTS = HashBiMap.create();
    private static final BiMap<String, PotionEffectType> EFFECTS = HashBiMap.create();

    private static final String PLAYER_HEAD = "PLAYER_HEAD";

    static {
        for (Field field : Enchantment.class.getDeclaredFields()) {
            if (field.getType() == Enchantment.class) {
                ENCHANTMENTS.put(field.getName(), (Enchantment) ReflectionUtil.getFieldInstance(field, null));
            }
        }
        for (Field field : PotionEffectType.class.getDeclaredFields()) {
            if (field.getType() == PotionEffectType.class) {
                EFFECTS.put(field.getName(), (PotionEffectType) ReflectionUtil.getFieldInstance(field, null));
            }
        }
    }

    @Nonnull
    public static ItemBuilder deserialize(Map<String, Object> map) {
        try {
            ItemBuilder builder = ItemBuilder.from((String) map.get("type"))
                    .amount((int) map.getOrDefault("amount", 1))
                    .name((String) map.get("name"), ColorUtil.RENDER_MARKERS)
                    .lore((List<String>) map.get("lore"), ColorUtil.RENDER_MARKERS);

            String skull64 = (String) map.get("skull");
            if (skull64 != null) builder.skull(skull64);

            int model = (int) map.getOrDefault("model", -1);
            if (model != -1) builder.model(model);

            // will work for enchanted books too
            List<Map<String, Object>> enchantmentList = (List<Map<String, Object>>) map.get("enchantments");
            if (enchantmentList != null) {
                for (Map<String, Object> mapEntry : enchantmentList) {
                    String e = (String) mapEntry.get("name");
                    Enchantment enchantment = Objects.requireNonNull(ENCHANTMENTS.get(e), "Enchantment " + e + " does not exist");
                    int level = (int) mapEntry.getOrDefault("level", 1);
                    builder.enchant(enchantment, level);
                }
            }

            // potion
            List<Map<String, Object>> effectList = (List<Map<String, Object>>) map.get("effects");
            if (effectList != null) {
                for (Map<String, Object> mapEntry : effectList) {
                    String e = (String) mapEntry.get("name");
                    PotionEffectType effect = Objects.requireNonNull(EFFECTS.get(e), "PotionEffectType " + e + " does not exist");
                    Integer duration = (Integer) mapEntry.get("duration");
                    int amplifier = (int) mapEntry.getOrDefault("amplifier", 0);
                    if (duration != null) builder.effect(new PotionEffect(effect, duration, amplifier));
                }
            }
            return builder;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deserializing ItemBuilder: " + e.getMessage());
        }
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        Object temp;

        map.put("type", getModernMaterial());
        if ((int)(temp = getAmount()) > 1) map.put("amount", temp);
        if ((temp = getName()) != null) map.put("name", ColorUtil.invertRendered((String)temp));
        if ((temp = getLoreList()) != null) map.put("lore", ((List<String>)temp).stream().map(ColorUtil::invertRendered).collect(Collectors.toList()));

        if ((temp = getSkull()) != null) map.put("skull", temp);
        if ((int)(temp = getModel()) != -1) map.put("model", temp);

        List<Map<String, Object>> subAdd = new ArrayList<>();
        BiMap<Enchantment, String> inverse1 = ENCHANTMENTS.inverse();
        for (Map.Entry<Enchantment, Integer> entry : getEnchants().entrySet()) {
            Map<String, Object> sub = new LinkedHashMap<>();
            sub.put("name", inverse1.get(entry.getKey()));
            sub.put("level", entry.getValue());
            subAdd.add(sub);
        }
        if (!subAdd.isEmpty()) map.put("enchantments", subAdd);

        subAdd = new ArrayList<>();
        BiMap<PotionEffectType, String> inverse2 = EFFECTS.inverse();
        List<PotionEffect> effects = getEffects();
        if (effects != null) for (PotionEffect effect : effects) {
            Map<String, Object> sub = new LinkedHashMap<>();
            sub.put("name", inverse2.get(effect.getType()));
            sub.put("amplifier", effect.getAmplifier());
            sub.put("duration", effect.getDuration());
            subAdd.add(sub);
        }
        if (!subAdd.isEmpty()) map.put("effects", subAdd);

        return map;
    }

    private final ItemStack itemStack;

    private ItemBuilder(ItemStack itemStack) {
        Validate.isTrue(itemStack.getType() != Material.AIR, "Cannot build from minecraft:air");
        Validate.isTrue(itemStack.getItemMeta() != null, "Cannot build from null");
        this.itemStack = itemStack;
    }

    public static ItemBuilder copy(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static ItemBuilder copy(ItemStack itemStack) {
        return new ItemBuilder(new ItemStack(itemStack));
    }

    public static ItemBuilder copy(ItemBuilder builder) {
        return copy(builder.build());
    }

    public static ItemBuilder mut(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public static ItemBuilder mut(ItemBuilder builder) {
        return mut(builder.build());
    }



    /**
     * Version safe item resolver
     * @param modern Name of the item in 1.20.1
     * @throws IllegalArgumentException if an item cannot be matched
     */
    @SuppressWarnings("deprecation")
    @Nonnull
    public static ItemBuilder from(String modern) {
        modern = modern.toUpperCase();

        Material material = Material.matchMaterial(modern);

        if (material == null) {
            if (Version.AT_MOST_v1_12.a()) {
                if (modern.equals("INK_SAC"))          // TODO misnomer
                    return copy(Material.matchMaterial("INK_SACK"));

                for (Map.Entry<Integer, List<String>> entry : AT_MOST_v1_12_TO_MODERN_MAP.entrySet()) {
                    int index = entry.getValue().indexOf(modern);
                    if (index != -1) {
                        material = Material.matchMaterial("" + entry.getKey());

                        if (material != null) return copy(new ItemStack(material, 1, (short) index));
                    }
                }
            } else if (Version.v1_13.a()) {
                String LEGACY_NAME = v1_13_TO_MODERN_MAP.inverse().get(modern);

                if (LEGACY_NAME != null) material = Material.matchMaterial(LEGACY_NAME);
            }
        }

        if (material != null)
            return copy(material);

        throw new IllegalArgumentException("Material " + modern + " does not exist ");
    }

    /**
     * Construct a firework star with a FireworkEffect
     * @param effect the effect
     * @return new instance
     */
    public static ItemBuilder fromStar(FireworkEffect effect) {
        return ItemBuilder.from("FIREWORK_STAR").star(effect);
    }

    public static ItemBuilder fromSkull(@Nonnull String base64) {
        return ItemBuilder.from(PLAYER_HEAD).skull(base64);
    }



    /**
     * Return a copy of this
     * @return {@link ItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ItemBuilder copy() {
        return copy(this);
    }

    /**
     * Set the meta of this
     * @param meta {@link ItemMeta} instance
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder meta(@Nonnull ItemMeta meta) {
        itemStack.setItemMeta(Objects.requireNonNull(meta));
        return this;
    }

    /**
     * Apply a language translation filter using Google Translate
     * to the name and lore of this
     * @param from from language
     * @param to to language
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    @Deprecated
    public ItemBuilder transcribe(@Nonnull String from, @Nonnull String to) {
        GoogleTranslate G = new GoogleTranslate();

        String name = getName();
        String lore = getLoreString();

        if (name != null)
            name(G.translate(name, from, to));
        if (lore != null)
            return lore(G.translate(lore, from, to));

        return this;
    }

    public static final int FLAG_NAME           = 0b1;
    public static final int FLAG_NAME_FORCE     = 0b10;
    public static final int FLAG_LORE           = 0b100;
    public static final int FLAG_LORE_FORCE     = 0b1000;
    public static final int FLAG_SKULL          = 0b10000;
    public static final int FLAG_MATERIAL       = 0b100000;

    /**
     * Apply attributes of other to this
     * @param otherItemStack {@link ItemStack} instance
     * @return {@link ItemBuilder} copy
     */
    public ItemBuilder apply(@Nonnull ItemStack otherItemStack) {
        return apply(otherItemStack, FLAG_NAME | FLAG_LORE);
    }

    //TODO Find a more suitable name for this
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    public ItemBuilder apply(@Nonnull ItemStack otherItemStack, int flags) {
        ItemMeta other = otherItemStack.getItemMeta();
        if (((flags & FLAG_NAME) == FLAG_NAME && other.hasDisplayName()) || (flags & FLAG_NAME_FORCE) == FLAG_NAME_FORCE)
            this.name(other.getDisplayName(), ColorUtil.AS_IS);

        if (((flags & FLAG_LORE) == FLAG_LORE && other.hasLore()) || (flags & FLAG_LORE_FORCE) == FLAG_LORE_FORCE)
            lore(other.getLore(), ColorUtil.AS_IS);

        // determine material
        if (((flags & FLAG_MATERIAL) == FLAG_MATERIAL)) {
            material(otherItemStack.getType());
            if (Version.AT_MOST_v1_13.a()) // TODO this seems very unsafe, but is uncommon
                itemStack.setDurability(otherItemStack.getDurability());
        }

        if (((flags & FLAG_SKULL) == FLAG_SKULL) && getMeta() instanceof SkullMeta)
            skull(ItemBuilder.from(PLAYER_HEAD).meta(other).getSkull());

        return this;
    }

    /**
     * Apply the name and lore of {@link ItemStack} to this
     * @param builder {@link ItemBuilder} instance
     * @return {@link ItemBuilder} this
     */
    //TODO Find a more suitable name for this
    @Nonnull
    public ItemBuilder apply(@Nonnull ItemBuilder builder) {
        return apply(builder.itemStack);
    }



    /**
     * Transfer the item name to the lore
     * @return {@link ItemBuilder} this
     */
    //TODO This function should be restructured and renamed
    @Nonnull
    @Deprecated
    public ItemBuilder name2Lore() {
        return name2Lore(null, ColorUtil.RENDER_MARKERS);
    }

    /**
     * Transfer the item name to the lore
     * @param mode {@link ColorUtil} mode
     * @return {@link ItemBuilder} this
     */
    //TODO This function should be restructured and renamed
    @Nonnull
    @Deprecated
    public ItemBuilder name2Lore(ColorUtil mode) {
        return name2Lore(null, ColorUtil.RENDER_MARKERS);
    }

    /**
     * Transfer the item name to the lore, with optional prepend
     * @param prepend before text
     * @return {@link ItemBuilder} this
     */
    //TODO This function should be restructured and renamed
    @Nonnull
    @Deprecated
    public ItemBuilder name2Lore(@Nullable String prepend) {
        return name2Lore(prepend, ColorUtil.RENDER_MARKERS);
    }

    /**
     * Transfer the item name to the lore, with optional prepend
     * @param prepend before text
     * @param mode {@link ColorUtil} mode
     * @return {@link ItemBuilder} this
     */
    //TODO This function should be restructured and renamed
    @Nonnull
    @Deprecated
    public ItemBuilder name2Lore(@Nullable String prepend, ColorUtil mode) {
        String name = getName();

        if (name != null)
            lore(prepend != null ? prepend : "" + name, mode);

        return this;
    }

    /**
     * Apply a {@link PotionEffect} to this
     * @param effect {@link PotionEffect} instance
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder effect(@Nonnull PotionEffect effect) {
        PotionMeta meta = (PotionMeta) getMeta();
        meta.addCustomEffect(effect, true);
        return meta(meta);
    }

    /**
     * Apply an {@link Enchantment} with level to this
     * @param enchantment {@link Enchantment} instance
     * @param level int level
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder enchant(@Nonnull Enchantment enchantment, int level) {
        ItemMeta meta = getMeta();
        if (meta instanceof EnchantmentStorageMeta) {
            ((EnchantmentStorageMeta) meta).addStoredEnchant(enchantment, level, true);
        } else
            meta.addEnchant(enchantment, level, true);
        return meta(meta);
    }

    /**
     * Apply skull data to this
     * @param base64 {@link String}
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder skull(@Nonnull String base64) {
        SkullMeta meta = (SkullMeta) getMeta();
        // TODO look into setOwner with OfflinePlayer (avoid reflections)
        //meta.setOwningPlayer(...);
        //final ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        //https://www.spigotmc.org/threads/custom-head-textures-%E2%80%94-solved.663443/

        // appears that added in 1.18.1 according to DOCS
        //  so, largely experimental, but at this point, who cares, mojang switching method signatures
        //  every fkn update make kermit..
        if (Version.AT_LEAST_v1_19.a()) {
            final UUID uuid = UUID.randomUUID();
            final PlayerProfile playerProfile = Bukkit.createPlayerProfile(uuid, uuid.toString().substring(0, 16));
            //playerProfile.setTextures(PlayerTextures("textures", textures));

            // {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/18a444c8e88fd3ae193c5e0d486274c5cb76701bbf395784d9031510657aec8f"}}}

            PlayerTextures textures = playerProfile.getTextures();

            URL url;
            try {
                String json_string = new String(Base64.getDecoder().decode(base64));
                String url_string = JsonParser.parseString(json_string).getAsJsonObject()
                        .getAsJsonObject("textures")
                        .getAsJsonObject("SKIN")
                        .getAsJsonPrimitive("url")
                        .getAsString();

                url = new URL(url_string);
            } catch (Exception e) {
                // fk
                throw new RuntimeException(e);
            }

            textures.setSkin(url);

            meta.setOwnerProfile(playerProfile);
        } else {
            ReflectionUtil.setFieldInstance(FIELD_profile, meta, Util.makeGameProfile(base64));
        }
        return meta(meta);
    }

    /**
     * Set the CustomModelData of @this
     * Fails silently on versions before 1.14.4
     * @param i data
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder model(@Nullable Integer i) {
        if (Version.AT_LEAST_v1_14.a()) {
            ItemMeta meta = getMeta();
            meta.setCustomModelData(i);
            return meta(meta);
        }
        return this;
    }

    /**
     * Apply a material type to this
     * @param material {@link Material} instance
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder material(Material material) {
        itemStack.setType(material);
        return this;
    }

    public ItemBuilder material(String modern) {
        ItemBuilder itemBuilder = from(modern);
        material(itemBuilder.getMaterial());
        if (Version.AT_MOST_v1_13.a()) {
            //noinspection deprecation
            itemStack.setDurability(itemBuilder.itemStack.getDurability());
        }
        return this;
    }

    /**
     * Apply a color coded name to this
     * If name is null, reset name
     * @param name {@link String} instance
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder name(@Nullable String name) {
        return this.name(name, ColorUtil.RENDER_ALL);
    }

    @Nonnull
    public ItemBuilder name(@Nullable String name, @Nonnull ColorUtil mode) {
        return name(name, mode, null);
    }

    @Nonnull
    public ItemBuilder name(@Nullable String name, @Nonnull ColorUtil mode, @Nullable String prependIfNonNull) {
        if (name == null) return this;

        ItemMeta meta = getMeta();

        name = mode.a(name);

        if (prependIfNonNull != null)
            name = prependIfNonNull + name;

        meta.setDisplayName(name);
        return meta(meta);
    }

    /**
     * Reset the name of this
     * @return {@link ItemBuilder} copy
     */
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    public ItemBuilder removeName() {
        ItemMeta meta = getMeta();
        meta.setDisplayName(null);
        return meta(meta);
        //return name(null, null);
    }



    /**
     * Add an item lore
     * Defaults:
     *  - Simple color coding
     *  - Newlines are transposed
     *  - Lore is removed if null
     * todo should make null throw or do nothing instead
     *      because adding and removing a lore should be clearly different
     * @param lore {@link String} instance
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder lore(@Nullable String lore) {
        return this.lore(lore, ColorUtil.RENDER_ALL, null);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable String lore, @Nonnull ColorUtil mode) {
        return this.lore(lore, mode, null);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable String lore, @Nonnull ColorUtil mode, @Nullable String perLineFormat) {
        return lore(lore == null
                ? null
                : Arrays.asList(lore.split("\n")), mode, perLineFormat);
    }



    @Nonnull
    public ItemBuilder lore(@Nullable String[] lore) {
        return lore(lore, ColorUtil.RENDER_ALL, null);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable String[] lore, @Nonnull ColorUtil mode) {
        return lore(lore, mode, null);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable String[] lore, @Nonnull ColorUtil mode, @Nullable String perLineFormat) {
        return lore(lore == null
                ? null
                : Arrays.asList(lore), mode, perLineFormat);
    }



    @Nonnull
    public ItemBuilder lore(@Nullable List<String> lore) {
        return lore(lore, ColorUtil.RENDER_ALL, null);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable List<String> lore, @Nonnull ColorUtil mode) {
        return lore(lore, mode, null);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable List<String> lore, @Nonnull ColorUtil mode, @Nullable String perLineFormat) {
        if (lore == null) return this;

        ItemMeta meta = getMeta();

        if (perLineFormat != null) lore.replaceAll(s -> String.format(perLineFormat, mode.a(s)));
        else lore.replaceAll(mode::a);

        meta.setLore(lore);

        return meta(meta);
    }

    /**
     * Remove the item lore
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder removeLore() {
        ItemMeta meta = getMeta();
        meta.setLore(null);
        return meta(meta);
    }

    /**
     * Apply a text substitution to the name and lore of the item
     * @param findValue what to replace
     * @param newValue the replacement text
     * @param delim delimiter %
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder replace(@Nonnull String findValue, @Nonnull String newValue, char delim) {
        String name = getName();
        if (name != null)
            name(name.replace(delim + findValue + delim, newValue), ColorUtil.AS_IS);

        List<String> lore = getLoreList();
        if (lore != null) {
            // manually replaces all macros
            lore.replaceAll(s -> s.replace(delim + findValue + delim, newValue));
            lore(lore, ColorUtil.AS_IS);
        }

        return this;
    }

    /**
     * Apply placeholders to the name and lore of this
     * @param p {@link Player} instance
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder placeholders(@Nullable Player p) {
        if (p != null && Main.getInstance().supportPlaceholders) {
            String name = getName();
            if (name != null)
                name(PlaceholderAPI.setPlaceholders(
                        p, name));

            List<String> lore = getLoreList();
            if (lore != null) {
                lore.replaceAll(text -> PlaceholderAPI.setPlaceholders(
                        p, text));
                return lore(lore);
            }
        }
        return this;
    }

    /**
     * Apply a color to this (for leather armor, potion, etc)
     * @param r red component
     * @param g green component
     * @param b blue component
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder color(int r, int g, int b) {
        return this.color(Color.fromRGB(r, g, b));
    }

    /**
     * Apply a color to this (for leather armor, potion, etc)
     * @param color {@link Color} instance
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder color(Color color) {
        ItemMeta meta = getMeta();

        if (meta instanceof PotionMeta) {
            ((PotionMeta)meta).setColor(color);
        } else if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta)meta).setColor(color);
        }
        return meta(meta);
    }

    /**
     * Set the amount of this
     * @param amount amount
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Makes this unbreakable
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder unbreakable() {
        ItemMeta meta = getMeta();
        meta.setUnbreakable(true);
        return meta(meta);
    }

    /**
     * Hide flags of this
     * @param flags the flags
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder hideFlags(ItemFlag ... flags) {
        ItemMeta meta = getMeta();
        meta.addItemFlags(flags);
        return meta(meta);
    }

    /**
     * Make an item look like it's enchanted
     * @param glint whether to glint
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder glow(boolean glint) {
        if (glint) {
            ItemMeta meta = getMeta();
            meta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
            return meta(meta).hideFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    /**
     * Apply a Firework Effect to this Firework Star
     * @param effect the effect
     * @return this
     */
    @Nonnull
    public ItemBuilder star(FireworkEffect effect) {
        FireworkEffectMeta meta = (FireworkEffectMeta) getMeta();
        meta.setEffect(effect);
        itemStack.setItemMeta(meta);
        return this;
    }

    @Nonnull
    @Deprecated
    public ItemBuilder fireworkEffect(FireworkEffect effect) {
        return star(effect);
    }


    /* ************************************************************ *
     *
     *
     *
     *      Accessors and getters
     *
     *
     *
     * ************************************************************ */

    /**
     * Get the Material of this
     * @return {@link Material} copy
     */
    @CheckReturnValue
    @Nonnull
    public Material getMaterial() {
        return itemStack.getType();
    }

    /**
     * Get the ItemMeta of this
     * @return {@link ItemMeta} copy
     */
    @CheckReturnValue
    public ItemMeta getMeta() {
        return itemStack.getItemMeta();
    }

    /**
     * Get the name of this
     * @return {@link String} copy
     */
    @CheckReturnValue
    @Nullable
    public String getName() {
        ItemMeta meta = getMeta();
        if (meta.hasDisplayName())
            return meta.getDisplayName();
        return null;
    }

    /**
     * Get an approximated locale name (not really locale, just Material properly formatted)
     * @return {@link Material} copy
     */
    @CheckReturnValue
    @Nonnull
    public String getLocaleName() {
        return WordUtils.capitalizeFully(
                getModernMaterial().toLowerCase().replace('_', ' '), '.');
        //return Util.punctuateAndGrammar(getMaterial());
    }

    /**
     * Get the Material of this
     * @return {@link Material} copy
     */
    @CheckReturnValue
    @Nonnull
    public String getNameOrLocaleName() {
        return Util.def(getName(), getLocaleName());
    }

    /**
     * Get the lore of this
     * @return {@link String} copy
     */
    @CheckReturnValue
    @Nullable
    public String getLoreString() {
        List<String> lore = getMeta().getLore();
        if (lore == null) return null;
        return String.join("\n", lore);
    }

    /**
     * Get the lore of this
     * @return {@link List<String>} copy
     */
    @CheckReturnValue
    @Nullable
    public List<String> getLoreList() {
        return getMeta().getLore();
    }

    /**
     * Get the lore of this
     * @return {@link String[]} copy
     */
    @SuppressWarnings("ConstantConditions")
    @CheckReturnValue
    @Nullable
    public String[] getLoreArray() {
        return getMeta().getLore().toArray(new String[0]);
    }


    /**
     * Get the amount of this
     * @return Amount
     */
    @CheckReturnValue
    public int getAmount() {
        return itemStack.getAmount();
    }

    /**
     * Get the enchantments of this
     * @return
     */
    @CheckReturnValue
    @Nonnull
    public Map<Enchantment, Integer> getEnchants() {
        ItemMeta meta = getMeta();
        if (meta instanceof EnchantmentStorageMeta)
            return ((EnchantmentStorageMeta) meta).getStoredEnchants();
        return meta.getEnchants();
    }

    @CheckReturnValue
    @Nullable
    public String getSkull() {
        try {
            SkullMeta meta = (SkullMeta) getMeta();
            GameProfile profile = (GameProfile) ReflectionUtil.getFieldInstance(FIELD_profile, meta);

            return profile.getProperties().get("textures").stream().findFirst().get().getValue();
        } catch (Exception ignored) {}
        return null;
    }

    @CheckReturnValue
    public int getModel() {
        ItemMeta meta = getMeta();
        //customModelData does not exist in 1.12.2
        if (Version.AT_LEAST_v1_14.a() && meta.hasCustomModelData())
            return meta.getCustomModelData();
        return -1;
    }

    @Nullable
    @CheckReturnValue
    public List<PotionEffect> getEffects() {
        try {
            PotionMeta meta = (PotionMeta) getMeta();
            return meta.getCustomEffects();
        } catch (Exception ignored) {}
        return null;
    }

    @CheckReturnValue
    public int getMaxSize() {
        return itemStack.getMaxStackSize();
    }



    private static final Map<Integer, List<String>> AT_MOST_v1_12_TO_MODERN_MAP = new HashMap<>();
    private static void addAll_v1_12(Object... values) {
        for (int i=0; i < values.length; i++) {
            int id = (int) values[i];
            List<String> MODERN = new ArrayList<>();
            i++;
            for (; i < values.length; i++) {
                MODERN.add((String) values[i]);

                if (i+1 == values.length || values[i+1] instanceof Integer)
                    break;
            }
            AT_MOST_v1_12_TO_MODERN_MAP.put(id, MODERN);
        }
    }

    private static final BiMap<String, String> v1_13_TO_MODERN_MAP = HashBiMap.create();
    private static void addAll_v1_13(String... values) {
        for (int i=0; i < values.length; i+=2) {
            v1_13_TO_MODERN_MAP.put(values[i], values[i+1]);
        }
    }

    static {
        // 1.8 -> 1.12 CONVERTED TO 1.18
        addAll_v1_12(
                1, "STONE", "GRANITE", "POLISHED_GRANITE", "DIORITE", "POLISHED_DIORITE", "ANDESITE", "POLISHED_ANDESITE",
                3, "DIRT", "COARSE_DIRT", "PODZOL",
                5, "OAK_PLANKS", "SPRUCE_PLANKS", "BIRCH_PLANKS", "JUNGLE_PLANKS", "ACACIA_PLANKS", "DARK_OAK_PLANKS",
                6, "OAK_SAPLING", "SPRUCE_SAPLING", "BIRCH_SAPLING", "JUNGLE_SAPLING", "ACACIA_SAPLING", "DARK_OAK_SAPLING",
                12, "SAND", "RED_SAND",
                17, "OAK_LOG", "SPRUCE_LOG", "BIRCH_LOG", "JUNGLE_LOG",
                18, "OAK_LEAVES", "SPRUCE_LEAVES", "BIRCH_LEAVES", "JUNGLE_LEAVES",
                19, "SPONGE", "WET_SPONGE",
                24, "SANDSTONE", "CHISELED_SANDSTONE", "SMOOTH_SANDSTONE",
                31, "GRASS", "FERN",
                35, "WHITE_WOOL", "ORANGE_WOOL", "MAGENTA_WOOL", "LIGHT_BLUE_WOOL", "YELLOW_WOOL", "LIME_WOOL", "PINK_WOOL", "GRAY_WOOL", "LIGHT_GRAY_WOOL", "CYAN_WOOL", "PURPLE_WOOL", "BLUE_WOOL", "BROWN_WOOL", "GREEN_WOOL", "RED_WOOL", "BLACK_WOOL",
                37, "DANDELION",
                38, "POPPY", "BLUE_ORCHID", "ALLIUM", "AZURE_BLUET", "RED_TULIP", "ORANGE_TULIP", "WHITE_TULIP", "PINK_TULIP", "OXEYE_DAISY",
                44, "SMOOTH_STONE_SLAB", "SANDSTONE_SLAB", "COBBLESTONE_SLAB", "BRICK_SLAB", "STONE_BRICK_SLAB", "NETHER_BRICK_SLAB", "QUARTZ_SLAB",
                95, "WHITE_STAINED_GLASS", "ORANGE_STAINED_GLASS", "MAGENTA_STAINED_GLASS", "LIGHT_BLUE_STAINED_GLASS", "YELLOW_STAINED_GLASS", "LIME_STAINED_GLASS", "PINK_STAINED_GLASS", "GRAY_STAINED_GLASS", "LIGHT_GRAY_STAINED_GLASS", "CYAN_STAINED_GLASS", "PURPLE_STAINED_GLASS", "BLUE_STAINED_GLASS", "BROWN_STAINED_GLASS", "GREEN_STAINED_GLASS", "RED_STAINED_GLASS", "BLACK_STAINED_GLASS",
                97, "INFESTED_STONE", "INFESTED_COBBLESTONE", "INFESTED_STONE_BRICKS", "INFESTED_MOSSY_STONE_BRICKS", "INFESTED_CRACKED_STONE_BRICKS", "INFESTED_CHISELED_STONE_BRICKS",
                98, "STONE_BRICKS", "MOSSY_STONE_BRICKS", "CRACKED_STONE_BRICKS", "CHISELED_STONE_BRICKS",
                126, "OAK_SLAB", "SPRUCE_SLAB", "BIRCH_SLAB", "JUNGLE_SLAB", "ACACIA_SLAB", "DARK_OAK_SLAB",
                139, "COBBLESTONE_WALL", "MOSSY_COBBLESTONE_WALL",
                145, "ANVIL", "CHIPPED_ANVIL", "DAMAGED_ANVIL",
                155, "QUARTZ_BLOCK", "CHISELED_QUARTZ_BLOCK", "QUARTZ_PILLAR",
                159, "WHITE_TERRACOTTA", "ORANGE_TERRACOTTA", "MAGENTA_TERRACOTTA", "LIGHT_BLUE_TERRACOTTA", "YELLOW_TERRACOTTA", "LIME_TERRACOTTA", "PINK_TERRACOTTA", "GRAY_TERRACOTTA", "LIGHT_GRAY_TERRACOTTA", "CYAN_TERRACOTTA", "PURPLE_TERRACOTTA", "BLUE_TERRACOTTA", "BROWN_TERRACOTTA", "GREEN_TERRACOTTA", "RED_TERRACOTTA", "BLACK_TERRACOTTA",
                160, "WHITE_STAINED_GLASS_PANE", "ORANGE_STAINED_GLASS_PANE", "MAGENTA_STAINED_GLASS_PANE", "LIGHT_BLUE_STAINED_GLASS_PANE", "YELLOW_STAINED_GLASS_PANE", "LIME_STAINED_GLASS_PANE", "PINK_STAINED_GLASS_PANE",  "GRAY_STAINED_GLASS_PANE", "LIGHT_GRAY_STAINED_GLASS_PANE", "CYAN_STAINED_GLASS_PANE", "PURPLE_STAINED_GLASS_PANE", "BLUE_STAINED_GLASS_PANE", "BROWN_STAINED_GLASS_PANE", "GREEN_STAINED_GLASS_PANE", "RED_STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE",
                161, "ACACIA_LEAVES", "DARK_OAK_LEAVES",
                162, "ACACIA_LOG", "DARK_OAK_LOG",
                168, "PRISMARINE", "PRISMARINE_BRICKS", "DARK_PRISMARINE",
                171, "WHITE_CARPET", "ORANGE_CARPET", "MAGENTA_CARPET", "LIGHT_BLUE_CARPET", "YELLOW_CARPET", "LIME_CARPET", "PINK_CARPET", "GRAY_CARPET", "LIGHT_GRAY_CARPET", "CYAN_CARPET", "PURPLE_CARPET", "BLUE_CARPET", "BROWN_CARPET", "GREEN_CARPET", "RED_CARPET", "BLACK_CARPET",
                175, "SUNFLOWER", "LILAC", "TALL_GRASS", "LARGE_FERN", "ROSE_BUSH", "PEONY",
                179, "RED_SANDSTONE", "CHISELED_RED_SANDSTONE", "SMOOTH_RED_SANDSTONE",
                //182, "RED_SANDSTONE_SLAB",
                //205, "PURPUR_SLAB",
                251, "WHITE_CONCRETE", "ORANGE_CONCRETE", "MAGENTA_CONCRETE", "LIGHT_BLUE_CONCRETE", "YELLOW_CONCRETE", "LIME_CONCRETE", "PINK_CONCRETE", "GRAY_CONCRETE", "LIGHT_GRAY_CONCRETE", "CYAN_CONCRETE", "PURPLE_CONCRETE", "BLUE_CONCRETE", "BROWN_CONCRETE", "GREEN_CONCRETE", "RED_CONCRETE", "BLACK_CONCRETE",
                252, "WHITE_CONCRETE_POWDER", "ORANGE_CONCRETE_POWDER", "MAGENTA_CONCRETE_POWDER", "LIGHT_BLUE_CONCRETE_POWDER", "YELLOW_CONCRETE_POWDER", "LIME_CONCRETE_POWDER", "PINK_CONCRETE_POWDER", "GRAY_CONCRETE_POWDER", "LIGHT_GRAY_CONCRETE_POWDER", "CYAN_CONCRETE_POWDER", "PURPLE_CONCRETE_POWDER", "BLUE_CONCRETE_POWDER", "BROWN_CONCRETE_POWDER", "GREEN_CONCRETE_POWDER", "RED_CONCRETE_POWDER", "BLACK_CONCRETE_POWDER",
                263, "COAL", "CHARCOAL",
                322, "GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE",
                349, null, "SALMON", null, "PUFFERFISH", "",        // TODO fallback instead of null?
                350, null, "COOKED_SALMON",
                351, "BLACK_DYE", "RED_DYE", "GREEN_DYE", "BROWN_DYE", "BLUE_DYE", "PURPLE_DYE", "CYAN_DYE", "LIGHT_GRAY_DYE", "GRAY_DYE", "PINK_DYE", "LIME_DYE", "YELLOW_DYE", "LIGHT_BLUE_DYE", "MAGENTA_DYE", "ORANGE_DYE", "WHITE_DYE",
                355, "WHITE_BED", "ORANGE_BED", "MAGENTA_BED", "LIGHT_BLUE_BED", "YELLOW_BED", "LIME_BED", "PINK_BED", "GRAY_BED", "LIGHT_GRAY_BED", "CYAN_BED", "PURPLE_BED", "BLUE_BED", "BROWN_BED", "GREEN_BED", "RED_BED", "BLACK_BED",
                //NBT 373, "POTIONS...",
                //NBT 383, "SPAWN_EGGS...",
                384, "EXPERIENCE_BOTTLE",
                386, "WRITABLE_BOOK",
                397, "SKELETON_SKULL", "WITHER_SKELETON_SKULL", "ZOMBIE_HEAD", PLAYER_HEAD, "CREEPER_HEAD", "DRAGON_HEAD",
                //NBT 403, "ENCHANTED_BOOKS..."
                401, "FIREWORK_ROCKET",
                402, "FIREWORK_STAR",
                404, "COMPARATOR",
                417, "IRON_HORSE_ARMOR",
                418, "GOLD_HORSE_ARMOR",
                419, "DIAMOND_HORSE_ARMOR",
                425, "BLACK_BANNER", "RED_BANNER", "GREEN_BANNER", "BROWN_BANNER", "BLUE_BANNER", "PURPLE_BANNER", "CYAN_BANNER", "LIGHT_GRAY_BANNER", "GRAY_BANNER", "PINK_BANNER", "LIME_BANNER", "YELLOW_BANNER", "LIGHT_BLUE_BANNER", "MAGENTA_BANNER", "ORANGE_BANNER", "WHITE_BANNER"
                //NBT 438, "SPLASH_POTIONS...",
                //NBT 440, "TIPPED_ARROWS...",
                //NBT 441, "LINGERING_POTIONS...",
        );

        addAll_v1_13(
                "ROSE_RED", "RED_DYE",
                "CACTUS_GREEN", "GREEN_DYE",
                "COCOA_BEANS", "BROWN_DYE",         // TODO misnomer
                "LAPIS_LAZULI", "BLUE_DYE",         // TODO misnomer
                "YELLOW_DYE", "DANDELION_YELLOW",
                "BONE_MEAL", "WHITE_DYE"            // TODO misnomer
        );
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    public String getModernMaterial() {
        final String NAME = getMaterial().name();

        if (Version.AT_MOST_v1_12.a()) {

            /// TODO
            // For INK_SACK, should INK_SAC or BLACK_DYE be returned?
            // this relation is 1:N, which is problematic

            // According to this process,
            // INK_SAC will be the result of INK_SACK
            // Not BLACK_DYE

            final int id = getMaterial().getId();
            final int dmg = itemStack.getDurability();

            List<String> MODERN_NAMES = AT_MOST_v1_12_TO_MODERN_MAP.get(id);

            if (MODERN_NAMES != null) return MODERN_NAMES.get(dmg);
        } else if (Version.v1_13.a()) {
            String get = v1_13_TO_MODERN_MAP.get(NAME);
            if (get != null) return get;
        }

        // else 1.14 and above
        return NAME;
    }

    @CheckReturnValue
    @Nonnull
    public ItemStack build() {
        return itemStack;
    }

    /**
     * Finalize the item
     * @return {@link ItemStack} built instance
     */
    @CheckReturnValue
    @Nonnull
    public ItemStack buildCopy() {
        return copy().build();
    }

    @CheckReturnValue
    @Nonnull
    public ItemBuilder renderAll() {
        return renderAll(true);
    }

    @CheckReturnValue
    @Nonnull
    public ItemBuilder renderAll(boolean renderAll) {
        if (renderAll) {
            name(getName(), ColorUtil.RENDER_ALL);
            return lore(getLoreList(), ColorUtil.RENDER_ALL);
        }
        return this;
    }

}
