 # Script file to test a(n) (instrumented) application.
 # Uncomment the lines beginning with ## to test the instrumented example HelloWorldApp.apk

 ANDROID_JAR= # full path to Android jar file 

 # NOTE: Package name and the main activity name can be found from the manifest file of the application project. 
 
 PATH_TO_SIGNED_APK= # full path to your instrumented application file
 ## PATH_TO_SIGNED_APK="${PWD}/example/signedHelloWorld/HelloWorldApp.apk"
 PACKAGE_NAME= # package name of your app
 ## PACKAGE_NAME="my.example.HelloWorld"
 MAIN_ACTIVITY= # main activity name of your app
 ## MAIN_ACTIVITY="my.example.HelloWorld.MainActivity"

 TESTER_CLASSPATH="${CLASSPATH}:${ANDROID_JAR}:${PWD}/lib/libForTester/jython-standalone-2.5.3.jar:${PWD}/invokeTestingApk/src"

 # compile tester file
 javac -cp ${TESTER_CLASSPATH} ${PWD}/invokeTestingApk/src/ApkExecutor.java

 # execute tester file (the last three arguments are: <Install? (Y/N)> <Invoke? (Y/N)> <Test? (Y/N)>)
 java -cp ${TESTER_CLASSPATH} ApkExecutor ${PATH_TO_SIGNED_APK} ${PACKAGE_NAME} ${MAIN_ACTIVITY} yes yes yes