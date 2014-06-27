#!/bin/bash

BLOG_HOME="."
export BLOG_HOME

J_PATH_SEP=`${BLOG_HOME}/path_sep.sh`

CLASSPATH="${BLOG_HOME}/lib/commons-math3-3.0.jar\
${J_PATH_SEP}${BLOG_HOME}/bin\
${J_PATH_SEP}${BLOG_HOME}/lib/Jama.jar\
${J_PATH_SEP}${BLOG_HOME}/lib/java-cup-11b.jar\
${J_PATH_SEP}${BLOG_HOME}/lib/jflex-1.6.0.jar\
${J_PATH_SEP}${BLOG_HOME}/lib/junit-4.10.jar"

# Specify classes to test at the command line.
# Example: ./test-junit.sh test.blog.MiscTest
java -cp $CLASSPATH org.junit.runner.JUnitCore $@
