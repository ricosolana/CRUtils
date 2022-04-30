package com.crazicrafter1.crutils;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

public enum ReflectionUtil {
    ;

    private final static String CRAFT_BUKKIT = Bukkit.getServer().getClass().getPackage().getName();
    private final static String NET_MINECRAFT;
            //ReflectionUtil.getMethod(
            //ReflectionUtil.getCraftBukkitClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class).getReturnType().getPackage().getName();

    // org\bukkit\craftbukkit\v1_8_R3\
    // equals: v1_8_R3
    final static String VERSION = CRAFT_BUKKIT.substring(CRAFT_BUKKIT.lastIndexOf(".")+1);
    final static int VERSION_MAJOR = Integer.parseInt(VERSION.substring(1, VERSION.indexOf("_R")).split("_")[1]);
    //final static int VERSION_MINOR = Integer.parseInt(VERSION.substring(1, VERSION.indexOf("_R")).split("_")[2]);

    static {
        // CrashReport remains consistent in root package across versions

        // net\minecraft\server\v1_8_R3\CrashReport
        // net\minecraft\server\v1_14_R1\CrashReport
        // net\minecraft\CrashReport
        Class<?> crashReport;
        try {
            crashReport = ReflectionUtil.getCanonicalClass(
                    "net.minecraft.server." + VERSION + ".CrashReport");

        } catch (Exception e) {
            crashReport = ReflectionUtil.getCanonicalClass("net.minecraft.CrashReport");
        }

        NET_MINECRAFT = crashReport.getPackage().getName();
    }

    // get class by package dir
    public static Class<?> getCanonicalClass(final String canonicalName) {
        try {
            return Class.forName(canonicalName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot find " + canonicalName, e);
        }
    }

    public static Class<?> getCraftBukkitClass(String name) {
        return getCanonicalClass(CRAFT_BUKKIT + "." + name);
    }

    public static Class<?> getNMClass(String name) {
        return getCanonicalClass(NET_MINECRAFT + "." + name);
    }

    public static Method getMethod(Class<?> clazz, String method, Class<?>... params) {
        try {
            //System.out.println("Trying to find method: " + method);
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

    public static void setFieldInstance(Field field, Object instance, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Locate a field in the clazz, which is of type clazzType
     * @param clazz
     * @param type
     * @return
     */
    public static Field findField(Class<?> clazz, String type) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().getSimpleName().equals(type)) return field;
        }
        throw new NoSuchFieldError("No field with type " + type + " in class: " + clazz.getName());
    }

    public static Field findField(Class<?> clazz, Class<?> type) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(type)) return field;
        }
        throw new NoSuchFieldError("No field with type " + type + " in class: " + clazz.getName());
    }

    public static Method findMethod(Class<?> clazz, Class<?> returnType, Class<?>... params) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getReturnType().equals(returnType)
                    && Arrays.equals(params, method.getParameterTypes()))
                return method;
        }
        throw new NoSuchMethodError("No method with params " + Arrays.toString(params) + " and return type " + returnType + " in class: " + clazz.getName());
    }

    public static Method findMethod(Class<?> clazz, String returnType, Class<?>... params) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getReturnType().getSimpleName().equals(returnType)
                    && Arrays.equals(params, method.getParameterTypes()))
                return method;
        }
        throw new NoSuchMethodError("No method with params " + Arrays.toString(params) + " and return type " + returnType + " in class: " + clazz.getName());
    }

    public static Method findMethod(Class<?> clazz, Class<?> returnType, String... params) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getReturnType().equals(returnType)
                    && Arrays.equals(params, Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).toArray()))
                return method;
        }
        throw new NoSuchMethodError("No method with params " + Arrays.toString(params) + " and return type " + returnType + " in class: " + clazz.getName());
    }

    public static Method findMethod(Class<?> clazz, String returnType, String... params) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getReturnType().getSimpleName().equals(returnType)
                    && Arrays.equals(params, Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).toArray()))
                return method;
        }
        throw new NoSuchMethodError("No method with params " + Arrays.toString(params) + " and return type " + returnType + " in class: " + clazz.getName());
    }

    // flaggers

}
