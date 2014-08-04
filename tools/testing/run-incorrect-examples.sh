#!/bin/bash

# There is a folder of BLOG Examples, located at $ERROR_EX_DIR
# which contains a list of BLOG files that should not run correctly
# (e.g. there is a missing semicolon, a matrix is not indexed correctly)
# This script runs all the files in $ERROR_EX_DIR with blog and asserts
# a non-zero exit code. All files that do not report a non-zero exit code
# are placed in tools/testing/output_incorrect/incorrect_examples.txt
ERROR_EX_DIR=tools/error-examples
INCORRECT_EX=tools/testing/output_incorrect/incorrect_examples.txt

rm -f $INCORRECT_EX
for f in $(find $ERROR_EX_DIR -name '*.blog'); do
   echo $f
   ./blog -n 100 $f 2> /dev/null 1> /dev/null
   if [ $? -eq 0 ]; then
       echo $f >> $INCORRECT_EX 
   fi
done	
