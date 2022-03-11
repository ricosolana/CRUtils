package com.crazicrafter1.crutils.refl;

import com.crazicrafter1.crutils.ReflectionUtil;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class ItemStackMirror {

    public static Field FIELD_nbtTag = ReflectionUtil.findField(Mirror.CLASS_ItemStack, "NBTTagCompound");

    private Object instance;

    public ItemStackMirror(ItemStack itemStack) {
        instance = ReflectionUtil.invokeStaticMethod(Mirror.METHOD_asNMSCopy, itemStack);
    }

    public ItemStack getItemStack() {
        return (ItemStack) ReflectionUtil.invokeStaticMethod(Mirror.METHOD_asCraftMirror, instance);
    }

    /**
     * Will return an instance to the nbt tag of this NMS ItemStack
     * @return wrapper to NBTTagCompound
     */
    public NBTTagCompoundMirror getOrCreateTag() {
        NBTTagCompoundMirror nbt = getTag();
        if (nbt == null)
            setTag(new NBTTagCompoundMirror());

        return getTag();
    }

    public NBTTagCompoundMirror getTag() {
        //Object nbt = ReflectionUtil.invokeMethod(Mirror.METHOD_getTag, instance);
        Object nbtTagCompound = ReflectionUtil.getFieldInstance(FIELD_nbtTag, instance);
        if (nbtTagCompound == null)
            return null;
        return new NBTTagCompoundMirror(nbtTagCompound);
    }

    public void setTag(NBTTagCompoundMirror mirror) {
        ReflectionUtil.setFieldInstance(FIELD_nbtTag, instance, mirror.instance);
        //ReflectionUtil.invokeMethod(Mirror.METHOD_setTag, instance, mirror.instance);
    }
}
