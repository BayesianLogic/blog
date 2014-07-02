#!/bin/bash
#
# Script for running the BLOG parser.  Typical usage:
#
# ./parse.sh  <model-file> 

BLOG_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ ! -d "${BLOG_HOME}/src/" ]; then
  BLOG_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )"/../ && pwd )"	
fi

export BLOG_HOME

if [ -e "${BLOG_HOME}/bin/iblog.scala" ]; then
  CPATH="${BLOG_HOME}/lib/java-cup-11b.jar:${BLOG_HOME}/lib/*:${CLASSPATH}"
else
	CPATH="${BLOG_HOME}/target/universal/stage/lib/java-cup-11b.jar:${BLOG_HOME}/target/universal/stage/lib/*:${CLASSPATH}"
fi

java -cp ${CPATH} blog.parse.Parse $@
