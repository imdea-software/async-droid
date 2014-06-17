Bounded Android Testing Tool

TODO: Describe our testing tool and its exploration mechanism when it is clear.


Requirements:

- Android SDK ADT Bundle:
(http://developer.android.com/sdk/index.html)
  - (Optional) Eclipse + ADT plugin
  - Android SDK Tools
  - Android Platform-tools
  - Android platform (android-16 is used in this project)
  - Android system image for the emulator (android-16 is used in this project)

- Python (v.2.7.5) 
 
- Jython (v.2.5.3)

- AndroidViewClient (v.5.1.1)
(http://github.com/dtmilano/AndroidViewClient)



Usage:

1. Instrument you application apk. file for testing:
   // Current instrumentation only inserts logs into run methods.

In instrumentAndroidApk folder:

javac -cp ./lib/android.jar:./lib/coffer.jar:./lib/jasminclasses.jar:./lib/java_cup.jar:./lib/JFlex.jar:./lib/pao.jar:./lib/polygot.jar:./lib/pth.jar:./lib/soot.jar:./lib/sootclasses.jar ./src/*.java

java -cp ./lib/android.jar:./lib/coffer.jar:./lib/jasminclasses.jar:./lib/java_cup.jar:./lib/JFlex.jar:./lib/pao.jar:./lib/polygot.jar:./lib/pth.jar:./lib/soot.jar:./lib/sootclasses.jar:./src AndroidInstrument <fullPathToApkFile> <fullPathToAndroidSDKPlatformsFolder> <fullPathToThisSrcFile> 

After execution, sootOutput directory will be created having the instrumented apk file inside.

2. Test the instrumented application:
	// Currently test input invokes UI events in the order of their ids in AndroidViewClient.

In invokeTestingApk folder:

javac -cp ./lib/jython-standalone-2.5.3.jar ./src/ApkExecutor.java

java -cp ./lib/jython-standalone-2.5.3.jar:./src ApkExecutor <fullPathTo/sdk/platform-tools/adb> <fullPathToInstrumentedApkFile> <appPackageName> <appMainActivity>

(Package name and the main activity name can be found from the manifest file of the application project.)

   
   