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

- Python (v.2.7.5) 
 
- Jython (v.2.5.3)

- AndroidViewClient (v.5.1.1) from http://github.com/dtmilano/AndroidViewClient

- You also must have created and started an AVD (Android Virtual Device) before executing step 3 - testing the instrumented application.

**Usage:**

1. Instrument your application .apk file for testing
  
  Note: Current instrumentation schedules the application threads in their registration order to our scheduler.

  The instrumentation uses [Soot framework](https://github.com/Sable/soot). Soot option -android-jars (that is set in our instrumentor code) looks into the "platforms" folder for the Android SDK that is the target SDK version defined in the application's manifest file. So, make sure that your target platform exists in your platforms folder. To instrument the example HelloWorldApp in example folder, SDK level 19 is required. (You can use "Android SDK Manager" to install necessary platform SDKs.)

  Fill in the necessary parameters in instrumentApk.sh and then run the script:

  ```
  sudo ./instrumentApk.sh
  ```

  After execution, sootOutput directory will be created having the instrumented apk file inside.


2. Sign the instrumented .apk file.

  1. Generate a private/public key pair using keytool (included in the Java SDK)

  ```
keytool -genkey -v -keystore my -release -key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
  ```

  2. Sign the instrumented .apk with the private key generated with keytool (included in the Java JDK)

  ```
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore 2 my -release -key. keystore my_application.apk alias_name
  ```

3. Test the instrumented application
  
  Note: Currently test input invokes UI events in the order of their ids in AndroidViewClient.
	
  Fill in the necessary parameters in the invokeTestingApk.sh  and run the script:

  ``` 
  sudo ./invokeTestingApk.sh
  ```

**Example:**

In the *example* directory, we provide a simple HelloWorld application (together with its signed version) that creates AsyncTasks, Threads, a HandlerThread, sends runnables and messages to the message queue of the HandlerThread depending on the user inputs. 

You can run instrument and test this example application by uncommenting the lines beginning with "##" in the script files.

To see the effect of the instrumentation, you can monitor the application threads using DDMS (Dalvik Debug Monitor Server) and examine LogCat outputs of the instrumented application.
