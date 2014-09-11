# Imports the monkeyrunner modules used by this program
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
from sys import exit

import sys, subprocess, getopt, printErrorScript

toInstall = False;

try:
    opts, args = getopt.getopt(sys.argv[1:], 'h', ['install', 'apkPath=', 'package=', 'activity=', 'delay='])
except getopt.GetoptError:
    print '<scriptFile> --install --apkPath <FullPathOfApkFile> --package <PackageName> --activity <MainActivity> --delays <delayNum>'
    sys.exit(-1)
for opt, arg in opts:
    if opt == '-h':
        print 'Usage: --install <scriptFile> --apkPath <FullPathOfApkFile> --package <PackageName> --activity <MainActivity> --delays <delayNum>' 
        sys.exit()
    elif opt == "--install":
        toInstall = True
    elif opt == "--apkPath":
        apkPath = arg
    elif opt == "--package":
        package = arg
    elif opt == "--activity":
        activity = arg
    elif opt == "--delay":
        delay = arg
   
print "APPLICATION PARAMETERS: "    
print "Application apk file: " + apkPath
print "Application package name: " + package
print "Activity name: " + activity

print "ANALYSIS PARAMETERS: "
print "Number of delays: " + delay


# Connects to the current device, returning a MonkeyDevice object
device = MonkeyRunner.waitForConnection()

# Uninstall if exists
# device.removePackage(sys.argv[2])

# (1) Installs the Android package
if (toInstall):
    print "Installing the application."    
    installed = device.installPackage(apkPath)
    if(installed == False):
        printErrorScript.onInstallationError()
        exit(1)


# (2) Run the appication
print "Invoking the application." 

# sets the name of the component to start
runComponent = package + '/' + activity

# Runs the component
device.startActivity(component=runComponent)


# (3) Execute inputs on the application
print "Executing inputs."    
ret = subprocess.call("python ${PWD}/invokeTestingApk/scripts/inputScript.py", shell=True)
if(ret == 2):
    printErrorScript.onInputExecutionError(2)
    exit(2)


# (4) Close the application
print "Closing the application."    
device.shell('am force-stop ' + package)
