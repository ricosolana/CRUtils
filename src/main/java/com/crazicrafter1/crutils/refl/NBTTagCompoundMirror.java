package com.crazicrafter1.crutils.refl;

import com.crazicrafter1.crutils.ReflectionUtil;

public class NBTTagCompoundMirror extends NBTBaseMirror {

    public NBTTagCompoundMirror() {
        this(ReflectionUtil.invokeConstructor(Mirror.CLASS_NBTTagCompound));
    }

    public NBTTagCompoundMirror(Object instance) {
       super(instance);
    }

    public NBTBaseMirror set(String key, NBTBaseMirror value) {
        return new NBTBaseMirror(ReflectionUtil.invokeMethod(Mirror.METHOD_set, instance, key, value.instance));
    }

    public void setInt(String key, int i) {
        ReflectionUtil.invokeMethod(Mirror.METHOD_setInt, instance, key, i);
    }

    public void setDouble(String key, double d) {
        ReflectionUtil.invokeMethod(Mirror.METHOD_setDouble, instance, key, d);
    }

    public void setString(String key, String s) {
        ReflectionUtil.invokeMethod(Mirror.METHOD_setString, instance, key, s);
    }

    public String getString(String key) {
        return (String) ReflectionUtil.invokeMethod(Mirror.METHOD_getString, instance, key);
    }

}
