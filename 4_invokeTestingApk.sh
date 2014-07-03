 # Script file to test a(n) (instrumented) application.

PATH_TO_SIGNED_APK="${PWD}/example/signed/HelloWorldApp.apk"
#PATH_TO_SIGNED_APK= # full path to your instrumented and signed application file
PATH_TO_SIGNED_TESTER="${PWD}/example/signed/TestApk-release-signed.apk"
#PATH_TO_SIGNED_APK= # full path to your signed tester application file

# Install the app to be tested 
# Comment out the following line if your app is already installed
monkeyrunner ${PWD}/invokeTestingApk/scripts/installScript.py ${PATH_TO_SIGNED_APK}
monkeyrunner ${PWD}/invokeTestingApk/scripts/installScript.py ${PATH_TO_SIGNED_TESTER}

mkdir logcatOutputs
cd logcatOutputs
touch "MyScheduler.log"
cd ..
adb logcat -s "MyScheduler" --line-buffered > "${PWD}/logcatOutputs/MyScheduler.log" &


# Invoke testing
adb shell am instrument -w my.example.test/android.test.InstrumentationTestRunner


#TODO: Kill the child process
exit