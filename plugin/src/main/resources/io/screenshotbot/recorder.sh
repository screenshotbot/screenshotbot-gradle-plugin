#!/bin/sh
set -e

type=`uname`

INSTALLER=screenshotbot-installer.sh
CURL="curl --retry 3 "
VERSION=`$CURL --fail https://screenshotbot.io/recorder-version/current || true`

if [ $type = "Linux" ] ; then
  if [ "`uname -m`" = "aarch64" ] ; then
    $CURL --progress-bar https://cdn.screenshotbot.io/artifact/${VERSION}recorder-linux-arm64 --output $INSTALLER
  else
    $CURL --progress-bar https://cdn.screenshotbot.io/artifact/${VERSION}recorder-linux --output $INSTALLER
  fi
elif [ $type = "Darwin" ] ; then
  $CURL --progress-bar https://cdn.screenshotbot.io/artifact/${VERSION}recorder-darwin --output $INSTALLER
else
  echo Unknown uname type: $type, please message support@screenshotbot.io
fi
sh ./$INSTALLER
rm -f $INSTALLER
