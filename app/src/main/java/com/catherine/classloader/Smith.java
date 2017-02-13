package com.catherine.classloader;

import java.lang.reflect.Field;

/**
 * Created by Catherine on 2017/2/13.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */

public class Smith<T> {
    private Object obj; //ClassLoader

    private boolean inited; //first time app launch
    private Field field;

    public Smith(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("obj cannot be null");
        }
        this.obj = obj;
    }

    private void prepare() {
        if (inited)
            return;
        inited = true;
        Class<?> c = obj.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField("parent"); //f here might be the classLoader of loaded classLoader's parents
                f.setAccessible(true);
                field = f;
                return;
            } catch (Exception e) {
//                e.printStackTrace(); // It's fine that you can't find the field at the first three executions.
            } finally {
                c = c.getSuperclass();
            }
        }
    }

    /**
     * 20160418 應該是在取得 (Classloder)Oldloader 的父 Classloder
     */
    public T get() throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
        prepare();

        if (field == null)
            throw new NoSuchFieldException();

        try {
            @SuppressWarnings("unchecked")
            T r = (T) field.get(obj);
            return r;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("unable to cast object");
        }
    }

    public void set(T val) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
        prepare();
        if (field == null)
            throw new NoSuchFieldException();

        field.set(obj, val);
    }
}
