#!/bin/bash

samples=$1
blogfile_fullpath=$2
trials=$3
# The output file for the results
output=$4
id=$5

if [ "$1" = "-h" ]; then
    echo "Usage of sample.sh"
    echo "sample.sh <samples> <input-file> <trials> <output-file> <id>"
    exit 0
fi

echo "File: $blogfile_fullpath"

mkdir -p tools/testing/output
LW=tools/testing/output/tmpLW$id
MH=tools/testing/output/tmpMH$id
rm -f $LW
rm -f $MH

for i in `seq 1 $trials`; do
    echo "Trial $i..."
    ./blog -r -n $samples -q $samples --interval $samples -s blog.sample.LWSampler $blogfile_fullpath >> $LW
    ./blog -r -n $samples -q $samples --interval $samples -s blog.sample.MHSampler $blogfile_fullpath >> $MH
done

echo "File:         $blogfile_fullpath" >> $output
echo "Trials:       $trials" >> $output
echo "Samples:      $samples" >> $output
echo "" >> $output
python tools/testing/sampling.py $id >> $output
echo "---------------------------------" >> $output

# Remove the temporary files
rm -f $LW
rm -f $MH
