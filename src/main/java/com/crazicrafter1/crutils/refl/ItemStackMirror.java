package com.crazicrafter1.crutils.refl;

import com.crazicrafter1.crutils.ReflectionUtil;
import org.bukkit.inventory.ItemStack;

public class ItemStackMirror {

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
        //Object nbt = ReflectionUtil.invokeMethod(Mirror.METHOD_getTag, instance);
        if (nbt == null)
            setTag(new NBTTagCompoundMirror());

        return getTag(); //new NBTTagCompoundMirror(nbt.instance);
    }

    public NBTTagCompoundMirror getTag() {
        Object nbt = ReflectionUtil.invokeMethod(Mirror.METHOD_getTag, instance);
        if (nbt == null)
            return null;
        return new NBTTagCompoundMirror(nbt);
    }

    public void setTag(NBTTagCompoundMirror mirror) {
        ReflectionUtil.invokeMethod(Mirror.METHOD_setTag, instance, mirror.instance);
    }

    //static net.minecraft.world.item.ItemStack i;
    //static {
    //    //i.getOrCreateTag()
    //}

}
