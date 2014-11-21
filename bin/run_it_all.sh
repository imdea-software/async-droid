#!/bin/bash

ROOT=${PWD}
BIN=${ROOT}/bin
BUILD=${ROOT}/build

set -e

if [ -z "${ANDROID_HOME}" ]
then
  echo "Please set your \$ANDROID_HOME variable."
  exit 1
fi

if [ ! -f "$1" ]
then
  echo "Please supply an APK file."
  exit 1
fi

APK_FILE=$(cd $(dirname "$1") && pwd)/$(basename "$1")
APP_NAME=$(basename "$1" .apk)

if [ ! -f ${BUILD}/${APP_NAME}.apk ]
then
  echo "Project must be built first; run 'ant -Dapk=${APK_FILE}'."
  exit 1
fi

# Install the app to be tested 
# Comment out the following line if your app is already installed
monkeyrunner ${BIN}/installScript.py "${BUILD}/${APP_NAME}.apk"
monkeyrunner ${BIN}/installScript.py "${BUILD}/TestApk.apk"

mkdir -p logcatOutputs
adb logcat ActivityManager:I *:S --line-buffered > "$PWD/logcatOutputs/AllActivities.log" &
adb logcat -s "MyScheduler" --line-buffered > "$PWD/logcatOutputs/MyScheduler.log" &
adb logcat -s *:E --line-buffered > "$PWD/logcatOutputs/AllErrors.log" &

# Invoke testing
adb shell am instrument -w my.apktester.test/android.test.InstrumentationTestRunner

# Kill the child processes
trap "kill 0" SIGINT SIGTERM EXIT

exit