package com.crazicrafter1.crutils.refl;

import com.crazicrafter1.crutils.ReflectionUtil;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class Mirror {

    public static Class<?> CLASS_CraftItemStack;
    public static Class<?> CLASS_ItemStack;
    public static Class<?> CLASS_NBTTagCompound;
    public static Class<?> CLASS_NBTBase;
    public static Class<?> CLASS_NBTTagList;

    public static Method METHOD_asNMSCopy;
    public static Method METHOD_asCraftMirror;

    static {
        try {
            CLASS_CraftItemStack = ReflectionUtil.getCraftBukkitClass("inventory.CraftItemStack");

            METHOD_asNMSCopy = ReflectionUtil.getMethod(CLASS_CraftItemStack, "asNMSCopy", ItemStack.class);
            CLASS_ItemStack = METHOD_asNMSCopy.getReturnType();
            METHOD_asCraftMirror = ReflectionUtil.getMethod(CLASS_CraftItemStack, "asCraftMirror", CLASS_ItemStack);

            CLASS_NBTTagCompound = ItemStackMirror.FIELD_nbtTag.getType();

            try {
                CLASS_NBTBase = CLASS_NBTTagCompound.getInterfaces()[0];
            } catch (Exception e) {
                // 1.12.2 derives from a class instead of an interface
                CLASS_NBTBase = CLASS_NBTTagCompound.getSuperclass();
            }

            CLASS_NBTTagList = ReflectionUtil.findMethod(CLASS_NBTTagCompound, "NBTTagList", String.class, int.class).getReturnType();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {}

}
