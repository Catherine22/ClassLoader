# ClassLoader

This repository hosts an example of dynamically loading an apk and in-depth documentation.

This repo demonstrates how apks can be loaded into an installed app via ClassLoader. Users can automatically update their application without reinstalling it. Once developers fix any bug or update new features, there is no need to re-build your apk (ClassLoader apk), developers upload patch apks to their server.

## Use case

1.  Build a ClassLoader apk and patch apk(s).
2.  Upload the patch apk to your server, only users who have installed ClassLoader apk can download it.
3.  Users install ClassLoader apk.
4.  Users open ClassLoader app and download the patch apk(s) which include(s) main business logic of your app.
5.  Verify patch apk(s) and load classes into the ClassLoader app.

Next, you will repeat the following steps again and again in your development life cycle:

1.  Add new features or fix bugs
2.  Build patch apk
3.  Update the patch apk in your server
4.  Users open ClassLoader app to download the patch apk and all the updates will be loaded to the installed app.

To do so, you must create two projects, ClassLoader and patch apk which combines features and/or layouts.

### The main job of ClassLoader app

-   Download and validate the patch apk(s) automatically.
-   Load classes of the patch apk(s).
-   Shrink your app and hide your code in patch apk(s).
-   Switch patch apks. I.e. You can run each patch apk independently without polluting their resources.

### The main job of patch apk(s)

-   Provide features like instant messaging, taking photos, scanning QR codes, login or anything.
-   UI components and business logic are both supported.

In this demo, ClassLoader app loads [Resource1.apk] and [Resource2.apk].

# Getting through the basis

## Java ClassLoader

There are three classLoaders used when running JVM:

1.  Bootstrap class loader
    Loading classes from <JAVA_HOME>/jre/lib directory
2.  Extension class loader
    Loading classes from <JAVA_HOME>/jre/lib/ext directory
3.  System class loader
    Loading classes from system class path (which is the same as the environment variable - CLASSPATH).

Besides, you could create your own class loader, aka. 'User-defined class loaders'.

## Android ClassLoader

Android virtual machine loads classes just like the way Java does, but they're slightly different.

### Dex

In an Android device, it packages your classes into one (or more) dex file(s) which is (are) located in an apk, and optimizes those dex files loading with Dalvik.

![enter description here][1]

(screenshot from https://youtu.be/skmOBriQ28E)

![enter description here][2]

(screenshot from https://youtu.be/skmOBriQ28E)

### ClassLoader

Here are class loaders works on Android:

| Class Loader    | Summary                                                                                                                                                    |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BootClassLoader | The top parent of the following classLoaders.                                                                                                              |
| PathClassLoader | Load classes located in data/app/... where your app installed.<br>Android uses this class for its system class loader and its application class loader(s). |
| DexClassLoader  | Load classes from .jar and .apk files containing a classes.dex entry. This can be used to execute code not installed as part of an application.            |
| URLClassLoader  | @hide<br>This class loader is used to load classes and resources from a search path of URLs referring to both JAR files and directories.                   |

First, let's focus on PathClassLoader and DexClassLoader. They both extend BaseDexClassLoader.

In PathClassLoader.class

```java
// set up optimizedDirectory to null to use the default system directory
public PathClassLoader(String dexPath, String libraryPath,
            ClassLoader parent) {
	super(dexPath, null, libraryPath, parent);
}
```

In DexClassLoader.class

```java
public DexClassLoader(String dexPath, String optimizedDirectory,
            String libraryPath, ClassLoader parent) {
	super(dexPath, new File(optimizedDirectory), libraryPath, parent);
}
```

It is noticeable that:

1. optimizedDirectory references to optimizes dex files.
2. PathClassLoader does not accept optimizedDirectory argument, so it is forced to associate the original optimized dex files.
3. We can pass the optimizedDirectory argument to DexClassLoader(). I.e. we can cache optimized dex files placed in internal storage.

That's why we can call DexClassLoader to load the user-defined apk, dex and jar files whereas PathClassLoader is responsible for loading the installed Apk.

The code snippet shows how ClassLoader works:

```java
package java.lang;
public abstract class ClassLoader {
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
            // First, check if the class has already been loaded
            Class c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        c = parent.loadClass(name, false);
                    } else {
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }

                if (c == null) {
                    // If still not found, then invoke findClass in order
                    // to find the class.
                    long t1 = System.nanoTime();
                    c = findClass(name);
                }
            }
            return c;
    }
}
```

It shows three steps to load a class.

Step1, check if the class has already been loaded

```java
findLoadedClass(name);
```

Step2, if the class is not found, we check if the class has already been loaded with the parent.

```java
if (parent != null) {
    c = parent.loadClass(name, false);
} else {
    c = findBootstrapClassOrNull(name);
}
```

Step3, if still not found, then invoke findClass in order to find the class.

```java
findClass(name);
```

The above code snippet presents that once a class has been loaded, it'll never be load again.

## Exercise

Let's play with some scenarios of class loaders.

```java
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Load core java libraries with " + String.class.getClassLoader());
        Log.i(TAG, "Load user-defined classes with " + MainActivity.class.getClassLoader());
        Log.i(TAG, "Load user-defined libraries with " + AppCompatActivity.class.getClassLoader()); // what you imports from gradle or libs/
        Log.i(TAG, "Default classLoader is " + getClassLoader());
        Log.i(TAG, "Default system classLoader is \"" + ClassLoader.getSystemClassLoader());
}
```

| Class Loader                               | Methods                                                                                                               |
| ------------------------------------------ | --------------------------------------------------------------------------------------------------------------------- |
| BootClassLoader                            | String.class.getClassLoader()                                                                                         |
| PathClassLoader[[DexPathList[[directory... | MainActivity.class.getClassLoader()<br>AppCompatActivity.class.getClassLoader()<br>ClassLoader.getSystemClassLoader() |
| PathClassLoader[DexPathList[[zip file...   | getClassLoader()                                                                                                      |

-   User-defined classes and libraries are loaded via PathClassLoader.
-   Core java libraries such as `java.lang.String` are loaded via BootClassLoader. Thus, you cannot create a String class and replace `java.lang.String` no matter they share the same package name and class name. **Android believes that they are two different classes because they are from different class loaders.**

> In Java, object A equates to Object B when they share the same package name, class name and **ClassLoader**

## How to use this app

-   This app is used to load classes from another apk, you can launch activities or call methods wrapped in another apk. That's why there is nothing but updating loading and verifying apks in this app.
-   Use `getClassLoader().loadClass()` to get activities from another apk, and access methods or fields by using Java reflection.

Here are some reflection examples:

Let's say Utils is the latest released feature, it will be loaded to your ClassLoader app.

```java
package com.catherine.resource1;

public class Utils {
    public static String myStaticField = "Default field";

    public static String getInputStringStatic(String value) {
        return value;
    }

    public static int getInputIntStatic(Integer value) {
        return value;
    }

    public static String getStringValueStatic() {
        return "(static) Hello, I'm apk1";
    }

    public static int getIntValueStatic() {
        return 1234;
    }

    public String getStringValue() {
        return "Hello, I'm apk1";
    }

    public int getIntValue() {
        return 4321;
    }
}
```

However, you cannot find the `Utils` class in your ClassLoader app. You cannot import it like any other classes as usual. You need to leverage Java reflection to access it.

1. Find the class

```java
private Class<?>  apkUtils;

try {
	apkUtils = getClassLoader().loadClass("com.catherine.resource1.Utils");
} catch (ClassNotFoundException e) {
	e.printStackTrace();
}
```

2. Access methods

```java
try {
//set null as the first parameter of invoke() while invoking a static method.
//static String getInputStringStatic(String value)
	Method getInputStringStatic = apkUtils.getDeclaredMethod("getInputStringStatic", String.class);
	String returns1 = (String) getInputStringStatic.invoke(null, "Hello, I'm your classLoader");
	Log.d("Reflection" , returns1);

//static int getInputIntStatic(Integer value)
	Method getInputIntStatic = apkUtils.getDeclaredMethod("getInputIntStatic", Integer.class);
	int returns2 = (Integer) getInputIntStatic.invoke(null, 86400);
	Log.d("Reflection" , returns2 + "");

//static String getStringValueStatic()
	 Method getStringValueStatic = apkUtils.getDeclaredMethod("getStringValueStatic");
	String returns3 = (String) getStringValueStatic.invoke(null);
	Log.d("Reflection" , returns3);

//static int getIntValueStatic()
	Method getIntValueStatic = apkUtils.getDeclaredMethod("getIntValueStatic");
	int returns4 = (Integer) getIntValueStatic.invoke(null);
	Log.d("Reflection" , returns4 + "");


//Get constructor for not-static method
	Constructor<?> cons = apkUtils.getConstructor();

//String getStringValue()
	Method getStringValue = apkUtils.getDeclaredMethod("getStringValue");
	String returns5 = (String) getStringValue.invoke(cons.newInstance());
	Log.d("Reflection" , returns5);

 //int getIntValue()
	Method getIntValue = apkUtils.getDeclaredMethod("getIntValue");
	int returns6 = (Integer) getIntValue.invoke(cons.newInstance());
	Log.d("Reflection" , returns6 + "");

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
}
```

3. You can access fields as well

```java
try {
	apkUtils = getClassLoader().loadClass("com.catherine.resource1.Utils");
} catch (ClassNotFoundException e) {
	e.printStackTrace();
}
try {
	Field myStaticField = apkUtils.getDeclaredField("myStaticField");
	Log.d("Reflection" , myStaticField.getName() + ":\t" + myStaticField.get(null));

	myStaticField.setAccessible(true);//You can update the field.
	myStaticField.set(null, "new value");
	myStaticField.setAccessible(false);

	Log.d("Reflection" , myStaticField.getName() + " updated:\t" + myStaticField.get(null));
} catch (NullPointerException e) {
	e.printStackTrace();
} catch (NoSuchFieldException e) {
	e.printStackTrace();
}
```

4.  Or maybe you don't want to use any methods or fields, you just launch the activity

```java
try {
	Class<?>  apkActivity = getClassLoader().loadClass("com.catherine.resource1.MainActivity");
	Intent intent = new Intent();
	intent.setClass(MainActivity.this, apkActivity);
	startActivity(intent);
} catch (ClassNotFoundException e) {
	e.printStackTrace();
}
```

# Warnings

Read the instructions carefully to make sure your app works properly.

1. Update Android Studio Settings

Disabled Instant Run when running classLoader application
![enter description here][3]

2. Place patch Apks in advance

Resources path: Android/data/package/files/xxx.apk

3. Disable Multidex

In build.gradle

```gradle
multiDexEnabled false
```

4. Register patch apk's activities and permissions in ClassLoader app's Manifest

Android studio probably throws some errors likes 'Unresolved package...', just ignore them. And don't forget to add the prefix of your activity name with its package.

E.g.

```xml
<activity android:name="com.catherine.resource1.MainActivity" />
<activity android:name="com.catherine.resource2.MainActivity" />
```

5. Load layouts

In your patch apk, you cannot map the view by calling `setContentView(@LayoutRes int layoutResID)`. Your ClassLoader app cannot find your resources via that method. You most use `View.inflate()`. Because applications access resources via the instance of Resource, new loaded resources will not be found in the original Resource object.

E.g.

Do not use this:

```java
setContentView(R.layout.activity_main);
```

Instead, replace it with:

```java
setContentView(View.inflate(getApplicationContext(), R.layout.activity_main, null));
```

6. How many patch apks should I load?

Typically, you don't need to load multiple patch apks. Multiple patch apks might cause resource conflicts.

Assuming you imports the `support-v4` library in both apk1 and apk2, and then you load apk1 first, then you are going to load apk2.  
You will find your ClassLoader app crashes or some resource errors happens. To fix it, you must have apk2 run on another process so that we can perfectly release loaded resources by killing the process before we switch to another patch apk.

```java
@Override
protected void onDestroy() {
    Process.killProcess(Process.myPid());
    super.onDestroy();
}
```

# Reference

-   [Android 动态加载基础 ClassLoader 工作机制]
-   [Understanding and Experimenting with MultiDex]
-   [Android ClassLoader 机制]

# License

```
Copyright 2017 Catherine Chen (https://github.com/Catherine22)

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```

[1]: https://raw.githubusercontent.com/Catherine22/ClassLoader/master/dex_1.png
[2]: https://raw.githubusercontent.com/Catherine22/ClassLoader/master/dex_2.png
[3]: https://raw.githubusercontent.com/Catherine22/ClassLoader/master/instant_run.png
[resource1.apk]: https://github.com/Catherine22/ClassLoader/tree/master/Resource1
[resource2.apk]: https://github.com/Catherine22/ClassLoader/tree/master/Resource2
[android 动态加载基础 classloader 工作机制]: https://segmentfault.com/a/1190000004062880
[understanding and experimenting with multidex]: https://youtu.be/skmOBriQ28E
[android classloader 机制]: http://blog.csdn.net/mr_liabill/article/details/50497055
