#!/usr/bin/sh -eu
export JAVA_ARGS=-Dartemis.instance=./
. etc/artemis.profile
if [ "${ARTEMIS_HOME}" = "" ]
then
    echo
    echo "    ERROR: You must set the env var ARTEMIS_HOME to use this wrapper."
    echo
    exit 1
fi
$ARTEMIS_HOME/bin/artemis $@ --broker etc/broker.xml
