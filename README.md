# Android App Schedule Enumerator


Requirements:

- Android SDK ADT Bundle:
(http://developer.android.com/sdk/index.html)
  - (Optional) Eclipse + ADT plugin
  - Android SDK Tools
  - Android Platform-tools
  - Android platform
  - Android system image for the emulator
  (Make sure that you added sdk/tools and sdk/platform-tools directories in your system path.)

- Python (v.2.7.5) 
 
- Jython (v.2.5.3)

- AndroidViewClient (v.5.1.1)
(http://github.com/dtmilano/AndroidViewClient)


Usage:

1. Instrument your application apk. file for testing
  
  Note: Current instrumentation only inserts logs into run methods.

  The instrumentation uses [Soot framework](https://github.com/Sable/soot). 
  
  Soot parameter -android-jars option (that is called in our instrumenter file) looks in the path to the "platforms" folder of the Android SDK that is the target SDK version defined in your app's manifest file. So, make sure that the target platform exists in your platforms folder. 

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

   
   