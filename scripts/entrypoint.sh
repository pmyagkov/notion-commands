#!/bin/bash

while test $1
do
  echo $1
  case "$1" in
    -l|--log-file)
      shift
      LOG_FILE="$1"
      ;;
    -n)
      shift
      NO_LAUNCH="$1"
      ;;
  esac
  shift
done

echo "NO_LAUNCH = ${NO_LAUNCH}"
echo "LOG_FILE = ${LOG_FILE}"

LAUNCH_COMMAND="lein run"
if [[ -n $LOG_FILE ]]; then
  LAUNCH_COMMAND="$LAUNCH_COMMAND > $LOG_FILE 2>&1"
fi

echo $LAUNCH_COMMAND

if [[ $NO_LAUNCH != 1 ]]; then
  echo "Launching the service..."
  `echo $LAUNCH_COMMAND`
else
  bash
fi
