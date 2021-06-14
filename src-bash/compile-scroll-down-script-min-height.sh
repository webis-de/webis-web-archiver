#!/bin/bash

# input
scriptfile=de/webis/webarchive/environment/scripts/ScrollDownScriptMinHeight.java
scriptname=scroll-down-min-height
scriptversion=1.0.0

mkdir -p scripts
scriptdir=scripts/$scriptname-$scriptversion
mkdir -p $scriptdir

if [ -e webis-web-archiver.jar ];then
  echo "Binaries are already downloaded"
else
  echo "Downloading binaries"
  wget https://github.com/webis-de/webis-web-archiver/releases/download/0.1.0/webis-web-archiver.jar
fi

mkdir -p build

echo "Compiling"
javac -cp src:webis-web-archiver.jar src/$scriptfile --release 8 -d build

echo "Adding resources"
cp -r resources/* build

echo "Creating JAR for $scriptdir"
jar cf $scriptdir/script.jar -C build .

echo "Creating configuration for $scriptdir"
scriptconf=$scriptdir/script.conf
scriptclass=$(echo $scriptfile | sed 's/\//./g' | sed 's/\.java$//')
echo -e "script = $scriptclass\nenvironment.name = de.webis.java\nenvironment.version = 1.0.0" | tee $scriptconf

