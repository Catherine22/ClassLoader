ClassLoader
===================

This repository hosts an example of dynamically loading an apk and in-depth documentation.

It's a very powerful technique which loads apks from internal storage with ClassLoader. Users can automatically update their application without reinstalling. Once developers fix any bug or update new features, they are supposed to repackage codes to an apk (I call it main apk) and upload to their server. The workflow would be:
1. Check the latest version while launching the application with resonable valifacation
2. Download the latest apk (main apk).
3. Load codes (That is what this demonstration does)

## 4 steps to master this project
 1. Publisher your class loader apk.
 2. Upload the main apk to your server, only users who have installed ClassLoader app can download it.
 3. Users install ClassLoader apk.
 4. Users open ClassLoader app to download the main apk which includes features of what your app really do and load it to ClassLoader app.

Assume that you fixed bugs and you are going to update your app, all you have to do is step2. Codes will be update while users launch ClassLoader app (step 4). It's kind of like 'hotfix'.

Therefore, you most create two projects, ClassLoader and main apk which combines features and layouts.

### The main job of ClassLoader app is
 - Download and validate the main apk
 - Load the main apk

### And what the main apk does is
 -  Features like chatting, taking photos, scanning QR codes, login or anything.
 -  Layouts, classes, libraries... any businesses your app provides.

In this project, I use this ClassLoader app to load [Resource1.apk] and [Resource2.apk].

# Features

 1. Load classes or resources from other apks or jars.
 2. Package all the functions you create to an apk, all you have to do is download this apk and loading it.
 3. Shrink your app and hide your codes.
 4. Automatic updates
 5. Switch apk, it means that you can load more than an apk. But in general, I think you just need to package all of your codes into an apk and it's probably quite enough to load classes or resources in a single apk.

# Illustration

## Java class loader

There are three classLoaders would be used when JVM started:

 1. Bootstrap class loader
Loading classes from <JAVA_HOME>/jre/lib directory
 2. Extension class loader
Loading classes from <JAVA_HOME>/jre/lib/ext directory
 3. System class loader
Loading classes from system class path (which is the same as the CLASSPATH environment variable).

Besides, you could define your own class loaders, which is 'User-defined class loaders'.

## Android class loader

Android virtual machine loads classes just like the way Java does, but they're slightly different.

### What is dex
In an Android device, it packages your classes into one (or more) dex file(s) which is (are) located in an apk, and optimizes those dex files loading with Dalvik.

![enter description here][1]

(screenshot from https://youtu.be/skmOBriQ28E)

![enter description here][2]

(screenshot from https://youtu.be/skmOBriQ28E)

### Class loader

Here are class loaders based on Android:

|Class Loader|Summary|
|----|----|
|BootClassLoader|The top parent of the following classLoaders.|
|PathClassLoader|Load classes located in data/app/... where your app installed.<br>Android uses this class for its system class loader and its application class loader(s).|
|DexClassLoader|Load classes from .jar and .apk files containing a classes.dex entry. This can be used to execute code not installed as part of an application.|
|URLClassLoader|@hide<br>This class loader is used to load classes and resources from a search path of URLs referring to both JAR files and directories. |


First, let's focus on PathClassLoader and DexClassLoader. Both of them extend BaseDexClassLoader.

In PathClassLoader.class
```java
// set optimizedDirectory as null means to use the default system directory for same
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

We found that optimizedDirectory is the only one difference between them.
optimizedDirectory is a directory where optimizes dex files would be written. While it's null in PathClassLoader, it associates original optimized dex file. And DexClassLoader could cache any optimize dex files you put in internal storage.

That's why we can assign DexClassLoader to load the user-defined apk, dex and jar files. PathClassLoader, however, loads the installed Apk.


Then let's see what ClassLoader does
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

                    // this is the defining class loader; record the stats
                }
            }
            return c;
    }
}
```

We realized that there're three steps to load a class.
First, check if the class has already been loaded,
```java
findLoadedClass(name);
```

Next, if the class is not found, we check if the class has already been loaded with the parent.
```java
if (parent != null) {
    c = parent.loadClass(name, false);
} else {
    c = findBootstrapClassOrNull(name);
}
```
Still not found, so we start to load the class ourselves.
```java
findClass(name);
```

By now, you can easily figure out that once a class has been loaded, it'll never be load again.


## Scenarios

Let's play with some scenarios of class loaders.
```java
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Load core java libraries with " + String.class.getClassLoader());
        Log.i(TAG, "Load user-defined classes with " + MainActivity.class.getClassLoader());
        Log.i(TAG, "Load user-defined libraries with " + AppCompatActivity.class.getClassLoader());//what you imported from gradle or libs/
        Log.i(TAG, "Default classLoader is " + getClassLoader());
        Log.i(TAG, "Default system classLoader is \"" + ClassLoader.getSystemClassLoader());
}
```

We know

|Class Loader|Methods|
|----|----|
|BootClassLoader|  String.class.getClassLoader() |
|PathClassLoader[[DexPathList[[directory...|  MainActivity.class.getClassLoader()<br>AppCompatActivity.class.getClassLoader()<br>ClassLoader.getSystemClassLoader() |
|PathClassLoader[DexPathList[[zip file...|getClassLoader()|

  - Both user-defined classes and libraries are loaded with PathClassLoader.
  - Core java libraries like java.lang.String are loaded with BootClassLoader. So you can't create a String class and replace java.lang.String, even though they've got the same package name and class name. **Android believes that they are totally different classes because they are from different class loaders.**


## About this project

  - This application is used to load classes from another apk, you can launch activities or call methods from that apk. That's why there is nothing but updating and loading apks in this app.
  - Use getClassLoader().loadClass() to get activities from an apk, and call methods or fields with Java reflection.

Here're some reflection examples:


Let's say Utils is what class you are going to use, your class looks like...
``` java
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

You want to call some methods of Utils. Unfortunately you can't find that Utils in your project (ClassLoader app), so you can't just import it like any other classes in your project.
So here we use reflection to resolve this issue.

 - First, find the class

``` java
private Class<?>  apkUtils;

try {
	apkUtils = getClassLoader().loadClass("com.catherine.resource1.Utils");
} catch (ClassNotFoundException e) {
	e.printStackTrace();
}
```

 - Call methods
``` java
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

 - Or set fields

``` java
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

 - Or maybe you don't want to use any methods or fields, you just launch the activity

``` java
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

#### 1. Android Studio Settings

 Disabled Instant Run when running classLoader application
 ![enter description here][3]

#### 2. Apks path

 Resources path: Android/data/package/files/xxx.apk   

#### 3. Multidex

 In build.gradle, disable multidex.

``` gradle
multiDexEnabled false
```

#### 4. Manifest

**Add all of the permission, activities and whatever you've added in your main apk's manifest to ClassLoader app's manifest file**. And android studio probably figures out some errors likes 'Unresolved package...', just ignore them. And don't forget to add the prefix of your activity name with its package.

E.g.
```xml
<activity android:name="com.catherine.resource1.MainActivity" />
<activity android:name="com.catherine.resource2.MainActivity" />
```

#### 5. View

In your main apk, you can't just get the view with setContentView(@LayoutRes int layoutResID). Your ClassLoader app can't find your resources with that method. You most use View.inflate() to find resources. Because applications access resources via the instance of Resource, new loaded resources are not found in the original Resource object.

E.g.

Do not use this:
``` java
setContentView(R.layout.activity_main);
```

Instead, replace it with:
``` java
setContentView(View.inflate(getApplicationContext(), R.layout.activity_main, null));
```

#### 6. Loading more than one apk
There's a scenario.
Assuming you imported the support-v4 library both apk1 and apk2, and you load apk1 first, then you are going to load apk2.
All of a sudden, your ClassLoader app crashes or some resource errors happens. Here's a solution, we make apk2 run on another process so that we can perfectly release loaded resources, and we also make sure that there're only resources of a single apk while the application is running.


``` java
@Override
protected void onDestroy() {
    Process.killProcess(Process.myPid());
    super.onDestroy();
}
```


# Reference
 - [Android动态加载基础 ClassLoader工作机制]
 - [Understanding and Experimenting with MultiDex]
 - [Android ClassLoader机制]


  [Resource1.apk]:<https://github.com/Catherine22/Resource1>
  [Resource2.apk]:<https://github.com/Catherine22/Resource2>

  [Android动态加载基础 ClassLoader工作机制]:<https://segmentfault.com/a/1190000004062880>  
  [Understanding and Experimenting with MultiDex]:<https://youtu.be/skmOBriQ28E>
  [Android ClassLoader机制]:<http://blog.csdn.net/mr_liabill/article/details/50497055>


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
