#!/bin/bash

source 0_my.config

# Build the tester application:
cd buildTesterApk/TestApk
android update project -p .
ant release
cd ..
cd ..

echo 'TestAndroidApp-release-unsigned.apk is generated in the bin directory.'
echo 'Sign that file before installation.'