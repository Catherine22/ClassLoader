ClassLoader
===================

This repository hosts examples of loading apks dynamical and in-depth documentation.

It's a very powerful technique to load apks from internal storage by ClassLoader. You can automatically update your app without reinstalling because you do everything in your apk which is dynamically loaded.

## 4 steps to master this project
 1. Publisher your class loader apk.
 2. Upload the main apk to your server make sure that every one has installed class loader app could download it.
 3. Users install class loader apk.
 4. Users open classLoader app to download the main apk which includes logic of what your app really do, and load it.
 
Assume that you fixed bugs and you are going to update your app, all you have to do is step2. Then, as users launch class loader app, it's going to download the latest apk you just uploaded and load it to class loader app. It's a little like 'hotfix'.

Therefore, you most create two projects, one loads classes and one includes logic.

### The main job of ClassLoader app is
 - Download and varify the apk
 - Load the apk

### And what the main apk does are
 1. Features you really use like chatting, taking photos, scanning QR codes, etc.
 2. Your user interface, logic, libraries... any bussiness your app providers.

In this project, I use this app to load [Resource1.apk] and [Resource2.apk].
# Features

 1. Load codes from another Apk, including classes, jars, etc.
 2. You put your logic to an apk, all you have to do is download  this apk and loading it.
 3. Shrink your app and hide your codes.
 4. Automatic updates
 5. Switch apk, it means that you can load more than an apk. But in general, I think you just need to package all of your logic into an apk and loading an apk is fair enough.

# Illustration

## Java class loader

There are three class loader are used then JVM started:

 1. Bootstrap class loader
Loading classes in <JAVA_HOME>/jre/lib directory
 2. Extension class loader
Loading classes in <JAVA_HOME>/jre/lib/ext directory
 3. System class loader
Loading classes in system class path (which is the same as the CLASSPATH environment variable).

Also, you could create your own class loaders, which is called 'User-defined class loaders'.

## Android class loader

Android virtual machine load classes just like the way Java does, but there's a slightly different.

### What is dex
In an Android device, it packages your classes into one (or more) dex file(s) which is (are) located in an apk, and optimizes those dex files loading by Dalvik.

![enter description here][1]

(screenshot from https://youtu.be/skmOBriQ28E)

![enter description here][2]

(screenshot from https://youtu.be/skmOBriQ28E)

### Class loader

Here are class loaders based on Android:

|ClassLoader|Summary|
|----|----|
|BootClassLoader|The top parent of the following classLoaders.|
|PathClassLoader|Load classes located in data/app/... where your app installed.<br>Android uses this class for its system class loader and for its application class loader(s).|
|DexClassLoader|Load classes from .jar and .apk files containing a classes.dex entry. This can be used to execute code not installed as part of an application.|
|URLClassLoader|@hide<br>This class loader is used to load classes and resources from a search path of URLs referring to both JAR files and directories. |


First, let's focus on PathClassLoader and DexClassLoader.

In PathClassLoader.class
```java
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

We found that the only one different between them is optimizedDirectory.
optimizedDirectory is a directory where optimized dex files should be written, so while it's null in PathClassLoader, it associates original optimized dex file. And DexClassLoader could cache any optimize dex files you put on internal storage.

That's why we can declare what apk, dex and jar files would be loaded by DexClassLoader.


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
First, check if the class has already been loaded, then
```java
findLoadedClass(name);
```

Next, if the class was not found, we check if the class has already been loaded by the parent class loader.
```java
if (parent != null) {
    c = parent.loadClass(name, false);
} else {
    c = findBootstrapClassOrNull(name);
}
```
Still not found, so we start to load the class by ourselves.
```java
findClass(name);
```

And now, you can easily figure out that once a class has been loaded, it'll never be load again. 
An

## About this project

  - This application is used to load classes from another apk, you can launch activities or call methods from another apk, so we don't put logic in this app.
  - Using getClassLoader().loadClass() to get activities from an apk, and calling methods or fields by Java reflection.

Here're some reflection examples:


Let's say Utils is what class you want to reflect, your class looks like...
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

And now you want to call methods of Utils. But you can't find Utils in your project, so you can't just import it like any other classes in your project.
So here we use reflection to resolve the problem.

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

# Warning

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
 
**Add all of the permissions, activities and whatever you've added in your apk's manifest to this app (your classLoader) 's manifest file**. And android studio probably figures out some errors likes 'Unresolved package...', just ignore them. And remember that you most prefix your activity name with its package.

E.g.
```xml
<activity android:name="com.catherine.resource1.MainActivity" />
<activity android:name="com.catherine.resource2.MainActivity" />
```

#### 5. View
 
In your apk, you can't just get the view by setContentView(@LayoutRes int layoutResID), it can't find your resources. You most use View.inflate() to find resources.
 
E.g.

Illegal
``` java
setContentView(R.layout.activity_main);
```

Legal
``` java
setContentView(View.inflate(getApplicationContext(), R.layout.activity_main, null));
```

# Issues
**It'll be fine if you just load an apk.
But if you try to load multi-apks, there still are some problems I haven't fixed.**

If there're some libraries like support-v4, zxing, whatever, you imported these libraries to some of the apks (in this case, it means  both resource1.apk and resource2.apk). And when you call methods or launch activities that are included the same libraries, it'll crash because resources're not found.

And then there's a workaround here that you must not use the same libraries in any apks you wanna load or you just load a single apk.

# Reference
 - [Android动态加载基础 ClassLoader工作机制]
 - [Understanding and Experimenting with MultiDex]
 

  [Resource1.apk]:<https://github.com/Catherine22/Resource1>
  [Resource2.apk]:<https://github.com/Catherine22/Resource2>
  
  [Android动态加载基础 ClassLoader工作机制]:<https://segmentfault.com/a/1190000004062880>  
  [Understanding and Experimenting with MultiDex]:<https://youtu.be/skmOBriQ28E>
  
  


  [1]: https://raw.githubusercontent.com/Catherine22/ClassLoader/master/dex_1.png
  [2]: https://raw.githubusercontent.com/Catherine22/ClassLoader/master/dex_2.png
  [3]: https://raw.githubusercontent.com/Catherine22/ClassLoader/master/instant_run.png
