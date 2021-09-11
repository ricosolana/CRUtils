package com.crazicrafter1.crutils;

import com.crazicrafter1.crutils.refl.GameProfileMirror;
import com.crazicrafter1.crutils.refl.PropertyMirror;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    private final ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = new ItemStack(itemStack);
    }

    // lexicals
    public ItemBuilder mergeLexicals(ItemStack itemStack) {
        return this.name(itemStack.getItemMeta().getDisplayName()).lore(itemStack.getItemMeta().getLore());
    }

    private static Object makeProfile(String b64) {
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


    public ItemBuilder skull(String base64) {
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

        Method setProfileMethod = ReflectionUtil.getMethod(
                ReflectionUtil.getCraftClass("inventory.CraftMetaSkull"),
                "setProfile",
                GameProfileMirror.gameProfileClass);

        ReflectionUtil.invokeMethod(setProfileMethod, meta, makeProfile(base64));

        itemStack.setItemMeta(meta);
        return this;
    }

    /**
        Set the custom model data of the item to work with a texture pack
     @param i the id of the texture
     */
    public ItemBuilder customModelData(Integer i) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(i);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
        Set the custom display name of an item
     @param name the name with &codes
     */
    public ItemBuilder name(String name) {
        ItemMeta meta = itemStack.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r" + name));
        itemStack.setItemMeta(meta);

        return this;
    }

    public ItemBuilder resetName() {
        return name(null);
    }

    public String getName() {
        return itemStack.getItemMeta().getDisplayName();
    }

    /**
        Set the lore of an item
     @param lore the lore
     */
    public ItemBuilder lore(String lore) {
        return lore(lore.split("\n"));
    }

    public ItemBuilder lore(String[] lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        if (lore != null) {
            ItemMeta meta = itemStack.getItemMeta();

            for (int i = 0; i < lore.size(); i++)
                lore.set(i, ChatColor.translateAlternateColorCodes('&', "&r" + lore.get(i)));

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

    /**
       Set the color of leather armor or a potion
     @param r red
     @param g green
     @param b blue
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
     * Set the amount of items
     */
    public ItemBuilder count(int c) {
        itemStack.setAmount(c);
        return this;
    }

    /**
       Flag an item as unbreakable
     */
    public ItemBuilder unbreakable() {
        ItemMeta meta = itemStack.getItemMeta();

        meta.setUnbreakable(true);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder enchant(Enchantment e, int level) {
        itemStack.addUnsafeEnchantment(e, level);

        return this;
    }

    public ItemBuilder hideFlags(ItemFlag ... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(flags);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Doesn't work correctly in 1.17
     */
    @Deprecated
    public ItemBuilder glow(boolean state) {

        if (true)
            return this;

        if (!state) return this;
        //itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        return this;
    }

    /*
    public ItemBuilder fast(){
        ItemStack item = new ItemStack(itemStack);

        net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        NBTTagCompound nbt = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();

        NBTTagList nbtTags = new NBTTagList();
        NBTTagCompound speed = new NBTTagCompound();

        speed.set("AttributeName", new NBTTagString("generic.attackSpeed"));
        speed.set("Name", new NBTTagString("Blah"));
        speed.set("Amount", new NBTTagDouble(9.8));
        speed.set("Operation", new NBTTagInt(0));
        speed.set("UUIDLeast", new NBTTagInt(1));
        speed.set("UUIDMost", new NBTTagInt(1));

        nbtTags.add(speed);
        nbt.set("AttributeModifiers", nbtTags);
        nmsStack.setTag(nbt);
        return new ItemBuilder(CraftItemStack.asCraftMirror(nmsStack));
    }
     */

    public ItemStack toItem() {
        return itemStack;
    }

}
