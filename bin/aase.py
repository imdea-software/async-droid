#!/usr/bin/env python

NAME = 'Android App Schedule Enumerator'
VERSION = '0.1'
DESCRIPTION = NAME + ' version ' + VERSION
options = []

import argparse
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
  if options.debug: print "CALLING", " ".join(cmd)
  subprocess.call(cmd, stdout=open(os.devnull,'w'), stderr=subprocess.STDOUT)

def uninstall(app_name):
  print "Uninstalling", app_name
  cmd = ["adb", "shell", "pm", "uninstall", app_name]
  if options.debug: print "CALLING", " ".join(cmd)
  subprocess.call(cmd, stdout=open(os.devnull,'w'), stderr=subprocess.STDOUT)

def start(app_name, activity, *args):
  print "Starting", app_name
  cmd = ["adb", "shell", "am", "start", app_name + "/" + activity]
  for a in args:
    cmd += ["-e"]
    cmd += a.split()
  if options.debug: print "CALLING", " ".join(cmd)
  subprocess.call(cmd, stdout=open(os.devnull,'w'), stderr=subprocess.STDOUT)

def stop(app_name):
  print "Stopping", app_name
  cmd = ["adb", "shell", "am", "force-stop", app_name]
  if options.debug: print "CALLING", " ".join(cmd)
  subprocess.call(cmd, stdout=open(os.devnull,'w'), stderr=subprocess.STDOUT)

def start_up(app_path, *args):
  if not device_is_running():
    sys.exit("Device is not running.")

  app_name = package_name(app_path)
  activity = launchable_activity(app_path)

  if not app_is_installed(app_name):
    install(app_name, app_path)

  if not app_is_running(app_name):
    start(app_name, activity, *args)

  return app_name

def tear_down(app_name):
  if app_is_running(app_name):
    stop(app_name)

  if app_is_installed(app_name):
    uninstall(app_name)

def do_record(app_path):
  app_name = start_up(app_path, "mode record")

  # TODO the recording...

  tear_down(app_name)

def do_replay(app_path, num_delays):
  app_name = start_up(app_path, "mode replay", "numDelays 2")

  # TODO the replaying...

  tear_down(app_name)

def validate_apk_file(f):
  if os.path.exists(f) and os.path.splitext(f)[1] == '.apk':
    return f
  else:
    sys.exit("Expected the path to an Android app's APK file.")

def aase_parser():
  p = argparse.ArgumentParser(description=DESCRIPTION)

  p.add_argument('--version', action='version', version=DESCRIPTION)

  p.add_argument('--debug',
    dest='debug', action='store_true', default=False,
    help='turn on debugging')

  p.add_argument('apkfile', metavar='APK-FILE',
    type=lambda f: validate_apk_file(f),
    help='the APK file of an Android app')

  p.add_argument('--record',
    dest='mode', action='store_const', const='record', default='record',
    help='run the app in record mode (default)')

  p.add_argument('--replay',
    dest='mode', action='store_const', const='replay',
    help='run the app in replay mode')

  p.add_argument('--delays',
    dest='delays', metavar='K', type=int, default=0,
    help='number of scheduler delays')

  return p

if __name__ == '__main__':
  options = aase_parser().parse_args()

  print "Running %s on %s in %s-mode" % (NAME, options.apkfile, options.mode)

  if options.mode == 'replay':
    do_replay(options.apkfile, options.delays)

  else:
    do_record(options.apkfile)
