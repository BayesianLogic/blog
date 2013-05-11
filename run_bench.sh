#!/bin/bash

BLOG_HOME="."
export BLOG_HOME

J_PATH_SEP=`${BLOG_HOME}/path_sep.sh`

if [ -d "${BLOG_HOME}/bin" ]; then
  CPATH="${BLOG_HOME}/bin${J_PATH_SEP}${BLOG_HOME}/*"
else 
	CPATH="${BLOG_HOME}/*"
fi

CPATH="${CPATH}${J_PATH_SEP}${BLOG_HOME}/lib/*"

java -cp ${CPATH} -Xmx2048M benchmark.blog.$@
