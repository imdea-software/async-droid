def onInstallationError():
    print "Could not install the application. "
    print "Some possible problems:"
    print "1. Your package may not be signed. Please sign it using jarsigner."
    print "2. Your package may be already installed."
    print "   Uninstall the package by typing the following commands to the command line: "
    print "       $ adb uninstall my.example.HelloWorld"
    print "3. Uninstalling process might not have removed all previous files."
    print "   Check the packge name in adb shell by typing the following commands to the command line: "
    print "       $ adb shell"
    print "   (i) If the app is not a default Android app:"
    print "       root@android:# ls /data/data"
    print "       If your package is listed, remove it:"
    print "       root@android:# rm -R /data/data/my.example.HelloWorld "
    print "   (ii) If the app is a default Android app:"
    print "       root@android:# pm list packages"
    print "   If your package is listed, enter the following commands:"
    print "       root@android:# remount"
    print "       root@android:# rm /system/app/PackageName.apk"
    print "Otherwise, try installing on adb shell:" 
    print "$ adb install <FullPathToYourApkFile/Name.apk>"


def onInputExecutionError(val):
    if (val == 2):
        print "Could not find the input script file."
    

