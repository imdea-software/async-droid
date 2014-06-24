# Imports the monkeyrunner modules used by this program
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
from sys import exit

import sys

if (len(sys.argv) != 2): # monkeyrunner <scriptFile> <FullPathOfApkFile>
	print ("Enter the argument: <FullPathOfApkFile>")

print "Application apk file: " + sys.argv[1]

# Connects to the current device, returning a MonkeyDevice object
device = MonkeyRunner.waitForConnection()

# Installs the Android package
installed = device.installPackage(sys.argv[1])


if(installed == False):
    print "Could not install the application. "
    exit(1)
