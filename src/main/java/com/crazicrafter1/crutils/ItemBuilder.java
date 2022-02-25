package com.crazicrafter1.crutils;

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
    private static Field FIELD_profile = ReflectionUtil.getField(CLASS_CraftMetaSkull, "profile");

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

    private static ArrayList<String> COLORS = new ArrayList<>(Arrays.asList(
            "WHITE",
            "ORANGE",
            "MAGENTA",
            "LIGHT_BLUE",
            "YELLOW",
            "LIME",
            "PINK",
            "GRAY",
            "LIGHT_GRAY",
            "CYAN",
            "PURPLE",
            "BLUE",
            "BROWN",
            "GREEN",
            "RED",
            "BLACK"
    ));

    private static HashMap<String, String> LEGACY_NAMES = new HashMap<>();
    static {
        LEGACY_NAMES.put("COMPARATOR", "REDSTONE_COMPARATOR");
        LEGACY_NAMES.put("IRON_HORSE_ARMOR", "IRON_BARDING");
        LEGACY_NAMES.put("GOLD_HORSE_ARMOR", "GOLD_BARDING");
        LEGACY_NAMES.put("DIAMOND_HORSE_ARMOR", "DIAMOND_BARDING");
        LEGACY_NAMES.put("FIREWORK_ROCKET", "FIREWORK");
        LEGACY_NAMES.put("FIREWORK_STAR", "FIREWORK_CHARGE");
        LEGACY_NAMES.put("EXPERIENCE_BOTTLE", "EXP_BOTTLE");
        LEGACY_NAMES.put("PLAYER_HEAD", "SKULL_ITEM");
    }

    /**
     * Version safe item getter
     * @param name
     * @return
     */
    public static ItemBuilder of(String name) {
        name = name.toUpperCase();
        try {
            try {
                return copyOf(Material.matchMaterial(name));
            } catch (Exception e1) {

                try {
                    return copyOf(Material.matchMaterial(
                            Objects.requireNonNull(LEGACY_NAMES.get(name))));
                } catch (Exception e2) {
                    int index = name.indexOf("_");
                    if (index != -1) {
                        int dmg;
                        String materialName;

                        int index2 = name.indexOf("_", index + 1);
                        if (index2 == -1) {
                            // then there is no way that the name is a double size color
                            dmg = COLORS.indexOf(name.substring(0, index));
                            materialName = name.substring(index + 1);
                        } else {
                            dmg = COLORS.indexOf(name.substring(0, index2));
                            if (dmg != -1)
                                materialName = name.substring(index2 + 1);
                            else {
                                materialName = name.substring(index + 1);
                                dmg = COLORS.indexOf(name.substring(0, index));
                            }
                        }
                        // then name is a 2 length
                        // LIGHT_BLUE_CARPET // 5,
                        // BLUE_WOOL // 4
                        // LIGHT_BLUE_STAINED_GLASS_PANE // 5

                        //Main.getInstance().info("materialName: " + materialName + ", dmg: " + dmg);

                        if (materialName.endsWith("DYE"))
                            return mutable(new ItemStack(Material.matchMaterial("INK_SACK"), 1, (short) (15-dmg)));

                        if (dmg != -1) {
                            return mutable(new ItemStack(Material.matchMaterial(materialName), 1, (short) dmg));
                        }

                    }
                }
            }
        } catch (Exception ignored) {
        }
        throw new RuntimeException("Item :'" + name + "' is invalid");
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
        return lore((List<String>) null, null);
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
