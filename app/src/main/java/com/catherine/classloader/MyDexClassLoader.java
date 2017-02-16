package com.catherine.classloader;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.net.URL;
import java.util.Enumeration;

import dalvik.system.DexClassLoader;

/**
 * Created by Catherine on 2017/2/13.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */

/**
 * DexClassLoader:
 * A class loader that loads classes from .jar and .apk files containing a classes.dex entry.
 * This can be used to execute code not installed as part of an application.
 */
public class MyDexClassLoader extends DexClassLoader {
    private static final String TAG = "MyDexClassLoader";

    /**
     * @param application MyApplication
     * @param dexPath     String: the list of jar/apk files containing classes and resources, delimited by File.pathSeparator, which defaults to ":" on Android
     * @param soPath      native libraries (path of .so files)
     * @param classloader the parent class loader
     */
    public MyDexClassLoader(MyApplication application, String dexPath, String soPath, ClassLoader classloader) {
        super(dexPath, application.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath(), soPath.replace("files", "lib"),
                classloader);

        Log.i(TAG, "DexClassLoader " + classloader.toString());
        Log.i(TAG, "dexPath " + dexPath);
        Log.i(TAG, "optimizedDirectory " + application.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath());
        Log.i(TAG, "soPath_lib " + soPath);
        Log.i(TAG, "librarySearchPath " + soPath.replace("files", "lib"));

    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Log.i(TAG, "findClass:" + name);
        return super.findClass(name);
    }

    @Override
    public String findLibrary(String name) {
        Log.i(TAG, "findLibrary:" + name);
        return super.findLibrary(name);
    }

    @Override
    protected URL findResource(String name) {
        Log.i(TAG, "findResource:" + name);
        return super.findResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) {
        Log.i(TAG, "findResources:" + name);
        return super.findResources(name);
    }

    @Override
    protected synchronized Package getPackage(String name) {
        Log.i(TAG, "getPackage:" + name);
        return super.getPackage(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException{
        Log.d(TAG, "loadClass:" + name);
        return super.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
        Log.d(TAG, "loadClass:" + name + " : " + resolve);
        return super.loadClass(name, resolve);
    }
}