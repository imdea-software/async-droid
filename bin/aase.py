#!/usr/bin/env python

import os.path
import re
import subprocess
import sys

def package_name(app_path):
  m = re.search(r'package: name=\'([^\']*)\'',
    subprocess.check_output(["aapt", "dump", "badging", app_path]))
  if m: return m.group(1)
  else: sys.exit("Could not find package name for " + app_path + ".")

def launchable_activity(app_path):
  m = re.search(r'launchable-activity: name=\'([^\']*)\'',
    subprocess.check_output(["aapt", "dump", "badging", app_path]))
  if m: return m.group(1)
  else: sys.exit("Could not find launchable acitvity for " + app_path + ".")

def device_is_running():
  return subprocess.call(["adb", "shell", "echo"],
    stdout=open(os.devnull,'w'), stderr=subprocess.STDOUT) == 0

def app_is_installed(app_name):
  return "package:" in subprocess.check_output(["adb", "shell", "pm", "list", "packages", app_name])

def app_is_running(app_name):
  return app_name in subprocess.check_output(["adb", "shell", "ps"])

def install(app_name, app_path):
  print "Installing", app_name
  cmd = ["adb", "install", app_path]
  subprocess.call(cmd, stdout=open(os.devnull,'w'), stderr=subprocess.STDOUT)

def uninstall(app_name):
  print "Uninstalling", app_name
  cmd = ["adb", "shell", "pm", "uninstall", app_name]
  subprocess.call(cmd, stdout=open(os.devnull,'w'), stderr=subprocess.STDOUT)

def start(app_name, activity, *args):
  print "Starting", app_name
  cmd = ["adb", "shell", "am", "start", app_name + "/" + activity]
  for a in args:
    cmd += ["-e"]
    cmd += a.split()
  subprocess.call(cmd, stdout=open(os.devnull,'w'), stderr=subprocess.STDOUT)

def stop(app_name):
  print "Stopping", app_name
  cmd = ["adb", "shell", "am", "force-stop", app_name]
  subprocess.call(cmd, stdout=open(os.devnull,'w'), stderr=subprocess.STDOUT)

def example_run(app_path):
  if not device_is_running():
    sys.exit("Device is not running.")

  app_name = package_name(app_path)
  activity = launchable_activity(app_path)

  if not app_is_installed(app_name):
    install(app_name, app_path)

  if not app_is_running(app_name):
    start(app_name, activity)

  # NOW RUN ALL THE TESTS WE WANT HERE, BEFORE FINALLY TEARING DOWN

  if app_is_running(app_name):
    stop(app_name)

  if app_is_installed(app_name):
    uninstall(app_name)


if len(sys.argv) < 2 or \
   not os.path.exists(sys.argv[1]) or \
   os.path.splitext(sys.argv[1])[1] != '.apk':
  sys.exit("Expected the path to an app's APK file.")

example_run(sys.argv[1])
