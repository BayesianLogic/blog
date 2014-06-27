#!/bin/bash

# A list of all BLOG examples that throw exceptions
errorFiles='testing/errorExamples.txt'
echo "Error Examples" > $errorFiles

# A list of all BLOG examples that run successfully
successFiles='testing/successExamples.txt'
echo "Successful Examples" > $successFiles

# A list of all BLOG examples that thre exceptions alongside with their stack trace
fileErrors='testing/errors.txt'
echo "" > $fileErrors

# A list of whether the BLOG example runs correctly or not
statusFiles='testing/status.csv'
echo "FileName,Status" > $statusFiles

for f in $(find ../example -name '*.blog'); do
    echo "Running $f"
    ../dblog -n 100 $f 2> testing/errors > testing/output
    errors=`cat testing/errors | wc -l`
    if [ "$errors" == "0" ]; then
        echo "$f,Pass" >> $statusFiles
        echo "$f" >> $successFiles
    else
        echo "$f,Fail" >> $statusFiles
        echo "$f" >> $errorFiles
        echo "$f" >> $fileErrors
        echo $(cat testing/errors) >> $fileErrors
        echo "" >> $fileErrors
    fi
done
rm testing/errors testing/output 

