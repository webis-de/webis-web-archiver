#!/bin/bash

OPTS=$(getopt --name $(basename $0) --options a:r:u:o:s:v:d: --longoptions archive:,reproductionmode:,url:,output:,script:,scriptversion:,scriptsdirectory: -- $@)
if [[ $? -ne 0 ]]; then
    exit 2
fi
eval set -- "$OPTS"

while true;do
  case "$1" in
    -a|--archive)
      archive=$(readlink -f -- "$2")
      shift 2
      ;;
    -r|--reproductionmode)
      mode="-$2"
      shift 2
      ;;
    -u|--url)
      url="$2"
      shift 2
      ;;
    -o|--output)
      output="$2"
      shift 2
      ;;
    -s|--script)
      script="--env SCRIPT=$2"
      shift 2
      ;;
    -v|--scriptversion)
      scriptversion="--env SCRIPT_VERSION=$2"
      shift 2
      ;;
    -d|--scriptsdirectory)
      scriptsdirectory="--volume $(readlink -f -- $2):/resources/scripts/:ro"
      shift 2
      ;;
    --)
      break
      ;;
  esac
done

if [ \( -z "$archive" \) -o \( -z "$url" \) -o \( -z "$output" \) ];then
  echo "USAGE"
  echo "  $0 [OPTIONS] --archive <directory> --url <url> --output <directory>"
  echo "WHERE"
  echo "  --archive"
  echo "    Gives the archive directory created by archive.sh"
  echo "  --url"
  echo "    Gives the start URL for the script"
  echo "  --output"
  echo "    Gives the directory to which logs and the script output are written"
  echo "  --script"
  echo "    Gives the name of the script to run (default: screenshot)"
  echo "  --scriptversion"
  echo "    Gives the minimum compatible version of the script (default: 1.0.0)"
  echo "  --scriptsdirectory <directory>"
  echo "    Gives the directory that contains all script directories. The script"
  echo "    directories are named <name>-<version> and contain the script.conf"
  echo "    and all resources used by the script. This is needed if a different"
  echo "    script to the default one is used."
  echo "  --reproductionmode"
  echo "    Changes the program used for web page reproduction from a custom"
  echo "    implementation to warcprox or pywb"
  exit 1
fi

mkdir -p $output # Creating directory is required to give web-archiver user permission to write
output=$(readlink -f -- $output)

maindir=$(readlink -f -- $(dirname $0)/..)

is_in_docker_group=$(groups | sed 's/ /\n/g' | grep '^docker$' | wc -l)

# Mounting /dev/shm is required for taking big screenshot in chrome
# /warcs/ can not be mounted read-only for warcprox mode (which does, however, not change anything, but tests that it could write on startup)
command="docker run --rm --user $(id -u) --env URL=\"$url\" --env DBUS_SESSION_BUS_ADDRESS=/dev/null --env MODE="reproduce$mode" $script $scriptversion $scriptsdirectory --volume $archive:/warcs/ --volume $output:/output/ --volume /dev/shm/:/dev/shm -a stdout -a stderr webis/web-archive-environment:1.2.1"
if [ $is_in_docker_group -eq 0 ];then
  sudo $command
else
  $command
fi

