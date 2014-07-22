#!/bin/bash

# Runs all examples and produces the following files in the testing/ dir.
# Assume that this script is called via bash from within the root directory
#
# status.txt
# - A CSV of all Blog Examples alongside their status
# 
# successExamples.txt
# - A list of all BLOG examples that run successfully
# 
# errorExamples.txt
# - A list of all BLOG examples that throw exceptions
# 
# errors/*
# - A list of files that threw a Java Exception (non-empty Standard Error)
# - Each file as its content contains the Stack Trace

# Location of the directory where all testing output goes
testDir='tools/testing'
mkdir -p $testDir

# A list of all BLOG examples that throw exceptions
errorFiles="$testDir/errorExamples.txt"
echo "Error Examples" > $errorFiles

# A list of all BLOG examples that run successfully
successFiles="$testDir/successExamples.txt"
echo "Successful Examples" > $successFiles

# A list of all BLOG examples that thre exceptions alongside with their stack trace
fileErrors="$testDir/errors.txt"
echo "" > $fileErrors

# A list of whether the BLOG example runs correctly or not
statusFiles="$testDir/status.csv"
echo "FileName,Status" > $statusFiles

for f in $(find example -name '*.blog'); do
    echo "Running $f"
    ./blog -n 100 $f 2> tools/testing/errors > tools/testing/output
    errors=`cat tools/testing/errors | wc -l`
    if [ "$errors" == "0" ]; then
        echo "$f,Pass" >> $statusFiles
        echo "$f" >> $successFiles
    else
        echo "$f,Fail" >> $statusFiles
        echo "$f" >> $errorFiles
        echo "$f" >> $fileErrors
        echo $(cat tools/testing/errors) >> $fileErrors
        echo "" >> $fileErrors
    fi
done
rm tools/testing/errors tools/testing/output 

for f in $(find example -name '*.dblog'); do
    echo "Running $f"
    ./dblog -n 100 $f 2> tools/testing/errors > tools/testing/output
    errors=`cat tools/testing/errors | wc -l`
    if [ "$errors" == "0" ]; then
        echo "$f,Pass" >> $statusFiles
        echo "$f" >> $successFiles
    else
        echo "$f,Fail" >> $statusFiles
        echo "$f" >> $errorFiles
        echo "$f" >> $fileErrors
        echo $(cat tools/testing/errors) >> $fileErrors
        echo "" >> $fileErrors
    fi
done
rm tools/testing/errors tools/testing/output 
