package com.catherine.resource2;

/**
 * Created by Catherine on 2017/2/13.
 * yacatherine19@gmail.com
 */


public class Utils {
    public static String myStaticField = "Default field";

    public static String getInputStringStatic(String value) {
        return value;
    }

    public static int getInputIntStatic(Integer value) {
        return value;
    }

    public static String getStringValueStatic() {
        return "(static) Hello from APK2";
    }

    public static int getIntValueStatic() {
        return 1234;
    }

    public String getStringValue() {
        return "Hello from APK2";
    }

    public int getIntValue() {
        return 4321;
    }
}