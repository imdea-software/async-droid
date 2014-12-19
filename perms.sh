#!/bin/sh

echo "ASE: Fixing permissions..."
export app_uname=`ls -al | cut -d \  -f 2 | awk 'NR==1'`
echo "ASE: &app_uname"
chown $app_uname:$app_uname files
chown $app_uname:$app_uname files/parameters.json
echo "ASE: Done. "
