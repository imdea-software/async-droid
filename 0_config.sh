#!/bin/bash

## FROM STEP 1

# full path to the .apk file you want to instrument
APK_FILE=$PWD/example/HelloWorldApp.apk

## FROM STEP 2

# Before building the test apk, make sure to provide the following.
# NOTE: Package name and the main activity name can be found from the manifest
# file of the application project.

# package name of your app into ......
PACKAGE_NAME="my.example.HelloWorld"

# main activity name of your app ....
MAIN_ACTIVITY="my.example.HelloWorld.MainActivity"

## FROM STEP 3


# package name of your app
APK_PACKAGE_NAME="my.example.HelloWorld"

## REQUIRED TOOLS
for X in apktool aapt
do
  if [ -z $(which $X) ]
  then
    echo "'$X' was not found in the PATH."
    exit 1
  fi
done

for X in \
 APK_FILE \
 PACKAGE_NAME MAIN_ACTIVITY \
 APK_PACKAGE_NAME
do
 if [ -z "$(eval echo "\$$X")" ]
 then
   echo "Please edit 0_config.sh to set certain variables to their proper values."
   echo "The $X variable was not set."
   exit 1
 fi
done
