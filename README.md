---
title: ClassLoader
tags: ClassLoader, Dex, Android
grammar_cjkRuby: true
---
Loading apks from external storage by another app (ClassLoader). You can automatically update you app without reinstalling because you do everything in your apk. It's just like 'hotfix'.


----------


# Instruction

This application is used to load classes from another apks, we don't put logic in this app.

----------
# Features

 1. Load codes from another Apk, including classes, jars, etc.
 2. You put your logic to an apk, all you have to do is downloaning this apk and loading it.
 3. Shrink your app and protect your codes.
 4. Automatic updates
 5. Switch apk, it means that you can load more than an apk. But in general, I think you just need to package all of your logic into an apk and loading an apk is fair enough.
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


