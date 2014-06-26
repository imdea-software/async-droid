 # Script file to test a(n) (instrumented) application.
 # Uncomment the lines beginning with ## to test the instrumented example HelloWorldApp.apk

 # NOTE: Package name and the main activity name can be found from the manifest file of the application project. 
 
 PATH_TO_SIGNED_APK= # full path to your instrumented application file
 ## PATH_TO_SIGNED_APK="${PWD}/example/signedHelloWorld/HelloWorldApp.apk"
 PACKAGE_NAME= # package name of your app
 ## PACKAGE_NAME="my.example.HelloWorld"
 MAIN_ACTIVITY= # main activity name of your app
 ## MAIN_ACTIVITY="my.example.HelloWorld.MainActivity"

 NUM_DELAYS= # Total numbr of delays to be spent in the analysis
 ## NUM_DELAYS=0
 
NUM_DELAYS=1

mkdir logcatOutputs
cd logcatOutputs
touch "MyScheduler.log"
cd ..
adb logcat -s "MyScheduler" --line-buffered > "${PWD}/logcatOutputs/MyScheduler.log" &

# Remove the installation option if the app to be tested is already installed 
monkeyrunner ${PWD}/invokeTestingApk/scripts/executorScript.py --install --apkPath ${PATH_TO_SIGNED_APK} --package ${PACKAGE_NAME} --activity ${MAIN_ACTIVITY} --delay 1

#TODO: Kill the child process
exit