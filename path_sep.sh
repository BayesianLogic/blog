#!/bin/sh
#
# Script invoked by the top-level Makefile to get the path separator
# for this operating system.  The path separator is ";" on Windows and
# ":" elsewhere (Linux, MacOS).  We determine the operating system by
# checking the OS environment variable, which has the value
# "Windows_NT" on Windows NT, 2000, XP, Vista, etc.  We don't consider
# earlier versions of Windows.

if [ "$OS" = "Windows_NT" ]; then
    echo ";"
else
    echo ":"
fi