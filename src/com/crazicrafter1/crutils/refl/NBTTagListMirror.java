package com.crazicrafter1.crutils.refl;

import com.crazicrafter1.crutils.ReflectionUtil;

import java.util.AbstractList;

public class NBTTagListMirror extends NBTBaseMirror {

    public NBTTagListMirror() {
        this(ReflectionUtil.invokeConstructor(Mirror.CLASS_NBTTagList));
    }

    public NBTTagListMirror(Object instance) {
        super(instance);
    }

    /**
     * This add wrapper method wraps NBTList java AbstractList<T>
     * @param instanceNBTBase
     */
    public void add(NBTBaseMirror instanceNBTBase) {
        // add is of AbstractList
        AbstractList<Object> listInstance = (AbstractList<Object>) instance;
        listInstance.add(instanceNBTBase.instance);
        //ReflectionUtil.invokeMethod(Mirror.METHOD_add, instance, instanceNBTBase.instance);
    }

}
