package com.catherine.resource1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "Resource1 MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // You can's just use setContentView(@LayoutRes int layoutResID), you must use setContentView(View view)
        // setContentView(R.layout.activity_main);
        setContentView(View.inflate(getApplicationContext(), R.layout.activity_main, null));

        printHowClassLoaderWorks();
    }

    private void printHowClassLoaderWorks() {
        Log.i(TAG, "Load core java libraries by " + String.class.getClassLoader());
        Log.i(TAG, "Load user-defined classes by " + MainActivity.class.getClassLoader());
        Log.i(TAG, "Load third party libraries by " + AppCompatActivity.class.getClassLoader());//what you imported from gradle or libs/
        Log.i(TAG, "Default classLoader is " + getClassLoader());
        Log.i(TAG, "Default system classLoader is " + ClassLoader.getSystemClassLoader());

        if (getClassLoader() == ClassLoader.getSystemClassLoader())
            Log.d(TAG, "Default class loader is equal to default system class loader.");
        else
            Log.e(TAG, "Default class loader is NOT equal to default system class loader.");
    }
}
