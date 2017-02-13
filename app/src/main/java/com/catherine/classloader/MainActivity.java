package com.catherine.classloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends Activity implements View.OnClickListener {
    private TextView tv_console;
    private Button bt_load_apk1, bt_call_method, bt_launch_apk, bt_load_apk2;

    private Class<?> apkActivity;
    private Class<?> apkUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_console = (TextView) findViewById(R.id.tv_console);
        bt_load_apk1 = (Button) findViewById(R.id.bt_load_apk1);
        bt_load_apk1.setOnClickListener(this);
        bt_call_method = (Button) findViewById(R.id.bt_call_method);
        bt_call_method.setOnClickListener(this);
        bt_launch_apk = (Button) findViewById(R.id.bt_launch_apk);
        bt_launch_apk.setOnClickListener(this);
        bt_load_apk2 = (Button) findViewById(R.id.bt_load_apk2);
        bt_load_apk2.setOnClickListener(this);


        //download apk from your server and save it to Android/data/this app's package name/files/.
        //you can just put your apks into Android/data/this app's package name/files/.
        tv_console.setText("Download apk...\n");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_load_apk1:
                ((MyApplication) getApplication()).RemoveApk();
                openApk(MyConfig.apk1);
                break;
            case R.id.bt_load_apk2:
                ((MyApplication) getApplication()).RemoveApk();
                openApk(MyConfig.apk2);
                break;
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
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bt_launch_apk:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, apkActivity);
                startActivity(intent);
                break;
        }
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
            } else if (MyConfig.apk2.equals(fileName)) {
                apkActivity = getClassLoader().loadClass(MyConfig.APK2_ACTIVITY_MAIN);
                apkUtils = getClassLoader().loadClass(MyConfig.APK2_UTILS);
            }
            history = tv_console.getText().toString();
            tv_console.setText("Done!" + "\n----\n" + history);
        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof ClassNotFoundException) {
                history = tv_console.getText().toString();
                tv_console.setText("Have you ever put your apk into correct dictionary?" + "\n----\n" + history);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Remove the latest loaded-apk
        ((MyApplication) getApplication()).RemoveApk();
        Log.d("MainActivity", "onDestroy");
    }
}
