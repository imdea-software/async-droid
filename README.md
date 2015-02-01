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

- Java (SE 7)

- Apache Ant (1.9.4)

- Python (2.7.5)


## Usage

Ensure the `ANDROID_HOME` environment variable points to a valid Android
platform SDK containing an Android platform library, e.g.,
`$ANDROID_HOME/platforms/android-19/android.jar`. Also, be sure you have
created and started an Android Virtual Device (AVD).

Instrument your application `.apk` file for testing. For instance, try the
example app in `example/HelloWorldApk.apk`:

    ant -Dapk=example/HelloWorldApp.apk

(Note that the original `.apk` file will remain unmodified.)

Then, test the instrumented application by:

Recording a set of user events (this overwrites the previously recorded events): 

	python bin/aase.py --record build/HelloWorldApp.apk
	
Then replaying them for schedules with/without a delay bound.

    python bin/aase.py --replay --delays 1 build/HelloWorldApp.apk

<!-- And see the results in `logcatOutputs`. -->

## Running the sample application

To demonstrate the detection of <a href="https://github.com/ojacquemart/vlilleChecker/issues/60"> a sample concurrency bug</a>, we provide a previous version of <a href="https://github.com/ojacquemart/vlilleChecker"> vlilleChecker</a>  Android App (built from commit 86b22a7).

Instrument the application `.apk` file for testing:

    ant -Dapk=example/sample_vlillechecker.apk -Dandroid.api.version=19

	
You can replay the user events that trigger the bug (with particular thread interleavings) by running:

    python bin/aase.py --replay --events events.trc build/sample_vlillechecker.apk
    
    
(Optional) You can also record your own a set of user events with the command below and then replay them: 

	python bin/aase.py --record build/sample_vlillechecker.apk     

