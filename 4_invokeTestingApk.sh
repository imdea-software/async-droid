#!/bin/bash

 # Script file to test a(n) (instrumented) application.

set -e

if [ ! -f "$1" ]
then
  echo "Please supply an APK file."
  exit 1
fi

APK_FILE=$(cd $(dirname "$1") && pwd)/$(basename "$1")
APK_NAME=$(basename "$1")

PATH_TO_SIGNED_APK=$(dirname $APK_FILE)/signed/$(basename $APK_FILE)
PATH_TO_SIGNED_TESTER=$(dirname $APK_FILE)/signed/TestApk-release-signed.apk

# Install the app to be tested 
# Comment out the following line if your app is already installed
monkeyrunner $PWD/invokeTestingApk/scripts/installScript.py $PATH_TO_SIGNED_APK
monkeyrunner $PWD/invokeTestingApk/scripts/installScript.py $PATH_TO_SIGNED_TESTER

mkdir logcatOutputs
adb logcat ActivityManager:I *:S --line-buffered > "$PWD/logcatOutputs/AllActivities.log" &
adb logcat -s "MyScheduler" --line-buffered > "$PWD/logcatOutputs/MyScheduler.log" &
adb logcat -s *:E --line-buffered > "$PWD/logcatOutputs/AllErrors.log" &


# Invoke testing
adb shell am instrument -w my.apktester.test/android.test.InstrumentationTestRunner


# Kill the child processes
trap "kill 0" SIGINT SIGTERM EXIT

exit