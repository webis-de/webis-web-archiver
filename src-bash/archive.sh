#/bin/bash

OPTS=$(getopt --name $(basename $0) --options u:o:s:v:d: --longoptions url:,output:,script:,scriptversion:,scriptsdirectory: -- $@)
if [[ $? -ne 0 ]]; then
    exit 2
fi
eval set -- "$OPTS"

while true;do
  case "$1" in
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
      scriptsdirectory="--volume $(realpath $2):/resources/scripts/:ro"
      shift 2
      ;;
    --)
      break
      ;;
  esac
done

if [ \( -z "$url" \) -o \( -z "$output" \) ];then
  echo "USAGE"
  echo "  $0 [OPTIONS] --url <url> --output <directory>"
  echo "WHERE"
  echo "  --url <url>"
  echo "    Gives the start URL for the script"
  echo "  --output <directory>"
  echo "    Gives the directory to which the archive, logs, and the script"
  echo "    output are written"
  echo "  --script <name>"
  echo "    Gives the name of the script to run (default: scroll-down)"
  echo "  --scriptversion <version>"
  echo "    Gives the minimum compatible version of the script (default: 1.0.0)"
  echo "  --scriptsdirectory <directory>"
  echo "    Gives the directory that contains all script directories. The script"
  echo "    directories are named <name>-<version> and contain the script.conf"
  echo "    and all resources used by the script. This is needed if a different"
  echo "    script to the default one is used."
  exit 1
fi

mkdir -p $output
output=$(realpath $output)
warcs=$output/archive
mkdir -p $warcs # Creating directory is required to give web-archiver user permission to write

maindir=$(realpath $(dirname $0)/..)

is_in_docker_group=$(groups | sed 's/ /\n/g' | grep '^docker$' | wc -l)

# Mounting /dev/shm is required for taking big screenshot in chrome
command="docker run --rm --user $(id -u) --env URL=\"$url\" --env DBUS_SESSION_BUS_ADDRESS=/dev/null $script $scriptversion $scriptsdirectory --volume $warcs:/warcs --volume $output:/output/ --volume /dev/shm/:/dev/shm -a stdout -a stderr webis/web-archive-environment:1.0.0"
if [ $is_in_docker_group -eq 0 ];then
  sudo $command
else
  $command
fi

