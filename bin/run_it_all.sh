#!/bin/bash

# Script file to instrument an application.

ROOT=${PWD}
BIN=${ROOT}/bin
LIB=${ROOT}/lib
BUILD=${ROOT}/build

set -e

if [ ! -f ${BUILD}/merger.jar ] || [ ! -f ${BUILD}/instrumentor.jar ]
then
  echo "Project must be built first; run 'ant'."
  exit 1
fi

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
APP_NAME=$(basename "$1" .apk)

MERGED=${BUILD}/${APP_NAME}-merged
EXTRACTED=${BUILD}/${APP_NAME}-extracted
DECODED=${BUILD}/${APP_NAME}-decoded

REINSTRUMENT=
REBUILD_TESTER=
RESIGN=

if [ ! -f "${BUILD}/${APP_NAME}-unsigned.apk" ] || [ -n "${REINSTRUMENT}" ]
then

  echo "-------------------------------------------------------------------------"
  echo "                      PHASE 1 : INSTRUMENT THE APK                       "
  echo "-------------------------------------------------------------------------"

  # Extract the .apk file to get classes.dex
  echo "Extracting ${APK_FILE} using apktool."
  mkdir -p ${MERGED}
  mkdir -p ${EXTRACTED}
  mkdir -p ${DECODED}

  unzip -u -d ${EXTRACTED} "${APK_FILE}"

  # Decode the manifest file using apktool (necessary to get original manifest and
  # resource files)
  echo "Decoding ${APK_FILE} using apktool."
  apktool d -f "${APK_FILE}" ${DECODED}

  echo "Merging application dex file with myScheduler dex file to be used in the instrumentation."
  # Merge the classes.dex file with myScheduler.dex file
  MERGER_CLASSPATH=${CLASSPATH}
  MERGER_CLASSPATH+=:${LIB}/dx.jar
  MERGER_CLASSPATH+=:${BUILD}/merger.jar

  # Merge the classes.dex file with myScheduler.dex file
  java -cp ${MERGER_CLASSPATH} mergeDexFiles/MergeDexFiles \
    ${EXTRACTED}/classes.dex ${BUILD}/scheduler.dex ${MERGED}/classes.dex
  
  # Recreate .apk file using aapt tool: (if you have more than one res file, add
  # all into the command)
  echo Recreating .apk file in ${EXTRACTED}/${APP_NAME}.
  MIN_SDK_LEVEL=$(grep minSdkVersion ${DECODED}/apktool.yml | grep -o "[0-9]\+")
  TARGET_SDK_LEVEL=$(grep targetSdkVersion ${DECODED}/apktool.yml | grep -o "[0-9]\+")
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
  aapt package -f -M ${DECODED}/AndroidManifest.xml -S ${DECODED}/res/ \
    -I "$ANDROID_JAR" --min-sdk-version $MIN_SDK_LEVEL \
    --target-sdk-version $TARGET_SDK_LEVEL -F ${BUILD}/${APP_NAME}.apk

  # add the new classes.dex into the .apk file
  cd ${MERGED}
  aapt add -f ${BUILD}/${APP_NAME}.apk classes.dex
  cd ${ROOT}
  
  echo Instrumenting .apk file ${BUILD}/${APP_NAME}.apk.
  # Compile instrumentor
  INSTRUMENTOR_CLASSPATH=${CLASSPATH}
  INSTRUMENTOR_CLASSPATH+=:${ANDROID_JAR}
  INSTRUMENTOR_CLASSPATH+=:${LIB}/coffer.jar
  INSTRUMENTOR_CLASSPATH+=:${LIB}/jasminclasses.jar
  INSTRUMENTOR_CLASSPATH+=:${LIB}/java_cup.jar
  INSTRUMENTOR_CLASSPATH+=:${LIB}/JFlex.jar
  INSTRUMENTOR_CLASSPATH+=:${LIB}/pao.jar
  INSTRUMENTOR_CLASSPATH+=:${LIB}/polygot.jar
  INSTRUMENTOR_CLASSPATH+=:${LIB}/pth.jar
  INSTRUMENTOR_CLASSPATH+=:${LIB}/soot.jar
  INSTRUMENTOR_CLASSPATH+=:${LIB}/sootclasses.jar
  INSTRUMENTOR_CLASSPATH+=:${BUILD}/instrumentor.jar

  # Run the instrumentor
  rm -rf sootOutput
  java -cp ${INSTRUMENTOR_CLASSPATH} myInstrumentor/AndroidInstrument \
    ${BUILD}/${APP_NAME}.apk ${ANDROID_HOME}/platforms ${PWD}/src \
    -output-format dex
  mv -f sootOutput/${APP_NAME}.apk ${BUILD}/${APP_NAME}-unsigned.apk
  rm -rf sootOutput

  echo "Instrumentation of ${APP_NAME} is complete."
  echo "Find the instrumented .apk file in ${BUILD}/${APP_NAME}-unsigned.apk"
fi

if [ ! -f "${BUILD}/TestApk-unsigned.apk" ] || [ -n "$REBUILD_TESTER" ]
then

  echo "-------------------------------------------------------------------------"
  echo "                      PHASE 2 : BUILD THE TESTER APP                     "
  echo "-------------------------------------------------------------------------"

  # Build the tester application:
  cd tester-app
  android update project -p .
  ant release
  cp bin/TestApk-release-unsigned.apk ${BUILD}/TestApk-unsigned.apk
  cd ..
  echo "${BUILD}/TestApk-unsigned.apk has been generated, and must be signed."
fi

if [ ! -f "${BUILD}/${APP_NAME}-signed.apk" ] || [ ! -f "${BUILD}/TestApk-signed.apk" ] || [ -n "$RESIGN" ]
then

  echo "-------------------------------------------------------------------------"
  echo "                      PHASE 3 : SIGN THE APK FILES                       "
  echo "-------------------------------------------------------------------------"

  KEYSTORE=${BUILD}/my.keystore
  STOREPASS=abcdefg
  KEYPASS=abcdefg
  ALIAS=my_alias

  cp -f "${BUILD}/${APP_NAME}-unsigned.apk" "${BUILD}/${APP_NAME}-signed.apk"
  cp -f "${BUILD}/TestApk-unsigned.apk" "${BUILD}/TestApk-signed.apk"

  if [ ! -f ${KEYSTORE} ]
  then
    keytool -genkey -keyalg RSA -keysize 2048 -validity 10000 \
      -noprompt -dname "CN=a, OU=b, O=c, L=d, S=e, C=f" \
      -alias $ALIAS -keystore ${KEYSTORE} \
      -storepass $STOREPASS -keypass $KEYPASS
  fi

  jarsigner -sigalg SHA1withRSA -digestalg SHA1 \
    -keystore $KEYSTORE -storepass $STOREPASS -keypass $KEYPASS \
    "${BUILD}/${APP_NAME}-signed.apk" $ALIAS

  jarsigner -sigalg SHA1withRSA -digestalg SHA1 \
    -keystore $KEYSTORE -storepass $STOREPASS -keypass $KEYPASS \
    "${BUILD}/TestApk-signed.apk" $ALIAS
fi

echo "-------------------------------------------------------------------------"
echo "                      PHASE 4 : INVOKE TESTING APK                       "
echo "-------------------------------------------------------------------------"

# Install the app to be tested 
# Comment out the following line if your app is already installed
monkeyrunner ${BIN}/installScript.py "${BUILD}/${APP_NAME}-signed.apk"
monkeyrunner ${BIN}/installScript.py "${BUILD}/TestApk-signed.apk"

mkdir -p logcatOutputs
adb logcat ActivityManager:I *:S --line-buffered > "$PWD/logcatOutputs/AllActivities.log" &
adb logcat -s "MyScheduler" --line-buffered > "$PWD/logcatOutputs/MyScheduler.log" &
adb logcat -s *:E --line-buffered > "$PWD/logcatOutputs/AllErrors.log" &

# Invoke testing
adb shell am instrument -w my.apktester.test/android.test.InstrumentationTestRunner

# Kill the child processes
trap "kill 0" SIGINT SIGTERM EXIT

exit