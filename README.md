# Android App Schedule Enumerator


Requirements:

- Android SDK ADT Bundle:
(http://developer.android.com/sdk/index.html)
  - (Optional) Eclipse + ADT plugin
  - Android SDK Tools
  - Android Platform-tools
  - Android platform (android-16 is used in this project)
  - Android system image for the emulator (android-16 is used in this project)
  (Make sure that you added sdk/tools and sdk/platform-tools directories in your system path.)

- Python (v.2.7.5) 
 
- Jython (v.2.5.3)

- AndroidViewClient (v.5.1.1)
(http://github.com/dtmilano/AndroidViewClient)


Usage:

1. Instrument your application apk. file for testing
  
  Note: Current instrumentation only inserts logs into run methods.

  The instrumentation uses [Soot framework](https://github.com/Sable/soot). Soot looks for the android.jar in sdk/platforms/android-9/ folder. Make sure you have this folder in your platforms directory (e.g. you can rename your platform directory to "android-9"). (Todo: Look for options to change this default directory)

  In instrumentAndroidApk folder:

  ```
  javac -cp /FullPathTo/sdk/platforms/android.jar:./lib/coffer.jar:./lib/jasminclasses.jar:./lib/java_cup.jar:./lib/JFlex.jar:./lib/pao.jar:./lib/polygot.jar:./lib/pth.jar:./lib/soot.jar:./lib/sootclasses.jar ./src/*.java

  java -cp /FullPathTo/sdk/platforms/android.jar:./lib/coffer.jar:./lib/jasminclasses.jar:./lib/java_cup.jar:./lib/JFlex.jar:./lib/pao.jar:./lib/polygot.jar:./lib/pth.jar:./lib/soot.jar:./lib/sootclasses.jar:./src AndroidInstrument <fullPathToApkFile> <fullPathToAndroidSDKPlatformsFolder> <fullPathToThisSrcFile> 
  ```

  After execution, sootOutput directory will be created having the instrumented apk file inside.

2. Test the instrumented application
  
  Note: Currently test input invokes UI events in the order of their ids in AndroidViewClient.

  In invokeTestingApk folder:

  ``` 
  javac -cp ./lib/jython-standalone-2.5.3.jar ./src/ApkExecutor.java

  java -cp ./lib/jython-standalone-2.5.3.jar:./src ApkExecutor <fullPathToInstrumentedApkFile> <appPackageName> <appMainActivity>
  ```
  (Package name and the main activity name can be found from the manifest file of the application project.)

   
   