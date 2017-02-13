ClassLoader
===================

Loading apks from internal storage by this app (ClassLoader). You can automatically update your app without reinstalling because you do everything in your apk which is dynamically loaded. All you have to do is fix bugs or add new features and package to an apk, then upload it to your host. As you launch this app, it downloads the apk you uploaded and load it. It's a little like 'hotfix'.

So, the most important features of ClassLoader is
 1. Download and varify the apk
 2. Load the apk

And what apk does is
 1. Features you really use like chatting, login, scanning QR Code, etc.
 2. Your user interface, logic, libraries, etc.

In this project, I use this app to load [Resource1.apk] and [Resource2.apk].
# Features

 1. Load codes from another Apk, including classes, jars, etc.
 2. You put your logic to an apk, all you have to do is download  this apk and loading it.
 3. Shrink your app and protect your codes.
 4. Automatic updates
 5. Switch apk, it means that you can load more than an apk. But in general, I think you just need to package all of your logic into an apk and loading an apk is fair enough.



# Illustration

  - This application is used to load classes from another apks, you can launch activities and call methods from another apk, we don't put logic in this app.
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


# Warning

#### 1. Android Studio Settings
 
 Disabled Instant Run when running classLoader application
 ![enter description here][1]
 
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

  [1]: https://raw.githubusercontent.com/Catherine22/ClassLoader/master/screen%20shot.png
  [Resource1.apk]:<https://github.com/Catherine22/Resource1>
  [Resource2.apk]:<https://github.com/Catherine22/Resource2>
