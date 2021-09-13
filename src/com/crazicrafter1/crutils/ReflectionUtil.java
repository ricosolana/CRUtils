package com.crazicrafter1.crutils;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public final class ReflectionUtil {

    private final static String CRAFTBUKKIT = Bukkit.getServer().getClass().getPackage().getName();
    private final static String NMS = "net.minecraft.server." + CRAFTBUKKIT.substring(23);

    public static boolean isOldVersion() {
        String v = CRAFTBUKKIT.substring(23);
        return !v.contains("1_17");
    }

    // Not instantiable
    private ReflectionUtil() { }

    // get class by package dir
    public static Class getCanonicalClass(final String canonicalName) {
        try {
            return Class.forName(canonicalName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot find " + canonicalName, e);
        }
    }

    public static Class<?> getCraftClass(String name) {
        return getCanonicalClass(CRAFTBUKKIT + "." + name);
    }

    public static Class<?> getNMSClass(String name) {
        return getCanonicalClass(NMS + "." + name);
    }

    public static Method getMethod(Class<?> clazz, String method, Class<?>... params) {
        try {
            return clazz.getDeclaredMethod(method, params);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot find " + method, e);
        }
    }

    public static Object invokeStaticMethod(Method method, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeMethod(Method method, Object instance, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Constructor<?> getConstructor(String clazzName, Class<?>... params) {
        Class<?> clazz = getCanonicalClass(clazzName);
        try {
            return clazz.getConstructor(params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... params) {
        try {
            return clazz.getConstructor(params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Construct a new object where args are Objects to be evaluated at runtime
     * constructor will be loaded by gathering the class type of each arg
     * @args notnull array
     */
    public static Object invokeConstructor(Class<?> clazz, Object... args) {
        try {
            ArrayList<Class<?>> params = new ArrayList<>(args.length);
            for (Object arg : args) {
                params.add(arg.getClass());
            }
            Constructor<?> constructor = clazz.getConstructor(params.toArray(new Class<?>[]{}));

            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeConstructor(Constructor<?> constructor, Object... args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getFieldInstance(Field field, Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
