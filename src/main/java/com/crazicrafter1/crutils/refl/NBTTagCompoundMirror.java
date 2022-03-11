package com.crazicrafter1.crutils.refl;

import com.crazicrafter1.crutils.Main;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Version;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;

public class NBTTagCompoundMirror extends NBTBaseMirror {

    public static Method METHOD_set = ReflectionUtil.findMethod(Mirror.CLASS_NBTTagCompound,        Mirror.CLASS_NBTBase,   String.class, Mirror.CLASS_NBTBase);
    public static Method METHOD_setInt = ReflectionUtil.findMethod(Mirror.CLASS_NBTTagCompound,     void.class,             String.class, int.class);
    public static Method METHOD_setDouble = ReflectionUtil.findMethod(Mirror.CLASS_NBTTagCompound,  void.class,             String.class, double.class);
    public static Method METHOD_setString = ReflectionUtil.findMethod(Mirror.CLASS_NBTTagCompound,  void.class,             String.class, String.class);
    public static Method METHOD_getString;

    static {
        if (Version.AT_MOST_v1_17.a()) {
            METHOD_getString = ReflectionUtil.getMethod(Mirror.CLASS_NBTTagCompound, "getString", String.class);
        } else if (Version.v1_18.a()) {
            METHOD_getString = ReflectionUtil.getMethod(Mirror.CLASS_NBTTagCompound, "l", String.class);
        }
    }

    public NBTTagCompoundMirror() {
        this(ReflectionUtil.invokeConstructor(Mirror.CLASS_NBTTagCompound));
    }

    public NBTTagCompoundMirror(Object instance) {
       super(instance);
    }

    public NBTBaseMirror set(String key, NBTBaseMirror value) {
        return new NBTBaseMirror(ReflectionUtil.invokeMethod(METHOD_set, instance, key, value.instance));
    }

    public void setInt(String key, int i) {
        ReflectionUtil.invokeMethod(METHOD_setInt, instance, key, i);
    }

    public void setDouble(String key, double d) {
        ReflectionUtil.invokeMethod(METHOD_setDouble, instance, key, d);
    }

    public void setString(String key, String s) {
        ReflectionUtil.invokeMethod(METHOD_setString, instance, key, s);
    }

    public String getString(String key) {
        return (String) ReflectionUtil.invokeMethod(METHOD_getString, instance, key);
    }

    @Override
    public String toString() {
        return "" + instance;
    }
}
