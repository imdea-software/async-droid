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

- You also must have created and started an AVD (Android Virtual Device) before executing step 3 - testing the instrumented application.

Usage:

1. Instrument your application .apk file for testing
  
  Note: Current instrumentation schedules the application threads in their registration order to our scheduler.

  The instrumentation uses [Soot framework](https://github.com/Sable/soot). Soot option -android-jars (that is set in our instrumentor code) looks into the "platforms" folder for the Android SDK that is the target SDK version defined in the application's manifest file. So, make sure that your target platform exists in your platforms folder. (You can use "Android SDK Manager" to install necessary platform SDKs.)

  In instrumentAndroidApk folder:

  ```
  javac -cp /FullPathTo/sdk/platforms/android.jar:./lib/coffer.jar:./lib/jasminclasses.jar:./lib/java_cup.jar:./lib/JFlex.jar:./lib/pao.jar:./lib/polygot.jar:./lib/pth.jar:./lib/soot.jar:./lib/sootclasses.jar ./src/myInstrumentor/*.java

  java -cp /FullPathTo/sdk/platforms/android.jar:./lib/coffer.jar:./lib/jasminclasses.jar:./lib/java_cup.jar:./lib/JFlex.jar:./lib/pao.jar:./lib/polygot.jar:./lib/pth.jar:./lib/soot.jar:./lib/sootclasses.jar:./src myInstrumentor/AndroidInstrument <fullPathToApkFile> <fullPathToAndroidSDKPlatformsFolder> <fullPathToThisSrcFile> -output-format dex

  ```

  After execution, sootOutput directory will be created having the instrumented apk file inside.

2. (Optional) You can view the jimple versions of your application code by providing -output-format jimple in the instrumentation command. (The last two arguments are directly passed to Soot and any output format accepted by soot can be  given as the last argument).

  ```
/FullPathTo/sdk/platforms/android.jar:./lib/coffer.jar:./lib/jasminclasses.jar:./lib/java_cup.jar:./lib/JFlex.jar:./lib/pao.jar:./lib/polygot.jar:./lib/pth.jar:./lib/soot.jar:./lib/sootclasses.jar:./src AndroidInstrument <fullPathToApkFile> <fullPathToAndroidSDKPlatformsFolder> <fullPathToThisSrcFile> -output-format jimple
  ```

  To compare the jimple files before and after instrumentation:
  1. Create jimple files by commenting out the statement that creates MyBodyTransformer in AndroidInstrument.java
  2. Create jimple files by executing the transformation statement 

  (Back up your files after Step 1 since Step 2 overwrites them in the SootOutput folder.)

  If you want to view the instrumented code in Java format, you can translate the classes.dex file in the instrumented .apk into .jar file using [dex2jar] (https://code.google.com/p/dex2jar/) and then any java decompiler. 

3. Test the instrumented application
  
  Note: Currently test input invokes UI events in the order of their ids in AndroidViewClient.

  In invokeTestingApk folder:

  ``` 
  javac -cp ./lib/jython-standalone-2.5.3.jar ./src/ApkExecutor.java

  java -cp ./lib/jython-standalone-2.5.3.jar:./src ApkExecutor <fullPathToInstrumentedApkFile> <appPackageName> <appMainActivity>
  ```
  (Package name and the main activity name can be found from the manifest file of the application project.)

   
Example:

An example directory for the instrumentation of a simple HelloWorld application is provided together with the (only Java) source code of this sample application. HelloWorld creates AsyncTask, Thread, HandlerThread, sends runnables and messages to the message queue of the HandlerThread depending on the user inputs. (Note that in the instrumentation process, we do not need the source of an app but only use the .apk file.)

You can view the sample output files created my instrumenting this application in sootOutput directory. (The jimple files of the support library android.support.v4 in the .apk file is excluded.)

The commands to execute that project are:

1. For instrumentation:
 
  ```
  javac -cp /FullPathTo/sdk/platforms/android.jar:./lib/coffer.jar:./lib/jasminclasses.jar:./lib/java_cup.jar:./lib/JFlex.jar:./lib/pao.jar:./lib/polygot.jar:./lib/pth.jar:./lib/soot.jar:./lib/sootclasses.jar ./src/myInstrumentor/*.java

  java -cp /FullPathTo/sdk/platforms/android.jar:./lib/coffer.jar:./lib/jasminclasses.jar:./lib/java_cup.jar:./lib/JFlex.jar:./lib/pao.jar:./lib/polygot.jar:./lib/pth.jar:./lib/soot.jar:./lib/sootclasses.jar:./src myInstrumentor/AndroidInstrument "/fullPathTo/HelloWorldApp.apk" "/fullPathTo/sdk/platforms" "/fullPathTo/projects/instrumentAndroidApk/src" -output-format dex
  ```

2. For testing:

  ``` 
  javac -cp ./lib/jython-standalone-2.5.3.jar ./src/ApkExecutor.java

  java -cp ./lib/jython-standalone-2.5.3.jar:./src ApkExecutor "/fullPathTo/HelloWorldApp.apk" "my.example.HelloWorld" "my.example.HelloWorld.MainActivity"
  ```

   