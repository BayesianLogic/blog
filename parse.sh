#!/bin/bash
#
# Script for running the BLOG parser.  Typical usage:
#
# ./parse.sh  <model-file> 

BLOG_HOME="."
export BLOG_HOME

J_PATH_SEP=`${BLOG_HOME}/path_sep.sh`

java -cp "${BLOG_HOME}/bin${J_PATH_SEP}${BLOG_HOME}/lib/java_cup.jar${J_PATH_SEP}${BLOG_HOME}/lib/JFlex-1.4.3.jar$" -Xmx2048M blog.parse.Parse $@