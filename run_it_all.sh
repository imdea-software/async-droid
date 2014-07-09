#!/bin/bash

# Script file to instrument an application.

set -e

if [ -z "$ANDROID_HOME" ]
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
APK_NAME=$(basename "$1")
SIGNED_APK=$(dirname $APK_FILE)/signed/$(basename $APK_FILE)
SIGNED_TESTER=$(dirname $APK_FILE)/signed/TestApk-release-signed.apk

REINSTRUMENT=
REBUILD_TESTER=
RESIGN=

if [ ! -f "sootOutput/$APK_NAME" ] || [ -n "$REINSTRUMENT" ]
then

  echo "-------------------------------------------------------------------------"
  echo "                      PHASE 1 : INSTRUMENT THE APK                       "
  echo "-------------------------------------------------------------------------"

  # Extract the .apk file to get classes.dex
  echo "Extracting $APK_FILE using apktool."
  mkdir -p mergerOutput
  cd mergerOutput
  mkdir -p extractedApk
  cp -f "$APK_FILE" "$APK_NAME.zip"
  unzip -u -d extractedApk "$APK_NAME.zip"
  cd ..

  # Decode the manifest file using apktool (necessary to get original manifest and
  # resource files)
  echo "Decoding $APK_FILE using apktool."
  apktool d -f "$APK_FILE" "${PWD}/mergerOutput/decodedApk"

  echo "Merging application dex file with myScheduler dex file to be used in the instrumentation."
  # Merge the classes.dex file with myScheduler.dex file
  MERGER_CLASSPATH=$CLASSPATH
  MERGER_CLASSPATH+=:$ANDROID_JAR
  MERGER_CLASSPATH+=:$PWD/lib/libForMergingDex/dx.jar
  MERGER_CLASSPATH+=:$PWD/instrumentAndroidApk/mergeDexes/src

  javac -cp ${MERGER_CLASSPATH} \
    instrumentAndroidApk/mergeDexes/src/mergeDexFiles/*.java 
  # Merge the classes.dex file with myScheduler.dex file 
  cp -rf "${PWD}/instrumentAndroidApk/instrumentApk/src/myScheduler/MyScheduler.dex" \
    "${PWD}/instrumentAndroidApk/mergeDexes/src/dex/MyScheduler.dex"
  cp -rf "${PWD}/mergerOutput/extractedApk/classes.dex" \
    "${PWD}/instrumentAndroidApk/mergeDexes/src/dex/classes.dex"
  java -cp ${MERGER_CLASSPATH} mergeDexFiles/MergeDexFiles "../dex/classes.dex" \
    "../dex/MyScheduler.dex"

  # Recreate .apk file using aapt tool: (if you have more than one res file, add
  # all into the command)
  echo Recreating .apk file in $PWD/myMerger/mergerOutput/extractedApk/${APP_NAME}.
  cd mergerOutput
  MIN_SDK_LEVEL=$(grep minSdkVersion decodedApk/apktool.yml | grep -o "[0-9]\+")
  TARGET_SDK_LEVEL=$(grep targetSdkVersion decodedApk/apktool.yml | grep -o "[0-9]\+")
  for V in $(seq $TARGET_SDK_LEVEL $MIN_SDK_LEVEL)
  do
    ANDROID_JAR=$ANDROID_HOME/platforms/android-$V/android.jar
    [ -f $ANDROID_JAR ] && break
  done
  if [ ! -f $ANDROID_JAR ]
  then
    echo "Could not locate android jar file."
    exit -1
  fi
  aapt package -f -M "decodedApk/AndroidManifest.xml" -S "decodedApk/res/" \
    -I "$ANDROID_JAR" --min-sdk-version $MIN_SDK_LEVEL \
    --target-sdk-version $TARGET_SDK_LEVEL -F "$APK_NAME"

  # add the new classes.dex into the .apk file
  aapt add -f $APK_NAME classes.dex
  cd ..

  echo Instrumenting .apk file $APK_NAME.
  # Compile instrumentor
  INSTRUMENTOR_LIB=$PWD/lib/libForInstrumentor
  INSTRUMENTOR_CLASSPATH=$CLASSPATH
  INSTRUMENTOR_CLASSPATH+=:$ANDROID_JAR
  INSTRUMENTOR_CLASSPATH+=:$INSTRUMENTOR_LIB/lib/coffer.jar
  INSTRUMENTOR_CLASSPATH+=:$INSTRUMENTOR_LIB/jasminclasses.jar
  INSTRUMENTOR_CLASSPATH+=:$INSTRUMENTOR_LIB/java_cup.jar
  INSTRUMENTOR_CLASSPATH+=:$INSTRUMENTOR_LIB/JFlex.jar
  INSTRUMENTOR_CLASSPATH+=:$INSTRUMENTOR_LIB/pao.jar
  INSTRUMENTOR_CLASSPATH+=:$INSTRUMENTOR_LIB/polygot.jar
  INSTRUMENTOR_CLASSPATH+=:$INSTRUMENTOR_LIB/pth.jar
  INSTRUMENTOR_CLASSPATH+=:$INSTRUMENTOR_LIB/soot.jar
  INSTRUMENTOR_CLASSPATH+=:$INSTRUMENTOR_LIB/sootclasses.jar
  INSTRUMENTOR_CLASSPATH+=:$PWD/instrumentAndroidApk/instrumentApk/src/myInstrumentor

  javac -cp $INSTRUMENTOR_CLASSPATH \
    instrumentAndroidApk/instrumentApk/src/myInstrumentor/*.java

  # Run the instrumentor
  rm -f "sootOutput/$APK_NAME"
  java -cp $INSTRUMENTOR_CLASSPATH AndroidInstrument \
    $PWD/mergerOutput/$APK_NAME $ANDROID_HOME/platforms $PWD/src \
    -output-format dex

  echo "Instrumentation of $APK_NAME is complete."
  echo "Find the instrumented .apk file in $PWD/sootOutput"
fi

if [ ! -f "buildTesterApk/TestApk/bin/TestApk-release-unsigned.apk" ] || [ -n "$REBUILD_TESTER" ]
then

  echo "-------------------------------------------------------------------------"
  echo "                      PHASE 2 : BUILD THE TESTER APP                     "
  echo "-------------------------------------------------------------------------"

  # Build the tester application:
  cd buildTesterApk/TestApk
  android update project -p .
  ant release
  cd ..
  cd ..

  echo 'TestAndroidApp-release-unsigned.apk is generated in the bin directory.'
  echo 'Sign that file before installation.'
fi

if [ ! -f "$SIGNED_APK" ] || [ ! -f "$SIGNED_TESTER" ] || [ -n "$RESIGN" ]
then

  echo "-------------------------------------------------------------------------"
  echo "                      PHASE 3 : SIGN THE APK FILES                       "
  echo "-------------------------------------------------------------------------"

  KEYSTORE=my.keystore
  STOREPASS=abcdefg
  KEYPASS=abcdefg
  ALIAS=my_alias

  mkdir -p $(dirname $APK_FILE)/signed
  cp -f "sootOutput/$APK_NAME" $SIGNED_APK
  cp -f "buildTesterApk/TestApk/bin/TestApk-release-unsigned.apk" $SIGNED_TESTER

  if [ ! -f $KEYSTORE ]
  then
    keytool -genkey -keyalg RSA -keysize 2048 -validity 10000 \
      -noprompt -dname "CN=a, OU=b, O=c, L=d, S=e, C=f" \
      -alias $ALIAS -keystore $KEYSTORE \
      -storepass $STOREPASS -keypass $KEYPASS
  fi

  jarsigner -sigalg SHA1withRSA -digestalg SHA1 \
    -keystore $KEYSTORE -storepass $STOREPASS -keypass $KEYPASS \
    $SIGNED_APK $ALIAS

  jarsigner -sigalg SHA1withRSA -digestalg SHA1 \
    -keystore $KEYSTORE -storepass $STOREPASS -keypass $KEYPASS \
    $SIGNED_TESTER $ALIAS
fi

echo "-------------------------------------------------------------------------"
echo "                      PHASE 4 : INVOKE TESTING APK                       "
echo "-------------------------------------------------------------------------"

# Install the app to be tested 
# Comment out the following line if your app is already installed
monkeyrunner $PWD/invokeTestingApk/scripts/installScript.py $SIGNED_APK
monkeyrunner $PWD/invokeTestingApk/scripts/installScript.py $SIGNED_TESTER

mkdir -p logcatOutputs
adb logcat ActivityManager:I *:S --line-buffered > "$PWD/logcatOutputs/AllActivities.log" &
adb logcat -s "MyScheduler" --line-buffered > "$PWD/logcatOutputs/MyScheduler.log" &
adb logcat -s *:E --line-buffered > "$PWD/logcatOutputs/AllErrors.log" &

# Invoke testing
adb shell am instrument -w my.example.test/android.test.InstrumentationTestRunner

# Kill the child processes
trap "kill 0" SIGINT SIGTERM EXIT

exit