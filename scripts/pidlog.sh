#!/bin/bash
#
# Usage: pidlog.sh <package> [logcat args...]
#
# Create an adb logcat instance for the pid of the given package.
#
# Given a package name, this script discovers the pid of the process that
# belongs to the package and starts an adb logcat instance to monitor all
# logging messages that originate from the pid. The script also automatically
# handles cases where the package is not running, or when the pid changes due
# to application restart.
#
# The first argument must be package name. The following arguments (if any)
# are passed to adb logcat verbatim.
#
# Written by Jan Janak <janakj@cs.columbia.edu>
#

package=$1
shift

find_pid="adb shell ps | grep $package | tr -s [:space:] ',' | cut -d , -f 2"

until_pid_changes() {
    local orig_pid=$1
    while true ; do
	local pid=$($find_pid)
	if [ "$orig_pid" != "$pid" ] ; then
	    break
	fi
	sleep 1
    done
}


while true ; do
    pid=$($find_pid)
    if [ -z "$pid" ] ; then
	echo "Waiting for package $package to get started"
	while true ; do
	    sleep 1
	    pid=$($find_pid)
	    [ -n "$pid" ] && break;
	done
    fi

    echo "Package $package found at pid $pid, starting adb logcat"
    adb logcat --pid $pid $@ &
    trap 'kill $!' EXIT

    until_pid_changes $pid
    echo "Pid $pid is gone, killing adb logcat"
    kill $!
done
