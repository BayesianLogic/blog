#!/bin/bash

# There is a folder of BLOG Examples, located at $ERROR_EX_DIR
# which contains a list of BLOG files that should not run correctly
# (e.g. there is a missing semicolon, a matrix is not indexed correctly)
# This script runs all the files in $ERROR_EX_DIR with blog and asserts
# a non-zero exit code. All files that do not report a non-zero exit code
# are placed in tools/testing/output_incorrect/incorrect_examples.txt
ERROR_EX_DIR=tools/error-examples/current
INCORRECT_EX=tools/testing/output_incorrect/incorrect_examples.txt

rm -f $INCORRECT_EX
for f in $(find $ERROR_EX_DIR -name '*.blog'); do
   ./blog -n 100 $f 2> /dev/null 1> /dev/null
   if [ $? -eq 0 ]; then
       echo $f >> $INCORRECT_EX 
   fi
done	

if [ -e $INCORRECT_EX ]; then
   exit 1
fi

# --- Invalid Command Line Arguments ---
./blog &> /dev/null
if [ $? -eq 0 ]; then
    echo "No BLOG input file specified"
    exit 1
fi

# Nonexistent inference engine provided
./blog -e FooInference example/burglary.blog &> /dev/null
if [ $? -eq 0 ]; then
    echo "Running './blog -e FooInference example/burglary.blog' returns successfully" 
    exit 1
fi

# Randomized output producing identical results
./blog -rn 20000 example/burglary.blog > tools/testing/output_incorrect/ex1
./blog -rn 20000 example/burglary.blog > tools/testing/output_incorrect/ex2

diff tools/testing/output_incorrect/ex1 tools/testing/output_incorrect/ex2 &> /dev/null

if [ $? -eq 0 ]; then
    echo "Two randomized runs of burglary should most likely produce different results"
    exit 1
fi
