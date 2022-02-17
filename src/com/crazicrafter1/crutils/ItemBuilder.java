package com.crazicrafter1.crutils;

import com.google.errorprone.annotations.CheckReturnValue;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;

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
        @NotNull
        public Immutable copy() {
            return new Immutable(this);
        }

        /**
         * Set the meta of this
         * @param meta {@link ItemMeta} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @NotNull
        public Immutable meta(@NotNull ItemMeta meta) {
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
        @NotNull
        public Immutable transcribe(@NotNull String from, @NotNull String to) {
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
        @NotNull
        public Immutable apply(@NotNull ItemMeta other) {
            return name(other.getDisplayName()).lore(other.getLore(), false);
        }

        /**
         * Apply name and lore of other to this
         * @param other {@link ItemStack} instance
         * @return {@link Immutable} copy
         */
        @SuppressWarnings("ConstantConditions")
        @CheckReturnValue
        @NotNull
        public Immutable apply(@NotNull ItemStack other) {
            return apply(other.getItemMeta());
        }

        /**
         * Apply name and lore of other to this
         * @param other {@link Immutable} instance
         * @return {@link Immutable} copy
         */
        @SuppressWarnings("ConstantConditions")
        @CheckReturnValue
        @NotNull
        public Immutable apply(@NotNull Immutable other) {
            return apply(other.getMeta());
        }

        /**
         * Apply a {@link PotionEffect} to this
         * @param effect {@link PotionEffect} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @NotNull
        public Immutable effect(@NotNull PotionEffect effect) {
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
        @NotNull
        public Immutable enchant(@NotNull Enchantment enchantment, int level) {
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
        @NotNull
        public Immutable skull(@NotNull String base64) {
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
        @NotNull
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
        @NotNull
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
        @NotNull
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
        @NotNull
        public Immutable name(@Nullable String name, boolean format) {
            ItemMeta meta = getMeta();
            name = (format && name != null) ?
                    Util.format("&r" + name) :
                    name;
            meta.setDisplayName(name);
            return meta(meta);
        }

        /**
         * Reset the name of this
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @NotNull
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
        @NotNull
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
        @NotNull
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
        @NotNull
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
        @NotNull
        public Immutable lore(List<String> lore, boolean format) {
            ItemMeta meta = getMeta();

            if (format && lore != null)
                for (int i = 0; i < lore.size(); i++)
                    lore.set(i, Util.format("&7" + lore.get(i)));

            meta.setLore(lore);

            return meta(meta);
        }

        /**
         * Reset the lore of this
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @NotNull
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
        @NotNull
        public Immutable macro(@NotNull String delim, @NotNull String match, @NotNull String value) {
            return name(Util.macro(getName(), delim, match, value)).
                    lore(Util.macro(getLoreString(), delim, match, value));
        }

        /**
         * Apply placeholders to the name and lore of this
         * @param p {@link Player} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @NotNull
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
        @NotNull
        public Immutable color(int r, int g, int b) {
            return this.color(Color.fromRGB(r, g, b));
        }

        /**
         * Apply a color to this (for leather armor, potion, etc)
         * @param color {@link Color} instance
         * @return {@link Immutable} copy
         */
        @CheckReturnValue
        @NotNull
        public Immutable color(@NotNull Color color) {
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
        @NotNull
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
        @NotNull
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
        @NotNull
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
        @NotNull
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
        @NotNull
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
        @NotNull
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
    @NotNull
    public ItemBuilder copy() {
        return copyOf(this);
    }

    /**
     * Set the meta of this
     * @param meta {@link ItemMeta} instance
     * @return {@link ItemBuilder} copy
     */
    @NotNull
    public ItemBuilder meta(@NotNull ItemMeta meta) {
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
    @NotNull
    public ItemBuilder transcribe(@NotNull String from, @NotNull String to) {
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
    @NotNull
    public ItemBuilder apply(@NotNull ItemMeta other) {
        return this.name(other.getDisplayName()).lore(other.getLore(), false);
    }

    /**
     * Apply name and lore of other to this
     * @param other {@link ItemMeta} instance
     * @return {@link ItemBuilder} copy
     */
    @SuppressWarnings("ConstantConditions")
    @NotNull
    public ItemBuilder apply(@NotNull ItemStack other) {
        return apply(other.getItemMeta());
    }

    /**
     * Apply the name and lore of {@link ItemStack} to this
     * @param builder {@link ItemBuilder} instance
     * @return {@link ItemBuilder} this
     */
    @NotNull
    public ItemBuilder apply(@NotNull ItemBuilder builder) {
        return apply(builder.getMeta());
    }

    /**
     * Apply a {@link PotionEffect} to this
     * @param effect {@link PotionEffect} instance
     * @return {@link ItemBuilder} copy
     */
    @NotNull
    public ItemBuilder effect(@NotNull PotionEffect effect) {
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
    @NotNull
    public ItemBuilder enchant(@NotNull Enchantment enchantment, int level) {
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
    @NotNull
    public ItemBuilder skull(@NotNull String base64) {
        SkullMeta meta = (SkullMeta) getMeta();
        ReflectionUtil.setFieldInstance(FIELD_profile, meta, Util.makeGameProfile(base64));
        return meta(meta);
    }

    /**
     * Set the CustomModelData of this
     * @param i data
     * @return {@link ItemBuilder} copy
     */
    @NotNull
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
    @NotNull
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
    @NotNull
    public ItemBuilder name(@Nullable String name) {
        return this.name(name, true);
    }

    /**
     * Apply a name to this using conditional color codes
     * If name is null, reset name
     * @param name {@link String} instance
     * @param format whether to format
     * @return {@link ItemBuilder} copy
     */
    @NotNull
    public ItemBuilder name(@Nullable String name, boolean format) {
        ItemMeta meta = itemStack.getItemMeta();
        name = format && name != null ?
                Util.format("&r" + name) :
                name;
        meta.setDisplayName(name);
        return meta(meta);
    }

    /**
     * Reset the name of this
     * @return {@link ItemBuilder} copy
     */
    @NotNull
    public ItemBuilder resetName() {
        return name(null);
    }

    /**
     * Apply a color coded lore to this, with lines separated by '\n'
     * If lore is null, reset lore
     * @param lore {@link String} instance
     * @return {@link ItemBuilder} copy
     */
    @NotNull
    public ItemBuilder lore(@Nullable String lore) {
        return this.lore(lore, true);
    }

    /**
     * Apply a lore to this using conditional color codes, with lines separated by '\n'
     * If lore is null, reset lore
     * @param lore {@link String} instance
     * @param format whether to format
     * @return {@link ItemBuilder} copy
     */
    @NotNull
    public ItemBuilder lore(@Nullable String lore, boolean format) {
        return lore == null ? lore((List<String>) null, format)
                : lore(Arrays.asList(lore.split("\n")), format);
    }

    /**
     * Apply a lore to this using conditional color codes, with lines separated by '\n'
     * If lore is null, reset lore
     * @param lore {@link String[]} instance
     * @param format whether to format
     * @return {@link ItemBuilder} copy
     */
    @NotNull
    public ItemBuilder lore(@Nullable String[] lore, boolean format) {
        return lore == null ? lore((List<String>) null, format) : lore(Arrays.asList(lore), format);
    }

    /**
     * Apply a lore to this using conditional color codes, with lines separated by '\n'
     * If lore is null, reset lore
     * @param lore {@link List<String>} instance
     * @param format whether to format
     * @return {@link ItemBuilder} copy
     */
    @NotNull
    public ItemBuilder lore(@Nullable List<String> lore, boolean format) {
        ItemMeta meta = getMeta();

        if (format && lore != null)
            for (int i = 0; i < lore.size(); i++)
                lore.set(i, Util.format("&7" + lore.get(i)));

        meta.setLore(lore);

        return meta(meta);
    }

    /**
     * Reset the lore of this
     * @return {@link ItemBuilder} copy
     */
    @NotNull
    public ItemBuilder resetLore() {
        return lore((List<String>) null, false);
    }

    /**
     * Apply a macro to the name and lore of this
     * @param delim the delimiter i.e %
     * @param match the macro name
     * @param value the macro value
     * @return {@link ItemBuilder} copy
     */
    @Deprecated
    @NotNull
    public ItemBuilder macro(@NotNull String delim, @NotNull String match, @NotNull String value) {
        name(Util.macro(getName(), delim, match, value));
        lore(Util.macro(getLoreString(), delim, match, value));

        return this;
    }

    /**
     * Apply placeholders to the name and lore of this
     * @param p {@link Player} instance
     * @return {@link ItemBuilder} copy
     */
    @CheckReturnValue
    @NotNull
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
    @NotNull
    public ItemBuilder color(int r, int g, int b) {
        return this.color(Color.fromRGB(r, g, b));
    }

    /**
     * Apply a color to this (for leather armor, potion, etc)
     * @param color {@link Color} instance
     * @return {@link ItemBuilder} copy
     */
    @NotNull
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
    @NotNull
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Makes this unbreakable
     * @return {@link ItemBuilder} copy
     */
    @NotNull
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
    @NotNull
    public ItemBuilder hideFlags(ItemFlag ... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(flags);
        return meta(meta);
    }

    /**
     * Make an item look like it's enchanted
     * @param glint whether to glint
     * @return {@link Immutable} copy
     */
    @NotNull
    public ItemBuilder glow(boolean glint) {
        if (glint) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
            return meta(meta);
        }
        return this;
    }

    /**
     * Apply firework effect to this
     * @param effect the effect
     * @return {@link Immutable} copy
     */
    @NotNull
    public ItemBuilder fireworkEffect(FireworkEffect effect) {
        FireworkEffectMeta meta = (FireworkEffectMeta) itemStack.getItemMeta();
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
    @NotNull
    public ItemStack build() {
        return itemStack;
    }
}
