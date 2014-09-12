# Android App Schedule Enumerator


## Requirements

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

## Usage

Instrument your application `.apk` file for testing. For instance, try the
example app in `example/HelloWorldApk.apk`:

    ant -Dapk=example/HelloWorldApk.apk

(Note that the original `.apk` file will remain unmodified.)
Then, test the instrumented application:

    ./bin/run_it_all.sh example/HelloWorldApk.apk

And see the results in `logcatOutputs`.