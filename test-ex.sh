#!/bin/bash

BLOG_HOME="."
export BLOG_HOME

J_PATH_SEP=`${BLOG_HOME}/path_sep.sh`

# Add additional test classes to this list
for CLASS in $(cat ${BLOG_HOME}/ex_test_classes)
do
    java -cp "${BLOG_HOME}/lib/commons-math3-3.0.jar${J_PATH_SEP}${BLOG_HOME}/bin${J_PATH_SEP}${BLOG_HOME}/lib/java_cup.jar${J_PATH_SEP}${BLOG_HOME}/lib/JFlex-1.4.3.jar$" $CLASS
    RET=$?
    if [ $RET != 0 ]
    then
	echo "Failed regression test for ${CLASS}"
	break
    fi
done