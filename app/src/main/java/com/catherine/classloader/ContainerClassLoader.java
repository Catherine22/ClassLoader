package com.catherine.classloader;

import android.content.Context;
import android.util.Log;

import dalvik.system.DexClassLoader;

/**
 * Created by Catherine on 2017/2/13.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */

public class ContainerClassLoader extends DexClassLoader {
    private static final String TAG = "ContainerClassLoader";

    /**
     * dexPath：apk path
     * application.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath()：path of .dex files decompiling from a apk
     * soPath.replace("files", "lib")：native libraries (path of .so files)
     */
    public ContainerClassLoader(MyApplication application, String dexPath, String soPath, ClassLoader classloader) {
        super(dexPath, application.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath(), soPath.replace("files", "lib"),
                classloader);
        Log.w(TAG, "ContainerClassLoader " + classloader.toString());
        Log.w(TAG, "dexPath " + dexPath);
        Log.w(TAG, "optimizedDirectory " + application.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath());
        Log.w(TAG, "soPath_lib " + soPath.replace("files", "lib"));
    }
}