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

MERGER_CLASSPATH="${CLASSPATH}:${ANDROID_JAR}:${PWD}/lib/libForMergingDex/dx.jar:${PWD}/instrumentAndroidApk/mergeDexes/src"
INSTRUMENTOR_LIB="${PWD}/lib/libForInstrumentor"
INSTRUMENTOR_CLASSPATH="${CLASSPATH}:${ANDROID_JAR}:${INSTRUMENTOR_LIB}/lib/coffer.jar:${INSTRUMENTOR_LIB}/jasminclasses.jar:\
${INSTRUMENTOR_LIB}/java_cup.jar:${INSTRUMENTOR_LIB}/JFlex.jar:${INSTRUMENTOR_LIB}/pao.jar:\
${INSTRUMENTOR_LIB}/polygot.jar:${INSTRUMENTOR_LIB}/pth.jar:${INSTRUMENTOR_LIB}/soot.jar:\
${INSTRUMENTOR_LIB}/sootclasses.jar:${PWD}/instrumentAndroidApk/instrumentApk/src/myInstrumentor"

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
sudo javac -cp ${MERGER_CLASSPATH} \
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
sudo javac -cp $INSTRUMENTOR_CLASSPATH \
  instrumentAndroidApk/instrumentApk/src/myInstrumentor/*.java

# Run the instrumentor
sudo rm -f "sootOutput/$APK_NAME"
sudo java -cp $INSTRUMENTOR_CLASSPATH AndroidInstrument \
  $PWD/mergerOutput/$APK_NAME $ANDROID_HOME/platforms $PWD/src \
  -output-format dex

echo "Instrumentation of $APK_NAME is complete."
echo "Find the instrumented .apk file in $PWD/sootOutput"
