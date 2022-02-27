package com.crazicrafter1.crutils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Mutqble itembuilder
 * changeable
 * return refwrences to this everywhere
 * high performancd
 */
public class ItemBuilder {

    private static final Class<?> CLASS_CraftMetaSkull = ReflectionUtil.getCraftBukkitClass("inventory.CraftMetaSkull");
    private static final Field FIELD_profile = ReflectionUtil.getField(CLASS_CraftMetaSkull, "profile");

    /**
     * Strictly static constant
     * immutanle class
     * copies only
     * will not transfer states across
     */
    public static class Immutable {
        private final ItemStack itemStack;

        public Immutable(Material material) {
            this.itemStack = new ItemStack(material);
        }

        public Immutable(ItemStack itemStack) {
            this.itemStack = new ItemStack(itemStack);
        }

        public Immutable(Immutable builder) {
            this.itemStack = builder.build();
        }

        /**
         * Return a copy of this
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable copy() {
            return new Immutable(this);
        }

        /**
         * Set the meta of this
         * @param meta {@link ItemMeta} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable meta(@Nonnull ItemMeta meta) {
            Immutable copy = copy();
            copy.itemStack.setItemMeta(Objects.requireNonNull(meta));
            return copy;
        }

        /**
         * Apply a language translation filter using Google Translate
         * to the name and lore of this
         * @param from from language
         * @param to to language
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable transcribe(@Nonnull String from, @Nonnull String to) {
            GoogleTranslate G = new GoogleTranslate();

            return name(G.translate(getName(), from, to))
                    .lore(G.translate(getLoreString(), from, to));
        }

        /**
         * Apply name and lore of other to this
         * @param other {@link ItemMeta} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable apply(@Nonnull ItemMeta other) {
            return name(other.getDisplayName()).lore(other.getLore(), false);
        }

        /**
         * Apply name and lore of other to this
         * @param other {@link ItemStack} instance
         * @return {@link Immutable} copy
         */
        @SuppressWarnings("ConstantConditions")
        @CheckReturnValue
        @Nonnull
        public Immutable apply(@Nonnull ItemStack other) {
            return apply(other.getItemMeta());
        }

        /**
         * Apply name and lore of other to this
         * @param other {@link Immutable} instance
         * @return {@link Immutable} copy
         */
        @SuppressWarnings("ConstantConditions")
        @CheckReturnValue
        @Nonnull
        public Immutable apply(@Nonnull Immutable other) {
            return apply(other.getMeta());
        }

        /**
         * Apply a {@link PotionEffect} to this
         * @param effect {@link PotionEffect} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable effect(@Nonnull PotionEffect effect) {
            PotionMeta meta = (PotionMeta) getMeta();
            meta.addCustomEffect(effect, true);
            return meta(meta);
        }

        /**
         * Apply an {@link Enchantment} with level to this
         * @param enchantment {@link Enchantment} instance
         * @param level int level
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable enchant(@Nonnull Enchantment enchantment, int level) {
            if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) getMeta();
                meta.addStoredEnchant(enchantment, level, true);
                return meta(meta);
            }
            ItemStack itemStackCopy = new ItemStack(itemStack);
            itemStackCopy.addUnsafeEnchantment(enchantment, level);
            return copy();
        }

        /**
         * Apply skull data to this
         * @param base64 {@link String}
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable skull(@Nonnull String base64) {
            SkullMeta meta = (SkullMeta) getMeta();
            ReflectionUtil.setFieldInstance(FIELD_profile, meta, Util.makeGameProfile(base64));
            return meta(meta);
        }

        /**
         * Set the CustomModelData of this
         * @param i data
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable model(@Nullable Integer i) {
            ItemMeta meta = getMeta();
            meta.setCustomModelData(i);
            return meta(meta);
        }

        /**
         * Apply a material type to this
         * @param material {@link Material} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable material(Material material) {
            Immutable copy = copy();
            copy.itemStack.setType(material);
            return copy;
        }

        /**
         * Apply a color coded name to this
         * If name is null, reset name
         * @param name {@link String} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable name(@Nullable String name) {
            return name(name, true);
        }

        /**
         * Apply a name to this using conditional color codes
         * If name is null, reset name
         * @param name {@link String} instance
         * @param format whether to format
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable name(@Nullable String name, boolean format) {
            ItemMeta meta = getMeta();
            name = (format && name != null) ?
                    ColorUtil.color("&r" + name) :
                    name;
            meta.setDisplayName(name);
            return meta(meta);
        }

        /**
         * Reset the name of this
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable resetName() {
            return name(null);
        }

        /**
         * Apply a color coded lore to this, with lines separated by '\n'
         * If lore is null, reset lore
         * @param lore {@link String} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable lore(@Nullable String lore) {
            return this.lore(lore, true);
        }

        /**
         * Apply a lore to this using conditional color codes, with lines separated by '\n'
         * If lore is null, reset lore
         * @param lore {@link String} instance
         * @param format whether to format
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable lore(@Nullable String lore, boolean format) {
            return lore == null ? lore((List<String>) null, format)
                    : lore(Arrays.asList(lore.split("\n")), format);
        }

        /**
         * Apply a lore to this using conditional color codes, with lines separated by '\n'
         * If lore is null, reset lore
         * @param lore {@link String[]} instance
         * @param format whether to format
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable lore(@Nullable String[] lore, boolean format) {
            return lore == null ? lore((List<String>) null, format) : lore(Arrays.asList(lore), format);
        }

        /**
         * Apply a lore to this using conditional color codes, with lines separated by '\n'
         * If lore is null, reset lore
         * @param lore {@link List<String>} instance
         * @param format whether to format
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable lore(List<String> lore, boolean format) {
            ItemMeta meta = getMeta();

            if (format && lore != null)
                for (int i = 0; i < lore.size(); i++)
                    lore.set(i, ColorUtil.color("&7" + lore.get(i)));

            meta.setLore(lore);

            return meta(meta);
        }

        /**
         * Reset the lore of this
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable resetLore() {
            return lore((List<String>) null, false);
        }

        /**
         * Apply a macro to the name and lore of this
         * @param delim the delimiter i.e %
         * @param match the macro name
         * @param value the macro value
         * @return {@link Immutable} copy
         */
        @Deprecated
        @CheckReturnValue
        @Nonnull
        public Immutable macro(@Nonnull String delim, @Nonnull String match, @Nonnull String value) {
            return name(Util.macro(getName(), delim, match, value)).
                    lore(Util.macro(getLoreString(), delim, match, value));
        }

        /**
         * Apply placeholders to the name and lore of this
         * @param p {@link Player} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable placeholders(@Nullable Player p) {
            Immutable copy = copy();
            if (p != null && Main.getInstance().supportPlaceholders) {
                String temp = getName();
                if (temp != null)
                    copy = name(PlaceholderAPI.setPlaceholders(
                            p, temp));

                temp = getLoreString();
                if (temp != null)
                    copy = copy.lore(PlaceholderAPI.setPlaceholders(
                            p, temp));
            }
            return copy;
        }

        /**
         * Apply a color to this (for leather armor, potion, etc)
         * @param r red component
         * @param g green component
         * @param b blue component
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable color(int r, int g, int b) {
            return this.color(Color.fromRGB(r, g, b));
        }

        /**
         * Apply a color to this (for leather armor, potion, etc)
         * @param color {@link Color} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable color(@Nonnull Color color) {
            ItemMeta meta = getMeta();

            if (meta instanceof PotionMeta) {
                ((PotionMeta)meta).setColor(color);
            } else {
                ((LeatherArmorMeta)meta).setColor(color);
            }
            return meta(meta);
        }

        /**
         * Set the amount of this
         * @param amount amount
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable amount(int amount) {
            Immutable copy = copy();
            copy.itemStack.setAmount(amount);
            return copy;
        }

        /**
         * Makes this unbreakable
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable unbreakable() {
            ItemMeta meta = getMeta();
            meta.setUnbreakable(true);
            return meta(meta);
        }

        /**
         * Hide flags of this
         * @param flags the flags
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable hideFlags(ItemFlag... flags) {
            ItemMeta meta = getMeta();
            meta.addItemFlags(flags);
            return meta(meta);
        }

        /**
         * Make an item look like it's enchanted
         * @param glint whether to glint
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable glow(boolean glint) {
            if (glint) {
                ItemMeta meta = getMeta();
                meta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                return meta(meta);
            }
            return copy();
        }

        /**
         * Apply firework effect to this
         * @param effect the effect
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nonnull
        public Immutable fireworkEffect(FireworkEffect effect) {
            FireworkEffectMeta meta = (FireworkEffectMeta) getMeta();
            meta.setEffect(effect);
            return meta(meta);
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
         * Get the ItemMeta of this
         * @return {@link ItemMeta} copy
         */
        @CheckReturnValue
        public ItemMeta getMeta() {
            return itemStack.getItemMeta();
        }

        /**
         * Get the name of this
         * @return {@link Immutable} copy
         */
        @SuppressWarnings("ConstantConditions")
        @CheckReturnValue
        @Nullable
        public String getName() {
            return itemStack.getItemMeta().getDisplayName();
        }

        /**
         * Get the lore of this
         * @return {@link Immutable} copy
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
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @Nullable
        public List<String> getLoreList() {
            return getMeta().getLore();
        }

        /**
         * Get the lore of this
         * @return {@link Immutable} copy
         */
        @SuppressWarnings("ConstantConditions")
        @CheckReturnValue
        @Nullable
        public String[] getLoreArray() {
            return getMeta().getLore().toArray(new String[0]);
        }

        /**
         * Finalize the item
         * @return {@link ItemStack} built instance
         */
        @CheckReturnValue
        @Nonnull
        public ItemStack build() {
            return new ItemStack(itemStack);
        }
    }

    private final ItemStack itemStack;

    private ItemBuilder(Material material) {
        Validate.isTrue(material != Material.AIR);
        this.itemStack = new ItemStack(material);
    }

    private ItemBuilder(ItemStack itemStack) {
        Validate.isTrue(itemStack.getType() != Material.AIR && itemStack.getItemMeta() != null);
        this.itemStack = itemStack;
    }

    public static ItemBuilder copyOf(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder mutable(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public static ItemBuilder copyOf(ItemStack itemStack) {
        return new ItemBuilder(new ItemStack(itemStack));
    }

    public static ItemBuilder mutable(ItemBuilder builder) {
        return mutable(builder.build());
    }

    public static ItemBuilder copyOf(ItemBuilder builder) {
        return copyOf(builder.build());
    }

    /**
     * Version safe item getter
     * @param MODERN_NAME Name of the item in 1.18
     * @throws IllegalArgumentException if an item cannot be matched
     */
    @SuppressWarnings("deprecation")
    @Nonnull
    public static ItemBuilder fromModernMaterial(String MODERN_NAME) {
        MODERN_NAME = MODERN_NAME.toUpperCase();

        Material material = Material.matchMaterial(MODERN_NAME);
        if (material == null) {
            if (Version.AT_MOST_v1_12.a()) {
                if (MODERN_NAME.equals("INK_SAC"))          // TODO misnomer
                    return copyOf(Material.matchMaterial("INK_SACK"));

                for (Map.Entry<Integer, List<String>> entry : AT_MOST_v1_12_TO_MODERN_MAP.entrySet()) {
                    int index = entry.getValue().indexOf(MODERN_NAME);
                    if (index != -1) {
                        material = Material.matchMaterial("" + entry.getKey());

                        if (material != null) return copyOf(new ItemStack(material, 1, (short) index));
                    }
                }
            } else if (Version.v1_13.a()) {
                String LEGACY_NAME = v1_13_TO_MODERN_MAP.inverse().get(MODERN_NAME);

                if (LEGACY_NAME != null) material = Material.matchMaterial(LEGACY_NAME);
            }
        }

        if (material != null)
            return copyOf(material);

        throw new IllegalArgumentException("Material " + MODERN_NAME + " does not exist ");
    }

    /**
     * Return a copy of this
     * @return {@link ItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ItemBuilder copy() {
        return copyOf(this);
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

    /**
     * Apply name and lore of other to this
     * @param other {@link ItemMeta} instance
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder apply(@Nonnull ItemMeta other) {
        if (other.hasDisplayName())
            this.name(other.getDisplayName());
        if (other.hasLore())
            lore(other.getLore(), ColorMode.COLOR);
        //return this.name(other.getDisplayName()).lore(other.getLore(), ColorMode.COLOR);
        return this;
    }

    /**
     * Apply name and lore of other to this
     * @param other {@link ItemMeta} instance
     * @return {@link ItemBuilder} copy
     */
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    public ItemBuilder apply(@Nonnull ItemStack other) {
        return apply(other.getItemMeta());
    }

    /**
     * Apply the name and lore of {@link ItemStack} to this
     * @param builder {@link ItemBuilder} instance
     * @return {@link ItemBuilder} this
     */
    @Nonnull
    public ItemBuilder apply(@Nonnull ItemBuilder builder) {
        return apply(builder.getMeta());
    }

    /**
     * Transfer the item name to the lore
     * @return {@link ItemBuilder} this
     */
    @Nonnull
    public ItemBuilder name2Lore() {
        return name2Lore(null, ColorMode.COLOR);
    }

    /**
     * Transfer the item name to the lore
     * @param mode {@link ColorMode} mode
     * @return {@link ItemBuilder} this
     */
    @Nonnull
    public ItemBuilder name2Lore(ColorMode mode) {
        return name2Lore(null, ColorMode.COLOR);
    }

    /**
     * Transfer the item name to the lore, with optional prepend
     * @param prepend before text
     * @return {@link ItemBuilder} this
     */
    @Nonnull
    public ItemBuilder name2Lore(@Nullable String prepend) {
        return name2Lore(prepend, ColorMode.COLOR);
    }

    /**
     * Transfer the item name to the lore, with optional prepend
     * @param prepend before text
     * @param mode {@link ColorMode} mode
     * @return {@link ItemBuilder} this
     */
    @Nonnull
    public ItemBuilder name2Lore(@Nullable String prepend, ColorMode mode) {
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
            return meta(meta);
        }
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    /**
     * Apply skull data to this
     * @param base64 {@link String}
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder skull(@Nonnull String base64) {
        SkullMeta meta = (SkullMeta) getMeta();
        ReflectionUtil.setFieldInstance(FIELD_profile, meta, Util.makeGameProfile(base64));
        return meta(meta);
    }

    /**
     * Set the CustomModelData of this
     * @param i data
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder model(@Nullable Integer i) {
        ItemMeta meta = getMeta();
        meta.setCustomModelData(i);
        return meta(meta);
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

    /**
     * Apply a color coded name to this
     * If name is null, reset name
     * @param name {@link String} instance
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder name(@Nullable String name) {
        return this.name(name, ColorMode.COLOR);
    }

    /**
     * Apply a name to this using conditional color codes
     * If name is null, reset name
     * @param name {@link String} instance
     * @param mode {@link ColorMode} mode
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder name(@Nullable String name, ColorMode mode) {
        ItemMeta meta = getMeta();

        if (name != null)
            name = mode.a(name);

        meta.setDisplayName(name);
        return meta(meta);
    }

    /**
     * Reset the name of this
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder resetName() {
        return name(null);
    }

    /**
     * Apply a color coded lore to this, with lines separated by '\n'
     * If lore is null, reset lore
     * @param lore {@link String} instance
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder lore(@Nullable String lore) {
        return this.lore(lore, ColorMode.COLOR);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable String lore, ColorMode mode) {
        return lore == null ? lore((List<String>) null, mode)
                : lore(Arrays.asList(lore.split("\n")), mode);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable String... lore) {
        return lore(ColorMode.COLOR, lore);
    }

    @Nonnull
    public ItemBuilder lore(ColorMode mode, @Nullable String... lore) {
        return lore(lore, mode);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable String[] lore, ColorMode mode) {
        return lore == null ? lore((List<String>) null, mode) : lore(Arrays.asList(lore), mode);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable List<String> lore) {
        return lore(lore, ColorMode.COLOR);
    }

    @Nonnull
    public ItemBuilder lore(@Nullable List<String> lore, @Nonnull ColorMode mode) {
        ItemMeta meta = getMeta();

        if (lore != null)
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, mode.a(ChatColor.GRAY + lore.get(i)));
            }

        meta.setLore(lore);

        return meta(meta);
    }

    /**
     * Reset the lore of this
     * @return {@link ItemBuilder} copy
     */
    @Nonnull
    public ItemBuilder resetLore() {
        return lore((List<String>) null, ColorMode.COLOR);
    }

    /**
     * Apply a macro to the name and lore of this
     * @param delim the delimiter i.e %
     * @param match the macro name
     * @param value the macro value
     * @return {@link ItemBuilder} copy
     */
    @Deprecated
    @Nonnull
    public ItemBuilder macro(@Nonnull String delim, @Nonnull String match, @Nonnull String value) {
        name(Util.macro(getName(), delim, match, value));
        lore(Util.macro(getLoreString(), delim, match, value));

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
            String temp = getName();
            if (temp != null)
                name(PlaceholderAPI.setPlaceholders(
                        p, temp));

            temp = getLoreString();
            if (temp != null)
                lore(PlaceholderAPI.setPlaceholders(
                        p, temp));
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
     * @return {@link Immutable} copy
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
     * Apply firework effect to this
     * @param effect the effect
     * @return {@link Immutable} copy
     */
    @Nonnull
    public ItemBuilder fireworkEffect(FireworkEffect effect) {
        FireworkEffectMeta meta = (FireworkEffectMeta) getMeta();
        meta.setEffect(effect);
        itemStack.setItemMeta(meta);
        return this;
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
    @SuppressWarnings("ConstantConditions")
    @CheckReturnValue
    @Nullable
    public String getName() {
        return getMeta().getDisplayName();
    }

    /**
     * Get the Material of this
     * @return {@link Material} copy
     */
    @CheckReturnValue
    @Nonnull
    public String getLocaleName() {
        return Util.punctuateAndGrammar(getMaterial());
    }

    /**
     * Get the Material of this
     * @return {@link Material} copy
     */
    @CheckReturnValue
    @Nonnull
    public String getNameOrLocaleName() {
        return Util.strDef(getName(), getLocaleName());
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



    private static final Map<Integer, List<String>> AT_MOST_v1_12_TO_MODERN_MAP = new HashMap<>();
    private static void addAll_v1_12(Object... values) {
        for (int i=0; i < values.length; i++) {
            Validate.isTrue(values[i] instanceof Integer || values[i] instanceof String, "Invalid type: " + values[i].getClass());

            List<String> MODERN = new ArrayList<>();

            i++;
            for (; i < values.length; i++) {
                if (values[i] instanceof Integer)
                    break;

                //if (values[i] instanceof String)
                    MODERN.add((String) values[i]);
            }
            AT_MOST_v1_12_TO_MODERN_MAP.put((Integer) values[i], MODERN);

            i--;
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
                35, "WHITE_WOOL", "ORANGE_WOOL", "MAGENTA_WOOL", "LIGHT_BLUE_WOOL", "YELLOW_WOOL", "LIME_WOOL", "PINK_WOOL", "GRAY_WOOL", "GRAY_WOOL", "LIGHT_GRAY_WOOL", "CYAN_WOOL", "PURPLE_WOOL", "BLUE_WOOL", "BROWN_WOOL", "GREEN_WOOL", "RED_WOOL", "BLACK_WOOL",
                37, "DANDELION",
                38, "POPPY", "BLUE_ORCHID", "ALLIUM", "AZURE_BLUET", "RED_TULIP", "ORANGE_TULIP", "WHITE_TULIP", "PINK_TULIP", "OXEYE_DAISY",
                44, "SMOOTH_STONE_SLAB", "SANDSTONE_SLAB", "COBBLESTONE_SLAB", "BRICK_SLAB", "STONE_BRICK_SLAB", "NETHER_BRICK_SLAB", "QUARTZ_SLAB",
                95, "WHITE_STAINED_GLASS", "ORANGE_STAINED_GLASS", "MAGENTA_STAINED_GLASS", "LIGHT_BLUE_STAINED_GLASS", "YELLOW_STAINED_GLASS", "LIME_STAINED_GLASS", "PINK_STAINED_GLASS", "GRAY_STAINED_GLASS", "GRAY_STAINED_GLASS", "LIGHT_GRAY_STAINED_GLASS", "CYAN_STAINED_GLASS", "PURPLE_STAINED_GLASS", "BLUE_STAINED_GLASS", "BROWN_STAINED_GLASS", "GREEN_STAINED_GLASS", "RED_STAINED_GLASS", "BLACK_STAINED_GLASS",
                97, "INFESTED_STONE", "INFESTED_COBBLESTONE", "INFESTED_STONE_BRICKS", "INFESTED_MOSSY_STONE_BRICKS", "INFESTED_CRACKED_STONE_BRICKS", "INFESTED_CHISELED_STONE_BRICKS",
                98, "STONE_BRICKS", "MOSSY_STONE_BRICKS", "CRACKED_STONE_BRICKS", "CHISELED_STONE_BRICKS",
                126, "OAK_SLAB", "SPRUCE_SLAB", "BIRCH_SLAB", "JUNGLE_SLAB", "ACACIA_SLAB", "DARK_OAK_SLAB",
                139, "COBBLESTONE_WALL", "MOSSY_COBBLESTONE_WALL",
                145, "ANVIL", "CHIPPED_ANVIL", "DAMAGED_ANVIL",
                155, "QUARTZ_BLOCK", "CHISELED_QUARTZ_BLOCK", "QUARTZ_PILLAR",
                159, "WHITE_TERRACOTTA", "ORANGE_TERRACOTTA", "MAGENTA_TERRACOTTA", "LIGHT_BLUE_TERRACOTTA", "YELLOW_TERRACOTTA", "LIME_TERRACOTTA", "PINK_TERRACOTTA", "GRAY_TERRACOTTA", "GRAY_TERRACOTTA", "LIGHT_GRAY_TERRACOTTA", "CYAN_TERRACOTTA", "PURPLE_TERRACOTTA", "BLUE_TERRACOTTA", "BROWN_TERRACOTTA", "GREEN_TERRACOTTA", "RED_TERRACOTTA", "BLACK_TERRACOTTA",
                160, "WHITE_STAINED_GLASS_PANE", "ORANGE_STAINED_GLASS_PANE", "MAGENTA_STAINED_GLASS_PANE", "LIGHT_BLUE_STAINED_GLASS_PANE", "YELLOW_STAINED_GLASS_PANE", "LIME_STAINED_GLASS_PANE", "PINK_STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE", "LIGHT_GRAY_STAINED_GLASS_PANE", "CYAN_STAINED_GLASS_PANE", "PURPLE_STAINED_GLASS_PANE", "BLUE_STAINED_GLASS_PANE", "BROWN_STAINED_GLASS_PANE", "GREEN_STAINED_GLASS_PANE", "RED_STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE",
                161, "ACACIA_LEAVES", "DARK_OAK_LEAVES",
                162, "ACACIA_LOG", "DARK_OAK_LOG",
                168, "PRISMARINE", "PRISMARINE_BRICKS", "DARK_PRISMARINE",
                171, "WHITE_CARPET", "ORANGE_CARPET", "MAGENTA_CARPET", "LIGHT_BLUE_CARPET", "YELLOW_CARPET", "LIME_CARPET", "PINK_CARPET", "GRAY_CARPET", "GRAY_CARPET", "LIGHT_GRAY_CARPET", "CYAN_CARPET", "PURPLE_CARPET", "BLUE_CARPET", "BROWN_CARPET", "GREEN_CARPET", "RED_CARPET", "BLACK_CARPET",
                175, "SUNFLOWER", "LILAC", "TALL_GRASS", "LARGE_FERN", "ROSE_BUSH", "PEONY",
                179, "RED_SANDSTONE", "CHISELED_RED_SANDSTONE", "SMOOTH_RED_SANDSTONE",
                //182, "RED_SANDSTONE_SLAB",
                //205, "PURPUR_SLAB",
                251, "WHITE_CONCRETE", "ORANGE_CONCRETE", "MAGENTA_CONCRETE", "LIGHT_BLUE_CONCRETE", "YELLOW_CONCRETE", "LIME_CONCRETE", "PINK_CONCRETE", "GRAY_CONCRETE", "GRAY_CONCRETE", "LIGHT_GRAY_CONCRETE", "CYAN_CONCRETE", "PURPLE_CONCRETE", "BLUE_CONCRETE", "BROWN_CONCRETE", "GREEN_CONCRETE", "RED_CONCRETE", "BLACK_CONCRETE",
                252, "WHITE_CONCRETE_POWDER", "ORANGE_CONCRETE_POWDER", "MAGENTA_CONCRETE_POWDER", "LIGHT_BLUE_CONCRETE_POWDER", "YELLOW_CONCRETE_POWDER", "LIME_CONCRETE_POWDER", "PINK_CONCRETE_POWDER", "GRAY_CONCRETE_POWDER", "GRAY_CONCRETE_POWDER", "LIGHT_GRAY_CONCRETE_POWDER", "CYAN_CONCRETE_POWDER", "PURPLE_CONCRETE_POWDER", "BLUE_CONCRETE_POWDER", "BROWN_CONCRETE_POWDER", "GREEN_CONCRETE_POWDER", "RED_CONCRETE_POWDER", "BLACK_CONCRETE_POWDER",
                263, "COAL", "CHARCOAL",
                322, "GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE",
                349, null, "SALMON", null, "PUFFERFISH", "",        // TODO fallback instead of null?
                350, null, "COOKED_SALMON",
                351, "BLACK_DYE", "RED_DYE", "GREEN_DYE", "BROWN_DYE", "BLUE_DYE", "PURPLE_DYE", "CYAN_DYE", "LIGHT_GRAY_DYE", "GRAY_DYE", "PINK_DYE", "LIME_DYE", "YELLOW_DYE", "LIGHT_BLUE_DYE", "MAGENTA_DYE", "ORANGE_DYE", "WHITE_DYE",
                355, "WHITE_BED", "ORANGE_BED", "MAGENTA_BED", "LIGHT_BLUE_BED", "YELLOW_BED", "LIME_BED", "PINK_BED", "GRAY_BED", "GRAY_BED", "LIGHT_GRAY_BED", "CYAN_BED", "PURPLE_BED", "BLUE_BED", "BROWN_BED", "GREEN_BED", "RED_BED", "BLACK_BED",
                //NBT 373, "POTIONS...",
                //NBT 383, "SPAWN_EGGS...",
                384, "EXPERIENCE_BOTTLE",
                397, "SKELETON_SKULL", "WITHER_SKELETON_SKULL", "ZOMBIE_HEAD", "PLAYER_HEAD", "CREEPER_HEAD", "DRAGON_HEAD",
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

            final int ID = getMaterial().getId();
            final int dmg = itemStack.getDurability();

            List<String> MODERN_NAMES = AT_MOST_v1_12_TO_MODERN_MAP.get(ID);

            if (MODERN_NAMES != null) return MODERN_NAMES.get(dmg);
        } else if (Version.v1_13.a()) {
            String get = v1_13_TO_MODERN_MAP.get(NAME);
            if (get != null) return get;
        }

        // else 1.14 and above
        return NAME;
    }

    /**
     * Finalize the item
     * @return {@link ItemStack} built instance
     */
    @CheckReturnValue
    @Nonnull
    public ItemStack build() {
        return itemStack;
    }
}
