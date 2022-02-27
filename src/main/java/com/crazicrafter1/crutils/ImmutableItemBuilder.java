package com.crazicrafter1.crutils;

import me.clip.placeholderapi.PlaceholderAPI;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Strictly static constant
 * immutanle class
 * copies only
 * will not transfer states across
 */
public class ImmutableItemBuilder {
    private final ItemStack itemStack;

    public ImmutableItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ImmutableItemBuilder(ItemStack itemStack) {
        this.itemStack = new ItemStack(itemStack);
    }

    public ImmutableItemBuilder(ImmutableItemBuilder builder) {
        this.itemStack = builder.build();
    }

    /**
     * Return a copy of this
     *
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder copy() {
        return new ImmutableItemBuilder(this);
    }

    /**
     * Set the meta of this
     *
     * @param meta {@link ItemMeta} instance
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder meta(@Nonnull ItemMeta meta) {
        ImmutableItemBuilder copy = copy();
        copy.itemStack.setItemMeta(Objects.requireNonNull(meta));
        return copy;
    }

    /**
     * Apply a language translation filter using Google Translate
     * to the name and lore of this
     *
     * @param from from language
     * @param to   to language
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder transcribe(@Nonnull String from, @Nonnull String to) {
        GoogleTranslate G = new GoogleTranslate();

        return name(G.translate(getName(), from, to))
                .lore(G.translate(getLoreString(), from, to));
    }

    /**
     * Apply name and lore of other to this
     *
     * @param other {@link ItemMeta} instance
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder apply(@Nonnull ItemMeta other) {
        return name(other.getDisplayName()).lore(other.getLore(), false);
    }

    /**
     * Apply name and lore of other to this
     *
     * @param other {@link ItemStack} instance
     * @return {@link ImmutableItemBuilder} copy
     */
    @SuppressWarnings("ConstantConditions")
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder apply(@Nonnull ItemStack other) {
        return apply(other.getItemMeta());
    }

    /**
     * Apply name and lore of other to this
     *
     * @param other {@link ImmutableItemBuilder} instance
     * @return {@link ImmutableItemBuilder} copy
     */
    @SuppressWarnings("ConstantConditions")
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder apply(@Nonnull ImmutableItemBuilder other) {
        return apply(other.getMeta());
    }

    /**
     * Apply a {@link PotionEffect} to this
     *
     * @param effect {@link PotionEffect} instance
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder effect(@Nonnull PotionEffect effect) {
        PotionMeta meta = (PotionMeta) getMeta();
        meta.addCustomEffect(effect, true);
        return meta(meta);
    }

    /**
     * Apply an {@link Enchantment} with level to this
     *
     * @param enchantment {@link Enchantment} instance
     * @param level       int level
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder enchant(@Nonnull Enchantment enchantment, int level) {
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
     *
     * @param base64 {@link String}
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder skull(@Nonnull String base64) {
        SkullMeta meta = (SkullMeta) getMeta();
        ReflectionUtil.setFieldInstance(ItemBuilder.FIELD_profile, meta, Util.makeGameProfile(base64));
        return meta(meta);
    }

    /**
     * Set the CustomModelData of this
     *
     * @param i data
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder model(@Nullable Integer i) {
        ItemMeta meta = getMeta();
        meta.setCustomModelData(i);
        return meta(meta);
    }

    /**
     * Apply a material type to this
     *
     * @param material {@link Material} instance
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder material(Material material) {
        ImmutableItemBuilder copy = copy();
        copy.itemStack.setType(material);
        return copy;
    }

    /**
     * Apply a color coded name to this
     * If name is null, reset name
     *
     * @param name {@link String} instance
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder name(@Nullable String name) {
        return name(name, true);
    }

    /**
     * Apply a name to this using conditional color codes
     * If name is null, reset name
     *
     * @param name   {@link String} instance
     * @param format whether to format
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder name(@Nullable String name, boolean format) {
        ItemMeta meta = getMeta();
        name = (format && name != null) ?
                ColorUtil.color("&r" + name) :
                name;
        meta.setDisplayName(name);
        return meta(meta);
    }

    /**
     * Reset the name of this
     *
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder resetName() {
        return name(null);
    }

    /**
     * Apply a color coded lore to this, with lines separated by '\n'
     * If lore is null, reset lore
     *
     * @param lore {@link String} instance
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder lore(@Nullable String lore) {
        return this.lore(lore, true);
    }

    /**
     * Apply a lore to this using conditional color codes, with lines separated by '\n'
     * If lore is null, reset lore
     *
     * @param lore   {@link String} instance
     * @param format whether to format
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder lore(@Nullable String lore, boolean format) {
        return lore == null ? lore((List<String>) null, format)
                : lore(Arrays.asList(lore.split("\n")), format);
    }

    /**
     * Apply a lore to this using conditional color codes, with lines separated by '\n'
     * If lore is null, reset lore
     *
     * @param lore   {@link String[]} instance
     * @param format whether to format
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder lore(@Nullable String[] lore, boolean format) {
        return lore == null ? lore((List<String>) null, format) : lore(Arrays.asList(lore), format);
    }

    /**
     * Apply a lore to this using conditional color codes, with lines separated by '\n'
     * If lore is null, reset lore
     *
     * @param lore   {@link List<String>} instance
     * @param format whether to format
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder lore(List<String> lore, boolean format) {
        ItemMeta meta = getMeta();

        if (format && lore != null)
            for (int i = 0; i < lore.size(); i++)
                lore.set(i, ColorUtil.color("&7" + lore.get(i)));

        meta.setLore(lore);

        return meta(meta);
    }

    /**
     * Reset the lore of this
     *
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder resetLore() {
        return lore((List<String>) null, false);
    }

    /**
     * Apply a macro to the name and lore of this
     *
     * @param delim the delimiter i.e %
     * @param match the macro name
     * @param value the macro value
     * @return {@link ImmutableItemBuilder} copy
     */
    @Deprecated
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder macro(@Nonnull String delim, @Nonnull String match, @Nonnull String value) {
        return name(Util.macro(getName(), delim, match, value)).
                lore(Util.macro(getLoreString(), delim, match, value));
    }

    /**
     * Apply placeholders to the name and lore of this
     *
     * @param p {@link Player} instance
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder placeholders(@Nullable Player p) {
        ImmutableItemBuilder copy = copy();
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
     *
     * @param r red component
     * @param g green component
     * @param b blue component
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder color(int r, int g, int b) {
        return this.color(Color.fromRGB(r, g, b));
    }

    /**
     * Apply a color to this (for leather armor, potion, etc)
     *
     * @param color {@link Color} instance
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder color(@Nonnull Color color) {
        ItemMeta meta = getMeta();

        if (meta instanceof PotionMeta) {
            ((PotionMeta) meta).setColor(color);
        } else {
            ((LeatherArmorMeta) meta).setColor(color);
        }
        return meta(meta);
    }

    /**
     * Set the amount of this
     *
     * @param amount amount
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder amount(int amount) {
        ImmutableItemBuilder copy = copy();
        copy.itemStack.setAmount(amount);
        return copy;
    }

    /**
     * Makes this unbreakable
     *
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder unbreakable() {
        ItemMeta meta = getMeta();
        meta.setUnbreakable(true);
        return meta(meta);
    }

    /**
     * Hide flags of this
     *
     * @param flags the flags
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder hideFlags(ItemFlag... flags) {
        ItemMeta meta = getMeta();
        meta.addItemFlags(flags);
        return meta(meta);
    }

    /**
     * Make an item look like it's enchanted
     *
     * @param glint whether to glint
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder glow(boolean glint) {
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
     *
     * @param effect the effect
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nonnull
    public ImmutableItemBuilder fireworkEffect(FireworkEffect effect) {
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
     *
     * @return {@link ItemMeta} copy
     */
    @CheckReturnValue
    public ItemMeta getMeta() {
        return itemStack.getItemMeta();
    }

    /**
     * Get the name of this
     *
     * @return {@link ImmutableItemBuilder} copy
     */
    @SuppressWarnings("ConstantConditions")
    @CheckReturnValue
    @Nullable
    public String getName() {
        return itemStack.getItemMeta().getDisplayName();
    }

    /**
     * Get the lore of this
     *
     * @return {@link ImmutableItemBuilder} copy
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
     *
     * @return {@link ImmutableItemBuilder} copy
     */
    @CheckReturnValue
    @Nullable
    public List<String> getLoreList() {
        return getMeta().getLore();
    }

    /**
     * Get the lore of this
     *
     * @return {@link ImmutableItemBuilder} copy
     */
    @SuppressWarnings("ConstantConditions")
    @CheckReturnValue
    @Nullable
    public String[] getLoreArray() {
        return getMeta().getLore().toArray(new String[0]);
    }

    /**
     * Finalize the item
     *
     * @return {@link ItemStack} built instance
     */
    @CheckReturnValue
    @Nonnull
    public ItemStack build() {
        return new ItemStack(itemStack);
    }
}
