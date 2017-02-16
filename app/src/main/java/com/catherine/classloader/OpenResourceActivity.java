package com.catherine.classloader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Catherine on 2017/2/15.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */

public class OpenResourceActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "OpenResourceActivity";
    private TextView tv_console;
    private Button bt_call_method, bt_launch_apk;

    private Class<?> apkActivity;
    private Class<?> apkUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_resource2);
        tv_console = (TextView) findViewById(R.id.tv_console);
        bt_call_method = (Button) findViewById(R.id.bt_call_method);
        bt_call_method.setOnClickListener(this);
        bt_launch_apk = (Button) findViewById(R.id.bt_launch_apk);
        bt_launch_apk.setOnClickListener(this);

        openApk(MyConfig.apk2);
    }

    @Override
    protected void onDestroy() {
        //Remove the latest loaded-apk
        ((MyApplication) getApplication()).RemoveApk();
        Log.d(TAG, "onDestroy");
        Process.killProcess(Process.myPid());
        super.onDestroy();
    }

    public void openApk(String fileName) {
        String history = tv_console.getText().toString();
        tv_console.setText("Loading..." + "\n----\n" + history);

        logClassLoader("start to load apk");
        ((MyApplication) getApplication()).LoadApk(fileName);
        logClassLoader("apk loaded");

        //switch apks
        try {
            if (MyConfig.apk1.equals(fileName)) {
                apkActivity = getClassLoader().loadClass(MyConfig.APK1_ACTIVITY_MAIN);
                apkUtils = getClassLoader().loadClass(MyConfig.APK1_UTILS);

                Log.d(TAG, "Load the class of the apk by " + apkActivity.getClassLoader());

            } else if (MyConfig.apk2.equals(fileName)) {
                apkActivity = getClassLoader().loadClass(MyConfig.APK2_ACTIVITY_MAIN);
                apkUtils = getClassLoader().loadClass(MyConfig.APK2_UTILS);
            }
            history = tv_console.getText().toString();
            tv_console.setText(getApkInfo(fileName) + "\n----\n" + "Done!" + "\n----\n" + history);
        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof ClassNotFoundException) {
                history = tv_console.getText().toString();
                tv_console.setText("Have you ever put your apk into correct directory?" + "\n----\n" + history);
            }
        }
    }

    private void logClassLoader(String msg) {
        ClassLoader oldloader = getClass().getClassLoader();
        int sum = 0;
        try {
            while (oldloader != null) {
                Log.e(msg + sum, "" + oldloader);
                sum++;
                oldloader = oldloader.getParent();
            }
            Log.e(msg + sum, "" + oldloader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getApkInfo(String fileName) {
        try {
            String dexPath = null;
            if (getExternalFilesDir(null) != null) {
                dexPath = new File(getExternalFilesDir(null), fileName).getAbsolutePath();
            } else if (getFilesDir() != null) {
                dexPath = new File(getFilesDir(), fileName).getAbsolutePath();
            }

            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(dexPath, 0);

            StringBuilder sb = new StringBuilder();
            sb.append("\n*** Apk info ***\n");
            sb.append("versionCode:" + info.versionCode);
            sb.append("\nversionName:" + info.versionName);
            sb.append("\n*** Apk info ***\n");

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_call_method:
                try {
                    //set null as the first parameter of invoke() while invoking a static method.
                    //static String getInputStringStatic(String value)
                    Method getInputStringStatic = apkUtils.getDeclaredMethod("getInputStringStatic", String.class);
                    String returns1 = (String) getInputStringStatic.invoke(null, "Hello, I'm your classLoader");
                    String history = tv_console.getText().toString();
                    tv_console.setText("getInputStringStatic:\t" + returns1 + "\n----\n" + history);

                    //static int getInputIntStatic(Integer value)
                    Method getInputIntStatic = apkUtils.getDeclaredMethod("getInputIntStatic", Integer.class);
                    int returns2 = (Integer) getInputIntStatic.invoke(null, 86400);
                    history = tv_console.getText().toString();
                    tv_console.setText("getInputIntStatic:\t" + returns2 + "\n----\n" + history);

                    //static String getStringValueStatic()
                    Method getStringValueStatic = apkUtils.getDeclaredMethod("getStringValueStatic");
                    String returns3 = (String) getStringValueStatic.invoke(null);
                    history = tv_console.getText().toString();
                    tv_console.setText("getStringValueStatic:\t" + returns3 + "\n----\n" + history);

                    //static int getIntValueStatic()
                    Method getIntValueStatic = apkUtils.getDeclaredMethod("getIntValueStatic");
                    int returns4 = (Integer) getIntValueStatic.invoke(null);
                    history = tv_console.getText().toString();
                    tv_console.setText("getIntValueStatic:\t" + returns4 + "\n----\n" + history);


                    //Get constructor for not-static method
                    Constructor<?> cons = apkUtils.getConstructor();

                    //String getStringValue()
                    Method getStringValue = apkUtils.getDeclaredMethod("getStringValue");
                    String returns5 = (String) getStringValue.invoke(cons.newInstance());
                    history = tv_console.getText().toString();
                    tv_console.setText("getStringValue:\t" + returns5 + "\n----\n" + history);

                    //int getIntValue()
                    Method getIntValue = apkUtils.getDeclaredMethod("getIntValue");
                    int returns6 = (Integer) getIntValue.invoke(cons.newInstance());
                    history = tv_console.getText().toString();
                    tv_console.setText("getIntValue:\t" + returns6 + "\n----\n" + history);

                    //Fields
                    Field myStaticField = apkUtils.getDeclaredField("myStaticField");

                    history = tv_console.getText().toString();
                    tv_console.setText(myStaticField.getName() + ":\t" + myStaticField.get(null) + "\n----\n" + history);

                    myStaticField.setAccessible(true);//You can update the field.
                    myStaticField.set(null, "new value");
                    myStaticField.setAccessible(false);

                    history = tv_console.getText().toString();
                    tv_console.setText(myStaticField.getName() + " updated:\t" + myStaticField.get(null) + "\n----\n" + history);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();

                    String history = tv_console.getText().toString();
                    tv_console.setText("Please load any apk first." + "\n----\n" + history);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bt_launch_apk:
                try {
                    Intent intent = new Intent();
                    intent.setClass(OpenResourceActivity.this, apkActivity);
                    startActivity(intent);
                } catch (NullPointerException e) {
                    e.printStackTrace();

                    String history = tv_console.getText().toString();
                    tv_console.setText("Please load any apk first." + "\n----\n" + history);
                }
                break;
        }
    }
}
