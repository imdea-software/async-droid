# The current configuration tests:
# PACKAGE_NAME="my.example.HelloWorld"
# MAIN_ACTIVITY="my.example.HelloWorld.MainActivity"

# If you want to test another application:
# Before building the test apk, make sure that you have provided:
# PACKAGE_NAME= # package name of your app into ......
# MAIN_ACTIVITY= # main activity name of your app ....
# NOTE: Package name and the main activity name can be found from the manifest file of the application project. 

# Build the tester application:
cd buildTesterApk/TestApk
android update project -p .
ant release
cd ..
cd ..

echo 'TestAndroidApp-release-unsigned.apk is generated in the bin directory.'
echo 'Sign that file before installation.'