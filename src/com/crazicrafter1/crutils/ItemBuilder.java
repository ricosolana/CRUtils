package com.crazicrafter1.crutils;

import com.crazicrafter1.crutils.refl.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ItemBuilder {

    private final ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = new ItemStack(itemStack);
    }

    /**
     *
     * @param itemStack Merge name and lore of itemStack to this.itemStack
     * @return
     */
    public ItemBuilder mergeLexicals(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            return this.name(itemStack.getItemMeta().getDisplayName()).lore(itemStack.getItemMeta().getLore(), false);
        }
        return this;
    }

    public ItemBuilder effect(PotionEffect effect) {
        if (itemStack.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            meta.addCustomEffect(effect, true);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        // Enchanted book
        if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemStack.getItemMeta();
            meta.addStoredEnchant(enchantment, level, true);
            itemStack.setItemMeta(meta);
        } else {
            itemStack.addUnsafeEnchantment(enchantment, level);
        }
        return this;
    }

    public ItemBuilder skull(String base64) {
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

        Method setProfileMethod = ReflectionUtil.getMethod(
                ReflectionUtil.getCraftClass("inventory.CraftMetaSkull"),
                "setProfile",
                GameProfileMirror.gameProfileClass);

        ReflectionUtil.invokeMethod(setProfileMethod, meta, Util.makeGameProfile(base64));

        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Sets the CustomModelData of the item
     * @param i the data
     * @return this
     */
    public ItemBuilder customModelData(Integer i) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(i);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Sets the material type of the item
     * @param material the material
     * @return this
     */
    public ItemBuilder type(Material material) {
        itemStack.setType(material);
        return this;
    }

    /**
     * Set the displayName of the item. '&' color prefix will be translated accordingly
     * @param name the custom name
     * @return this
     */
    public ItemBuilder name(String name) {
        return this.name(name, true);
    }

    /**
     * Set the displayName of the item. '&' color prefix will be translated if translate is 'true'
     * @param name the custom name
     * @return this
     */
    public ItemBuilder name(String name, boolean translate) {
        if (name == null)
            return this;

        ItemMeta meta = itemStack.getItemMeta();



        name = translate ?
                Util.format("&r" + name) :
                name;
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);

        return this;
    }

    /**
     * Sets the displayName of the item to its default
     * @return this
     */
    public ItemBuilder resetName() {
        return name(null);
    }

    public String getName() {
        if (itemStack.getItemMeta().hasDisplayName())
            return itemStack.getItemMeta().getDisplayName();
        return null;
    }

    /**
     * Set the lore of an item, with lines separated by a newline character
     * @param lore the lore
     * @return this
     */
    public ItemBuilder lore(String lore) {
        return this.lore(lore, true);
    }

    public ItemBuilder lore(String lore, boolean format) {
        if (lore == null)
            return this;
        return lore(lore.split("\n"), format);
    }

    public ItemBuilder lore(String[] lore, boolean format) {
        return lore(Arrays.asList(lore), format);
    }

    public ItemBuilder lore(List<String> lore, boolean format) {
        if (lore != null) {
            ItemMeta meta = itemStack.getItemMeta();

            if (format)
                for (int i = 0; i < lore.size(); i++)
                    lore.set(i, Util.format("&7" + lore.get(i)));

            meta.setLore(lore);

            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder resetLore() {
        ItemMeta meta = itemStack.getItemMeta();

        meta.setLore(null);

        itemStack.setItemMeta(meta);

        return this;
    }

    public String getLore() {
        List<String>lore = itemStack.getItemMeta().getLore();
        if (lore == null) return null;
        return String.join("\n", lore);
    }

    /**
     * Apply a macro replacement to the name and lore
     * @param delim the delimiter i.e %
     * @param match the macro name
     * @param value the macro value
     * @return this
     */
    public ItemBuilder macro(String delim, String match, String value) {
        name(Util.macro(getName(), delim, match, value));
        lore(Util.macro(getLore(), delim, match, value));

        return this;
    }

    /**
     * Apply PlaceholderAPI placeholders to the name and lore
     * if player is null, does nothing
     * @param p the player to substitute values from
     * @return this
     */
    public ItemBuilder placeholders(@Nullable Player p) {
        //Main.getInstance().info("support placeholders: " + Main.getInstance().supportPlaceholders);
        if (p != null && Main.getInstance().supportPlaceholders) {
            String temp = getName();
            if (temp != null)
                name(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(
                    p, temp));

            /*
             * not the most efficient process,
             * but this is java
             * so wbo really cares about performance
             */
            temp = getLore();
            if (temp != null)
                lore(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(
                    p, temp));
        }
        return this;
    }

    /**
     * Set the color of a colored item (leather armor, or potion)
     * @param r red component
     * @param g green component
     * @param b blue component
     * @return this
     */
    public ItemBuilder color(int r, int g, int b) {
        return this.color(Color.fromRGB(r, g, b));
    }

    public ItemBuilder color(Color color) {
        ItemMeta meta = itemStack.getItemMeta();

        if (meta instanceof PotionMeta) {
            ((PotionMeta)meta).setColor(color);
            itemStack.setItemMeta(meta);
        } else if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta)meta).setColor(color);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Set the item amount
     * @param c the amount
     * @return this
     */
    public ItemBuilder count(int c) {
        itemStack.setAmount(c);
        return this;
    }

    /**
     * Makes an item unbreakable
     * @return this
     */
    public ItemBuilder unbreakable() {
        ItemMeta meta = itemStack.getItemMeta();

        meta.setUnbreakable(true);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder hideFlags(ItemFlag ... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(flags);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Make an item look like it's enchanted
     * @param state true or false
     * @return this
     */
    public ItemBuilder glow(boolean state) {
        if (state) {
            //itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
            ItemMeta meta = itemStack.getItemMeta();
            meta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /*
    public ItemBuilder fast() {
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound nbt = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();

        NBTTagCompound speed = new NBTTagCompound();

        speed.setString("AttributeName", "generic.attackSpeed");
        speed.setString("Name", "Blah");
        speed.setDouble("Amount", 9.8);
        speed.setInt("Operation", 0);
        speed.setInt("UUIDLeast", 1);
        speed.setInt("UUIDMost", 1);

        NBTTagList nbtTags = new NBTTagList();
        nbtTags.add(speed);
        nbt.set("AttributeModifiers", nbtTags);
        nmsStack.setTag(nbt);
        return new ItemBuilder(CraftItemStack.asCraftMirror(nmsStack));
    }
     */

    //public ItemBuilder setNBT(String key, NBTBaseMirror nbtBase) {
    //    return
    //}

    /**
     * Cool concept, and work as intended,
     * but item does like equivalent to no damage on hit
     * @return
     */
    @Deprecated
    public ItemBuilder fast() {
        ItemStackMirror nmsStack = new ItemStackMirror(itemStack);

        NBTTagCompoundMirror nbt = nmsStack.getOrCreateTag();

        NBTTagCompoundMirror speed = new NBTTagCompoundMirror();

        speed.setString("AttributeName", "generic.attackSpeed");
        speed.setString("Name", "Blah");
        speed.setDouble("Amount", 9.8);
        speed.setInt("Operation", 0);
        speed.setInt("UUIDLeast", 1);
        speed.setInt("UUIDMost", 1);

        NBTTagListMirror nbtTags = new NBTTagListMirror();
        nbtTags.add(speed);
        nbt.set("AttributeModifiers", nbtTags);
        nmsStack.setTag(nbt);
        return new ItemBuilder(nmsStack.getItemStack());
    }

    public ItemBuilder dye(Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(color);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder fireworkEffect(FireworkEffect effect) {
        FireworkEffectMeta meta = (FireworkEffectMeta) itemStack.getItemMeta();
        meta.setEffect(effect);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStack toItem() {
        return itemStack;
    }

}
