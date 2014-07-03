# Android App Schedule Enumerator


**Requirements:**

- Android SDK ADT Bundle:
(http://developer.android.com/sdk/index.html)
  - (Optional) Eclipse + ADT plugin
  - Android SDK Tools
  - Android Platform-tools
  - Android platform (SDK 19 to run the example)
  - Android system image for the emulator

  (Make sure that you added sdk/tools and sdk/platform-tools directories in your system path.)

- ApkTool (v 1.5.2) from https://code.google.com/p/android-apktool/

- Ant tool

- Python (v.2.7.5) 
 
- Jython (v.2.5.3)


- You also must have created and started an AVD (Android Virtual Device) before executing step 3 - testing the instrumented application.

  The instrumentation uses [Soot framework](https://github.com/Sable/soot). Soot option -android-jars (that is set in our instrumentor code) looks into the "platforms" folder for the Android SDK that is the target SDK version defined in the application's manifest file. So, make sure that your target platform exists in your platforms folder. To instrument the example HelloWorldApp in example folder, SDK level 19 is required. (You can use "Android SDK Manager" to install necessary platform SDKs.)

**Usage:**

1. Instrument your application .apk file for testing
  
  Note: Current instrumentation schedules the application threads in their registration order to our scheduler.

  Fill in the necessary parameters in instrumentApk.sh and then run the script:

  ```
  sudo ./1_instrumentAndroidApk.sh
  ```

  After the execution of the script, sootOutput directory will be created having the instrumented apk file inside.


2. Build your tester application.

  To build a tester for the example HelloWorldApp:

   ```
  sudo ./2_buildTesterApk.sh
  ```

  If you will test another application, you will need to do the following steps before running the script:
  
  1. Write your application package name as the target application in buildTestterApk/TestApk/AndroidManifest.xml
  2. Enter the main activity name of your application in the buildTestterApk/TestApk/src/my/example/test/TestApk.java file.
  
  After the execution of the script, TestAndroidApp-release-unsigned.apk will be created inside the bin folder of the tester application project.


3. Sign (i) the instrumented .apk file (obtained in step 1) and (ii) the tester .apk (obtained in step 2) file.

  NOTE: These two .apk files should be signed with the same key.

  1. Generate a private/public key pair using keytool (included in the Java SDK) 
  You need to generate it once, then you can use the same key to sign your applications. 

    ```
    keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
    ```

  2. Sign the .apk files with the private key generated with keytool (included in the Java JDK)

    ```
    jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore 2 my-release-key.keystore my_application.apk alias_name
    jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore 2 my-release-key.keystore my_tester.apk alias_name
    ```


4. Test the instrumented application

  Make sure that you have an AVD running. You should see your device listed when you type: "adb devices" in the command line.
	
  Fill in the application name parameter in the invokeTesting.sh  and run the script:

  ``` 
  sudo ./4_invokeTestingApk.sh
  ```


**Example:**

In the *example* directory, we provide a simple HelloWorld application that creates AsyncTasks, Threads, a HandlerThread, sends runnables and messages to the message queue of the HandlerThread depending on the user inputs. We also provide its instrumented and signed version together with its tester application.

You can instrument and test this example application by running the scripts with the default parameter values.

To see the effect of the instrumentation, you can monitor the application threads using DDMS (Dalvik Debug Monitor Server) and examine LogCat outputs of the instrumented application.
