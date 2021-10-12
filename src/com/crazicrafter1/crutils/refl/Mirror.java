package com.crazicrafter1.crutils.refl;

import com.crazicrafter1.crutils.ReflectionUtil;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class Mirror {

    static Class<?> CLASS_CraftItemStack;
    static Class<?> CLASS_ItemStack;
    static Class<?> CLASS_NBTTagCompound;
    static Class<?> CLASS_NBTBase;
    static Class<?> CLASS_NBTTagList;

    static Method METHOD_asNMSCopy;
    static Method METHOD_asCraftMirror;
    static Method METHOD_getTag;
    static Method METHOD_setTag;

    static Method METHOD_set;
    static Method METHOD_setInt;
    static Method METHOD_setDouble;
    static Method METHOD_setString;

    static Method METHOD_getString;
    //static Method METHOD_add;

    static {
        try {
            CLASS_CraftItemStack = ReflectionUtil.getCraftClass("inventory.CraftItemStack");

            METHOD_asNMSCopy = ReflectionUtil.getMethod(CLASS_CraftItemStack, "asNMSCopy", ItemStack.class);
            CLASS_ItemStack = METHOD_asNMSCopy.getReturnType();
            METHOD_asCraftMirror = ReflectionUtil.getMethod(CLASS_CraftItemStack, "asCraftMirror", CLASS_ItemStack);


            // NMS ItemStack has an instance method to get tag
            // get it
            METHOD_getTag = ReflectionUtil.getMethod(CLASS_ItemStack, "getTag");
            CLASS_NBTTagCompound = METHOD_getTag.getReturnType();
            METHOD_setTag = ReflectionUtil.getMethod(CLASS_ItemStack, "setTag", CLASS_NBTTagCompound);

            //static final Class<?> CLASS_NBTBase = ReflectionUtil.getMethod(CLASS_NBTTagCompound,"get").getReturnType();
            CLASS_NBTBase = CLASS_NBTTagCompound.getInterfaces()[0];
            CLASS_NBTTagList = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "getList", String.class, int.class).getReturnType();

            METHOD_set = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "set", String.class, CLASS_NBTBase);
            METHOD_setInt = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "setInt", String.class, int.class);
            METHOD_setDouble = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "setDouble", String.class, double.class);
            METHOD_setString = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "setString", String.class, String.class);

            METHOD_getString = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "getString", String.class);

            //METHOD_add = ReflectionUtil.getMethod(CLASS_NBTTagList, "add", CLASS_NBTBase);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







/*
    static final Class<?> CLASS_CraftItemStack = ReflectionUtil.getCraftClass("inventory.CraftItemStack");

    static final Method METHOD_asNMSCopy = ReflectionUtil.getMethod(CLASS_CraftItemStack, "asNMSCopy", ItemStack.class);
    static final Class<?> CLASS_ItemStack = METHOD_asNMSCopy.getReturnType();
    static final Method METHOD_asCraftMirror = ReflectionUtil.getMethod(CLASS_CraftItemStack, "asCraftMirror", CLASS_ItemStack);


    // NMS ItemStack has an instance method to get tag
    // get it
    static final Method METHOD_getTag = ReflectionUtil.getMethod(CLASS_ItemStack, "getTag");
    static final Class<?> CLASS_NBTTagCompound = METHOD_getTag.getReturnType();
    static final Method METHOD_setTag = ReflectionUtil.getMethod(CLASS_ItemStack, "setTag", CLASS_NBTTagCompound);


    //static final Class<?> CLASS_NBTBase = ReflectionUtil.getMethod(CLASS_NBTTagCompound,"get").getReturnType();
    static final Class<?> CLASS_NBTBase = CLASS_NBTTagCompound.getInterfaces()[0];
    static final Class<?> CLASS_NBTTagList = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "getList", String.class, int.class).getReturnType();

    static final Method METHOD_set = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "set", String.class, CLASS_NBTBase);
    static final Method METHOD_setInt = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "setInt", String.class, int.class);
    static final Method METHOD_setDouble = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "setDouble", String.class, int.class);
    static final Method METHOD_setString = ReflectionUtil.getMethod(CLASS_NBTTagCompound, "setString", String.class, int.class);

    static final Method METHOD_add = ReflectionUtil.getMethod(CLASS_NBTTagList, "add", int.class, CLASS_NBTBase);


 */



}
