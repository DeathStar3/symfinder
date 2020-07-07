#!/bin/sh
RELEASE=53.0.3
FILE_NAME=firefox-$RELEASE.source.tar.xz
echo "####### FIREFOX $RELEASE SOURCE CHECKOUT ########"
echo "### STEP 1/2 : Download"
wget https://archive.mozilla.org/pub/firefox/releases/$RELEASE/source/$FILE_NAME
echo "### STEP 2/2 : Untar"
tar xvfJ $FILE_NAME
echo "####### FIREFOX $RELEASE SOURCE CHECKOUT DONE ########"
