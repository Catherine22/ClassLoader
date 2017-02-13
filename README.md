ClassLoader
===================

Loading apks from external storage by another app (ClassLoader). You can automatically update you app without reinstalling because you do everything in your apk. It's just like 'hotfix'.


----------
# Instruction

This application is used to load classes from another apks, you can launch activities and call methods from another apk, we don't put logic in this app.

----------
# Features

 1. Load codes from another Apk, including classes, jars, etc.
 2. You put your logic to an apk, all you have to do is downloaning this apk and loading it.
 3. Shrink your app and protect your codes.
 4. Automatic updates
 5. Switch apk, it means that you can load more than an apk. But in general, I think you just need to package all of your logic into an apk and loading an apk is fair enough.

----------
# Quick start


----------


# Warning
 ## 1. Android Studio Settings
 Disable to Instant Run
 ## 2. Loading resource from apk
 Resource path: Android/data/package/files/resource1.apk
 ## 3. Multidex issue
 In build.gradle, disable multidex.
 
``` gradle
multiDexEnabled false
```
 ## 4. Manifest
  - Add all of the permissions, activities and whatever you've added in your apk's manifest to this app (your classLoader) 's manifest file. And android studio probably figures out some errors likes 'Unresolved package...', just ignore them. **And remember that you most prefix your activity name with it's package**.

E.g.
```xml
<activity android:name="com.catherine.resource1.MainActivity" />
<activity android:name="com.catherine.resource2.MainActivity" />
```
 ## 5. View
 In your apk, you can't just get the view by setContentView(@LayoutRes int layoutResID), it can't find your resources. You most use View.inflate() to find resources.
 
E.g.

Illegal
``` java
setContentView(R.layout.activity_main);
```

legal
``` java
setContentView(View.inflate(getApplicationContext(), R.layout.activity_main, null));
```


