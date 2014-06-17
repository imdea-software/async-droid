# Imports the monkeyrunner modules used by this program
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

import sys

if (len(sys.argv) != 3): # monkeyrunner <scriptFile> <PackageName> <MainActivity>
	print ("Enter the arguments: <PackageName> <MainActivity>")
#sys.exit()

print "Application package name: " + sys.argv[1]
print "Activity name: " + sys.argv[2]


# Connects to the current device, returning a MonkeyDevice object
device = MonkeyRunner.waitForConnection()

# sets a variable with the package's internal name
package = sys.argv[1]

# sets a variable with the name of an Activity in the package
activity = sys.argv[2]

# sets the name of the component to start
runComponent = package + '/' + activity

# Runs the component
device.startActivity(component=runComponent)

