#!/bin/bash

KEYSTORE=my.keystore
ALIAS=my_alias

set -e

if [ ! -f "$1" ]
then
  echo "Please supply an APK file."
  exit 1
fi

if [ ! -f $KEYSTORE ]
then
  keytool -genkey -keyalg RSA -keysize 2048 -validity 10000 \
    -noprompt -dname "CN=a, OU=b, O=c, L=d, S=e, C=f" \
    -alias $ALIAS -keystore $KEYSTORE \
    -storepass abcdefg -keypass abcdefg
fi

sudo jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore $KEYSTORE -storepass abcdefg -keypass abcdefg \
  "$1" $ALIAS
