# Android App Schedule Enumerator

A tool for concurrency testing of Android apps.

## Requirements

- Android SDK: http://developer.android.com/sdk/index.html
    - Android SDK Tools
    - Android SDK Platform-tools
    - Android SDK Platform (we use Android 4.4.2 / API 19)
    - Android system image for the emulator
    - (Optional) Eclipse + ADT plugin

- (Optional) Alternative Android Virtual Devices:
    - Genymotion: http://www.genymotion.com

- Android Apktool (1.5.2)

- Java (SE 7)

- Apache Ant (1.9.4)

- Python (2.7.5)

- Jython (2.5.3)

## Usage

Ensure the `ANDROID_HOME` environment variable points to a valid Android
platform SDK containing an Android platform library, e.g.,
`$ANDROID_HOME/platforms/android-19/android.jar`. Also, be sure you have
created and started an Android Virtual Device (AVD).

Instrument your application `.apk` file for testing. For instance, try the
example app in `example/HelloWorldApk.apk`:

    ant -Dapk=example/HelloWorldApk.apk

(Note that the original `.apk` file will remain unmodified.)
Then, test the instrumented application:

    ./bin/run_it_all.sh example/HelloWorldApk.apk

And see the results in `logcatOutputs`.
