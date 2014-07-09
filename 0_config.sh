#!/bin/bash

## FROM STEP 1

# full path to Android SDK directory   
ANDROID_SDK_HOME=

# full path to Android jar file (version corresponding to your apk target SDK level)
ANDROID_JAR=

# full path to the apk tool shell file
APKTOOL_HOME=

# full path to the aapt tool shell file (path depends on your SDK version)
AAPTTOOL_HOME=

# NOTE: Min and Max SDK levels can be found from the manifest file or (mostly)
# the web page of the application project.

# default is 16
MIN_SDK_LEVEL=

# default is 19
MAX_SDK_LEVEL=

# full path to the .apk file you want to instrument, e.g., "${PWD}/example"
APK_PATH=

## name of the APK file, e.g., "HelloWorldApp.apk" 
APK_NAME=


## FROM STEP 2

# Before building the test apk, make sure to provide the following.
# NOTE: Package name and the main activity name can be found from the manifest
# file of the application project.

# package name of your app into ......
PACKAGE_NAME="my.example.HelloWorld"

# main activity name of your app ....
MAIN_ACTIVITY="my.example.HelloWorld.MainActivity"


## FROM STEP 3

# full path to your instrumented and signed application file
PATH_TO_SIGNED_APK="${PWD}/example/signed/HelloWorldApp.apk"

# full path to your signed tester application file
PATH_TO_SIGNED_TESTER="${PWD}/example/signed/TestApk-release-signed.apk"

# package name of your app
APK_PACKAGE_NAME="my.example.HelloWorld"

for X in \
 ANDROID_SDK_HOME ANDROID_JAR \
 APKTOOL_HOME AAPTTOOL APK_PATH APK_NAME \
 MIN_SDK_LEVEL MAX_SDK_LEVEL \
 PACKAGE_NAME MAIN_ACTIVITY \
 PATH_TO_SIGNED_APK PATH_TO_SIGNED_TESTER APK_PACKAGE_NAME
do
 if [ -z "$(eval echo "\$$X")" ]
 then
   echo "Please edit 0_config.sh to set certain variables to their proper values."
   echo "The $X variable was not set."
   exit 1
 fi
done
