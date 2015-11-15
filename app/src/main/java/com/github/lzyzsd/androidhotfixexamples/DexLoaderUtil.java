package com.github.lzyzsd.androidhotfixexamples;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by bruce on 11/14/15.
 */
public class DexLoaderUtil {
    private static final String TAG = "DexLoaderUtil";
    public static final String SECONDARY_DEX_NAME = "lib.apk";
    public static final String THIRD_DEX_NAME = "lib2.apk";
    private static final int BUF_SIZE = 8 * 1024;

    public static String getDexPath(Context context, String dexName) {
        return new File(context.getDir("dex", Context.MODE_PRIVATE), dexName).getAbsolutePath();
    }

    public static String getOptimizedDexPath(Context context) {
        return context.getDir("outdex", Context.MODE_PRIVATE).getAbsolutePath();
    }

    public static void copyDex(Context context, String dexName) {
        File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE),
                dexName);
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;

        try {
            bis = new BufferedInputStream(context.getAssets().open(dexName));
            dexWriter = new BufferedOutputStream(
                    new FileOutputStream(dexInternalStoragePath));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAndCall(Context context, String dexName) {
        final File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE), dexName);
        final File optimizedDexOutputPath = context.getDir("outdex", Context.MODE_PRIVATE);

        DexClassLoader cl = new DexClassLoader(dexInternalStoragePath.getAbsolutePath(),
                optimizedDexOutputPath.getAbsolutePath(),
                null,
                context.getClassLoader());
        call(cl);
    }

    public static void call(ClassLoader cl) {
        Class myClasz = null;
        try {
            myClasz =
                    cl.loadClass("com.example.MyClass");
            Object instance = myClasz.getConstructor().newInstance();
            myClasz.getDeclaredMethod("test1").invoke(instance);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Boolean injectAboveEqualApiLevel14(
            String dexPath, String defaultDexOptPath, String nativeLibPath, String dummyClassName) {
        Log.i(TAG, "--> injectAboveEqualApiLevel14");
        PathClassLoader pathClassLoader = (PathClassLoader) DexLoaderUtil.class.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, defaultDexOptPath, nativeLibPath, pathClassLoader);
        try {
            dexClassLoader.loadClass(dummyClassName);
            Object dexElements = combineArray(
                    getDexElements(getPathList(pathClassLoader)),
                    getDexElements(getPathList(dexClassLoader)));

            Object pathList = getPathList(pathClassLoader);
            setField(pathList, pathList.getClass(), "dexElements", dexElements);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        Log.i(TAG, "<-- injectAboveEqualApiLevel14 End.");
        return true;
    }

    private static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }


    private static Object getDexElements(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }


    private static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }


    private static void setField(Object obj, Class<?> cl, String field, Object value)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }
}
