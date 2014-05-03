#!/bin/bash
#
# Script for running the BLOG inference engine.  Typical usage:
#
# runblog [options] <model-file> <evidence-file> <query-file>
#
# If your current working directory is the top-level BLOG directory
# (the one that contains the "blog" and "common" subdirectories), you can
# use this script without modification.  If you'd like to be able to run
# blog from another directory, edit this script so BLOG_HOME is set to
# your top-level BLOG directory.

if [ -d "lib" ] ; then
  BLOG_HOME="."
else
  BLOG_HOME=".."
fi
export BLOG_HOME

J_PATH_SEP=`${BLOG_HOME}/path_sep.sh`

if [ -d "${BLOG_HOME}/bin" ]; then
  CPATH="${BLOG_HOME}/bin${J_PATH_SEP}${BLOG_HOME}/*"
else 
  CPATH="${BLOG_HOME}/*"
fi

CPATH="${CPATH}${J_PATH_SEP}${BLOG_HOME}/lib/*"

java -cp ${CPATH} -Xmx2048M blog.Main -e blog.engine.ParticleFilter $@